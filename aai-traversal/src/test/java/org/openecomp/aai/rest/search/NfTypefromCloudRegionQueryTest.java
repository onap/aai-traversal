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

public class NfTypefromCloudRegionQueryTest extends QueryTest {
	public NfTypefromCloudRegionQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {	
		
		Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "0", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region-id-1", "cloud-region-owner", "cloud-owner-name-1");
		Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "1", "aai-node-type", "tenant", "tenant-id", "tenantid01", "tenant-name", "tenantName01");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "2", "aai-node-type", "vserver", "vserver-id", "vserver-id-1","vserver-name","vserver-name-1");
		Vertex gnvf = graph.addVertex(T.label, "generic-vnf", T.id, "3", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-1");
		
		
		Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "4", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region-id-2", "cloud-region-owner", "cloud-owner-name-2");
		Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "5", "aai-node-type", "tenant", "tenant-id", "tenantid2", "tenant-name", "tenantName-2");
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "6", "aai-node-type", "vserver", "vserver-id", "vserver-id-1","vserver-name","vserver-name-1");
		Vertex gnvf1 = graph.addVertex(T.label, "generic-vnf", T.id, "7", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-1");
		
		GraphTraversalSource g = graph.traversal();
		rules.addTreeEdge(g, tenant,cloudregion);
		rules.addTreeEdge(g, vserver,tenant);
		rules.addEdge(g, vserver, gnvf);
		
		rules.addTreeEdge(g, tenant1,cloudregion1);
		rules.addTreeEdge(g, vserver1,tenant1);
		rules.addEdge(g, vserver1, gnvf1);
		
		expectedResult.add(gnvf);
		
	}

	@Override
	protected String getQueryName() {
		return	"nfType-fromCloudRegion";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("cloud-region-id", "cloud-region-id-1");
		
	}
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}
