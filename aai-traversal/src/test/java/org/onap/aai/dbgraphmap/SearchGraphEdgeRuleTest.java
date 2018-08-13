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
package org.onap.aai.dbgraphmap;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.exceptions.AAIException;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore
public class SearchGraphEdgeRuleTest {
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();
	
	@Autowired
	SearchGraph searchGraph;
	@Test
	public void getEdgeLabelTest() throws AAIException, EdgeRuleNotFoundException {
		String[] label = searchGraph.getEdgeLabel("customer", "service-subscription");
		
		assertEquals("subscribesTo", label[0]);
	}
	
	@Test
	public void getEdgeLabelThrowsExceptionWhenNoRuleExists() throws Exception {
		String nodeTypeA = "complex";
		String nodeTypeB = "service";
		expectedEx.expect(AAIException.class);
		expectedEx.expectMessage("No EdgeRule found for passed nodeTypes: complex, service.");
	    searchGraph.getEdgeLabel(nodeTypeA, nodeTypeB);
	}
	
	@Test
	public void getEdgeLabelThrowsExceptionWhenNodeTypesDoNotExist() throws Exception {
		String nodeTypeA = "A";
		String nodeTypeB = "B";
		expectedEx.expect(AAIException.class);
	    expectedEx.expectMessage("No EdgeRule found for passed nodeTypes: A, B.");
	    searchGraph.getEdgeLabel(nodeTypeA, nodeTypeB);    
	}
}
