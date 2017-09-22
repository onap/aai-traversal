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

public class PserverfromConfigurationFilterInterfaceIdTest extends QueryTest {
	public PserverfromConfigurationFilterInterfaceIdTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

		// Set up the test graph
		Vertex config1 = graph.addVertex(T.label, "configuration", T.id, "1", "aai-node-type", "configuration", "configuration-id", "configuration1");
		Vertex gvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "2", "aai-node-type", "generic-vnf", "vnf-id", "vnfid1");
		Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "3", "aai-node-type", "pserver", "hostname", "pservername1");
		Vertex loglink1 = graph.addVertex(T.label, "logical-link", T.id, "4", "aai-node-type", "logical-link", "link-name", "loglink1", "in-maint", "false",
				"link-type", "link-type1");
		Vertex lint1 = graph.addVertex(T.label, "l-interface", T.id, "5", "aai-node-type", "l-interface", "interface-name", "lint1", "is-port-mirrored", "true",
				"in-maint", "true", "is-ip-unnumbered", "false", "interface-id", "interface-id1");

		// Following are extra nodes that should not be picked up in
		// expectedResults
		Vertex config2 = graph.addVertex(T.label, "configuration", T.id, "11", "aai-node-type", "configuration", "configuration-id", "configuration2");
		Vertex gvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "12", "aai-node-type", "generic-vnf", "vnf-id", "vnfid2");
		Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "13", "aai-node-type", "pserver", "hostname", "pservername2");
		Vertex loglink2 = graph.addVertex(T.label, "logical-link", T.id, "14", "aai-node-type", "logical-link", "link-name", "loglink2", "in-maint", "false",
				"link-type", "link-type2");
		Vertex lint2 = graph.addVertex(T.label, "l-interface", T.id, "15", "aai-node-type", "l-interface", "interface-name", "lint1", "is-port-mirrored",
				"true", "in-maint", "true", "is-ip-unnumbered", "false", "interface-id", "interface-id2");

		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, config1, loglink1);
		rules.addEdge(g, config1, loglink2);
		rules.addEdge(g, lint1, loglink1);
		rules.addEdge(g, lint2, loglink1);
		rules.addEdge(g, loglink1, pserver1);
		rules.addEdge(g, loglink1, gvnf1);

		// These should not be picked up in expectedResults
		//rules.addEdge(g, config2, loglink2);
		rules.addEdge(g, lint2, loglink2);
		rules.addEdge(g, loglink2, pserver2);
		rules.addEdge(g, loglink2, gvnf2);

		// Note lint2 is not in expectedResults as the filter is based on
		// interface-id1
		expectedResult.add(config1);
		expectedResult.add(lint1);
		expectedResult.add(pserver1);
		expectedResult.add(gvnf1);

	}

	@Override
	protected String getQueryName() {
		return "pserver-fromConfigurationFilterInterfaceId";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("configuration-id", "configuration1");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("interfaceId", "interface-id1");
	}
}
