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

public class GetVnfVlanByCircuitIdTest extends QueryTest {
    public GetVnfVlanByCircuitIdTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        // Set up the test graph
        Vertex logicallink1 =
            graph.addVertex(T.label, "logical-link", T.id, "1", "aai-node-type", "logical-link",
                "link-name", "linkName1", "link-type", "linkType1", "circuit-id", "circuitId1");
        Vertex logicallink2 =
            graph.addVertex(T.label, "logical-link", T.id, "2", "aai-node-type", "logical-link",
                "link-name", "linkName2", "link-type", "linkType2", "circuit-id", "circuitId2");

        Vertex vlan1 = graph.addVertex(T.label, "vlan", T.id, "3", "aai-node-type", "vlan",
            "vlan-interface", "vlanInterface1");
        Vertex vlan2 = graph.addVertex(T.label, "vlan", T.id, "4", "aai-node-type", "vlan",
            "vlan-interface", "vlanInterface2");

        Vertex linterface1 = graph.addVertex(T.label, "l-interface", T.id, "5", "aai-node-type",
            "l-interface", "interface-name", "interfaceName1");
        Vertex linterface2 = graph.addVertex(T.label, "l-interface", T.id, "6", "aai-node-type",
            "l-interface", "interface-name", "interfaceName2");

        Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "7", "aai-node-type",
            "generic-vnf", "vnf-id", "vnfId1", "vnf-type", "vnfType1");
        Vertex genericvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "8", "aai-node-type",
            "generic-vnf", "vnf-id", "vnfId2", "vnf-type", "vnfType2");

        GraphTraversalSource g = graph.traversal();

        rules.addEdge(g, logicallink1, vlan1);
        rules.addTreeEdge(g, vlan1, linterface1);
        rules.addTreeEdge(g, linterface1, genericvnf1);

        rules.addEdge(g, logicallink2, vlan2);// false
        rules.addTreeEdge(g, vlan2, linterface2);// false
        rules.addTreeEdge(g, linterface2, genericvnf2);// false

        expectedResult.add(logicallink1);
        expectedResult.add(vlan1);
        expectedResult.add(linterface1);
        expectedResult.add(genericvnf1);

    }

    @Override
    protected String getQueryName() {
        return "getVnfVlanByCircuitId";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("link-name", "linkName1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        params.put("circuit-id", "circuitId1");
    }
}
