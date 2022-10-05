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

public class VnfVserverFromCustomerTest extends QueryTest {
    public VnfVserverFromCustomerTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex customer1 = graph.addVertex(T.label, "customer", T.id, "0", "aai-node-type",
            "customer", "global-customer-id", "customer1", "subscriber-type", "INFRA");
        Vertex service1 = graph.addVertex(T.label, "service-subscription", T.id, "1",
            "aai-node-type", "service-subscription", "service-type", "service1");
        Vertex instance = graph.addVertex(T.label, "service-instance", T.id, "2", "aai-node-type",
            "service-instance");
        Vertex vnf =
            graph.addVertex(T.label, "generic-vnf", T.id, "3", "aai-node-type", "generic-vnf");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "4", "aai-node-type", "vserver");

        Vertex customer2 = graph.addVertex(T.label, "customer", T.id, "5", "aai-node-type",
            "customer", "global-customer-id", "customer2", "subscriber-type", "INFRA2");
        Vertex service2 = graph.addVertex(T.label, "service-subscription", T.id, "6",
            "aai-node-type", "service-subscription", "service-type", "service2");
        Vertex instance2 = graph.addVertex(T.label, "service-instance", T.id, "7", "aai-node-type",
            "service-instance");
        Vertex vnf2 =
            graph.addVertex(T.label, "generic-vnf", T.id, "8", "aai-node-type", "generic-vnf");
        Vertex vserver2 =
            graph.addVertex(T.label, "vserver", T.id, "9", "aai-node-type", "vserver");

        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, customer1, service1);
        rules.addTreeEdge(g, service1, instance);
        rules.addEdge(g, vnf, instance);
        rules.addEdge(g, vnf, vserver);

        rules.addTreeEdge(g, customer2, service2); // false
        rules.addTreeEdge(g, service2, instance2); // false
        rules.addEdge(g, vnf2, instance2); // false
        rules.addEdge(g, vnf2, vserver2);// false

        expectedResult.add(vnf);
        expectedResult.add(vserver);

    }

    @Override
    protected String getQueryName() {
        return "vnf-vserver-fromCustomer";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "customer").has("subscriber-type", "INFRA");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
