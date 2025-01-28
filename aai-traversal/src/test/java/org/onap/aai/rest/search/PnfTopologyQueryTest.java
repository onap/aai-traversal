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

public class PnfTopologyQueryTest extends QueryTest {

    public PnfTopologyQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex pnf1 = graph.addVertex(T.label, "pnf", T.id, "0", "aai-node-type", "pnf", "pnf-name",
            "pnf1name");
        Vertex complex = graph.addVertex(T.label, "complex", T.id, "1", "aai-node-type", "complex",
            "physical-location-id", "clli");
        Vertex pnf1int1 = graph.addVertex(T.label, "p-interface", T.id, "2", "aai-node-type",
            "p-interface", "interface-name", "ge0/0/0");
        Vertex pnf1int2 = graph.addVertex(T.label, "p-interface", T.id, "3", "aai-node-type",
            "p-interface", "interface-name", "ge0/0/1");
        Vertex pnf1int3 = graph.addVertex(T.label, "p-interface", T.id, "4", "aai-node-type",
            "p-interface", "interface-name", "ge0/0/2");
        Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "5", "aai-node-type", "pserver",
            "hostname", "pservername");
        Vertex pserverint = graph.addVertex(T.label, "p-interface", T.id, "6", "aai-node-type",
            "p-interface", "interface-name", "xe0/0/0");
        Vertex plink1 = graph.addVertex(T.label, "physical-link", T.id, "7", "aai-node-type",
            "physical-link", "link-name", "ge0/0/0-to-xe0/0/0");
        Vertex pnf2 = graph.addVertex(T.label, "pnf", T.id, "8", "aai-node-type", "pnf", "pnf-name",
            "pnf2name");
        Vertex pnf2int = graph.addVertex(T.label, "p-interface", T.id, "9", "aai-node-type",
            "p-interface", "interface-name", "ge0/1/0");
        Vertex plink2 = graph.addVertex(T.label, "physical-link", T.id, "10", "aai-node-type",
            "physical-link", "link-name", "ge0/0/1-to-ge0/1/0");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "11", "aai-node-type", "vserver",
            "vserver-name", "vservername");
        Vertex pserverint2 = graph.addVertex(T.label, "p-interface", T.id, "12", "aai-node-type",
            "p-interface", "interface-name", "xe0/0/1");
        Vertex pnf2int2 = graph.addVertex(T.label, "p-interface", T.id, "13", "aai-node-type",
            "p-interface", "interface-name", "ge0/1/0");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, pnf1, complex);
        rules.addTreeEdge(g, pnf1, pnf1int1);
        rules.addTreeEdge(g, pnf1, pnf1int2);
        rules.addTreeEdge(g, pnf1, pnf1int3);
        rules.addEdge(g, pserver, complex);
        rules.addTreeEdge(g, pserver, pserverint);
        rules.addEdge(g, pnf1int1, plink1);
        rules.addEdge(g, pserverint, plink1);
        rules.addEdge(g, pnf2, complex);
        rules.addTreeEdge(g, pnf2, pnf2int);
        rules.addEdge(g, pnf1int2, plink2);
        rules.addEdge(g, pnf2int, plink2);
        rules.addEdge(g, vserver, pserver);
        rules.addTreeEdge(g, pserver, pserverint2);
        rules.addTreeEdge(g, pnf2, pnf2int2);

        expectedResult.add(pnf1);
        expectedResult.add(complex);
        expectedResult.add(pnf1int1);
        expectedResult.add(pnf1int2);
        expectedResult.add(pserver);
        expectedResult.add(pserverint);
        expectedResult.add(plink1);
        expectedResult.add(pnf2);
        expectedResult.add(pnf2int);
        expectedResult.add(plink2);
    }

    @Override
    protected String getQueryName() {
        // TODO Auto-generated method stub
        return "pnf-topology";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("pnf-name", "pnf1name");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
