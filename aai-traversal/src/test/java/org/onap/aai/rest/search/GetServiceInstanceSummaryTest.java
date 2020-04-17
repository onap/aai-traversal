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

public class GetServiceInstanceSummaryTest extends QueryTest {
	
	public GetServiceInstanceSummaryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//set up test graph
		Vertex serviceinstance = graph.addVertex(T.label, "service-instance", T.id, "1", "aai-node-type", "service-instance", "service-instance-id", "serviceinstanceid0");
		Vertex l3network = graph.addVertex(T.label, "l3-network", T.id, "2", "aai-node-type", "l3-network", "network-id", "networkid0");
		Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "3", "aai-node-type", "cloud-region", "cloud-region-id", "regionid0", "cloud-owner", "cloudOwnername0");
		Vertex servicesub = graph.addVertex(T.label, "service-subscription", T.id, "4", "aai-node-type", "service-subscription", "service-type", "servicetype0");
		Vertex customer = graph.addVertex(T.label, "customer", T.id, "5", "aai-node-type", "customer", "customer-id", "customerid0");
		Vertex genericvnf = graph.addVertex(T.label, "generic-vnf", T.id, "6", "aai-node-type", "generic-vnf", "vnf-id", "vnfid0");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "7", "aai-node-type", "vserver", "vserver-id", "vserverid0");
		Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "8", "aai-node-type", "tenant", "tenant-id", "tenantid0", "tenant-name", "tenantName0");
		Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "9", "aai-node-type", "cloud-region", "cloud-region-id", "regionid0", "cloud-owner", "cloudOwnername0");
		
		Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "10", "aai-node-type", "service-instance", "service-instance-id", "serviceinstanceid1");
		Vertex l3network1 = graph.addVertex(T.label, "l3-network", T.id, "11", "aai-node-type", "l3-network", "network-id", "networkid0");
		Vertex cloudregion2 = graph.addVertex(T.label, "cloud-region", T.id, "12", "aai-node-type", "cloud-region", "cloud-region-id", "regionid0", "cloud-owner", "cloudOwnername0");
		Vertex servicesub1 = graph.addVertex(T.label, "service-subscription", T.id, "13", "aai-node-type", "service-subscription", "service-type", "servicetype0");
		Vertex customer1 = graph.addVertex(T.label, "customer", T.id, "14", "aai-node-type", "customer", "customer-id", "customerid0");
		Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "15", "aai-node-type", "generic-vnf", "vnf-id", "vnfid0");
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "16", "aai-node-type", "vserver", "vserver-id", "vserverid0");
		Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "17", "aai-node-type", "tenant", "tenant-id", "tenantid0", "tenant-name", "tenantName0");
		Vertex cloudregion3 = graph.addVertex(T.label, "cloud-region", T.id, "18", "aai-node-type", "cloud-region", "cloud-region-id", "regionid0", "cloud-owner", "cloudOwnername0");
		GraphTraversalSource g = graph.traversal();
	
		rules.addEdge(g, serviceinstance, l3network);
		rules.addEdge(g, l3network, cloudregion);
		rules.addTreeEdge(g, serviceinstance, servicesub);
		rules.addTreeEdge(g, servicesub, customer);
		rules.addEdge(g, serviceinstance, genericvnf);
		rules.addEdge(g, genericvnf, vserver);
		rules.addTreeEdge(g, vserver, tenant);
		rules.addTreeEdge(g, tenant, cloudregion1);
		
		rules.addEdge(g, serviceinstance1, l3network1);//false
		rules.addEdge(g, l3network1, cloudregion2);//false
		rules.addTreeEdge(g, serviceinstance1, servicesub1);//false
		rules.addTreeEdge(g, servicesub1, customer1);//false
		rules.addEdge(g, serviceinstance1, genericvnf1);//false
		rules.addEdge(g, genericvnf1, vserver1);//false
		rules.addTreeEdge(g, vserver1, tenant1);//false
		rules.addTreeEdge(g, tenant1, cloudregion3);//false
				
		expectedResult.add(serviceinstance);
		expectedResult.add(cloudregion);
		expectedResult.add(cloudregion1);
		expectedResult.add(servicesub);
		expectedResult.add(customer);
	}
	@Override
	protected String getQueryName() {
		return "getServiceInstanceSummary";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "service-instance").has("service-instance-id", "serviceinstanceid0");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}

}
