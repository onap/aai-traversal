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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.aai.dbmap.DBConnectionType;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.LoaderFactory;
import org.openecomp.aai.introspection.ModelType;
import org.openecomp.aai.introspection.Version;
import org.openecomp.aai.serialization.db.DBSerializer;
import org.openecomp.aai.serialization.db.EdgeRules;
import org.openecomp.aai.serialization.db.exceptions.NoEdgeRuleFoundException;
import org.openecomp.aai.serialization.engines.QueryStyle;
import org.openecomp.aai.serialization.engines.TitanDBEngine;
import org.openecomp.aai.serialization.engines.TransactionalGraphEngine;
import org.openecomp.aai.serialization.queryformats.RawFormat;
import org.openecomp.aai.serialization.queryformats.exceptions.AAIFormatVertexException;
import org.openecomp.aai.serialization.queryformats.utils.UrlBuilder;

import com.google.gson.JsonObject;

public class SimpleFormatTest {

	protected Graph graph;
	private TransactionalGraphEngine dbEngine;

	protected final List<Vertex> expectedResult = new ArrayList<>();
	protected final EdgeRules rules = EdgeRules.getInstance();
	protected Loader loader;
	private DBSerializer serializer;

	@Mock
	private UrlBuilder urlBuilder;
	private RawFormat _simpleFormat;

	Vertex vfmodule = null;

	public SimpleFormatTest() throws AAIException, NoEdgeRuleFoundException {
		setUp();
	}

	public void setUp() throws AAIException, NoEdgeRuleFoundException {
		System.setProperty("AJSC_HOME", ".");
		System.setProperty("BUNDLECONFIG_DIR", "bundleconfig-local");
		MockitoAnnotations.initMocks(this);
		graph = TinkerGraph.open();
		loader = LoaderFactory.createLoaderForVersion(ModelType.MOXY, Version.v10);
		vfmodule = graph.addVertex(T.label, "vf-module",
				T.id, "5",
				"aai-node-type", "vf-module",
				"vf-module-id", "vf-module-id-val-68205",
				"vf-module-name", "example-vf-module-name-val-68205",
				"heat-stack-id", "example-heat-stack-id-val-68205",
				"orchestration-status", "example-orchestration-status-val-68205",
				"is-base-vf-module", "true",
				"resource-version", "1498166571906",
				"model-invariant-id", "fe8aac07-ce6c-4f9f-aa0d-b561c77da9e8",
				"model-invariant-id-local", "fe8aac07-ce6c-4f9f-aa0d-b561c77da9e8",
				"model-version-id", "0d23052d-8ffe-433e-a25d-da5da027bb7c",
				"model-version-id-local", "0d23052d-8ffe-433e-a25d-da5da027bb7c",
				"widget-model-id", "example-widget-model-id-val-68205",
				"widget-model-version", "example-widget--model-version-val-68205",
				"contrail-service-instance-fqdn", "example-contrail-service-instance-fqdn-val-68205");

		final ModelType factoryType = ModelType.MOXY;
		Loader loader = LoaderFactory.createLoaderForVersion(factoryType, Version.v10);
		dbEngine = spy(new TitanDBEngine(
				QueryStyle.TRAVERSAL,
				DBConnectionType.CACHED,
				loader));

		when(dbEngine.tx()).thenReturn(graph);
		TransactionalGraphEngine.Admin spyAdmin = spy(dbEngine.asAdmin());
		when(spyAdmin.getTraversalSource()).thenReturn(graph.traversal());
		when(dbEngine.asAdmin()).thenReturn(spyAdmin);
		serializer = new DBSerializer(Version.v10, dbEngine, factoryType, "Junit");
		_simpleFormat = new RawFormat.Builder(loader, serializer, urlBuilder).modelDriven().build();
		dbEngine.startTransaction();
	}

	@Test
	public void run() throws AAIFormatVertexException {
		assertNotNull(dbEngine.tx());
		System.out.println(dbEngine.tx());
		assertNotNull(graph.traversal());
		JsonObject json = _simpleFormat.createPropertiesObject(vfmodule);
		json.entrySet().stream().forEach((System.out::println));
		assertTrue(json.has("model-invariant-id"));

	}

}
