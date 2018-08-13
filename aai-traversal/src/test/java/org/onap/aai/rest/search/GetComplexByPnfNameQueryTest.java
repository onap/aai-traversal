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

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;

public class GetComplexByPnfNameQueryTest extends QueryTest {
	public GetComplexByPnfNameQueryTest () throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

		// Set up the test graph
		Vertex pnf1 = graph.addVertex(T.label, "pnf", T.id, "0", "aai-node-type", "pnf", "pnf-name", "pnf1name", "equip-vendor", "equip-vendor1", "equip-model","equip-model1");
		Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "1", "aai-node-type", "complex", "physical-location-id", "physical-location-id-1");
		
		// adding extra vertices and edges which shouldn't be picked.
		Vertex pnf2 = graph.addVertex(T.label, "pnf", T.id, "2", "aai-node-type", "pnf", "pnf-name", "pnf2name", "equip-vendor", "equip-vendor2", "equip-model","equip-model2");
		Vertex complex2 = graph.addVertex(T.label, "complex", T.id, "3", "aai-node-type", "complex", "physical-location-id", "physical-location-id-2");

		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, pnf1, complex1);
		rules.addEdge(g, pnf2, complex2);

		expectedResult.add(pnf1);
		expectedResult.add(complex1);
	}

	@Override
	protected String getQueryName() {
		return "getComplexByPnfName";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "pnf").has("pnf-name", "pnf1name");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}
