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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.Tree;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class GetServiceTopologyTest extends TreeQueryTest {

    public GetServiceTopologyTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph

        Vertex customer = graph.addVertex(T.label, "customer", T.id, "1", "aai-node-type",
            "customer", "global-customer-id", "customer-id-1", "subscriber-name", "customer-name1",
            "subscriber-type", "customer-type1");
        Vertex serviceSubscription = graph.addVertex(T.label, "service-subscription", T.id, "2",
            "aai-node-type", "service-subscription", "service-type", "service-subcription-1");

        Vertex serviceInstanceOne = graph.addVertex(T.label, "service-instance", T.id, "3",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1",
            "service-instance-name", "service-instance-name-1");
        Vertex genericVnfOne = graph.addVertex(T.label, "generic-vnf", T.id, "4", "aai-node-type",
            "generic-vnf", "vnf-name", "vnf-name", "vnf-type", "test", "vnf-id", "vnf-test=1");
        Vertex lInterfaceOne = graph.addVertex(T.label, "l-interface", T.id, "5", "aai-node-type",
            "l-interface", "interface-name", "test-l-interface-one");
        Vertex l3Ipv4AddressListOne =
            graph.addVertex(T.label, "l3-interface-ipv4-address-list", T.id, "6", "aai-node-type",
                "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "test1");
        Vertex l3Ipv6AddressListOne =
            graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "7", "aai-node-type",
                "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "test2");

        Vertex vlanOne = graph.addVertex(T.label, "vlan", T.id, "8", "aai-node-type", "vlan",
            "vlan-interface", "test-vlan-one");
        Vertex l3Ipv4AddressListTwo =
            graph.addVertex(T.label, "l3-interface-ipv4-address-list", T.id, "9", "aai-node-type",
                "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "test3");
        Vertex l3Ipv6AddressListTwo =
            graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "10", "aai-node-type",
                "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "test4");

        Vertex vserverOne = graph.addVertex(T.label, "vserver", T.id, "11", "aai-node-type",
            "vserver", "vserver-id-one", "test-vserver", "vserver-selflink", "test", "vserver-name",
            "test-vserver");
        Vertex lInterfaceTwo = graph.addVertex(T.label, "l-interface", T.id, "12", "aai-node-type",
            "l-interface", "interface-name", "test-l-interface-two");
        Vertex l3Ipv4AddressListThree =
            graph.addVertex(T.label, "l3-interface-ipv4-address-list", T.id, "13", "aai-node-type",
                "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "test5");
        Vertex l3Ipv6AddressListThree =
            graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "14", "aai-node-type",
                "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "test6");

        Vertex vlanTwo = graph.addVertex(T.label, "vlan", T.id, "15", "aai-node-type", "vlan",
            "vlan-interface", "test-vlan-two");
        Vertex l3Ipv4AddressListFour =
            graph.addVertex(T.label, "l3-interface-ipv4-address-list", T.id, "16", "aai-node-type",
                "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "test7");
        Vertex l3Ipv6AddressListFour =
            graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "17", "aai-node-type",
                "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "test8");

        Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "18", "aai-node-type", "pserver",
            "hostname", "test-pserver");
        Vertex complex = graph.addVertex(T.label, "complex", T.id, "19", "aai-node-type", "complex",
            "physical-location-id", "test-complex");

        Vertex allottedResource = graph.addVertex(T.label, "allotted-resource", T.id, "20",
            "aai-node-type", "allotted-resource", "id", "test-resource");
        Vertex serviceInstanceTwo = graph.addVertex(T.label, "service-instance", T.id, "21",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-2",
            "service-instance-name", "service-instance-name-1");
        Vertex genericVnfTwo = graph.addVertex(T.label, "generic-vnf", T.id, "22", "aai-node-type",
            "generic-vnf", "vnf-name", "vnf-name", "vnf-type", "test", "vnf-id", "vnf-test-2");
        Vertex lInterfaceThree = graph.addVertex(T.label, "l-interface", T.id, "23",
            "aai-node-type", "l-interface", "interface-name", "test-l-interface-three");
        Vertex l3Ipv4AddressListFive =
            graph.addVertex(T.label, "l3-interface-ipv4-address-list", T.id, "24", "aai-node-type",
                "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "test9");
        Vertex l3Ipv6AddressListFive =
            graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "25", "aai-node-type",
                "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "test10");

        Vertex vlanThree = graph.addVertex(T.label, "vlan", T.id, "26", "aai-node-type", "vlan",
            "vlan-interface", "test-vlan-three");
        Vertex l3Ipv4AddressListSix =
            graph.addVertex(T.label, "l3-interface-ipv4-address-list", T.id, "27", "aai-node-type",
                "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "test11");
        Vertex l3Ipv6AddressListSix =
            graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "28", "aai-node-type",
                "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "test12");

        Vertex vserverTwo = graph.addVertex(T.label, "vserver", T.id, "29", "aai-node-type",
            "vserver", "vserver-id", "test-vserver", "vserver-selflink", "test", "vserver-name",
            "test-vserver=two");
        Vertex lInterfaceFour = graph.addVertex(T.label, "l-interface", T.id, "30", "aai-node-type",
            "l-interface", "interface-name", "test-l-interface-four");
        Vertex l3Ipv4AddressListSeven =
            graph.addVertex(T.label, "l3-interface-ipv4-address-list", T.id, "31", "aai-node-type",
                "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "test13");
        Vertex l3Ipv6AddressListSeven =
            graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "32", "aai-node-type",
                "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "test14");

        Vertex vlanFour = graph.addVertex(T.label, "vlan", T.id, "33", "aai-node-type", "vlan",
            "vlan-interface", "test-vlan-four");
        Vertex l3Ipv4AddressListEight =
            graph.addVertex(T.label, "l3-interface-ipv4-address-list", T.id, "34", "aai-node-type",
                "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "test15");
        Vertex l3Ipv6AddressListEight =
            graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "35", "aai-node-type",
                "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "test16");

        GraphTraversalSource g = graph.traversal();

        rules.addTreeEdge(g, customer, serviceSubscription);
        rules.addTreeEdge(g, serviceSubscription, serviceInstanceOne);

        rules.addEdge(g, serviceInstanceOne, genericVnfOne);
        rules.addTreeEdge(g, genericVnfOne, lInterfaceOne);
        rules.addTreeEdge(g, lInterfaceOne, l3Ipv4AddressListOne);
        rules.addTreeEdge(g, lInterfaceOne, l3Ipv6AddressListOne);

        rules.addTreeEdge(g, lInterfaceOne, vlanOne);
        rules.addTreeEdge(g, vlanOne, l3Ipv4AddressListTwo);
        rules.addTreeEdge(g, vlanOne, l3Ipv6AddressListTwo);

        rules.addEdge(g, genericVnfOne, vserverOne);
        rules.addTreeEdge(g, vserverOne, lInterfaceTwo);
        rules.addTreeEdge(g, lInterfaceTwo, l3Ipv4AddressListThree);
        rules.addTreeEdge(g, lInterfaceTwo, l3Ipv6AddressListThree);
        rules.addTreeEdge(g, lInterfaceTwo, vlanTwo);
        rules.addTreeEdge(g, vlanTwo, l3Ipv4AddressListFour);
        rules.addTreeEdge(g, vlanTwo, l3Ipv6AddressListFour);
        rules.addEdge(g, vserverOne, pserver);
        rules.addEdge(g, pserver, complex);

        rules.addEdge(g, serviceInstanceOne, allottedResource);
        rules.addTreeEdge(g, allottedResource, serviceInstanceTwo);

        rules.addEdge(g, serviceInstanceTwo, genericVnfTwo);
        rules.addTreeEdge(g, genericVnfTwo, lInterfaceThree);
        rules.addTreeEdge(g, lInterfaceThree, l3Ipv4AddressListFive);
        rules.addTreeEdge(g, lInterfaceThree, l3Ipv6AddressListFive);

        rules.addTreeEdge(g, lInterfaceThree, vlanThree);
        rules.addTreeEdge(g, vlanThree, l3Ipv4AddressListSix);
        rules.addTreeEdge(g, vlanThree, l3Ipv6AddressListSix);

        rules.addEdge(g, genericVnfTwo, vserverTwo);
        rules.addTreeEdge(g, vserverTwo, lInterfaceFour);
        rules.addTreeEdge(g, lInterfaceFour, l3Ipv4AddressListSeven);
        rules.addTreeEdge(g, lInterfaceFour, l3Ipv6AddressListSeven);
        rules.addTreeEdge(g, lInterfaceFour, vlanFour);
        rules.addTreeEdge(g, vlanFour, l3Ipv4AddressListEight);
        rules.addTreeEdge(g, vlanFour, l3Ipv6AddressListEight);

        expectedResult.add(serviceInstanceOne);
        expectedResult.add(genericVnfOne);
        expectedResult.add(lInterfaceOne);
        expectedResult.add(l3Ipv4AddressListOne);
        expectedResult.add(l3Ipv6AddressListOne);
        expectedResult.add(vlanOne);
        expectedResult.add(l3Ipv4AddressListTwo);
        expectedResult.add(l3Ipv6AddressListTwo);

        expectedResult.add(vserverOne);
        expectedResult.add(lInterfaceTwo);
        expectedResult.add(l3Ipv4AddressListThree);
        expectedResult.add(l3Ipv6AddressListThree);

        expectedResult.add(vlanTwo);
        expectedResult.add(l3Ipv4AddressListFour);
        expectedResult.add(l3Ipv6AddressListFour);

        expectedResult.add(pserver);
        expectedResult.add(complex);

        expectedResult.add(allottedResource);
        expectedResult.add(serviceInstanceTwo);
        expectedResult.add(genericVnfTwo);
        expectedResult.add(lInterfaceThree);
        expectedResult.add(l3Ipv4AddressListFive);
        expectedResult.add(l3Ipv6AddressListFive);

        expectedResult.add(vlanThree);
        expectedResult.add(l3Ipv4AddressListSix);
        expectedResult.add(l3Ipv6AddressListSix);

        expectedResult.add(vserverTwo);
        expectedResult.add(lInterfaceFour);
        expectedResult.add(l3Ipv4AddressListSeven);
        expectedResult.add(l3Ipv6AddressListSeven);
        expectedResult.add(vlanFour);
        expectedResult.add(l3Ipv4AddressListEight);
        expectedResult.add(l3Ipv6AddressListEight);

    }

    @Test
    public void run() {
        super.run();
        Tree tree = treeList.get(0);
        ArrayList<Vertex> actualResult = new ArrayList<Vertex>();
        int i = 1;
        do {
            actualResult.addAll(tree.getObjectsAtDepth(i));
            i++;
        } while (!tree.getObjectsAtDepth(i).isEmpty());

        assertEquals("result has expected number of values", actualResult.size(),
            expectedResult.size());
        int size = actualResult.size() == expectedResult.size() ? expectedResult.size() : 0;
        for (i = 0; i < size; i++) {
            assertTrue("result has node " + expectedResult.get(i),
                actualResult.contains(expectedResult.get(i)));
        }

    }

    @Override
    protected String getQueryName() {
        return "getServiceTopology";
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
