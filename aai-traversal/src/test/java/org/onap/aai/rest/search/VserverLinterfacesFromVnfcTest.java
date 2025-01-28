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

public class VserverLinterfacesFromVnfcTest extends QueryTest {

    public VserverLinterfacesFromVnfcTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        Vertex vnfc =
            graph.addVertex(T.label, "vnfc", T.id, "0", "aai-node-type", "vnfc", "vnfc-name",
                "vnfcName1", "nfc-naming-code", "blue", "nfc-function", "correct-function");
        Vertex vserv = graph.addVertex(T.label, "vserver", T.id, "1", "aai-node-type", "vserver",
            "vserver-id", "vservId", "vserver-name", "vservName", "vserver-selflink", "me/self");
        Vertex linterface = graph.addVertex(T.label, "l-interface", T.id, "2", "aai-node-type",
            "l-interface", "l-interface-id", "l-interface-id0", "l-interface-name",
            "l-interface-name0", "network-name", "networkName0");
        Vertex linterface1 = graph.addVertex(T.label, "l-interface", T.id, "3", "aai-node-type",
            "l-interface", "l-interface-id", "l-interface-id1", "l-interface-name",
            "l-interface-name1", "network-name", "networkName1");
        Vertex vlan = graph.addVertex(T.label, "vlan", T.id, "4", "aai-node-type", "vlan",
            "vlan-interface", "vlan-interface0");

        Vertex vnfc1 =
            graph.addVertex(T.label, "vnfc", T.id, "10", "aai-node-type", "vnfc", "vnfc-name",
                "vnfcName2", "nfc-naming-code", "blue-1", "nfc-function", "correct-function-1");
        Vertex vserv1 = graph.addVertex(T.label, "vserver", T.id, "11", "aai-node-type", "vserver",
            "vserver-id", "vservId1", "vserver-name", "vservName1", "vserver-selflink",
            "me/self-1");
        Vertex linterface2 = graph.addVertex(T.label, "l-interface", T.id, "12", "aai-node-type",
            "l-interface", "l-interface-id", "l-interface-id2", "l-interface-name",
            "l-interface-name2", "network-name", "networkName2");
        Vertex linterface21 = graph.addVertex(T.label, "l-interface", T.id, "13", "aai-node-type",
            "l-interface", "l-interface-id", "l-interface-id21", "l-interface-name",
            "l-interface-name21", "network-name", "networkName21");
        Vertex vlan1 = graph.addVertex(T.label, "vlan", T.id, "14", "aai-node-type", "vlan",
            "vlan-interface", "vlan-interface1");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, vnfc, vserv);
        rules.addTreeEdge(g, vserv, linterface);
        rules.addTreeEdge(g, linterface, linterface1);
        rules.addTreeEdge(g, linterface1, vlan);

        rules.addEdge(g, vnfc1, vserv1);// false
        rules.addTreeEdge(g, vserv1, linterface2);// false
        rules.addTreeEdge(g, linterface2, linterface21);// false
        rules.addTreeEdge(g, linterface21, vlan1);// false

        expectedResult.add(vserv);
        expectedResult.add(linterface);
        expectedResult.add(linterface1);
        expectedResult.add(vlan);
    }

    @Override
    protected String getQueryName() {
        return "vserver-l-interfaces-fromVnfc";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "vnfc").has("vnfc-name", "vnfcName1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        params.put("networkName", "networkName0");
    }

}
