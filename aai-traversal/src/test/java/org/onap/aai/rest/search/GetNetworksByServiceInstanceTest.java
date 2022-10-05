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
package org.onap.aai.rest.search;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class GetNetworksByServiceInstanceTest extends QueryTest {

    public GetNetworksByServiceInstanceTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        Vertex serviceInstance = graph.addVertex(T.label, "service-instance", T.id, "0",
            "aai-node-type", "service-instance", "service-instance-id", "serviceinstanceid");
        Vertex l3Network = graph.addVertex(T.label, "l3-network", T.id, "1", "aai-node-type",
            "l3-network", "network-id", "networkId");
        Vertex vlanTag = graph.addVertex(T.label, "vlan-tag", T.id, "2", "aai-node-type",
            "vlan-tag", "vlan-tag-id", "vlanTagId");
        Vertex l3Network1 = graph.addVertex(T.label, "l3-network", T.id, "3", "aai-node-type",
            "l3-network", "network-id", "networkId1");
        Vertex vlanTag1 = graph.addVertex(T.label, "vlan-tag", T.id, "4", "aai-node-type",
            "vlan-tag", "vlan-tag-id", "vlanTagId");

        Vertex serviceInstance2 = graph.addVertex(T.label, "service-instance", T.id, "5",
            "aai-node-type", "service-instance", "service-instance-id", "serviceinstanceid1");
        Vertex l3Network2 = graph.addVertex(T.label, "l3-network", T.id, "6", "aai-node-type",
            "l3-network", "network-id", "networkId2");
        Vertex vlanTag2 = graph.addVertex(T.label, "vlan-tag", T.id, "7", "aai-node-type",
            "vlan-tag", "vlan-tag-id", "vlanTagId");
        Vertex l3Network3 = graph.addVertex(T.label, "l3-network", T.id, "8", "aai-node-type",
            "l3-network", "network-id", "networkId3");
        Vertex vlanTag3 = graph.addVertex(T.label, "vlan-tag", T.id, "9", "aai-node-type",
            "vlan-tag", "vlan-tag-id", "vlanTagId");
        Vertex vlanTag4 = graph.addVertex(T.label, "vlan-tag", T.id, "10", "aai-node-type",
            "vlan-tag", "vlan-tag-id", "vlanTagId");

        Vertex l3Network4 = graph.addVertex(T.label, "l3-network", T.id, "11", "aai-node-type",
            "l3-network", "network-id", "networkId4");
        Vertex l3Network5 = graph.addVertex(T.label, "l3-network", T.id, "12", "aai-node-type",
            "l3-network", "network-id", "networkId5");

        GraphTraversalSource g = graph.traversal();

        rules.addEdge(g, serviceInstance, l3Network);
        rules.addEdge(g, serviceInstance, l3Network2);

        rules.addEdge(g, l3Network, vlanTag);
        rules.addEdge(g, l3Network, vlanTag2);

        rules.addEdge(g, l3Network, l3Network1);
        rules.addEdge(g, l3Network1, vlanTag1);

        rules.addEdge(g, l3Network2, vlanTag4);

        rules.addEdge(g, l3Network2, l3Network3);
        rules.addEdge(g, l3Network3, vlanTag3);

        rules.addEdge(g, l3Network4, l3Network5);
        rules.addEdge(g, serviceInstance, l3Network5);

        expectedResult.add(l3Network);
        expectedResult.add(l3Network2);

        expectedResult.add(vlanTag);
        expectedResult.add(vlanTag2);

        expectedResult.add(l3Network1);
        expectedResult.add(vlanTag1);

        expectedResult.add(vlanTag4);

        expectedResult.add(l3Network3);
        expectedResult.add(vlanTag3);

        expectedResult.add(l3Network4);
        expectedResult.add(l3Network5);

        // expectedResult.add(vlanTag1);//false
        // expectedResult.add(l3Network1);//false
        // expectedResult.add(linterface1);//false
    }

    @Override
    protected String getQueryName() {
        return "getNetworksByServiceInstance";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "service-instance").has("service-instance-id", "serviceinstanceid");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }

}
