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

public class ComplexFromVnfTest extends QueryTest {
	public ComplexFromVnfTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {	
		//Set up the test graph
		Vertex gnvf1 = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1");
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "1", "aai-node-type", "vserver", "vserver-id", "vserver-id-1", "vserver-name", "vserver-name-1");
		Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "2", "aai-node-type", "pserver", "hostname", "hostname-1");
		Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "3", "aai-node-type", "complex", "physical-location-id", "physical-location-id-1", "country", "US");
	
		Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "5", "aai-node-type", "pserver", "hostname", "hostname-2");
		Vertex complex2 = graph.addVertex(T.label, "complex", T.id, "6", "aai-node-type", "complex", "physical-location-id", "physical-location-id-2", "country", "US");
		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, gnvf1, vserver1);
		rules.addEdge(g, vserver1, pserver1);
		rules.addEdge(g, pserver1, complex1);
		rules.addEdge(g, gnvf1, pserver2);
		rules.addEdge(g, pserver2, complex2);
		
		expectedResult.add(gnvf1);
		expectedResult.add(pserver1);
		expectedResult.add(complex1);
		expectedResult.add(pserver2);
		expectedResult.add(complex2);
	}

	@Override
	protected String getQueryName() {
		return	"complex-fromVnf";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("vnf-name", "vnf-name-1");
		
	}
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}
