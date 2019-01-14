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

public class ClosedLoopQueryTest extends QueryTest {
	public ClosedLoopQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {	
		
		
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "1", "aai-node-type", "vserver", "vserver-id", "vserver-id-1","vserver-name","vserver-name-1");
		Vertex gnvf = graph.addVertex(T.label, "generic-vnf", T.id, "2", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-1");
		
		Vertex serviceInstance = graph.addVertex(T.label, "service-instance", T.id, "3", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1");
		Vertex vfModule = graph.addVertex(T.label, "vf-module", T.id, "4", "aai-node-type", "vf-module", "vf-module-id", "vf-module-id-1");
		
		Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "5", "aai-node-type", "tenant", "tenant-id", "tenantid01", "tenant-name", "tenantName01");
		Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "6", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region-id-1", "cloud-region-owner", "cloud-owner-name-1");
		
		
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "7", "aai-node-type", "vserver", "vserver-id", "vserver-id-2","vserver-name","vserver-name-1");
		Vertex gnvf1 = graph.addVertex(T.label, "generic-vnf", T.id, "8", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-2");
		
		Vertex serviceInstance1 = graph.addVertex(T.label, "service-instance", T.id, "9", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-2");
		Vertex vfModule1 = graph.addVertex(T.label, "vf-module", T.id, "10", "aai-node-type", "vf-module", "vf-module-id", "vf-module-id-2");
		
		Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "11", "aai-node-type", "tenant", "tenant-id", "tenantid01", "tenant-name", "tenantName02");
		Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "12", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region-id-2", "cloud-region-owner", "cloud-owner-name-1");
		
		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, vserver, gnvf);
		rules.addEdge(g, gnvf, serviceInstance);
		rules.addTreeEdge(g, gnvf, vfModule);
		rules.addTreeEdge(g, vserver,tenant);
		rules.addTreeEdge(g, tenant,cloudregion);
		
		//Not expected in result
		rules.addEdge(g, vserver1, gnvf1);
		rules.addEdge(g, gnvf1, serviceInstance1);
		rules.addTreeEdge(g, gnvf1, vfModule1);
		rules.addTreeEdge(g, vserver1,tenant1);
		rules.addTreeEdge(g, tenant1,cloudregion1);
		//Not expected in result
		
		expectedResult.add(vserver);
		expectedResult.add(gnvf);
		expectedResult.add(serviceInstance);
		expectedResult.add(vfModule);
		expectedResult.add(tenant);
		expectedResult.add(cloudregion);
		
	}

	@Override
	protected String getQueryName() {
		return	"closed-loop";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "vserver").has("vserver-id", "vserver-id-1");
		
	}
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}
