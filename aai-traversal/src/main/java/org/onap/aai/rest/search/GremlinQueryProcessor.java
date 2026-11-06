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

import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinAntlrToJava;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinErrorListener;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinLexer;
import org.apache.tinkerpop.gremlin.language.grammar.GremlinParser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.ReadOnlyStrategy;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.verification.VerificationException;
import org.onap.aai.aailog.logs.AaiDBTraversalMetricLog;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.onap.aai.util.AAIConstants;

/**
 * Runs the raw Gremlin of a {@code /query} request.
 * <p>
 * The Gremlin is parsed with TinkerPop's Gremlin grammar instead of being run as Groovy,
 * so it can only contain traversal steps. It must be a single traversal spawned from a
 * plain {@code g} with {@code V()} or {@code E()}, which keeps callers from replacing the
 * strategies of the given traversal source, and it runs read-only.
 */
public final class GremlinQueryProcessor {

    private final GraphTraversalSource traversalSource;

    public GremlinQueryProcessor(GraphTraversalSource traversalSource) {
        this.traversalSource = traversalSource;
    }

    public List<Object> execute(String gremlin, SubGraphStyle subGraphStyle) throws AAIException {
        AaiDBTraversalMetricLog metricLog =
            new AaiDBTraversalMetricLog(AAIConstants.AAI_TRAVERSAL_MS);
        metricLog.pre(Optional.empty());
        GraphTraversal<?, ?> g;
        try {
            g = toTraversal(gremlin);
        } finally {
            metricLog.post();
        }

        try {
            return SubGraphs.collect(subGraphStyle, g);
        } catch (VerificationException e) {
            throw new AAIException("AAI_3000", e);
        }
    }

    private GraphTraversal<?, ?> toTraversal(String gremlin) throws AAIException {
        try {
            GremlinParser.QueryContext query = parseSingleTraversal(gremlin);
            return (GraphTraversal<?, ?>) new GremlinAntlrToJava(
                traversalSource.withStrategies(ReadOnlyStrategy.instance())).visitQuery(query);
        } catch (RuntimeException e) {
            throw new AAIException("AAI_3000", e);
        }
    }

    private static GremlinParser.QueryContext parseSingleTraversal(String gremlin)
        throws AAIException {
        GremlinLexer lexer = new GremlinLexer(CharStreams.fromString(gremlin));
        lexer.removeErrorListeners();
        lexer.addErrorListener(new GremlinErrorListener());
        GremlinParser parser = new GremlinParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(new GremlinErrorListener());

        List<GremlinParser.QueryContext> queries = parser.queryList().query();
        if (queries.size() != 1) {
            throw new AAIException("AAI_3000", "Expected a single traversal");
        }
        GremlinParser.QueryContext query = queries.get(0);
        GremlinParser.RootTraversalContext root = query.rootTraversal();
        if (root == null || query.getChildCount() != 1) {
            throw new AAIException("AAI_3000", "Expected a traversal without a terminal step");
        }
        if (root.traversalSource().getChildCount() != 1) {
            throw new AAIException("AAI_3000", "Expected the traversal to start from g");
        }
        GremlinParser.TraversalSourceSpawnMethodContext spawn = root.traversalSourceSpawnMethod();
        if (spawn.traversalSourceSpawnMethod_V() == null
            && spawn.traversalSourceSpawnMethod_E() == null) {
            throw new AAIException("AAI_3000", "Expected the traversal to start with V() or E()");
        }
        return query;
    }
}
