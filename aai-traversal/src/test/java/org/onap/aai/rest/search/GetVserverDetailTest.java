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
import org.onap.aai.edges.exceptions.AmbiguousRuleChoiceException;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class GetVserverDetailTest extends QueryTest {
    public GetVserverDetailTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException,
        EdgeRuleNotFoundException, AmbiguousRuleChoiceException {
        // set up test graph
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver",
            "vserver-id", "vserverid0");
        Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "1", "aai-node-type",
            "cloud-region", "cloud-region-id", "regionid0", "cloud-owner", "cloudOwnername0");
        Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "2", "aai-node-type", "tenant",
            "tenant-id", "tenantid0", "tenant-name", "tenantName0");
        Vertex genericvnf = graph.addVertex(T.label, "generic-vnf", T.id, "4", "aai-node-type",
            "generic-vnf", "vnf-id", "vnfid0");
        Vertex vfmodule = graph.addVertex(T.label, "vf-module", T.id, "5", "aai-node-type",
            "vf-module", "vf-module-id", "vfmoduleid0");
        Vertex serviceinstance = graph.addVertex(T.label, "service-instance", T.id, "6",
            "aai-node-type", "service-instance", "service-intsance-id", "serviceinstanceid0");
        Vertex modelver0 = graph.addVertex(T.label, "model-ver", T.id, "7", "aai-node-type",
            "model-ver", "model-version-id", "modelversionid0");
        Vertex modelver1 = graph.addVertex(T.label, "model-ver", T.id, "8", "aai-node-type",
            "model-ver", "model-version-id", "modelversionid1");
        Vertex modelver2 = graph.addVertex(T.label, "model-ver", T.id, "9", "aai-node-type",
            "model-ver", "model-version-id", "modelversionid2");
        Vertex model0 = graph.addVertex(T.label, "model", T.id, "10", "aai-node-type", "model",
            "model-invariant-id", "modelinvariantid0");
        Vertex model1 = graph.addVertex(T.label, "model", T.id, "11", "aai-node-type", "model",
            "model-invariant-id", "modelinvariantid1");
        Vertex model2 = graph.addVertex(T.label, "model", T.id, "12", "aai-node-type", "model",
            "model-invariant-id", "modelinvariantid2");

        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "13", "aai-node-type",
            "vserver", "vserver-id", "vserverid1");
        Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "14", "aai-node-type",
            "cloud-region", "cloud-region-id", "regionid0", "cloud-owner", "cloudOwnername0");
        Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "15", "aai-node-type", "tenant",
            "tenant-id", "tenantid0", "tenant-name", "tenantName0");
        Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "16", "aai-node-type",
            "generic-vnf", "vnf-id", "vnfid0");
        Vertex vfmodule1 = graph.addVertex(T.label, "vf-module", T.id, "17", "aai-node-type",
            "vf-module", "vf-module-id", "vfmoduleid0");
        Vertex serviceinstance1 = graph.addVertex(T.label, "service-instance", T.id, "18",
            "aai-node-type", "service-instance", "service-intsance-id", "serviceinstanceid0");
        Vertex modelver3 = graph.addVertex(T.label, "model-ver", T.id, "19", "aai-node-type",
            "model-ver", "model-version-id", "modelversionid0");
        Vertex modelver4 = graph.addVertex(T.label, "model-ver", T.id, "20", "aai-node-type",
            "model-ver", "model-version-id", "modelversionid1");
        Vertex modelver5 = graph.addVertex(T.label, "model-ver", T.id, "21", "aai-node-type",
            "model-ver", "model-version-id", "modelversionid2");
        Vertex model3 = graph.addVertex(T.label, "model", T.id, "22", "aai-node-type", "model",
            "model-invariant-id", "modelinvariantid0");
        Vertex model4 = graph.addVertex(T.label, "model", T.id, "23", "aai-node-type", "model",
            "model-invariant-id", "modelinvariantid1");
        Vertex model5 = graph.addVertex(T.label, "model", T.id, "24", "aai-node-type", "model",
            "model-invariant-id", "modelinvariantid2");
        GraphTraversalSource g = graph.traversal();

        rules.addTreeEdge(g, vserver, tenant);
        rules.addTreeEdge(g, tenant, cloudregion);
        rules.addEdge(g, vserver, genericvnf);
        rules.addPrivateEdge(g, genericvnf, modelver0, null);
        rules.addTreeEdge(g, modelver0, model0);
        rules.addTreeEdge(g, genericvnf, vfmodule);
        rules.addPrivateEdge(g, vfmodule, modelver1, null);
        rules.addTreeEdge(g, modelver1, model1);
        rules.addEdge(g, genericvnf, serviceinstance);
        rules.addPrivateEdge(g, serviceinstance, modelver2, null);
        rules.addTreeEdge(g, modelver2, model2);

        rules.addTreeEdge(g, vserver1, tenant1);// false
        rules.addTreeEdge(g, tenant1, cloudregion1);// false
        rules.addEdge(g, vserver1, genericvnf1);// false
        rules.addPrivateEdge(g, genericvnf1, modelver3, null);// false
        rules.addTreeEdge(g, modelver3, model3);// false
        rules.addTreeEdge(g, genericvnf1, vfmodule1);// false
        rules.addPrivateEdge(g, vfmodule1, modelver4, null);// false
        rules.addTreeEdge(g, modelver4, model4);// false
        rules.addEdge(g, genericvnf1, serviceinstance1);
        rules.addPrivateEdge(g, serviceinstance1, modelver5, null);// false
        rules.addTreeEdge(g, modelver5, model5);// false

        expectedResult.add(vserver);
        expectedResult.add(tenant);
        expectedResult.add(cloudregion);
        expectedResult.add(genericvnf);
        expectedResult.add(modelver0);
        expectedResult.add(model0);
        expectedResult.add(vfmodule);
        expectedResult.add(modelver1);
        expectedResult.add(model1);
        expectedResult.add(serviceinstance);
        expectedResult.add(modelver2);
        expectedResult.add(model2);
    }

    @Override
    protected String getQueryName() {
        return "getVserverDetail";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "vserver").has("vserver-id", "vserverid0");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
