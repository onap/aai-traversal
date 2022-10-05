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

public class UcpeInstanceQueryTest extends QueryTest {

    public UcpeInstanceQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        Vertex complex = graph.addVertex(T.label, "complex", T.id, "0", "aai-node-type", "complex",
            "physical-location-id", "clli");
        Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "1", "aai-node-type", "pserver",
            "hostname", "pservername");
        Vertex pnf = graph.addVertex(T.label, "pnf", T.id, "11", "aai-node-type", "pnf", "pnf-name",
            "pnfname");
        Vertex vnf = graph.addVertex(T.label, "generic-vnf", T.id, "7", "aai-node-type",
            "generic-vnf", "vnf-id", "vnfuuid");
        Vertex pserverint = graph.addVertex(T.label, "p-interface", T.id, "2", "aai-node-type",
            "p-interface", "interface-name", "xe0/0/0");
        Vertex pnfint = graph.addVertex(T.label, "p-interface", T.id, "12", "aai-node-type",
            "p-interface", "interface-name", "ge0/0/0");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, pserver, complex);
        rules.addEdge(g, pnf, complex);
        rules.addEdge(g, vnf, pserver);
        rules.addTreeEdge(g, pserver, pserverint);
        rules.addTreeEdge(g, pnf, pnfint);

        expectedResult.add(complex);
        expectedResult.add(pserver);
        // expectedResult.add(pnf);

    }

    @Override
    protected String getQueryName() {
        return "ucpe-instance";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("hostname", "pservername");
        // g.has("pnf-name", "pnfname");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;

    }

}
