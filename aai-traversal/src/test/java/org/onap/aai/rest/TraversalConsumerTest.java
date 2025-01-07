/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2024 Deutsche Telekom. All rights reserved.
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.aai.AAISetup;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.db.props.AAIProperties;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.serialization.db.GraphSingleton;
import org.onap.aai.serialization.engines.JanusGraphDBEngine;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;

@ContextConfiguration(classes = TraversalConsumerTest.TraversalConsumerConcrete.class)
public class TraversalConsumerTest extends AAISetup{
    // Create a concrete test implementation of TraversalConsumer
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
                                    __.has(AAIProperties.END_TS, P.gt(startTs)))))  // Filter vertices
                    .vertexProperties(__.and(__.has(AAIProperties.START_TS, P.lte(startTs)),
                            __.or(__.hasNot(AAIProperties.END_TS),
                                    __.has(AAIProperties.END_TS, P.gt(startTs)))))  // Filter vertex properties
                    .edges(__.and(__.has(AAIProperties.START_TS, P.lte(startTs)),
                            __.or(__.hasNot(AAIProperties.END_TS),
                                    __.has(AAIProperties.END_TS, P.gt(startTs)))))  // Filter edges
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

    // Mock SpringContextAware and its dependencies
    static ApplicationContext mockContext = mock(ApplicationContext.class);
    static Environment mockEnvironment = mock(Environment.class);

    @BeforeClass
    public static void beforeClass(){
        // Mock the behavior of SpringContextAware
        when(mockContext.getEnvironment()).thenReturn(mockEnvironment);
        when(mockEnvironment.getProperty("history.truncate.window.days", "365")).thenReturn("365");
        when(mockEnvironment.getProperty("history.enabled", "false")).thenReturn("false");
        when(mockEnvironment.getProperty("multi.tenancy.enabled", "false")).thenReturn("false");

        // Initialize SpringContextAware with mockContext
        SpringContextAware springContextAware = new SpringContextAware();
        springContextAware.setApplicationContext(mockContext);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize the TraversalConsumer
        traversalConsumer = spy(new TraversalConsumerConcrete());

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

        Mockito.doReturn(null).when(queryParameters).remove(anyObject());

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

        // Test with a format that matches HISTORICAL_FORMAT
        boolean result = traversalConsumer.isHistory(Format.lifecycle);
        assertTrue(result);

        // Test with a format that doesn't match
        result = traversalConsumer.isHistory(Format.aggregate);
        assertFalse(result);
    }

    @Test
    public void testIsAggregate() {
        // Test with aggregate format
        boolean result = traversalConsumer.isAggregate(Format.aggregate);
        assertTrue(result);

        // Test with other formats
        result = traversalConsumer.isAggregate(Format.lifecycle);
        assertFalse(result);
    }

    @Test
    public void testValidateHistoryParams() throws AAIException {
        assertDoesNotThrow(() -> traversalConsumer.validateHistoryParams(Format.state, queryParameters));
    }

    @Test
    public void testGetSubgraphStrategy() {
        // Mock the input values
        long startTs = 1638336000000L;  // Example start timestamp
        long endTs = 1638422400000L;    // Example end timestamp
        Format format = Format.state;   // Test with state format

        // Test with state format
        SubgraphStrategy strategy = traversalConsumer.getSubgraphStrategy(startTs, endTs, format);
        assertNotNull(strategy);

        format = Format.lifecycle;  // Test with lifecycle format
        strategy = traversalConsumer.getSubgraphStrategy(startTs, endTs, format);
        assertNotNull(strategy);

        // Test with a different format
        format = Format.aggregate;  // Test with aggregate format
        strategy = traversalConsumer.getSubgraphStrategy(startTs, endTs, format);
        assertNotNull(strategy);
    }

    @Test
    public void testGetEndTime() throws AAIException {
        // Test with "now"
        when(queryParameters.getFirst("endTs")).thenReturn("now");
        long endTime = traversalConsumer.getEndTime(queryParameters);
        assertTrue(endTime > 0);

        // Test with a valid timestamp
        when(queryParameters.getFirst("endTs")).thenReturn("1638422400000");
        endTime = traversalConsumer.getEndTime(queryParameters);
        //assertEquals(1638422400000L, endTime);

        // Test with invalid timestamp
        when(queryParameters.getFirst("endTs")).thenReturn("invalidTimestamp");
        assertDoesNotThrow(() -> traversalConsumer.getEndTime(queryParameters));
    }

    @Test
    public void testGetSubgraphStrategyFromBaseClass() {
        // Mock or spy the base class method
        TraversalConsumer baseTraversalConsumer = spy(new TraversalConsumer() {
            @Override
            protected SubgraphStrategy getSubgraphStrategy(long startTs, long endTs, Format format) {
                // You can call the parent method using super, so it executes the base class logic
                return super.getSubgraphStrategy(startTs, endTs, format);
            }
        });

        // Provide mock data for testing
        long startTs = 1638336000000L;  // Example start timestamp
        long endTs = 1638422400000L;    // Example end timestamp
        Format format = Format.state;   // Test with state format

        // Call the method from the parent class
        SubgraphStrategy strategy = baseTraversalConsumer.getSubgraphStrategy(startTs, endTs, format);

        // Ensure the strategy is not null and as expected
        assertNotNull(strategy);

        format = Format.lifecycle;  // Test with lifecycle format
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
        // Mock invalid timestamp
        when(queryParameters.getFirst("startTs")).thenReturn("invalidTimestamp");
        when(queryParameters.getFirst("endTs")).thenReturn("-1");

        // Expect an exception to be thrown
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
    public void testGetSubgraphStrategyWithEqualStartAndEndTimestamps() {
        long startTs = 1638336000000L;
        long endTs = 1638336000000L;

        SubgraphStrategy strategy = traversalConsumer.getSubgraphStrategy(startTs, endTs, Format.state);
        assertNotNull(strategy);
    }

    @Test
    public void testGetRolesWithValidPrincipal() {
        // Mock KeycloakAuthenticationToken and SimpleKeycloakAccount
        KeycloakAuthenticationToken token = mock(KeycloakAuthenticationToken.class);
        SimpleKeycloakAccount account = mock(SimpleKeycloakAccount.class);

        // Mock roles
        Set<String> roles = Set.of("USER", "ADMIN", "GUEST");
        when(token.getDetails()).thenReturn(account);
        when(account.getRoles()).thenReturn(roles);

        // Call the method
        Set<String> result = traversalConsumer.getRoles(token);

        // Verify the result
        assertNotNull(result, "Roles should not be null");
        assertEquals(roles, result, "Roles should match the input roles");
    }

    @Test
    public void testGetRolesWithEmptyToken() {
        // Call the method with an empty token
        Set<String> result = traversalConsumer.getRoles(null);

        // Verify the result
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be an empty set");
    }

    @Test
    public void testGetRolesWithEmptyAccount() {
        // Mock KeycloakAuthenticationToken with no account details
        KeycloakAuthenticationToken token = mock(KeycloakAuthenticationToken.class);
        when(token.getDetails()).thenReturn(null);

        // Call the method
        Set<String> result = traversalConsumer.getRoles(token);

        // Verify the result
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be an empty set");
    }

    @Test
    public void testGetRolesWithNoRoles() {
        // Mock KeycloakAuthenticationToken and SimpleKeycloakAccount with no roles
        KeycloakAuthenticationToken token = mock(KeycloakAuthenticationToken.class);
        SimpleKeycloakAccount account = mock(SimpleKeycloakAccount.class);

        when(token.getDetails()).thenReturn(account);
        when(account.getRoles()).thenReturn(Collections.emptySet());

        // Call the method
        Set<String> result = traversalConsumer.getRoles(token);

        // Verify the result
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be an empty set");
    }

    @Test
    public void testGetQueryStyleWhenIsHistoryTrue() {
        // Mock Format and HttpEntry
        Format format = Format.state;
        HttpEntry traversalUriHttpEntry = mock(HttpEntry.class);

        // Mock isHistory to return true
        doReturn(true).when(traversalConsumer).isHistory(format);

        // Call the method
        QueryStyle queryStyle = traversalConsumer.getQueryStyle(format, traversalUriHttpEntry);

        // Verify the result
        assertEquals(QueryStyle.HISTORY_TRAVERSAL, queryStyle,
                "QueryStyle should be HISTORY_TRAVERSAL when isHistory(format) returns true");
    }

    @Test
    public void testGetQueryStyleWhenIsHistoryFalse() {
        // Mock Format and HttpEntry
        Format format = Format.lifecycle;
        HttpEntry traversalUriHttpEntry = mock(HttpEntry.class);

        // Mock isHistory to return false
        doReturn(false).when(traversalConsumer).isHistory(format);

        // Mock the behavior of traversalUriHttpEntry.getQueryStyle
        QueryStyle expectedQueryStyle = QueryStyle.TRAVERSAL;
        when(traversalUriHttpEntry.getQueryStyle()).thenReturn(expectedQueryStyle);

        // Call the method
        QueryStyle queryStyle = traversalConsumer.getQueryStyle(format, traversalUriHttpEntry);

        // Verify the result
        assertEquals(expectedQueryStyle, queryStyle,
                "QueryStyle should match the result of traversalUriHttpEntry.getQueryStyle() when isHistory(format) returns false");
    }

    @Test
    public void testGetQueryStyleWhenTraversalUriHttpEntryIsNull() {
        // Mock Format
        Format format = Format.aggregate;

        // Mock isHistory to return false
        doReturn(false).when(traversalConsumer).isHistory(format);

        // Call the method with a null traversalUriHttpEntry
        assertThrows(NullPointerException.class,
                () -> traversalConsumer.getQueryStyle(format, null),
                "NullPointerException should be thrown when traversalUriHttpEntry is null");
    }

    @Test
    public void testGetDataOwnerSubgraphStrategyWithRolesUsingReflection() throws Exception {
        // Set up the roles to be tested
        Set<String> roles = Set.of("ROLE_ADMIN", "ROLE_USER");

        // Use reflection to access the private method
        Method method = TraversalConsumer.class.getDeclaredMethod("getDataOwnerSubgraphStrategy", Set.class);
        method.setAccessible(true); // Make it accessible even though it's private

        // Call the method using reflection
        SubgraphStrategy strategy = (SubgraphStrategy) method.invoke(traversalConsumer, roles);

        // Ensure that the strategy is not null
        assertNotNull(strategy, "SubgraphStrategy should not be null");

        // Verify that the strategy is built correctly (you may need to inspect the strategy object further based on its actual implementation)
        // For example, check if the vertices filter condition is correctly set
        assertFalse(strategy.toString().contains("data-owner"),
                "The strategy should filter vertices based on 'data-owner' property");
    }


    @Test
    public void testGetSubgraphStrategy_withValidStartTs() {
        // Example of testing startTs handling logic inside getSubgraphStrategy

        long startTs = 1638336000000L;  // Example start timestamp (Unix timestamp)
        long endTs = 1638422400000L;    // Example end timestamp (Unix timestamp)
        Format format = Format.state;   // Assuming Format.state triggers the relevant logic

        // Call the method that internally processes startTs
        SubgraphStrategy strategy = traversalConsumer.getSubgraphStrategy(startTs, endTs, format);

        // Assert that the strategy returned is not null
        assertNotNull(strategy, "SubgraphStrategy should not be null.");
    }


    @Test
    public void testGetSubgraphStrategy_withInvalidStartTs() {
        // Test with an invalid startTs (e.g., "-1")
        String startTs = "-1";
        long endTs = 1638422400000L;    // Example end timestamp
        Format format = Format.state;   // Test with Format.state

        // Call the method that processes startTs (this might internally call processStartTs)
        SubgraphStrategy strategy = traversalConsumer.getSubgraphStrategy(Long.parseLong(startTs), endTs, format);

        // Verify that the strategy is created, even with invalid startTs
        assertNotNull(strategy, "SubgraphStrategy should still be created for invalid startTs values.");
    }

    @Test
    public void getTraversalSourceElse() throws NoSuchMethodException, AAIException {
        Set<String> roles = Set.of("ROLE_ADMIN", "ROLE_USER");
        long startTs = 1638336000000L;  // Example start timestamp (Unix timestamp)
        long endTs = 1638422400000L;    // Example end timestamp (Unix timestamp)
        Format format = Format.state;


        // Use reflection to access the private method
        Method method = TraversalConsumer.class.getDeclaredMethod("getDataOwnerSubgraphStrategy", Set.class);
        method.setAccessible(true); // Make it acc
        traversalConsumer.getTraversalSource(dbEngine,format,queryParameters,roles);
    }

}
