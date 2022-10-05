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

public class GetComplexAndPserversTest extends QueryTest {
    public GetComplexAndPserversTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        Vertex complex = graph.addVertex(T.label, "complex", T.id, "1", "aai-node-type", "complex",
            "complex-id", "complex-1");
        Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "2", "aai-node-type", "pserver",
            "hostname", "pservername1");

        Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "3", "aai-node-type", "complex",
            "complex-id", "complex-2");
        Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "4", "aai-node-type", "pserver",
            "hostname", "pservername2");

        GraphTraversalSource g = graph.traversal();

        rules.addEdge(g, complex, pserver);

        // Not expected in result
        rules.addEdge(g, complex1, pserver1);
        // Not expected in result

        expectedResult.add(complex);
        expectedResult.add(pserver);
    }

    @Override
    protected String getQueryName() {
        return "getComplexAndPservers";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "complex").has("complex-id", "complex-1");

    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
