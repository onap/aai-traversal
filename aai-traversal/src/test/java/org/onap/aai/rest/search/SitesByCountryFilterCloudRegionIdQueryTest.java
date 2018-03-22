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

public class SitesByCountryFilterCloudRegionIdQueryTest extends QueryTest {

	
	public SitesByCountryFilterCloudRegionIdQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}
	
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
				
		//Set up the test graph
		Vertex complex = graph.addVertex(T.label, "complex", T.id, "0", "aai-node-type", "complex", "physical-location-id", "clli-100", "country", "countrycode-100");
		Vertex l3network = graph.addVertex(T.label, "l3-network", T.id, "1", "aai-node-type", "l3-network", "network-id", "networkId-100", "network-name", "networkName-100",
				"is-bound-to-vpn", "false", "is-provider-network", "false", "is-shared-network", "false", "is-external-network", "false");
		Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "2", "aai-node-type", "cloud-region", "cloud-owner", "cloudOwner-100", "cloud-region-id", "cloudRegionId-100");
		Vertex cloudregion2 = graph.addVertex(T.label, "cloud-region", T.id, "3", "aai-node-type", "cloud-region", "cloud-owner", "cloudOwner-200", "cloud-region-id", "cloudRegionId-200");
		Vertex l3network2 = graph.addVertex(T.label, "l3-network", T.id, "4", "aai-node-type", "l3-network", "network-id", "networkId-200", "network-name", "networkName-200",
				"is-bound-to-vpn", "false", "is-provider-network", "false", "is-shared-network", "false", "is-external-network", "false");
		Vertex complex2 = graph.addVertex(T.label, "complex", T.id, "5", "aai-node-type", "complex", "physical-location-id", "clli-200", "country", "countrycode-100");


	
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, complex, l3network);
		rules.addEdge(g, cloudregion, l3network);
		rules.addEdge(g, complex2, l3network2);
		rules.addEdge(g, cloudregion2, l3network2);
		
		expectedResult.add(complex);
		

	}

	@Override
	protected String getQueryName() {
		return "sites-byCountryFilterCloudRegionId";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "complex").has("country", "countrycode-100");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("cloudRegionId", "cloudRegionId-100");
		
	}
}
