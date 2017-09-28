/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
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

public class LocationNetTypeNetRolefromCloudRegionQueryTest extends QueryTest {
	public LocationNetTypeNetRolefromCloudRegionQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {	
		
		Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "0", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region-id-1", "cloud-region-owner", "cloud-owner-name-1");
		Vertex complex = graph.addVertex(T.label, "complex", T.id, "1", "aai-node-type", "complex", "physical-location-id", "physical-location-id-1");
		Vertex l3network = graph.addVertex(T.label, "l3-network", T.id, "2", "aai-node-type", "l3-network", "network-id", "network-id-1");
		
		
		Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "3", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region-id-2", "cloud-region-owner", "cloud-owner-name-2");
		Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "4", "aai-node-type", "complex", "physical-location-id", "physical-location-id-2");
		Vertex l3network1 = graph.addVertex(T.label, "l3-network", T.id, "5", "aai-node-type", "l3-network", "network-id", "network-id-2");
		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, cloudregion,complex);
		rules.addEdge(g, complex,l3network);
		
		rules.addEdge(g, cloudregion1,complex1);
		rules.addEdge(g, complex1,l3network1);
		
		expectedResult.add(cloudregion);
		expectedResult.add(complex);
		expectedResult.add(l3network);
		
	}

	@Override
	protected String getQueryName() {
		return	"locationNetTypeNetRole-fromCloudRegion";
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
