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

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;

public class OwningEntityfromServiceInstance extends QueryTest {
	public OwningEntityfromServiceInstance () throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

		// Set up the test graph
		Vertex service_instance = graph.addVertex(T.label, "service-instance", T.id, "1", "aai-node-type", "service-instance", "service-instance-id", "service-instance-1");
		Vertex owning_entity = graph.addVertex(T.label, "owning-entity", T.id, "2", "aai-node-type", "owning-entity", "owning-entity-id", "owning-entity-id-1",  "owning-entity-name", "owning-entity-name1");

		// adding extra vertices and edges which shouldn't be picked.
		Vertex service_instance2 = graph.addVertex(T.label, "service-instance", T.id, "3", "aai-node-type", "service-instance", "service-instance-id", "service-instance-2");
		Vertex owning_entity2 = graph.addVertex(T.label, "owning-entity", T.id, "4", "aai-node-type", "owning-entity", "owning-entity-id", "owning-entity-id-2",  "owning-entity-name", "owning-entity-name2");

		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, owning_entity, service_instance);
		rules.addEdge(g, owning_entity2, service_instance2);

		expectedResult.add(owning_entity);
	}

	@Override
	protected String getQueryName() {
		return "owning-entity-fromService-instance";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("service-instance-id", "service-instance-1");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}
