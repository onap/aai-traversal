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

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.edges.exceptions.AmbiguousRuleChoiceException;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;

public class GetServiceInstanceVnfVfModuleModelsTest extends QueryTest {

    public GetServiceInstanceVnfVfModuleModelsTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }
    @Test
    public void run() {
        super.run();
    }



    @Override
    protected void createGraph() throws AAIException, EdgeRuleNotFoundException, AmbiguousRuleChoiceException {

        Vertex serviceInstance = graph.addVertex(T.label, "service-instance", T.id, "1", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-1", "service-instance-name", "service-instance-name-1");
        Vertex modelver = graph.addVertex(T.label, "model-ver", T.id, "2", "aai-node-type", "model-ver","model-ver-id", "model-ver-id-1");
        Vertex genericvnf = graph.addVertex(T.label, "generic-vnf", T.id, "3", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-1");
        Vertex modelver1 = graph.addVertex(T.label, "model-ver", T.id, "4", "aai-node-type", "model-ver","model-ver-id", "model-ver-id-1");
        Vertex model = graph.addVertex(T.label, "model", T.id, "5", "aai-node-type", "model","model-id", "model-id-1");
        Vertex vfmodule = graph.addVertex(T.label, "vf-module", T.id, "6", "aai-node-type", "vf-module", "vf-module-id", "vf-module-id-1");
        Vertex modelver2 = graph.addVertex(T.label, "model-ver", T.id, "7", "aai-node-type", "model-ver","model-ver-id", "model-ver-id-2");
        Vertex model1 = graph.addVertex(T.label, "model", T.id, "8", "aai-node-type", "model","model-id", "model-id-2");
        
        Vertex serviceInstance1 = graph.addVertex(T.label, "service-instance", T.id, "9", "aai-node-type", "service-instance", "service-instance-id", "service-instance-id-2", "service-instance-name", "service-instance-name-2");
        Vertex l3network = graph.addVertex(T.label, "l3-network", T.id, "10", "aai-node-type", "l3-network","l3-network-id", "l3-network-id-1");
        Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "11", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-2");
        Vertex complex = graph.addVertex(T.label, "complex", T.id, "12", "aai-node-type", "complex","complex-id", "complex-1");
        Vertex vfmodule1 = graph.addVertex(T.label, "vf-module", T.id, "13", "aai-node-type", "vf-module", "vf-module-id", "vf-module-id-2");
        Vertex l3network1 = graph.addVertex(T.label, "l3-network", T.id, "14", "aai-node-type", "l3-network","l3-network-id", "l3-network-id-2");
        
        Vertex model2 = graph.addVertex(T.label, "model", T.id, "15", "aai-node-type", "model","model-id", "model-id-3");
        
        
        GraphTraversalSource g = graph.traversal();
        rules.addPrivateEdge(g, serviceInstance, modelver,null);
        rules.addTreeEdge(g, modelver, model2);
        rules.addEdge(g, serviceInstance, genericvnf);
        rules.addPrivateEdge(g, genericvnf, modelver1,null);
        rules.addTreeEdge(g, modelver1, model);
        rules.addTreeEdge(g, genericvnf, vfmodule);
        rules.addPrivateEdge(g, vfmodule, modelver2,null);
        rules.addTreeEdge(g, modelver2, model1);
        
        rules.addEdge(g, serviceInstance1, l3network);//not expected in result
        rules.addEdge(g, serviceInstance1, genericvnf1);//not expected in result
        rules.addEdge(g, genericvnf1, complex);//not expected in result
        rules.addTreeEdge(g, genericvnf1, vfmodule1);//not expected in result
        rules.addEdge(g, vfmodule1, l3network1);//not expected in result
        
        
        expectedResult.add(serviceInstance);
        expectedResult.add(modelver);
        expectedResult.add(model2);
        expectedResult.add(genericvnf);
        expectedResult.add(modelver1);
        expectedResult.add(model);
        expectedResult.add(vfmodule);
        expectedResult.add(modelver2);
        expectedResult.add(model1);
        

    }


    @Override
    protected String getQueryName() {
        return	"getServiceInstanceVnfVfModuleModels";
    }
    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
    	g.has("aai-node-type", "service-instance").has("service-instance-id", "service-instance-id-1");
    }
    @Override
    protected void addParam(Map<String, Object> params) {
        return;
    }
}
