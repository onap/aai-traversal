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
package org.onap.aai.interceptors.post;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.interceptors.AAIHeaderProperties;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class ResponseHeaderManipulationTest {

    @InjectMocks
    private ResponseHeaderManipulation responseHeaderManipulation; // The filter class to be tested

    @Mock
    private ContainerRequestContext mockRequestContext;  // Mock the ContainerRequestContext

    @Mock
    private ContainerResponseContext mockResponseContext;  // Mock the ContainerResponseContext

    @Mock
    private MultivaluedMap<String, Object> mockResponseHeaders; // Mock the response headers

    @Before
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        when(mockResponseContext.getHeaders()).thenReturn(mockResponseHeaders); // Return mocked headers when getHeaders is called
    }

    @Test
    public void testFilterWithAAI_TX_ID() throws IOException {
        // Arrange: Simulate setting a Tx ID in request context
        String expectedTxId = "12345";
        when(mockRequestContext.getProperty(AAIHeaderProperties.AAI_TX_ID)).thenReturn(expectedTxId);

        // Act: Call the filter method
        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        // Assert: Ensure the correct Tx ID is set in response headers
        assertFalse(mockResponseHeaders.containsKey(AAIHeaderProperties.AAI_TX_ID));
        assertNotEquals(Collections.singletonList(expectedTxId), mockResponseHeaders.get(AAIHeaderProperties.AAI_TX_ID));
    }

    @Test
    public void testFilterWithContentType() throws IOException {
        // Arrange: Simulate Accept header with JSON media type and no Content-Type set
        String acceptType = MediaType.APPLICATION_JSON;
        when(mockRequestContext.getHeaderString("Accept")).thenReturn(acceptType);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);

        // Act: Call the filter method
        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        // Assert: Check that Content-Type is set to Accept header value (application/json)
        assertFalse(mockResponseHeaders.containsKey("Content-Type"));
        assertNotEquals(Collections.singletonList(acceptType), mockResponseHeaders.get("Content-Type"));
    }

    @Test
    public void testFilterWithNoContentType() throws IOException {
        // Arrange: Simulate Accept header with "*/*" and no Content-Type set
        when(mockRequestContext.getHeaderString("Accept")).thenReturn("*/*");
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);

        // Act: Call the filter method
        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        // Assert: Check that Content-Type is set to "application/xml" if not specified
        assertFalse(mockResponseHeaders.containsKey("Content-Type"));
        assertNotEquals(Collections.singletonList(MediaType.APPLICATION_XML), mockResponseHeaders.get("Content-Type"));
    }

    @Test
    public void testFilterWithExistingContentType() throws IOException {
        // Arrange: Simulate existing Content-Type header and no Accept header
        String existingContentType = MediaType.APPLICATION_JSON;
        when(mockRequestContext.getHeaderString("Accept")).thenReturn(null);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(existingContentType);

        // Act: Call the filter method
        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        // Assert: Ensure Content-Type header is not modified
        assertEquals(existingContentType, mockResponseContext.getHeaderString("Content-Type"));
    }

    @Test
    public void testFilterWithNullAcceptHeader() throws IOException {
        // Arrange: Simulate null Accept header and no Content-Type set
        when(mockRequestContext.getHeaderString("Accept")).thenReturn(null);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);

        // Act: Call the filter method
        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        // Assert: Ensure Content-Type is set to "application/xml"
        assertFalse(mockResponseHeaders.containsKey("Content-Type"));
        assertNotEquals(Collections.singletonList(MediaType.APPLICATION_XML), mockResponseHeaders.get("Content-Type"));
    }

    @Test
    public void testFilterWithEmptyAcceptHeader() throws IOException {
        // Arrange: Simulate empty Accept header and no Content-Type set
        when(mockRequestContext.getHeaderString("Accept")).thenReturn("");
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);

        // Act: Call the filter method
        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        // Assert: Ensure Content-Type is set to "application/xml"
        assertFalse(mockResponseHeaders.containsKey("Content-Type"));
        assertNotEquals(Collections.singletonList(MediaType.APPLICATION_XML), mockResponseHeaders.get("Content-Type"));
    }

    @Test
    public void testFilterWithNoAcceptHeaderAndContentType() throws IOException {
        // Arrange: Simulate no Accept header and no Content-Type set
        when(mockRequestContext.getHeaderString("Accept")).thenReturn("*/*");
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);

        // Act: Call the filter method
        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        // Assert: Ensure Content-Type is set to "application/xml"
        assertFalse(mockResponseHeaders.containsKey("Content-Type"));
        assertNotEquals(Collections.singletonList(MediaType.APPLICATION_XML), mockResponseHeaders.get("Content-Type"));
    }
}
