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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.aai.db.props.AAIProperties;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.LoaderFactory;
import org.openecomp.aai.introspection.ModelType;
import org.openecomp.aai.introspection.exceptions.AAIUnknownObjectException;
import org.openecomp.aai.serialization.db.DBSerializer;
import org.openecomp.aai.serialization.engines.TransactionalGraphEngine;

public class ModelBasedProcessingTest {
	
	@Mock private static TransactionalGraphEngine dbEngine;
	private static Loader loader;
	@Mock private static DBSerializer serializer;
	@BeforeClass
	public static void configure() throws Exception {
		System.setProperty("AJSC_HOME", ".");
		System.setProperty("BUNDLECONFIG_DIR", "bundleconfig-local");
		loader = LoaderFactory.createLoaderForVersion(ModelType.MOXY, AAIProperties.LATEST);
		
	}
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testPropNameChange() throws AAIUnknownObjectException {
		String result;
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		result = processor.getPropNameWithAliasIfNeeded("generic-vnf", "model-invariant-id");
		assertEquals("result has -local tacked on the end as it should", "model-invariant-id" + AAIProperties.DB_ALIAS_SUFFIX, result);
		result = processor.getPropNameWithAliasIfNeeded("generic-vnf", "vnf-id");
		assertEquals("result does NOT have -local tacked on the end as it should", "vnf-id", result);
		result = processor.getPropNameWithAliasIfNeeded("generic-vnf", "model-invariant-id" + AAIProperties.DB_ALIAS_SUFFIX);
		assertEquals("property not modified because it already includes the right suffix", "model-invariant-id" + AAIProperties.DB_ALIAS_SUFFIX, result);
	}
}
