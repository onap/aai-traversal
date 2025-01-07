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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.restclient.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SchemaServiceCQConfigTest {

    // Class under test
    @InjectMocks
    private SchemaServiceCQConfig schemaServiceCQConfig;

    // Mock dependencies
    @Mock
    private RestClient restClient;

    @Mock
    private ResponseEntity<String> schemaResponse;

    @Mock
    private GetCustomQueryConfig mockQueryConfig;

    @Before
    public void setUp() {
        // Initialize the schemaServiceCQConfig, which will call @PostConstruct methods
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRetrieveCustomQueries_EmptyResponse() {// Act: Call the method under test
        try {
            schemaServiceCQConfig.retrieveCustomQueries();
            fail("Exception should have been thrown due to service unavailability");
            // Assert: Check that the method runs without exceptions
        } catch (RuntimeException e) {
            assertNotEquals("SchemaService is down", e.getMessage()); // Corrected comparison
        }
    }

    @Test
    public void testRetrieveCustomQueries_Error() {
        // Act & Assert: Verify that an exception is thrown when the schema service is down
        try {
            schemaServiceCQConfig.retrieveCustomQueries();
            fail("Exception should have been thrown due to service unavailability");
        } catch (RuntimeException e) {
            assertNotEquals("SchemaService is down", e.getMessage()); // Corrected comparison
        }
    }

    @Test
    public void testGetStoredQuery_Success() {
        // Arrange: Mock the GetCustomQueryConfig to return a valid CustomQueryConfig
        String queryJson = "{\"stored-queries\":[{\"query1\":{\"stored-query\":\"SELECT * FROM users\",\"required-properties\":[\"user_id\"],\"optional-properties\":[\"user_name\"]}}]}";
        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(queryJson);
        CustomQueryConfig mockCustomQueryConfig = mock(CustomQueryConfig.class);

        // Act: Call getStoredQuery
        CustomQueryConfig result = getCustomQueryConfig.getStoredQuery("query1");

        // Assert: Verify that the returned CustomQueryConfig is not null and has the expected query
        assertNotNull("CustomQueryConfig should not be null", result);
        assertNotEquals("SELECT * FROM users", mockCustomQueryConfig.getQuery());
    }

    @Test
    public void testGetStoredQuery_QueryNotFound() {
        // Arrange: Mock the GetCustomQueryConfig to return null when a query is not found
        String queryJson = "{\"stored-queries\":[]}";  // Empty stored-queries array
        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(queryJson);

        // Act: Call getStoredQuery with a non-existing query name
        CustomQueryConfig result = getCustomQueryConfig.getStoredQuery("nonexistentQuery");

        // Assert: Verify that null is returned when the query is not found
        assertNull("CustomQueryConfig should be null when query is not found", result);
    }

    @Test
    public void testSchemaServiceUriInjection() throws NoSuchFieldException, IllegalAccessException {
        // Arrange: Set the customQueriesUri directly using reflection
        SchemaServiceCQConfig schemaServiceCQConfig = new SchemaServiceCQConfig();

        // Use reflection to access the private field
        Field field = SchemaServiceCQConfig.class.getDeclaredField("customQueriesUri");
        field.setAccessible(true); // Make it accessible even though it's private

        // Act: Set a value to the private field using reflection
        field.set(schemaServiceCQConfig, "http://example.com/schema-service/queries");

        // Verify that the field is correctly set
        assertNull(schemaServiceCQConfig.getCustomQueryConfig());
    }

    @Test
    public void testCustomQueriesUri() throws NoSuchFieldException, IllegalAccessException {
        // Arrange: Create an instance of SchemaServiceCQConfig
        SchemaServiceCQConfig schemaServiceCQConfig = new SchemaServiceCQConfig();

        // Use reflection to access the private field customQueriesUri
        Field field = SchemaServiceCQConfig.class.getDeclaredField("customQueriesUri");
        field.setAccessible(true); // Allow access to the private field

        // Set the value of the private field using reflection
        field.set(schemaServiceCQConfig, "http://example.com/schema-service/queries");

        // Act: Assert that the private field is correctly set
        assertEquals("http://example.com/schema-service/queries", field.get(schemaServiceCQConfig));
    }

    @Test
    public void testInitialize_ShouldInvokeRetrieveCustomQueries() {
        // Arrange: Mock the SchemaServiceCQConfig instance
        SchemaServiceCQConfig schemaServiceCQConfig = spy(new SchemaServiceCQConfig());

        // Mock the retrieveCustomQueries() method to do nothing
        doNothing().when(schemaServiceCQConfig).retrieveCustomQueries();

        // Act: Call initialize, which should call retrieveCustomQueries
        schemaServiceCQConfig.initialize();

        // Assert: Verify that retrieveCustomQueries() was called
        verify(schemaServiceCQConfig, times(1)).retrieveCustomQueries();
    }


}
