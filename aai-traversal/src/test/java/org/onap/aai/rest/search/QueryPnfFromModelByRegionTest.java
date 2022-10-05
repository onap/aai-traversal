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

public class QueryPnfFromModelByRegionTest extends QueryTest {
    public QueryPnfFromModelByRegionTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        // serviceInstance 1,2,3 are good and 4 is bad based upon the filters
        Vertex serviceInst1 = graph.addVertex(T.label, "service-instance", T.id, "1",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance1",
            "model-invariant-id", "miid1", "model-version-id", "mvid1");
        Vertex serviceInst2 = graph.addVertex(T.label, "service-instance", T.id, "2",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance2",
            "model-invariant-id", "miid1", "model-version-id", "mvid1");
        Vertex serviceInst3 = graph.addVertex(T.label, "service-instance", T.id, "3",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance3",
            "model-invariant-id", "miid1", "model-version-id", "mvid1");
        Vertex serviceInst4 = graph.addVertex(T.label, "service-instance", T.id, "4",
            "aai-node-type", "service-instance", "service-instance-id", "service-instance4",
            "model-invariant-id", "miid2", "model-version-id", "mvid2");

        // pnf 1,2 & 3 are good based upon the filters
        Vertex pnf1 = graph.addVertex(T.label, "pnf", T.id, "5", "aai-node-type", "pnf", "pnf-name",
            "pnf1name", "equip-vendor", "equip-vendor1", "equip-model", "equip-model1");
        Vertex pnf2 = graph.addVertex(T.label, "pnf", T.id, "6", "aai-node-type", "pnf", "pnf-name",
            "pnf2name", "equip-vendor", "equip-vendor1", "equip-model", "equip-model1");
        Vertex pnf3 = graph.addVertex(T.label, "pnf", T.id, "7", "aai-node-type", "pnf", "pnf-name",
            "pnf3name", "equip-vendor", "equip-vendor1", "equip-model", "equip-model1");
        Vertex pnf4 = graph.addVertex(T.label, "pnf", T.id, "12", "aai-node-type", "pnf",
            "pnf-name", "pnf4name", "equip-vendor", "equip-vendor4", "equip-model", "equip-model4");

        Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "8", "aai-node-type", "complex",
            "physical-location-id", "physical-location-id-1");
        Vertex complex2 = graph.addVertex(T.label, "complex", T.id, "9", "aai-node-type", "complex",
            "physical-location-id", "physical-location-id-2");

        // cr 1 is good based upon the filter
        Vertex cloudRegion1 = graph.addVertex(T.label, "cloud-region", T.id, "10", "aai-node-type",
            "cloud-region", "cloud-region-id", "cloud-region1");
        Vertex cloudRegion2 = graph.addVertex(T.label, "cloud-region", T.id, "11", "aai-node-type",
            "cloud-region", "cloud-region-id", "cloud-region2");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, serviceInst1, pnf1);
        rules.addEdge(g, serviceInst2, pnf2);
        rules.addEdge(g, serviceInst3, pnf3);
        rules.addEdge(g, serviceInst4, pnf4);

        rules.addEdge(g, pnf1, complex1);
        rules.addEdge(g, pnf2, complex1);
        rules.addEdge(g, pnf3, complex2);
        rules.addEdge(g, pnf4, complex2);

        rules.addEdge(g, cloudRegion1, complex1);
        rules.addEdge(g, cloudRegion2, complex2);

        expectedResult.add(pnf1);
        expectedResult.add(pnf2);
    }

    @Override
    protected String getQueryName() {
        return "pnf-fromModel-byRegion";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("model-invariant-id", "miid1").has("model-version-id", "mvid1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        params.put("cloudRegionId", "cloud-region1");
        params.put("equipVendor", "equip-vendor1");
        params.put("equipModel", "equip-model1");
    }
}
