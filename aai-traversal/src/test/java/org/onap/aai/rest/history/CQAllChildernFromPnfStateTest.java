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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.Ignore;
import org.junit.Test;
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
public class CQAllChildernFromPnfStateTest extends AbstractSpringHistoryRestTest {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(CQAllChildernFromPnfStateTest.class);

    @Override
    public void createTestGraph() {
        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        boolean success = true;
        try {
            GraphTraversalSource g = transaction.traversal();

            Vertex pnf1 = g.addV()
                .property(AAIProperties.NODE_TYPE, "pnf", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.AAI_URI, "/network/pnfs/pnf/pnf-1",
                    AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C",
                    AAIProperties.START_TS, 100L)
                .property(AAIProperties.RESOURCE_VERSION, "100", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.START_TS, 100L, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("pnf-name", "pnf-1", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("in-maint", false, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("pnf-id", "pnf-1-id", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .next();

            Vertex pint1 = g.addV()
                .property(AAIProperties.NODE_TYPE, "p-interface", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.AAI_URI,
                    "/network/pnfs/pnf/pnf-1/p-interfaces/p-interface/pint-1",
                    AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C",
                    AAIProperties.START_TS, 100L)
                .property(AAIProperties.RESOURCE_VERSION, "100", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.START_TS, 100L, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("interface-name", "pint-1", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .next();

            Vertex lint1 = g.addV()
                .property(AAIProperties.NODE_TYPE, "l-interface", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.AAI_URI,
                    "/network/pnfs/pnf/pnf-1/p-interfaces/p-interface/pint-1/l-interfaces/l-interface/lint-1",
                    AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C",
                    AAIProperties.START_TS, 100L)
                .property(AAIProperties.RESOURCE_VERSION, "100", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C", AAIProperties.SOURCE_OF_TRUTH,
                    "JUNIT-C", AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property(AAIProperties.START_TS, 100L, AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .property("interface-name", "lint-1", AAIProperties.SOURCE_OF_TRUTH, "JUNIT-C",
                    AAIProperties.START_TX_ID, "JUNIT-C", AAIProperties.START_TS, 100L)
                .next();

            pint1.addEdge("tosca.relationships.network.BindsTo", pnf1, "start-ts", 100L, "private",
                false, "aai-uuid", "e713ce2c-62d5-4555-a481-95619cb158aa", "prevent-delete", "NONE",
                "delete-other-v", "IN", "source-of-truth", "JUNIT-EDGE-C", "start-tx-id",
                "JUNIT-EDGE-C", "contains-other-v", "IN");

            lint1.addEdge("tosca.relationships.network.BindsTo", pint1, "start-ts", 100L, "private",
                false, "aai-uuid", "e713ce2c-62d5-4555-a481-95619cb158ab", "prevent-delete", "NONE",
                "delete-other-v", "IN", "source-of-truth", "JUNIT-EDGE-C", "start-tx-id",
                "JUNIT-EDGE-C", "contains-other-v", "IN");

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

    private JsonArray executeCustomQuery(String endpoint, String queryName, String... startUris)
        throws Exception {
        JsonObject payload = new JsonObject();
        JsonArray start = new JsonArray();
        Arrays.stream(startUris).forEach(start::add);
        payload.add("start", start);
        payload.addProperty("query", "query/" + queryName);
        httpEntity = new HttpEntity(payload.toString(), headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        JsonArray results = JsonParser.parseString(responseEntity.getBody().toString())
            .getAsJsonObject().getAsJsonArray("results");
        LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint,
            responseEntity.getBody());

        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());
        return results;
    }

    private JsonArray executeGremlin(String endpoint, String query) throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("gremlin", query);
        httpEntity = new HttpEntity(payload.toString(), headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        JsonArray results = JsonParser.parseString(responseEntity.getBody().toString())
            .getAsJsonObject().getAsJsonArray("results");
        LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint,
            responseEntity.getBody());

        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());
        return results;
    }

    private void verifyResultUris(JsonArray results, String... uris) {
        Set<String> expected = new HashSet<>(Arrays.asList(uris));
        final Set<String> actualEquipType = new HashSet<>();
        for (JsonElement result : results) {
            actualEquipType.add(result.getAsJsonObject().get("uri").getAsString());
        }
        assertThat("Verify results uri's", actualEquipType, is(expected));
    }

    @Test
    public void pnfChildrenQueryTest() throws Exception {
        JsonArray results = executeCustomQuery("/aai/v14/query?format=state", "allchildren-fromPnf",
            "/network/pnfs/pnf/pnf-1");
        verifyResultUris(results, "/network/pnfs/pnf/pnf-1",
            "/network/pnfs/pnf/pnf-1/p-interfaces/p-interface/pint-1",
            "/network/pnfs/pnf/pnf-1/p-interfaces/p-interface/pint-1/l-interfaces/l-interface/lint-1");
    }

}
