package org.onap.aai.rest.search; /**
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
/*
package org.onap.aai.rest.search;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class VnfsVlansFromServiceInstanceTest extends QueryTest {
	public VnfsVlansFromServiceInstanceTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	
	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		
		
		//Set up the test graph	
		Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "0", "aai-node-type",
				"service-instance", "service-instance-id", "service-instance-id-1", "service-instance-name",
				"service-instance-name-1");	
		
		Vertex config1 = graph.addVertex(T.label, "configuration", T.id, "1", "aai-node-type", "configuration", "configuration-id", "configuration1");
		
		Vertex gvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "2", "aai-node-type", "generic-vnf", 
				"vnf-id", "gvnf1", "vnf-name", "genvnfname1", "nf-type", "sample-nf-type");

		Vertex linterface1 = graph.addVertex(T.label, "l-interface", T.id, "3", "aai-node-type", "l-interface", "l-interface-id", "l-interface-id0", "l-interface-name", "l-interface-name0","network-name","networkName0");
		
		Vertex vlan1 = graph.addVertex(T.label, "vlan", T.id, "4", "aai-node-type", "vlan", "vlan-interface", "vlan-interface1");


		Vertex serviceinstance2 = graph.addVertex(T.label, "service-instance", T.id, "5", "aai-node-type",
				"service-instance", "service-instance-id", "service-instance-id-2", "service-instance-name",
				"service-instance-name-1");

		Vertex config2 = graph.addVertex(T.label, "configuration", T.id, "6", "aai-node-type", "configuration", "configuration-id", "configuration2");

		Vertex gvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "7", "aai-node-type", "generic-vnf",
				"vnf-id", "gvnf2", "vnf-name", "genvnfname2", "nf-type", "sample-nf-type");

		Vertex linterface2 = graph.addVertex(T.label, "l-interface", T.id, "8", "aai-node-type", "l-interface", "l-interface-id", "l-interface-id2", "l-interface-name", "l-interface-name2","network-name","networkName1");

		Vertex vlan2 = graph.addVertex(T.label, "vlan", T.id, "9", "aai-node-type", "vlan", "vlan-interface", "vlan-interface2");

		GraphTraversalSource g = graph.traversal();
		
		rules.addEdge(g, serviceinstance1, config1); // True
		rules.addEdge(g, config1, gvnf1); // True
		rules.addEdge(g, config1, linterface1); // True
		rules.addTreeEdge(g, linterface1, vlan1); // True

		rules.addEdge(g, serviceinstance2, config2); // False
		rules.addEdge(g, config2, gvnf2); // False
		rules.addEdge(g, config2, linterface2);// False
		rules.addTreeEdge(g, linterface2, vlan2); // False
        
		expectedResult.add(gvnf1);
		expectedResult.add(vlan1);

	}
	@Override
	protected String getQueryName() {
		return "vnfs-vlans-fromServiceInstance";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "service-instance").has("service-instance-id", "service-instance-id-1");
	}
	
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}*/