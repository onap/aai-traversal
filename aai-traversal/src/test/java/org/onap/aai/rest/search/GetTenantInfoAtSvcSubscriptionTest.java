package org.onap.aai.rest.search;
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

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class GetTenantInfoAtSvcSubscriptionTest extends QueryTest {

    public GetTenantInfoAtSvcSubscriptionTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void test() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex servicesubscription = graph.addVertex(T.label, "service-subscription", T.id, "0",
            "aai-node-type", "service-subscription", "service-type", "service-type-0");
        Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "1", "aai-node-type", "tenant",
            "tenant-id", "tenant-id-0", "tenant-name", "tenant-name-0");
        Vertex cloudregion = graph.addVertex(T.label, "cloud-region", T.id, "2", "aai-node-type",
            "cloud-region", "cloud-owner", "cloud-owner-0", "cloud-region-id", "cloud-region-id-0");
        Vertex complex = graph.addVertex(T.label, "complex", T.id, "3", "aai-node-type", "complex",
            "physical-location-id", "physical-location-id-0", "physical-location-type",
            "physical-location-type-0", "street1", "street1-0", "city", "city-0", "postal-code",
            "postal-code-0", "country", "country-0", "region", "region-0");
        Vertex servicesubscription1 = graph.addVertex(T.label, "service-subscription", T.id, "5",
            "aai-node-type", "service-subscription", "service-type", "service-type-1");
        Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "6", "aai-node-type", "tenant",
            "tenant-id", "tenant-id-1", "tenant-name", "tenant-name-1");
        Vertex cloudregion1 = graph.addVertex(T.label, "cloud-region", T.id, "7", "aai-node-type",
            "cloud-region", "cloud-owner", "cloud-owner-1", "cloud-region-id", "cloud-region-id-1");
        Vertex complex1 = graph.addVertex(T.label, "complex", T.id, "8", "aai-node-type", "complex",
            "physical-location-id", "physical-location-id-1", "physical-location-type",
            "physical-location-type-1", "street1", "street1-1", "city", "city-1", "postal-code",
            "postal-code-1", "country", "country-1", "region", "region-1");

        GraphTraversalSource g = graph.traversal();
        rules.addEdge(g, servicesubscription, tenant);
        rules.addTreeEdge(g, tenant, cloudregion);
        rules.addEdge(g, cloudregion, complex);

        rules.addEdge(g, servicesubscription1, tenant1);
        rules.addTreeEdge(g, tenant1, cloudregion1);
        rules.addEdge(g, cloudregion1, complex1);

        expectedResult.add(servicesubscription);
        expectedResult.add(tenant);
        expectedResult.add(cloudregion);
        expectedResult.add(complex);
    }

    @Override
    protected String getQueryName() {
        return "getTenantInfoAtSvcSubscription";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "service-subscription").has("service-type", "service-type-0");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }

}
