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

public class PendingTopologyDetailTest extends QueryTest {
    public PendingTopologyDetailTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph

        Vertex genericvnf = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-0", "vnf-name", "vnf-name-0");
        Vertex platform = graph.addVertex(T.label, "platform", T.id, "2", "aai-node-type",
            "platform", "platform-name", "platform0");
        Vertex lineofbusiness = graph.addVertex(T.label, "line-of-business", T.id, "3",
            "aai-node-type", "line-of-business", "line-of-business-name", "business0");
        Vertex servinst = graph.addVertex(T.label, "service-instance", T.id, "4", "aai-node-type",
            "service-instance", "service-instance-id", "servInstId0", "service-type", "servType0");
        Vertex owningentity = graph.addVertex(T.label, "owning-entity", T.id, "5", "aai-node-type",
            "owning-entity", "owning-entity-id", "entityId0", "owning-entity-name", "entityName0");
        Vertex project = graph.addVertex(T.label, "project", T.id, "6", "aai-node-type", "project",
            "project-name", "project0");
        Vertex vfmodule = graph.addVertex(T.label, "vf-module", T.id, "38", "aai-node-type",
            "vf-module", "vf-module-id", "1");
        Vertex vnfc = graph.addVertex(T.label, "vnfc", T.id, "7", "aai-node-type", "vnfc",
            "vnfc-name", "vnfc0", "nfc-naming-code", "namingCode0", "nfc-function", "function0");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "8", "aai-node-type", "vserver",
            "vserver-id", "vserverid0");
        Vertex linterface =
            graph.addVertex(T.label, "l-interface", T.id, "9", "aai-node-type", "l-interface",
                "l-interface-id", "l-interface-id0", "l-interface-name", "l-interface-name0");
        Vertex l3inter1ipv4addresslist = graph.addVertex(T.label, "interface-ipv4-address-list",
            T.id, "10", "aai-node-type", "l3-interface-ipv4-address-list",
            "l3-interface-ipv4-address", "l3-interface-ipv4-address0");
        Vertex subnet4 = graph.addVertex(T.label, "subnet", T.id, "11", "aai-node-type", "subnet",
            "subnet-id", "subnet4-id0");
        Vertex l3network4 = graph.addVertex(T.label, "l3-network", T.id, "12", "aai-node-type",
            "l3-network", "network-id", "network4-id0", "network-name", "network4-name0");
        Vertex l3inter1ipv6addresslist = graph.addVertex(T.label, "l3-interface-ipv6-address-list",
            T.id, "13", "aai-node-type", "l3-interface-ipv6-address-list",
            "l3-interface-ipv6-address", "l3-interface-ipv6-address0");
        Vertex subnet6 = graph.addVertex(T.label, "subnet", T.id, "14", "aai-node-type", "subnet",
            "subnet-id", "subnet6-id0");
        Vertex l3network6 = graph.addVertex(T.label, "l3-network", T.id, "15", "aai-node-type",
            "l3-network", "network-id", "network6-id0", "network-name", "network6-name0");
        Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "16", "aai-node-type", "tenant",
            "tenant-id", "tenantid0", "tenant-name", "tenantName0");
        Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "17", "aai-node-type",
            "cloud-region", "cloud-region-id", "regionid0", "cloud-owner", "cloudOwnername0");
        Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "18", "aai-node-type", "pserver",
            "hostname", "pservername1");
        Vertex complex = graph.addVertex(T.label, "pserver", T.id, "19", "aai-node-type", "complex",
            "physical-location-id", "locationId", "physical-location-type", "locationType",
            "physical-location-id", "locationId", "city", "cityName", "state", "stateName",
            "postal-code", "zip", "country", "countryName");
        Vertex vipipv4addresslist = graph.addVertex(T.label, "vip-ipv4-address-list", T.id, "20",
            "aai-node-type", "vip-ipv4-address-list", "vip-ipv4-address", "vip-ipv4-address0");
        Vertex vipipv6addresslist = graph.addVertex(T.label, "vip-ipv6-address-list", T.id, "21",
            "aai-node-type", "vip-ipv6-address-list", "vip-ipv6-address", "vip-ipv6-address0");

        Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "40", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1");
        Vertex platform1 = graph.addVertex(T.label, "platform", T.id, "41", "aai-node-type",
            "platform", "platform-name", "platform1");

        Vertex vfmodule2 = graph.addVertex(T.label, "vf-module", T.id, "39", "aai-node-type",
            "vf-module", "vf-module-id", "2");
        Vertex genericvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "30", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-2", "vnf-name", "vnf-name2");
        Vertex vnfc2 = graph.addVertex(T.label, "vnfc", T.id, "31", "aai-node-type", "vnfc",
            "vnfc-name", "vnfc2", "nfc-naming-code", "namingCode2", "nfc-function", "function2");
        Vertex vipipv4addresslist2 = graph.addVertex(T.label, "vip-ipv4-address-list", T.id, "32",
            "aai-node-type", "vip-ipv4-address-list", "vip-ipv4-addres", "vip-ipv4-address2");
        Vertex vipipv6addresslist2 = graph.addVertex(T.label, "vip-ipv6-address-list", T.id, "33",
            "aai-node-type", "vip-ipv6-address-list", "vip-ipv6-address", "vip-ipv6-address2");
        Vertex subnet42 = graph.addVertex(T.label, "subnet", T.id, "34", "aai-node-type", "subnet",
            "subnet-id", "subnet4-id-0");
        Vertex l3network42 = graph.addVertex(T.label, "l3-network", T.id, "35", "aai-node-type",
            "l3-network", "network-id", "network4-id2", "network-name", "network4-name2");
        Vertex subnet62 = graph.addVertex(T.label, "subnet", T.id, "36", "aai-node-type", "subnet",
            "subnet-id", "subnet6-id2");
        Vertex l3network62 = graph.addVertex(T.label, "l3-network", T.id, "37", "aai-node-type",
            "l3-network", "network-id", "network6-id2", "network-name", "network6-name2");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, genericvnf, platform);
        rules.addEdge(g, genericvnf, lineofbusiness);
        rules.addEdge(g, genericvnf, servinst);
        rules.addEdge(g, owningentity, servinst);
        rules.addEdge(g, project, servinst);
        rules.addTreeEdge(g, genericvnf, vfmodule);
        rules.addEdge(g, vfmodule, vnfc);
        // rules.addEdge(g, genericvnf, vnfc);
        rules.addEdge(g, vnfc, vserver);
        rules.addTreeEdge(g, vserver, tenant);
        rules.addTreeEdge(g, tenant, cloudregion);
        rules.addEdge(g, pserver, vserver);
        rules.addEdge(g, complex, pserver);
        rules.addTreeEdge(g, linterface, vserver);
        rules.addTreeEdge(g, l3inter1ipv4addresslist, linterface);
        rules.addEdge(g, l3inter1ipv4addresslist, subnet4);
        rules.addTreeEdge(g, l3network4, subnet4);
        rules.addTreeEdge(g, vnfc, l3inter1ipv4addresslist);
        rules.addTreeEdge(g, l3inter1ipv6addresslist, linterface);
        rules.addEdge(g, l3inter1ipv6addresslist, subnet6);
        rules.addTreeEdge(g, l3network6, subnet6);
        rules.addTreeEdge(g, vnfc, l3inter1ipv6addresslist);
        rules.addTreeEdge(g, vipipv4addresslist, cloudregion);
        rules.addEdge(g, vipipv4addresslist, subnet4);
        rules.addEdge(g, vnfc, vipipv4addresslist);
        rules.addTreeEdge(g, vipipv6addresslist, cloudregion);
        rules.addEdge(g, vipipv6addresslist, subnet6);
        rules.addEdge(g, vnfc, vipipv6addresslist);

        rules.addEdge(g, genericvnf, platform1);

        // false
        rules.addTreeEdge(g, genericvnf2, vfmodule2);
        rules.addEdge(g, vfmodule2, vnfc2);
        rules.addTreeEdge(g, vipipv4addresslist2, cloudregion);
        rules.addTreeEdge(g, vipipv6addresslist2, cloudregion);
        rules.addEdge(g, vnfc2, vipipv4addresslist2);
        rules.addEdge(g, vnfc2, vipipv6addresslist2);
        rules.addEdge(g, vipipv4addresslist2, subnet42);
        rules.addEdge(g, vipipv6addresslist2, subnet62);
        rules.addTreeEdge(g, l3network42, subnet42);
        rules.addTreeEdge(g, l3network62, subnet62);

        rules.addEdge(g, genericvnf1, lineofbusiness);

        expectedResult.add(genericvnf);
        expectedResult.add(platform);
        expectedResult.add(lineofbusiness);
        expectedResult.add(owningentity);
        expectedResult.add(project);
        expectedResult.add(vfmodule);
        expectedResult.add(vnfc);
        expectedResult.add(l3inter1ipv4addresslist);
        expectedResult.add(subnet4);
        expectedResult.add(l3network4);
        expectedResult.add(l3inter1ipv6addresslist);
        expectedResult.add(subnet6);
        expectedResult.add(l3network6);
        expectedResult.add(vipipv4addresslist);
        expectedResult.add(vipipv6addresslist);

        expectedResult.add(platform1);

    }

    @Override
    protected String getQueryName() {
        return "pending-topology-detail";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "generic-vnf").has("vnf-id", "vnf-id-0");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
