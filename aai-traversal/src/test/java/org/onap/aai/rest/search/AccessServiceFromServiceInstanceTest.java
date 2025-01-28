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

public class AccessServiceFromServiceInstanceTest extends QueryTest {

    public AccessServiceFromServiceInstanceTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void test() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex serviceInstance = graph.addVertex(T.label, "service-instance", T.id, "1",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1",
            "service-instance-name", "service-instance-name-1");
        Vertex serviceSubscription = graph.addVertex(T.label, "service-subscription", T.id, "2",
            "aai-node-type", "service-subscription", "service-type", "service-subcription-1");
        Vertex customer = graph.addVertex(T.label, "customer", T.id, "3", "aai-node-type",
            "customer", "global-customer-id", "customer-id-1", "subscriber-name", "customer-name1",
            "subscriber-type", "customer-type1");
        Vertex forwardingPath = graph.addVertex(T.label, "forwarding-path", T.id, "4",
            "aai-node-type", "forwarding-path", "forwarding-path-id", "forwarding-path-id-1",
            "forwarding-path-name", "forwarding-path-name-1");
        Vertex configuration = graph.addVertex(T.label, "configuration", T.id, "5", "aai-node-type",
            "configuration", "configuration-id", "configuration-1", "configuration-type",
            "configuration-type-1", "configuration-sub-type", "configuration-sub-type-1");
        Vertex evc =
            graph.addVertex(T.label, "evc", T.id, "6", "aai-node-type", "evc", "evc-id", "evc-1");
        Vertex forwarder = graph.addVertex(T.label, "forwarder", T.id, "7", "aai-node-type",
            "forwarder", "sequence", "forwarder-1");
        Vertex forwarderEvc = graph.addVertex(T.label, "forwarder-evc", T.id, "8", "aai-node-type",
            "forwarder-evc", "forwarder-evc-id", "forwarder-evc-1");
        Vertex pInterface = graph.addVertex(T.label, "p-interface", T.id, "9", "aai-node-type",
            "p-interface", "interface-name", "p-interface-1");
        Vertex pnf = graph.addVertex(T.label, "pnf", T.id, "10", "aai-node-type", "pnf", "pnf-name",
            "pnf1name");
        Vertex lagInterface = graph.addVertex(T.label, "lag-interface", T.id, "11", "aai-node-type",
            "lag-interface", "interface-name", "lagint1");
        Vertex logicalLink = graph.addVertex(T.label, "logical-link", T.id, "12", "aai-node-type",
            "logical-link", "link-name", "logical-link-1", "link-type", "LAG");
        Vertex badLogicalLink = graph.addVertex(T.label, "logical-link", T.id, "13",
            "aai-node-type", "logical-link", "link-name", "logical-link-bad", "link-type", "BAD");
        Vertex wrongInterfaceOne = graph.addVertex(T.label, "l-interface", T.id, "14",
            "aai-node-type", "l-interface", "interface-name", "wrong-interface-1");
        Vertex wrongInterfaceTwo = graph.addVertex(T.label, "l-interface", T.id, "15",
            "aai-node-type", "l-interface", "interface-name", "wrong-interface-2");
        Vertex wrongInterfaceThree = graph.addVertex(T.label, "l-interface", T.id, "16",
            "aai-node-type", "l-interface", "interface-name", "wrong-interface-3");
        Vertex wrongInterfaceFour = graph.addVertex(T.label, "l-interface", T.id, "17",
            "aai-node-type", "l-interface", "interface-name", "wrong-interface-4");
        Vertex vlanMapping = graph.addVertex(T.label, "vlan-mapping", T.id, "18", "aai-node-type",
            "vlan-mapping", "vlan-mapping-id", "vlan-mapping-1");

        GraphTraversalSource g = graph.traversal();

        rules.addTreeEdge(g, serviceInstance, serviceSubscription);
        rules.addTreeEdge(g, serviceSubscription, customer);
        rules.addEdge(g, serviceInstance, forwardingPath);
        rules.addEdge(g, forwardingPath, configuration);
        rules.addTreeEdge(g, configuration, evc);
        rules.addTreeEdge(g, forwardingPath, forwarder);
        rules.addEdge(g, forwarder, configuration);
        rules.addTreeEdge(g, configuration, forwarderEvc);
        rules.addTreeEdge(g, vlanMapping, forwarderEvc);
        rules.addEdge(g, forwarder, pInterface);
        rules.addTreeEdge(g, pnf, pInterface);
        rules.addEdge(g, forwarder, lagInterface);
        rules.addTreeEdge(g, lagInterface, pnf);
        rules.addEdge(g, logicalLink, lagInterface);

        // incorrect nodes
        rules.addEdge(g, badLogicalLink, lagInterface);
        rules.addEdge(g, configuration, wrongInterfaceOne);
        rules.addEdge(g, forwarder, wrongInterfaceTwo);
        rules.addTreeEdge(g, pInterface, wrongInterfaceThree);
        rules.addTreeEdge(g, lagInterface, wrongInterfaceFour);

        expectedResult.add(serviceInstance);
        expectedResult.add(serviceSubscription);
        expectedResult.add(customer);
        expectedResult.add(forwardingPath);
        expectedResult.add(configuration);
        expectedResult.add(evc);
        expectedResult.add(forwarder);
        expectedResult.add(forwarderEvc);
        expectedResult.add(vlanMapping);
        expectedResult.add(pInterface);
        expectedResult.add(pnf);
        expectedResult.add(lagInterface);
        expectedResult.add(logicalLink);

    }

    @Override
    protected String getQueryName() {
        return "access-service-fromServiceInstance";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "customer").has("global-customer-id", "customer-id-1")
            .in("org.onap.relationships.inventory.BelongsTo")
            .has("aai-node-type", "service-subscription")
            .has("service-type", "service-subcription-1")
            .in("org.onap.relationships.inventory.BelongsTo")
            .has("aai-node-type", "service-instance")
            .has("service-instance-id", "service-instance-id-1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }

}
