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

public class UcpeTopologyQueryTest extends QueryTest {
    public UcpeTopologyQueryTest() throws AAIException, NoEdgeRuleFoundException {
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
        Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "1", "aai-node-type", "pserver",
            "hostname", "pservername");
        Vertex pserverint1 = graph.addVertex(T.label, "p-interface", T.id, "2", "aai-node-type",
            "p-interface", "interface-name", "xe0/0/0");
        Vertex pserverint2 = graph.addVertex(T.label, "p-interface", T.id, "3", "aai-node-type",
            "p-interface", "interface-name", "xe0/0/0");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "4", "aai-node-type", "vserver",
            "vserver-name", "vservername");
        Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "5", "aai-node-type", "tenant",
            "tenant-id", "tenantuuid", "tenant-name", "tenantname");
        Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "6", "aai-node-type",
            "cloud-region", "cloud-region-id", "clouduuid", "cloud-region-owner", "cloudOwnername");
        Vertex vnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "7", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf1uuid");
        Vertex vnf1image = graph.addVertex(T.label, "vnf-image", T.id, "8", "aai-node-type",
            "vnf-image", "att-uuid", "vnf1imageuuid");
        Vertex vnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "9", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf1uuid");
        Vertex vnf2image = graph.addVertex(T.label, "vnf-image", T.id, "10", "aai-node-type",
            "vnf-image", "att-uuid", "vnf2imageuuid");
        Vertex pnf = graph.addVertex(T.label, "pnf", T.id, "11", "aai-node-type", "pnf", "pnf-name",
            "pnf1name");
        Vertex pnfint = graph.addVertex(T.label, "p-interface", T.id, "12", "aai-node-type",
            "p-interface", "interface-name", "ge0/0/0");
        Vertex plink1 = graph.addVertex(T.label, "physical-link", T.id, "13", "aai-node-type",
            "physical-link", "link-name", "ge0/0/0-to-xe0/0/0");
        Vertex servinst1 = graph.addVertex(T.label, "service-instance", T.id, "14", "aai-node-type",
            "service-instance", "service-instance-id", "servinst1uuid");
        Vertex servsub1 = graph.addVertex(T.label, "service-subscription", T.id, "15",
            "aai-node-type", "service-subscription", "service-type", "servType1");
        Vertex customer = graph.addVertex(T.label, "customer", T.id, "16", "aai-node-type",
            "customer", "global-customer-id", "custuuid");
        Vertex servinst2 = graph.addVertex(T.label, "service-instance", T.id, "17", "aai-node-type",
            "service-instance", "service-instance-id", "servinst2uuid");
        Vertex servsub2 = graph.addVertex(T.label, "service-subscription", T.id, "18",
            "aai-node-type", "service-subscription", "service-type", "servType2");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, pserver, complex);
        rules.addTreeEdge(g, pserver, pserverint1);
        rules.addTreeEdge(g, pserver, pserverint2);
        rules.addEdge(g, pnf, complex);
        rules.addTreeEdge(g, pnf, pnfint);
        rules.addEdge(g, pserverint1, plink1);
        rules.addEdge(g, pnfint, plink1);
        rules.addEdge(g, vserver, pserver);
        rules.addTreeEdge(g, vserver, tenant);
        rules.addTreeEdge(g, tenant, cloudregion);
        rules.addEdge(g, pserver, vnf1);
        rules.addEdge(g, vserver, vnf2);
        rules.addEdge(g, vnf1, vnf1image);
        rules.addEdge(g, vnf2, vnf2image);
        rules.addEdge(g, vnf1, servinst1);
        rules.addTreeEdge(g, servinst1, servsub1);
        rules.addTreeEdge(g, servsub1, customer);
        rules.addEdge(g, vnf2, servinst2);
        rules.addTreeEdge(g, servinst2, servsub2);
        rules.addTreeEdge(g, servsub2, customer);

        expectedResult.add(pnf);
        expectedResult.add(complex);
        expectedResult.add(pnfint);
        expectedResult.add(pserver);
        expectedResult.add(pserverint1);
        expectedResult.add(plink1);
        expectedResult.add(vnf1);
        expectedResult.add(vnf1image);
        expectedResult.add(vnf2);
        expectedResult.add(vnf2image);
        expectedResult.add(servinst1);
        expectedResult.add(servinst2);
        expectedResult.add(servsub1);
        expectedResult.add(servsub2);
        expectedResult.add(customer);

    }

    @Override
    protected String getQueryName() {
        return "ucpe-topology";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("hostname", "pservername");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
