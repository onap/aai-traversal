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

public class GFPVnfDataTest extends QueryTest {
    public GFPVnfDataTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // set up test graph
        Vertex genericvnf =
            graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type", "generic-vnf",
                "vnf-id", "vnfid0", "vnf-name", "vnf-name-1", "nf-type", "sample-nf-type");
        Vertex linterface =
            graph.addVertex(T.label, "l-interface", T.id, "2", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id0", "l-interface-name", "l-interface-name0");
        Vertex l3inter1ipv4addresslist = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "3", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address-0");
        Vertex l3inter1ipv6addresslist = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "4", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address-0");
        Vertex vlan = graph.addVertex(T.label, "vlan", T.id, "5", "aai-node-type", "vlan",
            "vlan-interface", "vlan1");
        Vertex vnfImage = graph.addVertex(T.label, "vnf-image", T.id, "6", "aai-node-type",
            "vnf-image", "vnf-image-uuid", "vnf1imageuuid");
        Vertex networkProfile = graph.addVertex(T.label, "network-profile", T.id, "7",
            "aai-node-type", "network-profile", "nm-profile-name", "nm-profile-name-1");
        Vertex lagint = graph.addVertex(T.label, "lag-interface", T.id, "8", "aai-node-type",
            "lag-interface", "interface-name", "lagint1");

        Vertex lagint0 = graph.addVertex(T.label, "lag-interface", T.id, "11", "aai-node-type",
            "lag-interface", "interface-name", "lagint31");
        Vertex linterface0 =
            graph.addVertex(T.label, "l-interface", T.id, "12", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id30", "l-interface-name", "l-interface-name30");
        Vertex vlan0 = graph.addVertex(T.label, "vlan", T.id, "13", "aai-node-type", "vlan",
            "vlan-interface", "vlan31");
        Vertex l3inter1ipv4addresslist0 = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "14", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address-30");
        Vertex l3inter1ipv6addresslist0 = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "15", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address-30");

        Vertex genericvnf1 =
            graph.addVertex(T.label, "generic-vnf", T.id, "10", "aai-node-type", "generic-vnf",
                "vnf-id", "vnfid10", "vnf-name", "vnf-name-11", "nf-type", "sample-nf-type11");
        Vertex linterface1 =
            graph.addVertex(T.label, "l-interface", T.id, "20", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id10", "l-interface-name", "l-interface-name10");
        Vertex l3inter1ipv4addresslist1 = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "30", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address-10");
        Vertex l3inter1ipv6addresslist1 = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "40", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address-10");
        Vertex vlan1 = graph.addVertex(T.label, "vlan", T.id, "50", "aai-node-type", "vlan",
            "vlan-interface", "vlan11");
        Vertex vnfImage1 = graph.addVertex(T.label, "vnf-image", T.id, "60", "aai-node-type",
            "vnf-image", "vnf-image-uuid", "vnf1imageuuid11");
        Vertex networkProfile1 = graph.addVertex(T.label, "network-profile", T.id, "70",
            "aai-node-type", "network-profile", "nm-profile-name", "nm-profile-name-11");
        Vertex lagint1 = graph.addVertex(T.label, "lag-interface", T.id, "80", "aai-node-type",
            "lag-interface", "interface-name", "lagint11");

        Vertex lagint2 = graph.addVertex(T.label, "lag-interface", T.id, "21", "aai-node-type",
            "lag-interface", "interface-name", "lagint312");
        Vertex linterface2 =
            graph.addVertex(T.label, "l-interface", T.id, "22", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id30", "l-interface-name", "l-interface-name302");
        Vertex vlan2 = graph.addVertex(T.label, "vlan", T.id, "23", "aai-node-type", "vlan",
            "vlan-interface", "vlan312");
        Vertex l3inter1ipv4addresslist2 = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "24", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address-302");
        Vertex l3inter1ipv6addresslist2 = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "25", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address-302");

        GraphTraversalSource g = graph.traversal();

        rules.addTreeEdge(g, genericvnf, linterface);
        rules.addTreeEdge(g, l3inter1ipv4addresslist, linterface);
        rules.addTreeEdge(g, l3inter1ipv6addresslist, linterface);
        rules.addTreeEdge(g, linterface, vlan);
        rules.addTreeEdge(g, l3inter1ipv4addresslist, vlan);
        rules.addTreeEdge(g, l3inter1ipv6addresslist, vlan);

        rules.addTreeEdge(g, genericvnf, lagint0);
        rules.addTreeEdge(g, lagint0, linterface0);
        rules.addTreeEdge(g, linterface0, vlan0);
        rules.addTreeEdge(g, l3inter1ipv4addresslist0, linterface0);
        rules.addTreeEdge(g, l3inter1ipv6addresslist0, linterface0);

        rules.addEdge(g, genericvnf, vnfImage);
        rules.addEdge(g, genericvnf, networkProfile);

        // false
        rules.addTreeEdge(g, genericvnf1, linterface1);
        rules.addTreeEdge(g, l3inter1ipv4addresslist1, linterface1);
        rules.addTreeEdge(g, l3inter1ipv6addresslist1, linterface1);
        rules.addTreeEdge(g, linterface1, vlan1);
        rules.addTreeEdge(g, l3inter1ipv4addresslist1, vlan1);
        rules.addTreeEdge(g, l3inter1ipv6addresslist1, vlan1);

        rules.addTreeEdge(g, genericvnf1, lagint2);
        rules.addTreeEdge(g, linterface2, lagint2);
        rules.addTreeEdge(g, linterface2, vlan2);
        rules.addTreeEdge(g, l3inter1ipv4addresslist2, linterface2);
        rules.addTreeEdge(g, l3inter1ipv6addresslist2, linterface2);

        rules.addEdge(g, genericvnf1, vnfImage1);
        rules.addEdge(g, genericvnf1, networkProfile1);

        expectedResult.add(genericvnf);
        expectedResult.add(l3inter1ipv4addresslist);
        expectedResult.add(l3inter1ipv6addresslist);
        expectedResult.add(vlan);
        expectedResult.add(linterface0);
        expectedResult.add(vnfImage);
        expectedResult.add(networkProfile);

    }

    @Override
    protected String getQueryName() {
        return "gfp-vnf-data";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "generic-vnf").has("vnf-name", "vnf-name-1").has("vnf-id", "vnfid0");

    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
