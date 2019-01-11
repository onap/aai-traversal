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

public class LinterfacesCPFromVnfcTest extends QueryTest {

	public LinterfacesCPFromVnfcTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex vnfc = graph.addVertex(T.label, "vnfc",T.id, "0", "aai-node-type", "vnfc", 
						"vnfc-name", "vnfcName1", "nfc-naming-code", "blue", "nfc-function", "correct-function","model-invariant-id","modelinvariantid","model-customization-id","modelcustomizationid");		
		Vertex cp = graph.addVertex(T.label, "cp",T.id, "1", "aai-node-type", "cp","cp-id", "cpId");
		Vertex vlanTag = graph.addVertex(T.label, "vlan-tag",T.id, "2", "aai-node-type", "vlan-tag","vlan-tag-id", "vlanTagId");
		Vertex l3Network = graph.addVertex(T.label, "l3-network",T.id, "3", "aai-node-type", "l3-network","network-id", "networkId","is-provider-network",true);
		Vertex vserv = graph.addVertex(T.label, "vserver",T.id, "4", "aai-node-type", "vserver",
						"vserver-id", "vservId", "vserver-name", "vservName", "vserver-selflink", "me/self");
		Vertex linterface = graph.addVertex(T.label, "l-interface", T.id, "5", "aai-node-type", "l-interface", "l-interface-id", "l-interface-id0", "l-interface-name", "l-interface-name0","network-name","networkName0");
		
		Vertex vnfc1 = graph.addVertex(T.label, "vnfc",T.id, "6", "aai-node-type", "vnfc", 
						"vnfc-name", "vnfcName1", "nfc-naming-code", "blue", "nfc-function", "correct-function","model-invariant-id","modelinvariantid1","model-customization-id","modelcustomizationid1");		
		Vertex cp1 = graph.addVertex(T.label, "cp",T.id, "7", "aai-node-type", "cp","cp-id", "cpId");
		Vertex vlanTag1 = graph.addVertex(T.label, "vlan-tag",T.id, "8", "aai-node-type", "vlan-tag","vlan-tag-id", "vlanTagId");
		Vertex l3Network1 = graph.addVertex(T.label, "l3-network",T.id, "9", "aai-node-type", "l3-network","network-id", "networkId","is-provider-network",false);
		Vertex vserv1 = graph.addVertex(T.label, "vserver",T.id, "10", "aai-node-type", "vserver",
						"vserver-id", "vservId", "vserver-name", "vservName", "vserver-selflink", "me/self");
		Vertex linterface1 = graph.addVertex(T.label, "l-interface", T.id, "11", "aai-node-type", "l-interface", "l-interface-id", "l-interface-id0", "l-interface-name", "l-interface-name0","network-name","networkName0");

		GraphTraversalSource g = graph.traversal();
		
		rules.addTreeEdge(g, vnfc,cp);
		rules.addEdge(g, cp,vlanTag);
		rules.addEdge(g, cp,l3Network);		
		rules.addEdge(g, vnfc,vserv);
		rules.addTreeEdge(g, vserv, linterface);
		
		rules.addTreeEdge(g, vnfc1,cp1);
		rules.addEdge(g, cp1,vlanTag1);
		rules.addEdge(g, cp1,l3Network1);		
		rules.addEdge(g, vnfc1,vserv1);
		rules.addTreeEdge(g, vserv1, linterface1);
		
		expectedResult.add(vlanTag);
		expectedResult.add(l3Network);
		expectedResult.add(linterface);
		//expectedResult.add(vlanTag1);//false
		//expectedResult.add(l3Network1);//false
		//expectedResult.add(linterface1);//false
	}

	@Override
	protected String getQueryName() {
		return "l-interface-to-CP";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "vnfc").has("model-invariant-id", "modelinvariantid").has("model-customization-id", "modelcustomizationid");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("isProviderNetwork", true);
	}

	
}
