/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.search;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GenericQueryProcessorTest {

    private TransactionalGraphEngine mockDbEngine;
    private GremlinServerSingleton mockGremlinServerSingleton;

    @BeforeEach
    public void setUp() {
        mockDbEngine = mock(TransactionalGraphEngine.class);
        mockGremlinServerSingleton = mock(GremlinServerSingleton.class);
    }

    @Test
    public void testSetTraversalSource() {
        GraphTraversalSource mockTraversalSource = mock(GraphTraversalSource.class);
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.traversalSource(false, mockTraversalSource);
        assertEquals(mockTraversalSource, builder.getTraversalSource());
    }

    @Test
    public void testAsTreeParameterWithDslQueryShorterThanQueryEnd() throws Exception {
        // Test when query is shorter than the ".cap('x').unfold().dedup()" string.
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        MultivaluedMap<String, String> mockQueryParams = new MultivaluedHashMap<>();
        mockQueryParams.add("as-tree", "true");
        builder.uriParams(mockQueryParams);

        String dslQuery = "some dsl query";
        builder.queryFrom(dslQuery, "dsl");

        GenericQueryProcessor queryProcessor = builder.create();
        Method removeDslQueryEndMethod = GenericQueryProcessor.class.getDeclaredMethod("removeDslQueryEnd", String.class);
        removeDslQueryEndMethod.setAccessible(true);

        String transformedQuery = (String) removeDslQueryEndMethod.invoke(queryProcessor, dslQuery);
        assertEquals("some dsl query", transformedQuery);
    }

    @Test
    public void testAsTreeParameterWithDslQueryLongerThanQueryEnd() throws Exception {
        // Test when query is longer than the ".cap('x').unfold().dedup()" string.
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        MultivaluedMap<String, String> mockQueryParams = new MultivaluedHashMap<>();
        mockQueryParams.add("as-tree", "true");
        builder.uriParams(mockQueryParams);

        String dslQuery = "some dsl query.cap('x').unfold().dedup()";
        builder.queryFrom(dslQuery, "dsl");

        GenericQueryProcessor queryProcessor = builder.create();
        Method removeDslQueryEndMethod = GenericQueryProcessor.class.getDeclaredMethod("removeDslQueryEnd", String.class);
        removeDslQueryEndMethod.setAccessible(true);

        String transformedQuery = (String) removeDslQueryEndMethod.invoke(queryProcessor, dslQuery);
        // Here we expect the trailing ".cap('x').unfold().dedup()" to be removed and ".tree()" appended
        assertEquals("some dsl query", transformedQuery);
    }
}
