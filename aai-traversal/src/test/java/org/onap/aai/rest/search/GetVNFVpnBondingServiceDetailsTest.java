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
import org.junit.Ignore;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class GetVNFVpnBondingServiceDetailsTest extends QueryTest {

    public GetVNFVpnBondingServiceDetailsTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    // Test ignored for time being as query files pulled from aai-queries in schema-service differ
    // from the stored-queries.json file that used to reside in traversal.
    @Ignore
    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph

        Vertex gnvf1 = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "1", "aai-node-type", "vserver",
            "vserver-name1", "vservername1");
        Vertex linter1 =
            graph.addVertex(T.label, "l-interface", T.id, "2", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id-1", "l-interface-name", "l-interface-name1");
        Vertex linter2 =
            graph.addVertex(T.label, "l-interface", T.id, "3", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id-2", "l-interface-name", "l-interface-name2");
        Vertex vlan1 = graph.addVertex(T.label, "vlan", T.id, "4", "aai-node-type", "vlan",
            "vlan-interface", "vlan11");
        Vertex config1 = graph.addVertex(T.label, "configuration", T.id, "5", "aai-node-type",
            "configuration", "configuration-id", "configuration1");
        Vertex l3network1 =
            graph.addVertex(T.label, "l3-network", T.id, "6", "aai-node-type", "l3-network",
                "ll3-network-id", "l3-network-id-1", "l3-network-name", "l3-network-name1");
        Vertex l3inter1ipv4addresslist = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "7", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address-list-id", "l3-interface-ipv4-address-list-id-1",
            "l3-interface-ipv6-address-list-name", "l3-interface-ipv6-address-list-name1");
        Vertex l3inter1ipv6addresslist = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "8", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address-list-id", "l3-interface-ipv6-address-list-id-1",
            "l3-interface-ipv6-address-list-name", "l3-interface-ipv6-address-list-name1");
        Vertex configVpnBinding = graph.addVertex(T.label, "vpn-binding", T.id, "9",
            "aai-node-type", "vpn-binding", "vpn-id", "test-binding-config", "vpn-name", "test");
        Vertex customer = graph.addVertex(T.label, "customer", T.id, "10", "aai-node-type",
            "customer", "customer-id", "customer-id-1", "customer-name", "customer-name1");
        Vertex subnet1 = graph.addVertex(T.label, "subnet", T.id, "11", "aai-node-type", "subnet",
            "subnet-id", "subnet-id-11");
        Vertex routeTarget1 = graph.addVertex(T.label, "route-target", T.id, "12", "aai-node-type",
            "route-target", "global-route-target", "111");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, gnvf1, vserver);// false
        rules.addEdge(g, gnvf1, config1);// false
        rules.addTreeEdge(g, vserver, linter1);// true
        rules.addTreeEdge(g, linter1, linter2);// true
        rules.addTreeEdge(g, linter2, vlan1);// true
        rules.addEdge(g, config1, l3network1);// false
        rules.addTreeEdge(g, l3network1, subnet1);// true
        rules.addEdge(g, subnet1, l3inter1ipv4addresslist);// false
        rules.addEdge(g, subnet1, l3inter1ipv6addresslist);// false
        rules.addEdge(g, l3network1, configVpnBinding);// false
        rules.addEdge(g, configVpnBinding, customer);// false
        rules.addTreeEdge(g, configVpnBinding, routeTarget1);// true

        expectedResult.add(vserver);
        expectedResult.add(config1);
        expectedResult.add(linter1);
        expectedResult.add(linter2);
        expectedResult.add(vlan1);
        expectedResult.add(l3network1);
        expectedResult.add(subnet1);
        expectedResult.add(l3inter1ipv4addresslist);
        expectedResult.add(l3inter1ipv6addresslist);
        expectedResult.add(configVpnBinding);
        expectedResult.add(customer);
        expectedResult.add(routeTarget1);

    }

    @Override
    protected String getQueryName() {
        return "getVNFVpnBondingServiceDetails";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("vnf-name", "vnf-name-1");

    }

    @Override
    protected void addParam(Map<String, Object> params) {
        params.put("vnf-name", "vnf-name-1");
    }
}
