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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.introspection.Version;
import org.onap.aai.query.builder.GremlinTraversal;
import org.onap.aai.serialization.db.EdgeRules;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;

public abstract class QueryTest {

	private EELFLogger logger;
	protected Graph graph;
	private GremlinServerSingleton gremlinServerSingleton;
	private GremlinGroovyShellSingleton shell;
	@Mock private TransactionalGraphEngine dbEngine;
	protected final List<Vertex> expectedResult = new ArrayList<>();
	//expectedResultForMaps is for when the query returns a HashMap, not a Vertex
	protected String expectedResultForMaps = new String();
	protected final EdgeRules rules = EdgeRules.getInstance();
	protected Loader loader;
	
	public QueryTest() throws AAIException, NoEdgeRuleFoundException {
		setUp();
		logger = EELFManager.getInstance().getLogger(getClass());
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
		this.run(false);
	}
	
	public void run(boolean isHashMap) {
		
		String query = gremlinServerSingleton.getStoredQueryFromConfig(getQueryName());
		Map<String, Object> params = new HashMap<>();
		addParam(params);
		when(dbEngine.getQueryBuilder(any(QueryStyle.class))).thenReturn(new GremlinTraversal<>(loader, graph.traversal()));
		logger.info("Stored query in abstraction form {}", query);
		query = GroovyQueryBuilderSingleton.getInstance().executeTraversal(dbEngine, query, params);
		logger.info("After converting to gremlin query {}", query);
		query = "g" + query;
		GraphTraversal<Vertex, Vertex> g = graph.traversal().V();
		addStartNode(g);
		params.put("g", g);
		
		//Certain custom queries return HashMaps instead of Vertex; different code must used for both cases to avoid a ClassCastException
		if(!isHashMap) {
			GraphTraversal<Vertex, Vertex> result = (GraphTraversal<Vertex, Vertex>)shell.executeTraversal(query, params);
			
			List<Vertex> vertices = result.toList();

			logger.info("Expected result set of vertexes [{}]", convert(expectedResult));
			logger.info("Actual Result set of vertexes [{}]", convert(vertices));

			List<Vertex> nonDuplicateExpectedResult = new ArrayList<>(new HashSet<>(expectedResult));
			vertices = new ArrayList<>(new HashSet<>(vertices));

			nonDuplicateExpectedResult.sort(Comparator.comparing(vertex -> vertex.id().toString()));
			vertices.sort(Comparator.comparing(vertex -> vertex.id().toString()));

			// Use this instead of the assertTrue as this provides more useful
			// debugging information such as this when expected and actual differ:
			// java.lang.AssertionError: Expected all the vertices to be found
			// Expected :[v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11], v[12]]
			// Actual   :[v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11], v[12]]
			assertEquals("Expected all the vertices to be found", nonDuplicateExpectedResult, vertices);
		}
		else {
			GraphTraversal<HashMap<String,Long>, HashMap<String,Long>> result = (GraphTraversal<HashMap<String,Long>, HashMap<String,Long>>)shell.executeTraversal(query, params);
			
			String map = result.toList().toString();
			System.out.println(map);
			assertTrue("all hash maps found", map.equals(expectedResultForMaps) && expectedResultForMaps.equals(map));			
		}
	}

	private String convert(List<Vertex> vertices){
		return vertices
				.stream()
				.map(vertex -> vertex.property("aai-node-type").value().toString())
				.collect(Collectors.joining(","));
	}

	protected abstract void createGraph() throws AAIException, NoEdgeRuleFoundException;
		
	protected abstract String getQueryName();
	
	protected abstract void addStartNode(GraphTraversal<Vertex, Vertex> g);
	
	protected abstract void addParam(Map<String, Object> params);
}
