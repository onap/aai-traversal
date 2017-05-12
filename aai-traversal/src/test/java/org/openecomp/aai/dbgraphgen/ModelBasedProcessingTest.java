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

package org.openecomp.aai.dbgraphgen;
//package org.openecomp.aai.dbgen;
//
//import java.util.ArrayList;
//
//import org.junit.BeforeClass;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//
//import org.openecomp.aai.exceptions.AAIException;
//import org.openecomp.aai.ingestModel.DbMaps;
//import org.openecomp.aai.ingestModel.IngestModelMoxyOxm;
//import org.openecomp.aai.util.AAIConstants;
//
//public class ModelBasedProcessingTest {
//	
//	private static DbMaps dbMaps = null;
//	private static ModelBasedProcessing processor;
//	@BeforeClass
//	public static void configure() throws Exception {
//		System.setProperty("AJSC_HOME", ".");
//		System.setProperty("BUNDLECONFIG_DIR", "bundleconfig-local");
//		ArrayList<String> apiVersions = new ArrayList<String>();
//		apiVersions.add("v9");
//		apiVersions.add("v8");
//		apiVersions.add("v7");
//		apiVersions.add("v2");
//		IngestModelMoxyOxm m = new IngestModelMoxyOxm();
//		m.init(apiVersions);
//		
//		dbMaps = m.dbMapsContainer.get(AAIConstants.AAI_DEFAULT_API_VERSION);
//		processor = new ModelBasedProcessing();
//		
//	}
//	
//	@Rule
//	public ExpectedException expectedEx = ExpectedException.none();
//
//	@Test
//	public void check4EdgeRuleThrowsExceptionWhenNodeTypeADoesNotExist() throws Exception {
//		String nodeTypeA = "cccomplex";
//		String nodeTypeB = "pserver";
//	    expectedEx.expect(AAIException.class);
//	    expectedEx.expectMessage("AAI_6115");
//	    processor.check4EdgeRule(nodeTypeA, nodeTypeB, dbMaps);    
//	}
//	
//	@Test
//	public void check4EdgeRuleThrowsExceptionWhenNodeTypeBDoesNotExist() throws Exception {
//		String nodeTypeA = "complex";
//		String nodeTypeB = "ppppserver";
//	    expectedEx.expect(AAIException.class);
//	    expectedEx.expectMessage("AAI_6115");
//	    processor.check4EdgeRule(nodeTypeA, nodeTypeB, dbMaps);    
//	}
//	
//	@Test
//	public void check4EdgeRuleThrowsExceptionWhenNoRuleExists() throws Exception {
//		String nodeTypeA = "complex";
//		String nodeTypeB = "service";
//		expectedEx.expect(AAIException.class);
//	    expectedEx.expectMessage("AAI_6120");
//	    processor.check4EdgeRule(nodeTypeA, nodeTypeB, dbMaps);    
//	}
//}
