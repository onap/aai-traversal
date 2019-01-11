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

public class GetRouterRoadmTailSummaryTest extends QueryTest{

	public GetRouterRoadmTailSummaryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex serviceInstance = graph.addVertex(T.label, "service-instance", T.id, "1", "aai-node-type", "service-instance", 
				"service-instance-id", "service-instance-id-1", "service-instance-name", "service-instance-name-1");
        Vertex serviceSubscription = graph.addVertex(T.label, "service-subscription", T.id, "2", "aai-node-type", "service-subscription", 
        		"service-type", "service-subcription-1");
        Vertex customer = graph.addVertex(T.label, "customer", T.id, "3", "aai-node-type", "customer", ""
        		+ "global-customer-id", "customer-id-1", "subscriber-name", "customer-name1", "subscriber-type", "customer-type1");
        Vertex logicalLink1 = graph.addVertex(T.label, "logical-link", T.id, "4", "aai-node-type", "logical-link", "link-name", "logical-link-1");
        Vertex logicalLink2 = graph.addVertex(T.label, "logical-link", T.id, "5", "aai-node-type", "logical-link", "link-name", "logical-link-2");
        Vertex pInterface1 = graph.addVertex(T.label, "p-interface", T.id, "6", "aai-node-type", "p-interface", "interface-name", "p-interface-1");  
        Vertex pInterface2 = graph.addVertex(T.label, "p-interface", T.id, "7", "aai-node-type", "p-interface", "interface-name", "p-interface-2");  
        Vertex pInterface3 = graph.addVertex(T.label, "p-interface", T.id, "8", "aai-node-type", "p-interface", "interface-name", "p-interface-3");  
        Vertex pnf1 = graph.addVertex(T.label, "pnf", T.id, "9", "aai-node-type", "pnf", "pnf-name", "pnf1name");
        Vertex pnf2 = graph.addVertex(T.label, "pnf", T.id, "10", "aai-node-type", "pnf", "pnf-name", "pnf2name");
        
        Vertex lInterface1 = graph.addVertex(T.label, "l-interface", T.id, "11", "aai-node-type", "l-interface", "interface-name", "l-interface-1"); 
        
        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, customer, serviceSubscription);
        rules.addTreeEdge(g, serviceSubscription, serviceInstance);
        
        rules.addEdge(g, serviceInstance, logicalLink1);
        rules.addEdge(g, logicalLink2, logicalLink1);
        rules.addEdge(g, logicalLink2, pInterface1);
        rules.addEdge(g, logicalLink2, pInterface2);
        rules.addEdge(g, logicalLink2, pInterface3);
        
        rules.addTreeEdge(g, pInterface1, pnf1);
        rules.addTreeEdge(g, pInterface2, pnf2);
        rules.addTreeEdge(g, pInterface3, pnf2);
        
        rules.addEdge(g, logicalLink2, lInterface1);//false
        
        expectedResult.add(pnf1);
        expectedResult.add(pInterface1);
        //expectedResult.add(logicalLink1);
        expectedResult.add(pInterface2);
        expectedResult.add(pInterface3);
        expectedResult.add(pnf2);
        expectedResult.add(logicalLink2);
        expectedResult.add(serviceInstance);
        expectedResult.add(serviceSubscription);
        expectedResult.add(customer);
	}

	@Override
	protected String getQueryName() {
		return "getRouterRoadmTailSummary";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "pnf").has("pnf-name", "pnf1name");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}

}
