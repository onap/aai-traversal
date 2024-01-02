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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.AAISetup;
import org.onap.aai.HttpTestUtil;
import org.onap.aai.PayloadUtil;
import org.onap.aai.TraversalTestConfiguration;
import org.onap.aai.config.PropertyPasswordConfiguration;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.entities.AAIErrorResponse;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

@ContextConfiguration(initializers = PropertyPasswordConfiguration.class)
@Import(TraversalTestConfiguration.class)
public class GfpVserverDataStoredQueryTest extends AbstractSpringRestTest {

    @Autowired
    SchemaVersions schemaVersions;

    @Autowired
    protected RestTemplate restTemplate;

    private static final Logger logger =
        LoggerFactory.getLogger(GfpVserverDataStoredQueryTest.class);

    private HttpTestUtil httpTestUtil;

    private SchemaVersion version;
    private String cloudRegionUri;

    @Override
    public void createTestGraph() {
        version = schemaVersions.getDefaultVersion();

        cloudRegionUri = "/aai/" + version.toString() + "/cloud-infrastructure/cloud-regions/"
            + "cloud-region/testOwner1/testRegion1";
        httpTestUtil = new HttpTestUtil();

        Map<String, String> templateValues = new HashMap<>();

        templateValues.put("cloud-owner", "testOwner1");
        templateValues.put("cloud-region-id", "testRegion1");
        templateValues.put("tenant-id", "testTenant1");
        templateValues.put("tenant-name", "testTenantName1");
        templateValues.put("vserver-id", "testVserver1");
        templateValues.put("vserver-name", "junit-vservers");
        templateValues.put("interface-name", "testlInterfaceName1");
        templateValues.put("ipv4-address", "192.33.233.233");
        templateValues.put("ipv6-address", "2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        templateValues.put("vlan-interface", "vlan-interface1");

        String cloudRegionPayload;
        try {
            cloudRegionPayload = PayloadUtil.getTemplatePayload("cloud-region-with-linterface.json", templateValues);
        } catch (IOException e) {
            cloudRegionPayload = "";
        }

        Response response = null;
        try {
            response = httpTestUtil.doPut(cloudRegionUri, cloudRegionPayload);
        } catch (UnsupportedEncodingException | AAIException e) {}
        logger.info("Response status received {}", response.getEntity());

        assertNotNull("Expected the response to be not null", response);
        assertEquals("Expecting the cloud region to be created", 201, response.getStatus());
        logger.info("Successfully created the cloud region with linterface");
    }

    @Test
    public void testStoredQueryVerifyDoesNotThrowMethodTooLargeWhenLargeNumberOfStartingVertexes()
        throws Exception {

        // Add hundred thousand vserver vertexes to properly
        // test the scenario where the application was
        // failing with method too large
        String vservers = System.getProperty("perf.vservers.count", "1000");
        addVservers(Integer.parseInt(vservers));

        Map<String, String> templateValues = new HashMap<>();

        // Purposefully putting the filter to the testVserver1 as
        // since this is a junit test other junit tests could put
        // vserver and not properly clean up after the test
        // so doing this to ensure that this is testing against the particular vserver
        // as not to fail when another unit test decide to put vserver and not clean up
        templateValues.put("start", "nodes/vservers?vserver-name=junit-vservers");
        templateValues.put("query", "gfp-vserver-data");

        String payload = PayloadUtil.getTemplatePayload("custom-query.json", templateValues);
        String query = String.format("/aai/%s/query?format=resource_and_url", version.toString());

        httpEntity = new HttpEntity<String>(payload, headers);
        ResponseEntity<String> response =
            restTemplate.exchange(baseUrl + query, HttpMethod.PUT, httpEntity, String.class);

        String entity = response.getBody();
        assertEquals("Expected the response to be 200 but got this returned: "
            + response.getBody(), 200, response.getStatusCodeValue());
        List<String> urls = JsonPath.read(entity, "$.results[*].url");
        assertEquals("Expected the urls to be 3", 3, urls.size());
        removeVertexes();
    }

    @Test
    public void testStoredQueryWhenQueryDoesNotExistShouldReturnBadRequest() throws Exception {

        Map<String, String> templateValues = new HashMap<>();
        templateValues.put("start", "nodes/vservers");
        templateValues.put("query", "fake-query");
        String payload = PayloadUtil.getTemplatePayload("custom-query.json", templateValues);
        String query = String.format("/aai/%s/query?format=resource_and_url", version.toString());

        httpEntity = new HttpEntity<String>(payload, headers);
        ResponseEntity<AAIErrorResponse> response =
            restTemplate.exchange(baseUrl + query, HttpMethod.PUT, httpEntity, AAIErrorResponse.class);

        AAIErrorResponse entity = response.getBody();
        assertEquals("Expected the response to be 400 but got this returned: " + entity, 400,
            response.getStatusCodeValue());
        assertEquals("Query payload is invalid %1", entity.getRequestError().getServiceException().getVariables().get(1));
    }

    @Test
    public void testStoredQueryWhenStartFilterReturnsZeroVertexesItShouldHandleProperly()
        throws Exception {

        Map<String, String> templateValues = new HashMap<>();
        templateValues.put("start", "nodes/vservers?vserver-name=nonexistent-filter");
        templateValues.put("query", "gfp-vserver-data");

        String payload = PayloadUtil.getTemplatePayload("custom-query.json", templateValues);
        String query = String.format("/aai/%s/query?format=resource_and_url", version.toString());
        httpEntity = new HttpEntity<String>(payload, headers);
        ResponseEntity<AAIErrorResponse> response =
            restTemplate.exchange(baseUrl + query, HttpMethod.PUT, httpEntity, AAIErrorResponse.class);

        AAIErrorResponse entity = response.getBody();
        assertEquals("Expected the response to be 404 but got this returned: " + entity, 404,
            response.getStatusCodeValue());
        assertEquals("Node Not Found. Start URI returned no vertexes, please check the start URI", entity.getRequestError().getServiceException().getVariables().get(2));
    }

    @After
    public void tearDown() {
        removeVertexes();
    }

    private void removeVertexes() {

        JanusGraph JanusGraph = AAIGraph.getInstance().getGraph();
        JanusGraphTransaction transaction = JanusGraph.newTransaction();

        boolean success = true;

        try {
            GraphTraversalSource g = transaction.traversal();
            g.V().has("source-of-truth", "JUNIT").toList().stream()
                .forEach((vertex) -> vertex.remove());
        } catch (Exception ex) {
            success = false;
            logger.error("Unable to remove all of the junit vservers due to {}", ex);
        } finally {
            if (success) {
                transaction.commit();
            } else {
                transaction.rollback();
            }
        }

    }

    private void addVservers(int vserversCount) {

        JanusGraph JanusGraph = AAIGraph.getInstance().getGraph();
        JanusGraphTransaction transaction = JanusGraph.newTransaction();

        boolean success = true;

        try {

            GraphTraversalSource g = transaction.traversal();
            for (int index = 0; index < vserversCount; index++) {
                String randomVserverId = UUID.randomUUID().toString();
                g.addV().property("aai-node-type", "vserver")
                    .property("vserver-id", "random-" + randomVserverId)
                    .property("vserver-name", "junit-vservers").property("source-of-truth", "JUNIT")
                    .next();
            }

        } catch (Exception ex) {
            success = false;
            logger.error("Unable to add all of the vservers due to {}", ex);
        } finally {
            if (success) {
                transaction.commit();
            } else {
                transaction.rollback();
            }
        }
    }
}
