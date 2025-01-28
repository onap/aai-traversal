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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyFromSubscriberNameAndServiceTypeTest extends QueryTest {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(TopologyFromSubscriberNameAndServiceTypeTest.class);

    public TopologyFromSubscriberNameAndServiceTypeTest()
        throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex customer1 = graph.addVertex(T.label, "customer", T.id, "1", "aai-node-type",
            "customer", "global-customer-id", "global-customer-id-1", "subscriber-name",
            "subscriber-name-1");
        // Customer has 2 subscriptions - we filter by subscription-type
        Vertex servicesubscription1 = graph.addVertex(T.label, "service-subscription", T.id, "2",
            "aai-node-type", "service-subscription", "service-type", "service-type-1");
        Vertex servicesubscription112 = graph.addVertex(T.label, "service-subscription", T.id,
            "112", "aai-node-type", "service-subscription", "service-type", "service-type-112");

        Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "3",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1",
            "service-instance-name", "service-instance-name-1");
        Vertex serviceinstance113 = graph.addVertex(T.label, "service-instance", T.id, "113",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-113",
            "service-instance-name", "service-instance-name-113");

        Vertex gnvf1 =
            graph.addVertex(T.label, "generic-vnf", T.id, "4", "aai-node-type", "generic-vnf",
                "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1", "service-id", "service-id-1");
        Vertex gnvf114 =
            graph.addVertex(T.label, "generic-vnf", T.id, "114", "aai-node-type", "generic-vnf",
                "vnf-id", "vnf-id-114", "vnf-name", "vnf-name-2", "service-id", "service-id-2");

        Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "5", "aai-node-type", "pserver",
            "hostname", "pservername1");
        Vertex pserver115 = graph.addVertex(T.label, "pserver", T.id, "115", "aai-node-type",
            "pserver", "hostname", "pservername115");

        Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "6", "aai-node-type", "complex",
            "physical-location-id", "physical-location-id-1", "country", "US");

        Vertex pnfint1 = graph.addVertex(T.label, "p-interface", T.id, "7", "aai-node-type",
            "p-interface", "interface-name", "ge0/0/0");

        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "8", "aai-node-type", "vserver",
            "vserver-id", "vserver-id-1", "vserver-name", "vserver-name-1", "vserver-selflink",
            "vserver-selflink-1");

        Vertex vserver116 = graph.addVertex(T.label, "vserver", T.id, "116", "aai-node-type",
            "vserver", "vserver-id", "vserver-id-116", "vserver-name", "vserver-name-116",
            "vserver-selflink", "vserver-selflink-116");

        Vertex flavor1 = graph.addVertex(T.label, "flavor", T.id, "9", "aai-node-type", "flavor",
            "flavor-id", "flavor-id-1", "flavor-name", "flavor-name-1", "flavor-selflink",
            "flavor-selflink-1");
        Vertex image1 = graph.addVertex(T.label, "image", T.id, "10", "aai-node-type", "image",
            "image-id", "image-id-1", "image-name", "image-name-1", "image-os-distro",
            "image-os-distro-1", "image-os-version", "image-os-version-1");

        Vertex linter1 =
            graph.addVertex(T.label, "l-interface", T.id, "11", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id-1", "l-interface-name", "l-interface-name1");
        Vertex logicallink1 = graph.addVertex(T.label, "logical-link", T.id, "12", "aai-node-type",
            "logical-link", "link-name", "link-name-1", "l-interface-name", "l-interface-name1");
        Vertex l3inter1ipv4addresslist = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "13", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address-1");
        Vertex subnet1 = graph.addVertex(T.label, "subnet", T.id, "14", "aai-node-type", "subnet",
            "subnet-id", "subnet-id-1");
        Vertex l3network1 = graph.addVertex(T.label, "l3-network", T.id, "15", "aai-node-type",
            "l3-network", "network-id", "network-id-1", "network-name", "network-name-1");
        Vertex l3inter1ipv6addresslist = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "16", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address-1");
        Vertex subnet2 = graph.addVertex(T.label, "subnet", T.id, "17", "aai-node-type", "subnet",
            "subnet-id", "subnet-id-2");
        Vertex l3network2 = graph.addVertex(T.label, "l3-network", T.id, "18", "aai-node-type",
            "l3-network", "network-id", "network-id-2", "network-name", "network-name2");

        Vertex customer2 = graph.addVertex(T.label, "customer", T.id, "19", "aai-node-type",
            "customer", "global-customer-id", "global-customer-id-2", "subscriber-name",
            "subscriber-name-2");
        Vertex servicesubscription2 = graph.addVertex(T.label, "service-subscription", T.id, "20",
            "aai-node-type", "service-subscription", "service-type", "service-type-2");
        Vertex serviceinstance2 = graph.addVertex(T.label, "service-instance", T.id, "21",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-2",
            "service-instance-name", "service-instance-name-2");
        Vertex gnvf2 =
            graph.addVertex(T.label, "generic-vnf", T.id, "22", "aai-node-type", "generic-vnf",
                "vnf-id", "vnf-id-2", "vnf-name", "vnf-name-2", "service-id", "service-id-2");

        Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "23", "aai-node-type",
            "pserver", "hostname", "pservername1");
        Vertex complex2 = graph.addVertex(T.label, "complex", T.id, "24", "aai-node-type",
            "complex", "physical-location-id", "physical-location-id-2", "country", "US");
        Vertex pnfint2 = graph.addVertex(T.label, "p-interface", T.id, "25", "aai-node-type",
            "p-interface", "interface-name", "ge0/0/0");

        Vertex plink1 = graph.addVertex(T.label, "physical-link", T.id, "251", "aai-node-type",
            "physical-link", "link-name", "ge0/0/0-to-xe0/0/0");

        Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "26", "aai-node-type",
            "vserver", "vserver-id", "vserver-id-2", "vserver-name", "vserver-name-2",
            "vserver-selflink", "vserver-selflink-2");
        Vertex flavor2 = graph.addVertex(T.label, "flavor", T.id, "27", "aai-node-type", "flavor",
            "flavor-id", "flavor-id-2", "flavor-name", "flavor-name-2", "flavor-selflink",
            "flavor-selflink-2");
        Vertex image2 = graph.addVertex(T.label, "image", T.id, "28", "aai-node-type", "image",
            "image-id", "image-id-2", "image-name", "image-name-2", "image-os-distro",
            "image-os-distro-2", "image-os-version", "image-os-version-2");

        Vertex linter2 =
            graph.addVertex(T.label, "l-interface", T.id, "29", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id-2", "l-interface-name", "l-interface-name2");
        Vertex logicallink2 = graph.addVertex(T.label, "logical-link", T.id, "30", "aai-node-type",
            "logical-link", "link-name", "link-name-2", "l-interface-name", "l-interface-name2");
        Vertex l3inter1ipv4addresslist2 = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "31", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address-2");
        Vertex subnet3 = graph.addVertex(T.label, "subnet", T.id, "32", "aai-node-type", "subnet",
            "subnet-id", "subnet-id-3");
        Vertex l3network3 = graph.addVertex(T.label, "l3-network", T.id, "33", "aai-node-type",
            "l3-network", "network-id", "network-id-3", "network-name", "network-name-3");
        Vertex l3inter1ipv6addresslist2 = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "34", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address-2");
        Vertex subnet4 = graph.addVertex(T.label, "subnet", T.id, "35", "aai-node-type", "subnet",
            "subnet-id", "subnet-id-4");
        Vertex l3network4 = graph.addVertex(T.label, "l3-network", T.id, "36", "aai-node-type",
            "l3-network", "network-id", "network-id-4", "network-name", "network-name4");

        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, customer1, servicesubscription1);// true

        rules.addTreeEdge(g, customer1, servicesubscription112);// true

        rules.addTreeEdge(g, servicesubscription1, serviceinstance1);// true
        rules.addTreeEdge(g, servicesubscription1, serviceinstance113);// true

        rules.addEdge(g, serviceinstance1, gnvf1);// true
        rules.addEdge(g, serviceinstance113, gnvf114);// true

        rules.addEdge(g, gnvf1, vserver1);// true
        rules.addEdge(g, gnvf114, vserver116);// true

        rules.addEdge(g, gnvf1, pserver1);// true
        rules.addEdge(g, gnvf114, pserver115);// true

        rules.addEdge(g, pserver1, complex1);// true
        rules.addTreeEdge(g, pserver1, pnfint1);
        rules.addEdge(g, pnfint1, plink1);

        rules.addEdge(g, vserver1, flavor1);// true
        rules.addEdge(g, vserver1, image1);// true
        rules.addEdge(g, vserver1, pserver1);// true
        rules.addEdge(g, linter1, logicallink1);// true
        rules.addTreeEdge(g, vserver1, linter1);// true
        rules.addTreeEdge(g, linter1, l3inter1ipv4addresslist);// true
        rules.addTreeEdge(g, linter1, l3inter1ipv6addresslist);// true
        rules.addEdge(g, l3inter1ipv4addresslist, subnet1);// true
        rules.addTreeEdge(g, subnet1, l3network1);// true
        rules.addEdge(g, l3inter1ipv6addresslist, subnet2);// true
        rules.addTreeEdge(g, subnet2, l3network2);// true
        // false

        rules.addTreeEdge(g, customer2, servicesubscription2);// true
        rules.addTreeEdge(g, servicesubscription2, serviceinstance2);// true
        rules.addEdge(g, serviceinstance2, gnvf2);// true
        rules.addEdge(g, gnvf2, pserver2);// true
        rules.addEdge(g, pserver2, complex2);// true
        rules.addTreeEdge(g, pserver2, pnfint2);// true

        rules.addEdge(g, gnvf2, vserver2);// true

        rules.addEdge(g, vserver2, flavor2);// true
        rules.addEdge(g, vserver2, image2);// true
        rules.addEdge(g, linter2, logicallink2);// true
        rules.addTreeEdge(g, vserver2, linter2);// true
        rules.addTreeEdge(g, linter2, l3inter1ipv4addresslist2);// true
        rules.addTreeEdge(g, linter2, l3inter1ipv6addresslist2);// true
        rules.addEdge(g, l3inter1ipv4addresslist2, subnet3);// true
        rules.addTreeEdge(g, subnet3, l3network3);// true
        rules.addEdge(g, l3inter1ipv6addresslist2, subnet4);// true
        rules.addTreeEdge(g, subnet4, l3network4);// true

        expectedResult.add(complex1);
        expectedResult.add(vserver1);
        expectedResult.add(vserver116);
        expectedResult.add(pserver1);
        expectedResult.add(flavor1);
        expectedResult.add(image1);
        expectedResult.add(linter1);
        expectedResult.add(logicallink1);
        expectedResult.add(plink1);
        expectedResult.add(l3inter1ipv4addresslist);
        expectedResult.add(subnet1);
        expectedResult.add(l3network1);
        expectedResult.add(l3inter1ipv6addresslist);
        expectedResult.add(subnet2);
        expectedResult.add(l3network2);

    }

    @Override
    protected String getQueryName() {
        return "spaas-topology-fromServiceInstance";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "customer").has("global-customer-id", "global-customer-id-1")
            .in("org.onap.relationships.inventory.BelongsTo")
            .has("aai-node-type", "service-subscription").has("service-type", "service-type-1")
            .in("org.onap.relationships.inventory.BelongsTo")
            .has("aai-node-type", "service-instance");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        // params.put("serviceType", "service-type-1");
        // params.put("subscriberName", "subscriber-name-1");
    }
}
