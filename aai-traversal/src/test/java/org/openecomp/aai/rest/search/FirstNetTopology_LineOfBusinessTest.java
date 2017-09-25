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

public class FirstNetTopology_LineOfBusinessTest extends QueryTest {
	public FirstNetTopology_LineOfBusinessTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//set up test graph
		Vertex lineofbusiness = graph.addVertex(T.label, "line-of-business", T.id, "0", "aai-node-type", "line-of-business", "line-of-business-name", "business0");
		Vertex genericvnf = graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type", "generic-vnf", "vnf-id", "vnfid0","vnf-name", "vnf-name-1", "nf-type", "sample-nf-type");
		Vertex vnfc = graph.addVertex(T.label, "vnfc", T.id, "2", "aai-node-type", "vnfc", "vnfc-name", "vnfc0", "nfc-naming-code", "namingCode0", "nfc-function", "function0");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver", "vserver-id", "vserverid0","vserver-name", "vserver-name-1");
		Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "4", "aai-node-type", "pserver", "hostname", "pservername1");
		Vertex pnf = graph.addVertex(T.label, "pnf", T.id, "5", "aai-node-type", "pnf", "pnf-name", "pnf1name");
		
		
		Vertex lineofbusiness1 = graph.addVertex(T.label, "line-of-business", T.id, "10", "aai-node-type", "line-of-business", "line-of-business-name", "business1");
		Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "11", "aai-node-type", "generic-vnf", "vnf-id", "vnfid1","vnf-name", "vnf-name-2", "nf-type", "sample-nf-type1");
		Vertex vnfc1 = graph.addVertex(T.label, "vnfc", T.id, "12", "aai-node-type", "vnfc", "vnfc-name", "vnfc1", "nfc-naming-code", "namingCode1", "nfc-function", "function1");
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "13", "aai-node-type", "vserver", "vserver-id", "vserverid1","vserver-name", "vserver-name-2");
		Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "14", "aai-node-type", "pserver", "hostname", "pservername2");
		Vertex pnf1 = graph.addVertex(T.label, "pnf", T.id, "15", "aai-node-type", "pnf", "pnf-name", "pnf1name1");

		
		GraphTraversalSource g = graph.traversal();
		
		rules.addEdge(g, genericvnf,lineofbusiness);
		rules.addEdge(g, genericvnf, vnfc);
		rules.addEdge(g, genericvnf, vserver);
		rules.addEdge(g, genericvnf,pserver);
		rules.addEdge(g, genericvnf, pnf);
		
		//false
		rules.addEdge(g, genericvnf1,lineofbusiness1);
		rules.addEdge(g, genericvnf1, vnfc1);
		rules.addEdge(g, genericvnf1, vserver1);
		rules.addEdge(g, genericvnf1,pserver1);
		rules.addEdge(g, genericvnf1, pnf1);

		expectedResult.add(genericvnf);
		expectedResult.add(vnfc);
		expectedResult.add(vserver);
		expectedResult.add(pserver);
		expectedResult.add(pnf);
		



	}
	@Override
	protected String getQueryName() {
		return	"fn-topology";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "line-of-business").has("line-of-business-name", "business0");

	}
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}