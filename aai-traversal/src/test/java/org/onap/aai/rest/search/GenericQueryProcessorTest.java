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
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.enums.QueryVersion;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
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
    public void testSetQueryProcessorType() {
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.processWith(QueryProcessorType.GREMLIN_SERVER);
        assertEquals(QueryProcessorType.GREMLIN_SERVER, builder.getProcessorType());
    }

    @Test
    public void testSetTraversalSource() {
        GraphTraversalSource mockTraversalSource = mock(GraphTraversalSource.class);
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.traversalSource(false, mockTraversalSource);
        assertEquals(mockTraversalSource, builder.getTraversalSource());
    }

    @Test
    public void testSetStyle() {
        QueryStyle style = QueryStyle.HISTORY_GREMLIN_TRAVERSAL;
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.setStyle(style);
        assertEquals(style, builder.getStyle());
    }

    @Test
    public void testSetDslApiVersion() {
        QueryVersion version = QueryVersion.V2;
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.setDslApiVersion(version);
        assertEquals(version, builder.getDslApiVersion());
    }

    @Test
    public void testSetHistory() {
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.setHistory(true);
        assertTrue(builder.isHistory());
    }

    @Test
    public void testSetHistoryFalse() {
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.setHistory(false);
        assertFalse(builder.isHistory());
    }

    @Test
    public void testEmptyQueryAndEmptyVertices() throws AAIException, FileNotFoundException {
        GenericQueryProcessor.Builder builder = new GenericQueryProcessor.Builder(mockDbEngine, mockGremlinServerSingleton);
        builder.queryFrom("", "gremlin");
        builder.startFrom(Collections.emptyList());

        GenericQueryProcessor queryProcessor = builder.create();
        List<Object> result = queryProcessor.execute(SubGraphStyle.star);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAsTreeParameterWithDslQuery() throws Exception {
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
}
