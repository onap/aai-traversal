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

import org.junit.jupiter.api.Test;

public class DslTraversalProcessorTest {

    @Test
    public void thatQueryWithoutTheDslEndIsUnchanged() {
        assertEquals("g.V().has('aai-node-type','pserver')",
            DslTraversalProcessor.removeQueryEnd("g.V().has('aai-node-type','pserver')"));
    }

    @Test
    public void thatTrailingDslEndIsRemoved() {
        assertEquals("g.V().has('aai-node-type','pserver').store('x')", DslTraversalProcessor
            .removeQueryEnd("g.V().has('aai-node-type','pserver').store('x').cap('x').unfold().dedup()"));
    }

    @Test
    public void thatOnlyTheLastDslEndIsRemoved() {
        assertEquals("g.V().union(__.cap('x').unfold().dedup()).range(0,10)",
            DslTraversalProcessor.removeQueryEnd(
                "g.V().union(__.cap('x').unfold().dedup()).cap('x').unfold().dedup().range(0,10)"));
    }
}
