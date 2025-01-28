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

public class CloudRegionFromVnfTest extends QueryTest {

    public CloudRegionFromVnfTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        Vertex gv = graph.addVertex(T.id, "00", "aai-node-type", "generic-vnf", "vnf-id", "gvId",
            "vnf-name", "name", "vnf-type", "type");
        Vertex vnfc = graph.addVertex(T.id, "10", "aai-node-type", "vnfc", "vnfc-name", "vnfcName1",
            "nfc-naming-code", "blue", "nfc-function", "correct-function");
        Vertex vserv = graph.addVertex(T.id, "20", "aai-node-type", "vserver", "vserver-id",
            "vservId", "vserver-name", "vservName", "vserver-selflink", "me/self");
        Vertex cr = graph.addVertex(T.id, "30", "aai-node-type", "cloud-region", "cloud-owner",
            "some guy", "cloud-region-id", "crId");
        Vertex tenant = graph.addVertex(T.id, "40", "aai-node-type", "tenant", "tenant-id", "ten1",
            "tenant-name", "tenName");
        Vertex pserv = graph.addVertex(T.id, "50", "aai-node-type", "pserver", "hostname",
            "hostname1", "in-maint", "false");
        Vertex vserv2 = graph.addVertex(T.id, "60", "aai-node-type", "vserver", "vserver-id",
            "vservId2", "vserver-name", "vservName2", "vserver-selflink", "me/self");
        Vertex pserv2 = graph.addVertex(T.id, "70", "aai-node-type", "pserver", "hostname",
            "hostname2", "in-maint", "false");
        Vertex tenant2 = graph.addVertex(T.id, "80", "aai-node-type", "tenant", "tenant-id", "ten2",
            "tenant-name", "tenName2");
        Vertex cr2 = graph.addVertex(T.id, "90", "aai-node-type", "cloud-region", "cloud-owner",
            "some guy2", "cloud-region-id", "crId2");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, gv, vnfc);
        rules.addEdge(g, vnfc, vserv);
        rules.addEdge(g, vserv, pserv);
        rules.addTreeEdge(g, cr, tenant);
        rules.addTreeEdge(g, tenant, vserv);
        rules.addEdge(g, gv, vserv2);
        rules.addEdge(g, vserv2, pserv2);
        rules.addTreeEdge(g, vserv2, tenant2);
        rules.addTreeEdge(g, tenant2, cr2);

        expectedResult.add(gv);
        expectedResult.add(cr);
        expectedResult.add(tenant);
        expectedResult.add(vnfc);
        expectedResult.add(vserv);
        expectedResult.add(pserv);
        expectedResult.add(cr2);
        expectedResult.add(tenant2);
        expectedResult.add(vserv2);
        expectedResult.add(pserv2);
    }

    @Override
    protected String getQueryName() {
        return "cloud-region-fromVnf";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "generic-vnf").has("vnf-id", "gvId");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        // N/A for this query
    }

}
