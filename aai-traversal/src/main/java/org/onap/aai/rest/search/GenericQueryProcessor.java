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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.search;

import java.io.FileNotFoundException;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.javatuples.Pair;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.query.builder.MissingOptionalParameter;
import org.onap.aai.rest.dsl.DslQueryProcessor;
import org.onap.aai.rest.enums.QueryVersion;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.restcore.util.URITools;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericQueryProcessor {

    private static Logger LOGGER = LoggerFactory.getLogger(GenericQueryProcessor.class);

    protected final Optional<URI> uri;
    protected final MultivaluedMap<String, String> queryParams;
    protected final Optional<Collection<Vertex>> vertices;
    protected static Pattern p = Pattern.compile("query/(.*+)");
    protected Optional<String> gremlin;
    protected final TransactionalGraphEngine dbEngine;
    protected GremlinServerSingleton gremlinServerSingleton;
    protected GroovyQueryBuilder groovyQueryBuilder = new GroovyQueryBuilder();
    protected final boolean isGremlin;
    protected Optional<DslQueryProcessor> dslQueryProcessorOptional;

    public Map<String, List<String>> getPropertiesMap() {
        return propertiesList;
    }

    public void setPropertiesMap(Map<String, List<String>> propertiesMap) {
        this.propertiesList = propertiesMap;
    }

    private Map<String, List<String>> propertiesList;
    /*
     * dsl parameters to store dsl query and to check
     * 
     * if this is a DSL request
     */
    protected Optional<String> dsl;
    protected final boolean isDsl;
    protected boolean isHistory;
    protected GraphTraversalSource traversalSource;
    protected QueryStyle style;
    protected QueryVersion dslApiVersion;
    protected Format format;

    protected GenericQueryProcessor(Builder builder) {
        this.uri = builder.getUri();
        this.dbEngine = builder.getDbEngine();
        this.vertices = builder.getVertices();
        this.gremlin = builder.getGremlin();
        this.isGremlin = builder.isGremlin();
        this.dsl = builder.getDsl();
        this.isDsl = builder.isDsl();
        this.gremlinServerSingleton = builder.getGremlinServerSingleton();
        this.dslQueryProcessorOptional = builder.getDslQueryProcessor();
        this.dslApiVersion = builder.getDslApiVersion();

        if (uri.isPresent()) {
            queryParams = URITools.getQueryMap(uri.get());
        } else if (builder.getUriParams() != null) {
            queryParams = builder.getUriParams();
        } else {
            queryParams = new MultivaluedHashMap<>();
        }
        this.traversalSource = builder.getTraversalSource();
        this.style = builder.getStyle();
        this.isHistory = builder.isHistory();
        this.format = builder.getFormat();
    }

    protected abstract GraphTraversal<?, ?> runQuery(String query, Map<String, Object> params,
        GraphTraversalSource traversalSource);

    protected List<Object> processSubGraph(SubGraphStyle style, GraphTraversal<?, ?> g) {
        final List<Object> resultVertices = new Vector<>();
        g.store("y");

        if (SubGraphStyle.prune.equals(style) || SubGraphStyle.star.equals(style)) {
            g.barrier().bothE();
            if (SubGraphStyle.prune.equals(style)) {
                g.where(__.otherV().where(P.within("y")));
            }
            g.dedup().subgraph("subGraph").cap("subGraph").map(x -> (Graph) x.get()).next()
                .traversal().V().forEachRemaining(x -> {
                    resultVertices.add(x);
                });
        } else {
            resultVertices.addAll(g.toList());
        }
        return resultVertices;
    }

    public List<Object> execute(SubGraphStyle style) throws FileNotFoundException, AAIException {
        final List<Object> resultVertices;

        Pair<String, Map<String, Object>> tuple = this.createQuery();
        String query = tuple.getValue0();
        if (queryParams.containsKey("as-tree")) {
            if (queryParams.getFirst("as-tree").equalsIgnoreCase("true")) {
                if (this.isDsl) { // If dsl query and as-tree parameter is true, remove "end"
                                  // concatenation and append tree.
                    query = removeDslQueryEnd(query);
                }
                query = query.concat(".tree()"); // Otherwise, normal gremlin query will just append
                                                 // tree
            }
        }
        Map<String, Object> params = tuple.getValue1();

        if (query.equals("") && (vertices.isPresent() && vertices.get().isEmpty())) {
            // nothing to do, just exit
            return new ArrayList<>();
        }
        GraphTraversal<?, ?> g = this.runQuery(query, params, traversalSource);

        resultVertices = this.processSubGraph(style, g);

        return resultVertices;
    }

    private String removeDslQueryEnd(String query) {
        String end = ".cap('x').unfold().dedup()";
        if (query.length() <= end.length()) {
            return query;
        }
        if (query.contains(end)) {
            int startIndex = query.length() - end.length();
            for (int i = 0; startIndex - i >= 0; i++) { // remove tailing instance
                startIndex = query.length() - end.length() - i;
                int lastIndex = query.length() - i;
                if (query.substring(startIndex, lastIndex).equals(end)) {
                    query = query.substring(0, startIndex) + query.substring(lastIndex);
                    break;
                }
            }
        }
        return query;
    }

    protected Pair<String, Map<String, Object>> createQuery() throws AAIException {
        Map<String, Object> params = new HashMap<>();
        String query = "";
        if (this.isGremlin) {
            query = gremlin.get();

        } else if (this.isDsl) {
            String dslUserQuery = dsl.get();
            if (dslQueryProcessorOptional.isPresent()) {
                Map<String, Object> resultMap =
                    dslQueryProcessorOptional.get().parseAaiQuery(dslApiVersion, dslUserQuery);
                String dslQuery = resultMap.get("query").toString();
                Object propMap = resultMap.get("propertiesMap");
                if (propMap instanceof Map) {
                    Map<String, List<String>> newPropMap = new HashMap<String, List<String>>();
                    newPropMap = (Map<String, List<String>>) propMap;
                    setPropertiesMap(newPropMap);
                }
                query = groovyQueryBuilder.executeTraversal(dbEngine, dslQuery, params, style,
                    traversalSource);
                String startPrefix = "g.V()";
                query = startPrefix + query;
            }
            LOGGER.debug("Converted to gremlin query\n {}", query);
        } else {
            Matcher m = p.matcher(uri.get().getPath());
            String queryName = "";
            List<String> optionalParameters = Collections.emptyList();
            if (m.find()) {
                queryName = m.group(1);
                CustomQueryConfig queryConfig =
                    gremlinServerSingleton.getCustomQueryConfig(queryName);
                if (queryConfig != null) {
                    query = queryConfig.getQuery();
                    optionalParameters = queryConfig.getQueryOptionalProperties();
                }
            }

            for (String key : queryParams.keySet()) {
                params.put(key, queryParams.getFirst(key));
                if (optionalParameters.contains(key)) {
                    optionalParameters.remove(key);
                }
            }

            if (!optionalParameters.isEmpty()) {
                MissingOptionalParameter missingParameter = MissingOptionalParameter.getInstance();
                for (String key : optionalParameters) {
                    params.put(key, missingParameter);
                }
            }

            if (vertices.isPresent() && !vertices.get().isEmpty()) {

                // Get the vertices and convert them into object array
                // The reason for this was .V() takes in an array of objects
                // not a list of objects so that needs to be converted
                // Also instead of statically creating the list which is a bad practice
                // We are binding the array dynamically to the groovy processor correctly
                // This will fix the memory issue of the method size too big
                // as statically creating a list string and passing is not appropriate

                Object[] startVertices = vertices.get().toArray();

                params.put("startVertexes", startVertices);

                if (query == null) {
                    query = "";
                } else {
                    query = groovyQueryBuilder.executeTraversal(dbEngine, query, params, style,
                        traversalSource);
                }

                String startPrefix = "g.V(startVertexes)";

                if (!"".equals(query)) {
                    query = startPrefix + query;
                } else {
                    query = startPrefix;
                }

                // Getting all the vertices and logging them is not reasonable
                // As it could have performance impacts so doing a check here
                // to see if the logger is trace so only print the start vertexes
                // otherwise we would like to see what the gremlin query that was converted
                // So to check if the output matches the desired behavior
                // This way if to enable deeper logging, just changing logback would work
                if (LOGGER.isTraceEnabled()) {
                    String readQuery = query.replaceAll("startVertexes",
                        Arrays.toString(startVertices).replaceAll("[^0-9,]", ""));
                    LOGGER.trace("Converted to gremlin query including the start vertices \n {}",
                        readQuery);
                } else if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Converted to gremlin query without the start vertices \n {}",
                        query);
                }
            } else {
                throw new AAIException("AAI_6148");
            }

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

        private Optional<String> dsl = Optional.empty();
        private boolean isDsl = false;
        private DslQueryProcessor dslQueryProcessor;
        private GremlinServerSingleton gremlinServerSingleton;
        private Optional<String> nodeType = Optional.empty();
        private boolean isNodeTypeQuery = false;
        protected MultivaluedMap<String, String> uriParams;
        protected GraphTraversalSource traversalSource;
        protected boolean isHistory = false;
        protected QueryVersion dslApiVersion;
        protected Format format;

        protected QueryStyle style = QueryStyle.GREMLIN_TRAVERSAL;

        public Builder(TransactionalGraphEngine dbEngine,
            GremlinServerSingleton gremlinServerSingleton) {
            this.dbEngine = dbEngine;
            this.gremlinServerSingleton = gremlinServerSingleton;
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

        public Builder queryFrom(String query, String queryType) {

            if (queryType.equals("gremlin")) {
                this.gremlin = Optional.of(query);
                this.isGremlin = true;
            }
            if (queryType.equals("dsl")) {
                this.dsl = Optional.of(query);
                this.isDsl = true;
            }
            if (queryType.equals("nodeQuery")) {
                this.nodeType = Optional.of(query);
                this.isNodeTypeQuery = true;
            }
            return this;
        }

        public Builder uriParams(MultivaluedMap<String, String> uriParams) {
            this.uriParams = uriParams;
            return this;
        }

        public Builder processWith(QueryProcessorType type) {
            this.processorType = type;
            return this;
        }

        public Builder format(Format format) {
            this.format = format;
            return this;
        }

        public Builder traversalSource(boolean isHistory, GraphTraversalSource source) {
            this.traversalSource = source;
            this.isHistory = isHistory;
            if (this.isHistory) {
                this.style = QueryStyle.HISTORY_GREMLIN_TRAVERSAL;
            }

            return this;
        }

        public Builder queryProcessor(DslQueryProcessor dslQueryProcessor) {
            this.dslQueryProcessor = dslQueryProcessor;
            return this;
        }

        public Builder version(QueryVersion version) {
            this.dslApiVersion = version;
            return this;
        }

        public Optional<DslQueryProcessor> getDslQueryProcessor() {
            return Optional.ofNullable(this.dslQueryProcessor);
        }

        public TransactionalGraphEngine getDbEngine() {
            return dbEngine;
        }

        public Optional<URI> getUri() {
            return uri;
        }

        public MultivaluedMap<String, String> getUriParams() {
            return uriParams;
        }

        public Optional<String> getGremlin() {
            return gremlin;
        }

        public boolean isGremlin() {
            return isGremlin;
        }

        public Optional<String> getDsl() {
            return dsl;
        }

        public boolean isDsl() {
            return isDsl;
        }

        public Optional<Collection<Vertex>> getVertices() {
            return vertices;
        }

        public QueryProcessorType getProcessorType() {
            return processorType;
        }

        public GremlinServerSingleton getGremlinServerSingleton() {
            return gremlinServerSingleton;
        }

        public Optional<String> getNodeType() {
            return nodeType;
        }

        public boolean isNodeTypeQuery() {
            return isNodeTypeQuery;
        }

        public GenericQueryProcessor create() {
            if (isNodeTypeQuery()) {
                return new NodeQueryProcessor(this);
            }
            return new GroovyShellImpl(this);
        }

        public GraphTraversalSource getTraversalSource() {
            return traversalSource;
        }

        public void setTraversalSource(GraphTraversalSource traversalSource) {
            this.traversalSource = traversalSource;
        }

        public boolean isHistory() {
            return isHistory;
        }

        public void setHistory(boolean history) {
            isHistory = history;
        }

        public QueryStyle getStyle() {
            return style;
        }

        public void setStyle(QueryStyle style) {
            this.style = style;
        }

        public QueryVersion getDslApiVersion() {
            return dslApiVersion;
        }

        public void setDslApiVersion(QueryVersion dslApiVersion) {
            this.dslApiVersion = dslApiVersion;
        }

        public Format getFormat() {
            return this.format;
        }

    }
}
