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

public class GetPinterfacePhysicalLinkBySvcInstIdTest extends QueryTest {

    public GetPinterfacePhysicalLinkBySvcInstIdTest()
        throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void test() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex serviceinstance = graph.addVertex(T.label, "service-instance", T.id, "0",
            "aai-node-type", "service-instance", "service-instance-id", "s-instance-id1");
        Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "1",
            "aai-node-type", "service-instance", "service-instance-id", "s-instance-id2");

        Vertex gnvf = graph.addVertex(T.label, "generic-vnf", T.id, "2", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1");
        Vertex gnvf1 = graph.addVertex(T.label, "generic-vnf", T.id, "3", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-2", "vnf-name", "vnf-name-2");

        Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "4", "aai-node-type", "pserver",
            "hostname", "pservername1");
        Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "5", "aai-node-type", "pserver",
            "hostname", "pservername2");

        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "6", "aai-node-type", "vserver",
            "vserver-id", "vserverid1");
        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "7", "aai-node-type", "vserver",
            "vserver-id", "vserverid2");

        Vertex pInterface = graph.addVertex(T.label, "p-interface", T.id, "8", "aai-node-type",
            "p-interface", "interface-name", "p-interface-1");
        Vertex pInterface1 = graph.addVertex(T.label, "p-interface", T.id, "9", "aai-node-type",
            "p-interface", "interface-name", "p-interface-2");

        Vertex plink = graph.addVertex(T.label, "physical-link", T.id, "10", "aai-node-type",
            "physical-link", "link-name", "link-name-1");
        Vertex plink1 = graph.addVertex(T.label, "physical-link", T.id, "11", "aai-node-type",
            "physical-link", "link-name", "link-name-2");

        GraphTraversalSource g = graph.traversal();

        rules.addEdge(g, serviceinstance, gnvf);
        rules.addEdge(g, gnvf, vserver);
        rules.addEdge(g, vserver, pserver);
        rules.addTreeEdge(g, pserver, pInterface);
        rules.addEdge(g, pInterface, plink);

        rules.addEdge(g, serviceinstance1, gnvf1);
        rules.addEdge(g, gnvf1, vserver1);
        rules.addEdge(g, vserver1, pserver1);
        rules.addTreeEdge(g, pserver1, pInterface1);
        rules.addEdge(g, pInterface1, plink1);

        expectedResult.add(serviceinstance);
        expectedResult.add(gnvf);
        expectedResult.add(vserver);
        expectedResult.add(pserver);
        expectedResult.add(pInterface);
        expectedResult.add(plink);

    }

    @Override
    protected String getQueryName() {
        return "getPinterfacePhysicalLinkBySvcInstId";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "service-instance").has("service-instance-id", "s-instance-id1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }

}
