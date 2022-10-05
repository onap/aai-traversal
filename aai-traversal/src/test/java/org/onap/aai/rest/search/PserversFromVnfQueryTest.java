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

public class PserversFromVnfQueryTest extends QueryTest {
    public PserversFromVnfQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        // Set up the test graph
        Vertex complex = graph.addVertex(T.label, "complex", T.id, "0", "aai-node-type", "complex",
            "physical-location-id", "clli");

        Vertex vnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type",
            "generic-vnf", "vnf-id", "vnfid1");
        Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "2", "aai-node-type", "pserver",
            "hostname", "pservername1");

        Vertex vnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "3", "aai-node-type",
            "generic-vnf", "vnf-id", "vnfid2");
        Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "4", "aai-node-type", "pserver",
            "hostname", "pservername2");
        Vertex pserver3 = graph.addVertex(T.label, "pserver", T.id, "5", "aai-node-type", "pserver",
            "hostname", "pservername3");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "6", "aai-node-type", "vserver",
            "vserver-name", "vservername");
        Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "7", "aai-node-type", "tenant",
            "tenant-id", "tenantuuid", "tenant-name", "tenantname");
        Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "8", "aai-node-type",
            "cloud-region", "cloud-region-id", "clouduuid", "cloud-region-owner", "cloudOwnername");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, pserver1, complex);
        rules.addEdge(g, pserver2, complex);

        rules.addEdge(g, pserver1, vnf1);
        rules.addEdge(g, pserver2, vnf2);

        rules.addEdge(g, pserver3, complex);
        rules.addTreeEdge(g, tenant, cloudregion);
        rules.addTreeEdge(g, vserver, tenant);
        rules.addEdge(g, vserver, pserver3);
        rules.addEdge(g, vserver, vnf2);

        expectedResult.add(pserver2);
        expectedResult.add(pserver3);

    }

    @Override
    protected String getQueryName() {
        return "pservers-fromVnf";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("vnf-id", "vnfid2");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
