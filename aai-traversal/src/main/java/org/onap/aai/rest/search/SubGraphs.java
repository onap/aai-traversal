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
import java.util.Vector;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.onap.aai.serialization.queryformats.SubGraphStyle;

final class SubGraphs {

    private SubGraphs() {}

    static List<Object> collect(SubGraphStyle style, GraphTraversal<?, ?> g) {
        final List<Object> resultVertices = new Vector<>();
        g.store("y");

        if (SubGraphStyle.prune.equals(style) || SubGraphStyle.star.equals(style)) {
            g.barrier().bothE();
            if (SubGraphStyle.prune.equals(style)) {
                g.where(__.otherV().where(P.within("y")));
            }
            g.dedup().subgraph("subGraph").cap("subGraph").map(x -> (Graph) x.get()).next()
                .traversal().V().forEachRemaining(resultVertices::add);
        } else {
            resultVertices.addAll(g.toList());
        }
        return resultVertices;
    }
}
