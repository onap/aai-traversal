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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finds the nodes of one type that were created or modified since a point in time.
 */
public final class RecentNodesProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecentNodesProcessor.class);

    private static final String QUERY = "builder.getVerticesByProperty('aai-node-type', nodeType)"
        + ".or(builder.newInstance().getVerticesGreaterThanProperty('aai-created-ts',startTime),"
        + "  builder.newInstance().getVerticesGreaterThanProperty('aai-last-mod-ts',startTime)"
        + ")";

    private final TransactionalGraphEngine dbEngine;
    private final GroovyQueryBuilder queryBuilder = new GroovyQueryBuilder();

    public RecentNodesProcessor(TransactionalGraphEngine dbEngine) {
        this.dbEngine = dbEngine;
    }

    public List<Object> execute(String nodeType, long startTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("nodeType", nodeType);

        String query = "g.V()" + queryBuilder.executeTraversal(dbEngine, QUERY, params);
        LOGGER.debug("Converted to gremlin query without the start vertices \n {}", query);

        GraphTraversal<?, ?> g = GroovyTraversalRunner.run(query, params,
            dbEngine.asAdmin().getTraversalSource(), Optional.empty());
        return new ArrayList<>(g.toList());
    }
}
