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

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Tree;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class GetDHVLogicalLinkTest extends TreeQueryTest {
    public GetDHVLogicalLinkTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();

        Tree tree = treeList.get(0);
        Vertex gvnf1 = graph.traversal().V().has("aai-node-type", "generic-vnf")
            .has("vnfname", "vnfname1").next();
        Vertex linterface1 = graph.traversal().V().has("aai-node-type", "l-interface")
            .has("network-name", "networkName1").next();
        Vertex vlan1 = graph.traversal().V().has("aai-node-type", "vlan")
            .has("vlan-interface", "vlan-interface1").next();
        Vertex logicalLink1 = graph.traversal().V().has("aai-node-type", "logical-link")
            .has("link-type", "linkType1").next();

        Vertex gvnf2 = graph.traversal().V().has("aai-node-type", "generic-vnf")
            .has("vnfname", "vnfname2").next();

        assertTrue(tree.containsKey(gvnf1));
        assertTrue(((Tree) tree.get(gvnf1)).containsKey(linterface1));
        assertTrue(((Tree) (((Tree) tree.get(gvnf1)).get(linterface1))).containsKey(vlan1));
        assertTrue(((Tree) ((Tree) (((Tree) tree.get(gvnf1)).get(linterface1))).get(vlan1))
            .containsKey(logicalLink1));

        assertFalse(tree.containsKey(gvnf2));
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        // Set up the test graph
        Vertex gvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type",
            "generic-vnf", "vnfname", "vnfname1", "vnf-name", "vnfname1", "vnf-type", "vnftype1");

        Vertex linterface1 = graph.addVertex(T.label, "l-interface", T.id, "1", "aai-node-type",
            "l-interface", "l-interface-id", "l-interface-id0", "l-interface-name",
            "l-interface-name1", "network-name", "networkName1");

        Vertex vlan1 = graph.addVertex(T.label, "vlan", T.id, "2", "aai-node-type", "vlan",
            "vlan-interface", "vlan-interface1");

        Vertex logicalLink1 =
            graph.addVertex(T.label, "logical-link", T.id, "3", "aai-node-type", "logical-link",
                "link-name", "linkName1", "in-maint", "false", "link-type", "linkType1");

        // Set up the test graph for false test cases
        Vertex gvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "4", "aai-node-type",
            "generic-vnf", "vnfname", "vnfname2", "vnf-name", "vnfname2", "vnf-type", "vnftype2");

        Vertex linterface2 = graph.addVertex(T.label, "l-interface", T.id, "5", "aai-node-type",
            "l-interface", "l-interface-id", "l-interface-id2", "l-interface-name",
            "l-interface-name2", "network-name", "networkName2");

        Vertex vlan2 = graph.addVertex(T.label, "vlan", T.id, "6", "aai-node-type", "vlan",
            "vlan-interface", "vlan-interface2");

        Vertex logicalLink2 =
            graph.addVertex(T.label, "logical-link", T.id, "7", "aai-node-type", "logical-link",
                "link-name", "linkName2", "in-maint", "false", "link-type", "linkType2");

        // GraphTraversalSource g = graph.traversal();

        rules.addTreeEdge(gts, gvnf1, linterface1); // true
        rules.addTreeEdge(gts, linterface1, vlan1); // true
        rules.addEdge(gts, vlan1, logicalLink1); // true

        rules.addTreeEdge(gts, gvnf2, linterface2); // false
        rules.addTreeEdge(gts, linterface2, vlan2); // false
        rules.addEdge(gts, vlan2, logicalLink2); // false

        expectedResult.add(gvnf1);
        expectedResult.add(linterface1);
        expectedResult.add(vlan1);
        expectedResult.add(logicalLink1);

    }

    @Override
    protected String getQueryName() {
        return "getDHVLogicalLink";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "generic-vnf").has("vnfname", "vnfname1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
