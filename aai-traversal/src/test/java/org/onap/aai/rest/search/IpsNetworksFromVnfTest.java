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
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class IpsNetworksFromVnfTest extends QueryTest {

	public IpsNetworksFromVnfTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex genericVnf = graph.addVertex(T.label, "generic-vnf",T.id, "0", "aai-node-type", "generic-vnf", "generic-vnf-id", "serviceinstanceid0");		
		Vertex vnfc = graph.addVertex(T.label, "vnfc",T.id, "1", "aai-node-type", "vnfc","vnfc-id", "vnfcId1");
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
		
		
		GraphTraversalSource g = graph.traversal();
		
		rules.addEdge(g, genericVnf,vnfc);
		rules.addEdge(g, vnfc,vipIpv4AddressList);
		rules.addEdge(g, vnfc,vipIpv6AddressList);		
		
		rules.addEdge(g, vipIpv4AddressList,subnetIpv4);		
		rules.addTreeEdge(g, subnetIpv4,l3Network1Ipv4);
		
		rules.addEdge(g, vipIpv6AddressList,subnetIpv6);		
		rules.addTreeEdge(g, subnetIpv6,l3Network1Ipv6);
		
		rules.addEdge(g, genericVnf,vserver);		
		rules.addEdge(g, vserver,pserver);
		
		rules.addTreeEdge(g, vnfc,l3InterfaceIpv4AddressListLint);
		rules.addTreeEdge(g, vnfc,l3InterfaceIpv6AddressListlInt);
		
		rules.addEdge(g, l3InterfaceIpv4AddressListLint,l3NetworklintIpv4);		
		rules.addEdge(g, l3InterfaceIpv4AddressListLint,subnetlintIpv4);		
		rules.addTreeEdge(g, subnetlintIpv4,l3Network1lintIpv4);
				
		rules.addEdge(g, l3InterfaceIpv6AddressListlInt,l3NetworklintIpv6);		
		rules.addEdge(g, l3InterfaceIpv6AddressListlInt,subnetlintIpv6);		
		rules.addTreeEdge(g, subnetlintIpv6,l3Network1lintIpv6);
		
		rules.addEdge(g, pserver,complex);		
				
		
		expectedResult.add(genericVnf);
		expectedResult.add(vnfc);
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
		g.has("aai-node-type", "generic-vnf");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}

	
}
