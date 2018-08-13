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

public class GfpVserverDataQueryTest extends QueryTest {
	public GfpVserverDataQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//set up test graph
		Vertex genericvnf = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type", "generic-vnf", "vnf-id", "vnfid0", "nf-type", "sample-nf-type");
		Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "1", "aai-node-type", "cloud-region", "cloud-region-id", "regionid0", "cloud-owner", "cloudOwnername0");
		Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "2", "aai-node-type", "tenant", "tenant-id", "tenantid0", "tenant-name", "tenantName0");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver", "vserver-id", "vserverid0");
		Vertex linterface = graph.addVertex(T.label, "l-interface", T.id, "4", "aai-node-type", "l-interface", "l-interface-id", "l-interface-id0", "l-interface-name", "l-interface-name0");
		Vertex l3inter1ipv4addresslist = graph.addVertex(T.label, "interface-ipv4-address-list", T.id, "5", "aai-node-type", "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "l3-interface-ipv4-address-0");
		Vertex l3inter1ipv6addresslist = graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "8", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "l3-interface-ipv6-address-0");
		Vertex vlan = graph.addVertex(T.label, "vlan", T.id, "9", "aai-node-type", "vlan", "vlan-interface", "vlan-interface0");
		Vertex sriovVf = graph.addVertex(T.label, "sriov-vf", T.id, "10", "aai-node-type", "sriov-vf", "pci-id", "pci-id0");
		
		GraphTraversalSource g = graph.traversal();
	
		
		rules.addTreeEdge(g, tenant, cloudregion);
		rules.addTreeEdge(g, vserver, tenant);
		rules.addEdge(g, genericvnf, vserver);
		rules.addTreeEdge(g, linterface, vserver);
		rules.addTreeEdge(g, l3inter1ipv4addresslist, linterface);
        rules.addTreeEdge(g, l3inter1ipv6addresslist, linterface);
        rules.addTreeEdge(g, vlan, linterface);
        rules.addTreeEdge(g, sriovVf, linterface);
				
		expectedResult.add(l3inter1ipv4addresslist);
		expectedResult.add(l3inter1ipv6addresslist);
		expectedResult.add(vlan);
	}
	@Override
	protected String getQueryName() {
		return	"gfp-vserver-data";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "vserver");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}