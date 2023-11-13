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
package org.onap.aai.rest.history;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.aai.PayloadUtil;
import org.onap.aai.db.props.AAIProperties;
import org.onap.aai.dbmap.AAIGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Ignore("The state format requires the history schema to be loaded.  "
    + "Because aaigraph is a singleton its very complicated to have 2 different schemas loaded for testing.  "
    + "This needs to be addressed.")
public class DslConsumerHistoryStateEdgeTest extends AbstractSpringHistoryRestTest {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(DslConsumerHistoryStateEdgeTest.class);

    @Override
    public void createTestGraph() {
        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        boolean success = true;
        try {
            GraphTraversalSource g = transaction.traversal();

            Vertex pserver = g.addV()
                .property(AAIProperties.NODE_TYPE, "pserver", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.AAI_URI,
                    "/cloud-infrastructure/pservers/pserver/test-pserver-dsl",
                    AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C",
                    AAIProperties.START_TS, 100L)
                .property(AAIProperties.RESOURCE_VERSION, "100", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("hostname", "test-pserver-dsl", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("in-maint", false, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("equip-type", "ps-type", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.START_TS, 100L, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .next();
            Vertex complex = g.addV()
                .property(AAIProperties.NODE_TYPE, "complex", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.AAI_URI,
                    "/cloud-infrastructure/complexes/complex/test-complex-dsl",
                    AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C",
                    AAIProperties.START_TS, 100L)
                .property(AAIProperties.RESOURCE_VERSION, "100", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("physical-location-id", "test-complex-dsl", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("street", "200 S. Laurel Ave", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("city", "Middletown", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("state", "NJ", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("zip", "11111", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.START_TS, 100L, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .next();

            pserver.addEdge("org.onap.relationships.inventory.LocatedIn", complex, "start-ts", 100,
                "private", false, "aai-uuid", "edge1", "end-source-of-truth", "JUNIT-E-D-1",
                "end-tx-id", "JUNIT-E-D-1", "end-ts", 300, "prevent-delete", "IN", "delete-other-v",
                "NONE", "source-of-truth", "JUNIT-E-C-1", "start-tx-id", "JUNIT-E-C-1",
                "contains-other-v", "NONE");
            pserver.addEdge("org.onap.relationships.inventory.LocatedIn", complex, "start-ts", 500,
                "private", false, "aai-uuid", "edge2", "end-source-of-truth", "JUNIT-E-D-2",
                "end-tx-id", "JUNIT-E-D-2", "end-ts", 700, "prevent-delete", "IN", "delete-other-v",
                "NONE", "source-of-truth", "JUNIT-E-C-2", "start-tx-id", "JUNIT-E-C-2",
                "contains-other-v", "NONE");
            pserver.addEdge("org.onap.relationships.inventory.LocatedIn", complex, "start-ts", 900,
                "private", false, "aai-uuid", "edge3", "end-source-of-truth", "JUNIT-E-D-3",
                "end-tx-id", "JUNIT-E-D-3", "end-ts", 1100, "prevent-delete", "IN",
                "delete-other-v", "NONE", "source-of-truth", "JUNIT-E-C-3", "start-tx-id",
                "JUNIT-E-C-3", "contains-other-v", "NONE");
            pserver.addEdge("org.onap.relationships.inventory.LocatedIn", complex, "start-ts", 1300,
                "private", false, "aai-uuid", "edge3", "prevent-delete", "IN", "delete-other-v",
                "NONE", "source-of-truth", "JUNIT-E-C-3", "start-tx-id", "JUNIT-E-C-3",
                "contains-other-v", "NONE");
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

    private JsonArray getResultsForPserverLookupByHostname(String endpoint) throws Exception {
        return getResultsForPserverLookupByHostnameAndAnother(endpoint, "");
    }

    private JsonArray getResultsForPserverLookupByHostnameAndAnother(String endpoint,
        String additionalFilterInParen) throws Exception {
        Map<String, String> dslQueryMap = new HashMap<>();
        dslQueryMap.put("dsl-query",
            "pserver*('hostname','test-pserver-dsl')" + additionalFilterInParen);
        String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        JsonArray results = JsonParser.parseString(responseEntity.getBody().toString())
            .getAsJsonObject().getAsJsonArray("results");
        LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint,
            responseEntity.getBody());

        assertNotNull("Response from /aai/v14/dsl is not null", responseEntity);
        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());
        return results;
    }

    private void verifyEquipTypeValues(JsonArray results, String... expectedEquipTypes) {
        List<String> expected = Arrays.asList(expectedEquipTypes);
        final List<String> actualEquipType = new ArrayList<>();
        for (JsonElement result : results) {
            for (JsonElement property : result.getAsJsonObject().get("properties")
                .getAsJsonArray()) {
                if (property.getAsJsonObject().get("key").getAsString().equals("equip-type")) {
                    assertFalse("State format properties should not have end timestamps",
                        property.getAsJsonObject().has("end-timestamp"));
                    if (property.getAsJsonObject().has("value")
                        && !property.getAsJsonObject().get("value").isJsonNull()) {
                        actualEquipType.add(property.getAsJsonObject().get("value").getAsString());
                    } else {
                        actualEquipType.add(null);
                    }
                }
            }
        }
        assertThat("Verify equip-type in state", actualEquipType, is(expected));
    }

    private void verifyEquipTypeSoTs(JsonArray results, String... expectedSTOs) {
        List<String> expected = Arrays.asList(expectedSTOs);
        final List<String> actualEquipType = new ArrayList<>();
        for (JsonElement result : results) {
            for (JsonElement property : result.getAsJsonObject().get("properties")
                .getAsJsonArray()) {
                if (property.getAsJsonObject().get("key").getAsString().equals("equip-type")) {
                    if (property.getAsJsonObject().has("sot")
                        && !property.getAsJsonObject().get("sot").isJsonNull()) {
                        actualEquipType.add(property.getAsJsonObject().get("sot").getAsString());
                    } else {
                        actualEquipType.add(null);
                    }
                }
            }
        }
        assertThat("Verify equip-type SoTs in state", actualEquipType, is(expected));
    }

    private void verifyEquipTypeTimestamps(JsonArray results, Long... expectedSTOs) {
        List<Long> expected = Arrays.asList(expectedSTOs);
        final List<Long> actualEquipType = new ArrayList<>();
        for (JsonElement result : results) {
            for (JsonElement property : result.getAsJsonObject().get("properties")
                .getAsJsonArray()) {
                if (property.getAsJsonObject().get("key").getAsString().equals("equip-type")) {
                    if (property.getAsJsonObject().has("timestamp")
                        && !property.getAsJsonObject().get("timestamp").isJsonNull()) {
                        actualEquipType
                            .add(property.getAsJsonObject().get("timestamp").getAsLong());
                    } else {
                        actualEquipType.add(null);
                    }
                }
            }
        }
        assertThat("Verify equip-type timestamps in state", actualEquipType, is(expected));
    }

    private void verifyRelatedToCreatedTimestamps(JsonArray results,
        Long... edgeCreatedTimestamps) {
        List<Long> expected = Arrays.asList(edgeCreatedTimestamps);
        final List<Long> actualEquipType = new ArrayList<>();
        for (JsonElement result : results) {
            result.getAsJsonObject().get("related-to").getAsJsonArray()
                .forEach(jsonElement -> actualEquipType
                    .add(jsonElement.getAsJsonObject().get("timestamp").getAsLong()));
        }
        assertThat("Verify related-to createdtimestamps in state", actualEquipType, is(expected));
    }

    private void verifyRelatedToCreatedDeletedTimestamps(JsonArray results,
        Long... edgeDeletedTimestamps) {
        List<Long> expected = Arrays.asList(edgeDeletedTimestamps);
        final List<Long> actualEquipType = new ArrayList<>();
        for (JsonElement result : results) {
            result.getAsJsonObject().get("related-to").getAsJsonArray().forEach(jsonElement -> {
                if (jsonElement.getAsJsonObject().has("end-timestamp")) {
                    actualEquipType
                        .add(jsonElement.getAsJsonObject().get("end-timestamp").getAsLong());
                }
            });
        }
        assertThat("Verify related-to created timestamps in state", actualEquipType, is(expected));
    }

    private void verifyRelatedToCount(JsonArray results, int expectedSize) {
        int actualSize = 0;
        for (JsonElement result : results) {
            actualSize += result.getAsJsonObject().get("related-to").getAsJsonArray().size();
        }
        assertEquals("Verify related-to count", actualSize, expectedSize);
    }

    private void verifyAllPropsTxIds(JsonArray results) {
        final Set<String> propMissingCreatedTxId = new HashSet<>();
        final Set<String> propContainingEndTxId = new HashSet<>();
        for (JsonElement result : results) {
            for (JsonElement property : result.getAsJsonObject().get("properties")
                .getAsJsonArray()) {
                if (!property.getAsJsonObject().has("tx-id")) {
                    propMissingCreatedTxId.add(property.getAsJsonObject().get("key").getAsString());
                }
                if (property.getAsJsonObject().has("end-tx-id")) {
                    propContainingEndTxId.add(property.getAsJsonObject().get("key").getAsString());
                }
            }
        }

        assertThat("Verify no prop is missing tx-id in state", propMissingCreatedTxId,
            is(Collections.EMPTY_SET));
        assertThat("Verify no prop has end-tx-id in state", propContainingEndTxId,
            is(Collections.EMPTY_SET));
    }

    private void verifyRelatedToCreatedTxId(JsonArray results) {
        final Set<JsonObject> edgeMissingCreatedTxId = new HashSet<>();
        final Set<JsonObject> edgeContainingEndTxId = new HashSet<>();
        for (JsonElement result : results) {
            result.getAsJsonObject().get("related-to").getAsJsonArray().forEach(jsonElement -> {
                if (!jsonElement.getAsJsonObject().has("tx-id")) {
                    edgeMissingCreatedTxId.add(jsonElement.getAsJsonObject());
                }
                if (jsonElement.getAsJsonObject().has("end-tx-id")) {
                    edgeContainingEndTxId.add(jsonElement.getAsJsonObject());
                }
            });
        }
        assertThat("Verify no related-to is missing tx-id in state", edgeMissingCreatedTxId,
            is(Collections.EMPTY_SET));
        assertThat("Verify no related-to has end-tx-id in state", edgeContainingEndTxId,
            is(Collections.EMPTY_SET));
    }

    @Test
    public void stateQueryStartTsBeforeCreationTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=50";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 0, results.size());

        verifyEquipTypeValues(results);
        verifyEquipTypeSoTs(results);
        verifyEquipTypeTimestamps(results);
        verifyRelatedToCreatedTimestamps(results);
        verifyRelatedToCreatedDeletedTimestamps(results);
        verifyRelatedToCount(results, 0);
        verifyAllPropsTxIds(results);
        verifyRelatedToCreatedTxId(results);
    }

    @Test
    public void stateQueryStartTsBeforeSecondEdgeCreationTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=400";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-C");
        verifyEquipTypeTimestamps(results, 100L);
        verifyRelatedToCreatedTimestamps(results);
        verifyRelatedToCreatedDeletedTimestamps(results);
        verifyRelatedToCount(results, 0);
        verifyAllPropsTxIds(results);
        verifyRelatedToCreatedTxId(results);
    }

    @Test
    public void stateQueryStartTsAfterSecondEdgeCreationTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=600";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-C");
        verifyEquipTypeTimestamps(results, 100L);
        verifyRelatedToCreatedTimestamps(results, 500L);
        verifyRelatedToCreatedDeletedTimestamps(results);
        verifyRelatedToCount(results, 1);
        verifyAllPropsTxIds(results);
        verifyRelatedToCreatedTxId(results);
    }

    @Test
    public void stateQueryStartTsAtSecondEdgeDeletionTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=700";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-C");
        verifyEquipTypeTimestamps(results, 100L);
        verifyRelatedToCreatedTimestamps(results);
        verifyRelatedToCreatedDeletedTimestamps(results);
        verifyRelatedToCount(results, 0);
        verifyAllPropsTxIds(results);
        verifyRelatedToCreatedTxId(results);
    }

    @Test
    public void stateQueryStartTsAfterSecondEdgeDeletionTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=800";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-C");
        verifyEquipTypeTimestamps(results, 100L);
        verifyRelatedToCreatedTimestamps(results);
        verifyRelatedToCreatedDeletedTimestamps(results);
        verifyRelatedToCount(results, 0);
        verifyAllPropsTxIds(results);
        verifyRelatedToCreatedTxId(results);
    }

    @Test
    public void stateQueryStartTsAtLastEdgeActionTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=1300";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-C");
        verifyEquipTypeTimestamps(results, 100L);
        verifyRelatedToCreatedTimestamps(results, 1300L);
        verifyRelatedToCreatedDeletedTimestamps(results);
        verifyRelatedToCount(results, 1);
        verifyAllPropsTxIds(results);
        verifyRelatedToCreatedTxId(results);
    }

    @Test
    public void stateQueryStartTsAfterLastEdgeActionTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=1400";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-C");
        verifyEquipTypeTimestamps(results, 100L);
        verifyRelatedToCreatedTimestamps(results, 1300L);
        verifyRelatedToCreatedDeletedTimestamps(results);
        verifyRelatedToCount(results, 1);
        verifyAllPropsTxIds(results);
        verifyRelatedToCreatedTxId(results);
    }
}
