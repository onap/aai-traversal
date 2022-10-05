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

public class GetWlBundleIdQueryTest extends QueryTest {
    public GetWlBundleIdQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        Vertex lagLink = graph.addVertex(T.label, "lag-link", T.id, "1", "aai-node-type",
            "lag-link", "lag-link-id", "lag-link-id-1", "link-name", "link-name-1");
        Vertex logicalLink = graph.addVertex(T.label, "logical-link", T.id, "2", "aai-node-type",
            "logical-link", "logical-link-id", "logical-link-id-1");
        Vertex serviceInstance = graph.addVertex(T.label, "service-instance", T.id, "3",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1");
        Vertex serviceSubcription =
            graph.addVertex(T.label, "service-subscription", T.id, "4", "aai-node-type",
                "service-subscription", "service-subscription-id", "service-subscription-id-1");
        Vertex customer = graph.addVertex(T.label, "customer", T.id, "5", "aai-node-type",
            "customer", "customer-id", "customer-id-1");
        Vertex logicalLink1 = graph.addVertex(T.label, "logical-link", T.id, "6", "aai-node-type",
            "logical-link", "logical-link-id", "logical-link-id-2");

        Vertex lagLink1 = graph.addVertex(T.label, "lag-link", T.id, "7", "aai-node-type",
            "lag-link", "lag-link-id", "lag-link-id-2", "link-name", "link-name-2");
        Vertex logicalLink2 = graph.addVertex(T.label, "logical-link", T.id, "8", "aai-node-type",
            "logical-link", "logical-link-id", "logical-link-id-3");
        Vertex serviceInstance1 = graph.addVertex(T.label, "service-instance", T.id, "9",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-2");
        Vertex serviceSubcription1 =
            graph.addVertex(T.label, "service-subscription", T.id, "10", "aai-node-type",
                "service-subscription", "service-subscription-id", "service-subscription-id-2");
        Vertex customer1 = graph.addVertex(T.label, "customer", T.id, "11", "aai-node-type",
            "customer", "customer-id", "customer-id-2");
        Vertex logicalLink3 = graph.addVertex(T.label, "logical-link", T.id, "12", "aai-node-type",
            "logical-link", "logical-link-id", "logical-link-id-4");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, lagLink, logicalLink);
        rules.addEdge(g, logicalLink, serviceInstance);
        rules.addTreeEdge(g, serviceInstance, serviceSubcription);
        rules.addTreeEdge(g, serviceSubcription, customer);
        rules.addEdge(g, logicalLink, logicalLink1);

        // Not expected in result
        rules.addEdge(g, lagLink1, logicalLink2);
        rules.addEdge(g, logicalLink2, serviceInstance1);
        rules.addTreeEdge(g, serviceInstance1, serviceSubcription1);
        rules.addTreeEdge(g, serviceSubcription1, customer1);
        rules.addEdge(g, logicalLink2, logicalLink3);
        // Not expected in result

        expectedResult.add(lagLink);
        expectedResult.add(logicalLink);
        expectedResult.add(serviceInstance);
        expectedResult.add(serviceSubcription);
        expectedResult.add(customer);
        expectedResult.add(logicalLink1);

    }

    @Override
    protected String getQueryName() {
        return "getWlBundleId";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "lag-link").has("link-name", "link-name-1");

    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
