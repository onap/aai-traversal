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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.dsl.DslQueryProcessor;
import org.onap.aai.rest.enums.QueryVersion;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Translates a DSL query to Gremlin with a {@link DslQueryProcessor} and runs it.
 */
public final class DslTraversalProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DslTraversalProcessor.class);

    // appended by DslQueryBuilder.end(); tree() has to replace it
    private static final String DSL_QUERY_END = ".cap('x').unfold().dedup()";

    private final TransactionalGraphEngine dbEngine;
    private final GraphTraversalSource traversalSource;
    private final QueryStyle style;
    private final GroovyQueryBuilder groovyQueryBuilder = new GroovyQueryBuilder();

    public record DslResult(List<Object> results, Map<String, List<String>> selectedProperties) {}

    public DslTraversalProcessor(TransactionalGraphEngine dbEngine,
        GraphTraversalSource traversalSource, QueryStyle style) {
        this.dbEngine = dbEngine;
        this.traversalSource = traversalSource;
        this.style = style;
    }

    public DslResult execute(DslQueryProcessor dslQueryProcessor, QueryVersion version,
        String dsl, boolean asTree, SubGraphStyle subGraphStyle) throws AAIException {
        Map<String, Object> resultMap = dslQueryProcessor.parseAaiQuery(version, dsl);
        String dslQuery = resultMap.get("query").toString();

        Map<String, Object> params = new HashMap<>();
        String query = "g.V()"
            + groovyQueryBuilder.executeTraversal(dbEngine, dslQuery, params, style, traversalSource);
        LOGGER.debug("Converted to gremlin query\n {}", query);

        if (asTree) {
            query = removeQueryEnd(query) + ".tree()";
        }

        GraphTraversal<?, ?> g =
            GroovyTraversalRunner.run(query, params, traversalSource, Optional.empty());
        return new DslResult(SubGraphs.collect(subGraphStyle, g), selectedProperties(resultMap));
    }

    static String removeQueryEnd(String query) {
        int index = query.lastIndexOf(DSL_QUERY_END);
        if (index < 0) {
            return query;
        }
        return query.substring(0, index) + query.substring(index + DSL_QUERY_END.length());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> selectedProperties(Map<String, Object> resultMap) {
        Object propertiesMap = resultMap.get("propertiesMap");
        return propertiesMap instanceof Map ? (Map<String, List<String>>) propertiesMap : null;
    }
}
