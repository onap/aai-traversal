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
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;
public class RelatedToCustomQueryTest extends QueryTest{

    public RelatedToCustomQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }
    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type", "generic-vnf", "vnf-id", "genvnf1", "vnf-name", "genvnfname1", "nf-type", "sample-nf-type");

        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "2", "aai-node-type", "vserver", "vserver-id", "vserverid01");
        Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver", "vserver-id", "vserverid02");
        Vertex vserver3 = graph.addVertex(T.label, "vserver", T.id, "4", "aai-node-type", "vserver", "vserver-id", "vserverid03");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, genericvnf1, vserver1);
        rules.addEdge(g, genericvnf1, vserver2);
        rules.addEdge(g, genericvnf1, vserver3);

        expectedResult.add(vserver1);
        expectedResult.add(vserver2);
        expectedResult.add(vserver3);

    }

    @Override
    protected String getQueryName() {
        return "related-to";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("vnf-id", "genvnf1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        params.put("startingNodeType", "generic-vnf");
        params.put("relatedToNodeType", "vserver");


    }

}
