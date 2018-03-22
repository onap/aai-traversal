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

public class VnfsFromServiceInstanceTest extends QueryTest {
	public VnfsFromServiceInstanceTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	
	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		
		
		//Set up the test graph	
        Vertex serviceinstance = graph.addVertex(T.label, "service-instance", T.id, "1", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1", "service-instance-name", "service-instance-name-1");
        Vertex servicesubscription = graph.addVertex(T.label, "service-subscription", T.id, "2", "aai-node-type", "service-subscription", "service-subscription-id", "service-subscription-id-1","service-subscription-name","service-subscription-name1");
        Vertex customer = graph.addVertex(T.label, "customer", T.id, "3", "aai-node-type", "customer", "customer-id", "customer-id-1", "customer-name", "customer-name1");
		Vertex vnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "4", "aai-node-type", "generic-vnf", "vnf-id", "vnfid1", "vnf-name", "vnfname1", "vnf-type", "vnftype1");
		Vertex serviceinstance2 = graph.addVertex(T.label, "service-instance", T.id, "5", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-2", "service-instance-name", "service-instance-name-2");
		Vertex vnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "6", "aai-node-type", "generic-vnf", "vnf-id", "vnfid2", "vnf-name", "vnfname2", "vnf-type", "vnftype1");
		
		GraphTraversalSource g = graph.traversal();

		rules.addEdge(g, vnf1, serviceinstance);//true
		rules.addEdge(g, vnf2, serviceinstance2);

        rules.addTreeEdge(g, serviceinstance, servicesubscription);//true
        rules.addTreeEdge(g, serviceinstance2, servicesubscription);
        rules.addTreeEdge(g, servicesubscription, customer);//true
        
		expectedResult.add(vnf1);

	}
	@Override
	protected String getQueryName() {
		return "vnfs-fromServiceInstance";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "service-instance").has("service-instance-id", "service-instance-id-1");
	}
	
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}
