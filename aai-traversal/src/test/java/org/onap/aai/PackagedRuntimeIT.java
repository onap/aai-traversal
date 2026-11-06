package org.onap.aai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PackagedRuntimeIT {

    @TempDir
    Path temporaryDirectory;

    private Path packagedJar() {
        Path jar = Path.of("target", "aai-traversal-1.19.0-SNAPSHOT-build", "lib",
            "aai-traversal-1.19.0-SNAPSHOT.jar").toAbsolutePath();
        assertTrue(Files.isRegularFile(jar), jar.toString());
        return jar;
    }

    @Test
    void graphClientsLoadFromShippedLibraries() throws Exception {
        List<URL> libraries = new ArrayList<>();
        try (JarFile jar = new JarFile(packagedJar().toFile())) {
            assertEquals("org.onap.aai.TraversalApp", jar.getManifest().getMainAttributes().getValue("Start-Class"));
            for (var entry : jar.stream().filter(entry -> entry.getName().startsWith("BOOT-INF/lib/")
                && entry.getName().endsWith(".jar")).toList()) {
                Path library = temporaryDirectory.resolve(Path.of(entry.getName()).getFileName());
                try (var input = jar.getInputStream(entry)) {
                    Files.copy(input, library);
                }
                libraries.add(library.toUri().toURL());
            }
        }
        assertTrue(!libraries.isEmpty());
        try (URLClassLoader loader = new URLClassLoader(libraries.toArray(URL[]::new),
            ClassLoader.getPlatformClassLoader())) {
            ClassLoader originalLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            try {
                loader.loadClass("io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory");
                loader.loadClass("org.apache.tinkerpop.gremlin.driver.Channelizer$WebSocketChannelizer")
                    .getDeclaredConstructors();
                Class<?> clusterType = loader.loadClass("org.apache.tinkerpop.gremlin.driver.Cluster");
                Object builder = clusterType.getMethod("build").invoke(null);
                Object cluster = builder.getClass().getMethod("create").invoke(builder);
                try {
                    loader.loadClass("com.datastax.oss.driver.api.core.CqlSession").getMethod("builder").invoke(null);
                } finally {
                    clusterType.getMethod("close").invoke(cluster);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(originalLoader);
            }
        }
    }

    @Test
    void packagedManagementAndQueriesKeepTheirHttpContracts() throws Exception {
        Path backendFixture = Path.of(org.janusgraph.diskstorage.inmemory.InMemoryStoreManager.class
            .getProtectionDomain().getCodeSource().getLocation().toURI()).toAbsolutePath();
        assertTrue(backendFixture.getFileName().toString().startsWith("janusgraph-inmemory-"));
        assertTrue(backendFixture.toString().endsWith(".jar"));
        Path log = Path.of("target", "packaged-runtime.log").toAbsolutePath();
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
            "-DAJSC_HOME=.", "-DBUNDLECONFIG_DIR=src/main/resources", "-Dloader.path=" + backendFixture,
            "-jar", packagedJar().toString(),
            "--server.address=127.0.0.1", "--server.port=0", "--server.ssl.enabled=false",
            "--management.server.address=127.0.0.1", "--management.server.port=0",
            "--logging.level.org.springframework.boot.tomcat=INFO",
            "--spring.profiles.active=production", "--aai.notifications.enabled=false",
            "--server.local.startpath=src/main/resources",
            "--aai.graph.properties.path=src/main/resources/etc/appprops/janusgraph-realtime.properties",
            "--schema.ingest.file=src/test/resources/application-test.properties")
            .redirectErrorStream(true).start();
        CompletableFuture<List<Integer>> started = new CompletableFuture<>();
        Thread outputReader = new Thread(() -> {
            List<Integer> ports = new ArrayList<>();
            Pattern portPattern = Pattern.compile("Tomcat started on port (\\d+)");
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter output = Files.newBufferedWriter(log)) {
                String line;
                while ((line = input.readLine()) != null) {
                    output.write(line);
                    output.newLine();
                    output.flush();
                    var matcher = portPattern.matcher(line);
                    if (matcher.find() && !ports.contains(Integer.valueOf(matcher.group(1)))) {
                        ports.add(Integer.valueOf(matcher.group(1)));
                    }
                    if (ports.size() == 2 && line.contains("Traversal Microservice Started")) {
                        started.complete(List.copyOf(ports));
                    }
                }
                if (!started.isDone()) {
                    started.completeExceptionally(new IllegalStateException("Packaged startup failed; see " + log));
                }
            } catch (Exception exception) {
                started.completeExceptionally(exception);
            }
        }, "traversal-packaged-output");
        outputReader.setDaemon(true);
        outputReader.start();
        try {
            List<Integer> ports = started.get(90, TimeUnit.SECONDS);
            assertTrue(!ports.get(0).equals(ports.get(1)));
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            HttpResponse<String> health = request(client, ports.get(1), "/actuator/health", null, null, "application/json");
            assertEquals(200, health.statusCode(), health.body());
            assertTrue(health.body().contains("UP"), health.body());
            HttpResponse<String> metrics = request(client, ports.get(1), "/actuator/prometheus", null, null, "text/plain");
            assertEquals(200, metrics.statusCode(), metrics.body());
            assertTrue(metrics.body().contains("group_id"), metrics.body());
            assertEquals(200, request(client, ports.get(0), "/aai/util/echo", null, null, "application/json").statusCode());
            String endpoint = "/aai/v32/query?format=unsupported";
            String payload = "{\"gremlin\":\"g.V()\"}";
            HttpResponse<String> denied = request(client, ports.get(0), endpoint, payload, null, "application/json");
            assertEquals(401, denied.statusCode(), denied.body());
            String authorization = "Basic " + Base64.getEncoder().encodeToString("AAI:AAI".getBytes(StandardCharsets.UTF_8));
            HttpResponse<String> error = request(client, ports.get(0), endpoint, payload, authorization, "application/json");
            assertEquals(400, error.statusCode(), error.body());
            var envelope = new com.fasterxml.jackson.databind.ObjectMapper().readTree(error.body());
            assertTrue(envelope.path("requestError").path("serviceException").path("messageId").asText()
                .startsWith("SVC"), error.body());
            HttpResponse<String> query = request(client, ports.get(0), "/aai/v32/query?format=pathed",
                "{\"gremlin\":\"g.V().has('hostname','packaged/identifier')\"}", authorization, "application/json");
            assertEquals(200, query.statusCode(), query.body());
            assertTrue(new com.fasterxml.jackson.databind.ObjectMapper().readTree(query.body()) != null, query.body());
        } finally {
            process.destroy();
            try {
                if (!process.waitFor(15, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    assertTrue(process.waitFor(15, TimeUnit.SECONDS));
                }
            } finally {
                outputReader.join(5000);
            }
            assertTrue(!outputReader.isAlive(), "Packaged output reader must terminate");
        }
    }

    private HttpResponse<String> request(HttpClient client, int port, String path, String body,
        String authorization, String accept) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .timeout(Duration.ofSeconds(15)).header("Accept", accept)
            .header("X-FromAppId", "JUNIT").header("X-TransactionId", "packaged-runtime")
            .header("Real-Time", "true");
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        if (body != null) {
            request.header("Content-Type", "application/json").PUT(HttpRequest.BodyPublishers.ofString(body));
        } else {
            request.GET();
        }
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}