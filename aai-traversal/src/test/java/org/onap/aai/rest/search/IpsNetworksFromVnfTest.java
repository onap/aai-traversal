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

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.junit.Ignore;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class IpsNetworksFromVnfTest extends QueryTest {

	public IpsNetworksFromVnfTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Ignore
	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex genericVnf = graph.addVertex(T.label, "generic-vnf",T.id, "0", "aai-node-type", "generic-vnf", "generic-vnf-id", "serviceinstanceid0");
		Vertex vnfc = graph.addVertex(T.label, "vnfc",T.id, "1", "aai-node-type", "vnfc","vnfc-id", "vnfcId1");
		Vertex cp = graph.addVertex(T.label, "cp",T.id, "30", "aai-node-type", "cp","cp-id", "cpId1");
		Vertex vipIpv4AddressList = graph.addVertex(T.label, "vip-ipv4-address-list",T.id, "2", "aai-node-type", "vip-ipv4-address-list","vip-ipv4-address-list-id", "vip-ipv4-address-listId2");
		Vertex vipIpv6AddressList = graph.addVertex(T.label, "vip-ipv6-address-list",T.id, "3", "aai-node-type", "vip-ipv6-address-list","vip-ipv6-address-list-id", "vip-ipv6-address-listId3");


		Vertex subnetIpv4 = graph.addVertex(T.label, "subnet",T.id, "7", "aai-node-type", "subnet","subnet-id", "subnetId7");
		Vertex l3Network1Ipv4 = graph.addVertex(T.label, "l3-network",T.id, "8", "aai-node-type", "l3-network","l3-network-id", "l3-networkId8");

		Vertex subnetIpv6 = graph.addVertex(T.label, "subnet",T.id, "10", "aai-node-type", "subnet","subnet-id", "subnetId10");
		Vertex l3Network1Ipv6 = graph.addVertex(T.label, "l3-network",T.id, "11", "aai-node-type", "l3-network","l3-network-id", "l3-networkId11");

		Vertex vserver = graph.addVertex(T.label, "vserver",T.id, "18", "aai-node-type", "vserver","vserver-id", "vserverId18");
		Vertex linterface = graph.addVertex(T.label, "l-interface",T.id, "19", "aai-node-type", "l-interface","l-interface-id", "l-interfaceId19");
		Vertex pserver = graph.addVertex(T.label, "pserver",T.id, "20", "aai-node-type", "pserver","pserver-id", "pserverId20");


		Vertex l3NetworklintIpv4 = graph.addVertex(T.label, "l3-network",T.id, "21", "aai-node-type", "l3-network","l3-network-id", "l3-networkId21");
		Vertex subnetlintIpv4 = graph.addVertex(T.label, "subnet",T.id, "22", "aai-node-type", "subnet","subnet-id", "subnetId22");
		Vertex l3Network1lintIpv4 = graph.addVertex(T.label, "l3-network",T.id, "23", "aai-node-type", "l3-network","l3-network-id", "l3-networkId23");

		Vertex l3NetworklintIpv6 = graph.addVertex(T.label, "l3-network",T.id, "24", "aai-node-type", "l3-network","l3-network-id", "l3-networkId24");
		Vertex subnetlintIpv6 = graph.addVertex(T.label, "subnet",T.id, "25", "aai-node-type", "subnet","subnet-id", "subnetId25");
		Vertex l3Network1lintIpv6 = graph.addVertex(T.label, "l3-network",T.id, "26", "aai-node-type", "l3-network","l3-network-id", "l3-networkId26");


		Vertex l3InterfaceIpv4AddressListLint = graph.addVertex(T.label, "l3-interface-ipv4-address-list",T.id, "27", "aai-node-type", "l3-interface-ipv4-address-list","l3-interface-ipv4-address-list-id", "l3-interface-ipv4-address-listId27");
		Vertex l3InterfaceIpv6AddressListlInt = graph.addVertex(T.label, "l3-interface-ipv6-address-list",T.id, "28", "aai-node-type", "l3-interface-ipv6-address-list","l3-interface-ipv6-address-list-id", "l3-interface-ipv6-address-listId28");


		Vertex complex = graph.addVertex(T.label, "complex",T.id, "29", "aai-node-type", "complex","complex-id", "complexId29");
		
		
		
		Vertex genericVnf1 = graph.addVertex(T.label, "generic-vnf",T.id, "31", "aai-node-type", "generic-vnf", "generic-vnf-id", "serviceinstanceid1");
		Vertex vnfc1 = graph.addVertex(T.label, "vnfc",T.id, "32", "aai-node-type", "vnfc","vnfc-id", "vnfcId2");
		Vertex cp1 = graph.addVertex(T.label, "cp",T.id, "33", "aai-node-type", "cp","cp-id", "cpId3");
		Vertex vipIpv4AddressList1 = graph.addVertex(T.label, "vip-ipv4-address-list",T.id, "34", "aai-node-type", "vip-ipv4-address-list","vip-ipv4-address-list-id", "vip-ipv4-address-listId4");
		Vertex vipIpv6AddressList1 = graph.addVertex(T.label, "vip-ipv6-address-list",T.id, "35", "aai-node-type", "vip-ipv6-address-list","vip-ipv6-address-list-id", "vip-ipv6-address-listId5");
		Vertex subnetIpv41 = graph.addVertex(T.label, "subnet",T.id, "36", "aai-node-type", "subnet","subnet-id", "subnetId6");
		Vertex l3Network1Ipv41 = graph.addVertex(T.label, "l3-network",T.id, "37", "aai-node-type", "l3-network","l3-network-id", "l3-networkId7");
		Vertex subnetIpv61 = graph.addVertex(T.label, "subnet",T.id, "38", "aai-node-type", "subnet","subnet-id", "subnetId8");
		Vertex l3Network1Ipv61 = graph.addVertex(T.label, "l3-network",T.id, "39", "aai-node-type", "l3-network","l3-network-id", "l3-networkId9");
		Vertex vserver1 = graph.addVertex(T.label, "vserver",T.id, "40", "aai-node-type", "vserver","vserver-id", "vserverId10");
		Vertex linterface1 = graph.addVertex(T.label, "l-interface",T.id, "41", "aai-node-type", "l-interface","l-interface-id", "l-interfaceId11");
		Vertex pserver1 = graph.addVertex(T.label, "pserver",T.id, "42", "aai-node-type", "pserver","pserver-id", "pserverId12");
		Vertex l3NetworklintIpv41 = graph.addVertex(T.label, "l3-network",T.id, "43", "aai-node-type", "l3-network","l3-network-id", "l3-networkId13");
		Vertex subnetlintIpv41 = graph.addVertex(T.label, "subnet",T.id, "44", "aai-node-type", "subnet","subnet-id", "subnetId14");
		Vertex l3Network1lintIpv41 = graph.addVertex(T.label, "l3-network",T.id, "45", "aai-node-type", "l3-network","l3-network-id", "l3-networkId15");
		Vertex l3NetworklintIpv61 = graph.addVertex(T.label, "l3-network",T.id, "46", "aai-node-type", "l3-network","l3-network-id", "l3-networkId16");
		Vertex subnetlintIpv61 = graph.addVertex(T.label, "subnet",T.id, "47", "aai-node-type", "subnet","subnet-id", "subnetId17");
		Vertex l3Network1lintIpv61 = graph.addVertex(T.label, "l3-network",T.id, "48", "aai-node-type", "l3-network","l3-network-id", "l3-networkId18");
		Vertex l3InterfaceIpv4AddressListLint1 = graph.addVertex(T.label, "l3-interface-ipv4-address-list",T.id, "49", "aai-node-type", "l3-interface-ipv4-address-list","l3-interface-ipv4-address-list-id", "l3-interface-ipv4-address-listId19");
		Vertex l3InterfaceIpv6AddressListlInt1 = graph.addVertex(T.label, "l3-interface-ipv6-address-list",T.id, "50", "aai-node-type", "l3-interface-ipv6-address-list","l3-interface-ipv6-address-list-id", "l3-interface-ipv6-address-listId20");
		Vertex complex1 = graph.addVertex(T.label, "complex",T.id, "51", "aai-node-type", "complex","complex-id", "complexId21");


		GraphTraversalSource g = graph.traversal();

		rules.addEdge(g, genericVnf,vnfc);
		rules.addTreeEdge(g, vnfc,cp);
		rules.addEdge(g, cp,vipIpv4AddressList);
		rules.addEdge(g, cp,vipIpv6AddressList);

		rules.addEdge(g, vipIpv4AddressList,subnetIpv4);
		rules.addTreeEdge(g, subnetIpv4,l3Network1Ipv4);

		rules.addEdge(g, vipIpv6AddressList,subnetIpv6);
		rules.addTreeEdge(g, subnetIpv6,l3Network1Ipv6);

		rules.addEdge(g, genericVnf,vserver);
		rules.addEdge(g, vserver,pserver);

		rules.addTreeEdge(g, cp,l3InterfaceIpv4AddressListLint);
		rules.addTreeEdge(g, cp,l3InterfaceIpv6AddressListlInt);

		rules.addEdge(g, l3InterfaceIpv4AddressListLint,l3NetworklintIpv4);
		rules.addEdge(g, l3InterfaceIpv4AddressListLint,subnetlintIpv4);
		rules.addTreeEdge(g, subnetlintIpv4,l3Network1lintIpv4);

		rules.addEdge(g, l3InterfaceIpv6AddressListlInt,l3NetworklintIpv6);
		rules.addEdge(g, l3InterfaceIpv6AddressListlInt,subnetlintIpv6);
		rules.addTreeEdge(g, subnetlintIpv6,l3Network1lintIpv6);

		rules.addEdge(g, pserver,complex);
		
		//false
		rules.addEdge(g, genericVnf1,vnfc1);
		rules.addTreeEdge(g, vnfc1,cp1);
		rules.addEdge(g, cp1,vipIpv4AddressList1);
		rules.addEdge(g, cp1,vipIpv6AddressList1);
		rules.addEdge(g, vipIpv4AddressList1,subnetIpv41);
		rules.addTreeEdge(g, subnetIpv41,l3Network1Ipv41);
		rules.addEdge(g, vipIpv6AddressList1,subnetIpv61);
		rules.addTreeEdge(g, subnetIpv61,l3Network1Ipv61);
		rules.addEdge(g, genericVnf1,vserver1);
		rules.addEdge(g, vserver1,pserver1);
		rules.addTreeEdge(g, cp1,l3InterfaceIpv4AddressListLint1);
		rules.addTreeEdge(g, cp1,l3InterfaceIpv6AddressListlInt1);
		rules.addEdge(g, l3InterfaceIpv4AddressListLint1,l3NetworklintIpv41);
		rules.addEdge(g, l3InterfaceIpv4AddressListLint1,subnetlintIpv41);
		rules.addTreeEdge(g, subnetlintIpv41,l3Network1lintIpv41);
		rules.addEdge(g, l3InterfaceIpv6AddressListlInt1,l3NetworklintIpv61);
		rules.addEdge(g, l3InterfaceIpv6AddressListlInt1,subnetlintIpv61);
		rules.addTreeEdge(g, subnetlintIpv61,l3Network1lintIpv61);
		rules.addEdge(g, pserver1,complex1);
		//false

		expectedResult.add(genericVnf);
		expectedResult.add(vnfc);
		expectedResult.add(cp);
		expectedResult.add(vipIpv4AddressList);
		expectedResult.add(vipIpv6AddressList);

		expectedResult.add(subnetIpv4);
		expectedResult.add(l3Network1Ipv4);

		expectedResult.add(subnetIpv6);
		expectedResult.add(l3Network1Ipv6);

		expectedResult.add(l3InterfaceIpv4AddressListLint);
		expectedResult.add(l3InterfaceIpv6AddressListlInt);

		expectedResult.add(l3NetworklintIpv4);
		expectedResult.add(subnetlintIpv4);
		expectedResult.add(l3Network1lintIpv4);

		expectedResult.add(l3NetworklintIpv6);
		expectedResult.add(subnetlintIpv6);
		expectedResult.add(l3Network1lintIpv6);

		expectedResult.add(vserver);
		expectedResult.add(pserver);
		expectedResult.add(complex);

	}

	@Override
	protected String getQueryName() {
		return "ips-networks-fromVnf";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "generic-vnf").has("generic-vnf-id", "serviceinstanceid0");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}

	
}
