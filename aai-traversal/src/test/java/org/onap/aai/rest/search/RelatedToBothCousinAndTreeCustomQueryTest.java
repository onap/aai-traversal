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
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.query.builder.MissingOptionalParameter;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;

public class RelatedToBothCousinAndTreeCustomQueryTest extends QueryTest{

    public RelatedToBothCousinAndTreeCustomQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }
    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        Vertex lagInterface1 = graph.addVertex(T.label, "lag-interface", T.id, "1", "aai-node-type", "lag-interface", "interface-name", "lag-int1");

        Vertex lInterface1 = graph.addVertex(T.label, "l-interface", T.id, "2", "aai-node-type", "l-interface", "interface-name", "l-interfaceid01");
        Vertex lInterface2 = graph.addVertex(T.label, "l-interface", T.id, "3", "aai-node-type", "l-interface", "interface-name", "l-interfaceid02");
        Vertex lInterface3 = graph.addVertex(T.label, "l-interface", T.id, "4", "aai-node-type", "l-interface", "interface-name", "l-interfaceid03");
        Vertex lInterface4 = graph.addVertex(T.label, "l-interface", T.id, "5", "aai-node-type", "l-interface", "interface-name", "l-interfaceid04");
        Vertex lInterface5 = graph.addVertex(T.label, "l-interface", T.id, "6", "aai-node-type", "l-interface", "interface-name", "l-interfaceid05");
        Vertex lInterface6 = graph.addVertex(T.label, "l-interface", T.id, "7", "aai-node-type", "l-interface", "interface-name", "l-interfaceid06");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, lagInterface1, lInterface1);
        rules.addEdge(g, lagInterface1, lInterface2);
        rules.addEdge(g, lagInterface1, lInterface3);
        rules.addTreeEdge(g, lagInterface1, lInterface4);
        rules.addTreeEdge(g, lagInterface1, lInterface5);
        rules.addTreeEdge(g, lagInterface1, lInterface6);

        expectedResult.add(lInterface1);
        expectedResult.add(lInterface2);
        expectedResult.add(lInterface3);
        expectedResult.add(lInterface4);
        expectedResult.add(lInterface5);
        expectedResult.add(lInterface6);

    }

    @Override
    protected String getQueryName() {
        return "related-to";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("interface-name", "lag-int1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        params.put("startingNodeType", "lag-interface");
        params.put("relatedToNodeType", "l-interface");
        params.put("edgeType", MissingOptionalParameter.getInstance());

    }

}
