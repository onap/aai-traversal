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

public class GenericVnfsFromPserverTest extends QueryTest {
    public GenericVnfsFromPserverTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph

        Vertex vnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1", "vnf-type", "test",
            "nf-function", "test", "nf-role", "test", "nf-naming-code", "test");
        Vertex vnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-2", "vnf-name", "vnf-name-2", "vnf-type", "test",
            "nf-function", "test", "nf-role", "test", "nf-naming-code", "test");
        Vertex vnf3 = graph.addVertex(T.label, "generic-vnf", T.id, "2", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-3", "vnf-name", "vnf-name-3", "vnf-type", "test",
            "nf-function", "test", "nf-role", "test", "nf-naming-code", "test");
        Vertex vnf4 = graph.addVertex(T.label, "generic-vnf", T.id, "3", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-4", "vnf-name", "vnf-name-4", "vnf-type", "test",
            "nf-function", "test", "nf-role", "test", "nf-naming-code", "test");
        Vertex badVnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "4", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-bad-1", "vnf-name", "vnf-bad-1", "vnf-type", "test");
        Vertex badVnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "5", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-bad-2", "vnf-name", "vnf-bad-2", "nf-function", "test");
        Vertex badVnf3 = graph.addVertex(T.label, "generic-vnf", T.id, "6", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-bad-3", "vnf-name", "vnf-bad-3", "vnf-type", "bad",
            "nf-function", "bad");
        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "7", "aai-node-type", "vserver",
            "vserver-id", "vserver-id-1", "vserver-name", "vserver-name-1");
        Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "8", "aai-node-type", "vserver",
            "vserver-id", "vserver-id-2", "vserver-name", "vserver-name-2");
        Vertex badVserver = graph.addVertex(T.label, "vserver", T.id, "9", "aai-node-type",
            "vserver", "vserver-id", "vserver-bad", "vserver-name", "vserver-bad");
        Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "10", "aai-node-type",
            "pserver", "hostname", "hostname-1", "fqdn", "fqdn");
        Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "11", "aai-node-type",
            "pserver", "hostname", "hostname-2", "fqdn", "fqdn");
        Vertex pserver3 = graph.addVertex(T.label, "pserver", T.id, "12", "aai-node-type",
            "pserver", "hostname", "hostname-3", "fqdn", "fqdn");
        Vertex badPserver1 = graph.addVertex(T.label, "pserver", T.id, "13", "aai-node-type",
            "pserver", "hostname", "hostname-bad-1", "fqdn", "fqdn");
        Vertex badPserver2 = graph.addVertex(T.label, "pserver", T.id, "14", "aai-node-type",
            "pserver", "hostname", "hostname-bad-2", "fqdn", "fqdn");
        Vertex badPserver3 = graph.addVertex(T.label, "pserver", T.id, "15", "aai-node-type",
            "pserver", "hostname", "hostname-bad-3", "fqdn", "fqdn");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, pserver1, vnf1);
        rules.addEdge(g, pserver2, vserver1);
        rules.addEdge(g, vserver1, vnf2);
        rules.addEdge(g, pserver3, vnf3);
        rules.addEdge(g, pserver3, vserver2);
        rules.addEdge(g, vserver2, vnf4);
        rules.addEdge(g, badPserver1, badVnf1);
        rules.addEdge(g, badPserver2, badVserver);
        rules.addEdge(g, badVserver, badVnf2);
        rules.addEdge(g, badPserver3, badVnf3);

        expectedResult.add(vnf1);
        expectedResult.add(vnf2);
        expectedResult.add(vnf3);
        expectedResult.add(vnf4);
    }

    @Override
    protected String getQueryName() {
        return "genericVnfs-fromPserver";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "pserver").has("fqdn", "fqdn");

    }

    @Override
    protected void addParam(Map<String, Object> params) {
        params.put("vnfType", "test");
        params.put("nfFunction", "test");
        params.put("nfRole", "test");
        params.put("nfNamingCode", "test");
    }
}
