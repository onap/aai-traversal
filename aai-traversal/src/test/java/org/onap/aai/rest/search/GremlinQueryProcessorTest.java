/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.queryformats.SubGraphStyle;

public class GremlinQueryProcessorTest {

    private GraphTraversalSource g;

    @BeforeEach
    public void setUp() {
        g = TinkerGraph.open().traversal();
        g.addV("pserver").property("hostname", "pserver-1").iterate();
        g.addV("pserver").property("hostname", "pserver-2").iterate();
    }

    @Test
    public void thatTraversalRuns() throws AAIException {
        List<Object> hostnames = new GremlinQueryProcessor(g)
            .execute("g.V().has('hostname', 'pserver-2').values('hostname')", SubGraphStyle.no_op);

        assertEquals(List.of("pserver-2"), hostnames);
    }

    @Test
    public void thatTraversalKeepsTheStrategiesOfTheTraversalSource() throws AAIException {
        GraphTraversalSource filtered = g.withStrategies(
            SubgraphStrategy.build().vertices(__.has("hostname", "pserver-1")).create());

        List<Object> hostnames = new GremlinQueryProcessor(filtered)
            .execute("g.V().values('hostname')", SubGraphStyle.no_op);

        assertEquals(List.of("pserver-1"), hostnames);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "'id'.execute()",
        "new File('/etc/passwd').text",
        "g.V().map{ it.get() }",
        "g.V().toList()",
        "g.V(); g.E()",
        "g.tx().commit()",
        "g.addV('pserver')",
        "g.io('/tmp/graph.json').write()",
        "g.withStrategies(new SubgraphStrategy(vertices: __.identity())).V()"})
    public void thatAnythingButASingleTraversalFromGIsRejected(String gremlin) {
        AAIException e = assertThrows(AAIException.class,
            () -> new GremlinQueryProcessor(g).execute(gremlin, SubGraphStyle.no_op));

        assertEquals("AAI_3000", e.getCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "g.V().drop()",
        "g.V().property('hostname', 'changed')",
        "g.V().addE('tosca.relationships.HostedOn').to(__.V())"})
    public void thatTraversalCannotWriteToTheGraph(String gremlin) {
        AAIException e = assertThrows(AAIException.class,
            () -> new GremlinQueryProcessor(g).execute(gremlin, SubGraphStyle.no_op));

        assertEquals("AAI_3000", e.getCode());
        assertEquals(List.of("pserver-1", "pserver-2"), g.V().values("hostname").toList());
        assertEquals(0L, g.E().count().next());
    }
}
