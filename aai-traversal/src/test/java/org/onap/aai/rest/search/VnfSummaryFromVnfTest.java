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

public class VnfSummaryFromVnfTest extends QueryTest {
	public VnfSummaryFromVnfTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {


		//Set up the test graph
		Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type", "generic-vnf", "vnf-id", "vnfId1", "vnf-name", "vnf-name-1", "vnf-type", "vnfType1");
		Vertex genericvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "2", "aai-node-type", "generic-vnf", "vnf-id", "vnfId2", "vnf-name", "vnf-name-2", "vnf-type", "vnfType2");
		Vertex platform =  graph.addVertex(T.label, "platform", T.id, "3", "aai-node-type", "platform", "platform-name", "platform-name-1");
		Vertex platform2 =  graph.addVertex(T.label, "platform", T.id, "4", "aai-node-type", "platform", "platform-name", "platform-name-2");
		Vertex lineofbus = graph.addVertex(T.label, "line-of-business", T.id, "5", "aai-node-type", "line-of-business", "line-of-business-name", "line-of-business-name-1");
		Vertex lineofbus2 = graph.addVertex(T.label, "line-of-business", T.id, "6", "aai-node-type", "line-of-business", "line-of-business-name", "line-of-business-name-2");
		Vertex serviceinst = graph.addVertex(T.label, "service-instance", T.id, "7", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1");
		Vertex serviceinst2 = graph.addVertex(T.label, "service-instance", T.id, "8", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-2");
		Vertex owningent = graph.addVertex(T.label, "owning-entity", T.id, "9", "aai-node-type", "owning-entity", "owning-entity-id", "owning-entity-id-1");
		Vertex owningent2 = graph.addVertex(T.label, "owning-entity", T.id, "10", "aai-node-type", "owning-entity", "owning-entity-id", "owning-entity-id-2");
		Vertex project = graph.addVertex(T.label, "project", T.id, "11", "aai-node-type", "project", "project-name", "project-name-1");
		Vertex project2 = graph.addVertex(T.label, "project", T.id, "12", "aai-node-type", "project", "project-name", "project-name-2");
		Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "13", "aai-node-type", "pserver", "hostname", "hostname-1");
		Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "14", "aai-node-type", "pserver", "hostname", "hostname-2");
		Vertex pserver3 = graph.addVertex(T.label, "pserver", T.id, "15", "aai-node-type", "pserver", "hostname", "hostname-2");
		Vertex pserver4 = graph.addVertex(T.label, "pserver", T.id, "16", "aai-node-type", "pserver", "hostname", "hostname-2");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "17", "aai-node-type", "vserver", "vserver-id", "vserver-id-1");
		Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "18", "aai-node-type", "vserver", "vserver-id", "vserver-id-2");

		GraphTraversalSource g = graph.traversal();

		rules.addEdge(g, genericvnf1, platform);
		rules.addEdge(g, genericvnf1, lineofbus);
		rules.addEdge(g, genericvnf1, serviceinst);
		rules.addEdge(g, serviceinst, owningent);
		rules.addEdge(g, serviceinst, project);
		rules.addEdge(g, genericvnf1, pserver);
		rules.addEdge(g, genericvnf1, vserver);
		rules.addEdge(g, vserver, pserver2);

		rules.addEdge(g, genericvnf2, platform2);//false
		rules.addEdge(g, genericvnf2, lineofbus2);//false
		rules.addEdge(g, genericvnf2, serviceinst2);//false
		rules.addEdge(g, serviceinst2, owningent2);//false
		rules.addEdge(g, serviceinst2, project2);//false
		rules.addEdge(g, genericvnf2, pserver3);//false
		rules.addEdge(g, genericvnf2, vserver2);//false
		rules.addEdge(g, vserver2, pserver4);//false

		expectedResult.add(genericvnf1);
		expectedResult.add(platform);
		expectedResult.add(lineofbus);
		expectedResult.add(serviceinst);
		expectedResult.add(owningent);
		expectedResult.add(project);
		expectedResult.add(pserver);
		expectedResult.add(vserver);
		expectedResult.add(pserver2);

	}

	@Override
	protected String getQueryName() {
		return "vnf-summary-fromVnf";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("vnf-name","vnf-name-1");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}