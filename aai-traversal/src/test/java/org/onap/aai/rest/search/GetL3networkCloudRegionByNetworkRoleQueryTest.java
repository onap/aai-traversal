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

public class GetL3networkCloudRegionByNetworkRoleQueryTest extends QueryTest {
    public GetL3networkCloudRegionByNetworkRoleQueryTest()
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
        Vertex l3Network1 = graph.addVertex(T.label, "l3-network", T.id, "0", "aai-node-type",
            "l3-network", "network-id", "networkId1", "network-name", "networkName1",
            "network-role", "networkRole1");
        Vertex genericVnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type",
            "generic-vnf", "vnf-id", "vnfId1", "vnf-name", "vnfName1", "vnf-type", "vnfType1");
        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "2", "aai-node-type", "vserver",
            "vserver-id", "vserverId1", "vserver-name", "vserverName1", "vserver-selflink",
            "vserverSelfLink1");
        Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "3", "aai-node-type", "tenant",
            "tenant-id", "tenantId1", "tenant-name", "tenantName1");
        Vertex cloudRegion1 = graph.addVertex(T.label, "cloud-region", T.id, "4", "aai-node-type",
            "cloud-region", "cloud-owner", "cloudOwner1", "cloud-region-id", "cloudRegionId1");

        // adding extra vertices and edges which shouldn't be picked.
        Vertex l3Network2 = graph.addVertex(T.label, "l3-network", T.id, "5", "aai-node-type",
            "l3-network", "network-id", "networkId2", "network-name", "networkName2",
            "network-role", "networkRole2");
        Vertex genericVnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "6", "aai-node-type",
            "generic-vnf", "vnf-id", "vnfId2", "vnf-name", "vnfName2", "vnf-type", "vnfType2");
        Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "7", "aai-node-type", "vserver",
            "vserver-id", "vserverId2", "vserver-name", "vserverName2", "vserver-selflink",
            "vserverSelfLink2");
        Vertex tenant2 = graph.addVertex(T.label, "tenant", T.id, "8", "aai-node-type", "tenant",
            "tenant-id", "tenantId2", "tenant-name", "tenantName2");
        Vertex cloudRegion2 = graph.addVertex(T.label, "cloud-region", T.id, "9", "aai-node-type",
            "cloud-region", "cloud-owner", "cloudOwner2", "cloud-region-id", "cloudRegionId2");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, l3Network1, genericVnf1);
        rules.addEdge(g, genericVnf1, vserver1);
        rules.addTreeEdge(g, vserver1, tenant1);
        rules.addTreeEdge(g, tenant1, cloudRegion1);

        rules.addEdge(g, l3Network2, genericVnf2);
        rules.addEdge(g, genericVnf2, vserver2);
        rules.addTreeEdge(g, vserver2, tenant2);
        rules.addTreeEdge(g, tenant2, cloudRegion2);

        expectedResult.add(l3Network1);
        expectedResult.add(genericVnf1);
        expectedResult.add(vserver1);
        expectedResult.add(tenant1);
        expectedResult.add(cloudRegion1);
    }

    @Override
    protected String getQueryName() {
        return "getL3networkCloudRegionByNetworkRole";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "l3-network").has("network-id", "networkId1").has("network-role",
            "networkRole1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
