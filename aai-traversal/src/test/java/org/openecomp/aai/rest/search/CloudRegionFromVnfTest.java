package org.openecomp.aai.rest.search;

import static org.junit.Assert.*;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class CloudRegionFromVnfTest extends QueryTest {

	public CloudRegionFromVnfTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex gv = graph.addVertex(T.id, "00", "aai-node-type", "generic-vnf", "vnf-id", "gvId", "vnf-name", "name", "vnf-type", "type");
		Vertex vnfc = graph.addVertex(T.id, "10", "aai-node-type", "vnfc", 
				"vnfc-name", "vnfcName1", "nfc-naming-code", "blue", "nfc-function", "correct-function");
		Vertex vserv = graph.addVertex(T.id, "20", "aai-node-type", "vserver",
				"vserver-id", "vservId", "vserver-name", "vservName", "vserver-selflink", "me/self");
		Vertex cr = graph.addVertex(T.id, "30", "aai-node-type", "cloud-region", "cloud-owner", "some guy", "cloud-region-id", "crId");
		Vertex tenant = graph.addVertex(T.id, "40", "aai-node-type", "tenant", "tenant-id", "ten1", "tenant-name", "tenName");
		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, gv, vnfc);
		rules.addEdge(g, vnfc, vserv);
		rules.addTreeEdge(g, cr, tenant);
		rules.addTreeEdge(g, tenant, vserv);
		
		expectedResult.add(cr);
		expectedResult.add(tenant);
		expectedResult.add(vnfc);
		expectedResult.add(vserv);
	}

	@Override
	protected String getQueryName() {
		return "cloud-region-fromVnf";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "generic-vnf").has("vnf-id", "gvId");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		// N/A for this query
	}

}
