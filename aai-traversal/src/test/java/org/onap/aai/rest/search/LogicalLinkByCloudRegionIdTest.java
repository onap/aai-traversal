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

public class LogicalLinkByCloudRegionIdTest extends QueryTest {
    public LogicalLinkByCloudRegionIdTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        // Set up the test graph
        Vertex cloudRegion1 = graph.addVertex(T.label, "cloud-region", T.id, "0", "aai-node-type",
            "cloud-region", "cloud-owner", "cloudOwner1", "cloud-region-id", "cloudRegionId1");
        Vertex logicalLink1 =
            graph.addVertex(T.label, "logical-link", T.id, "1", "aai-node-type", "logical-link",
                "link-name", "linkName1", "in-maint", "false", "link-type", "linkType1");

        // adding extra vertices and edges which shouldn't be picked.
        Vertex cloudRegion2 = graph.addVertex(T.label, "cloud-region", T.id, "2", "aai-node-type",
            "cloud-region", "cloud-owner", "cloudOwner2", "cloud-region-id", "cloudRegionId3");
        Vertex logicalLink2 =
            graph.addVertex(T.label, "logical-link", T.id, "3", "aai-node-type", "logical-link",
                "link-name", "linkName2", "in-maint", "false", "link-type", "linkType4");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, cloudRegion1, logicalLink1);
        rules.addEdge(g, cloudRegion2, logicalLink2);

        expectedResult.add(cloudRegion1);
        expectedResult.add(logicalLink1);
    }

    @Override
    protected String getQueryName() {
        return "getLogicalLinkByCloudRegionId";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "cloud-region").has("cloud-region-id", "cloudRegionId1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
