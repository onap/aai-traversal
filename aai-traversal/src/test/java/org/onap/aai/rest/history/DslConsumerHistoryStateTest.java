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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

@Ignore("The state format requires the history schema to be loaded.  "
    + "Because aaigraph is a singleton its very complicated to have 2 different schemas loaded for testing.  "
    + "This needs to be addressed.")
public class DslConsumerHistoryStateTest extends AbstractSpringHistoryRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DslConsumerHistoryStateTest.class);

    @Override
    public void createTestGraph() {
        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        boolean success = true;
        try {
            GraphTraversalSource g = transaction.traversal();
            /*
             * Pserver
             * - created @ time 100
             * - update in-maint/equip-type @ 500
             * - deleted @ time 1000
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
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT, "JUNIT-U",
                    AAIProperties.END_TX_ID, "JUNIT-U", AAIProperties.START_TS, 100L,
                    AAIProperties.END_TS, 500L)
                .property("in-maint", true, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-U",
                    AAIProperties.START_TX_ID, "JUNIT-U", AAIProperties.END_SOT, "JUNIT-D",
                    AAIProperties.END_TX_ID, "JUNIT-D", AAIProperties.START_TS, 500L,
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

            /*
             * Pserver
             * - created @ time 10000
             * - update in-maint/equip-type @ 15000
             */
            g.addV()
                .property(AAIProperties.NODE_TYPE, "pserver", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 10000L)
                .property(AAIProperties.AAI_URI,
                    "/cloud-infrastructure/pservers/pserver/test-pserver-dsl",
                    AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C",
                    AAIProperties.START_TS, 10000L)
                .property(AAIProperties.RESOURCE_VERSION, "10000", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT,
                    "JUNIT-U", AAIProperties.END_TX_ID, "JUNIT-U", AAIProperties.START_TS, 10000L,
                    AAIProperties.END_TS, 15000L)
                .property(AAIProperties.RESOURCE_VERSION, "15000", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-U", AAIProperties.START_TX_ID, "JUNIT-U", AAIProperties.START_TS, 15000)
                .property("hostname", "test-pserver-dsl", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 10000L)
                .property("in-maint", true, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT, "JUNIT-U",
                    AAIProperties.END_TX_ID, "JUNIT-U", AAIProperties.START_TS, 10000L,
                    AAIProperties.END_TS, 15000L)
                .property("in-maint", false, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-U",
                    AAIProperties.START_TX_ID, "JUNIT-U", AAIProperties.START_TS, 15000L)
                .property("equip-type", "second-ps-type", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.END_SOT, "JUNIT-U",
                    AAIProperties.END_TX_ID, "JUNIT-U", AAIProperties.START_TS, 10000L,
                    AAIProperties.END_TS, 15000L)
                .property("equip-type", "second-ps-type-update", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-U", AAIProperties.START_TX_ID, "JUNIT-U", AAIProperties.START_TS, 15000L)
                .property(AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 10000L)
                .property(AAIProperties.START_TS, 10000L, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 10000L)
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

    private void verifyEquipTypeValues(JsonArray results, String... equipType) {
        Set<String> expectedEquipTypes = new HashSet<>(Arrays.asList(equipType));
        final Set<String> actualEquipType = new HashSet<>();
        for (JsonElement result : results) {
            for (JsonElement property : result.getAsJsonObject().get("properties")
                .getAsJsonArray()) {
                assertFalse("State format properties should not have end timestamps",
                    property.getAsJsonObject().has("end-timestamp"));
                if (property.getAsJsonObject().get("key").getAsString().equals("equip-type")) {
                    actualEquipType.add(property.getAsJsonObject().get("value").getAsString());
                }
            }
        }
        assertThat("Verify equip-type in state", actualEquipType, is(expectedEquipTypes));
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

    @Test
    public void stateQueryBeforeCreationTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=50";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(0, results.size());
    }

    @Test
    public void stateQueryAtCreationTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=100";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "first-ps-type");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAfterCreationTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=200";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "first-ps-type");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAtUpdateTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=500";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "first-ps-type-update");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAfterUpdateTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=700";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "first-ps-type-update");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAtDeletionTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=1000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(0, results.size());
    }

    @Test
    public void stateQueryAfterDeletionTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=2000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(0, results.size());
    }

    // WITH endTs=startTs
    @Test
    public void stateQueryBeforeCreationWithEndTsTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=50&endTs=50";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(0, results.size());
    }

    @Test
    public void stateQueryAtCreationTimeWithEndTsTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=100&endTs=100";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "first-ps-type");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAfterCreationTimeWithEndTsTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=200&endTs=200";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "first-ps-type");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAtUpdateTimeWithEndTsTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=500&endTs=500";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "first-ps-type-update");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAfterUpdateTimeWithEndTsTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=700&endTs=700";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "first-ps-type-update");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAtDeletionTimeWithEndTsTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=1000&endTs=1000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(0, results.size());
    }

    @Test
    public void stateQueryAfterDeletionTimeWithEndTsTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=2000&endTs=2000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(0, results.size());
    }

    // Second pserver

    @Test
    public void stateQueryBeforeSecondCreationTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=9000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(0, results.size());
    }

    @Test
    public void stateQueryAtSecondCreationTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=10000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "second-ps-type");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAfterSecondCreationTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=12000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "second-ps-type");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAtUpdateAfterSecondCreationTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=15000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "second-ps-type-update");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryAfterUpdateAfterSecondCreationTimeTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state&startTs=17000";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "second-ps-type-update");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryNoTimestampTest() throws Exception {
        String endpoint = "/aai/v14/dsl?format=state";
        JsonArray results = getResultsForPserverLookupByHostname(endpoint);
        assertEquals(1, results.size());
        verifyEquipTypeValues(results, "second-ps-type-update");
        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryNoTimestampWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());

        verifyEquipTypeValues(results, equipType);

        verifyAllPropsTxIds(results);
    }

    @Test
    public void stateQueryTimestampBeforeFirstCreateWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=50";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

    }

    @Test
    public void stateQueryTimestampAtFirstCreateWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=100";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());

        verifyEquipTypeValues(results, equipType);

        verifyAllPropsTxIds(results);

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

    }

    @Test
    public void stateQueryTimestampAfterFirstCreateWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=200";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());

        verifyEquipTypeValues(results, equipType);

        verifyAllPropsTxIds(results);

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

    }

    @Test
    public void stateQueryTimestampAtFirstUpdateWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=500";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());

        verifyEquipTypeValues(results, equipType);

        verifyAllPropsTxIds(results);

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

    }

    @Test
    public void stateQueryTimestampAfterFirstUpdateWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=700";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());

        verifyEquipTypeValues(results, equipType);

        verifyAllPropsTxIds(results);

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

    }

    @Test
    public void stateQueryTimestampAtFirstDeleteWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=1000";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

    }

    @Test
    public void stateQueryTimestampAfterFirstDeleteWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=5000";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

    }

    @Test
    public void stateQueryTimestampAtSecondCreateWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=10000";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());

        verifyEquipTypeValues(results, equipType);

        verifyAllPropsTxIds(results);

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

    }

    @Test
    public void stateQueryTimestampAfterSecondCreateWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=12000";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());

        verifyEquipTypeValues(results, equipType);

        verifyAllPropsTxIds(results);

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

    }

    @Test
    public void stateQueryTimestampAtSecondUpdateWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=15000";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());

        verifyEquipTypeValues(results, equipType);

        verifyAllPropsTxIds(results);

    }

    @Test
    public void stateQueryTimestampAfterSecondUpdateWithEquipTypeFilterTest() throws Exception {

        String endpoint = "/aai/v14/dsl?format=state&startTs=15000";
        String equipType = "first-ps-type";
        JsonArray results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "first-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 0, results.size());

        equipType = "second-ps-type-update";
        results = getResultsForPserverLookupByHostnameAndAnother(endpoint,
            "('equip-type','" + equipType + "')");
        assertEquals("Filter equip-type on " + equipType, 1, results.size());

        verifyEquipTypeValues(results, equipType);

        verifyAllPropsTxIds(results);

    }
}
