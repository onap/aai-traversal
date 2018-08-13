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
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

import java.util.Map;

public class GetLinterfaceFromNewvceTest extends QueryTest {
    public GetLinterfaceFromNewvceTest () throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void run() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {

        // Set up the test graph
        Vertex newvce = graph.addVertex(T.label, "newvce", T.id, "0", "aai-node-type", "newvce", "vnf-id2", "vnfId2-1", "vnf-name", "vnfName-1", "vnf-type", "vnfType-1");
        Vertex linterfaceNewvce1 = graph.addVertex(T.label, "l-interface", T.id, "1", "aai-node-type", "l-interface", "l-interface-id", "lInterfaceId-10", 
        							"l-interface-name", "lInterfaceName-1", "interface-role", "UPLINK");
        Vertex linterfaceNewvce2 = graph.addVertex(T.label, "l-interface", T.id, "2", "aai-node-type", "l-interface", "l-interface-id", "lInterfaceId-20", 
        							"l-interface-name", "lInterfaceName-1", "interface-role", "CUSTOMER");
        Vertex linterfaceNewvce3 = graph.addVertex(T.label, "l-interface", T.id, "3", "aai-node-type", "l-interface", "l-interface-id", "lInterfaceId-30", 
        							"l-interface-name", "lInterfaceName-3", "interface-role", "CUSTOMER-UPLINK");
        
        Vertex logicalLink1 = graph.addVertex(T.label, "logical-link", T.id, "4", "aai-node-type", "logical-link", "link-name", "linkName-1", "in-maint", "false", "link-type","linkType-1");

        //  1 generic-vnf with 2 lags each has 1 linterface
        Vertex genericvnf1 = graph.addVertex(T.label, "generic-vnf", T.id, "5", "aai-node-type", "generic-vnf", "vnf-id", "vnfId-1","vnf-name", "vnfName-1");
        Vertex lagint1 = graph.addVertex(T.label, "lag-interface", T.id, "6", "aai-node-type", "lag-interface","interface-name", "lagIntName-1");
		Vertex linterface1 = graph.addVertex(T.label, "l-interface", T.id, "7", "aai-node-type", "l-interface", "l-interface-id", "lInterfaceId-1", 
										"l-interface-name", "lInterfaceName-1", "interface-role", "CUSTOMER");

		Vertex linterface2 = graph.addVertex(T.label, "l-interface", T.id, "9", "aai-node-type", "l-interface", "l-interface-id", "lInterfaceId-2", 
										"l-interface-name", "lInterfaceName-2", "interface-role", "CUSTOMER-UPLINK");
		Vertex linterface3 = graph.addVertex(T.label, "l-interface", T.id, "10", "aai-node-type", "l-interface", "l-interface-id", "lInterfaceId-3", 
										"l-interface-name", "lInterfaceName-3", "interface-role", "UPLINK");
		Vertex linterface4 = graph.addVertex(T.label, "l-interface", T.id, "11", "aai-node-type", "l-interface", "l-interface-id", "lInterfaceId-4", "l-interface-name", "lInterfaceName-4", "interface-role", "CUSTOMER-UPLINK");
		
		Vertex lagint2 = graph.addVertex(T.label, "lag-interface", T.id, "8", "aai-node-type", "lag-interface","interface-name", "lagIntName-2");
		Vertex linterface21 = graph.addVertex(T.label, "l-interface", T.id, "20", "aai-node-type", "l-interface", "l-interface-id", "lInterfaceId-21", 
				"l-interface-name", "lInterfaceName-2", "interface-role", "CUSTOMER-UPLINK");
		
		Vertex genericvnf2 = graph.addVertex(T.label, "generic-vnf", T.id, "12", "aai-node-type", "generic-vnf", "vnf-id", "vnfId-2","vnf-name", "vnfName-2");
		Vertex linterface31 = graph.addVertex(T.label, "l-interface", T.id, "13", "aai-node-type", "l-interface", "l-interface-id", "lInterfaceId-21", "l-interface-name", "lInterfaceName-21", "interface-role", "CUSTOMER-UPLINK");
		Vertex lagint3 = graph.addVertex(T.label, "lag-interface", T.id, "14", "aai-node-type", "lag-interface","interface-name", "lagIntName-22");
		
		
        GraphTraversalSource g = graph.traversal();
        rules.addTreeEdge(g, newvce, linterfaceNewvce1);   	// true
        rules.addTreeEdge(g, newvce, linterfaceNewvce2);
        rules.addTreeEdge(g, newvce, linterfaceNewvce3);        
        rules.addEdge(g, linterfaceNewvce1, logicalLink1);   // true
        rules.addEdge(g, linterfaceNewvce2, logicalLink1);     
                                           
        rules.addTreeEdge(g, genericvnf1, lagint1);
        rules.addTreeEdge(g, lagint1, linterface1);

        rules.addTreeEdge(g, lagint1, linterface2);		// true
        rules.addTreeEdge(g, lagint1, linterface3);		
        rules.addTreeEdge(g, lagint1, linterface4);		// true
        rules.addTreeEdge(g, lagint2, linterface21);
        
        rules.addEdge(g, linterface2, logicalLink1);   // true
        rules.addEdge(g, linterface4, logicalLink1);   // true
        rules.addEdge(g, linterface21, logicalLink1);   // true
        
        rules.addTreeEdge(g, genericvnf2, lagint3);
        rules.addTreeEdge(g, lagint2, linterface31);

        
        expectedResult.add(linterface2);
        expectedResult.add(linterface4);
        expectedResult.add(linterface21);
       
    }

    @Override
    protected String getQueryName() {
        return "getLinterface-fromNewvce";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "newvce").has("vnf-id2", "vnfId2-1");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
    	params.put("interfaceRole1", "UPLINK");
    	params.put("interfaceRole2", "CUSTOMER-UPLINK");
    }
}