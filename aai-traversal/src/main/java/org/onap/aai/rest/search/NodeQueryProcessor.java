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
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.javatuples.Pair;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeQueryProcessor extends GroovyShellImpl {

    private static Logger logger = LoggerFactory.getLogger(NodeQueryProcessor.class);

    protected String nodeType;
    private MultivaluedMap<String, String> nodeQueryParams = new MultivaluedHashMap<>();
    protected final Optional<Collection<Vertex>> vertices;
    protected static Pattern p = Pattern.compile("query/(.*+)");
    protected Optional<String> gremlin;
    protected final TransactionalGraphEngine dbEngine;
    protected GroovyQueryBuilder queryBuilder = new GroovyQueryBuilder();

    protected NodeQueryProcessor(Builder builder) {
        super(builder);
        this.nodeQueryParams = builder.uriParams;
        if (builder.getNodeType().isPresent()) {
            this.nodeType = builder.getNodeType().get();
        }
        this.dbEngine = builder.getDbEngine();
        this.vertices = builder.getVertices();

    }

    public Pair<String, Map<String, Object>> createQuery() throws AAIException {
        Map<String, Object> params = new HashMap<>();

        Long timeNowInMilliSecs = System.currentTimeMillis();
        Long startTime = 0L;
        if (nodeQueryParams.containsKey("hours")) {
            Long hoursInMilliSec =
                TimeUnit.HOURS.toMillis(Long.parseLong(nodeQueryParams.getFirst("hours")));
            startTime = timeNowInMilliSecs - hoursInMilliSec;
        } else if (nodeQueryParams.containsKey("date-time")) {
            Long dateTime = Long.parseLong(nodeQueryParams.getFirst("date-time"));
            startTime = dateTime;
        }

        String query = "builder.getVerticesByProperty('aai-node-type', nodeType)"
            + ".or(builder.newInstance().getVerticesGreaterThanProperty('aai-created-ts',startTime),"
            + "  builder.newInstance().getVerticesGreaterThanProperty('aai-last-mod-ts',startTime)"
            + ")";

        params.put("startTime", startTime);
        params.put("nodeType", nodeType);

        query = queryBuilder.executeTraversal(dbEngine, query, params);

        String startPrefix = "g.V()";

        query = startPrefix + query;

        if (logger.isDebugEnabled()) {
            logger.debug("Converted to gremlin query without the start vertices \n {}", query);
        }

        return new Pair<>(query, params);
    }

    public List<Object> execute(SubGraphStyle style) throws FileNotFoundException, AAIException {
        final List<Object> resultVertices = new Vector<>();

        Pair<String, Map<String, Object>> tuple = this.createQuery();
        String query = tuple.getValue0();
        Map<String, Object> params = tuple.getValue1();

        if (query.equals("")) {
            // nothing to do, just exit
            return new ArrayList<>();
        }
        GraphTraversal<?, ?> g =
            this.runQuery(query, params, dbEngine.asAdmin().getTraversalSource());

        resultVertices.addAll(g.toList());

        return resultVertices;
    }

}
