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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.search;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ContainmentPathQueryTest extends PathQueryTest {

    public ContainmentPathQueryTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        Vertex pnf1 = graph.addVertex(T.label, "pnf", T.id, "0", "aai-node-type", "pnf", "pnf-name", "pnf-1");
        Vertex pInterface1 = graph.addVertex(T.label, "p-interface", T.id, "1", "aai-node-type", "p-interface", "interface-name", "p-interface-1");
        Vertex lInterface1 = graph.addVertex(T.label, "l-interface", T.id, "2", "aai-node-type", "l-interface", "interface-name", "l-interface-1");
        Vertex vlan1 = graph.addVertex(T.label, "vlan", T.id, "3", "aai-node-type", "vlan", "vlan-interface", "vlan-1");

        rules.addTreeEdge(gts, pnf1, pInterface1);
        rules.addTreeEdge(gts, pInterface1, lInterface1);
        rules.addTreeEdge(gts, lInterface1, vlan1);
    }

    @Test
    public void vlanPathTest() {
        super.run();
        assertEquals("1 path is returned ",1, pathList.size());
        Path path = pathList.get(0);

        Vertex pnf1 = graph.traversal().V().has("aai-node-type", "pnf").has("pnf-name", "pnf-1").next();
        Vertex pInterface1 = graph.traversal().V().has("aai-node-type", "p-interface").has("interface-name", "p-interface-1").next();
        Vertex lInterface1 = graph.traversal().V().has("aai-node-type", "l-interface").has("interface-name", "l-interface-1").next();
        Vertex vlan1 = graph.traversal().V().has("aai-node-type", "vlan").has("vlan-interface", "vlan-1").next();

        //remoce edges
        assertThat(path.objects().stream().filter(o -> o instanceof Vertex).collect(Collectors.toList()), contains(vlan1, lInterface1, pInterface1, pnf1));

    }

    @Override
    protected String getQueryName() {
        return "containment-path";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "vlan").has("vlan-interface", "vlan-1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {

    }
}
