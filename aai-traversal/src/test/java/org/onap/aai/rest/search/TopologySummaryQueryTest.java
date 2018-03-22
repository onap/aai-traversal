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

public class TopologySummaryQueryTest extends QueryTest {

	public TopologySummaryQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}
	
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex vnf = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type", "generic-vnf", "vnf-id", "vnfuuid");
		Vertex vnfint = graph.addVertex(T.label, "l-interface", T.id, "10", "aai-node-type", "l-interface", "interface-name", "xe0/0/0");
		Vertex vnfc = graph.addVertex(T.label, "vnfc", T.id, "1", "aai-node-type", "vnfc");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "2", "aai-node-type", "vserver");
		Vertex vserverint = graph.addVertex(T.label, "l-interface", T.id, "11", "aai-node-type", "l-interface", "interface-name", "xe0/0/0");
		Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "3", "aai-node-type", "tenant");
		Vertex region = graph.addVertex(T.label, "could-region", T.id, "4", "aai-node-type", "cloud-region");
		Vertex image = graph.addVertex(T.label, "image", T.id, "5", "aai-node-type", "image");
		Vertex flavor = graph.addVertex(T.label, "flavor", T.id, "6", "aai-node-type", "flavor");
		Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "7", "aai-node-type", "pserver");
		Vertex pserverint = graph.addVertex(T.label, "p-interface", T.id, "9", "aai-node-type", "p-interface", "interface-name", "xe0/0/0");
		Vertex complex = graph.addVertex(T.label, "complex", T.id, "8", "aai-node-type", "complex");
		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, vnf, vnfc);
		rules.addEdge(g, vnf, vserver);
		rules.addEdge(g, vnfc, vserver);
		rules.addTreeEdge(g, vserver, tenant);
		rules.addTreeEdge(g, tenant, region);
		rules.addEdge(g, vserver, image);
		rules.addEdge(g, vserver, flavor);
		rules.addEdge(g, vserver, pserver);
		rules.addEdge(g, pserver, complex);
		rules.addEdge(g, region, complex);
		rules.addTreeEdge(g, pserver, pserverint);
		rules.addTreeEdge(g, vnf, vnfint);
		rules.addTreeEdge(g, vserver, vserverint);
		
		expectedResult.add(vnf);
		expectedResult.add(vnfc);
		expectedResult.add(vserver);
		expectedResult.add(tenant);
		expectedResult.add(region);
		expectedResult.add(image);
		expectedResult.add(flavor);
		expectedResult.add(pserver);
		expectedResult.add(complex);
		
	}

	@Override
	protected String getQueryName() {
		return "topology-summary";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("vnf-id", "vnfuuid");
		
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;		
	}

}
