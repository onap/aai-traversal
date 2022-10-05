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

public class TopologyFromCloudRegionIdAndServiceIdTest extends QueryTest {
    public TopologyFromCloudRegionIdAndServiceIdTest()
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
        Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "61",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1",
            "service-instance-name", "service-instance-name-1");
        Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "1", "aai-node-type",
            "cloud-region", "cloud-region-id", "cloud-region-id-1", "cloud-owner", "cloud-owner-1");
        Vertex availibityzone1 = graph.addVertex(T.label, "cloud-region", T.id, "2",
            "aai-node-type", "availability-zone", "availability-zone-name", "az-name-1",
            "hypervisor-type", "hypervisortype-1");
        Vertex gnvf1 =
            graph.addVertex(T.label, "generic-vnf", T.id, "3", "aai-node-type", "generic-vnf",
                "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1", "service-id", "service-id-1");

        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "7", "aai-node-type", "vserver",
            "vserver-id", "vserver-id-1", "vserver-name", "vserver-name-1", "vserver-selflink",
            "vserver-selflink-1");
        Vertex flavor1 = graph.addVertex(T.label, "flavor", T.id, "8", "aai-node-type", "flavor",
            "flavor-id", "flavor-id-1", "flavor-name", "flavor-name-1", "flavor-selflink",
            "flavor-selflink-1");
        Vertex image1 = graph.addVertex(T.label, "image", T.id, "9", "aai-node-type", "image",
            "image-id", "image-id-1", "image-name", "image-name-1", "image-os-distro",
            "image-os-distro-1", "image-os-version", "image-os-version-1");
        Vertex volume1 = graph.addVertex(T.label, "volume", T.id, "10", "aai-node-type", "volume",
            "volume-id", "volume-id-1", "volume-selflink", "volume-selflink-1");
        Vertex vnfc1 = graph.addVertex(T.label, "vnfc", T.id, "11", "aai-node-type", "vnfc",
            "vnfc-name", "vnfc-name-1", "nfc-naming-code", "nfc-naming-code-1");
        Vertex snapshot1 = graph.addVertex(T.label, "snapshot", T.id, "12", "aai-node-type",
            "snapshot", "snapshot-id", "snapshot-id-1");
        Vertex vfmodule1 = graph.addVertex(T.label, "vf-module", T.id, "13", "aai-node-type",
            "vf-module", "vf-module-id", "vf-module-id-1", "vf-module-name", "vf-module-name1");
        Vertex linter1 =
            graph.addVertex(T.label, "l-interface", T.id, "14", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id-1", "l-interface-name", "l-interface-name1");
        Vertex logicallink1 = graph.addVertex(T.label, "logical-link", T.id, "15", "aai-node-type",
            "logical-link", "link-name", "link-name-1", "l-interface-name", "l-interface-name1");
        Vertex l3inter1ipv4addresslist = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "16", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address-1");
        Vertex subnet14 = graph.addVertex(T.label, "subnet", T.id, "17", "aai-node-type", "subnet",
            "subnet-id", "subnet-id-14");
        Vertex l3network14 = graph.addVertex(T.label, "l3-network", T.id, "18", "aai-node-type",
            "l3-network", "network-id", "network-id-14", "network-name", "network-name-14");
        Vertex l3inter1ipv6addresslist = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "19", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address-1");
        Vertex subnet16 = graph.addVertex(T.label, "subnet", T.id, "20", "aai-node-type", "subnet",
            "subnet-id", "subnet-id-16");
        Vertex l3network16 = graph.addVertex(T.label, "l3-network", T.id, "21", "aai-node-type",
            "l3-network", "network-id", "network-id-16", "network-name", "network-name16");

        Vertex gnvf2 =
            graph.addVertex(T.label, "generic-vnf", T.id, "33", "aai-node-type", "generic-vnf",
                "vnf-id", "vnf-id-2", "vnf-name", "vnf-name-2", "service-id", "service-id-wrong");
        Vertex serviceinstance2 = graph.addVertex(T.label, "service-instance", T.id, "36",
            "aai-node-type", "service-instance", "service-instance-id", "servInstId-2",
            "service-type", "servType-2", "service-role", "servRole-2");
        Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "37", "aai-node-type",
            "vserver", "vserver-id", "vserver-id-2", "vserver-name", "vserver-name-2",
            "vserver-selflink", "vserver-selflink-2");
        Vertex flavor2 = graph.addVertex(T.label, "flavor", T.id, "38", "aai-node-type", "flavor",
            "flavor-id", "flavor-id-2", "flavor-name", "flavor-name-2", "flavor-selflink",
            "flavor-selflink-2");
        Vertex image2 = graph.addVertex(T.label, "image", T.id, "39", "aai-node-type", "image",
            "image-id", "image-id-2", "image-name", "image-name-2", "image-os-distro",
            "image-os-distro-2", "image-os-version", "image-os-version-2");
        Vertex volume2 = graph.addVertex(T.label, "volume", T.id, "40", "aai-node-type", "volume",
            "volume-id", "volume-id-2", "volume-selflink", "volume-selflink-2");
        Vertex vnfc2 = graph.addVertex(T.label, "vnfc", T.id, "41", "aai-node-type", "vnfc",
            "vnfc-name", "vnfc-name-2", "nfc-naming-code", "nfc-naming-code-2");
        Vertex snapshot2 = graph.addVertex(T.label, "snapshot", T.id, "42", "aai-node-type",
            "snapshot", "snapshot-id", "snapshot-id-2");
        Vertex vfmodule2 = graph.addVertex(T.label, "vf-module", T.id, "43", "aai-node-type",
            "vf-module", "vf-module-id", "vf-module-id-2", "vf-module-name", "vf-module-name2");
        Vertex linter2 =
            graph.addVertex(T.label, "l-interface", T.id, "44", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id-2", "l-interface-name", "l-interface-name2");
        Vertex logicallink2 = graph.addVertex(T.label, "logical-link", T.id, "45", "aai-node-type",
            "logical-link", "link-name", "link-name-2", "l-interface-name", "l-interface-name2");
        Vertex l3inter1ipv4addresslist2 = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "46", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address-2");
        Vertex subnet24 = graph.addVertex(T.label, "subnet", T.id, "47", "aai-node-type", "subnet",
            "subnet-id", "subnet-id-24");
        Vertex l3network24 = graph.addVertex(T.label, "l3-network", T.id, "48", "aai-node-type",
            "l3-network", "network-id", "network-id-24", "network-name", "network-name-24");
        Vertex l3inter1ipv6addresslist2 = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "49", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address-2");
        Vertex subnet26 = graph.addVertex(T.label, "subnet", T.id, "50", "aai-node-type", "subnet",
            "subnet-id", "subnet-id-26");
        Vertex l3network26 = graph.addVertex(T.label, "l3-network", T.id, "51", "aai-node-type",
            "l3-network", "network-id", "network-id-26", "network-name", "network-name26");

        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, cloudregion1, availibityzone1);// true
        rules.addEdge(g, availibityzone1, gnvf1);// true
        rules.addEdge(g, gnvf1, serviceinstance1);// true
        rules.addEdge(g, gnvf1, vserver1);// true

        rules.addEdge(g, vserver1, flavor1);// true
        rules.addEdge(g, vserver1, image1);// true
        rules.addTreeEdge(g, vserver1, volume1);// true
        rules.addEdge(g, vserver1, vnfc1);// true
        rules.addEdge(g, vserver1, snapshot1);// true
        rules.addEdge(g, vserver1, vfmodule1);// true
        rules.addEdge(g, linter1, logicallink1);// true
        rules.addTreeEdge(g, vserver1, linter1);// true
        rules.addTreeEdge(g, linter1, l3inter1ipv4addresslist);// true
        rules.addTreeEdge(g, linter1, l3inter1ipv6addresslist);// true
        rules.addEdge(g, l3inter1ipv4addresslist, subnet14);// true
        rules.addTreeEdge(g, subnet14, l3network14);// true
        rules.addEdge(g, l3inter1ipv6addresslist, subnet16);// true
        rules.addTreeEdge(g, subnet16, l3network16);// true

        // false
        rules.addEdge(g, availibityzone1, gnvf2);
        rules.addEdge(g, gnvf2, serviceinstance2);
        rules.addEdge(g, gnvf2, vserver2);
        rules.addEdge(g, vserver2, flavor2);
        rules.addEdge(g, vserver2, image2);
        rules.addTreeEdge(g, vserver2, volume2);
        rules.addEdge(g, vserver2, vnfc2);
        rules.addEdge(g, vserver2, snapshot2);
        rules.addEdge(g, vserver2, vfmodule2);
        rules.addEdge(g, linter2, logicallink2);
        rules.addTreeEdge(g, vserver2, linter2);
        rules.addTreeEdge(g, linter2, l3inter1ipv4addresslist2);
        rules.addTreeEdge(g, linter2, l3inter1ipv6addresslist2);
        rules.addEdge(g, l3inter1ipv4addresslist2, subnet24);
        rules.addTreeEdge(g, subnet24, l3network24);
        rules.addEdge(g, l3inter1ipv6addresslist2, subnet26);
        rules.addTreeEdge(g, subnet26, l3network26);

        expectedResult.add(serviceinstance1);
        expectedResult.add(vserver1);
        expectedResult.add(flavor1);
        expectedResult.add(image1);
        expectedResult.add(volume1);
        expectedResult.add(vnfc1);
        expectedResult.add(snapshot1);
        expectedResult.add(vfmodule1);
        expectedResult.add(linter1);
        expectedResult.add(logicallink1);
        expectedResult.add(l3inter1ipv4addresslist);
        expectedResult.add(subnet14);
        expectedResult.add(l3network14);
        expectedResult.add(l3inter1ipv6addresslist);
        expectedResult.add(subnet16);
        expectedResult.add(l3network16);

    }

    @Override
    protected String getQueryName() {
        return "topology-fromCloudRegionIdandServiceId";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "cloud-region").has("cloud-owner", "cloud-owner-1")
            .has("cloud-region-id", "cloud-region-id-1");

    }

    @Override
    protected void addParam(Map<String, Object> params) {
        params.put("serviceId", "service-id-1");
    }
}
