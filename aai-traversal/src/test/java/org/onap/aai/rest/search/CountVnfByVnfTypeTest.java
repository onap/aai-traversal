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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class CountVnfByVnfTypeTest extends QueryTest {

	public CountVnfByVnfTypeTest() throws AAIException, NoEdgeRuleFoundException {
		super();
    }

    @Test
    public void test() {
    	super.run(true);
    }

    @Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//Set up the test graph
	    Vertex genericVnfTypeA1 = graph.addVertex(T.label, "genric-vnf", T.id, "1", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-1", "vnf-name", "vnf-name-1", "vnf-type", "A");
	    Vertex genericVnfTypeB1 = graph.addVertex(T.label, "genric-vnf", T.id, "2", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-2", "vnf-name", "vnf-name-2", "vnf-type", "B");
	    Vertex genericVnfTypeC1 = graph.addVertex(T.label, "genric-vnf", T.id, "3", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-3", "vnf-name", "vnf-name-3", "vnf-type", "C");
	    Vertex genericVnfTypeA2 = graph.addVertex(T.label, "genric-vnf", T.id, "4", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-4", "vnf-name", "vnf-name-4", "vnf-type", "A");
	    Vertex genericVnfTypeB2 = graph.addVertex(T.label, "genric-vnf", T.id, "5", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-5", "vnf-name", "vnf-name-5", "vnf-type", "B");
	    Vertex genericVnfTypeA3 = graph.addVertex(T.label, "genric-vnf", T.id, "6", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-6", "vnf-name", "vnf-name-6", "vnf-type", "A");
	    Vertex genericVnfTypeA4 = graph.addVertex(T.label, "genric-vnf", T.id, "7", "aai-node-type", "generic-vnf", "vnf-id", "vnf-id-7", "vnf-name", "vnf-name-7", "vnf-type", "A");
	    
	    GraphTraversalSource g = graph.traversal();
	    
	    expectedResultForMaps = expectedResultForMaps + "[A=4, B=2, C=1]";
	}

    @Override
	protected String getQueryName() {
		return "count-vnf-byVnfType";
	}


    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
    	return;
    }

    @Override
    protected void addParam(Map<String, Object> params) {
    	return;
    }

}
