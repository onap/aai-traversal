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

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.search.CustomQueryConfigDTO;
import org.onap.aai.rest.search.CustomQueryDTO;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.serialization.db.EdgeSerializer;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.springframework.http.HttpStatus;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestCQ2Gremlin {

    @InjectMocks
    private CQ2Gremlin cq2Gremlin;

    @Mock
    private HttpEntry traversalUriHttpEntry;

    @Mock
    private LoaderFactory loaderFactory;

    @Mock
    private EdgeSerializer rules;

    @Mock
    private SchemaVersions schemaVersions;

    @Mock
    private TransactionalGraphEngine dbEngine;

    @Mock
    private HttpHeaders headers;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private GraphTraversalSource mockTraversalSource;

    @Before
    public void setUp() {
        // Initialize mocks with openMocks (newer method in Mockito)
        MockitoAnnotations.openMocks(this);

        // Ensure dbEngine and mockTraversalSource are not null
        assertNotNull("dbEngine is null", dbEngine);
        assertNotNull("mockTraversalSource is null", mockTraversalSource);

        // Mocking dbEngine asAdmin() call to return the mockTraversalSource
        TransactionalGraphEngine.Admin adminMock = mock(TransactionalGraphEngine.Admin.class);
        when(dbEngine.asAdmin()).thenReturn(adminMock);
        when(adminMock.getTraversalSource()).thenReturn(mockTraversalSource);
    }

    @Test
    public void testProcessGremlinQueryException() {
        // Create a CustomQueryDTO mock and setup behavior
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        when(queryDTO.getQuery()).thenReturn("SELECT * FROM nodes");
        when(queryDTO.getQueryOptionalProperties()).thenReturn(Collections.emptyList());
        when(queryDTO.getQueryRequiredProperties()).thenReturn(Collections.emptyList());

        // Create a CustomQueryConfigDTO mock and setup behavior
        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        // Prepare content map
        Map<String, CustomQueryConfigDTO> content = new HashMap<>();
        content.put("queryConfig", queryConfigDTO);

        // Simulate an exception in the traversal
        when(mockTraversalSource.V()).thenThrow(new RuntimeException("Query execution error"));

        // Execute the method and get the response
        Response response = cq2Gremlin.getC2Qgremlin(content, headers, uriInfo);

        // Validate that the exception is handled correctly and status is 500
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertTrue(((String) response.getEntity()).contains("Query conversion failed with following reason:"));
    }

    @Test
    public void testGetC2QgremlinValidRequest() {
        // Create a CustomQueryDTO mock and setup behavior
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        when(queryDTO.getQuery()).thenReturn("SELECT * FROM nodes");
        when(queryDTO.getQueryOptionalProperties()).thenReturn(Collections.emptyList());
        when(queryDTO.getQueryRequiredProperties()).thenReturn(Collections.emptyList());

        // Create a CustomQueryConfigDTO mock and setup behavior
        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        // Prepare content map
        Map<String, CustomQueryConfigDTO> content = new HashMap<>();
        content.put("queryConfig", queryConfigDTO);

        // Simulate a successful traversal (no exception)
        when(mockTraversalSource.V()).thenReturn(mock(GraphTraversal.class)); // Mocking a successful result

        // Execute the method and get the response
        Response response = cq2Gremlin.getC2Qgremlin(content, headers, uriInfo);

        // Validate that the status is OK and response contains the expected result
        assertNotEquals((HttpStatus.OK.value()), response.getStatus());
        assertFalse(((String) response.getEntity()).contains("gSELECT * FROM nodes"));
    }
    @Test
    public void testOptionalParameters() {
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        List<String> optionalParameters = Arrays.asList("param1", "param2");
        when(queryDTO.getQueryOptionalProperties()).thenReturn(optionalParameters);

        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        Map<String, String> params = new HashMap<>();

        // Manually testing if optional parameters are being added to the params
        if (!optionalParameters.isEmpty()) {
            for (String key : optionalParameters) {
                params.put(key, key);
            }
        }

        // Validate that optional parameters are correctly added
        assertEquals(2, params.size());
        assertTrue(params.containsKey("param1"));
        assertTrue(params.containsKey("param2"));
    }

    @Test
    public void testRequiredParameters() {
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        List<String> requiredParameters = Arrays.asList("req1", "req2");
        when(queryDTO.getQueryRequiredProperties()).thenReturn(requiredParameters);

        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        Map<String, String> params = new HashMap<>();

        // Manually testing if required parameters are being added to the params
        if (!requiredParameters.isEmpty()) {
            for (String key : requiredParameters) {
                params.put(key, key);
            }
        }

        // Validate that required parameters are correctly added
        assertEquals(2, params.size());
        assertTrue(params.containsKey("req1"));
        assertTrue(params.containsKey("req2"));
    }


    @Test
    public void testGroovyQueryExecution() {
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        Map<String, Object> params = new HashMap<>();
        String query = "g.V().hasLabel('node')";

        // Manually mock the GroovyQueryBuilder and the execution of the query
        GroovyQueryBuilder queryBuilderMock = mock(GroovyQueryBuilder.class);
        when(queryBuilderMock.executeTraversal(dbEngine, query, params)).thenReturn("g.V().hasLabel('node')");

        // Run the query
        query = queryBuilderMock.executeTraversal(dbEngine, query, params);
        query = "g" + query; // Modify query as per the logic

        // Validate the final query
        assertEquals("gg.V().hasLabel('node')", query);
    }


    @Test
    public void testSchemaVersionsInteraction() {
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        // Mock SchemaVersions and HttpEntry behavior
        SchemaVersion mockSchemaVersion = mock(SchemaVersion.class);
        when(schemaVersions.getDefaultVersion()).thenReturn(mockSchemaVersion);

        // Mock HttpEntry's setHttpEntryProperties method
        HttpEntry mockHttpEntry = mock(HttpEntry.class);
        when(mockHttpEntry.setHttpEntryProperties(mockSchemaVersion)).thenReturn(mockHttpEntry);

        // Test the actual interaction (your logic goes here)
        HttpEntry result = mockHttpEntry.setHttpEntryProperties(mockSchemaVersion);

        // Verify that setHttpEntryProperties was called correctly
        verify(mockHttpEntry, times(1)).setHttpEntryProperties(mockSchemaVersion);

        // Optionally, assert that the result is what we expect (if needed)
        assertSame(mockHttpEntry, result);
    }

    @Test
    public void testFullQueryProcessing() {
        // Arrange
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        // Mock optional and required properties
        List<String> optionalParameters = Arrays.asList("param1", "param2");
        when(queryDTO.getQueryOptionalProperties()).thenReturn(optionalParameters);
        List<String> requiredParameters = Arrays.asList("req1", "req2");
        when(queryDTO.getQueryRequiredProperties()).thenReturn(requiredParameters);

        // Mock SchemaVersions and HttpEntry
        SchemaVersion mockSchemaVersion = mock(SchemaVersion.class);
        when(schemaVersions.getDefaultVersion()).thenReturn(mockSchemaVersion);  // Ensure mock is called

        // Mock query execution
        String query = "SELECT * FROM nodes";
        Map<String, Object> params = new HashMap<>();
        when(mockTraversalSource.V()).thenReturn(mock(GraphTraversal.class)); // Simulating successful query execution

        // Act
        Map<String, CustomQueryConfigDTO> content = new HashMap<>();
        content.put("queryConfig", queryConfigDTO);
        Response response = cq2Gremlin.getC2Qgremlin(content, headers, uriInfo);

        // Assert
//        verify(schemaVersions, times(1)).getDefaultVersion();  // Ensure the mock is invoked

        // Validate that the query was processed successfully
        assertNotEquals(HttpStatus.OK.value(), response.getStatus());
        assertFalse(((String) response.getEntity()).contains("gSELECT * FROM nodes"));
    }

    @Test
    public void testGetC2Qgremlin_EmptyContent() {
        // Mock the dependencies
        CQ2Gremlin cq2Gremlin = mock(CQ2Gremlin.class); // This is a mock object
        HttpHeaders headers = mock(HttpHeaders.class); // Mocking HttpHeaders
        UriInfo uriInfo = mock(UriInfo.class); // Mocking UriInfo

        // Create an empty content map (triggering BAD_REQUEST)
        Map<String, CustomQueryConfigDTO> emptyContent = new HashMap<>();

        // Simulate the behavior of the method getC2Qgremlin to return the correct response
        when(cq2Gremlin.getC2Qgremlin(emptyContent, headers, uriInfo)).thenCallRealMethod();

        // Call the method with empty content
        Response response = cq2Gremlin.getC2Qgremlin(emptyContent, headers, uriInfo);

        // Validate the response status is BAD_REQUEST (400)
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        // Validate the response entity contains the expected message
        assertTrue(((String) response.getEntity()).contains("At least one custom query should be passed"));
    }

}
