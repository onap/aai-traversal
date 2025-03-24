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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.aai.restclient.RestClient;
import org.springframework.http.ResponseEntity;
import java.lang.reflect.Field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SchemaServiceCQConfigTest {

    @InjectMocks
    private SchemaServiceCQConfig schemaServiceCQConfig;

    @Mock
    private RestClient restClient;

    @Mock
    private ResponseEntity<String> schemaResponse;

    @Mock
    private GetCustomQueryConfig mockQueryConfig;

    @Test
    public void testGetStoredQuery_Success() {

        String queryJson = """
        {
            "stored-queries": [
                {
                    "query": {
                        "stored-query": "SELECT * FROM users",
                        "required-properties": ["user_id"],
                        "optional-properties": ["user_name"]
                    }
                }
            ]
        }
    """;

        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(queryJson);
        CustomQueryConfig result = getCustomQueryConfig.getStoredQuery("query");

        assertNotNull(result);
        assertEquals("SELECT * FROM users", result.getQuery(), "Stored query should match expected query");
    }

    @Test
    public void testRetrieveCustomQueries_EmptyResponse() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            schemaServiceCQConfig.retrieveCustomQueries();
        });
             String expectedMessage = "Cannot invoke \"org.springframework.http.ResponseEntity.getBody()\" because \"schemaResponse\" is null";  // Replace with your expected message
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }

    @Test
    public void testGetStoredQuery_QueryNotFound() {
        String queryJson = "{\"stored-queries\":[]}";
        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(queryJson);

        CustomQueryConfig result = getCustomQueryConfig.getStoredQuery("nonexistentQuery");

        assertNull(result, "CustomQueryConfig should be null when query is not found");
    }

    @Test
    public void testSchemaServiceUriInjection() throws NoSuchFieldException, IllegalAccessException {
        SchemaServiceCQConfig schemaServiceCQConfig = new SchemaServiceCQConfig();

        Field field = SchemaServiceCQConfig.class.getDeclaredField("customQueriesUri");
        field.setAccessible(true);

        field.set(schemaServiceCQConfig, "http://example.com/schema-service/queries");

        assertNull(schemaServiceCQConfig.getCustomQueryConfig());
    }

    @Test
    public void testCustomQueriesUri() throws NoSuchFieldException, IllegalAccessException {
        SchemaServiceCQConfig schemaServiceCQConfig = new SchemaServiceCQConfig();

        Field field = SchemaServiceCQConfig.class.getDeclaredField("customQueriesUri");
        field.setAccessible(true);

        field.set(schemaServiceCQConfig, "http://example.com/schema-service/queries");

        assertEquals("http://example.com/schema-service/queries", field.get(schemaServiceCQConfig));
    }

    @Test
    public void testInitialize_ShouldInvokeRetrieveCustomQueries() {
        SchemaServiceCQConfig schemaServiceCQConfig = spy(new SchemaServiceCQConfig());

        doNothing().when(schemaServiceCQConfig).retrieveCustomQueries();

        schemaServiceCQConfig.initialize();

        verify(schemaServiceCQConfig, times(1)).retrieveCustomQueries();
    }
}
