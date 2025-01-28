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

public class VserverLogicallinkFromPServerTest extends QueryTest {

    public VserverLogicallinkFromPServerTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void test() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "1", "aai-node-type", "pserver",
            "hostname", "hostname-1");
        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "2", "aai-node-type", "vserver",
            "vserver-id", "vserver-id-1", "vserver-name", "vserver-name-1");
        Vertex lInterface1 = graph.addVertex(T.label, "l-interface", T.id, "3", "aai-node-type",
            "l-interface", "interface-name", "interface-name-1");
        Vertex logicalLink1 = graph.addVertex(T.label, "l", T.id, "4", "aai-node-type",
            "logical-link", "link-name", "link-name-1");

        Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "5", "aai-node-type", "pserver",
            "hostname", "hostname-2");
        Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "6", "aai-node-type", "vserver",
            "vserver-id", "vserver-id-2", "vserver-name", "vserver-name-2");
        Vertex lInterface2 = graph.addVertex(T.label, "l-interface", T.id, "7", "aai-node-type",
            "l-interface", "interface-name", "interface-name-2");
        Vertex logicalLink2 = graph.addVertex(T.label, "l", T.id, "8", "aai-node-type",
            "logical-link", "link-name", "link-name-2");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, pserver1, vserver1);
        rules.addTreeEdge(g, vserver1, lInterface1);
        rules.addEdge(g, lInterface1, logicalLink1);

        rules.addEdge(g, pserver2, vserver2);
        rules.addTreeEdge(g, vserver2, lInterface2);
        rules.addEdge(g, lInterface2, logicalLink2);

        expectedResult.add(vserver1);
        expectedResult.add(logicalLink1);
    }

    @Override
    protected String getQueryName() {
        return "vserverlogicallink-frompServer";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("hostname", "hostname-1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }

}
