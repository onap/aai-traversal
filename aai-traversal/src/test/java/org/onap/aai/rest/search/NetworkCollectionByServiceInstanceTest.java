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

public class NetworkCollectionByServiceInstanceTest extends QueryTest {
    public NetworkCollectionByServiceInstanceTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        // Set up the test graph
        Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "0",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1",
            "service-instance-name", "service-instance-name-1");

        Vertex collection1 =
            graph.addVertex(T.label, "collection", T.id, "1", "aai-node-type", "collection");

        Vertex instancegroup1 =
            graph.addVertex(T.label, "instance-group", T.id, "2", "aai-node-type", "instance-group",
                "id", "id-0", "instance-group-type", "instance-group-type-0", "instance-group-role",
                "instance-group-role-0", "instance-group-function", "instance-group-function-0",
                "instance-group-description", "instance-group-description-0");

        Vertex l3network1 =
            graph.addVertex(T.label, "l3-network", T.id, "3", "aai-node-type", "l3-network");

        Vertex serviceinstance2 = graph.addVertex(T.label, "service-instance", T.id, "4",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-2",
            "service-instance-name", "service-instance-name-1");

        Vertex collection2 =
            graph.addVertex(T.label, "collection", T.id, "5", "aai-node-type", "collection");

        Vertex instancegroup2 =
            graph.addVertex(T.label, "instance-group", T.id, "6", "aai-node-type", "instance-group",
                "id", "id-0", "instance-group-type", "instance-group-type-0", "instance-group-role",
                "instance-group-role-0", "instance-group-function", "instance-group-function-0",
                "instance-group-description", "instance-group-description-0");

        Vertex l3network2 =
            graph.addVertex(T.label, "l3-network", T.id, "7", "aai-node-type", "l3-network");

        GraphTraversalSource g = graph.traversal();

        rules.addEdge(g, serviceinstance1, collection1); // True
        rules.addEdge(g, collection1, instancegroup1); // True
        rules.addEdge(g, instancegroup1, l3network1); // True

        rules.addEdge(g, serviceinstance2, collection2); // False
        rules.addEdge(g, collection2, instancegroup2); // False
        rules.addEdge(g, instancegroup2, l3network2);// False

        expectedResult.add(serviceinstance1);
        expectedResult.add(collection1);
        expectedResult.add(instancegroup1);
        expectedResult.add(l3network1);

    }

    @Override
    protected String getQueryName() {
        return "network-collection-ByServiceInstance";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "service-instance").has("service-instance-id",
            "service-instance-id-1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
