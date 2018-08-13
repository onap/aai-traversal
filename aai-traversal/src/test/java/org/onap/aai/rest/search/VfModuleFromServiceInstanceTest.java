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

public class VfModuleFromServiceInstanceTest extends QueryTest {

	public VfModuleFromServiceInstanceTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void test() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//Set up the test graph
		Vertex serviceinstance = graph.addVertex(T.label, "service-instance", T.id, "0", "aai-node-type", "service-instance", "service-instance-id", "s-instance-id1");
		Vertex vfmodule = graph.addVertex(T.label, "vf-module", T.id, "2", "aai-node-type", "vf-module", "vf-module-id", "vf-module-id-0", "vf-module-name", "vf-module-name0");
		
		Vertex gnvf = graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-0", "vnf-name", "vnf-name-0");

			
		Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "10", "aai-node-type", "service-instance", "service-instance-id", "s-instance-id11");
		Vertex vfmodule1 = graph.addVertex(T.label, "vf-module", T.id, "12", "aai-node-type", "vf-module", "vf-module-id", "vf-module-id-01", "vf-module-name", "vf-module-name01");
		
		Vertex gnvf1 = graph.addVertex(T.label, "generic-vnf", T.id, "11", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-01", "vnf-name", "vnf-name-01");

		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, serviceinstance, gnvf);
		rules.addTreeEdge(g, gnvf, vfmodule);
		
		rules.addEdge(g, serviceinstance1, gnvf1); //false
		rules.addTreeEdge(g, gnvf1, vfmodule1); //false
		
		expectedResult.add(vfmodule);

	}

	@Override
	protected String getQueryName() {
		return "vfModule-fromServiceInstance";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "service-instance").has("service-instance-id", "s-instance-id1");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}

}
