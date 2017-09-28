/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
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

public class ServiceFromPserverAndSubsNameTest extends QueryTest {
	public ServiceFromPserverAndSubsNameTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	
	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		
		
		//Set up the test graph
		Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "1", "aai-node-type", "pserver", "hostname", "pservername01");
		Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "2", "aai-node-type", "pserver", "hostname", "pservername02-wrong");
		
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver", "vserver-id", "vserverid01");
		Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "4", "aai-node-type", "vserver", "vserver-id", "vserverid02");
		Vertex vserver3 = graph.addVertex(T.label, "vserver", T.id, "5", "aai-node-type", "vserver", "vserver-id", "vserverid03");
			
		Vertex vnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "8", "aai-node-type", "generic-vnf", "vnf-id", "vnfid01");
		Vertex vnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "9", "aai-node-type", "generic-vnf", "vnf-id", "vnfid02");
		Vertex vnf3 = graph.addVertex(T.label, "generic-vnf", T.id, "10", "aai-node-type", "generic-vnf", "vnf-id", "vnfid03");
		
		Vertex servinst1 = graph.addVertex(T.label, "service-instance", T.id, "61", "aai-node-type", "service-instance", "service-instance-id", "servInstId01", "service-type", "servType01");
		Vertex servinst2 = graph.addVertex(T.label, "service-instance", T.id, "12", "aai-node-type", "service-instance", "service-instance-id", "servInstId02", "service-type", "servType02", "service-role", "servRole02");
		Vertex servinst3 = graph.addVertex(T.label, "service-instance", T.id, "13", "aai-node-type", "service-instance", "service-instance-id", "servInstId03-wrong", "service-type", "servType03", "service-role", "servRole03");
		
		Vertex servsub1 = graph.addVertex(T.label, "service-subscription", T.id, "14", "aai-node-type", "service-subscription", "service-type", "servType01");
		Vertex servsub2 = graph.addVertex(T.label, "service-subscription", T.id, "15", "aai-node-type", "service-subscription", "service-type", "servType02");
		Vertex servsub3 = graph.addVertex(T.label, "service-subscription", T.id, "16", "aai-node-type", "service-subscription", "service-type", "servType03-wrong");
		
		Vertex customer1 = graph.addVertex(T.label, "customer", T.id, "17", "aai-node-type", "customer", "global-customer-id", "custid01", "subscriber-name", "subscriberName01");
		Vertex customer2 = graph.addVertex(T.label, "customer", T.id, "18", "aai-node-type", "customer", "global-customer-id", "custid02-wrong", "subscriber-name", "subscriberName-wrong");
		
		GraphTraversalSource g = graph.traversal();				
		rules.addEdge(g, vserver1, pserver1); //true
		rules.addEdge(g, vnf1, vserver1);  //true
		rules.addEdge(g, vnf1, servinst1);  //true
		rules.addTreeEdge(g, servinst1, servsub1); //true
		rules.addTreeEdge(g, servsub1, customer1);  //true
				
		rules.addEdge(g, vserver2, pserver1); //true
		rules.addEdge(g, vnf2, vserver2);  //true
		rules.addEdge(g, vnf2, servinst2);  //true
		rules.addEdge(g, vnf2, servinst3);  //false
		rules.addTreeEdge(g, servinst2, servsub2); //true
		rules.addTreeEdge(g, servsub2, customer1);  //true
				
		rules.addEdge(g, vserver3, pserver2); //false		
		rules.addEdge(g, vnf3, vserver3);  //false
		rules.addEdge(g, vnf3, servinst3);  //false
		rules.addTreeEdge(g, servinst3, servsub3); //false		
		rules.addTreeEdge(g, servsub3, customer2);  //false
				
		
		expectedResult.add(servinst1);
		expectedResult.add(servsub1);
		expectedResult.add(servinst2);
		expectedResult.add(servsub2);

	}
	
	@Override
	protected String getQueryName() {
		return "service-fromPserverandSubsName";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type","pserver").has("hostname", "pservername01");
	}
	
	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("subscriberName","subscriberName01");
	}
}
