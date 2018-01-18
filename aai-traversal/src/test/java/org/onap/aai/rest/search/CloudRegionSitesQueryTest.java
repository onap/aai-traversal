/*-
 * ============LICENSE_START=======================================================
 * org.onap.aai
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

package org.onap.aai.rest.search;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class CloudRegionSitesQueryTest extends QueryTest {

	public CloudRegionSitesQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	
	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex region1 = graph.addVertex(T.label, "cloud-region", T.id, "0", "aai-node-type", "cloud-region", "cloud-owner", "cloudOwner1");
		Vertex region2 = graph.addVertex(T.label, "cloud-region", T.id, "1", "aai-node-type", "cloud-region", "cloud-owner", "cloudOwner1");
		Vertex region3 = graph.addVertex(T.label, "cloud-region", T.id, "2", "aai-node-type", "cloud-region", "cloud-owner", "cloudOwner1");
		Vertex region4 = graph.addVertex(T.label, "cloud-region", T.id, "3", "aai-node-type", "cloud-region", "cloud-owner", "cloudOwner2");
		Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "4", "aai-node-type", "complex");
		Vertex complex2 = graph.addVertex(T.label, "complex", T.id, "5", "aai-node-type", "complex");
		Vertex complex3 = graph.addVertex(T.label, "complex", T.id, "6", "aai-node-type", "complex");

		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, region1, complex1);
		rules.addEdge(g, region2, complex1);
		rules.addEdge(g, region3, complex2);
		rules.addEdge(g, region4, complex3);

		expectedResult.add(region1);
		expectedResult.add(region2);
		expectedResult.add(region3);
		expectedResult.add(complex1);
		expectedResult.add(complex2);
		
	}

	@Override
	protected String getQueryName() {
		return "cloud-region-sites";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "cloud-region");
		
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("owner", "cloudOwner1");
	}

}
