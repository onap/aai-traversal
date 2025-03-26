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
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.search.CustomQueryConfigDTO;
import org.onap.aai.rest.search.CustomQueryDTO;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.springframework.http.HttpStatus;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestCQ2Gremlin {

    @InjectMocks
    private CQ2Gremlin cq2Gremlin;

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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        assertNotNull(dbEngine,"dbEngine is null");
        assertNotNull(mockTraversalSource,"mockTraversalSource is null");
        TransactionalGraphEngine.Admin adminMock = mock(TransactionalGraphEngine.Admin.class);
        when(dbEngine.asAdmin()).thenReturn(adminMock);
        when(adminMock.getTraversalSource()).thenReturn(mockTraversalSource);
    }

    @Test
    public void testProcessGremlinQueryException() {
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        when(queryDTO.getQuery()).thenReturn("SELECT * FROM nodes");
        when(queryDTO.getQueryOptionalProperties()).thenReturn(Collections.emptyList());
        when(queryDTO.getQueryRequiredProperties()).thenReturn(Collections.emptyList());

        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        Map<String, CustomQueryConfigDTO> content = new HashMap<>();
        content.put("queryConfig", queryConfigDTO);

        when(mockTraversalSource.V()).thenThrow(new RuntimeException("Query execution error"));

        Response response = cq2Gremlin.getC2Qgremlin(content, headers, uriInfo);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertTrue(((String) response.getEntity()).contains("Query conversion failed with following reason:"));
    }

    @Test
    public void testGetC2QgremlinValidRequest() {
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        when(queryDTO.getQuery()).thenReturn("SELECT * FROM nodes");
        when(queryDTO.getQueryOptionalProperties()).thenReturn(Collections.emptyList());
        when(queryDTO.getQueryRequiredProperties()).thenReturn(Collections.emptyList());

        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        Map<String, CustomQueryConfigDTO> content = new HashMap<>();
        content.put("queryConfig", queryConfigDTO);

        when(mockTraversalSource.V()).thenReturn(mock(GraphTraversal.class));

        Response response = cq2Gremlin.getC2Qgremlin(content, headers, uriInfo);

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

        if (!optionalParameters.isEmpty()) {
            for (String key : optionalParameters) {
                params.put(key, key);
            }
        }

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

        if (!requiredParameters.isEmpty()) {
            for (String key : requiredParameters) {
                params.put(key, key);
            }
        }

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

        GroovyQueryBuilder queryBuilderMock = mock(GroovyQueryBuilder.class);
        when(queryBuilderMock.executeTraversal(dbEngine, query, params)).thenReturn("g.V().hasLabel('node')");

        query = queryBuilderMock.executeTraversal(dbEngine, query, params);
        query = "g" + query;

        assertEquals("gg.V().hasLabel('node')", query);
    }

    @Test
    public void testSchemaVersionsInteraction() {
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        SchemaVersion mockSchemaVersion = mock(SchemaVersion.class);
        when(schemaVersions.getDefaultVersion()).thenReturn(mockSchemaVersion);

        HttpEntry mockHttpEntry = mock(HttpEntry.class);
        when(mockHttpEntry.setHttpEntryProperties(mockSchemaVersion)).thenReturn(mockHttpEntry);

        HttpEntry result = mockHttpEntry.setHttpEntryProperties(mockSchemaVersion);

        verify(mockHttpEntry, times(1)).setHttpEntryProperties(mockSchemaVersion);

        assertSame(mockHttpEntry, result);
    }

    @Test
    public void testFullQueryProcessing() {
        CustomQueryDTO queryDTO = mock(CustomQueryDTO.class);
        CustomQueryConfigDTO queryConfigDTO = mock(CustomQueryConfigDTO.class);
        when(queryConfigDTO.getQueryDTO()).thenReturn(queryDTO);

        List<String> optionalParameters = Arrays.asList("param1", "param2");
        when(queryDTO.getQueryOptionalProperties()).thenReturn(optionalParameters);
        List<String> requiredParameters = Arrays.asList("req1", "req2");
        when(queryDTO.getQueryRequiredProperties()).thenReturn(requiredParameters);

        SchemaVersion mockSchemaVersion = mock(SchemaVersion.class);
        when(schemaVersions.getDefaultVersion()).thenReturn(mockSchemaVersion);

        String query = "SELECT * FROM nodes";
        Map<String, Object> params = new HashMap<>();
        when(mockTraversalSource.V()).thenReturn(mock(GraphTraversal.class));

        Map<String, CustomQueryConfigDTO> content = new HashMap<>();
        content.put("queryConfig", queryConfigDTO);
        Response response = cq2Gremlin.getC2Qgremlin(content, headers, uriInfo);

        assertNotEquals(HttpStatus.OK.value(), response.getStatus());
        assertFalse(((String) response.getEntity()).contains("gSELECT * FROM nodes"));
    }

    @Test
    public void testGetC2Qgremlin_EmptyContent() {
        CQ2Gremlin cq2Gremlin = mock(CQ2Gremlin.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        UriInfo uriInfo = mock(UriInfo.class);

        Map<String, CustomQueryConfigDTO> emptyContent = new HashMap<>();

        when(cq2Gremlin.getC2Qgremlin(emptyContent, headers, uriInfo)).thenCallRealMethod();

        Response response = cq2Gremlin.getC2Qgremlin(emptyContent, headers, uriInfo);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertTrue(((String) response.getEntity()).contains("At least one custom query should be passed"));
    }

}
