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

public class VserverFromVnfQueryTest extends QueryTest {

	public VserverFromVnfQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex gv = graph.addVertex(T.id, "00", "aai-node-type", "generic-vnf", "vnf-id", "gvId", "vnf-name", "gvName", "vnf-type", "some-type");
		Vertex vnfc = graph.addVertex(T.id, "10", "aai-node-type", "vnfc", 
						"vnfc-name", "vnfcName1", "nfc-naming-code", "blue", "nfc-function", "correct-function");
		Vertex vserv = graph.addVertex(T.id, "20", "aai-node-type", "vserver",
						"vserver-id", "vservId", "vserver-name", "vservName", "vserver-selflink", "me/self");
		Vertex lint = graph.addVertex(T.id, "30", "aai-node-type", "l-interface", "interface-name", "lintName");
		Vertex ipv4 = graph.addVertex(T.id, "40", "aai-node-type", "l3-interface-ipv4-address-list", "l3-interface-ipv4-address", "0.0.0.0");
		Vertex ipv6 = graph.addVertex(T.id, "50", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address", "0.0.0.0");
		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, gv, vnfc);
		rules.addEdge(g, vserv, vnfc);
		rules.addTreeEdge(g, vserv, lint);
		rules.addTreeEdge(g, lint, ipv4);
		rules.addTreeEdge(g, lint, ipv6);
		
		expectedResult.add(vserv);
		expectedResult.add(lint);
		expectedResult.add(ipv4);
		expectedResult.add(ipv6);
		expectedResult.add(vnfc);
	}

	@Override
	protected String getQueryName() {
		return "vserver-fromVnf";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "generic-vnf").has("vnf-id", "gvId");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("nfcFunction", "correct-function");
	}

	
}
