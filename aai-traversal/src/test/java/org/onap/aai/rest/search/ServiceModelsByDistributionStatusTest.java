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

public class ServiceModelsByDistributionStatusTest extends QueryTest {
	public ServiceModelsByDistributionStatusTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//Set up the test graph	
        Vertex model1 = graph.addVertex(T.label, "model", T.id, "1", "aai-node-type", "model", "model-invariant-id", "model-invariant-id-1", "model-type", "resource");
        Vertex modelver1 = graph.addVertex(T.label, "model-ver", T.id, "2", "aai-node-type", "model-ver", "model-version-id", "model-version-id-1","model-name","model-name1", "model-version","model-version-1", "distribution-status", "distribution-status1");
        Vertex modelver11 = graph.addVertex(T.label, "model-ver", T.id, "21", "aai-node-type", "model-ver", "model-version-id", "model-version-id-11","model-name","model-name11", "model-version","model-version-1", "distribution-status", "distribution-status2");
        
        Vertex model2 = graph.addVertex(T.label, "model", T.id, "3", "aai-node-type", "model", "model-invariant-id", "model-invariant-id-2", "model-type", "resource");
        Vertex modelver2 = graph.addVertex(T.label, "model-ver", T.id, "4", "aai-node-type", "model-ver", "model-version-id", "model-version-id-2","model-name","model-name2", "model-version","model-version-22", "distribution-status", "distribution-status2");
        
        Vertex model3 = graph.addVertex(T.label, "model", T.id, "5", "aai-node-type", "model", "model-invariant-id", "model-invariant-id-3", "model-type", "service");
        Vertex modelver3 = graph.addVertex(T.label, "model-ver", T.id, "6", "aai-node-type", "model-ver", "model-version-id", "model-version-id-13","model-name","model-name3", "model-version","model-version-3", "distribution-status", "distribution-status1");
        Vertex modelver13 = graph.addVertex(T.label, "model-ver", T.id, "7", "aai-node-type", "model-ver", "model-version-id", "model-version-id-33","model-name","model-name33", "model-version","model-version-33", "distribution-status", "distribution-status2");
       
        GraphTraversalSource g = graph.traversal();

        rules.addTreeEdge(g, modelver2,model2);
		
		rules.addTreeEdge(g, modelver3, model3);
		rules.addTreeEdge(g, modelver13, model3);
		rules.addTreeEdge(g, modelver11, model3);
		
		expectedResult.add(model3);
		expectedResult.add(modelver13);
		expectedResult.add(modelver11);
		

	}
	@Override
	protected String getQueryName() {
		return "serviceModels-byDistributionStatus";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "model").has("model-invariant-id", "model-invariant-id-3");
	}
	
	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("distributionStatus", "distribution-status2");
		return;
	}
}
