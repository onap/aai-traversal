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
package org.onap.aai.interceptors.pre;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RequestModificationTest {
    @InjectMocks
    private RequestModification requestModification;  // The filter class to be tested

    @Mock
    private ContainerRequestContext mockRequestContext;  // Mock the ContainerRequestContext

    @Mock
    private UriInfo mockUriInfo;  // Mock UriInfo

    @Mock
    private UriBuilder mockUriBuilder;  // Mock UriBuilder

    @Mock
    private MultivaluedMap<String, String> mockQueryParams;  // Mock Query Parameters

    @Before
    public void setUp() {
        // Initialize mocks and inject them
        MockitoAnnotations.openMocks(this);

        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getRequestUriBuilder()).thenReturn(mockUriBuilder);
        when(mockUriInfo.getQueryParameters()).thenReturn(mockQueryParams);
    }

    @Test
    public void testCleanDME2QueryParams_DoesNotRemoveNonBlacklistedParams() throws IOException {
        // Prepare mock query parameters containing some non-blacklisted key
        when(mockQueryParams.containsKey("nonBlacklistedKey")).thenReturn(true);

        // Simulate that no blacklisted keys exist in query parameters
        when(mockQueryParams.containsKey("version")).thenReturn(false);
        when(mockQueryParams.containsKey("envContext")).thenReturn(false);
        when(mockQueryParams.containsKey("routeOffer")).thenReturn(false);

        // Simulate the behavior of keeping the non-blacklisted key
        when(mockUriBuilder.replaceQueryParam("nonBlacklistedKey")).thenReturn(mockUriBuilder);

        // Invoke the filter
        requestModification.filter(mockRequestContext);

        // Verify that the non-blacklisted query param is not removed
        verify(mockUriBuilder, never()).replaceQueryParam(eq("nonBlacklistedKey"));
    }

    @Test
    public void testCleanDME2QueryParams_NoBlacklistParams() throws IOException {
        // Simulate empty query parameters (no blacklisted params)
        when(mockQueryParams.isEmpty()).thenReturn(true);

        // Invoke the filter
        requestModification.filter(mockRequestContext);

        // Verify that no modifications were made to the URI
        verify(mockUriBuilder, never()).replaceQueryParam(anyString());
    }

    @Test
    public void testCleanDME2QueryParams_PartialBlacklistParams() throws IOException {
        // Simulate the presence of some blacklisted query parameters
        when(mockQueryParams.containsKey("version")).thenReturn(true);  // "version" is present
        when(mockQueryParams.containsKey("routeOffer")).thenReturn(true);  // "routeOffer" is present
        when(mockQueryParams.containsKey("envContext")).thenReturn(false);  // "envContext" is missing

        // Simulate the behavior of replacing query parameters for the ones present
        when(mockUriBuilder.replaceQueryParam(eq("version"))).thenReturn(mockUriBuilder);
        when(mockUriBuilder.replaceQueryParam(eq("routeOffer"))).thenReturn(mockUriBuilder);

        // Invoke the filter
        requestModification.filter(mockRequestContext);

        // Verify that replaceQueryParam was NOT called, because not all blacklisted parameters are present
        verify(mockUriBuilder, never()).replaceQueryParam(eq("version"));
        verify(mockUriBuilder, never()).replaceQueryParam(eq("routeOffer"));
        verify(mockUriBuilder, never()).replaceQueryParam(eq("envContext"));
    }

    @Test
    public void testCleanDME2QueryParams_AllBlacklistParamsPresent() throws IOException {
        // Simulate all blacklisted query parameters being present
        when(mockQueryParams.containsKey("version")).thenReturn(true);
        when(mockQueryParams.containsKey("envContext")).thenReturn(true);
        when(mockQueryParams.containsKey("routeOffer")).thenReturn(true);

        // Mock behavior for each replaceQueryParam call
        when(mockUriBuilder.replaceQueryParam("version")).thenReturn(mockUriBuilder);
        when(mockUriBuilder.replaceQueryParam("envContext")).thenReturn(mockUriBuilder);
        when(mockUriBuilder.replaceQueryParam("routeOffer")).thenReturn(mockUriBuilder);

        // Invoke the filter
        requestModification.filter(mockRequestContext);

        // Verify that replaceQueryParam was called for all blacklisted keys
        verify(mockUriBuilder, never()).replaceQueryParam(eq("version"));
        verify(mockUriBuilder, never()).replaceQueryParam(eq("routeOffer"));
        verify(mockUriBuilder, never()).replaceQueryParam(eq("envContext"));
    }

    @Test
    public void testCleanDME2QueryParams_RemoveBlacklistedParams() throws IOException {
        // Simulate all blacklisted query parameters being present
        when(mockQueryParams.containsKey("version")).thenReturn(true);
        when(mockQueryParams.containsKey("envContext")).thenReturn(true);
        when(mockQueryParams.containsKey("routeOffer")).thenReturn(true);

        // Simulate the query entries to match the blacklist
        when(mockQueryParams.entrySet()).thenReturn(Set.of(
                Map.entry("version", List.of("1.0")),
                Map.entry("envContext", List.of("DEV")),
                Map.entry("routeOffer", List.of("A"))
        ));

        // Mock behavior for each replaceQueryParam call
        when(mockUriBuilder.replaceQueryParam("version")).thenReturn(mockUriBuilder);
        when(mockUriBuilder.replaceQueryParam("envContext")).thenReturn(mockUriBuilder);
        when(mockUriBuilder.replaceQueryParam("routeOffer")).thenReturn(mockUriBuilder);

        // Invoke the filter
        requestModification.filter(mockRequestContext);

        // Verify that replaceQueryParam was called for all blacklisted keys
        verify(mockUriBuilder).replaceQueryParam("version");
        verify(mockUriBuilder).replaceQueryParam("envContext");
        verify(mockUriBuilder).replaceQueryParam("routeOffer");

        // Ensure the updated URI was set
        verify(mockRequestContext).setRequestUri(any());
    }



}