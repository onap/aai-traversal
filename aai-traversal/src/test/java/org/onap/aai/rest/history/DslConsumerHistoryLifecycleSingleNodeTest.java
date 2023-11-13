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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
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
import com.google.gson.JsonParser;

@Ignore("The lifecycle format requires the history schema to be loaded.  "
    + "Because aaigraph is a singleton its very complicated to have 2 different schemas loaded for testing.  "
    + "This needs to be addressed.")
public class DslConsumerHistoryLifecycleSingleNodeTest extends AbstractSpringHistoryRestTest {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(DslConsumerHistoryLifecycleSingleNodeTest.class);

    @Override
    public void createTestGraph() {
        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        boolean success = true;
        try {
            GraphTraversalSource g = transaction.traversal();
            /*
             * Pserver
             * - created pserver @ time 100
             * - equip-type = first-ps-type
             * - update equip-type @ 500
             * - equip-type = first-ps-type-update
             * - deleted pserver @ time 1000
             */
            g.addV()
                .property(AAIProperties.NODE_TYPE, "pserver", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT,
                    "JUNIT-D", AAIProperties.END_TX_ID, "JUNIT-D", AAIProperties.START_TS, 100L,
                    AAIProperties.END_TS, 1000L)
                .property(AAIProperties.AAI_URI,
                    "/cloud-infrastructure/pservers/pserver/test-pserver-dsl",
                    AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C",
                    AAIProperties.END_SOT, "JUNIT-D", AAIProperties.END_TX_ID, "JUNIT-D",
                    AAIProperties.START_TS, 100L, AAIProperties.END_TS, 1000L)
                .property(AAIProperties.RESOURCE_VERSION, "100", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT,
                    "JUNIT-U", AAIProperties.END_TX_ID, "JUNIT-U", AAIProperties.START_TS, 100L,
                    AAIProperties.END_TS, 500L)
                .property(AAIProperties.RESOURCE_VERSION, "500", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-U", AAIProperties.START_TX_ID, "JUNIT-U", AAIProperties.END_SOT,
                    "JUNIT-D", AAIProperties.END_TX_ID, "JUNIT-D", AAIProperties.START_TS, 500L,
                    AAIProperties.END_TS, 1000L)
                .property("hostname", "test-pserver-dsl", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT, "JUNIT-D",
                    AAIProperties.END_TX_ID, "JUNIT-D", AAIProperties.START_TS, 100L,
                    AAIProperties.END_TS, 1000L)
                .property("in-maint", false, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT, "JUNIT-D",
                    AAIProperties.END_TX_ID, "JUNIT-D", AAIProperties.START_TS, 100L,
                    AAIProperties.END_TS, 1000L)
                .property("equip-type", "first-ps-type", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT, "JUNIT-U",
                    AAIProperties.END_TX_ID, "JUNIT-U", AAIProperties.START_TS, 100L,
                    AAIProperties.END_TS, 500L)
                .property("equip-type", "first-ps-type-update", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-U", AAIProperties.START_TX_ID, "JUNIT-U", AAIProperties.END_SOT,
                    "JUNIT-D", AAIProperties.END_TX_ID, "JUNIT-D", AAIProperties.START_TS, 500L,
                    AAIProperties.END_TS, 1000L)
                .property(AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT,
                    "JUNIT-D", AAIProperties.END_TX_ID, "JUNIT-D", AAIProperties.START_TS, 100L,
                    AAIProperties.END_TS, 1000L)
                .property(AAIProperties.END_SOT, "JUNIT-D", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-D", AAIProperties.START_TX_ID, "JUNIT-D", AAIProperties.END_SOT,
                    "JUNIT-D", AAIProperties.END_TX_ID, "JUNIT-D", AAIProperties.START_TS, 100L,
                    AAIProperties.END_TS, 1000L)
                .property(AAIProperties.START_TS, 100L, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT, "JUNIT-D",
                    AAIProperties.END_TX_ID, "JUNIT-D", AAIProperties.START_TS, 100L,
                    AAIProperties.END_TS, 1000L)
                .property(AAIProperties.END_TS, 1000L, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-D",
                    AAIProperties.START_TX_ID, "JUNIT-D", AAIProperties.END_SOT, "JUNIT-D",
                    AAIProperties.END_TX_ID, "JUNIT-D", AAIProperties.START_TS, 1000L,
                    AAIProperties.END_TS, 1000L)
                .next();

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
                    if (property.getAsJsonObject().has("value")
                        && !property.getAsJsonObject().get("value").isJsonNull()) {
                        actualEquipType.add(property.getAsJsonObject().get("value").getAsString());
                    } else {
                        actualEquipType.add(null);
                    }
                }
            }
        }
        assertThat("Verify equip-type in lifecycle", actualEquipType, is(expected));
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
        assertThat("Verify equip-type SoTs in lifecycle", actualEquipType, is(expected));
    }

    private void verifyEquipTypeTxId(JsonArray results, String... expectedSTOs) {
        List<String> expected = Arrays.asList(expectedSTOs);
        final List<String> actualEquipType = new ArrayList<>();
        for (JsonElement result : results) {
            for (JsonElement property : result.getAsJsonObject().get("properties")
                .getAsJsonArray()) {
                if (property.getAsJsonObject().get("key").getAsString().equals("equip-type")) {
                    if (property.getAsJsonObject().has("tx-id")
                        && !property.getAsJsonObject().get("tx-id").isJsonNull()) {
                        actualEquipType.add(property.getAsJsonObject().get("tx-id").getAsString());
                    } else {
                        actualEquipType.add(null);
                    }
                }
            }
        }
        assertThat("Verify equip-type tx-ids in lifecycle", actualEquipType, is(expected));
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
        assertThat("Verify equip-type timestamps in lifecycle", actualEquipType, is(expected));
    }

    private void verifyNodeTimestamps(JsonArray results, Long... nodeTimestamps) {
        List<Long> expected = Arrays.asList(nodeTimestamps);
        final List<Long> actualEquipType = new ArrayList<>();
        for (JsonElement result : results) {
            result.getAsJsonObject().get("node-actions").getAsJsonArray()
                .forEach(jsonElement -> actualEquipType
                    .add(jsonElement.getAsJsonObject().get("timestamp").getAsLong()));
        }
        assertThat("Verify node-changes timestamps in lifecycle", actualEquipType, is(expected));
    }

    @Test
    public void lifecycleQueryNoStartOrEndTsTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=lifecycle";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "first-ps-type-update", "first-ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTxId(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTimestamps(results, 500L, 100L);
        verifyNodeTimestamps(results, 1000L, 100L);

        String equipType = "first-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());
        verifyEquipTypeValues(results, "first-ps-type-update", "first-ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTxId(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTimestamps(results, 500L, 100L);
        verifyNodeTimestamps(results, 1000L, 100L);

        equipType = "other-ps-type-recreated";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());
    }

    @Test
    public void lifecycleQueryStartTsBeforeCreationTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=lifecycle&startTs=50";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "first-ps-type-update", "first-ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTxId(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTimestamps(results, 500L, 100L);
        verifyNodeTimestamps(results, 1000L, 100L);

        String equipType = "first-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());
        verifyEquipTypeValues(results, "first-ps-type-update", "first-ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTxId(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTimestamps(results, 500L, 100L);
        verifyNodeTimestamps(results, 1000L, 100L);

        equipType = "other-ps-type-recreated";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());
    }

    @Test
    public void lifecycleQueryStartTsAtCreationTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=lifecycle&startTs=100";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "first-ps-type-update", "first-ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTxId(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTimestamps(results, 500L, 100L);
        verifyNodeTimestamps(results, 1000L, 100L);

        String equipType = "first-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());
        verifyEquipTypeValues(results, "first-ps-type-update", "first-ps-type");
        verifyEquipTypeSoTs(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTxId(results, "JUNIT-U", "JUNIT-C");
        verifyEquipTypeTimestamps(results, 500L, 100L);
        verifyNodeTimestamps(results, 1000L, 100L);

        equipType = "other-ps-type-recreated";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());
    }

    @Test
    public void lifecycleQueryStartTsAfterCreationTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=lifecycle&startTs=300";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "first-ps-type-update");
        verifyEquipTypeSoTs(results, "JUNIT-U");
        verifyEquipTypeTxId(results, "JUNIT-U");
        verifyEquipTypeTimestamps(results, 500L);
        verifyNodeTimestamps(results, 1000L);

        String equipType = "first-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());
        verifyEquipTypeValues(results, "first-ps-type-update");
        verifyEquipTypeSoTs(results, "JUNIT-U");
        verifyEquipTypeTxId(results, "JUNIT-U");
        verifyEquipTypeTimestamps(results, 500L);
        verifyNodeTimestamps(results, 1000L);

        equipType = "other-ps-type-recreated";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());
    }

    @Test
    public void lifecycleQueryStartTsAtUpdateTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=lifecycle&startTs=500";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results, "first-ps-type-update");
        verifyEquipTypeSoTs(results, "JUNIT-U");
        verifyEquipTypeTxId(results, "JUNIT-U");
        verifyEquipTypeTimestamps(results, 500L);
        verifyNodeTimestamps(results, 1000L);

        String equipType = "first-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());
        verifyEquipTypeValues(results, "first-ps-type-update");
        verifyEquipTypeSoTs(results, "JUNIT-U");
        verifyEquipTypeTxId(results, "JUNIT-U");
        verifyEquipTypeTimestamps(results, 500L);
        verifyNodeTimestamps(results, 1000L);

        equipType = "other-ps-type-recreated";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());
    }

    @Test
    public void lifecycleQueryStartTsAfterUpdateTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=lifecycle&startTs=700";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());

        verifyEquipTypeValues(results);
        verifyEquipTypeSoTs(results);
        verifyEquipTypeTxId(results);
        verifyEquipTypeTimestamps(results);
        verifyNodeTimestamps(results, 1000L);

        String equipType = "first-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "other-ps-type-recreated";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());
    }

    @Test
    public void lifecycleQueryStartTsAtDeleteTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=lifecycle&startTs=1000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 1, results.size());
        assertEquals("properties size should be 0",
            results.get(0).getAsJsonObject().get("properties").getAsJsonArray().size(), 0);

        String equipType = "first-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());
    }

    @Test
    public void lifecycleQueryStartTsAfterDeleteTimeTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=lifecycle&startTs=5000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals("Result size", 0, results.size());

        String equipType = "first-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "other-ps-type-recreated";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());
    }

}
