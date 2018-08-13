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

public class InstanceGroupsByCloudRegionQueryTest extends QueryTest {

	public InstanceGroupsByCloudRegionQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void test() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//Set up the test graph
		Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "1", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region-id-0", "cloud-region-owner", "cloud-owner-name-0","cloud-region-version","cloud-region-version-0");
		Vertex instancegroup = graph.addVertex(T.label, "instance-group", T.id, "2", "aai-node-type", "instance-group", "id", "id-0", "instance-group-type", "instance-group-type-0","instance-group-role","instance-group-role-0","instance-group-function","instance-group-function-0","instance-group-description","instance-group-description-0");
		
		Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "3", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region-id-1", "cloud-region-owner", "cloud-owner-name-1","cloud-region-version","cloud-region-version-1");
		Vertex instancegroup1 = graph.addVertex(T.label, "instance-group", T.id, "4", "aai-node-type", "instance-group", "id", "id-1", "instance-group-type", "instance-group-type-1","instance-group-role","instance-group-role-1","instance-group-function","instance-group-function-1","instance-group-description","instance-group-description-1");

		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, cloudregion, instancegroup);
		
		rules.addEdge(g, cloudregion1, instancegroup1); //false
		
		expectedResult.add(instancegroup);
	}

	@Override
	protected String getQueryName() {
		return "instance-groups-byCloudRegion";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "cloud-region").has("cloud-region-owner", "cloud-owner-name-0").has("cloud-region-id", "cloud-region-id-0");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("role", "instance-group-role-0");
		params.put("type", "instance-group-type-0");
		params.put("function", "instance-group-function-0");
	}

}
