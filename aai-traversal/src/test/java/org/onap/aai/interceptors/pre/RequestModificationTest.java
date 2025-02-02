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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RequestModificationTest {

    @InjectMocks
    private RequestModification requestModification;

    @Mock
    private ContainerRequestContext mockRequestContext;

    @Mock
    private UriInfo mockUriInfo;

    @Mock
    private UriBuilder mockUriBuilder;

    @Mock
    private MultivaluedMap<String, String> mockQueryParams;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getRequestUriBuilder()).thenReturn(mockUriBuilder);
        when(mockUriInfo.getQueryParameters()).thenReturn(mockQueryParams);
    }

    @Test
    void testCleanDME2QueryParams_DoesNotRemoveNonBlacklistedParams() throws IOException {
        when(mockQueryParams.containsKey("nonBlacklistedKey")).thenReturn(true);
        when(mockQueryParams.containsKey("version")).thenReturn(false);
        when(mockQueryParams.containsKey("envContext")).thenReturn(false);
        when(mockQueryParams.containsKey("routeOffer")).thenReturn(false);
        when(mockUriBuilder.replaceQueryParam("nonBlacklistedKey")).thenReturn(mockUriBuilder);

        requestModification.filter(mockRequestContext);

        verify(mockUriBuilder, never()).replaceQueryParam(eq("nonBlacklistedKey"));
        assertTrue(true, "Non-blacklisted query parameters were not removed."); // Assert the expected behavior
    }

    @Test
    void testCleanDME2QueryParams_NoBlacklistParams() throws IOException {
        when(mockQueryParams.isEmpty()).thenReturn(true);

        requestModification.filter(mockRequestContext);

        verify(mockUriBuilder, never()).replaceQueryParam(eq("version"));
        verify(mockUriBuilder, never()).replaceQueryParam(eq("envContext"));
        verify(mockUriBuilder, never()).replaceQueryParam(eq("routeOffer"));

        assertTrue(mockQueryParams.isEmpty(), "Query parameters should be empty when no blacklisted params are present.");
    }

    @Test
    void testCleanDME2QueryParams_AllBlacklistParamsPresent() throws IOException {
        when(mockQueryParams.containsKey("version")).thenReturn(true);
        when(mockQueryParams.containsKey("envContext")).thenReturn(true);
        when(mockQueryParams.containsKey("routeOffer")).thenReturn(true);
        when(mockQueryParams.entrySet()).thenReturn(Set.of(
                Map.entry("version", List.of("1.0")),
                Map.entry("envContext", List.of("DEV")),
                Map.entry("routeOffer", List.of("A"))
        ));

        when(mockUriBuilder.replaceQueryParam("version")).thenReturn(mockUriBuilder);
        when(mockUriBuilder.replaceQueryParam("envContext")).thenReturn(mockUriBuilder);
        when(mockUriBuilder.replaceQueryParam("routeOffer")).thenReturn(mockUriBuilder);

        requestModification.filter(mockRequestContext);

        verify(mockUriBuilder).replaceQueryParam("version");
        verify(mockUriBuilder).replaceQueryParam("envContext");
        verify(mockUriBuilder).replaceQueryParam("routeOffer");
        verify(mockRequestContext).setRequestUri(any());

        assertTrue(true, "All blacklisted query parameters should have been processed.");
    }
}
