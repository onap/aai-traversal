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
package org.onap.aai.rest;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.dbmap.DBConnectionType;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.search.CustomQueryTestDTO;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.restcore.search.GremlinGroovyShell;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.serialization.db.EdgeSerializer;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;


@Path("/cq2gremlintest")
public class CQ2GremlinTest extends RESTAPI {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(CQ2GremlinTest.class);

	private HttpEntry traversalUriHttpEntry;


	@Autowired
	protected LoaderFactory loaderFactory;

	@Autowired
	protected EdgeSerializer rules;

	protected Loader loader;
	protected GraphTraversalSource gts;


	@Autowired
	public CQ2GremlinTest(
			HttpEntry traversalUriHttpEntry,
			@Value("${schema.uri.base.path}") String basePath
			){
		this.traversalUriHttpEntry  = traversalUriHttpEntry;

	}

	@PUT
	@Path("")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getC2Qgremlin(@RequestBody CustomQueryTestDTO content,@Context HttpHeaders headers, @Context UriInfo info) throws AAIException {
		if(content == null){
			return Response.status(HttpStatus.BAD_REQUEST.value()).entity("At least one Json payload should be passed").build();
		}
		String sourceOfTruth = headers.getRequestHeaders().getFirst("X-FromAppId");
		String realTime = headers.getRequestHeaders().getFirst("Real-Time");
		SchemaVersions schemaVersions = SpringContextAware.getBean(SchemaVersions.class);
		DBConnectionType type = this.determineConnectionType(sourceOfTruth, realTime);
		traversalUriHttpEntry.setHttpEntryProperties(schemaVersions.getDefaultVersion(), type);
		traversalUriHttpEntry.setPaginationParameters("-1", "-1");
		return processC2UnitTest(content);
	}

	private Response processC2UnitTest(CustomQueryTestDTO content) {

		TransactionalGraphEngine dbEngine = traversalUriHttpEntry.getDbEngine();
		Graph graph = TinkerGraph.open();
		gts = graph.traversal();
		List<Vertex> expectedVertices = createGraph(content, graph);
		GremlinGroovyShell shell = new GremlinGroovyShell();
		loader = loaderFactory.createLoaderForVersion(ModelType.MOXY, new SchemaVersion("v16"));
		LinkedHashMap <String, Object> params = new LinkedHashMap<>();

		//Adding parameters
		content.getQueryRequiredProperties().forEach((K, V) -> {params.put(K, V);});
		content.getQueryOptionalProperties().forEach((K, V) -> {params.put(K, V);});

		String query = new GroovyQueryBuilder().executeTraversal(dbEngine, content.getStoredQuery(), params);
		query = "g" + query;
		GraphTraversal<Vertex, Vertex> g = graph.traversal().V();
		addStartNode(g, content);
		params.put("g", g);

		//Assertion
		GraphTraversal<Vertex, Vertex> result = (GraphTraversal<Vertex, Vertex>)shell.executeTraversal(query, params);

		List<Vertex> vertices = result.toList();

		LOGGER.info("Expected result set of vertexes [{}]", convert(expectedVertices));
		LOGGER.info("Actual Result set of vertexes [{}]", convert(vertices));

		List<Vertex> nonDuplicateExpectedResult = new ArrayList<>(new HashSet<>(expectedVertices));
		vertices = new ArrayList<>(new HashSet<>(vertices));

		nonDuplicateExpectedResult.sort(Comparator.comparing(vertex -> vertex.id().toString()));
		vertices.sort(Comparator.comparing(vertex -> vertex.id().toString()));


		// Use this instead of the assertTrue as this provides more useful
		// debugging information such as this when expected and actual differ:
		// java.lang.AssertionError: Expected all the vertices to be found
		// Expected :[v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11], v[12]]
		// Actual   :[v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11], v[12]]
		if(nonDuplicateExpectedResult.equals(vertices)){
			return Response.ok("Sucessfully executed Junit").build();
		}
		return Response.status(400).build();

	}

	private List<Vertex> createGraph(CustomQueryTestDTO content, Graph graph) {
		Map<String, Vertex> verticesMap = Maps.newLinkedHashMap();
		//Creating all the Vertices
		content.getVerticesDtos().stream().forEach(vertex -> {
			StringBuilder vertexIdentifier = new StringBuilder();
			List<String> keyValues = Lists.newArrayList();
			keyValues.add(T.id.toString());
			keyValues.add(String.format("%02d", verticesMap.size() * 10));
			AtomicInteger index = new AtomicInteger(0);
			vertex.forEach((K, V) -> {
				if(index.get() == 1)
					vertexIdentifier.append(V);
				keyValues.add(K);
				keyValues.add(V);
				index.incrementAndGet();
			});
			Vertex graphVertex = graph.addVertex(keyValues.toArray());
			verticesMap.put(vertexIdentifier.toString(), graphVertex);
		});

		GraphTraversalSource g = graph.traversal();

		//Creating all the Edges
		content.getEdgesDtos().stream().forEach(edge -> {
			String fromId = edge.get("from-id");
			String toId = edge.get("to-id");
			boolean treeEdgeIdentifier = !"NONE".equalsIgnoreCase(edge.get("contains-other-v"));
			Vertex fromVertex = verticesMap.get(fromId);
			Vertex toVertex = verticesMap.get(toId);
			try{
				if(treeEdgeIdentifier){
					rules.addTreeEdge(g, fromVertex, toVertex);
				}
				else{
					rules.addEdge(g, fromVertex, toVertex);
				}
			} catch(AAIException ex){
				LOGGER.warn(ex.toString(), ex);
			}

		});


		List<Vertex> expectedVertices = Lists.newArrayList();
		content.getExpectedResultsDtos().getIds().stream().forEach(vertexId -> {
			expectedVertices.add(verticesMap.get(vertexId));
		});
		return expectedVertices;
	}

	protected void addStartNode(GraphTraversal<Vertex, Vertex> g, CustomQueryTestDTO content) {
		Optional<LinkedHashMap<String, String>> startNodeVertex = content.getVerticesDtos().stream().filter(map -> map.containsKey("start-node")).findFirst();
		if(!startNodeVertex.isPresent()){
			throw new IllegalArgumentException("start-node was not specified");
		}
		startNodeVertex.get().forEach((K, V) -> {
			g.has(K, V);
		});
	}

	protected String convert(List<Vertex> vertices){
		return vertices
				.stream()
				.map(vertex -> vertex.property("aai-node-type").value().toString())
				.collect(Collectors.joining(","));
	}



}
