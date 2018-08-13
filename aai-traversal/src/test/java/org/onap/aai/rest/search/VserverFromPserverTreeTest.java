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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.search;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Tree;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VserverFromPserverTreeTest extends TreeQueryTest {

    public VserverFromPserverTreeTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        Vertex p1 = graph.addVertex(T.label, "pserver", T.id, "0", "aai-node-type", "pserver", "hostname", "pserver-name-1");
        Vertex p2 = graph.addVertex(T.label, "pserver", T.id, "1", "aai-node-type", "pserver", "hostname", "pserver-name-2");
        Vertex v1 = graph.addVertex(T.label, "vserver", T.id, "2", "aai-node-type", "vserver", "vserver-id", "vservId-1", "vserver-name", "vserv-name-1", "vserver-selflink", "me/self");
        Vertex v2 = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver", "vserver-id", "vservId-2", "vserver-name", "vserv-name-2", "vserver-selflink", "me/self");
        Vertex v3 = graph.addVertex(T.label, "vserver", T.id, "4", "aai-node-type", "vserver", "vserver-id", "vservId-3", "vserver-name", "vserv-name-3", "vserver-selflink", "me/self");

        rules.addEdge(gts, p1, v1);
        rules.addEdge(gts, p1, v2);
        rules.addEdge(gts, p1, v3);
    }

    @Test
    public void run() {
        super.run();
        Tree tree = treeList.get(0);

        Vertex p1 = graph.traversal().V().has("aai-node-type","pserver").has("hostname","pserver-name-1").next();
        Vertex p2 = graph.traversal().V().has("aai-node-type","pserver").has("hostname","pserver-name-2").next();
        Vertex v1 = graph.traversal().V().has("aai-node-type", "vserver").has("vserver-id","vservId-1").next();
        Vertex v2 = graph.traversal().V().has("aai-node-type", "vserver").has("vserver-id","vservId-2").next();
        Vertex v3 = graph.traversal().V().has("aai-node-type", "vserver").has("vserver-id","vservId-3").next();

        assertTrue(tree.containsKey(p1));
        assertTrue(((Tree) tree.get(p1)).containsKey(v1));
        assertTrue(((Tree) tree.get(p1)).containsKey(v2));
        assertTrue(((Tree) tree.get(p1)).containsKey(v3));
        assertFalse(tree.containsKey(p2));
    }

    @Override
    protected String getQueryName() {
        return "vservers-fromPserver-tree";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "pserver").has("hostname", "pserver-name-1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {

    }
}
