/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG.
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

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.ws.rs.core.MultivaluedMap;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.query.builder.MissingOptionalParameter;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.restcore.util.URITools;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a stored query from the start vertices of a {@code /query} request. Without a stored
 * query it returns the start vertices themselves.
 */
public final class CustomQueryProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomQueryProcessor.class);

    private final TransactionalGraphEngine dbEngine;
    private final GraphTraversalSource traversalSource;
    private final QueryStyle style;
    private final GroovyQueryBuilder groovyQueryBuilder = new GroovyQueryBuilder();

    public CustomQueryProcessor(TransactionalGraphEngine dbEngine,
        GraphTraversalSource traversalSource, QueryStyle style) {
        this.dbEngine = dbEngine;
        this.traversalSource = traversalSource;
        this.style = style;
    }

    public List<Object> execute(CustomQueryConfig queryConfig, URI queryUri,
        Collection<Vertex> startVertices, SubGraphStyle subGraphStyle) throws AAIException {
        if (startVertices.isEmpty()) {
            throw new AAIException("AAI_6148");
        }

        Map<String, Object> params = new HashMap<>();
        MultivaluedMap<String, String> queryParams = URITools.getQueryMap(queryUri);
        for (String key : queryParams.keySet()) {
            params.put(key, queryParams.getFirst(key));
        }
        String storedQuery = null;
        if (queryConfig != null) {
            storedQuery = queryConfig.getQuery();
            for (String key : queryConfig.getQueryOptionalProperties()) {
                params.putIfAbsent(key, MissingOptionalParameter.getInstance());
            }
        }

        // .V() takes an array, and binding it as a parameter keeps the generated script small
        // however many start vertices there are
        Object[] startVertexes = startVertices.toArray();
        params.put("startVertexes", startVertexes);

        String query = "g.V(startVertexes)";
        if (storedQuery != null) {
            query += groovyQueryBuilder.executeTraversal(dbEngine, storedQuery, params, style,
                traversalSource);
        }

        if (LOGGER.isTraceEnabled()) {
            String readQuery = query.replaceAll("startVertexes",
                Arrays.toString(startVertexes).replaceAll("[^0-9,]", ""));
            LOGGER.trace("Converted to gremlin query including the start vertices \n {}",
                readQuery);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Converted to gremlin query without the start vertices \n {}", query);
        }

        GraphTraversal<?, ?> g =
            GroovyTraversalRunner.run(query, params, traversalSource, Optional.of(queryUri));
        return SubGraphs.collect(subGraphStyle, g);
    }
}
