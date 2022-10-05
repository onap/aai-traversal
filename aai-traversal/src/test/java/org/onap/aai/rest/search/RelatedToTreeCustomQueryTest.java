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

public class RelatedToTreeCustomQueryTest extends QueryTest {

    public RelatedToTreeCustomQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        Vertex genericvnf1 =
            graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type", "generic-vnf",
                "vnf-id", "genvnf1", "vnf-name", "genvnfname1", "nf-type", "sample-nf-type");

        Vertex entitlement1 = graph.addVertex(T.label, "entitlement", T.id, "2", "aai-node-type",
            "entitlement", "group-uuid", "entitlementid01", "resource-uuid", "rentitlementid01");
        Vertex entitlement2 = graph.addVertex(T.label, "entitlement", T.id, "3", "aai-node-type",
            "entitlement", "group-uuid", "entitlementid02", "resource-uuid", "rentitlementid02");
        Vertex entitlement3 = graph.addVertex(T.label, "entitlement", T.id, "4", "aai-node-type",
            "entitlement", "group-uuid", "entitlementid03", "resource-uuid", "rentitlementid03");

        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, genericvnf1, entitlement1);
        rules.addTreeEdge(g, genericvnf1, entitlement2);
        rules.addTreeEdge(g, genericvnf1, entitlement3);

        expectedResult.add(entitlement1);
        expectedResult.add(entitlement2);
        expectedResult.add(entitlement3);

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
        params.put("relatedToNodeType", "entitlement");
        params.put("edgeType", "TREE");
    }

}
