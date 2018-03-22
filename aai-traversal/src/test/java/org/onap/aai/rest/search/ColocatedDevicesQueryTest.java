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

public class ColocatedDevicesQueryTest extends QueryTest {
	public ColocatedDevicesQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	
	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		
		
		//Set up the test graph
		Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "0", "aai-node-type", "complex", "physical-location-id", "clli1");
		Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "1", "aai-node-type", "pserver", "hostname", "pservername1");
		Vertex pserverint1 = graph.addVertex(T.label, "p-interface", T.id, "2", "aai-node-type", "p-interface", "interface-name", "xe0/0/0");	
		Vertex pnf1 = graph.addVertex(T.label, "pnf", T.id, "3", "aai-node-type", "pnf", "pnf-name", "pnfname1");
		Vertex pnfint1 = graph.addVertex(T.label, "p-interface", T.id, "4", "aai-node-type", "p-interface", "interface-name", "ge0/0/0");
		Vertex plink1 = graph.addVertex(T.label, "physical-link", T.id, "5", "aai-node-type", "physical-link", "link-name", "ge0/0/0-to-xe0/0/0");
		
		Vertex complex2 = graph.addVertex(T.label, "complex", T.id, "6", "aai-node-type", "complex", "physical-location-id", "clli2");
		Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "7", "aai-node-type", "pserver", "hostname", "pservername2");
		Vertex pserverint2 = graph.addVertex(T.label, "p-interface", T.id, "8", "aai-node-type", "p-interface", "interface-name", "xe0/0/1");
		Vertex pserver3 = graph.addVertex(T.label, "pserver", T.id, "9", "aai-node-type", "pserver", "hostname", "pservername3");
		Vertex pserverint3 = graph.addVertex(T.label, "p-interface", T.id, "10", "aai-node-type", "p-interface", "interface-name", "xe0/0/3");
		Vertex plink2 = graph.addVertex(T.label, "physical-link", T.id, "11", "aai-node-type", "physical-link", "link-name", "xe0/0/1-to-xe0/0/3");
		
		Vertex pnf2 = graph.addVertex(T.label, "pnf", T.id, "12", "aai-node-type", "pnf", "pnf-name", "pnfname2");
		Vertex pnfint2 = graph.addVertex(T.label, "p-interface", T.id, "13", "aai-node-type", "p-interface", "interface-name", "ge0/0/2");
		Vertex pnf3 = graph.addVertex(T.label, "pnf", T.id, "14", "aai-node-type", "pnf", "pnf-name", "pnfname3");
		Vertex pnfint3 = graph.addVertex(T.label, "p-interface", T.id, "15", "aai-node-type", "p-interface", "interface-name", "ge0/0/3");
		Vertex plink3 = graph.addVertex(T.label, "physical-link", T.id, "16", "aai-node-type", "physical-link", "link-name", "ge0/0/2-to-ge0/0/3");
		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, pserver1, complex1);
		rules.addTreeEdge(g, pserver1, pserverint1);		
		rules.addEdge(g, pnf1, complex1);
		rules.addTreeEdge(g, pnf1, pnfint1);
		rules.addEdge(g, pserverint1, plink1);
		rules.addEdge(g, pnfint1, plink1);
		
		rules.addEdge(g, pserver2, complex1);
		rules.addTreeEdge(g, pserver2, pserverint2);
		rules.addEdge(g, pserver3, complex2);
		rules.addTreeEdge(g, pserver3, pserverint3);
		rules.addEdge(g, pserverint2, plink2);
		rules.addEdge(g, pserverint3, plink2);
		
		rules.addEdge(g, pnf2, complex2);
		rules.addTreeEdge(g, pnf2, pnfint2);
		rules.addEdge(g, pnf3, complex2);
		rules.addTreeEdge(g, pnf3, pnfint3);
		rules.addEdge(g, pnfint2, plink3);
		rules.addEdge(g, pnfint3, plink3);
		
		
		expectedResult.add(pnf1);
		expectedResult.add(pnfint1);
		expectedResult.add(pserver1);
		expectedResult.add(pserverint1);
		expectedResult.add(plink1);		
		expectedResult.add(pserver2);
		expectedResult.add(pserverint2);
		expectedResult.add(plink2);


	}
	@Override
	protected String getQueryName() {
		return "colocated-devices";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("hostname", "pservername1");
	}
	
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}
