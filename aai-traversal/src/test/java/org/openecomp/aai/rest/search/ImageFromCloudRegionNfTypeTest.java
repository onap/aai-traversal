package org.openecomp.aai.rest.search;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class ImageFromCloudRegionNfTypeTest extends QueryTest {

	public ImageFromCloudRegionNfTypeTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	
	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "10", "aai-node-type", "cloud-region", "cloud-region-id", "cloudreg1", "cloud-region-owner", "cloudOwnername00");
		Vertex cloudregion2 = graph.addVertex(T.label, "cloud-region", T.id, "11", "aai-node-type", "cloud-region", "cloud-region-id", "cloudregWrong", "cloud-region-owner", "cloudOwnername01");
		
		Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "20", "aai-node-type", "generic-vnf", "vnf-id", "genvnf1", "vnf-name", "genvnfname1", "nf-type", "sample-nf-type");
		Vertex genericvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "21", "aai-node-type", "generic-vnf", "vnf-id", "genvnf2", "vnf-name", "genvnfname2", "nf-type", "wrong-nf-type");
		Vertex genericvnf3 = graph.addVertex(T.label, "generic-vnf", T.id, "22", "aai-node-type", "generic-vnf", "vnf-id", "genvnf3", "vnf-name", "genvnfname3", "nf-type", "sample-nf-type");
		
		Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "30", "aai-node-type", "tenant", "tenant-id", "tenantid01", "tenant-name", "tenantName01");
		Vertex tenant2 = graph.addVertex(T.label, "tenant", T.id, "31", "aai-node-type", "tenant", "tenant-id", "tenantid02", "tenant-name", "tenantName02");
		
		Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "40", "aai-node-type", "vserver", "vserver-id", "vserverid01");
		Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "41", "aai-node-type", "vserver", "vserver-id", "vserverid02");
		Vertex vserver3 = graph.addVertex(T.label, "vserver", T.id, "42", "aai-node-type", "vserver", "vserver-id", "vserverid03");
		Vertex vserver4 = graph.addVertex(T.label, "vserver", T.id, "43", "aai-node-type", "vserver", "vserver-id", "vserverid04");
		Vertex vserver5 = graph.addVertex(T.label, "vserver", T.id, "44", "aai-node-type", "vserver", "vserver-id", "vserverid05");
		
		Vertex image1 = graph.addVertex(T.label, "image", T.id, "50", "aai-node-type", "image", 
												"image-id", "image1", "image-name", "imageName1", "image-os-distro", "boopOS", "image-os-version", "1.0", "image-selflink", "self/link");
		Vertex image2 = graph.addVertex(T.label, "image", T.id, "51", "aai-node-type", "image", 
				"image-id", "image2", "image-name", "imageName2", "image-os-distro", "beepOS", "image-os-version", "1.0", "image-selflink", "self/link");
		Vertex image3 = graph.addVertex(T.label, "image", T.id, "52", "aai-node-type", "image", 
				"image-id", "image3", "image-name", "imageName3", "image-os-distro", "beepOS", "image-os-version", "1.1", "image-selflink", "self/link");
		Vertex image4 = graph.addVertex(T.label, "image", T.id, "53", "aai-node-type", "image", 
				"image-id", "image4", "image-name", "imageName4", "image-os-distro", "beepOS", "image-os-version", "1.1", "image-selflink", "self/link");
		
		GraphTraversalSource g = graph.traversal();
		
		rules.addTreeEdge(g, cloudregion1, tenant1);
		rules.addTreeEdge(g, cloudregion2, tenant2);
		rules.addTreeEdge(g, tenant1, vserver1);
		rules.addTreeEdge(g, tenant1, vserver2);
		rules.addTreeEdge(g, tenant2, vserver3);
		rules.addTreeEdge(g, tenant1, vserver4);
		rules.addTreeEdge(g, tenant1, vserver5);
		rules.addEdge(g, vserver1, image1);
		rules.addEdge(g, vserver2, image2);
		rules.addEdge(g, vserver3, image3);
		rules.addEdge(g, vserver4, image4);
		rules.addEdge(g, vserver5, image1);
		rules.addEdge(g, genericvnf1, vserver1);
		rules.addEdge(g, genericvnf2, vserver2);
		rules.addEdge(g, genericvnf1, vserver3);
		rules.addEdge(g, genericvnf3, vserver4);
		
		expectedResult.add(image1);
		expectedResult.add(image4);
	}

	@Override
	protected String getQueryName() {
		return "images-fromCloudRegionNfType";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type","cloud-region").has("cloud-region-id","cloudreg1");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("nfType", "sample-nf-type");
	}

}
