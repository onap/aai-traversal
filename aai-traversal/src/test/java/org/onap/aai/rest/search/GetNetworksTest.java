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

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class GetNetworksTest extends QueryTest {

    public GetNetworksTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void test() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex owningentity = graph.addVertex(T.label, "owning-entity", T.id, "1", "aai-node-type",
            "owning-entity", "owning-entity-id", "owning-entity-id-0", "owning-entity-name",
            "owning-entity-name-0");
        Vertex serviceinstance = graph.addVertex(T.label, "service-instance", T.id, "2",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-0");
        Vertex l3network = graph.addVertex(T.label, "l3-network", T.id, "3", "aai-node-type",
            "l3-network", "network-id", "network-id-0", "network-role", "network-role-0",
            "is-bound-to-vpn", "false", "is-provider-network", "false", "is-shared-network",
            "false", "is-external-network", "false");
        Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "10", "aai-node-type",
            "cloud-region", "cloud-region-id", "cloud-region-id-0", "cloud-region-owner",
            "cloud-owner-name-0", "cloud-region-version", "cloud-region-version-0");
        Vertex l3network2 = graph.addVertex(T.label, "l3-network", T.id, "8", "aai-node-type",
            "l3-network", "network-id", "network-id-2", "network-role", "network-role-2",
            "is-bound-to-vpn", "false", "is-provider-network", "false", "is-shared-network",
            "false", "is-external-network", "false");
        Vertex l3network3 = graph.addVertex(T.label, "l3-network", T.id, "9", "aai-node-type",
            "l3-network", "network-id", "network-id-0", "network-role", "network-role-3",
            "is-bound-to-vpn", "false", "is-provider-network", "false", "is-shared-network",
            "false", "is-external-network", "false");
        Vertex cloudregion2 = graph.addVertex(T.label, "cloud-region", T.id, "11", "aai-node-type",
            "cloud-region", "cloud-region-id", "cloud-region-id-2", "cloud-region-owner",
            "cloud-owner-name-0", "cloud-region-version", "cloud-region-version-0");

        Vertex owningentity1 = graph.addVertex(T.label, "owning-entity", T.id, "4", "aai-node-type",
            "owning-entity", "owning-entity-id", "owning-entity-id-1", "owning-entity-name",
            "owning-entity-name-1");
        Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "5",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1");
        Vertex l3network1 =
            graph.addVertex(T.label, "l3-network", T.id, "6", "aai-node-type", "l3-network",
                "network-id", "network-id-1", "is-bound-to-vpn", "false", "is-provider-network",
                "false", "is-shared-network", "false", "is-external-network", "false");
        Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "7", "aai-node-type",
            "cloud-region", "cloud-region-id", "cloud-region-id-1", "cloud-region-owner",
            "cloud-owner-name-1", "cloud-region-version", "cloud-region-version-1");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, owningentity, serviceinstance);
        rules.addEdge(g, serviceinstance, l3network);
        rules.addEdge(g, l3network, cloudregion);
        rules.addEdge(g, serviceinstance, l3network2);
        rules.addEdge(g, l3network2, cloudregion);
        rules.addEdge(g, l3network3, cloudregion2);
        rules.addEdge(g, serviceinstance, l3network3);

        rules.addEdge(g, owningentity1, serviceinstance1);
        rules.addEdge(g, serviceinstance1, l3network1); // false
        rules.addEdge(g, l3network1, cloudregion1);

        expectedResult.add(l3network);
    }

    @Override
    protected String getQueryName() {
        return "getNetworks";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "owning-entity").has("owning-entity-id", "owning-entity-id-0");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        params.put("networkRole", "network-role-0");
        params.put("cloudRegionId", "cloud-region-id-0");
    }

}
