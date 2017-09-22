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

public class NetworkNameFromNetworkRoleQueryTest extends QueryTest {

	public NetworkNameFromNetworkRoleQueryTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex cr1 = graph.addVertex(T.id, "00", "aai-node-type", "cloud-region", "cloud-owner", "foo", "cloud-region-id", "cr1");
		Vertex cr2 = graph.addVertex(T.id, "01", "aai-node-type", "cloud-region", "cloud-owner", "bar", "cloud-region-id", "cr2");
		
		Vertex l3net1 = graph.addVertex(T.id, "10", "aai-node-type", "l3-network", 
							"network-id", "net1", "network-name", "netname1", "network-role", "correct-role");
		Vertex l3net2 = graph.addVertex(T.id, "11", "aai-node-type", "l3-network",
							"network-id", "net2", "network-name", "netname2", "network-role", "wrong-role");
		Vertex l3net3 = graph.addVertex(T.id, "12", "aai-node-type", "l3-network",
							"network-id", "net3", "network-name", "netname3", "network-role", "correct-role");
		
		Vertex np1 = graph.addVertex(T.id, "20", "aai-node-type", "network-policy", "network-policy-id", "npId1");
		Vertex np2 = graph.addVertex(T.id, "21", "aai-node-type", "network-policy", "network-policy-id", "npId2");
		Vertex np3 = graph.addVertex(T.id, "22", "aai-node-type", "network-policy", "network-policy-id", "npId3");
		
		GraphTraversalSource g = graph.traversal();
		rules.addEdge(g, cr1, l3net1);
		rules.addEdge(g, l3net1, np1);
		rules.addEdge(g, cr1, l3net2);
		rules.addEdge(g, l3net2, np2);
		rules.addEdge(g, cr2, l3net3);
		rules.addEdge(g, l3net3, np3);
		
		expectedResult.add(l3net1);
		expectedResult.add(np1);
	}

	@Override
	protected String getQueryName() {
		return "network-name-fromNetwork-role";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "cloud-region").has("cloud-owner", "foo").has("cloud-region-id", "cr1");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("networkRole", "correct-role");
	}
}
