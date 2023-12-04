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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.Assert;
import org.junit.Test;
import org.onap.aai.PayloadUtil;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.util.AAIConfig;
import org.onap.aai.util.TraversalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DslConsumerTest extends AbstractSpringRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DslConsumerTest.class);

    @Override
    public void createTestGraph() {
        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        boolean success = true;
        try {
            GraphTraversalSource g = transaction.traversal();
            Vertex p1 = g.addV().property("aai-node-type", "pserver")
                .property("hostname", "test-pserver-dsl").property("in-maint", false)
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver-dsl")
                .next();
            Vertex p2 = g.addV().property("aai-node-type", "pserver")
                .property("hostname", "test-pserver-dsl-02").property("in-maint", false)
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver-dsl-02")
                .next();
            Vertex p3 = g.addV().property("aai-node-type", "pserver")
                .property("hostname", "test-pserver-dsl-03").property("in-maint", false)
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver-dsl-03")
                .next();
            Vertex p4 = g.addV().property("aai-node-type", "pserver")
                .property("hostname", "test-pserver-dsl-04").property("in-maint", false)
                .property("source-of-truth", "JUNIT").property("number-of-cpus", 364)
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver-dsl-04")
                .next();
            Vertex c1 = g.addV().property("aai-node-type", "complex")
                .property("physical-location-id", "test-complex-dsl").property("state", "NJ")
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/complexes/complex/test-complex-dsl")
                .next();
            Vertex cr1 = g.addV().property("aai-node-type", "cloud-region")
                .property("cloud-owner", "test-cloud-owner-01")
                .property("cloud-region-id", "test-cloud-region-id-01")
                .property("source-of-truth", "JUNIT")
                .property("aai-uri",
                    "/cloud-infrastructure/cloud-regions/cloud-region/test-cloud-owner-01/test-cloud-region-id-01")
                .next();
            Vertex pnf01 =
                g.addV().property("aai-node-type", "pnf").property("pnf-name", "test-pnf-name-01")
                    .property("in-maint", false).property("source-of-truth", "JUNIT")
                    .property("aai-uri", "/network/pnfs/pnf/test-pnf-name-01").next();
            Vertex vserver2 = g.addV().property("aai-node-type", "vserver")
                .property("vserver-id", "test-vserver-id-2")
                .property("vserver-name", "test-vserver-name-2").property("in-maint", "false")
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/vservers/vserver/test-vserver-id-2").next();
            Vertex tenant2 = g.addV().property("aai-node-type", "tenant")
                .property("tenant-id", "test-tenant-id-2")
                .property("tenant-name", "test-tenant-name-2").property("source-of-truth", "JUNIT")
                .property("aai-uri", "/tenants/tenant/test-tenant-id-2").next();
            Vertex linterface2 = g.addV().property("aai-node-type", "l-interface")
                .property("interface-name", "test-interface-name-02").property("priority", "123")
                .property("is-port-mirrored", "true").property("source-of-truth", "JUNIT")
                .property("aai-uri", "/l-interfaces/l-interface/test-interface-name-02").next();
            Vertex oamNetwork2 = g.addV().property("aai-node-type", "oam-network")
                .property("network-uuid", "test-network-uuid-02")
                .property("network-name", "test-network-name-02").property("cvlan-tag", "456")
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/oam-networks/oam-network/test-network-uuid-02").next();
            Vertex cr2 = g.addV().property("aai-node-type", "cloud-region")
                .property("cloud-owner", "test-cloud-owner-02")
                .property("cloud-region-id", "test-cloud-region-id-02")
                .property("source-of-truth", "JUNIT")
                .property("aai-uri",
                    "/cloud-infrastructure/cloud-regions/cloud-region/test-cloud-owner-02/test-cloud-region-id-02")
                .next();

            // For adding edges, check the dbedgetules and the property from and to node
            // along with the other properties to populate information
            p1.addEdge("org.onap.relationships.inventory.LocatedIn", c1, "private", false,
                "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v", "NONE",
                "default", true);
            p1.addEdge("org.onap.relationships.inventory.LocatedIn", cr1, "private", false,
                "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v", "NONE",
                "default", true);
            p3.addEdge("org.onap.relationships.inventory.LocatedIn", c1, "private", false,
                "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v", "NONE",
                "default", true);
            p4.addEdge("org.onap.relationships.inventory.LocatedIn", c1, "private", false,
                "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v", "NONE",
                "default", true);
            tenant2.addEdge("org.onap.relationships.inventory.BelongsTo", cr2, "private", false,
                "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v", "NONE",
                "default", true);
            vserver2.addEdge("org.onap.relationships.inventory.BelongsTo", tenant2, "private",
                false, "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v",
                "NONE", "default", true);
            linterface2.addEdge("tosca.relationships.network.BindsTo", vserver2, "direction", "OUT",
                "multiplicity", "MANY2ONE", "contains-other-v", "!OUT", "delete-other-v", "!OUT",
                "prevent-delete", "NONE", "default", true);
            oamNetwork2.addEdge("org.onap.relationships.inventory.BelongsTo", cr2, "direction",
                "OUT", "multiplicity", "MANY2ONE", "contains-other-v", "!OUT", "delete-other-v",
                "NONE", "prevent-delete", "!OUT", "default", true);

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
    public void testDslQuery() throws Exception {

        String endpoint = "/aai/v14/dsl?format=console";
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query", "pserver*('hostname','test-pserver-dsl')");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        headers.add("X-Dsl-Version", "V1");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint,
            responseEntity.getBody());
        System.out.println(responseEntity.getBody());
        assertNotNull("Response from /aai/v14/dsl is not null", responseEntity);
        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint,
            responseEntity.getBody());
        assertNotNull("Response from /aai/v14/dsl is not null", responseEntity);
        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());

        // Make sure that there are no two result <result><result>
        assertThat(responseEntity.getBody().toString(),
            is(not(containsString("<result><result>"))));
        assertThat(responseEntity.getBody().toString(), is(containsString("<results><result>")));
    }

    @Test
    public void testDslQueryV2() throws Exception {

        String endpoint = "/aai/v14/dsl?format=console";
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query", "pserver*('hostname','test-pserver-dsl') > complex*");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        headers.add("X-Dsl-Version", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint,
            responseEntity.getBody());
        assertNotNull("Response from /aai/v14/dsl is not null", responseEntity);
        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint,
            responseEntity.getBody());
        assertNotNull("Response from /aai/v14/dsl is not null", responseEntity);
        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());

        // Make sure that there are no two result <result><result>
        assertThat(responseEntity.getBody().toString(),
            is(not(containsString("<result><result>"))));
        assertThat(responseEntity.getBody().toString(), is(containsString("<results><result>")));
    }

    @Test
    public void testDslQueryV2Aggregate() throws Exception {
        String endpoint = "/aai/v17/dsl?format=aggregate";
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query", "pserver*('hostname','test-pserver-dsl')");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        System.out.println("Payload" + payload);
        headers.add("X-Dsl-Version", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint,
            responseEntity.getBody());
        System.out.println(responseEntity.getBody());
        assertNotNull("Response from /aai/v17/dsl is not null", responseEntity);
        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());
    }

    @Test
    public void testDslQueryException() throws Exception {
        Map<String, String> dslQuerymap = new HashMap<>();
        dslQuerymap.put("dsl-query", "xserver");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQuerymap);

        ResponseEntity responseEntity = null;

        String endpoint = "/aai/v11/dsl?format=console";

        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        assertEquals("Expected the response to be 404", HttpStatus.BAD_REQUEST,
            responseEntity.getStatusCode());
    }

    @Test
    public void testDslQueryOverride() throws Exception {
        Map<String, String> dslQuerymap = new HashMap<>();
        dslQuerymap.put("dsl-query", "pserver*");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQuerymap);

        ResponseEntity responseEntity = null;

        String endpoint = "/aai/v11/dsl?format=console";

        headers.add("X-DslOverride", AAIConfig.get(TraversalConstants.DSL_OVERRIDE));
        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        assertEquals("Expected the response to be 404", HttpStatus.BAD_REQUEST,
            responseEntity.getStatusCode());
    }

    @Test
    public void testSelectedPropertiesNotRequiredOnDSLStartNode() throws Exception {
        Map<String, String> dslQuerymap = new HashMap<>();
        dslQuerymap.put("dsl-query", "pserver*('equip-model','abc')");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQuerymap);

        String endpoint = "/aai/v11/dsl?format=console";

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);

        assertEquals("Expected the response to be " + HttpStatus.OK, HttpStatus.OK,
            responseEntity.getStatusCode());
    }

    @Test
    public void testAPropertyIsRequiredOnDSLStartNode() throws Exception {
        Map<String, String> dslQuerymap = new HashMap<>();
        dslQuerymap.put("dsl-query", "pserver*");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQuerymap);

        String endpoint = "/aai/v11/dsl?format=console";

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);

        assertEquals("Expected the response to be " + HttpStatus.BAD_REQUEST,
            HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void testDslQueryProcessingV2_WithSimpleFormat_WithAsTreeQueryParameter()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=simple&depth=0&nodesOnly=true&as-tree=true";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
        JsonObject properties = resultsValue.get("properties").getAsJsonObject();
        assertEquals(2, properties.size());
        assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
        assertTrue(properties.get("in-maint").toString().equals("false"));
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryProcessingV2_WithSimpleFormat_WithoutAsTreeQueryParameter()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=simple&depth=0&nodesOnly=true";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
        JsonObject properties = resultsValue.get("properties").getAsJsonObject();
        assertEquals(2, properties.size());
        assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
        assertTrue(properties.get("in-maint").toString().equals("false"));
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryProcessingV2_WithResourceFormat_WithAsTreeQueryParameter()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=resource&depth=0&nodesOnly=true&as-tree=true";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
        JsonObject properties = resultsValue.get("pserver").getAsJsonObject();
        assertEquals(2, properties.size());
        assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
        assertTrue(properties.get("in-maint").toString().equals("false"));
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryProcessingV2_WithResourceFormat_WithoutAsTreeQueryParameter()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=resource&depth=0&nodesOnly=true";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
        JsonObject properties = resultsValue.get("pserver").getAsJsonObject();
        assertEquals(2, properties.size());
        assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
        assertTrue(properties.get("in-maint").toString().equals("false"));
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryProcessingV2_WithResourceAndUrlFormat_WithAsTreeQueryParameter()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint =
            "/aai/v16/dsl?format=resource_and_url&depth=0&nodesOnly=true&as-tree=true";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
        JsonObject properties = resultsValue.get("pserver").getAsJsonObject();
        assertEquals(2, properties.size());
        assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
        assertTrue(properties.get("in-maint").toString().equals("false"));
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryProcessingV2_WithResourceAndUrlFormat_WithoutAsTreeQueryParameter()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=resource_and_url&depth=0&nodesOnly=true";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
        JsonObject properties = resultsValue.get("pserver").getAsJsonObject();
        assertEquals(2, properties.size());
        assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
        assertTrue(properties.get("in-maint").toString().equals("false"));
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryTestAggregateFormatLastNodeNotSelectedAndNotReturned()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "pserver{'hostname'}('hostname','test-pserver-dsl') > complex");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=aggregate";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
        assertNull(
            resultsValue.get("/aai/v16/cloud-infrastructure/complexes/complex/test-complex-dsl")); // assert
                                                                                                   // complex
                                                                                                   // is
                                                                                                   // not
                                                                                                   // returned
                                                                                                   // since
                                                                                                   // it
                                                                                                   // is
                                                                                                   // not
                                                                                                   // selected
        JsonObject properties =
            resultsValue.get("/aai/v16/cloud-infrastructure/pservers/pserver/test-pserver-dsl")
                .getAsJsonObject().get("properties").getAsJsonObject();
        assertEquals(1, properties.size());
        assertThat(properties.get("hostname").toString(), is("\"test-pserver-dsl\"")); // assert
                                                                                       // only
                                                                                       // hostname
                                                                                       // is
                                                                                       // selected
        assertNull(properties.get("in-maint")); // assert that in-maint is not returned in the
                                                // properties list
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryTestAggregateFormatLastNodeSelectedAndReturned() throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "pserver{'hostname'}('hostname','test-pserver-dsl') > complex*");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=aggregate";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        JsonArray resultsValue = resultsArray.get(0).getAsJsonArray();
        assertNotNull(resultsValue.get(1).getAsJsonObject()
            .get("/aai/v16/cloud-infrastructure/complexes/complex/test-complex-dsl")); // assert
                                                                                       // complex is
                                                                                       // returned
                                                                                       // since it
                                                                                       // is
                                                                                       // selected
        JsonObject properties = resultsValue.get(0).getAsJsonObject()
            .get("/aai/v16/cloud-infrastructure/pservers/pserver/test-pserver-dsl")
            .getAsJsonObject().get("properties").getAsJsonObject();
        assertEquals(1, properties.size());
        assertThat(properties.get("hostname").toString(), is("\"test-pserver-dsl\"")); // verify
                                                                                       // that only
                                                                                       // selected
                                                                                       // attribute
                                                                                       // (hostname)
                                                                                       // is
                                                                                       // displayed
        assertNull(properties.get("in-maint")); // assert that in-maint is not returned in the
                                                // properties list
        JsonObject complexProperties = resultsValue.get(1).getAsJsonObject()
            .get("/aai/v16/cloud-infrastructure/complexes/complex/test-complex-dsl")
            .getAsJsonObject().get("properties").getAsJsonObject();
        assertEquals(2, complexProperties.size()); // internal properties like source-of-truth,
                                                   // node-type and aai-uri are not returned.
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryTestAggregateFormatInternalPropsNotReturned() throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query", "pserver*('hostname','test-pserver-dsl')");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=aggregate";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        JsonObject properties = resultsArray.get(0).getAsJsonObject()
            .get("/aai/v16/cloud-infrastructure/pservers/pserver/test-pserver-dsl")
            .getAsJsonObject().get("properties").getAsJsonObject();
        assertEquals(2, properties.size());
        assertThat(properties.get("hostname").toString(), is("\"test-pserver-dsl\"")); // verify
                                                                                       // that only
                                                                                       // hostname
                                                                                       // is
                                                                                       // displayed
        assertNull(properties.get("source-of-truth")); // assert that source-of-truth is not
                                                       // returned in properties list
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryTestWithMultipleWheres() throws Exception {
        // Return pservers where pserver has edge to complex "AND" same pserver also has an edge to
        // cloud-region
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "pserver*('hostname','test-pserver-dsl')(> complex)(> cloud-region)");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v18/dsl?format=aggregate";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        assertEquals(1, resultsArray.size()); // Returns only test-pserver-dsl, does not return
                                              // test-pserver-dsl-03 since it does not have an edge
                                              // to cloud-region
        assertEquals(null, resultsArray.get(0).getAsJsonObject()
            .get("/aai/v18/cloud-infrastructure/pservers/pserver/test-pserver-dsl-03")); // not
                                                                                         // returned
        assertNotNull(resultsArray.get(0).getAsJsonObject()
            .get("/aai/v18/cloud-infrastructure/pservers/pserver/test-pserver-dsl"));
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryTestWithMultipleWhereNots() throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query", "pserver*('hostname')!(> complex)!(> cloud-region)");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v18/dsl?format=aggregate";

        // Add header with V2 to use the {} feature as a part of dsl query
        headers.add("X-DslApiVersion", "V2");
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        assertEquals(1, resultsArray.size()); // Returns only test-pserver-dsl-02
        assertEquals(null, resultsArray.get(0).getAsJsonObject()
            .get("/aai/v18/cloud-infrastructure/pservers/pserver/test-pserver-dsl")); // not
                                                                                      // returned
        assertNotNull(resultsArray.get(0).getAsJsonObject()
            .get("/aai/v18/cloud-infrastructure/pservers/pserver/test-pserver-dsl-02"));
        headers.remove("X-DslApiVersion");
    }

    @Test
    public void testDslQueryProcessing_ExpectedError_WrongDataType() throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query", "pserver*('number-of-cpus','test')");

        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=simple";

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        Assert.assertTrue(responseString.contains(
            "Value ['test'] is not an instance of the expected data type for property key ['number-of-cpus'] and cannot be converted. "
                + "Expected: class java.lang.Integer, found: class java.lang.String"));
    }

    @Test
    public void testDslQueryOnComplex_WithResourceFormatWithUnionAsStartNode_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "[complex*('source-of-truth', 'JUNIT'), complex*('aai-uri', '/cloud-infrastructure/complexes/complex/test-complex-dsl')]");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=resource";

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        JsonObject resultValue = resultsArray.get(0).getAsJsonObject();
        JsonObject complex = resultValue.get("complex").getAsJsonObject();
        Assert.assertEquals("\"test-complex-dsl\"", complex.get("physical-location-id").toString());
    }

    @Test
    public void testDslQueryOnPserver_WithResourceFormatWithUnionAsStartNode_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "[pserver*('hostname','test-pserver-dsl'), complex*('physical-location-id', 'test-complex-dsl')]");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=resource";

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Extract the properties array from the response and compare in assert statements
        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        for (JsonElement je : resultsArray) {
            JsonObject jo = je.getAsJsonObject();
            if (jo.get("complex") != null) {
                Assert.assertEquals("\"test-complex-dsl\"",
                    jo.get("complex").getAsJsonObject().get("physical-location-id").toString());
            } else if (jo.get("pserver") != null) {
                Assert.assertEquals("\"test-pserver-dsl\"",
                    jo.get("pserver").getAsJsonObject().get("hostname").toString());
            } else {
                Assert.fail();
            }
        }
    }

    @Test
    public void testDslQueryOnNodesWithEdges_WithResourceFormatWithUnionAsStartNode_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "[pserver*('hostname','test-pserver-dsl-02'), pserver*('hostname','test-pserver-dsl')>complex*, pnf('pnf-name','pnf-name-noResults')>lag-interface>l-interface] > complex*");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=resource";

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString(); // pnf should have no results

        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        boolean hasPserver1 = false, hasPserver2 = false;
        for (JsonElement je : resultsArray) {
            JsonObject jo = je.getAsJsonObject();
            if (jo.get("complex") != null) {
                Assert.assertEquals("\"test-complex-dsl\"",
                    jo.get("complex").getAsJsonObject().get("physical-location-id").toString());
            } else if (jo.get("pserver") != null) {
                if (jo.get("pserver").getAsJsonObject().get("hostname").toString()
                    .equals("\"test-pserver-dsl\"")) {
                    hasPserver1 = true;
                }
                if (jo.get("pserver").getAsJsonObject().get("hostname").toString()
                    .equals("\"test-pserver-dsl-02\"")) {
                    hasPserver2 = true;
                }
            } else {
                Assert.fail();
            }
        }
        Assert.assertTrue(hasPserver1 && hasPserver2);
    }

    @Test
    public void testDslQueryOnNodesWithEdges2_WithResourceFormatWithUnionAsStartNode_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "[pnf*('pnf-name','test-pnf-name-01'),pserver(>cloud-region*('cloud-owner','test-cloud-owner-01'))]");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        String endpoint = "/aai/v16/dsl?format=resource";

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        JsonObject results = JsonParser.parseString(responseString).getAsJsonObject();
        JsonArray resultsArray = results.get("results").getAsJsonArray();
        for (JsonElement je : resultsArray) {
            JsonObject jo = je.getAsJsonObject();
            if (jo.get("pnf") != null) {
                Assert.assertEquals("\"test-pnf-name-01\"",
                    jo.get("pnf").getAsJsonObject().get("pnf-name").toString());
            } else if (jo.get("cloud-region") != null) {
                Assert.assertEquals("\"test-cloud-owner-01\"",
                    jo.get("cloud-region").getAsJsonObject().get("cloud-owner").toString());
            } else {
                Assert.fail();
            }
        }
    }

    @Test
    public void testDslQuery_IsAgnosticWithBooleanPropertyAsString_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        String endpoint = "/aai/v19/dsl?format=resource&nodesOnly=true";

        // With expected boolean value "false" as a boolean value (no quotes)
        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', 1)");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Confirm that the vserver was returned in the response
        Assert.assertTrue(responseString.contains("\"vserver-id\":\"test-vserver-id-2\""));

        dslQueryMap.remove("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', 1)");

        // With expected boolean value of in-maint, "false", in string form (with single quotes)
        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', 'false')");
        payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);

        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        responseString = responseEntity.getBody().toString();
        // Confirm that the vserver was returned in the response
        Assert.assertTrue(responseString.contains("\"vserver-id\":\"test-vserver-id-2\""));
    }

    @Test
    public void testDslQuery_IsAgnosticWithWrongBooleanPropertyAsString_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        String endpoint = "/aai/v19/dsl?format=resource&nodesOnly=true";

        // all string values not boolean related default to false
        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', 'bogusBoolean')>l-interface*('priority', 123)");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Confirm that the l-interface was returned in the response
        Assert.assertTrue(responseString.contains("\"interface-name\":\"test-interface-name-02\""));
    }

    @Test
    public void testDslQuery_IsAgnosticWithBooleanPropertyAsInteger0_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        String endpoint = "/aai/v19/dsl?format=resource&nodesOnly=true";

        // 0 is false, should return value
        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', 0)>l-interface*('priority', 123)");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Confirm that the l-interface was returned in the response
        Assert.assertTrue(responseString.contains("\"interface-name\":\"test-interface-name-02\""));
    }

    @Test
    public void testDslQuery_IsAgnosticWithBooleanPropertyAsInteger1_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        String endpoint = "/aai/v19/dsl?format=resource&nodesOnly=true";

        // 0 is false, should return value
        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', 1)>l-interface*('priority', 123)");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Confirm that the l-interface was returned in the response
        Assert
            .assertTrue(!responseString.contains("\"interface-name\":\"test-interface-name-02\""));
    }

    @Test
    public void testDslQuery_IsAgnosticWithBooleanPropertyAsTrue_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        String endpoint = "/aai/v19/dsl?format=resource&nodesOnly=true";

        // 0 is false, should return value
        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', true)>l-interface*('priority', 123)");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Confirm that the l-interface was returned in the response
        Assert
            .assertTrue(!responseString.contains("\"interface-name\":\"test-interface-name-02\""));
    }

    @Test
    public void testDslQuery_IsAgnosticWithBooleanPropertyAsTrueString_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        String endpoint = "/aai/v19/dsl?format=resource&nodesOnly=true";

        // 0 is false, should return value
        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', 'true')>l-interface*('priority', 123)");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();
        // Confirm that the l-interface was returned in the response
        Assert
            .assertTrue(!responseString.contains("\"interface-name\":\"test-interface-name-02\""));
    }

    @Test
    public void testDslQuery_IsAgnosticWithIntegerPropertyAsString_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        String endpoint = "/aai/v19/dsl?format=resource&nodesOnly=true";

        // With expected boolean value "false" as a boolean value (no quotes)
        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', false)>l-interface*('priority', '00123')");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Confirm that the l-interface was returned in the response
        Assert.assertTrue(responseString.contains("\"interface-name\":\"test-interface-name-02\""));
        dslQueryMap.remove("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', false)>l-interface*('priority', '00123')");

        // With expected boolean value of in-maint, "false", in string form (with single quotes)
        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>tenant*>vserver*('in-maint', 'false')>l-interface*('priority', 00123)");
        payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);

        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        responseString = responseEntity.getBody().toString();

        // Confirm that the l-interface was returned in the response
        Assert.assertTrue(responseString.contains("\"interface-name\":\"test-interface-name-02\""));
    }

    @Test
    public void testDslQuery_IsAgnosticWithLongPropertyAsString_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        String endpoint = "/aai/v19/dsl?format=resource&nodesOnly=true";

        // With expected boolean value "false" as a boolean value (no quotes)
        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>oam-network*('cvlan-tag', '456')");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Confirm that the oam-network was returned in the response
        Assert.assertTrue(responseString.contains("\"cvlan-tag\":456"));
        dslQueryMap.remove("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>oam-network*('cvlan-tag', '456')");

        dslQueryMap.put("dsl-query",
            "cloud-region*('cloud-owner', 'test-cloud-owner-02')>oam-network*('cvlan-tag', 456)");
        payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);

        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        responseString = responseEntity.getBody().toString();

        // Confirm that the oam-network was returned in the response
        Assert.assertTrue(responseString.contains("\"cvlan-tag\":456"));
    }

    @Test
    public void testDslQuery_IsAgnosticWithPrimitivePropertiesInList_ReturnSuccessfulResponse()
        throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        String endpoint = "/aai/v19/dsl?format=resource&nodesOnly=true";

        // With expected boolean value "false" as a boolean value (no quotes)
        dslQueryMap.put("dsl-query",
            "complex('state')>pserver*('number-of-cpus', '234', '364', 2342)");
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        String responseString = responseEntity.getBody().toString();

        // Confirm that the pserver was returned in the response
        Assert.assertTrue(responseString.contains("\"number-of-cpus\":364"));
        dslQueryMap.remove("dsl-query",
            "complex('state')>pserver*('number-of-cpus', '234', '364', 2342)");

        dslQueryMap.put("dsl-query",
            "complex('state')>pserver*('number-of-cpus', '234', 364, 2342)");
        payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);

        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        responseString = responseEntity.getBody().toString();

        // Confirm that the pserver was returned in the response
        Assert.assertTrue(responseString.contains("\"number-of-cpus\":364"));
    }
}
