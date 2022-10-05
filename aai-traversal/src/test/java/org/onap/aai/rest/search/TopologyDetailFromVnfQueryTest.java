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

public class TopologyDetailFromVnfQueryTest extends QueryTest {

    public TopologyDetailFromVnfQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        Vertex gnvf1 =
            graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type", "generic-vnf",
                "vnf-id", "vnfuuid", "vnf-name", "vnf-name-1", "service-id", "service-id-1");
        Vertex gnvf2 =
            graph.addVertex(T.label, "generic-vnf", T.id, "2", "aai-node-type", "generic-vnf",
                "vnf-id", "vnf-id-2", "vnf-name", "vnf-name-2", "service-id", "service-id-wrong");

        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver",
            "vserver-id", "vserver-id-1", "vserver-name", "vserver-name-1", "vserver-selflink",
            "vserver-selflink-1");
        Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "4", "aai-node-type", "vserver",
            "vserver-id", "vserver-id-2", "vserver-name", "vserver-name-2", "vserver-selflink",
            "vserver-selflink-2");

        Vertex vnfc1 = graph.addVertex(T.label, "vnfc", T.id, "5", "aai-node-type", "vnfc",
            "vnfc-name", "vnfc-name-1", "nfc-naming-code", "nfc-naming-code-1");
        Vertex vnfc2 = graph.addVertex(T.label, "vnfc", T.id, "6", "aai-node-type", "vnfc",
            "vnfc-name", "vnfc-name-2", "nfc-naming-code", "nfc-naming-code-2");

        Vertex tenant1 = graph.addVertex(T.label, "tenant-id", T.id, "7", "aai-node-type", "tenant",
            "tenant-id", "TenantID", "tenant-name", "TenantName");
        Vertex tenant2 = graph.addVertex(T.label, "tenant-id", T.id, "8", "aai-node-type", "tenant",
            "tenant-id", "TenantID2", "tenant-name", "TenantName2");

        Vertex flavor1 = graph.addVertex(T.label, "flavor", T.id, "9", "aai-node-type", "flavor",
            "flavor-id", "flavor-id-1", "flavor-name", "flavor-name-1", "flavor-selflink",
            "flavor-selflink-1");
        Vertex image1 = graph.addVertex(T.label, "image", T.id, "10", "aai-node-type", "image",
            "image-id", "image-id-1", "image-name", "image-name-1", "image-os-distro",
            "image-os-distro-1", "image-os-version", "image-os-version-1");

        Vertex flavor2 = graph.addVertex(T.label, "flavor", T.id, "11", "aai-node-type", "flavor",
            "flavor-id", "flavor-id-2", "flavor-name", "flavor-name-2", "flavor-selflink",
            "flavor-selflink-2");
        Vertex image2 = graph.addVertex(T.label, "image", T.id, "12", "aai-node-type", "image",
            "image-id", "image-id-2", "image-name", "image-name-2", "image-os-distro",
            "image-os-distro-2", "image-os-version", "image-os-version-2");

        Vertex linter1 =
            graph.addVertex(T.label, "l-interface", T.id, "13", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id-1", "l-interface-name", "l-interface-name1");
        Vertex linter2 =
            graph.addVertex(T.label, "l-interface", T.id, "14", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id-2", "l-interface-name", "l-interface-name2");

        Vertex l3inter1ipv4addresslist = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "15", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address-1");
        Vertex l3inter1ipv4addresslist2 = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "16", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address-2");

        Vertex l3inter1ipv6addresslist = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "17", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address-1");
        Vertex l3inter1ipv6addresslist2 = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "18", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address-2");

        Vertex cloudRegion1 = graph.addVertex(T.label, "cloud-region", T.id, "19", "aai-node-type",
            "cloud-region", "cloud-owner", "CloudOwner1", "cloud-region-id", "CloudRegionId1");
        Vertex cloudRegion2 = graph.addVertex(T.label, "cloud-region", T.id, "20", "aai-node-type",
            "cloud-region", "cloud-owner", "CloudOwner2", "cloud-region-id", "CloudRegionId2");

        Vertex availibityzone1 = graph.addVertex(T.label, "availability-zone", T.id, "21",
            "aai-node-type", "availability-zone", "availability-zone-name", "az-name-1",
            "hypervisor-type", "hypervisortype-1");

        Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "22", "aai-node-type",
            "pserver", "pserver-id", "PserverID1", "hostname", "PserverHostName1");
        Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "23", "aai-node-type",
            "pserver", "pserver-id", "PserverID2", "hostname", "PserverHostName2"); // false

        Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "24",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1",
            "service-instance-name", "service-instance-name-1");
        Vertex serviceinstance2 = graph.addVertex(T.label, "service-instance", T.id, "25",
            "aai-node-type", "service-instance", "service-instance-id", "servInstId-2",
            "service-type", "servType-2", "service-role", "servRole-2");

        Vertex vfmodule1 = graph.addVertex(T.label, "vf-module", T.id, "26", "aai-node-type",
            "vf-module", "vf-module-id", "vf-module-id-1", "vf-module-name", "vf-module-name1");
        Vertex vfmodule2 = graph.addVertex(T.label, "vf-module", T.id, "27", "aai-node-type",
            "vf-module", "vf-module-id", "vf-module-id-2", "vf-module-name", "vf-module-name2");

        Vertex volumegroup1 =
            graph.addVertex(T.label, "volume-group", T.id, "28", "aai-node-type", "volume-group",
                "volume-group-id", "volume-group-id-1", "volume-group-name", "volume-group-name1");
        Vertex volumegroup2 =
            graph.addVertex(T.label, "volume-group", T.id, "29", "aai-node-type", "volume-group",
                "volume-group-id", "volume-group-id-2", "volume-group-name", "volume-group-name2");

        Vertex complex =
            graph.addVertex(T.label, "complex", T.id, "30", "aai-node-type", "complex");

        GraphTraversalSource g = graph.traversal();// true

        rules.addEdge(g, gnvf1, vserver1);// true

        rules.addEdge(g, vserver1, vnfc1);// true
        rules.addEdge(g, vserver2, vnfc1);// true

        rules.addTreeEdge(g, vserver1, tenant1);// true
        rules.addTreeEdge(g, tenant1, cloudRegion1);// true
        rules.addTreeEdge(g, cloudRegion1, availibityzone1);// true
        rules.addEdge(g, vserver1, image1);// true
        rules.addEdge(g, vserver1, flavor1);// true
        rules.addEdge(g, vserver1, pserver1);// true
        rules.addEdge(g, vserver2, image1);
        rules.addEdge(g, vserver2, flavor1);
        rules.addEdge(g, pserver1, complex);// true

        rules.addTreeEdge(g, vserver1, linter1);// true
        rules.addTreeEdge(g, linter1, l3inter1ipv4addresslist);// true
        rules.addTreeEdge(g, linter1, l3inter1ipv6addresslist);// true

        rules.addEdge(g, gnvf1, serviceinstance1);// true
        rules.addTreeEdge(g, gnvf1, vfmodule1);// true
        rules.addEdge(g, vfmodule1, volumegroup1);

        // ---------------------------------------------------

        // rules.addTreeEdge(g, vserver1, tenant2);//false
        // rules.addTreeEdge(g, vserver2, linter1);//false
        // rules.addEdge(g, vserver1, vnfc2);//false

        expectedResult.add(gnvf1);
        expectedResult.add(vnfc1);
        expectedResult.add(vserver1);
        expectedResult.add(tenant1);
        expectedResult.add(cloudRegion1);
        expectedResult.add(image1);
        expectedResult.add(flavor1);
        expectedResult.add(pserver1);
        expectedResult.add(complex);

        expectedResult.add(serviceinstance1);
        expectedResult.add(availibityzone1);
        expectedResult.add(volumegroup1);
        expectedResult.add(linter1);
        expectedResult.add(l3inter1ipv4addresslist);
        expectedResult.add(l3inter1ipv6addresslist);

    }

    @Override
    protected String getQueryName() {
        return "topology-detail-fromVnf";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("vnf-id", "vnfuuid");

    }

    @Override
    protected void addParam(Map<String, Object> params) {
        // return;
        params.put("serviceId", "service-id-1");
    }

}
