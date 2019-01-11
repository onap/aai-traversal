package org.onap.aai.rest.search;

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

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class FabricInformationFromVnfTest extends QueryTest{
	
	public FabricInformationFromVnfTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void test() {
		super.run();
	}
	
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//Set up the test graph
		Vertex gvnf = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-0", "vnf-name", "vnf-name-0", "vnf-type", "vnf-type-0");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "1", "aai-node-type", "vserver", "vserver-id", "vserver-id-0", "vserver-name", "vserver-name-0", "vserver-selflink", "vserver-selflink-0");
		Vertex linterface = graph.addVertex(T.label, "l-interface", T.id, "2", "aai-node-type", "l-interface", "interface-name", "interface-name-0");
		Vertex sriovvf = graph.addVertex(T.label, "sriov-vf", T.id, "3", "aai-node-type", "sriov-vf", "pci-id", "pci-id-0");
		Vertex sriovpf = graph.addVertex(T.label, "sriov-pf", T.id, "4", "aai-node-type", "sriov-pf", "pf-pci-id", "pf-pci-id-0");
		Vertex pinterface = graph.addVertex(T.label, "p-interface", T.id, "5", "aai-node-type", "p-interface", "interface-name", "interface-name-0");
		Vertex pserver = graph.addVertex(T.label, "p-server", T.id, "6", "aai-node-type", "pserver", "hostname", "hostname-0");
		Vertex vnfc = graph.addVertex(T.label, "vnfc", T.id, "7", "aai-node-type", "vnfc", "vnfc-name", "vnfc-name-0", "nfc-naming-code", "nfc-naming-code-0", "nfc-naming-function", "nfc-naming-function-0");
		Vertex cp = graph.addVertex(T.label, "cp", T.id, "8", "aai-node-type", "cp", "cp-instance-id", "cp-instance-id-0");
		Vertex vlantag = graph.addVertex(T.label, "vlan-tag", T.id, "9", "aai-node-type", "vlan-tag","vlan-tag-id", "vlan-tag-id-0");
		
		Vertex gvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "16", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1", "vnf-type", "vnf-type-1");
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "10", "aai-node-type", "vserver", "vserver-id", "vserver-id-1", "vserver-name", "vserver-name-0", "vserver-selflink", "vserver-selflink-0");
		Vertex vlantag1 = graph.addVertex(T.label, "vlan-tag", T.id, "11", "aai-node-type", "vlan-tag","vlan-tag-id", "vlan-tag-id-1");
		Vertex vnfc1 = graph.addVertex(T.label, "vnfc", T.id, "14", "aai-node-type", "vnfc", "vnfc-name", "vnfc-name-1", "nfc-naming-code", "nfc-naming-code-1", "nfc-naming-function", "nfc-naming-function-1");
		Vertex cp1 = graph.addVertex(T.label, "cp", T.id, "15", "aai-node-type", "cp", "cp-instance-id", "cp-instance-id-1");

		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, gvnf, vserver);
		rules.addTreeEdge(g, vserver, linterface);
		rules.addTreeEdge(g, linterface, sriovvf);
		rules.addEdge(g, sriovvf, sriovpf);
		rules.addTreeEdge(g, sriovpf, pinterface);
		rules.addTreeEdge(g, pinterface, pserver);
		rules.addEdge(g, vserver, vnfc);
		rules.addTreeEdge(g, vnfc, cp);
		rules.addEdge(g, cp, vlantag);
		
		rules.addEdge(g, gvnf1, vserver1);
		rules.addEdge(g, vserver1, vnfc1);
		rules.addTreeEdge(g, vnfc1, cp1);
		rules.addEdge(g, cp1, vlantag1);//false
		
		
		expectedResult.add(gvnf);
		expectedResult.add(vserver);
		expectedResult.add(pinterface);
		expectedResult.add(pserver);
		expectedResult.add(vnfc);
		expectedResult.add(vlantag);
	}

	@Override
	protected String getQueryName() {
		return "fabric-information-fromVnf";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "generic-vnf").has("vnf-id", "vnf-id-0");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}

}
