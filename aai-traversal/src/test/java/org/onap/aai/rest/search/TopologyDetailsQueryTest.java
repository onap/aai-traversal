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

public class TopologyDetailsQueryTest extends QueryTest {
	public TopologyDetailsQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	@Test
	public void run() {
		super.run();
	}
	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//set up test graph
		Vertex genericvnf = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type", "generic-vnf", "vnf-id", "vnfid0","vnf-name", "vnf-name-1", "nf-type", "sample-nf-type");
		Vertex platform = graph.addVertex(T.label, "platform", T.id, "1", "aai-node-type", "platform", "platform-name", "platform0");
		Vertex lineofbusiness = graph.addVertex(T.label, "line-of-business", T.id, "2", "aai-node-type", "line-of-business", "line-of-business-name", "business0");
		Vertex servinst = graph.addVertex(T.label, "service-instance", T.id, "3", "aai-node-type", "service-instance", "service-instance-id", "servInstId0", "service-type", "servType0");
		Vertex owningentity = graph.addVertex(T.label, "owning-entity", T.id, "4", "aai-node-type", "owning-entity", "owning-entity-id", "entityId0", "owning-entity-name", "entityName0");
		Vertex project = graph.addVertex(T.label, "project", T.id, "5", "aai-node-type", "project", "project-name", "project0");
		Vertex vnfc = graph.addVertex(T.label, "vnfc", T.id, "6", "aai-node-type", "vnfc", "vnfc-name", "vnfc0", "nfc-naming-code", "namingCode0", "nfc-function", "function0");
		Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "7", "aai-node-type", "vserver", "vserver-id", "vserverid0");
		Vertex linterface = graph.addVertex(T.label, "l-interface", T.id, "8", "aai-node-type", "l-interface", "l-interface-id", "l-interface-id0", "l-interface-name", "l-interface-name0");
		Vertex l3inter1ipv4addresslist = graph.addVertex(T.label, "interface-ipv4-address-list", T.id, "9", "aai-node-type", "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "l3-interface-ipv4-address-0");
		Vertex subnet4 = graph.addVertex(T.label, "subnet", T.id, "10", "aai-node-type", "subnet", "subnet-id", "subnet4-id-0");
		Vertex l3network4 = graph.addVertex(T.label, "l3-network", T.id, "11", "aai-node-type", "l3-network", "network-id", "network4-id-0", "network-name", "network4-name0");		
		Vertex l3inter1ipv6addresslist = graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "12", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "l3-interface-ipv6-address-0");
		Vertex subnet6 = graph.addVertex(T.label, "subnet", T.id, "13", "aai-node-type", "subnet", "subnet-id", "subnet6-id-0");	
		Vertex l3network6 = graph.addVertex(T.label, "l3-network", T.id, "14", "aai-node-type", "l3-network", "network-id", "network6-id-0", "network-name", "network6-name0");
		Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "15", "aai-node-type", "tenant", "tenant-id", "tenantid0", "tenant-name", "tenantName0");
		Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "16", "aai-node-type", "cloud-region", "cloud-region-id", "regionid0", "cloud-owner", "cloudOwnername0");
		Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "17", "aai-node-type", "pserver", "hostname", "pservername1");
		Vertex complex = graph.addVertex(T.label, "pserver", T.id, "18", "aai-node-type", "complex", "physical-location-id", "locationId", "physical-location-type", "locationType", "physical-location-id", "locationId", 
				"city", "cityName", "state", "stateName", "postal-code", "zip", "country", "countryName");



		Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "20", "aai-node-type", "generic-vnf", "vnf-id", "vnfid1","vnf-name", "vnf-name-2", "nf-type", "sample-nf-type1");
		Vertex platform1 = graph.addVertex(T.label, "platform", T.id, "21", "aai-node-type", "platform", "platform-name", "platform1");
		Vertex lineofbusiness1 = graph.addVertex(T.label, "line-of-business", T.id, "22", "aai-node-type", "line-of-business", "line-of-business-name", "business1");
		Vertex servinst1 = graph.addVertex(T.label, "service-instance", T.id, "23", "aai-node-type", "service-instance", "service-instance-id", "servInstId1", "service-type", "servType1");
		Vertex owningentity1 = graph.addVertex(T.label, "owning-entity", T.id, "24", "aai-node-type", "owning-entity", "owning-entity-id", "entityId1", "owning-entity-name", "entityName1");
		Vertex project1 = graph.addVertex(T.label, "project", T.id, "25", "aai-node-type", "project", "project-name", "project1");
		Vertex vnfc1 = graph.addVertex(T.label, "vnfc", T.id, "26", "aai-node-type", "vnfc", "vnfc-name", "vnfc1", "nfc-naming-code", "namingCode1", "nfc-function", "function1");
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "27", "aai-node-type", "vserver", "vserver-id", "vserverid1");
		Vertex linterface1 = graph.addVertex(T.label, "l-interface", T.id, "28", "aai-node-type", "l-interface", "l-interface-id", "l-interface-id1", "l-interface-name", "l-interface-name1");
		Vertex l3inter1ipv4addresslist1 = graph.addVertex(T.label, "interface-ipv4-address-list", T.id, "29", "aai-node-type", "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "l3-interface-ipv4-address-1");
		Vertex subnet41 = graph.addVertex(T.label, "subnet", T.id, "30", "aai-node-type", "subnet", "subnet-id", "subnet4-id-1");
		Vertex l3network41 = graph.addVertex(T.label, "l3-network", T.id, "31", "aai-node-type", "l3-network", "network-id", "network4-id-1", "network-name", "network4-name1");		
		Vertex l3inter1ipv6addresslist1 = graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "32", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "l3-interface-ipv6-address-1");
		Vertex subnet61 = graph.addVertex(T.label, "subnet", T.id, "33", "aai-node-type", "subnet", "subnet-id", "subnet6-id-1");	
		Vertex l3network61 = graph.addVertex(T.label, "l3-network", T.id, "34", "aai-node-type", "l3-network", "network-id", "network6-id-1", "network-name", "network6-name1");
		Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "35", "aai-node-type", "tenant", "tenant-id", "tenantid0", "tenant-name", "tenantName1");
		Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "36", "aai-node-type", "cloud-region", "cloud-region-id", "regionid1", "cloud-owner", "cloudOwnername1");
		Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "37", "aai-node-type", "pserver", "hostname", "pservername2");
		Vertex complex1 = graph.addVertex(T.label, "pserver", T.id, "38", "aai-node-type", "complex", "physical-location-id", "locationId1", "physical-location-type", "locationType1", "physical-location-id", "locationId1", 
				"city", "cityName1", "state", "stateName1", "postal-code", "zip1", "country", "countryName1");


		GraphTraversalSource g = graph.traversal();

		rules.addEdge(g, genericvnf, platform);
		rules.addEdge(g, genericvnf, lineofbusiness);
		rules.addEdge(g, genericvnf, servinst);
		rules.addEdge(g, owningentity, servinst);
		rules.addEdge(g, project, servinst);
		rules.addEdge(g, genericvnf, vnfc);
		rules.addEdge(g, genericvnf,vserver);
		rules.addTreeEdge(g, linterface, vserver);
		rules.addTreeEdge(g, l3inter1ipv4addresslist, linterface);
		rules.addEdge(g, l3inter1ipv4addresslist, subnet4);
		rules.addTreeEdge(g, l3network4, subnet4);
		rules.addTreeEdge(g, l3inter1ipv6addresslist, linterface);
		rules.addEdge(g, l3inter1ipv6addresslist, subnet6);
		rules.addTreeEdge(g, l3network6, subnet6);
		rules.addTreeEdge(g, vserver, tenant);
		rules.addTreeEdge(g, tenant, cloudregion);
		rules.addEdge(g, pserver, vserver);
		rules.addEdge(g, complex, pserver);


		//false
		rules.addEdge(g, genericvnf1, platform1);
		rules.addEdge(g, genericvnf1, lineofbusiness1);
		rules.addEdge(g, genericvnf1, servinst1);
		rules.addEdge(g, owningentity1, servinst1);
		rules.addEdge(g, project1, servinst1);
		rules.addEdge(g, genericvnf1, vnfc1);
		rules.addEdge(g, genericvnf1,vserver1);
		rules.addTreeEdge(g, linterface1, vserver1);
		rules.addTreeEdge(g, l3inter1ipv4addresslist1, linterface1);
		rules.addEdge(g, l3inter1ipv4addresslist1, subnet41);
		rules.addTreeEdge(g, l3network41, subnet41);
		rules.addTreeEdge(g, l3inter1ipv6addresslist1, linterface1);
		rules.addEdge(g, l3inter1ipv6addresslist1, subnet61);
		rules.addTreeEdge(g, l3network61, subnet61);
		rules.addTreeEdge(g, vserver1, tenant1);
		rules.addTreeEdge(g, tenant1, cloudregion1);
		rules.addEdge(g, pserver1, vserver1);
		rules.addEdge(g, complex1, pserver1);




		expectedResult.add(genericvnf);
		expectedResult.add(platform);
		expectedResult.add(lineofbusiness);
		expectedResult.add(owningentity);
		expectedResult.add(project);
		expectedResult.add(vnfc);
		expectedResult.add(vserver);
		expectedResult.add(linterface);
		expectedResult.add(l3inter1ipv4addresslist);
		expectedResult.add(subnet4);
		expectedResult.add(l3network4);
		expectedResult.add(l3inter1ipv6addresslist);
		expectedResult.add(subnet6);
		expectedResult.add(l3network6);
		expectedResult.add(cloudregion);
		expectedResult.add(complex);



	}
	@Override
	protected String getQueryName() {
		return	"topology-detail";
	}
	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "generic-vnf").has("vnf-name", "vnf-name-1").has("vnf-id", "vnfid0");

	}
	@Override
	protected void addParam(Map<String, Object> params) {
		return;
	}
}
