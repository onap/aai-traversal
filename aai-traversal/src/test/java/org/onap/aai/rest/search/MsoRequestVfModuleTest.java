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

public class MsoRequestVfModuleTest extends QueryTest {
    public MsoRequestVfModuleTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // set up test graph
        Vertex vfmodule = graph.addVertex(T.label, "vf-module", T.id, "0", "aai-node-type",
            "vf-module", "vf-module-id", "vf-module-id-1", "vf-module-name", "vf-module-name1");
        Vertex genericvnf =
            graph.addVertex(T.label, "generic-vnf", T.id, "1", "aai-node-type", "generic-vnf",
                "vnf-id", "vnfid0", "vnf-name", "vnf-name-1", "nf-type", "sample-nf-type");
        Vertex serviceInstance = graph.addVertex(T.label, "service-instance", T.id, "2",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1",
            "service-instance-name", "service-instance-name-1");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver",
            "vserver-id", "vserverid0");
        Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "4", "aai-node-type", "tenant",
            "tenant-id", "tenantid0", "tenant-name", "tenantName0");
        Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "5", "aai-node-type",
            "cloud-region", "cloud-region-id", "regionid0", "cloud-owner", "cloudOwnername0");
        Vertex volumegroup =
            graph.addVertex(T.label, "volume-group", T.id, "6", "aai-node-type", "volume-group",
                "volume-group-id", "volume-group-id-1", "volume-group-name", "volume-group-name1");

        Vertex vfmodule1 = graph.addVertex(T.label, "vf-module", T.id, "10", "aai-node-type",
            "vf-module", "vf-module-id", "vf-module-id-10", "vf-module-name", "vf-module-name10");
        Vertex genericvnf1 =
            graph.addVertex(T.label, "generic-vnf", T.id, "11", "aai-node-type", "generic-vnf",
                "vnf-id", "vnfid0", "vnf-name", "vnf-name-10", "nf-type", "sample-nf-type1");
        Vertex serviceInstance1 = graph.addVertex(T.label, "service-instance", T.id, "12",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-10",
            "service-instance-name", "service-instance-name-10");
        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "13", "aai-node-type",
            "vserver", "vserver-id", "vserverid10");
        Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "14", "aai-node-type", "tenant",
            "tenant-id", "tenantid10", "tenant-name", "tenantName10");
        Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "15", "aai-node-type",
            "cloud-region", "cloud-region-id", "regionid10", "cloud-owner", "cloudOwnername10");
        Vertex volumegroup1 = graph.addVertex(T.label, "volume-group", T.id, "16", "aai-node-type",
            "volume-group", "volume-group-id", "volume-group-id-10", "volume-group-name",
            "volume-group-name10");

        GraphTraversalSource g = graph.traversal();

        rules.addTreeEdge(g, genericvnf, vfmodule);
        rules.addEdge(g, genericvnf, serviceInstance);
        rules.addEdge(g, vserver, vfmodule);
        rules.addTreeEdge(g, vserver, tenant);
        rules.addTreeEdge(g, tenant, cloudregion);
        rules.addEdge(g, vfmodule, volumegroup);

        // false
        rules.addTreeEdge(g, genericvnf1, vfmodule1);
        rules.addEdge(g, genericvnf1, serviceInstance1);
        rules.addEdge(g, vserver1, vfmodule1);
        rules.addTreeEdge(g, vserver1, tenant1);
        rules.addTreeEdge(g, tenant1, cloudregion1);
        rules.addEdge(g, vfmodule1, volumegroup1);

        expectedResult.add(vfmodule);
        expectedResult.add(genericvnf);
        expectedResult.add(serviceInstance);
        expectedResult.add(cloudregion);
        expectedResult.add(volumegroup);

    }

    @Override
    protected String getQueryName() {
        return "so-request-vfModule";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "vf-module").has("vf-module-id", "vf-module-id-1")
            .has("vf-module-name", "vf-module-name1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
    }
}
