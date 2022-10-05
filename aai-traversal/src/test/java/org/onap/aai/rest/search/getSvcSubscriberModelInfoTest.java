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

public class getSvcSubscriberModelInfoTest extends QueryTest {

    public getSvcSubscriberModelInfoTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph()
        throws AAIException, EdgeRuleNotFoundException, AmbiguousRuleChoiceException {

        Vertex serviceInstance = graph.addVertex(T.label, "service-instance", T.id, "1",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1",
            "service-instance-name", "service-instance-name-1");
        Vertex serviceSubscription = graph.addVertex(T.label, "service-subscription", T.id, "2",
            "aai-node-type", "service-subscription", "service-type", "service-subcription-1");
        Vertex modelver =
            graph.addVertex(T.label, "model-ver", T.id, "3", "aai-node-type", "model-ver");

        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, serviceInstance, serviceSubscription);
        rules.addPrivateEdge(g, serviceInstance, modelver, null);

        expectedResult.add(serviceInstance);
        expectedResult.add(serviceSubscription);
        expectedResult.add(modelver);

    }

    @Override
    protected String getQueryName() {
        return "getSvcSubscriberModelInfo";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "service-instance").has("service-instance-id",
            "service-instance-id-1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
