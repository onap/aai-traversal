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

public class GetCustomerVPNBondingServiceDetailsTest extends QueryTest {

    public GetCustomerVPNBondingServiceDetailsTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void test() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph

        Vertex serviceSubscription = graph.addVertex(T.label, "service-subscription", T.id, "1",
            "aai-node-type", "service-subscription", "service-type", "start-test");
        Vertex customer = graph.addVertex(T.label, "customer", T.id, "2", "aai-node-type",
            "customer", "global-customer-id", "customer-test", "subscriber-name", "test-name",
            "subscriber-type", "test-type");
        Vertex serviceInstance = graph.addVertex(T.label, "service-instance", T.id, "3",
            "aai-node-type", "service-instance", "service-instance-id", "test-instance");
        Vertex configurationOne =
            graph.addVertex(T.label, "configuration", T.id, "4", "aai-node-type", "configuration",
                "configuration-id", "test-config-1", "configuration-type", "VLAN-NETWORK-RECEPTOR");
        Vertex configurationTwo =
            graph.addVertex(T.label, "configuration", T.id, "5", "aai-node-type", "configuration",
                "configuration-id", "test-config-2", "configuration-type", "VLAN-NETWORK-RECEPTOR");
        Vertex badConfiguration =
            graph.addVertex(T.label, "configuration", T.id, "6", "aai-node-type", "configuration",
                "configuration-id", "bad-config", "configuration-type", "bad");
        Vertex genericVnfOne = graph.addVertex(T.label, "generic-vnf", T.id, "7", "aai-node-type",
            "generic-vnf", "vnf-id", "test-generic-vnf-1", "vnf-type", "right-relationship");
        Vertex genericVnfTwo = graph.addVertex(T.label, "generic-vnf", T.id, "8", "aai-node-type",
            "generic-vnf", "vnf-id", "test-generic-vnf-2", "vnf-type", "wrong-relationship");
        Vertex rightInstanceGroup = graph.addVertex(T.label, "instance-group", T.id, "9",
            "aai-node-type", "instance-group", "id", "test-group-right", "description",
            "MemberOf relationship", "instance-group-type", "lower case ha for high availability");
        Vertex wrongInstanceGroup = graph.addVertex(T.label, "instance-group", T.id, "10",
            "aai-node-type", "instance-group", "id", "test-group-wrong", "description",
            "Uses relationship", "instance-group-type", "lower case ha for high availability");
        Vertex l3Network = graph.addVertex(T.label, "l3-network", T.id, "11", "aai-node-type",
            "l3-network", "network-id", "test-l3");
        Vertex subnet = graph.addVertex(T.label, "subnet", T.id, "12", "aai-node-type", "subnet",
            "subnet-id", "test-subnet");
        Vertex l3InterfaceIpv6AddressList =
            graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "13", "aai-node-type",
                "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "test-ipv6");
        Vertex l3InterfaceIpv4AddressList =
            graph.addVertex(T.label, "l3-interface-ipv4-address-list", T.id, "14", "aai-node-type",
                "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "test-ipv4");
        Vertex l3VpnBinding = graph.addVertex(T.label, "vpn-binding", T.id, "15", "aai-node-type",
            "vpn-binding", "vpn-id", "test-binding", "vpn-name", "test");
        Vertex l3RouteTarget = graph.addVertex(T.label, "route-target", T.id, "16", "aai-node-type",
            "route-target", "global-route-target", "test-target", "route-target-role", "test");
        Vertex parentLInterface = graph.addVertex(T.label, "l-interface", T.id, "17",
            "aai-node-type", "l-interface", "interface-name", "parent-test-l-interface");
        Vertex vlan = graph.addVertex(T.label, "vlan", T.id, "18", "aai-node-type", "vlan",
            "vlan-interface", "test-vlan");
        Vertex childLInterface = graph.addVertex(T.label, "l-interface", T.id, "19",
            "aai-node-type", "l-interface", "interface-name", "child-test-l-interface");
        Vertex configurationThree =
            graph.addVertex(T.label, "configuration", T.id, "20", "aai-node-type", "configuration",
                "configuration-id", "test-config-3", "configuration-type", "VRF ENTRY");
        Vertex configVpnBinding = graph.addVertex(T.label, "vpn-binding", T.id, "21",
            "aai-node-type", "vpn-binding", "vpn-id", "test-binding-config", "vpn-name", "test");
        Vertex configRouteTarget =
            graph.addVertex(T.label, "route-target", T.id, "22", "aai-node-type", "route-target",
                "global-route-target", "test-target-config", "route-target-role", "test");
        Vertex pnf = graph.addVertex(T.label, "pnf", T.id, "23", "aai-node-type", "pnf", "pnf-name",
            "test-pnf", "nf-role", "D2IPE");
        Vertex badPnf = graph.addVertex(T.label, "pnf", T.id, "24", "aai-node-type", "pnf",
            "pnf-name", "test-pnf", "nf-role", "none");

        GraphTraversalSource g = graph.traversal();

        rules.addTreeEdge(g, customer, serviceSubscription);
        rules.addTreeEdge(g, serviceSubscription, serviceInstance);
        rules.addEdge(g, serviceInstance, configurationOne);
        rules.addEdge(g, serviceInstance, configurationTwo);
        rules.addEdge(g, serviceInstance, badConfiguration);
        rules.addEdge(g, configurationOne, genericVnfOne);
        rules.addEdge(g, configurationTwo, genericVnfTwo);
        rules.addEdge(g, genericVnfOne, rightInstanceGroup,
            "org.onap.relationships.inventory.MemberOf");
        rules.addEdge(g, genericVnfTwo, wrongInstanceGroup,
            "org.onap.relationships.inventory.Uses");
        rules.addEdge(g, configurationOne, l3Network);
        rules.addTreeEdge(g, l3Network, subnet);
        rules.addEdge(g, subnet, l3InterfaceIpv6AddressList);
        rules.addEdge(g, subnet, l3InterfaceIpv4AddressList);
        rules.addEdge(g, l3Network, l3VpnBinding);
        rules.addTreeEdge(g, l3VpnBinding, l3RouteTarget);
        rules.addEdge(g, configurationOne, parentLInterface);
        rules.addTreeEdge(g, parentLInterface, vlan);
        rules.addTreeEdge(g, parentLInterface, childLInterface);
        rules.addEdge(g, serviceInstance, configurationThree);
        rules.addEdge(g, configurationThree, configVpnBinding);
        rules.addTreeEdge(g, configVpnBinding, configRouteTarget);
        rules.addEdge(g, configurationThree, pnf);
        rules.addEdge(g, configurationThree, badPnf);

        expectedResult.add(customer);
        expectedResult.add(serviceInstance);
        expectedResult.add(configurationOne);
        expectedResult.add(configurationTwo);
        expectedResult.add(genericVnfOne);
        expectedResult.add(genericVnfTwo);
        expectedResult.add(rightInstanceGroup);
        expectedResult.add(l3Network);
        expectedResult.add(subnet);
        expectedResult.add(l3InterfaceIpv6AddressList);
        expectedResult.add(l3InterfaceIpv4AddressList);
        expectedResult.add(l3VpnBinding);
        expectedResult.add(l3RouteTarget);
        expectedResult.add(parentLInterface);
        expectedResult.add(vlan);
        expectedResult.add(childLInterface);
        expectedResult.add(configurationThree);
        expectedResult.add(configVpnBinding);
        expectedResult.add(configRouteTarget);
        expectedResult.add(pnf);
    }

    @Override
    protected String getQueryName() {
        return "getCustomerVPNBondingServiceDetails";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "service-subscription").has("service-type", "start-test");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }

}
