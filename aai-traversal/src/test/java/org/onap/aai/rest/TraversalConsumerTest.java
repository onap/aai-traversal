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
package org.onap.aai.rest;

import jakarta.ws.rs.core.*;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.AAISetup;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.db.props.AAIProperties;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.serialization.engines.JanusGraphDBEngine;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import java.lang.reflect.Method;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = TraversalConsumerTest.TraversalConsumerConcrete.class)
public class TraversalConsumerTest extends AAISetup {

    private static final QueryStyle queryStyle = QueryStyle.TRAVERSAL;
    private static Loader loader;
    GraphTraversalSource source;

    @Configuration
    static class TraversalConsumerConcrete extends TraversalConsumer {

        @Bean
        public TraversalConsumer traversalConsumer() {
            return new TraversalConsumerConcrete();
        }

        @Override
        protected SubgraphStrategy getSubgraphStrategy(long startTs, long endTs, Format format) {
            return SubgraphStrategy.build()
                    .vertices(__.and(__.has(AAIProperties.START_TS, P.lte(startTs)),
                            __.or(__.hasNot(AAIProperties.END_TS),
                                    __.has(AAIProperties.END_TS, P.gt(startTs)))))
                    .vertexProperties(__.and(__.has(AAIProperties.START_TS, P.lte(startTs)),
                            __.or(__.hasNot(AAIProperties.END_TS),
                                    __.has(AAIProperties.END_TS, P.gt(startTs)))))
                    .edges(__.and(__.has(AAIProperties.START_TS, P.lte(startTs)),
                            __.or(__.hasNot(AAIProperties.END_TS),
                                    __.has(AAIProperties.END_TS, P.gt(startTs)))))
                    .create();
        }
    }

    private TransactionalGraphEngine dbEngine;

    @Mock
    private MultivaluedMap<String, String> queryParameters;

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    private TraversalConsumerConcrete traversalConsumer;

    private HttpHeaders httpHeaders;

    private UriInfo uriInfo;

    private MultivaluedMap<String, String> headersMultiMap;

    private List<String> aaiRequestContextList;

    private List<MediaType> outputMediaTypes;

    static ApplicationContext mockContext = mock(ApplicationContext.class);
    static Environment mockEnvironment = mock(Environment.class);

    @BeforeClass
    public static void beforeClass() {
        when(mockContext.getEnvironment()).thenReturn(mockEnvironment);
        when(mockEnvironment.getProperty("history.truncate.window.days", "365")).thenReturn("365");
        when(mockEnvironment.getProperty("history.enabled", "false")).thenReturn("false");
        when(mockEnvironment.getProperty("multi.tenancy.enabled", "false")).thenReturn("false");

        SpringContextAware springContextAware = new SpringContextAware();
        springContextAware.setApplicationContext(mockContext);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        traversalConsumer = spy(new TraversalConsumerConcrete());

        TransactionalGraphEngine newDbEngine = new JanusGraphDBEngine(queryStyle, loader);
        dbEngine = spy(newDbEngine);

        httpHeaders = mock(HttpHeaders.class);
        uriInfo = mock(UriInfo.class);

        headersMultiMap = new MultivaluedHashMap<>();

        headersMultiMap.add("X-FromAppId", "JUNIT");
        headersMultiMap.add("X-TransactionId", UUID.randomUUID().toString());
        headersMultiMap.add("Real-Time", "true");
        headersMultiMap.add("Accept", "application/json");
        headersMultiMap.add("aai-request-context", "");

        outputMediaTypes = new ArrayList<>();
        outputMediaTypes.add(APPLICATION_JSON);

        aaiRequestContextList = new ArrayList<>();
        aaiRequestContextList.add("");

        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);
        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);
        when(httpHeaders.getRequestHeader("X-FromAppId")).thenReturn(Arrays.asList("JUNIT"));
        when(httpHeaders.getRequestHeader("X-TransactionId")).thenReturn(Arrays.asList("JUNIT"));
        when(httpHeaders.getRequestHeader("aai-request-context")).thenReturn(aaiRequestContextList);

        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(uriInfo.getQueryParameters(false)).thenReturn(queryParameters);

        doReturn(null).when(queryParameters).remove(any());

        when(httpHeaders.getMediaType()).thenReturn(APPLICATION_JSON);
        final ModelType factoryType = ModelType.MOXY;
        Loader loader = loaderFactory.createLoaderForVersion(factoryType,
                schemaVersions.getRelatedLinkVersion());
        dbEngine = spy(new JanusGraphDBEngine(QueryStyle.TRAVERSAL, loader));
    }

    @Test
    public void testIsHistoryEnabled() {
        assertFalse(traversalConsumer.isHistoryEnabled());
    }

    @Test
    public void testIsHistory() {
        when(traversalConsumer.isHistoryEnabled()).thenReturn(true);

        boolean result = traversalConsumer.isHistory(Format.lifecycle);
        assertTrue(result);

        result = traversalConsumer.isHistory(Format.aggregate);
        assertFalse(result);
    }

    @Test
    public void testIsAggregate() {
        boolean result = traversalConsumer.isAggregate(Format.aggregate);
        assertTrue(result);

        result = traversalConsumer.isAggregate(Format.lifecycle);
        assertFalse(result);
    }

    @Test
    public void testValidateHistoryParams() throws AAIException {
        assertDoesNotThrow(() -> traversalConsumer.validateHistoryParams(Format.state, queryParameters));
    }

    @Test
    public void testGetSubgraphStrategy() {
        long startTs = 1638336000000L;
        long endTs = 1638422400000L;
        Format format = Format.state;

        SubgraphStrategy strategy = traversalConsumer.getSubgraphStrategy(startTs, endTs, format);
        assertNotNull(strategy);

        format = Format.lifecycle;
        strategy = traversalConsumer.getSubgraphStrategy(startTs, endTs, format);
        assertNotNull(strategy);

        format = Format.aggregate;
        strategy = traversalConsumer.getSubgraphStrategy(startTs, endTs, format);
        assertNotNull(strategy);
    }

    @Test
    public void testGetEndTime() throws AAIException {
        when(queryParameters.getFirst("endTs")).thenReturn("now");
        long endTime = traversalConsumer.getEndTime(queryParameters);
        assertTrue(endTime > 0);

        when(queryParameters.getFirst("endTs")).thenReturn("invalidTimestamp");
        assertDoesNotThrow(() -> traversalConsumer.getEndTime(queryParameters));
    }

    @Test
    public void testGetSubgraphStrategyFromBaseClass() {
        TraversalConsumer baseTraversalConsumer = spy(new TraversalConsumer() {
            @Override
            protected SubgraphStrategy getSubgraphStrategy(long startTs, long endTs, Format format) {
                return super.getSubgraphStrategy(startTs, endTs, format);
            }
        });

        long startTs = 1638336000000L;
        long endTs = 1638422400000L;
        Format format = Format.state;

        SubgraphStrategy strategy = baseTraversalConsumer.getSubgraphStrategy(startTs, endTs, format);
        assertNotNull(strategy);

        format = Format.lifecycle;
        strategy = baseTraversalConsumer.getSubgraphStrategy(startTs, endTs, format);
        assertNotNull(strategy);
    }

    @Test
    public void testFurthestInThePast() {
        Long furthestPast = traversalConsumer.getFurthestInThePast();
        assertNotNull(furthestPast);
        assertTrue(furthestPast > 0);
    }

    @Test
    public void testValidateHistoryParamsWithInvalidTime() {
        when(queryParameters.getFirst("startTs")).thenReturn("invalidTimestamp");
        when(queryParameters.getFirst("endTs")).thenReturn("-1");

        assertThrows(IllegalArgumentException.class,
                () -> traversalConsumer.validateHistoryParams(Format.state, queryParameters));
    }

    @Test
    public void testValidateHistoryParamsWithNegativeTimestamps() {
        when(queryParameters.getFirst("startTs")).thenReturn("-100");
        when(queryParameters.getFirst("endTs")).thenReturn("-50");

        assertThrows(AAIException.class,
                () -> traversalConsumer.validateHistoryParams(Format.state, queryParameters));
    }

    @Test
    public void testValidateHistoryParamsWithEndBeforeStart() {
        when(queryParameters.getFirst("startTs")).thenReturn("1638422400000");
        when(queryParameters.getFirst("endTs")).thenReturn("1638336000000");

        assertThrows(AAIException.class,
                () -> traversalConsumer.validateHistoryParams(Format.state, queryParameters));
    }

    @Test
    public void testGetQueryStyleWhenIsHistoryTrue() {
        Format format = Format.state;
        HttpEntry traversalUriHttpEntry = mock(HttpEntry.class);

        doReturn(true).when(traversalConsumer).isHistory(format);

        QueryStyle queryStyle = traversalConsumer.getQueryStyle(format, traversalUriHttpEntry);

        assertEquals(QueryStyle.HISTORY_TRAVERSAL, queryStyle,
                "QueryStyle should be HISTORY_TRAVERSAL when isHistory(format) returns true");
    }

    @Test
    public void testGetQueryStyleWhenIsHistoryFalse() {
        Format format = Format.lifecycle;
        HttpEntry traversalUriHttpEntry = mock(HttpEntry.class);

        doReturn(false).when(traversalConsumer).isHistory(format);

        QueryStyle expectedQueryStyle = QueryStyle.TRAVERSAL;
        when(traversalUriHttpEntry.getQueryStyle()).thenReturn(expectedQueryStyle);

        QueryStyle queryStyle = traversalConsumer.getQueryStyle(format, traversalUriHttpEntry);

        assertEquals(expectedQueryStyle, queryStyle,
                "QueryStyle should match the result of traversalUriHttpEntry.getQueryStyle() when isHistory(format) returns false");
    }

    @Test
    public void testGetDataOwnerSubgraphStrategyWithRolesUsingReflection() throws Exception {
        Set<String> roles = Set.of("ROLE_ADMIN", "ROLE_USER");

        Method method = TraversalConsumer.class.getDeclaredMethod("getDataOwnerSubgraphStrategy", Set.class);
        method.setAccessible(true);

        SubgraphStrategy strategy = (SubgraphStrategy) method.invoke(traversalConsumer, roles);

        assertNotNull(strategy, "SubgraphStrategy should not be null");
        assertFalse(strategy.toString().contains("data-owner"),
                "The strategy should filter vertices based on 'data-owner' property");
    }

}
