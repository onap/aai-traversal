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
package org.onap.aai.interceptors.pre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.aai.TraversalApp;
import org.onap.aai.interceptors.AAIHeaderProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class) // Ensures Spring context is available in tests
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TraversalApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = RequestTransactionLoggingTest.TestConfig.class)  // Load the custom config for this test
public class RequestTransactionLoggingTest {

    @InjectMocks
    private RequestTransactionLogging requestTransactionLogging;

    @Mock
    private ContainerRequestContext mockRequestContext;

    @Mock
    private MultivaluedMap<String, String> mockHeaders;

    @Mock
    private UriInfo mockUriInfo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
        resetMocks(); // Reset mock states

        when(mockRequestContext.getHeaders()).thenReturn(mockHeaders);
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);

        when(mockHeaders.getFirst("Content-Type")).thenReturn(null);
        when(mockHeaders.getFirst("Accept")).thenReturn("application/xml");

        doNothing().when(mockHeaders).putSingle(anyString(), anyString());
        when(mockHeaders.containsKey("Content-Type")).thenReturn(false);
        when(mockHeaders.containsKey("Accept")).thenReturn(true);
    }

    private void resetMocks() {
        // Resetting mocks to avoid shared state between tests
        Mockito.reset(mockRequestContext, mockHeaders, mockUriInfo);
    }

    @Test
    void testFilter_noContentTypeHeader_shouldNotModifyRequest() throws IOException {
        // Prepare mock responses
        when(mockHeaders.getFirst("Content-Type")).thenReturn(null);
        when(mockHeaders.getFirst("Accept")).thenReturn("application/xml");
        when(mockRequestContext.getUriInfo()).thenReturn(null);

        // Run the filter
        requestTransactionLogging.filter(mockRequestContext);

        // Verify that Content-Type is added and Accept is preserved
        verify(mockHeaders).putSingle("Content-Type", "application/json");
        verify(mockHeaders).putSingle("Accept", "application/xml");
    }

    @Test
    void testFilter_shouldSetTransactionIdAndRequestProperties() throws IOException {
        // Set mock data for headers and uri info
        when(mockHeaders.getFirst("Content-Type")).thenReturn("application/json");
        when(mockHeaders.getFirst("Accept")).thenReturn("*/*");
        when(mockUriInfo.getPath()).thenReturn("/test/path");

        // Run the filter
        requestTransactionLogging.filter(mockRequestContext);

        // Verify that the correct properties are set
        verify(mockRequestContext).setProperty(eq(AAIHeaderProperties.AAI_TX_ID), anyString());
        verify(mockRequestContext).setProperty(eq(AAIHeaderProperties.AAI_REQUEST), anyString());
        verify(mockRequestContext).setProperty(eq(AAIHeaderProperties.AAI_REQUEST_TS), anyString());

        // Verify that headers are set properly
        verify(mockRequestContext.getHeaders()).putSingle(eq("Content-Type"), eq("application/json"));
        verify(mockRequestContext.getHeaders()).putSingle(eq("Accept"), eq("application/xml"));
    }

    @Test
    void testFilter_withDslPath_shouldSetAcceptHeaderToJson() throws IOException {
        // Mock URI path for DSL request
        when(mockUriInfo.getPath()).thenReturn("/some/dsl");
        when(mockHeaders.getFirst("Content-Type")).thenReturn("application/json");

        // Run the filter
        requestTransactionLogging.filter(mockRequestContext);

        // Verify that the Accept header is set to application/json
        verify(mockRequestContext.getHeaders()).putSingle("Accept", "application/json");
    }

    @Test
    void testFilter_withQueryPath_shouldSetAcceptHeaderToJson() throws IOException {
        // Mock URI path for query request
        when(mockUriInfo.getPath()).thenReturn("/some/query");

        // Run the filter
        requestTransactionLogging.filter(mockRequestContext);

        // Verify that the Accept header is set to application/json
        verify(mockRequestContext.getHeaders()).putSingle("Accept", "application/json");
    }

    @Test
    void testFilter_withRecentsPath_shouldSetAcceptHeaderToJson() throws IOException {
        // Mock URI path for recents request
        when(mockUriInfo.getPath()).thenReturn("/some/recents/data");

        // Run the filter
        requestTransactionLogging.filter(mockRequestContext);

        // Verify that the Accept header is set to application/json
        verify(mockRequestContext.getHeaders()).putSingle("Accept", "application/json");
    }

    @Test
    void testFilter_withOtherPath_shouldSetAcceptHeaderToDefault() throws IOException {
        // Mock URI path for other request
        when(mockUriInfo.getPath()).thenReturn("/some/other/path");

        // Run the filter
        requestTransactionLogging.filter(mockRequestContext);

        // Verify that Accept and Content-Type are set to their default values
        verify(mockRequestContext.getHeaders()).putSingle("Accept", "application/xml");
        verify(mockRequestContext.getHeaders()).putSingle("Content-Type", "application/json");
    }

    @Test
    void testGetAAITxIdToHeader_shouldGenerateTxIdWithTimestamp() throws Exception {
        String currentTimeStamp = "20241211";
        Method method = RequestTransactionLogging.class.getDeclaredMethod("getAAITxIdToHeader", String.class);
        method.setAccessible(true);

        String txId = (String) method.invoke(requestTransactionLogging, currentTimeStamp);

        // Verify that the generated transaction ID contains the timestamp
        assertNotNull(txId);
        assertTrue(txId.contains(currentTimeStamp), "Transaction ID should contain the timestamp");
    }

    @Test
    void testGetRequest_shouldCreateRequestJson() throws Exception {
        String fullId = "12345-abc";
        Method method = RequestTransactionLogging.class.getDeclaredMethod("getRequest", ContainerRequestContext.class, String.class);
        method.setAccessible(true);

        String requestJson = (String) method.invoke(requestTransactionLogging, mockRequestContext, fullId);

        // Verify that the generated request JSON contains expected fields
        assertNotNull(requestJson);
        assertTrue(requestJson.contains("ID"));
        assertTrue(requestJson.contains("Http-Method"));
        assertTrue(requestJson.contains("Headers"));
    }

    // Custom configuration for this test class to isolate the beans
    @Configuration
    static class TestConfig {
        @Bean
        public RequestTransactionLogging requestTransactionLogging() {
            return new RequestTransactionLogging();
        }

        // Any other beans that need to be mocked for this test class can go here
    }
}
