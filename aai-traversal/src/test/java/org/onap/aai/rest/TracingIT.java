package org.onap.aai.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aai.TraversalApp;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import zipkin2.reporter.brave.AsyncZipkinSpanHandler;

public class TracingIT {

    private static final String TRACE_ID = "463ac35c9f6413ad48485a3953bb6124";
    private static final String PARENT_ID = "a2fb4a1d1a96d312";

    @BeforeClass
    public static void setupConfig() {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");
        assertFalse(ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
            .anyMatch(argument -> argument.startsWith("-javaagent:") && argument.contains("opentelemetry")));
    }

    @Test
    public void enabledTracingExportsZipkinAndPropagatesW3cAndB3() throws Exception {
        try (LoopbackCollector collector = new LoopbackCollector();
            ConfigurableApplicationContext context = application("--management.tracing.enabled=true",
                "--management.zipkin.tracing.endpoint=" + collector.address() + "/api/v2/spans")) {
            Tracer tracer = context.getBeanProvider(Tracer.class).getIfAvailable(() -> Tracer.NOOP);
            Propagator propagator = context.getBeanProvider(Propagator.class).getIfAvailable();
            for (String type : new String[] {"w3c", "b3"}) {
                Map<String, String> carrier = "w3c".equals(type)
                    ? Map.of("traceparent", "00-" + TRACE_ID + "-" + PARENT_ID + "-01")
                    : Map.of("b3", TRACE_ID + "-" + PARENT_ID + "-1");
                Span span = propagator == null ? tracer.nextSpan().name("traversal-" + type).start()
                    : propagator.extract(carrier, Map::get).name("traversal-" + type).start();
                try (Tracer.SpanInScope scope = tracer.withSpan(span)) {
                    assertEquals("ok", context.getBean(RestTemplateBuilder.class).build()
                        .getForObject(collector.address() + "/request", String.class));
                } finally {
                    span.end();
                }
                Map<String, String> headers = collector.headers.poll(5, TimeUnit.SECONDS);
                assertNotNull(headers);
                assertTrue(headers.toString(), headers.get("traceparent").startsWith("00-" + TRACE_ID + "-"));
                assertTrue(headers.toString(), headers.get("b3").startsWith(TRACE_ID + "-"));
            }
            assertEquals(1, context.getBeansOfType(Tracer.class).size());
            assertEquals(1, context.getBeansOfType(brave.Tracing.class).size());
            assertEquals(1, context.getBeansOfType(AsyncZipkinSpanHandler.class).size());
            context.getBean(AsyncZipkinSpanHandler.class).flush();
            ObjectMapper mapper = context.getBean(ObjectMapper.class);
            boolean w3cExported = false;
            boolean b3Exported = false;
            boolean clientExported = false;
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (!(w3cExported && b3Exported && clientExported) && System.nanoTime() < deadline) {
                String exported = collector.exports.poll(1, TimeUnit.SECONDS);
                if (exported == null) {
                    continue;
                }
                for (JsonNode exportedSpan : mapper.readTree(exported)) {
                    if (TRACE_ID.equals(exportedSpan.path("traceId").asText())) {
                        w3cExported |= "traversal-w3c".equals(exportedSpan.path("name").asText())
                            && PARENT_ID.equals(exportedSpan.path("parentId").asText());
                        b3Exported |= "traversal-b3".equals(exportedSpan.path("name").asText())
                            && PARENT_ID.equals(exportedSpan.path("parentId").asText());
                        clientExported |= "CLIENT".equals(exportedSpan.path("kind").asText());
                    }
                }
            }
            assertTrue("W3C parent must reach the real Zipkin endpoint", w3cExported);
            assertTrue("B3 parent must reach the real Zipkin endpoint", b3Exported);
            assertTrue("The production HTTP client must export a span", clientExported);
        }
    }

    @Test
    public void defaultTracingDoesNotCreateBraveOrExportOrTraceHeaders() throws Exception {
        try (LoopbackCollector collector = new LoopbackCollector();
            ConfigurableApplicationContext context = application(
                "--management.zipkin.tracing.endpoint=" + collector.address() + "/api/v2/spans")) {
            assertEquals("false", context.getEnvironment().getProperty("management.tracing.enabled"));
            assertTrue(context.getBeansOfType(brave.Tracing.class).isEmpty());
            assertTrue(context.getBeansOfType(AsyncZipkinSpanHandler.class).isEmpty());
            Tracer tracer = context.getBeanProvider(Tracer.class).getIfAvailable(() -> Tracer.NOOP);
            Span span = tracer.nextSpan().name("traversal-disabled").start();
            try (Tracer.SpanInScope scope = tracer.withSpan(span)) {
                assertEquals("ok", context.getBean(RestTemplateBuilder.class).build()
                    .getForObject(collector.address() + "/request", String.class));
            } finally {
                span.end();
            }
            assertEquals(Map.of("traceparent", "null", "b3", "null"), collector.headers.poll(5, TimeUnit.SECONDS));
            assertTrue(collector.exports.isEmpty());
        }
    }

    private ConfigurableApplicationContext application(String... tracingProperties) {
        String[] properties = {
            "--server.address=127.0.0.1", "--server.port=0", "--server.ssl.enabled=false",
            "--management.server.address=127.0.0.1", "--management.server.port=0",
            "--spring.profiles.active=production", "--aai.notifications.enabled=false",
            "--server.local.startpath=src/main/resources",
            "--aai.graph.properties.path=src/main/resources/etc/appprops/janusgraph-realtime.properties",
            "--schema.ingest.file=src/test/resources/application-test.properties"
        };
        String[] arguments = Arrays.copyOf(properties, properties.length + tracingProperties.length);
        System.arraycopy(tracingProperties, 0, arguments, properties.length, tracingProperties.length);
        return new SpringApplicationBuilder(TraversalApp.class).run(arguments);
    }

    private static class LoopbackCollector implements AutoCloseable {
        private final HttpServer server;
        private final BlockingQueue<Map<String, String>> headers = new LinkedBlockingQueue<>();
        private final BlockingQueue<String> exports = new LinkedBlockingQueue<>();

        LoopbackCollector() throws Exception {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/request", exchange -> {
                headers.add(Map.of("traceparent", String.valueOf(exchange.getRequestHeaders().getFirst("traceparent")),
                    "b3", String.valueOf(exchange.getRequestHeaders().getFirst("b3"))));
                byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(body);
                }
            });
            server.createContext("/api/v2/spans", exchange -> {
                try (var input = "gzip".equals(exchange.getRequestHeaders().getFirst("Content-Encoding"))
                    ? new GZIPInputStream(exchange.getRequestBody()) : exchange.getRequestBody()) {
                    exports.add(new String(input.readAllBytes(), StandardCharsets.UTF_8));
                }
                exchange.sendResponseHeaders(202, -1);
                exchange.close();
            });
            server.start();
        }

        String address() {
            return "http://127.0.0.1:" + server.getAddress().getPort();
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}