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

import groovy.lang.MissingPropertyException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GroovyTraversalRunnerTest {

    private GraphTraversalSource g;

    @BeforeEach
    public void setUp() {
        g = TinkerGraph.open().traversal();
        g.addV("pserver").property("hostname", "pserver-1").iterate();
        g.addV("pserver").property("hostname", "pserver-2").iterate();
    }

    @Test
    public void thatQueryRunsAgainstTheGivenTraversalSourceWithItsParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("hostname", "pserver-2");

        List<?> hostnames = GroovyTraversalRunner
            .run("g.V().has('hostname', hostname).values('hostname')", params, g, Optional.empty())
            .toList();

        assertEquals(List.of("pserver-2"), hostnames);
    }

    @Test
    public void thatUnboundVariableFails() {
        Map<String, Object> params = new HashMap<>();

        assertThrows(MissingPropertyException.class,
            () -> GroovyTraversalRunner.run("vnfs-fromServiceInstance", params, g, Optional.empty()));
    }
}
