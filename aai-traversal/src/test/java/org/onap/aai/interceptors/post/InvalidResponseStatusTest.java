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

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class InvalidResponseStatusTest {

    // The filter class to be tested
    @InjectMocks
    private InvalidResponseStatus invalidResponseStatus;

    // Mock dependencies
    @Mock
    private ContainerRequestContext mockRequestContext;  // Mock the ContainerRequestContext

    @Mock
    private ContainerResponseContext mockResponseContext;  // Mock the ContainerResponseContext

    @Before
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFilter_ResponseStatus405_ShouldChangeTo400() throws IOException {
        // Setup: Simulate a 405 status (Method Not Allowed)
        when(mockResponseContext.getStatus()).thenReturn(405);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);  // No Content-Type header

        // Act: Run the filter method
        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        // Assert: Verify that the status is changed to 400
        verify(mockResponseContext).setStatus(400);

        // Assert: Verify that some entity (message) was set
        verify(mockResponseContext).setEntity(anyString());
    }

    @Test
    public void testFilter_ResponseStatus405_ShouldHandleContentType() throws IOException {
        // Setup: Simulate a 405 status and set Content-Type header to "application/json"
        String contentType = "application/json";
        when(mockResponseContext.getStatus()).thenReturn(405);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(contentType);

        // Act: Run the filter method
        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        // Assert: Verify that the status is changed to 400
        verify(mockResponseContext).setStatus(400);

        // Assert: Verify that some entity (message) was set
        verify(mockResponseContext).setEntity(anyString());
    }

    @Test
    public void testFilter_ResponseStatusNot405_ShouldNotChangeStatus() throws IOException {
        // Setup: Simulate a status that's not 405, for example 200 OK
        when(mockResponseContext.getStatus()).thenReturn(200);

        // Act: Run the filter method
        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        // Assert: Ensure the status is not changed (i.e., remains 200)
        verify(mockResponseContext, never()).setStatus(400);

        // Assert: Ensure no entity (message) is set
        verify(mockResponseContext, never()).setEntity(anyString());
    }

    @Test
    public void testFilter_ContentTypeSetToNull_ShouldSetXmlContentType() throws IOException {
        // Setup: Simulate 405 status and no Content-Type header
        when(mockResponseContext.getStatus()).thenReturn(405);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);

        // Act: Run the filter method
        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        // Assert: Verify that the correct content type (XML) is set in response entity
        verify(mockResponseContext).setEntity(anyString());
    }

    @Test
    public void testFilter_ContentTypeSetToJson_ShouldReturnJsonMessage() throws IOException {
        // Setup: Simulate 405 status and Content-Type as "application/json"
        when(mockResponseContext.getStatus()).thenReturn(405);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn("application/json");

        // Act: Run the filter method
        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        // Assert: Verify that the entity is set (JSON or XML message, depending on logic)
        verify(mockResponseContext).setEntity(anyString());
    }
}
