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

public class CloudRegionFromNfTypeVendorVersion_withOptionalTest extends QueryTest {

	public CloudRegionFromNfTypeVendorVersion_withOptionalTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	
	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex image1 = graph.addVertex(T.label, "image", T.id, "10", "aai-node-type", "image", 
				"image-id", "image1", "image-name", "imageName1", "image-os-distro", "boopOS", "image-os-version", "1.0", "image-selflink", "self/link",
				"application-vendor","vendor1","application-version","1.0");
		Vertex image2 = graph.addVertex(T.label, "image", T.id, "11", "aai-node-type", "image", 
				"image-id", "image2", "image-name", "imageName2", "image-os-distro", "boopOS", "image-os-version", "1.0", "image-selflink", "self/link",
				"application-vendor","vendor1","application-version","2.1");
		Vertex image3 = graph.addVertex(T.label, "image", T.id, "12", "aai-node-type", "image", 
				"image-id", "image3", "image-name", "imageName3", "image-os-distro", "boopOS", "image-os-version", "1.0", "image-selflink", "self/link",
				"application-vendor","wrongVendor","application-version","1.0");
		
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "20", "aai-node-type", "vserver", "vserver-id", "vserverid01");
		Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "21", "aai-node-type", "vserver", "vserver-id", "vserverid02");
		Vertex vserver3 = graph.addVertex(T.label, "vserver", T.id, "22", "aai-node-type", "vserver", "vserver-id", "vserverid03");
		
		Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "30", "aai-node-type", "tenant", "tenant-id", "tenantid01", "tenant-name", "tenantName01");
		Vertex tenant2 = graph.addVertex(T.label, "tenant", T.id, "31", "aai-node-type", "tenant", "tenant-id", "tenantid02", "tenant-name", "tenantName02");
		Vertex tenant3 = graph.addVertex(T.label, "tenant", T.id, "32", "aai-node-type", "tenant", "tenant-id", "tenantid03", "tenant-name", "tenantName03");
		
		Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "40", "aai-node-type", "generic-vnf", "vnf-id", "genvnf1", "vnf-name", "genvnfname1", "nf-type", "sample-nf-type");
		
		Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "50", "aai-node-type", "cloud-region", "cloud-region-id", "cloudreg1", "cloud-region-owner", "cloudOwnername00");
		Vertex cloudregion2 = graph.addVertex(T.label, "cloud-region", T.id, "51", "aai-node-type", "cloud-region", "cloud-region-id", "cloudreg2", "cloud-region-owner", "cloudOwnername01");
		Vertex cloudregion3 = graph.addVertex(T.label, "cloud-region", T.id, "52", "aai-node-type", "cloud-region", "cloud-region-id", "cloudreg3", "cloud-region-owner", "cloudOwnername02");
		
		GraphTraversalSource g = graph.traversal();
		
		rules.addTreeEdge(g, cloudregion1, tenant1);
		rules.addTreeEdge(g, cloudregion2, tenant2);
		rules.addTreeEdge(g, cloudregion3, tenant3);
		rules.addTreeEdge(g, tenant1, vserver1);
		rules.addTreeEdge(g, tenant2, vserver2);
		rules.addTreeEdge(g, tenant3, vserver3);
		rules.addEdge(g, genericvnf1, vserver1);
		rules.addEdge(g, genericvnf1, vserver2);
		rules.addEdge(g, genericvnf1, vserver3);
		rules.addEdge(g, vserver1, image1);
		rules.addEdge(g, vserver2, image2);
		rules.addEdge(g, vserver3, image3);
		
		expectedResult.add(cloudregion1);
	}

	@Override
	protected String getQueryName() {
		return "cloudRegion-fromNfTypeVendorVersion";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type","image").has("application-vendor","vendor1").has("application-version","1.0");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("nfType", "sample-nf-type");
	}

}
