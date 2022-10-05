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
 * http://www.apache.org/licenses/LICENSE-2.0
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

public class SriovTopologyFromVnfQueryTest extends QueryTest {
    public SriovTopologyFromVnfQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        // Set up the test graph
        Vertex gvnf1 =
            graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type", "generic-vnf",
                "vnf-id", "gvnf1", "vnf-name", "genvnfname1", "nf-type", "sample-nf-type");
        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "1", "aai-node-type", "vserver",
            "vserver-name", "vservername1");
        Vertex lint1 = graph.addVertex(T.label, "l-interface", T.id, "2", "aai-node-type",
            "l-interface", "interface-name", "lint1", "is-port-mirrored", "true", "in-maint",
            "true", "is-ip-unnumbered", "false");

        Vertex sriovVf1 = graph.addVertex(T.label, "sriov-vf", T.id, "3", "aai-node-type",
            "sriov-vf", "pci-id", "pcIid1");
        Vertex sriovPf1 = graph.addVertex(T.label, "sriov-pf", T.id, "4", "aai-node-type",
            "sriov-pf", "pf-pci-id", "pfPciId1");

        Vertex pint1 = graph.addVertex(T.label, "p-interface", T.id, "5", "aai-node-type",
            "p-interface", "interface-name", "ge0/0/0");
        Vertex plink1 = graph.addVertex(T.label, "physical-link", T.id, "6", "aai-node-type",
            "physical-link", "link-name", "ge0/0/0-to-xe0/0/0");

        Vertex gvnf2 =
            graph.addVertex(T.label, "generic-vnf", T.id, "10", "aai-node-type", "generic-vnf",
                "vnf-id", "gvnf2", "vnf-name", "genvnfname2", "nf-type", "sample-nf-type1");
        Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "11", "aai-node-type",
            "vserver", "vserver-name", "vservername2");
        Vertex lint2 = graph.addVertex(T.label, "l-interface", T.id, "12", "aai-node-type",
            "l-interface", "interface-name", "lint2", "is-port-mirrored", "true", "in-maint",
            "true", "is-ip-unnumbered", "false");

        Vertex sriovVf2 = graph.addVertex(T.label, "sriov-vf", T.id, "13", "aai-node-type",
            "sriov-vf", "pci-id", "pcIid2");
        Vertex sriovPf2 = graph.addVertex(T.label, "sriov-pf", T.id, "14", "aai-node-type",
            "sriov-pf", "pf-pci-id", "pfPciId2");

        Vertex pint2 = graph.addVertex(T.label, "p-interface", T.id, "15", "aai-node-type",
            "p-interface", "interface-name", "ge0/0/1");
        Vertex plink2 = graph.addVertex(T.label, "physical-link", T.id, "16", "aai-node-type",
            "physical-link", "link-name", "ge0/0/0-to-xe0/0/1");

        GraphTraversalSource g = graph.traversal();

        rules.addEdge(g, gvnf1, vserver1);
        rules.addTreeEdge(g, vserver1, lint1);
        rules.addTreeEdge(g, lint1, sriovVf1);
        rules.addEdge(g, sriovVf1, sriovPf1);
        rules.addTreeEdge(g, sriovPf1, pint1);
        rules.addEdge(g, pint1, plink1);

        rules.addEdge(g, gvnf2, vserver2);// false
        rules.addTreeEdge(g, vserver2, lint2);// false
        rules.addTreeEdge(g, lint2, sriovVf2);// false
        rules.addEdge(g, sriovVf2, sriovPf2);// false
        rules.addTreeEdge(g, sriovPf2, pint2);// false
        rules.addEdge(g, pint2, plink2);// false

        expectedResult.add(gvnf1);
        expectedResult.add(vserver1);
        expectedResult.add(sriovVf1);
        expectedResult.add(plink1);

    }

    @Override
    protected String getQueryName() {
        return "sriov-topology-fromVnf";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "generic-vnf").has("vnf-id", "gvnf1").has("vnf-name", "genvnfname1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
