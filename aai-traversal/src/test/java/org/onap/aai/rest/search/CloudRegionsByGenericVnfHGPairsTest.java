package org.onap.aai.rest.search;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class CloudRegionsByGenericVnfHGPairsTest extends LinkedHashMapQueryTest {

    public CloudRegionsByGenericVnfHGPairsTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void test() {
        super.run();
        String expectedServiceInstanceUri = "service-instance-id1";
        String[] expectedCloudRegions = {"c1oud-region-1", "c1oud-region-3", "c1oud-region-4"};
        String[] actualCloudRegions = new String[3];
        assertEquals("Number of results is correct", hashMapList.size(), 3);
        for (int i = 0; i < hashMapList.size(); i++) {
            LinkedHashMap currentMap = hashMapList.get(i);
            assertEquals("Result " + i + " has correct service-instance",
                currentMap.get("a").toString(), expectedServiceInstanceUri);
            actualCloudRegions[i] = currentMap.get("b").toString();
        }
        Arrays.sort(expectedCloudRegions);
        Arrays.sort(actualCloudRegions);
        assertTrue("Cloud regions are correct",
            Arrays.equals(actualCloudRegions, expectedCloudRegions));
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex servicesubscription = graph.addVertex(T.label, "service-subscription", T.id, "0",
            "aai-node-type", "service-subscription", "service-type", "DHV");
        Vertex servicesubscription1 = graph.addVertex(T.label, "service-subscription", T.id, "1",
            "aai-node-type", "service-subscription", "service-type", "HNGATEWAY");
        Vertex servicesubscription2 = graph.addVertex(T.label, "service-subscription", T.id, "2",
            "aai-node-type", "service-subscription", "service-type", "service-type-2");

        Vertex servicesubscription3 = graph.addVertex(T.label, "service-subscription", T.id, "3",
            "aai-node-type", "service-subscription", "service-type", "HNGATEWAY");
        Vertex servicesubscription4 = graph.addVertex(T.label, "service-subscription", T.id, "4",
            "aai-node-type", "service-subscription", "service-type", "HNGATEWAY");
        Vertex servicesubscription5 = graph.addVertex(T.label, "service-subscription", T.id, "5",
            "aai-node-type", "service-subscription", "service-type", "HNGATEWAY");

        Vertex serviceinstance = graph.addVertex(T.label, "service-instance", T.id, "6",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id1",
            "aai-uri", "service-instance-id1");
        Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "7",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id2",
            "aai-uri", "service-instance-id2");
        Vertex serviceinstance2 = graph.addVertex(T.label, "service-instance", T.id, "8",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id3",
            "aai-uri", "service-instance-id3");
        Vertex serviceinstance3 = graph.addVertex(T.label, "service-instance", T.id, "9",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id4",
            "aai-uri", "service-instance-id4");

        Vertex serviceinstance4 = graph.addVertex(T.label, "service-instance", T.id, "10",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id5",
            "aai-uri", "service-instance-id5");
        Vertex serviceinstance5 = graph.addVertex(T.label, "service-instance", T.id, "11",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id6",
            "aai-uri", "service-instance-id6");
        Vertex serviceinstance6 = graph.addVertex(T.label, "service-instance", T.id, "12",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id7",
            "aai-uri", "service-instance-id7");

        Vertex serviceinstance7 = graph.addVertex(T.label, "service-instance", T.id, "13",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id8",
            "aai-uri", "service-instance-id8");
        Vertex serviceinstance8 = graph.addVertex(T.label, "service-instance", T.id, "14",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id9",
            "aai-uri", "service-instance-id9");
        Vertex serviceinstance9 = graph.addVertex(T.label, "service-instance", T.id, "15",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id10",
            "aai-uri", "service-instance-id10");

        Vertex allottedresource = graph.addVertex(T.label, "allotted-resource", T.id, "16",
            "aai-node-type", "allotted-resource");
        Vertex allottedresource1 = graph.addVertex(T.label, "allotted-resource", T.id, "17",
            "aai-node-type", "allotted-resource");
        Vertex allottedresource2 = graph.addVertex(T.label, "allotted-resource", T.id, "18",
            "aai-node-type", "allotted-resource");
        Vertex allottedresource3 = graph.addVertex(T.label, "allotted-resource", T.id, "35",
            "aai-node-type", "allotted-resource");

        Vertex gvnf = graph.addVertex(T.label, "generic-vnf", T.id, "19", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-0", "vnf-type", "HG");
        Vertex gvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "20", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-1", "vnf-type", "vnf-type-1");
        Vertex gvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "21", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-2", "vnf-type", "HG");
        Vertex gvnf3 = graph.addVertex(T.label, "generic-vnf", T.id, "22", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-2", "vnf-type", "HG");
        Vertex gvnf4 = graph.addVertex(T.label, "generic-vnf", T.id, "36", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-2", "vnf-type", "HP");

        Vertex vserver =
            graph.addVertex(T.label, "vserver", T.id, "23", "aai-node-type", "vserver");
        Vertex vserver1 =
            graph.addVertex(T.label, "vserver", T.id, "24", "aai-node-type", "vserver");
        Vertex vserver2 =
            graph.addVertex(T.label, "vserver", T.id, "25", "aai-node-type", "vserver");
        Vertex vserver3 =
            graph.addVertex(T.label, "vserver", T.id, "26", "aai-node-type", "vserver");
        Vertex vserver4 =
            graph.addVertex(T.label, "vserver", T.id, "38", "aai-node-type", "vserver");

        Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "27", "aai-node-type", "tenant");
        Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "28", "aai-node-type", "tenant");
        Vertex tenant2 = graph.addVertex(T.label, "tenant", T.id, "29", "aai-node-type", "tenant");
        Vertex tenant3 = graph.addVertex(T.label, "tenant", T.id, "30", "aai-node-type", "tenant");
        Vertex tenant4 = graph.addVertex(T.label, "tenant", T.id, "39", "aai-node-type", "tenant");

        Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "31", "aai-node-type",
            "cloud-region", "aai-uri", "c1oud-region-1");
        Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "32", "aai-node-type",
            "cloud-region", "aai-uri", "c1oud-region-2");
        Vertex cloudregion2 = graph.addVertex(T.label, "cloud-region", T.id, "33", "aai-node-type",
            "cloud-region", "aai-uri", "c1oud-region-3");
        Vertex cloudregion3 = graph.addVertex(T.label, "cloud-region", T.id, "34", "aai-node-type",
            "cloud-region", "aai-uri", "c1oud-region-4");
        Vertex cloudregion4 = graph.addVertex(T.label, "cloud-region", T.id, "40", "aai-node-type",
            "cloud-region", "aai-uri", "c1oud-region-5");

        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, servicesubscription, serviceinstance);

        rules.addEdge(g, serviceinstance, allottedresource);
        rules.addEdge(g, serviceinstance, allottedresource1);
        rules.addEdge(g, serviceinstance, allottedresource2);
        rules.addEdge(g, serviceinstance, allottedresource3);

        rules.addTreeEdge(g, allottedresource, serviceinstance1);
        rules.addTreeEdge(g, serviceinstance1, servicesubscription1);
        rules.addTreeEdge(g, servicesubscription1, serviceinstance2);
        rules.addEdge(g, serviceinstance2, gvnf);
        rules.addEdge(g, gvnf, vserver);
        rules.addTreeEdge(g, vserver, tenant);
        rules.addTreeEdge(g, tenant, cloudregion);

        rules.addTreeEdge(g, allottedresource1, serviceinstance4);
        rules.addTreeEdge(g, serviceinstance4, servicesubscription3);
        rules.addTreeEdge(g, servicesubscription3, serviceinstance5);
        rules.addEdge(g, serviceinstance5, gvnf2);
        rules.addEdge(g, gvnf2, vserver2);
        rules.addTreeEdge(g, vserver2, tenant2);
        rules.addTreeEdge(g, tenant2, cloudregion2);

        rules.addTreeEdge(g, allottedresource2, serviceinstance6);
        rules.addTreeEdge(g, serviceinstance6, servicesubscription4);
        rules.addTreeEdge(g, servicesubscription4, serviceinstance7);
        rules.addEdge(g, serviceinstance7, gvnf3);
        rules.addEdge(g, gvnf3, vserver3);
        rules.addTreeEdge(g, vserver3, tenant3);
        rules.addTreeEdge(g, tenant3, cloudregion3);

        rules.addTreeEdge(g, allottedresource3, serviceinstance8);
        rules.addTreeEdge(g, serviceinstance8, servicesubscription5);
        rules.addTreeEdge(g, servicesubscription5, serviceinstance9);
        rules.addEdge(g, serviceinstance9, gvnf4);
        rules.addEdge(g, gvnf4, vserver4);
        rules.addTreeEdge(g, vserver4, tenant4);
        rules.addTreeEdge(g, tenant4, cloudregion4);// Not expected in output as vnf-type is not HG

        rules.addTreeEdge(g, servicesubscription2, serviceinstance3);
        rules.addEdge(g, serviceinstance3, allottedresource);
        rules.addEdge(g, serviceinstance2, gvnf1);
        rules.addEdge(g, gvnf1, vserver1);
        rules.addTreeEdge(g, vserver1, tenant1);
        rules.addTreeEdge(g, tenant1, cloudregion1);// Not expected in output as
                                                    // service-subscription is not DHV

    }

    @Override
    protected String getQueryName() {
        return "cloud-regions-by-generic-vnf-HG-pairs";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "service-subscription").has("service-type", "DHV");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }

}
