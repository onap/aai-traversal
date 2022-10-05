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

public class TopologySummaryFromCloudRegionQueryTest extends QueryTest {

    public TopologySummaryFromCloudRegionQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        Vertex region = graph.addVertex(T.label, "cloud-region", T.id, "1", "aai-node-type",
            "cloud-region", "cloud-region-id", "cloud-region-id-1", "cloud-region-owner",
            "cloud-region-owner-1", "cloud-region-version", "cloud-region-version-1");
        Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "2", "aai-node-type", "tenant",
            "tenant-id", "tenant-id-1", "tenant-name", "tenant-name-1");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver",
            "vserver-id", "vserver-id-1", "vserver-name", "vserver-name-1");
        Vertex vnf = graph.addVertex(T.label, "generic-vnf", T.id, "4", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-uuid-1", "vnf-name", "vnf-name=1");
        Vertex vnfc = graph.addVertex(T.label, "vnfc", T.id, "13", "aai-node-type", "vnfc",
            "vnfc-name", "VnfcName", "nfc-naming-code", "NfcNamingCode", "nfc-function",
            "NfcFunction", "in-maint", "false", "is-closed-loop-disabled", "false");
        Vertex vnfc2 = graph.addVertex(T.label, "vnfc", T.id, "15", "aai-node-type", "vnfc",
            "vnfc-name", "VnfcName3", "nfc-naming-code", "NfcNamingCode3", "nfc-function",
            "NfcFunction3", "in-maint", "false", "is-closed-loop-disabled", "false");
        Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "5", "aai-node-type", "pserver",
            "hostname", "pserver-hostname-1");
        Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "6", "aai-node-type", "pserver",
            "hostname", "pserver-hostname-2");

        Vertex region_exclude = graph.addVertex(T.label, "cloud-region", T.id, "7", "aai-node-type",
            "cloud-region", "cloud-region-id", "cloud-region-id-2", "cloud-region-owner",
            "cloud-region-owner-2", "cloud-region-version", "cloud-region-version-2");
        Vertex tenant_exclude = graph.addVertex(T.label, "tenant", T.id, "8", "aai-node-type",
            "tenant", "tenant-id", "tenant-id-2", "tenant-name", "tenant-name-2");
        Vertex vserver_exclude = graph.addVertex(T.label, "vserver", T.id, "9", "aai-node-type",
            "vserver", "vserver-id", "vserver-id-2", "vserver-name", "vserver-name-2");
        Vertex vnf_exclude = graph.addVertex(T.label, "generic-vnf", T.id, "10", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-uuid-2", "vnf-name", "vnf-name=2");
        Vertex vnfc_exclude = graph.addVertex(T.label, "vnfc", T.id, "14", "aai-node-type", "vnfc",
            "vnfc-name", "VnfcName2", "nfc-naming-code", "NfcNamingCode2", "nfc-function",
            "NfcFunction2", "in-maint", "false", "is-closed-loop-disabled", "false");
        Vertex vnfc_exclude2 = graph.addVertex(T.label, "vnfc", T.id, "16", "aai-node-type", "vnfc",
            "vnfc-name", "VnfcName4", "nfc-naming-code", "NfcNamingCode4", "nfc-function",
            "NfcFunction4", "in-maint", "false", "is-closed-loop-disabled", "false");
        Vertex pserver_exclude = graph.addVertex(T.label, "pserver", T.id, "11", "aai-node-type",
            "pserver", "hostname", "pserver-hostname-2");
        Vertex pserver2_exclude = graph.addVertex(T.label, "pserver", T.id, "12", "aai-node-type",
            "pserver", "hostname", "pserver-hostname-3");

        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, region, tenant);
        rules.addTreeEdge(g, tenant, vserver);
        rules.addEdge(g, vserver, pserver);
        rules.addEdge(g, vserver, vnf);
        rules.addEdge(g, vnf, vnfc);
        rules.addEdge(g, vserver, vnfc2);
        rules.addEdge(g, region, pserver2);

        rules.addTreeEdge(g, region_exclude, tenant_exclude);
        rules.addTreeEdge(g, tenant_exclude, vserver_exclude);
        rules.addEdge(g, vserver_exclude, pserver_exclude);
        rules.addEdge(g, vserver_exclude, vnf_exclude);
        rules.addEdge(g, vnf_exclude, vnfc_exclude);
        rules.addEdge(g, vserver_exclude, vnfc_exclude2);
        rules.addEdge(g, region_exclude, pserver2_exclude);

        expectedResult.add(region);
        expectedResult.add(tenant);
        expectedResult.add(vserver);
        expectedResult.add(pserver);
        expectedResult.add(vnf);
        expectedResult.add(vnfc);
        expectedResult.add(vnfc2);
        expectedResult.add(pserver2);
    }

    @Override
    protected String getQueryName() {
        return "topology-summary-fromCloudRegion";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("cloud-region-id", "cloud-region-id-1").has("cloud-region-owner",
            "cloud-region-owner-1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }

}
