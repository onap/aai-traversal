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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Tree;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class GetComplexFromHostnameQueryTest extends TreeQueryTest {
    public GetComplexFromHostnameQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void run() {
        super.run();
        Tree tree = treeList.get(0);
        Vertex pserver1 = graph.traversal().V().has("aai-node-type", "pserver")
            .has("hostname", "pserver-name-1").next();
        Vertex pserver2 = graph.traversal().V().has("aai-node-type", "pserver")
            .has("hostname", "pserver-name-2").next();
        Vertex complex1 = graph.traversal().V().has("aai-node-type", "complex")
            .has("physical-location-id", "physical-location-id-1").next();
        assertTrue(tree.containsKey(pserver1));
        assertTrue(((Tree) tree.get(pserver1)).containsKey(complex1));
        assertFalse(tree.containsKey(pserver2));
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        // Set up the test graph
        Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "0", "aai-node-type", "pserver",
            "hostname", "pserver-name-1");
        Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "4", "aai-node-type", "pserver",
            "hostname", "pserver-name-2");
        Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "1", "aai-node-type", "complex",
            "physical-location-id", "physical-location-id-1");
        rules.addEdge(gts, pserver1, complex1);
        rules.addEdge(gts, pserver2, complex1);
    }

    @Override
    protected String getQueryName() {
        return "getComplexFromHostname";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "pserver").has("hostname", "pserver-name-1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
    }
}
