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

public class TopologySummaryFromTenantTest extends QueryTest {

	public TopologySummaryFromTenantTest() throws AAIException,NoEdgeRuleFoundException {
		super();
	}
	
	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex tenant = graph.addVertex(T.label, "tenant-id", T.id, "0", "aai-node-type", "tenant", "tenant-id", "TenantID", "tenant-name", "TenantName");
		Vertex cloudRegion = graph.addVertex(T.label, "cloud-region", T.id, "1", "aai-node-type", "cloud-region", "cloud-owner", "CloudOwner", "cloud-region-id", "CloudRegionId");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "2", "aai-node-type", "vserver", "vserver-id", "vservId-1", "vserver-name", "vserv-name-1", "vserver-selflink", "me/self");
		Vertex genericVnf = graph.addVertex(T.label, "generic-vnf", T.id, "3", "aai-node-type", "generic-vnf", "vnf-id", "VnfID", "vnf-name", "VnfName", "vnf-type", "VnfType");
		Vertex  pserver = graph.addVertex(T.label, "pserver", T.id, "4", "aai-node-type", "pserver", "pserver-id", "PserverID", "hostname", "PserverHostName");
		
		Vertex tenant2 = graph.addVertex(T.label, "tenant-id2", T.id, "5", "aai-node-type", "tenant", "tenant-id", "TenantID2", "tenant-name", "TenantName2");
		Vertex cloudRegion2 = graph.addVertex(T.label, "cloud-region2", T.id, "6", "aai-node-type", "cloud-region", "cloud-owner", "CloudOwner2", "cloud-region-id", "CloudRegionId2");
		Vertex vserver2 = graph.addVertex(T.label, "vserver2", T.id, "7", "aai-node-type", "vserver", "vserver-id", "vservId-2", "vserver-name", "vserv-name-2", "vserver-selflink", "me/self"); //false
		Vertex genericVnf2 = graph.addVertex(T.label, "generic-vnf2", T.id, "8", "aai-node-type", "generic-vnf", "vnf-id", "VnfID2", "vnf-name", "VnfName2", "vnf-type", "VnfType");  //false
		Vertex  pserver2 = graph.addVertex(T.label, "pserver2", T.id, "9", "aai-node-type", "pserver", "pserver-id", "PserverID2", "hostname", "PserverHostName2");  //false

		GraphTraversalSource g = graph.traversal();
		rules.addTreeEdge(g, tenant, cloudRegion);
		rules.addTreeEdge(g, tenant, vserver);
		rules.addEdge(g, vserver, genericVnf);
		rules.addEdge(g, vserver, pserver);
		
		//tenant2
		rules.addTreeEdge(g, tenant2, cloudRegion2);//false
		rules.addTreeEdge(g, tenant2, vserver2);//false
		rules.addEdge(g, vserver2, genericVnf2);  //false
		rules.addEdge(g, vserver2, pserver2);  //false
		
		
		expectedResult.add(tenant);
		expectedResult.add(cloudRegion);
		expectedResult.add(vserver);
		expectedResult.add(genericVnf);
		expectedResult.add(pserver);
				
	}

	@Override
	protected String getQueryName() {
		return "topology-summary-fromTenant";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "tenant").has("tenant-id","TenantID");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}

}
