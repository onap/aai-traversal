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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

public class GremlinServerSingletonTest {

    private GremlinServerSingleton gremlinServerSingleton;
    private CQConfig customQueryInfo;
    private GetCustomQueryConfig getCustomQueryConfig;
    private CustomQueryConfig customQueryConfig;

    @BeforeEach
    public void setUp() {
        customQueryInfo = Mockito.mock(CQConfig.class);
        getCustomQueryConfig = Mockito.mock(GetCustomQueryConfig.class);
        customQueryConfig = Mockito.mock(CustomQueryConfig.class);
        Mockito.when(customQueryInfo.getCustomQueryConfig()).thenReturn(getCustomQueryConfig);
        gremlinServerSingleton = new GremlinServerSingleton(customQueryInfo);
    }

    @Test
    public void testGetStoredQueryFromConfig_QueryExists() {
        String key = "testKey";
        String expectedQuery = "MATCH (n) RETURN n";
        Mockito.when(getCustomQueryConfig.getStoredQuery(key)).thenReturn(customQueryConfig);
        Mockito.when(customQueryConfig.getQuery()).thenReturn(expectedQuery);

        String query = gremlinServerSingleton.getStoredQueryFromConfig(key);
        assertNotNull(query);
        assertEquals(expectedQuery, query);
    }

    @Test
    public void testGetStoredQueryFromConfig_QueryDoesNotExist() {
        String key = "invalidKey";
        Mockito.when(getCustomQueryConfig.getStoredQuery(key)).thenReturn(null);

        String query = gremlinServerSingleton.getStoredQueryFromConfig(key);
        assertNull(query);
    }

    @Test
    public void testGetCustomQueryConfig_QueryExists() {
        String key = "testKey";
        Mockito.when(getCustomQueryConfig.getStoredQuery(key)).thenReturn(customQueryConfig);

        CustomQueryConfig result = gremlinServerSingleton.getCustomQueryConfig(key);
        assertNotNull(result);
        assertEquals(customQueryConfig, result);
    }

    @Test
    public void testGetCustomQueryConfig_QueryDoesNotExist() {
        String key = "invalidKey";
        Mockito.when(getCustomQueryConfig.getStoredQuery(key)).thenReturn(null);

        CustomQueryConfig result = gremlinServerSingleton.getCustomQueryConfig(key);
        assertNull(result);
    }
}
