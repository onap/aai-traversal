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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.onap.aai.TraversalApp;
import org.onap.aai.TraversalTestConfiguration;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.restclient.PropertyPasswordConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import io.prometheus.client.exporter.common.TextFormat;

/**
 * Test REST requests against configuration resource
 */
@AutoConfigureMetrics
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(
    initializers = PropertyPasswordConfiguration.class,
    classes = {SpringContextAware.class})
@Import(TraversalTestConfiguration.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {SpringContextAware.class, TraversalApp.class})
public class ConfigurationTest extends AbstractSpringRestTest {
    @Autowired
    RestTemplate restTemplate;

    @Value("${local.management.port}")
    private int mgtPort;

    private HttpEntity<String> httpEntity;
    private String actuatorurl;
    private HttpHeaders headers;

    @Before
    public void setup() throws UnsupportedEncodingException {

        headers = new HttpHeaders();

        headers.set("Accept", "text/plain");
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");

        String authorization = Base64.getEncoder().encodeToString("AAI:AAI".getBytes("UTF-8"));
        headers.add("Authorization", "Basic " + authorization);

        httpEntity = new HttpEntity<String>(headers);
        baseUrl = "http://localhost:" + randomPort;
        actuatorurl = "http://localhost:" + mgtPort;
    }

    @Test
    public void TestManagementEndpointConfiguration() {
        ResponseEntity<String> responseEntity = null;
        String responseBody = null;

        // set Accept as text/plain in order to get access of endpoint "/actuator/prometheus"
        responseEntity = restTemplate.exchange(actuatorurl + "/actuator/prometheus", HttpMethod.GET,
            httpEntity, String.class);
        responseBody = responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        System.out.println("responseBody---------" + responseBody);
        assertFalse(responseBody.contains("aai_uri"));
        assertTrue(responseBody.contains("group_id"));

        // Set Accept as MediaType.APPLICATION_JSON in order to get access of endpoint
        // "/actuator/info" and "/actuator/health"
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpEntity = new HttpEntity<String>(headers);
        responseEntity = restTemplate.exchange(actuatorurl + "/actuator/info", HttpMethod.GET,
            httpEntity, String.class);
        responseBody = (String) responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseBody.contains("aai-traversal"));

        responseEntity = restTemplate.exchange(actuatorurl + "/actuator/health", HttpMethod.GET,
            httpEntity, String.class);
        responseBody = (String) responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseBody.contains("UP"));
    }
}
