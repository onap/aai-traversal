/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.aai.rest.search;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class VnfTopologyFromVfModuleTest extends QueryTest {
    public VnfTopologyFromVfModuleTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        //Set up the test graph


        Vertex gnvf1 = graph.addVertex(T.label, "generic-vnf", T.id, "0", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1");
        Vertex serviceinstance = graph.addVertex(T.label, "service-instance", T.id, "1", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1", "service-instance-name", "service-instance-name-1");
        Vertex servicesubscription = graph.addVertex(T.label, "service-subscription", T.id, "2", "aai-node-type", "service-subscription", "service-subscription-id", "service-subscription-id-1","service-subscription-name","service-subscription-name1");
        Vertex customer = graph.addVertex(T.label, "customer", T.id, "3", "aai-node-type", "customer", "customer-id", "customer-id-1", "customer-name", "customer-name1");
        Vertex allottedresource = graph.addVertex(T.label, "allotted-resource", T.id, "4", "aai-node-type", "allotted-resource", "allotted-resource-id", "allotted-resource-id-1", "allotted-resource-name", "allotted-resource-name1");
        Vertex vfmodule = graph.addVertex(T.label, "vf-module", T.id, "5", "aai-node-type", "vf-module", "vf-module-id", "vf-module-id-1", "vf-module-name", "vf-module-name1");
        Vertex volumegroup = graph.addVertex(T.label, "volume-group", T.id, "6", "aai-node-type", "volume-group", "volume-group-id", "volume-group-id-1", "volume-group-name", "volume-group-name1");
        Vertex linter1 = graph.addVertex(T.label, "l-interface", T.id, "7", "aai-node-type", "l-interface", "l-interface-id", "l-interface-id-1", "l-interface-name", "l-interface-name1");
        Vertex l3inter1ipv4addresslist = graph.addVertex(T.label, "interface-ipv4-address-list", T.id, "8", "aai-node-type", "l3-interface-ipv4-address-list", "l3-interface-ipv4-address-list-id", "l3-interface-ipv4-address-list-id-1", "l3-interface-ipv6-address-list-name", "l3-interface-ipv6-address-list-name1");
        Vertex l3network1 = graph.addVertex(T.label, "l3-network", T.id, "9", "aai-node-type", "l3-network", "ll3-network-id", "l3-network-id-1", "l3-network-name", "l3-network-name1");
        Vertex l3inter1ipv6addresslist = graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "10", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address-list-id", "l3-interface-ipv6-address-list-id-1", "l3-interface-ipv6-address-list-name", "l3-interface-ipv6-address-list-name1");
        Vertex vserver = graph.addVertex(T.label, "vserver", T.id, "11", "aai-node-type", "vserver", "vserver-name1", "vservername1");
        Vertex tenant = graph.addVertex(T.label, "tenant", T.id, "12", "aai-node-type", "tenant", "tenant-name1", "tenant-name-1","tenant-id", "tenant-id-1");
        Vertex region1 = graph.addVertex(T.label, "cloud-region", T.id, "13", "aai-node-type", "cloud-region", "cloud-owner", "cloudOwner1");
        Vertex pserver = graph.addVertex(T.label, "pserver", T.id, "14", "aai-node-type", "pserver", "hostname", "pservername");
        Vertex linter2 = graph.addVertex(T.label, "l-interface", T.id, "15", "aai-node-type", "l-interface", "l-interface-id", "l-interface-id-2", "l-interface-name", "l-interface-name2");
        Vertex l3inter2ipv4addresslist = graph.addVertex(T.label, "interface-ipv6-address-list", T.id, "16", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address-list-id", "l3-interface-ipv6-address-list-id-2", "l3-interface-ipv6-address-list-name", "l3-interface-ipv6-address-list-name2");
        Vertex l3network2 = graph.addVertex(T.label, "l3-network", T.id, "17", "aai-node-type", "l3-network", "ll3-network-id", "l3-network-id-2", "l3-network-name", "l3-network-name2");
        Vertex l3inter2ipv6addresslist = graph.addVertex(T.label, "l3-interface-ipv6-address-list", T.id, "18", "aai-node-type", "l3-interface-ipv6-address-list", "l3-interface-ipv6-address-list-id", "l3-interface-ipv6-address-list-id-2", "l3-interface-ipv6-address-list-name", "l3-interface-ipv6-address-list-name2");
        Vertex l3network3 = graph.addVertex(T.label, "l3-network", T.id, "19", "aai-node-type", "l3-network", "ll3-network-id", "l3-network-id-3", "l3-network-name", "l3-network-name3");
        Vertex l3network4 = graph.addVertex(T.label, "l3-network", T.id, "20", "aai-node-type", "l3-network", "ll3-network-id", "l3-network-id-4", "l3-network-name", "l3-network-name4");
        Vertex vfmoduleStart = graph.addVertex(T.label, "vf-module", T.id, "21", "aai-node-type", "vf-module", "vf-module-id", "vf-module-id-2", "vf-module-name", "vf-module-name2");



        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, gnvf1, vfmoduleStart);//true
        rules.addEdge(g, gnvf1, serviceinstance);//false
        rules.addTreeEdge(g, serviceinstance, servicesubscription);//true
        rules.addTreeEdge(g, servicesubscription, customer);//true
        rules.addTreeEdge(g, serviceinstance, allottedresource);//true
        rules.addTreeEdge(g, gnvf1, vfmodule);//true
        rules.addEdge(g, gnvf1, volumegroup);//false
        rules.addTreeEdge(g, gnvf1, linter1);//true
        rules.addTreeEdge(g, linter1, l3inter1ipv4addresslist);//true
        rules.addEdge(g, l3inter1ipv4addresslist, l3network1);//false
        rules.addTreeEdge(g, linter1, l3inter1ipv6addresslist);//true
        rules.addEdge(g, l3inter1ipv6addresslist, l3network2);//false
        rules.addEdge(g, gnvf1, vserver);//false
        rules.addTreeEdge(g, vserver, tenant);//true
        rules.addTreeEdge(g, tenant, region1);//true
        rules.addEdge(g, vserver, pserver);//false
        rules.addTreeEdge(g, vserver, linter2);//false
        rules.addTreeEdge(g, linter2, l3inter2ipv4addresslist);//false
        rules.addEdge(g, l3inter2ipv4addresslist, l3network3);//false
        rules.addTreeEdge(g, linter2, l3inter2ipv6addresslist);//true
        rules.addEdge(g, l3inter2ipv6addresslist, l3network4);//true


        expectedResult.add(gnvf1);
        expectedResult.add(serviceinstance);
        expectedResult.add(customer);
        expectedResult.add(allottedresource);
        expectedResult.add(vfmodule);
        expectedResult.add(vfmoduleStart);
        expectedResult.add(volumegroup);
        expectedResult.add(l3inter1ipv4addresslist);
        expectedResult.add(l3network1);
        expectedResult.add(l3inter1ipv6addresslist);
        expectedResult.add(l3network2);
        expectedResult.add(vserver);
        expectedResult.add(tenant);
        expectedResult.add(region1);
        expectedResult.add(pserver);
        expectedResult.add(l3inter2ipv4addresslist);
        expectedResult.add(l3network3);
        expectedResult.add(l3inter2ipv6addresslist);
        expectedResult.add(l3network4);

    }

    @Override
    protected String getQueryName() {
        return	"vnf-topology-fromVfModule";
    }
    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("vf-module-name", "vf-module-name2");

    }
    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
