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
import org.janusgraph.graphdb.query.QueryProcessor;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.enums.QueryVersion;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.SubGraphStyle;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GenericQueryProcessorTest {

    private TransactionalGraphEngine mockDbEngine;
    private GremlinServerSingleton mockGremlinServerSingleton;

    @Before
    public void setUp() {
        // Mock the necessary components
        mockDbEngine = mock(TransactionalGraphEngine.class);
        mockGremlinServerSingleton = mock(GremlinServerSingleton.class);
    }

    @Test
    public void testSetQueryProcessorType() {
        // Initialize the builder
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);

        // Set the processor type to GREMLIN_SERVER
        builder.processWith(QueryProcessorType.GREMLIN_SERVER);

        // Assert that the processor type is set correctly
        assertEquals(QueryProcessorType.GREMLIN_SERVER, builder.getProcessorType());
    }

    @Test
    public void testSetTraversalSource() {
        // Create a mock GraphTraversalSource
        GraphTraversalSource mockTraversalSource = mock(GraphTraversalSource.class);

        // Initialize the builder and set the traversal source
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.traversalSource(false, mockTraversalSource);

        // Assert that the traversal source is set correctly
        assertEquals(mockTraversalSource, builder.getTraversalSource());
    }

    @Test
    public void testSetStyle() {
        // Initialize the builder and set the style to HISTORY_GREMLIN_TRAVERSAL
        QueryStyle style = QueryStyle.HISTORY_GREMLIN_TRAVERSAL;
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.setStyle(style);

        // Assert that the style is set correctly
        assertEquals(style, builder.getStyle());
    }

    @Test
    public void testSetDslApiVersion() {
        // Initialize the builder and set the DSL API version
        QueryVersion version = QueryVersion.V2;
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.setDslApiVersion(version);

        // Assert that the DSL API version is set correctly
        assertEquals(version, builder.getDslApiVersion());
    }

    @Test
    public void testSetHistory() {
        // Initialize the builder and set history to true
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.setHistory(true);

        // Assert that the history flag is set to true
        assertTrue(builder.isHistory());
    }

    @Test
    public void testSetHistoryFalse() {
        // Initialize the builder and set history to false
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.setHistory(false);

        // Assert that the history flag is set to false
        assertFalse(builder.isHistory());
    }

    @Test
    public void testSetTraversalSourceWithHistory() {
        // Create a mock GraphTraversalSource
        GraphTraversalSource mockTraversalSource = mock(GraphTraversalSource.class);

        // Initialize the builder and set history to true with traversal source
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.traversalSource(true, mockTraversalSource);

        // Assert that the traversal source is set correctly and history style is applied
        assertEquals(mockTraversalSource, builder.getTraversalSource());
        assertEquals(QueryStyle.HISTORY_GREMLIN_TRAVERSAL, builder.getStyle());
    }

    @Test
    public void testSetTraversalSourceDirectly() {
        // Create a mock GraphTraversalSource
        GraphTraversalSource mockTraversalSource = mock(GraphTraversalSource.class);

        // Initialize the builder and set the traversal source directly using setTraversalSource method
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.setTraversalSource(mockTraversalSource);

        // Assert that the traversal source is set correctly
        assertEquals(mockTraversalSource, builder.getTraversalSource());
    }

    @Test
    public void testEmptyQueryAndEmptyVertices() throws AAIException, FileNotFoundException {
        // Initialize the builder and set up the conditions where query is empty and vertices are empty
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);

        // Set the query to an empty string
        builder.queryFrom("", "gremlin");

        // Set the vertices to be empty
        builder.startFrom(Collections.emptyList());

        // Create the GenericQueryProcessor instance
        GenericQueryProcessor queryProcessor = builder.create();

        // Call the execute method with a mock SubGraphStyle (use an empty style as we are not testing deep logic)
        List<Object> result = queryProcessor.execute(SubGraphStyle.star);

        // Assert that the result is an empty ArrayList
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAsTreeParameterWithDslQuery() throws Exception {
        // Initialize the builder and set up the conditions
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);

        // Mock the URI query parameters where "as-tree" is true
        MultivaluedMap<String, String> mockQueryParams = new MultivaluedHashMap<>();
        mockQueryParams.add("as-tree", "true");
        builder.uriParams(mockQueryParams);

        // Set a DSL query
        String dslQuery = "some dsl query";
        builder.queryFrom(dslQuery, "dsl");

        // Create the GenericQueryProcessor instance
        GenericQueryProcessor queryProcessor = builder.create();

        // Use reflection to access the private removeDslQueryEnd method
        Method removeDslQueryEndMethod = GenericQueryProcessor.class.getDeclaredMethod("removeDslQueryEnd", String.class);
        removeDslQueryEndMethod.setAccessible(true);

        // Call the method with a sample DSL query
        String transformedQuery = (String) removeDslQueryEndMethod.invoke(queryProcessor, dslQuery);

        // Check the transformed query
        assertEquals("some dsl query", transformedQuery);
    }
}
