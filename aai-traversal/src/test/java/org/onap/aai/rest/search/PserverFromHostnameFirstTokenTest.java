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


import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class PserverFromHostnameFirstTokenTest extends QueryTest {

	public PserverFromHostnameFirstTokenTest() throws AAIException, NoEdgeRuleFoundException {
		super();
	}

	@Test
	public void test() {
		super.run();
	}

	@Override
	protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
		//Set up the test graph
		Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "1", "aai-node-type", "pserver", "hostname", "hostname-1.abc.com", "source-of-truth", "RO");		
		Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "5", "aai-node-type", "pserver", "hostname", "hostname-2.abc.com", "source-of-truth", "RO");
		Vertex pserver3 = graph.addVertex(T.label, "pserver", T.id, "6", "aai-node-type", "pserver", "hostname", "hostname-13.abc.com", "source-of-truth", "AAI-EXTENSIONS");
		Vertex pserver4 = graph.addVertex(T.label, "pserver", T.id, "7", "aai-node-type", "pserver", "hostname", "hostname-12.abc.com", "source-of-truth", "RCT");
		Vertex pserver5 = graph.addVertex(T.label, "pserver", T.id, "8", "aai-node-type", "pserver", "hostname", "hostname-20.abc.com", "source-of-truth", "RO");

		
		expectedResult.add(pserver1);
		expectedResult.add(pserver3);

	}

	@Override
	protected String getQueryName() {
		return "pserver-fromHostnameFirstToken";
	}

	@Override
	protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
		g.has("aai-node-type","pserver");
	}

	@Override
	protected void addParam(Map<String, Object> params) {
		params.put("hostnameFirstToken", "hostname-1");
		params.put("sourcesOfTruth", "RO', 'AAI-EXTENSIONS");  //placement of single quotes is intentional, values between the first and last values must be in single quotes
		
	}

}
