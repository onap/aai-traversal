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

import org.junit.jupiter.api.Test;
import org.onap.aai.interceptors.AAIHeaderProperties;

import javax.ws.rs.container.ContainerRequestContext;

import static org.mockito.Mockito.*;

class HttpHeaderInterceptorTest {

    @Test
    public void testOverridePostToPatch() throws Exception {
        // Arrange
        ContainerRequestContext mockRequestContext = mock(ContainerRequestContext.class);
        when(mockRequestContext.getMethod()).thenReturn("POST");
        when(mockRequestContext.getHeaderString(AAIHeaderProperties.HTTP_METHOD_OVERRIDE)).thenReturn("PATCH");

        HttpHeaderInterceptor interceptor = new HttpHeaderInterceptor();

        // Act
        interceptor.filter(mockRequestContext);

        // Assert
        verify(mockRequestContext).setMethod(HttpHeaderInterceptor.patchMethod);
    }

    @Test
    public void testNoOverrideHeader() throws Exception {
        // Arrange
        ContainerRequestContext mockRequestContext = mock(ContainerRequestContext.class);
        when(mockRequestContext.getMethod()).thenReturn("POST");
        when(mockRequestContext.getHeaderString(AAIHeaderProperties.HTTP_METHOD_OVERRIDE)).thenReturn(null);

        HttpHeaderInterceptor interceptor = new HttpHeaderInterceptor();

        // Act
        interceptor.filter(mockRequestContext);

        // Assert
        verify(mockRequestContext, never()).setMethod(anyString());
    }

    @Test
    public void testNonPostMethodNoChange() throws Exception {
        // Arrange
        ContainerRequestContext mockRequestContext = mock(ContainerRequestContext.class);
        when(mockRequestContext.getMethod()).thenReturn("GET");
        when(mockRequestContext.getHeaderString(AAIHeaderProperties.HTTP_METHOD_OVERRIDE)).thenReturn("PATCH");

        HttpHeaderInterceptor interceptor = new HttpHeaderInterceptor();

        // Act
        interceptor.filter(mockRequestContext);

        // Assert
        verify(mockRequestContext, never()).setMethod(anyString());
    }

    @Test
    public void testOverrideWithNonPatchValue() throws Exception {
        // Arrange
        ContainerRequestContext mockRequestContext = mock(ContainerRequestContext.class);
        when(mockRequestContext.getMethod()).thenReturn("POST");
        when(mockRequestContext.getHeaderString(AAIHeaderProperties.HTTP_METHOD_OVERRIDE)).thenReturn("PUT");

        HttpHeaderInterceptor interceptor = new HttpHeaderInterceptor();

        // Act
        interceptor.filter(mockRequestContext);

        // Assert
        verify(mockRequestContext, never()).setMethod(anyString());
    }

}
