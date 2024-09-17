/**
 * ============LICENSE_START==================================================
 * org.onap.aai
 * ===========================================================================
 * Copyright © 2017-2020 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
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
 * ============LICENSE_END====================================================
 */
package org.onap.aai.it.multitenancy;

import static org.junit.Assert.*;

import com.jayway.jsonpath.JsonPath;

import dasniko.testcontainers.keycloak.KeycloakContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.onap.aai.PayloadUtil;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.rest.AbstractSpringRestTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;

@Import(KeycloakTestConfiguration.class)
@TestPropertySource(locations = "classpath:it/application-keycloak-test.properties")
public class MultiTenancyIT extends AbstractSpringRestTest {

    @Autowired
    private KeycloakContainer keycloakContainer;
    @Autowired
    private RoleHandler roleHandler;
    @Autowired
    private KeycloakTestProperties properties;

    @Override
    public void createTestGraph() {
        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        boolean success = true;

        try {
            GraphTraversalSource g = transaction.traversal();

            g.addV().property("aai-node-type", "pnf").property("pnf-name", "test-pnf-name-01")
                .property("prov-status", "in_service").property("data-owner", "operator")
                .property("in-maint", false).property("source-of-truth", "JUNIT")
                .property("aai-uri", "/network/pnfs/pnf/test-pnf-name-01").next();

            g.addV().property("aai-node-type", "pnf").property("pnf-name", "test-pnf-name-02")
                .property("prov-status", "in_service").property("in-maint", false)
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/network/pnfs/pnf/test-pnf-name-02").next();

            g.addV().property("aai-node-type", "pnf").property("pnf-name", "test-pnf-name-03")
                .property("prov-status", "in_service").property("data-owner", "selector")
                .property("in-maint", false).property("source-of-truth", "JUNIT")
                .property("aai-uri", "/network/pnfs/pnf/test-pnf-name-03").next();

            g.addV().property("aai-node-type", "pnf").property("pnf-name", "test-pnf-name-04")
                .property("prov-status", "in_service").property("data-owner", "selector")
                .property("in-maint", false).property("source-of-truth", "JUNIT")
                .property("aai-uri", "/network/pnfs/pnf/test-pnf-name-04").next();

            g.addV().property("aai-node-type", "pnf").property("pnf-name", "test-pnf-name-05")
                .property("prov-status", "in_service").property("data-owner", "selector")
                .property("in-maint", false).property("source-of-truth", "JUNIT")
                .property("aai-uri", "/network/pnfs/pnf/test-pnf-name-05").next();
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

    @Test
    public void testDslQueryWithDataOwner() throws Exception {
        baseUrl = "http://localhost:" + randomPort;
        String endpoint = baseUrl + "/aai/v29/dsl?format=console";
        List<Object> queryResults = null;
        ResponseEntity responseEntity = null;

        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query", "pnf*('prov-status','in_service') ");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);

        // get pnf with ran (operator)
        String username = "ran", password = "ran";
        headers = this.getHeaders(username, password);
        httpEntity = new HttpEntity(payload, headers);
        responseEntity = restTemplate.exchange(endpoint, HttpMethod.PUT, httpEntity, String.class);
        queryResults = JsonPath.read(responseEntity.getBody().toString(), "$.results");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(queryResults.size(), 2);

        // get pnf with bob (operator_readOnly)
        username = "bob";
        password = "bob";
        headers = this.getHeaders(username, password);
        httpEntity = new HttpEntity(payload, headers);
        responseEntity = restTemplate.exchange(endpoint, HttpMethod.PUT, httpEntity, String.class);
        queryResults = JsonPath.read(responseEntity.getBody().toString(), "$.results");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(queryResults.size(), 2);

        // get pnf with ted (selector)
        username = "ted";
        password = "ted";
        headers = this.getHeaders(username, password);
        httpEntity = new HttpEntity(payload, headers);
        responseEntity = restTemplate.exchange(endpoint, HttpMethod.PUT, httpEntity, String.class);
        queryResults = JsonPath.read(responseEntity.getBody().toString(), "$.results");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(queryResults.size(), 4);

        // add role to ted and try to get pnf again
        roleHandler.addToUser(RoleHandler.OPERATOR, username);
        headers = this.getHeaders(username, password);
        httpEntity = new HttpEntity(payload, headers);
        responseEntity = restTemplate.exchange(endpoint, HttpMethod.PUT, httpEntity, String.class);
        queryResults = JsonPath.read(responseEntity.getBody().toString(), "$.results");
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(queryResults.size(), 5);
    }

    private HttpHeaders getHeaders(String username, String password) {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");
        headers.add("Authorization", "Bearer " + getStringToken(username, password));

        return headers;
    }

    private String getStringToken(String username, String password) {
        Keycloak keycloakClient = KeycloakBuilder.builder()
            .serverUrl(keycloakContainer.getAuthServerUrl()).realm(properties.realm)
            .clientId(properties.clientId).clientSecret(properties.clientSecret).username(username)
            .password(password).build();

        AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();
        assertNotNull(tokenResponse);
        return tokenResponse.getToken();
    }
}
