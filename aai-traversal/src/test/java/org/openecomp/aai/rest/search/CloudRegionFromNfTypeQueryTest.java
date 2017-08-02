/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.aai.rest.search;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class CloudRegionFromNfTypeQueryTest extends QueryTest {
	public CloudRegionFromNfTypeQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//set up test graph
		Vertex genericvnf = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type", "generic-vnf", "vnf-id", "vnfid01", "nf-type", "sample-nf-type");
		Vertex cloudregion0 = graph.addVertex(T.label, "cloud-region", T.id, "3", "aai-node-type", "cloud-region", "cloud-region-id", "regionid00", "cloud-region-owner", "cloudOwnername00");
		Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "4", "aai-node-type", "tenant", "tenant-id", "tenantid01", "tenant-name", "tenantName01");
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "5", "aai-node-type", "vserver", "vserver-id", "vserverid01");
		
		Vertex genericvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "8", "aai-node-type", "generic-vnf", "vnf-id", "vnfid02", "nf-type", "ex-nf-type");
		Vertex cloudregion3 = graph.addVertex(T.label, "cloud-region", T.id, "9", "aai-node-type", "cloud-region", "cloud-region-id", "regionid03", "cloud-region-owner", "cloudOwnername03");
		Vertex tenant2 = graph.addVertex(T.label, "tenant", T.id, "10", "aai-node-type", "tenant", "tenant-id", "tenantid02", "tenant-name", "tenantName02");
		Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "11", "aai-node-type", "vserver", "vserver-id", "vserverid02");
		
		GraphTraversalSource g = graph.traversal();
	
		rules.addTreeEdge(g, tenant, cloudregion0);
		rules.addTreeEdge(g, vserver1, tenant);
		rules.addEdge(g, genericvnf, vserver1);
				
		rules.addTreeEdge(g, tenant2, cloudregion3);
		rules.addTreeEdge(g, vserver2, tenant2);
		rules.addEdge(g, genericvnf2, vserver2);
		
		expectedResult.add(cloudregion0);
	}
	@Override
	protected String getQueryName() {
		return	"cloudRegion-fromNfType";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("nf-type", "sample-nf-type");
	}
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}
