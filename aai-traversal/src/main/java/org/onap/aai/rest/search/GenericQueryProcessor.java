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

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.javatuples.Pair;
import org.onap.aai.restcore.util.URITools;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.SubGraphStyle;

import jersey.repackaged.com.google.common.base.Joiner;

public abstract class GenericQueryProcessor {

	protected final Optional<URI> uri;
	protected final MultivaluedMap<String, String> queryParams;
	protected final Optional<Collection<Vertex>> vertices;
	protected static Pattern p = Pattern.compile("query/(.*+)");
	protected Optional<String> gremlin;
	protected final TransactionalGraphEngine dbEngine;
	protected static GremlinServerSingleton gremlinServerSingleton = GremlinServerSingleton.getInstance();
	protected static GroovyQueryBuilderSingleton queryBuilderSingleton = GroovyQueryBuilderSingleton.getInstance();
	protected final boolean isGremlin;
	
	protected GenericQueryProcessor(Builder builder) {
		this.uri = builder.getUri();
		this.dbEngine = builder.getDbEngine();
		this.vertices = builder.getVertices();
		this.gremlin = builder.getGremlin();
		this.isGremlin = builder.isGremlin();
		if (uri.isPresent()) {
			queryParams = URITools.getQueryMap(uri.get());
		} else {
			queryParams = new MultivaluedHashMap<>();
		}
	}
	
	protected abstract GraphTraversal<?,?> runQuery(String query, Map<String, Object> params);
	
	protected List<Object> processSubGraph(SubGraphStyle style, GraphTraversal<?,?> g) {
		final List<Object> resultVertices = new Vector<>();
		g.store("x");
		
		if (SubGraphStyle.prune.equals(style) || SubGraphStyle.star.equals(style)) {
			g.barrier().bothE();
			if (SubGraphStyle.prune.equals(style)) {
				g.where(__.otherV().where(P.within("x")));
			}
			g.dedup().subgraph("subGraph").cap("subGraph").map(x -> (Graph)x.get()).next().traversal().V().forEachRemaining(x -> {
				resultVertices.add(x);
			});
		} else {
			resultVertices.addAll(g.toList());
		}
		return resultVertices;
	}
	
	public List<Object> execute(SubGraphStyle style) throws FileNotFoundException {
		final List<Object> resultVertices;

		Pair<String, Map<String, Object>> tuple = this.createQuery();
		String query = tuple.getValue0();
		Map<String, Object> params = tuple.getValue1();

		if (query.equals("") && (vertices.isPresent() && vertices.get().isEmpty())) {
			//nothing to do, just exit
			return new ArrayList<>();
		}
		GraphTraversal<?,?> g = this.runQuery(query, params);
		
		resultVertices = this.processSubGraph(style, g);
		
		return resultVertices;
	}
	
	protected Pair<String, Map<String, Object>> createQuery() {
		Map<String, Object> params = new HashMap<>();
		String query = "";
		if (!this.isGremlin) {
			Matcher m = p.matcher(uri.get().getPath());
			String queryName = "";
			if (m.find()) {
				queryName = m.group(1);
			}
		
			for (String key : queryParams.keySet()) {
				params.put(key, queryParams.getFirst(key));
			}
			
			query = gremlinServerSingleton.getStoredQuery(queryName);
			if (query == null) {
				query = "";
			} else {
				query = queryBuilderSingleton.executeTraversal(dbEngine, query, params);
			}
			
			
			List<Object> ids = new ArrayList<>();
			
			if (vertices.isPresent() && !vertices.get().isEmpty()) {
				for (Vertex v : vertices.get()) {
					ids.add(v.id());
				}
				StringBuilder sb = new StringBuilder();
				sb.append("[");
				sb.append(Joiner.on(",").join(ids));
				sb.append("]");
				String startPrefix = "aaiStartQuery = " + sb.toString() + " as Object[];g.V(aaiStartQuery)";
				if (!"".equals(query)) {
					query = startPrefix + query;
				} else {
					query = startPrefix;
				}
			}
			
		} else {
			query = gremlin.get();
		}
		
		return new Pair<>(query, params);
	}
	
	public static class Builder {

		private final TransactionalGraphEngine dbEngine;
		private Optional<URI> uri = Optional.empty();
		private Optional<String> gremlin = Optional.empty();
		private boolean isGremlin = false;
		private Optional<Collection<Vertex>> vertices = Optional.empty();
		private QueryProcessorType processorType = QueryProcessorType.GREMLIN_SERVER;
		
		public Builder(TransactionalGraphEngine dbEngine) {
			this.dbEngine = dbEngine;
		}
		
		public Builder queryFrom(URI uri) {
			this.uri = Optional.of(uri);
			this.isGremlin = false;
			return this;
		}
		
		public Builder startFrom(Collection<Vertex> vertices) {
			this.vertices = Optional.of(vertices);
			return this;
		}
		
		public Builder queryFrom(String gremlin) {
			this.gremlin = Optional.of(gremlin);
			this.isGremlin = true;
			return this;
		}
		
		public Builder processWith(QueryProcessorType type) {
			this.processorType = type;
			return this;
		}
		public TransactionalGraphEngine getDbEngine() {
			return dbEngine;
		}

		public Optional<URI> getUri() {
			return uri;
		}

		public Optional<String> getGremlin() {
			return gremlin;
		}

		public boolean isGremlin() {
			return isGremlin;
		}

		public Optional<Collection<Vertex>> getVertices() {
			return vertices;
		}
		
		public QueryProcessorType getProcessorType() {
			return processorType;
		}
		
		public GenericQueryProcessor create() {
			
			if (this.getProcessorType().equals(QueryProcessorType.GREMLIN_SERVER)) {
				return new GremlinServerImpl(this);
			} else if (this.getProcessorType().equals(QueryProcessorType.LOCAL_GROOVY)) {
				return new GroovyShellImpl(this);
			} else {
				return new GremlinServerImpl(this);
			}
		}
		
	}
}
