/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.fail;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.onap.aai.config.PropertyPasswordConfiguration;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.queryformats.Format;
import org.onap.aai.util.AAIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;

/**
 * A sample junit test using spring boot that provides the ability to spin
 * up the application from the junit layer and run rest requests against
 * SpringBootTest annotation with web environment requires which spring boot
 * class to load and the random port starts the application on a random port
 * and injects back into the application for the field with annotation LocalServerPort
 * <p>
 *
 * This can be used to potentially replace a lot of the fitnesse tests since
 * they will be testing against the same thing except fitnesse uses hbase
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = TraversalApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(initializers = PropertyPasswordConfiguration.class)
@Import(TraversalTestConfiguration.class)
public class AAIGremlinQueryTest {

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    private static final Logger logger = LoggerFactory.getLogger(AAIGremlinQueryTest.class);

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    RestTemplate restTemplate;

    @LocalServerPort
    int randomPort;

    private HttpEntity httpEntity;

    private HttpHeaders headers;

    private String baseUrl;

    @BeforeClass
    public static void setupConfig() throws AAIException {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");
        AAIConfig.init();
    }

    public void createGraph() {

        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();

        boolean success = true;

        try {

            GraphTraversalSource g = transaction.traversal();

            g.addV().property("aai-node-type", "pserver").property("hostname", "test-pserver")
                .property("in-maint", false).property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver").next();

        } catch (Exception ex) {
            success = false;
        } finally {
            if (success) {
                transaction.commit();
            } else {
                transaction.rollback();
                fail("Unable to setup the graph");
            }
        }
    }

    @Before
    public void setup() throws Exception {

        AAIConfig.init();
        AAIGraph.getInstance();

        createGraph();
        headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");

        String authorization = Base64.getEncoder().encodeToString("AAI:AAI".getBytes("UTF-8"));
        headers.add("Authorization", "Basic " + authorization);

        baseUrl = "http://localhost:" + randomPort;
    }

    @Test
    public void testPserverCountUsingGremlin() throws Exception {
        Map<String, String> gremlinQueryMap = new HashMap<>();
        gremlinQueryMap.put("gremlin-query", "g.V().has('hostname', 'test-pserver').count()");

        String payload = PayloadUtil.getTemplatePayload("gremlin-query.json", gremlinQueryMap);

        ResponseEntity responseEntity = null;

        String endpoint = "/aai/v11/query?format=console";

        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

        String result = JsonPath.read(responseEntity.getBody().toString(), "$.results[0].result");
        assertThat(result, is("1"));

    }

    @Test
    public void testPserverCountUsingGremlinReturnsJsonWhenAcceptIsMissing() throws Exception {

        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "");
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");
        Map<String, String> gremlinQueryMap = new HashMap<>();
        gremlinQueryMap.put("gremlin-query", "g.V().has('hostname', 'test-pserver').count()");

        String payload = PayloadUtil.getTemplatePayload("gremlin-query.json", gremlinQueryMap);

        ResponseEntity responseEntity = null;

        String endpoint = "/aai/v11/query?format=console";

        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

        String result = JsonPath.read(responseEntity.getBody().toString(), "$.results[0].result");
        assertThat(result, is("1"));
    }

    @Test
    public void testPserverGremlinFormatsWithXmlResponse() throws Exception {

        Map<String, String> gremlinQueryMap = new HashMap<>();
        gremlinQueryMap.put("gremlin-query", "g.V().has('hostname', 'test-pserver')");

        String payload = PayloadUtil.getTemplatePayload("gremlin-query.json", gremlinQueryMap);

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        httpEntity = new HttpEntity(payload, headers);
        String endpoint = "/aai/v11/query?format=count";
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().toString(),
            containsString("<results><result><pserver>1</pserver></result></results>"));

        gremlinQueryMap.put("gremlin-query", "g.V().has('hostname', 'test-pserver')");
        payload = PayloadUtil.getTemplatePayload("gremlin-query.json", gremlinQueryMap);
        httpEntity = new HttpEntity(payload, headers);

        Format[] formats = new Format[] {Format.graphson, Format.pathed, Format.id, Format.resource,
            Format.simple, Format.resource_and_url, Format.console, Format.raw, Format.count};

        for (Format format : formats) {

            endpoint = "/aai/v11/query?format=" + format.toString();

            logger.debug("Current endpoint being executed {}", endpoint);
            responseEntity =
                restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
            assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

            String responseBody = responseEntity.getBody().toString();
            logger.debug("Response from the gremlin query: {}", responseBody);
            assertThat(responseBody, containsString("<results><result>"));
            assertThat(responseBody, is(not(containsString("<result><result>"))));
        }
    }

    @Test
    public void testPserverCountUsingDsl() throws Exception {
        Map<String, String> dslQuerymap = new HashMap<>();
        dslQuerymap.put("dsl-query", "pserver*('hostname', 'test-pserver')");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQuerymap);

        ResponseEntity responseEntity;

        String endpoint = "/aai/v11/dsl?format=console";

        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

        String result = JsonPath.read(responseEntity.getBody().toString(), "$.results[0].result");
        assertThat(result, containsString("v["));
    }

    @Test
    public void testPserverDslFormatsWithXmlResponse() throws Exception {

        Map<String, String> dslQuerymap = new HashMap<>();
        dslQuerymap.put("dsl-query", "pserver*('hostname', 'test-pserver')");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQuerymap);

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        httpEntity = new HttpEntity(payload, headers);
        String endpoint = "/aai/v11/dsl?format=count";
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().toString(),
            containsString("<results><result><pserver>1</pserver></result></results>"));

        httpEntity = new HttpEntity(payload, headers);

        Format[] formats = new Format[] {Format.graphson, Format.pathed, Format.id, Format.resource,
            Format.simple, Format.resource_and_url, Format.console, Format.raw, Format.count};

        for (Format format : formats) {

            endpoint = "/aai/v11/dsl?format=" + format.toString();

            logger.debug("Current endpoint being executed {}", endpoint);
            responseEntity =
                restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
            assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

            String responseBody = responseEntity.getBody().toString();
            logger.debug("Response from the dsl query: {}", responseBody);
            assertThat(responseBody, containsString("<results><result>"));
            assertThat(responseBody, is(not(containsString("<result><result>"))));
        }
    }

    @After
    public void tearDown() {

        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        boolean success = true;

        try {

            GraphTraversalSource g = transaction.traversal();

            g.V().has("source-of-truth", "JUNIT").toList().forEach(Vertex::remove);

        } catch (Exception ex) {
            success = false;
        } finally {
            if (success) {
                transaction.commit();
            } else {
                transaction.rollback();
                fail("Unable to teardown the graph");
            }
        }

    }
}
