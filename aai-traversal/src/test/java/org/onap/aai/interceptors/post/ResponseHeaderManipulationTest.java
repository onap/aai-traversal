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
package org.onap.aai.interceptors.post;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Collections;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class ResponseHeaderManipulationTest {

    @InjectMocks
    private ResponseHeaderManipulation responseHeaderManipulation;

    @Mock
    private ContainerRequestContext mockRequestContext;

    @Mock
    private ContainerResponseContext mockResponseContext;

    @Mock
    private MultivaluedMap<String, Object> mockResponseHeaders;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockResponseContext.getHeaders()).thenReturn(mockResponseHeaders);
    }

    @Test
    public void testFilterWithNoContentType() throws IOException {
        // Arrange: Simulate Accept header with "*/*" and no Content-Type set
        when(mockRequestContext.getHeaderString("Accept")).thenReturn("*/*");

        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        assertFalse(mockResponseHeaders.containsKey("Content-Type"));
    }

    @Test
    public void testFilterWithExistingContentType() throws IOException {
        // Arrange: Simulate existing Content-Type header and no Accept header
        String existingContentType = MediaType.APPLICATION_JSON;
        when(mockRequestContext.getHeaderString("Accept")).thenReturn(null);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(existingContentType);

        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        assertEquals(existingContentType, mockResponseContext.getHeaderString("Content-Type"));
    }

    @Test
    public void testFilterWithNullAcceptHeader() throws IOException {
        // Arrange: Simulate null Accept header and no Content-Type set
        when(mockRequestContext.getHeaderString("Accept")).thenReturn(null);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);

        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        assertFalse(mockResponseHeaders.containsKey("Content-Type"));
        assertNotEquals(Collections.singletonList(MediaType.APPLICATION_XML), mockResponseHeaders.get("Content-Type"));
    }

    @Test
    public void testFilterWithEmptyAcceptHeader() throws IOException {
        // Arrange: Simulate empty Accept header and no Content-Type set
        when(mockRequestContext.getHeaderString("Accept")).thenReturn("");
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(null);

        responseHeaderManipulation.filter(mockRequestContext, mockResponseContext);

        assertFalse(mockResponseHeaders.containsKey("Content-Type"));
        assertNotEquals(Collections.singletonList(MediaType.APPLICATION_XML), mockResponseHeaders.get("Content-Type"));
    }
}
