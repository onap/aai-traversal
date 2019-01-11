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

public class GetTenantInfoAtSvcInstanceQueryTest extends QueryTest {
	public GetTenantInfoAtSvcInstanceQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {	
		
		
		Vertex serviceInstance = graph.addVertex(T.label, "service-instance", T.id, "1", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1");
		Vertex gnvf = graph.addVertex(T.label, "generic-vnf", T.id, "2", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-1");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver", "vserver-id", "vserver-id-1","vserver-name","vserver-name-1");
		Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "4", "aai-node-type", "tenant", "tenant-id", "tenantid01", "tenant-name", "tenantName01");
		Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "5", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region-id-1", "cloud-region-owner", "cloud-owner-name-1");
		Vertex complex = graph.addVertex(T.label, "complex", T.id, "6", "aai-node-type", "complex", "complex-id", "complex-1");
		
		Vertex serviceInstance1 = graph.addVertex(T.label, "service-instance", T.id, "7", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-2");
		Vertex gnvf1 = graph.addVertex(T.label, "generic-vnf", T.id, "8", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-2");
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "9", "aai-node-type", "vserver", "vserver-id", "vserver-id-2","vserver-name","vserver-name-2");
		Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "10", "aai-node-type", "tenant", "tenant-id", "tenantid02", "tenant-name", "tenantName02");
		Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "11", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region-id-2", "cloud-region-owner", "cloud-owner-name-2");
		Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "12", "aai-node-type", "complex", "complex-id", "complex-2");
		
		
		
		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, serviceInstance, gnvf);
		rules.addEdge(g, gnvf, vserver);
		rules.addTreeEdge(g, vserver,tenant);
		rules.addTreeEdge(g, tenant,cloudregion);
		rules.addEdge(g, cloudregion, complex);
		
		//Not expected in result
		rules.addEdge(g, serviceInstance1, gnvf1);
		rules.addEdge(g, gnvf1, vserver1);
		rules.addTreeEdge(g, vserver1,tenant1);
		rules.addTreeEdge(g, tenant1,cloudregion1);
		rules.addEdge(g, cloudregion1, complex1);
		//Not expected in result
		
		expectedResult.add(serviceInstance);
		expectedResult.add(gnvf);
		expectedResult.add(vserver);
		expectedResult.add(tenant);
		expectedResult.add(cloudregion);
		expectedResult.add(complex);
		
		
	}

	@Override
	protected String getQueryName() {
		return	"getTenantInfoAtSvcInstance";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "service-instance").has("service-instance-id", "service-instance-id-1");
		
	}
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}
