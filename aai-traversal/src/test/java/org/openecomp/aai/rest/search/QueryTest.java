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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.LoaderFactory;
import org.openecomp.aai.introspection.ModelType;
import org.openecomp.aai.introspection.Version;
import org.openecomp.aai.query.builder.GremlinTraversal;
import org.openecomp.aai.serialization.db.EdgeRules;
import org.openecomp.aai.serialization.db.exceptions.NoEdgeRuleFoundException;
import org.openecomp.aai.serialization.engines.QueryStyle;
import org.openecomp.aai.serialization.engines.TransactionalGraphEngine;

public abstract class QueryTest {
	
	protected Graph graph;
	private GremlinServerSingleton gremlinServerSingleton;
	private GremlinGroovyShellSingleton shell;
	@Mock private TransactionalGraphEngine dbEngine;
	protected final List<Vertex> expectedResult = new ArrayList<>();
	protected final EdgeRules rules = EdgeRules.getInstance();
	protected Loader loader;
	
	public QueryTest() throws AAIException, NoEdgeRuleFoundException {
		setUp();
	}
	public void setUp() throws AAIException, NoEdgeRuleFoundException {
		System.setProperty("AJSC_HOME", ".");
		System.setProperty("BUNDLECONFIG_DIR", "bundleconfig-local");
		MockitoAnnotations.initMocks(this);
		graph = TinkerGraph.open();
		createGraph();
		gremlinServerSingleton = GremlinServerSingleton.getInstance();
		shell = GremlinGroovyShellSingleton.getInstance();
		loader = LoaderFactory.createLoaderForVersion(ModelType.MOXY, Version.getLatest());
	}
	
	public void run() {
		
		String query = gremlinServerSingleton.getStoredQuery(getQueryName());
		Map<String, Object> params = new HashMap<>();
		addParam(params);
		when(dbEngine.getQueryBuilder(any(QueryStyle.class))).thenReturn(new GremlinTraversal<>(loader, graph.traversal()));
		query = GroovyQueryBuilderSingleton.getInstance().executeTraversal(dbEngine, query, params);
		query = "g" + query;
		GraphTraversal<Vertex, Vertex> g = graph.traversal().V();
		addStartNode(g);
		params.put("g", g);
		GraphTraversal<Vertex, Vertex> result = (GraphTraversal<Vertex, Vertex>)shell.executeTraversal(query, params);
		
		List<Vertex> vertices = result.toList();
		assertTrue("all vertices found", vertices.containsAll(expectedResult) && expectedResult.containsAll(vertices));

	}
	
	protected abstract void createGraph() throws AAIException, NoEdgeRuleFoundException;
		
	protected abstract String getQueryName();
	
	protected abstract void addStartNode(GraphTraversal<Vertex, Vertex> g);
	
	protected abstract void addParam(Map<String, Object> params);
}
