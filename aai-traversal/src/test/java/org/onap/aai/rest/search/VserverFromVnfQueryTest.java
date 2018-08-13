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

public class VserverFromVnfQueryTest extends QueryTest {

	public VserverFromVnfQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex gv = graph.addVertex(T.id, "00", "aai-node-type", "generic-vnf", "vnf-id", "gvId", "vnf-name", "gvName", "vnf-type", "some-type");
		Vertex vnfc = graph.addVertex(T.id, "10", "aai-node-type", "vnfc", 
						"vnfc-name", "vnfcName1", "nfc-naming-code", "blue", "nfc-function", "correct-function");
		Vertex vserv = graph.addVertex(T.id, "20", "aai-node-type", "vserver",
						"vserver-id", "vservId", "vserver-name", "vservName", "vserver-selflink", "me/self");
		Vertex lint = graph.addVertex(T.id, "30", "aai-node-type", "l-interface", "interface-name", "lintName");
		Vertex ipv4 = graph.addVertex(T.id, "40", "aai-node-type", "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "0.0.0.0");
		Vertex ipv6 = graph.addVertex(T.id, "50", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "0.0.0.0");
		
		
		Vertex gv1 = graph.addVertex(T.id, "60", "aai-node-type", "generic-vnf", "vnf-id", "gvId1", "vnf-name", "gvName1", "vnf-type", "some-type1");
		Vertex vnfc1 = graph.addVertex(T.id, "70", "aai-node-type", "vnfc", 
						"vnfc-name", "vnfcName11", "nfc-naming-code", "blue1", "nfc-function", "correct-function1");
		Vertex vserv1 = graph.addVertex(T.id, "80", "aai-node-type", "vserver",
						"vserver-id", "vservId1", "vserver-name", "vservName1", "vserver-selflink", "me/self1");
		Vertex lint1 = graph.addVertex(T.id, "90", "aai-node-type", "l-interface", "interface-name", "lintName1");
		Vertex ipv41 = graph.addVertex(T.id, "1", "aai-node-type", "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "0.0.0.0.1");
		Vertex ipv61 = graph.addVertex(T.id, "2", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "0.0.0.0.1");

		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, gv, vnfc);
		rules.addEdge(g, vserv, vnfc);
		rules.addTreeEdge(g, vserv, lint);
		rules.addTreeEdge(g, lint, ipv4);
		rules.addTreeEdge(g, lint, ipv6);
		
		rules.addEdge(g, gv1, vnfc1);//false
		rules.addEdge(g, vserv1, vnfc1);//false
		rules.addTreeEdge(g, vserv1, lint1);//false
		rules.addTreeEdge(g, lint1, ipv41);//false
		rules.addTreeEdge(g, lint1, ipv61);//false
		
		expectedResult.add(vserv);
		expectedResult.add(lint);
		expectedResult.add(ipv4);
		expectedResult.add(ipv6);
		expectedResult.add(vnfc);
	}

	@Override
	protected String getQueryName() {
		return "vserver-fromVnf";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "generic-vnf").has("vnf-id", "gvId");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("nfcNamingCode", "blue");
	}

	
}
