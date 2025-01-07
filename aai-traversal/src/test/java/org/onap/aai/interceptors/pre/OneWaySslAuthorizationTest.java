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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.service.AuthorizationService;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class OneWaySslAuthorizationTest {

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private ContainerRequestContext containerRequestContext;

    @InjectMocks
    private OneWaySslAuthorization oneWaySslAuthorization;

    @Before
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Mock the authorization service's behavior
        when(authorizationService.checkIfUserAuthorized(anyString())).thenReturn(true);
    }

    @Test
    public void testFilterWithValidBasicAuth() throws Exception {
        // Prepare test data
        String basicAuth = "Basic validToken";
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        // Mock the request context behavior
        when(containerRequestContext.getHeaderString("Authorization")).thenReturn(basicAuth);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);

        // Mock the UriInfo and RequestUri
        UriInfo uriInfoMock = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/some/other/path"));
        when(uriInfoMock.getPath()).thenReturn("/some/other/path");

        // Mock Authorization Service to return true for valid token
        when(authorizationService.checkIfUserAuthorized("validToken")).thenReturn(true);

        // Call the filter method
        oneWaySslAuthorization.filter(containerRequestContext);

        // Verify that no abort method is called when authorization is successful
        verify(containerRequestContext, times(0)).abortWith(any());
    }


    @Test
    public void testFilterWithInvalidBasicAuth() throws Exception {
        // Prepare test data
        String basicAuth = "Basic invalidToken";
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        // Mock UriInfo and RequestUri behavior
        UriInfo uriInfoMock = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/some/other/path"));
        when(uriInfoMock.getPath()).thenReturn("/some/other/path");

        // Mock the request context behavior
        when(containerRequestContext.getHeaderString("Authorization")).thenReturn(basicAuth);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);

        // Mock Authorization Service to return false for invalid token
        when(authorizationService.checkIfUserAuthorized("invalidToken")).thenReturn(false);

        // Call the filter method
        oneWaySslAuthorization.filter(containerRequestContext);

        // Verify that abortWith is called to respond with an error
        verify(containerRequestContext, times(1)).abortWith(any(Response.class));
    }


    @Test
    public void testFilterWithoutBasicAuthHeader() throws Exception {
        // Prepare test data
        String basicAuth = null;
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        // Mock the request context behavior
        when(containerRequestContext.getHeaderString("Authorization")).thenReturn(basicAuth);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);

        // Mock the UriInfo and RequestUri
        UriInfo uriInfoMock = mock(UriInfo.class);
        UriBuilder uriBuilderMock = mock(UriBuilder.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/util/echo"));
        when(uriInfoMock.getPath()).thenReturn("/util/echo");

        // Call the filter method
        oneWaySslAuthorization.filter(containerRequestContext);

        // Verify that the method should return early and not abort for the /util/echo path
        verify(containerRequestContext, times(0)).abortWith(any(Response.class)); // No abort should happen
    }

    @Test
    public void testFilterForEchoPath() throws Exception {
        // Test the special case for /util/echo path, where the filter should not block
        String path = "/util/echo";

        // Mock UriInfo and RequestUri behavior
        UriInfo uriInfoMock = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost" + path));
        when(uriInfoMock.getPath()).thenReturn(path);

        // Call the filter method
        oneWaySslAuthorization.filter(containerRequestContext);

        // Verify that abortWith is not called as it's an allowed path
        verify(containerRequestContext, times(0)).abortWith(any());
    }

    @Test
    public void testErrorResponse() throws Exception {
        // Access the private method using reflection
        Method errorResponseMethod = OneWaySslAuthorization.class.getDeclaredMethod("errorResponse", String.class, List.class);
        errorResponseMethod.setAccessible(true);

        // Prepare test data
        String errorCode = "AAI_3300";
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        // Invoke the method via reflection
        Object result = errorResponseMethod.invoke(oneWaySslAuthorization, errorCode, acceptHeaderValues);

        // Verify the response is not empty and has the correct status
        assertTrue(result instanceof Optional);
        Optional<Response> responseOptional = (Optional<Response>) result;
        assertTrue(responseOptional.isPresent());
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), responseOptional.get().getStatus());
    }
    @Test
    public void testFilterWithNullBasicAuthHeader() throws Exception {
        // Prepare test data
        String basicAuth = null; // No authorization header
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        // Mock the request context behavior
        when(containerRequestContext.getHeaderString("Authorization")).thenReturn(basicAuth);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);

        // Mock the UriInfo and RequestUri
        UriInfo uriInfoMock = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/some/other/path"));
        when(uriInfoMock.getPath()).thenReturn("/some/other/path");

        // Call the filter method
        oneWaySslAuthorization.filter(containerRequestContext);

        // Verify that abortWith is called because the authorization header is null
        verify(containerRequestContext, times(1)).abortWith(any(Response.class));
    }
    @Test
    public void testFilterWithInvalidAuthorizationHeaderFormat() throws Exception {
        // Prepare test data
        String basicAuth = "Bearer invalidToken"; // Authorization header doesn't start with "Basic "
        List<MediaType> acceptHeaderValues = Arrays.asList(MediaType.APPLICATION_JSON_TYPE);

        // Mock the request context behavior
        when(containerRequestContext.getHeaderString("Authorization")).thenReturn(basicAuth);
        when(containerRequestContext.getAcceptableMediaTypes()).thenReturn(acceptHeaderValues);

        // Mock the UriInfo and RequestUri
        UriInfo uriInfoMock = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfoMock);
        when(uriInfoMock.getRequestUri()).thenReturn(URI.create("http://localhost/some/other/path"));
        when(uriInfoMock.getPath()).thenReturn("/some/other/path");

        // Call the filter method
        oneWaySslAuthorization.filter(containerRequestContext);

        // Verify that abortWith is called because the authorization header doesn't start with "Basic "
        verify(containerRequestContext, times(1)).abortWith(any(Response.class));
    }

}
