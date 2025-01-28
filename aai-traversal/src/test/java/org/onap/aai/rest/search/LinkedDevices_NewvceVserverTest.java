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

public class LinkedDevices_NewvceVserverTest extends QueryTest {

    public LinkedDevices_NewvceVserverTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Note: I don't know if this topology is realistic, but it doesn't really matter bc we're
        // just testing functionality
        Vertex newvce1 = graph.addVertex(T.label, "newvce", T.id, "00", "aai-node-type", "newvce",
            "vnf-id2", "newvce1", "vnf-name", "bob", "vnf-type", "new");

        Vertex lint1 = graph.addVertex(T.label, "l-interface", T.id, "10", "aai-node-type",
            "l-interface", "interface-name", "lint1", "is-port-mirrored", "true", "in-maint",
            "true", "is-ip-unnumbered", "false");
        Vertex loglink1 = graph.addVertex(T.label, "logical-link", T.id, "20", "aai-node-type",
            "logical-link", "link-name", "loglink1", "in-maint", "false", "link-type", "sausage");
        Vertex lint2 = graph.addVertex(T.label, "l-interface", T.id, "11", "aai-node-type",
            "l-interface", "interface-name", "lint2", "is-port-mirrored", "true", "in-maint",
            "true", "is-ip-unnumbered", "false");
        Vertex vlan = graph.addVertex(T.label, "vlan", T.id, "40", "aai-node-type", "vlan",
            "vlan-interface", "vlan1");

        Vertex newvce2 = graph.addVertex(T.label, "newvce", T.id, "01", "aai-node-type", "newvce",
            "vnf-id2", "newvce2", "vnf-name", "bob", "vnf-type", "new");

        Vertex loglink2 = graph.addVertex(T.label, "logical-link", T.id, "21", "aai-node-type",
            "logical-link", "link-name", "loglink2", "in-maint", "false", "link-type", "sausage");
        Vertex lint3 = graph.addVertex(T.label, "l-interface", T.id, "12", "aai-node-type",
            "l-interface", "interface-name", "lint3", "is-port-mirrored", "true", "in-maint",
            "true", "is-ip-unnumbered", "false");

        Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "50", "aai-node-type",
            "cloud-region", "cloud-owner", "att", "cloud-region-id", "crId");
        Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "60", "aai-node-type", "tenant",
            "tenant-id", "tenId", "tenant-name", "verity");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "30", "aai-node-type", "vserver",
            "vserver-id", "vserv1", "vserver-name", "frank");

        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, newvce1, lint1);
        rules.addTreeEdge(g, lint1, vlan);
        rules.addEdge(g, lint1, loglink1);
        rules.addEdge(g, lint2, loglink1);

        rules.addTreeEdge(g, cloudregion, tenant);
        rules.addTreeEdge(g, tenant, vserver);
        rules.addTreeEdge(g, vserver, lint2);

        rules.addEdge(g, vlan, loglink2);
        rules.addTreeEdge(g, newvce2, lint3);
        rules.addEdge(g, loglink2, lint3);

        expectedResult.add(newvce1);
        expectedResult.add(newvce2);
        expectedResult.add(vserver);
    }

    @Override
    protected String getQueryName() {
        return "linked-devices";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "newvce").has("vnf-id2", "newvce1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        // n/a for this query
    }
}
