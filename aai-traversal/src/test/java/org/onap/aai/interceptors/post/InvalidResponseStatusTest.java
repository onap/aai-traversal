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
import java.io.IOException;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InvalidResponseStatusTest {

    @InjectMocks
    private InvalidResponseStatus invalidResponseStatus;

    @Mock
    private ContainerRequestContext mockRequestContext;

    @Mock
    private ContainerResponseContext mockResponseContext;

    private final int[] status = {405};

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
            // Mock getStatus to return the current value in the status array
            when(mockResponseContext.getStatus()).thenAnswer(invocation -> status[0]);

            // Use doAnswer to update the status value when setStatus is called
            doAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                if (args.length > 0 && args[0] instanceof Integer) {
                    status[0] = (Integer) args[0];  // Update the mock's status
                }
                return null;
            }).when(mockResponseContext).setStatus(anyInt());
        }

    @Test
    public void testFilter_statusIs405_shouldChangeTo400() throws IOException {
            invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        verify(mockResponseContext).setStatus(400);

        // Check the final status of the response context
        assertEquals(400, mockResponseContext.getStatus()); // Ensure status is now 400
    }

    @Test
    public void testFilter_ResponseStatus405_ShouldHandleContentType() throws IOException {
        // Setup: Simulate a 405 status and set Content-Type header to "application/json"
        String contentType = "application/json";
        when(mockResponseContext.getStatus()).thenReturn(405);
        when(mockResponseContext.getHeaderString("Content-Type")).thenReturn(contentType);

        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        verify(mockResponseContext).setStatus(400);

        verify(mockResponseContext).setEntity(anyString());

        assertEquals("application/json",mockResponseContext.getHeaderString("Content-Type"));
    }

    @Test
    public void testFilter_ResponseStatusNot405_ShouldNotChangeStatus() throws IOException {
        when(mockResponseContext.getStatus()).thenReturn(200);

        invalidResponseStatus.filter(mockRequestContext, mockResponseContext);

        verify(mockResponseContext, never()).setStatus(400);

        verify(mockResponseContext, never()).setEntity(anyString());

        assertTrue("Response status should remain 200", mockResponseContext.getStatus() == 200);
        assertNull("Entity should not be set", mockResponseContext.getEntity());
    }
}
