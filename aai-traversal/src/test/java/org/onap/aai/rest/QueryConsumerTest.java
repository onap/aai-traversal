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
package org.onap.aai.rest;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.HttpTestUtil;
import org.onap.aai.PayloadUtil;
import org.onap.aai.TraversalApp;
import org.onap.aai.TraversalTestConfiguration;
import org.onap.aai.WebClientConfiguration;
import org.onap.aai.config.PropertyPasswordConfiguration;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.exceptions.AAIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = TraversalApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(initializers = PropertyPasswordConfiguration.class)
@Import({TraversalTestConfiguration.class, WebClientConfiguration.class})
public class QueryConsumerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryConsumerTest.class);
    private HttpTestUtil httpTestUtil;

    private String pserverUri;

    @Autowired WebTestClient webTestClient;

    @Autowired
    RestTemplate restTemplate;

    @LocalServerPort
    int randomPort;

    private HttpEntity httpEntity;

    private HttpHeaders headers;

    private String baseUrl;

    private String cloudRegionUri;

    @Before
    public void setup() throws Exception {

        headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");
        String authorization = Base64.getEncoder().encodeToString("AAI:AAI".getBytes("UTF-8"));
        headers.add("Authorization", "Basic " + authorization);
        baseUrl = "http://localhost:" + randomPort;
        httpTestUtil = new HttpTestUtil();
        addPserver();

    }

    private void addPserver() throws Exception, UnsupportedEncodingException, AAIException {
        String hostname = "test-" + UUID.randomUUID().toString();
        pserverUri = "/aai/v11/cloud-infrastructure/pservers/pserver/" + hostname;
        Map<String, String> pserverMap = new HashMap<>();
        pserverMap.put("hostname", hostname);
        String payload = PayloadUtil.getTemplatePayload("pserver.json", pserverMap);
        httpTestUtil.doPut(pserverUri, payload);
    }

    private void addCloudRegion(Map<String, String> cloudRegionMap, String cloudRegionUri)
        throws Exception, UnsupportedEncodingException, AAIException {
        String cloudRegionPayload =
            PayloadUtil.getTemplatePayload("cloud-region-with-vserver.json", cloudRegionMap);
        Response response = httpTestUtil.doPut(cloudRegionUri, cloudRegionPayload);
    }

    private void addComplex(Map<String, String> complexMap, String complexUri)
        throws Exception, UnsupportedEncodingException, AAIException {
        String complexPayload = PayloadUtil.getTemplatePayload("complex.json", complexMap);
        Response response = httpTestUtil.doPut(complexUri, complexPayload);
    }

    @Test
    public void testRequiredAGood() throws Exception {
        String endpoint = "/aai/v14/query?format=pathed";
        Map<String, String> cloudRegionMap = new HashMap<>();
        cloudRegionMap.put("cloud-owner", "test-owner-id1111");
        cloudRegionMap.put("cloud-region-id", "test-region-id1111");
        cloudRegionMap.put("tenant-id", "test-tenant-id1111");
        cloudRegionMap.put("tenant-name", "test-tenant-name-id1111");
        cloudRegionMap.put("vserver-id", "some-vserver-id-id1111");
        cloudRegionMap.put("vserver-name", "test-vserver-name-id1111");
        cloudRegionMap.put("pserver-uri", pserverUri);
        cloudRegionUri =
            "/aai/v14/cloud-infrastructure/cloud-regions/cloud-region/test-owner-id1111/test-region-id1111";
        addCloudRegion(cloudRegionMap, cloudRegionUri);

        Map<String, String> complexMap = new HashMap<>();
        complexMap.put("physical-location-id", "location-1111");
        complexMap.put("cloud-region-uri", cloudRegionUri);
        String complexUri = "/aai/v14/cloud-infrastructure/complexes/complex/location-1111";
        addComplex(complexMap, complexUri);

        Map<String, String> customQueryMap = new HashMap<>();

        customQueryMap.put("start", "cloud-infrastructure/cloud-regions");
        customQueryMap.put("query", "cloud-region-sites?owner=test-owner-id1111");

        String payload = PayloadUtil.getTemplatePayload("custom-query.json", customQueryMap);
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity<String> responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        LOGGER.info("Response of custom query : {}", responseEntity.getBody().toString());
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

        // assertThat(responseEntity.getBody().toString(),
        // containsString(customerUri2));
    }

    @Test
    public void thatPaginatedResponseCanBeRetrieved() throws Exception {
        Map<String, String> cloudRegionMap = new HashMap<>();
        cloudRegionMap.put("cloud-owner", "test-owner-id1111");
        cloudRegionMap.put("cloud-region-id", "test-region-id1111");
        cloudRegionMap.put("tenant-id", "test-tenant-id1111");
        cloudRegionMap.put("tenant-name", "test-tenant-name-id1111");
        cloudRegionMap.put("vserver-id", "some-vserver-id-id1111");
        cloudRegionMap.put("vserver-name", "test-vserver-name-id1111");
        cloudRegionMap.put("pserver-uri", pserverUri);
        cloudRegionUri =
            "/aai/v14/cloud-infrastructure/cloud-regions/cloud-region/test-owner-id1111/test-region-id1111";
        addCloudRegion(cloudRegionMap, cloudRegionUri);

        Map<String, String> complexMap = new HashMap<>();
        complexMap.put("physical-location-id", "location-1111");
        complexMap.put("cloud-region-uri", cloudRegionUri);
        String complexUri = "/aai/v14/cloud-infrastructure/complexes/complex/location-1111";
        addComplex(complexMap, complexUri);

        Map<String, String> customQueryMap = new HashMap<>();
        customQueryMap.put("start", "cloud-infrastructure/cloud-regions");
        customQueryMap.put("query", "cloud-region-sites?owner=test-owner-id1111");

        String payload = PayloadUtil.getTemplatePayload("custom-query.json", customQueryMap);
        String path = "/aai/v14/query";

        String response = webTestClient.put()
            .uri(uriBuilder -> uriBuilder
                .path(path)
                .queryParam("format", "console")
                .queryParam("resultIndex", 0)
                .queryParam("resultSize", 1)
                .build())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("total-results", 2)
            .expectHeader().valueEquals("total-pages", 2)
            .returnResult(String.class)
            .getResponseBody()
            .blockFirst();

        String expectedResponse = "";
        assertEquals(expectedResponse, response);

    }

    @Test
    public void testRequiredBad() throws Exception {
        String endpoint = "/aai/v14/query?format=pathed";
        Map<String, String> cloudRegionMap = new HashMap<>();
        cloudRegionMap.put("cloud-owner", "test-owner-id2222");
        cloudRegionMap.put("cloud-region-id", "test-region-id2222");
        cloudRegionMap.put("tenant-id", "test-tenant-id2222");
        cloudRegionMap.put("tenant-name", "test-tenant-name-id2222");
        cloudRegionMap.put("vserver-id", "some-vserver-id-id2222");
        cloudRegionMap.put("vserver-name", "test-vserver-name-id2222");
        cloudRegionMap.put("pserver-uri", pserverUri);
        cloudRegionUri =
            "/aai/v14/cloud-infrastructure/cloud-regions/cloud-region/test-owner-id2222/test-region-id2222";
        addCloudRegion(cloudRegionMap, cloudRegionUri);

        Map<String, String> customQueryMap = new HashMap<>();

        customQueryMap.put("start", "cloud-infrastructure/cloud-regions");
        customQueryMap.put("query", "cloud-region-sites?owner=test-owner-id2222&extra=extraParam");

        String payload = PayloadUtil.getTemplatePayload("custom-query.json", customQueryMap);
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        LOGGER.info("Response of custom query : {}", responseEntity.getBody().toString());
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));

        assertThat(responseEntity.getBody().toString(), containsString("3022"));
    }

    @After
    public void tearDown() {

        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        boolean success = true;
        try {
            GraphTraversalSource g = transaction.traversal();
            g.V().has("source-of-truth", "JUNIT").toList().forEach(v -> v.remove());

        } catch (Exception ex) {
            success = false;
            LOGGER.error("Unable to remove the vertexes", ex);
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
