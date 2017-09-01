package org.openecomp.aai.rest.search;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class LinkedDevices_ComplicatedGVtoGV extends QueryTest {

	public LinkedDevices_ComplicatedGVtoGV() throws AAIException, NoEdgeRuleFoundException {
		super();
	}
	
	@Test
	public void run() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		Vertex gvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "00", "aai-node-type", "generic-vnf", 
						"vnf-id", "gvnf1", "vnf-name", "genvnfname1", "nf-type", "sample-nf-type");
		
		Vertex lint1 = graph.addVertex(T.label, "l-interface", T.id, "10", "aai-node-type", "l-interface",
						"interface-name", "lint1", "is-port-mirrored", "true", "in-maint", "true", "is-ip-unnumbered", "false");
		
		Vertex loglink1 = graph.addVertex(T.label, "logical-link", T.id, "20", "aai-node-type", "logical-link",
						"link-name", "loglink1", "in-maint", "false", "link-type", "sausage");
		
		Vertex lint2 = graph.addVertex(T.label, "l-interface", T.id, "11", "aai-node-type", "l-interface",
						"interface-name", "lint2", "is-port-mirrored", "true", "in-maint", "true", "is-ip-unnumbered", "false");

		Vertex vlan = graph.addVertex(T.label, "vlan", T.id, "30", "aai-node-type", "vlan",
						"vlan-interface", "vlan1");
		
		Vertex loglink2 = graph.addVertex(T.label, "logical-link", T.id, "21", "aai-node-type", "logical-link",
						"link-name", "loglink2", "in-maint", "false", "link-type", "sausage");
		
		Vertex lagint = graph.addVertex(T.label, "lag-interface", T.id, "40", "aai-node-type", "lag-interface", 
						"interface-name", "lagint1");
		
		Vertex gvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "01", "aai-node-type", "generic-vnf", 
						"vnf-id", "gvnf2", "vnf-name", "genvnfname2", "nf-type", "sample-nf-type");
		
		GraphTraversalSource g = graph.traversal();
		rules.addTreeEdge(g, gvnf1, lint1);
		rules.addEdge(g, lint1, loglink1);
		rules.addEdge(g, lint2, loglink1);
		rules.addTreeEdge(g, lint2, vlan);
		rules.addEdge(g, vlan, loglink2);
		rules.addEdge(g, loglink2, lagint);
		rules.addTreeEdge(g, gvnf2, lagint);
		
		expectedResult.add(gvnf1);
		expectedResult.add(gvnf2);
	}

	@Override
	protected String getQueryName() {
		return "linked-devices";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type", "generic-vnf").has("vnf-id", "gvnf1");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		// n/a for this query
	}

}
