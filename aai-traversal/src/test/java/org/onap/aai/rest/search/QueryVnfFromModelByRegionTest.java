/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.rest.search;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;

public class QueryVnfFromModelByRegionTest extends QueryTest {
  public QueryVnfFromModelByRegionTest() throws AAIException, NoEdgeRuleFoundException {
    super();
  }

  @Test
  public void run() {
    super.run();
  }

  @Override
  protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
    Vertex serviceInst1 = graph.addVertex(T.label, "service-instance", T.id, "1", "aai-node-type", "service-instance",
        "service-instance-id", "service-instance1", "model-invariant-id", "miid1", "model-version-id", "mvid1");
    Vertex serviceInst2 = graph.addVertex(T.label, "service-instance", T.id, "12", "aai-node-type", "service-instance",
        "service-instance-id", "service-instance2", "model-invariant-id", "miid2", "model-version-id", "mvid2");

    Vertex genericVnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "2", "aai-node-type", "generic-vnf", "generic-vnf-id", "generic-vnf1");
    Vertex vserver1 = graph.addVertex(T.label, "vserver", T.id, "3", "aai-node-type", "vserver", "vserver-id", "vserver1");
    Vertex tenant1 = graph.addVertex(T.label, "tenant", T.id, "4", "aai-node-type", "tenant", "tenant-id", "tenant1");
    Vertex cloudRegion1 = graph.addVertex(T.label, "cloud-region", T.id, "5", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region1");

    // Right invariant and version IDs, wrong cloud region
    Vertex genericVnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "6", "aai-node-type", "generic-vnf",
        "generic-vnf-id", "generic-vnf2");
    Vertex vserver2 = graph.addVertex(T.label, "vserver", T.id, "7", "aai-node-type", "vserver", "vserver-id", "vserver2");
    Vertex tenant2 = graph.addVertex(T.label, "tenant", T.id, "8", "aai-node-type", "tenant", "tenant-id", "tenant2");
    Vertex cloudRegion2 = graph.addVertex(T.label, "cloud-region", T.id, "9", "aai-node-type", "cloud-region", "cloud-region-id", "cloud-region2");

    // On both the right and the wrong service-instance, with the right cloud-region
    Vertex genericVnf3 = graph.addVertex(T.label, "generic-vnf", T.id, "10", "aai-node-type", "generic-vnf",
        "generic-vnf-id", "generic-vnf3");
    Vertex vserver3 = graph.addVertex(T.label, "vserver", T.id, "11", "aai-node-type", "vserver", "vserver-id", "vserver3");

    GraphTraversalSource g = graph.traversal();
    rules.addEdge(g, serviceInst1, genericVnf1);
    rules.addEdge(g, genericVnf1, vserver1);
    rules.addTreeEdge(g, tenant1, vserver1);
    rules.addTreeEdge(g, cloudRegion1, tenant1);

    rules.addEdge(g, serviceInst1, genericVnf2);
    rules.addEdge(g, genericVnf2, vserver2);
    rules.addTreeEdge(g, tenant2, vserver2);
    rules.addTreeEdge(g, cloudRegion2, tenant2);
    
    rules.addEdge(g, serviceInst2, genericVnf3);
    rules.addEdge(g, genericVnf3, vserver3);
    rules.addTreeEdge(g, tenant1, vserver3);

    expectedResult.add(genericVnf1);
  }

  @Override
  protected String getQueryName() {
    return "queryvnfFromModelbyRegion";
  }

  @Override
  protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
    g.has("model-invariant-id", "miid1").has("model-version-id", "mvid1");
  }

  @Override
  protected void addParam(Map<String, Object> params) {
    params.put("cloudRegionId", "cloud-region1");
  }
}
