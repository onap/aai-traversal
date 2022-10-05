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

public class DestinationFromConfigurationQueryTest extends QueryTest {

    public DestinationFromConfigurationQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void test() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex config = graph.addVertex(T.label, "configuration", T.id, "0", "aai-node-type",
            "configuration", "configuration-id", "configuration");
        Vertex logicalLink = graph.addVertex(T.label, "l", T.id, "1", "aai-node-type",
            "logical-link", "link-name", "link-name-0");
        Vertex lInterface = graph.addVertex(T.label, "l-interface", T.id, "2", "aai-node-type",
            "l-interface", "interface-name", "interface-name-0");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver",
            "vserver-id", "vserver-id-0", "vserver-name", "vserver-name-0");
        Vertex vfmodule = graph.addVertex(T.label, "vf-module", T.id, "4", "aai-node-type",
            "vf-module", "vf-module-id", "vf-module-id-0", "vf-module-name", "vf-module-name0");

        Vertex gnvf = graph.addVertex(T.label, "generic-vnf", T.id, "5", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-0", "vnf-name", "vnf-name-0");

        Vertex logicalLink1 = graph.addVertex(T.label, "l", T.id, "11", "aai-node-type",
            "logical-link", "link-name", "link-name-1");
        Vertex lInterface1 = graph.addVertex(T.label, "l-interface", T.id, "12", "aai-node-type",
            "l-interface", "interface-name", "interface-name-1");
        Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "13", "aai-node-type",
            "vserver", "vserver-id", "vserver-id-1", "vserver-name", "vserver-name-1");
        Vertex vfmodule1 = graph.addVertex(T.label, "vf-module", T.id, "14", "aai-node-type",
            "vf-module", "vf-module-id", "vf-module-id-1", "vf-module-name", "vf-module-name1");

        Vertex gnvf1 = graph.addVertex(T.label, "generic-vnf", T.id, "15", "aai-node-type",
            "generic-vnf", "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1");
        Vertex pnf1 = graph.addVertex(T.label, "pnf", T.id, "16", "aai-node-type", "pnf",
            "pnf-name", "pnfname-1");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, config, logicalLink);
        rules.addEdge(g, logicalLink, lInterface, "org.onap.relationships.inventory.Destination");
        rules.addTreeEdge(g, lInterface, vserver);
        rules.addEdge(g, vserver, vfmodule);
        rules.addTreeEdge(g, vfmodule, gnvf);

        rules.addEdge(g, logicalLink, lInterface1, "tosca.relationships.network.LinksTo");// false
        rules.addTreeEdge(g, lInterface1, vserver1);// false
        rules.addEdge(g, vserver1, vfmodule1);// false
        rules.addTreeEdge(g, vfmodule1, gnvf1);// false

        rules.addEdge(g, config, logicalLink1);
        rules.addEdge(g, logicalLink1, gnvf1);
        rules.addEdge(g, gnvf1, pnf1);

        expectedResult.add(gnvf);
        expectedResult.add(pnf1);
    }

    @Override
    protected String getQueryName() {
        return "destination-FromConfiguration";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "configuration").has("configuration-id", "configuration");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }

}
