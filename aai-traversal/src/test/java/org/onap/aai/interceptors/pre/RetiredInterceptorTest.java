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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.service.RetiredService;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class RetiredInterceptorTest {

    @Mock
    private RetiredService retiredService;

    @Mock
    private ContainerRequestContext containerRequestContext;

    @Mock
    private javax.ws.rs.core.UriInfo uriInfo;  // Mock UriInfo

    private RetiredInterceptor retiredInterceptor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        String basePath = "/aai/v15";
        retiredInterceptor = new RetiredInterceptor(retiredService, basePath);
    }

    @Test
    public void testFilter_withRetiredUri_allVersions() throws Exception {
        // Mocking the request URI and the version
        String requestURI = "/aai/v15/service/retired";
        String version = "v15";

        // Mock retired patterns for all versions
        List<Pattern> retiredAllVersionPatterns = new ArrayList<>();
        retiredAllVersionPatterns.add(Pattern.compile("/aai/v.*service/retired"));

        // Mock retired patterns for specific version
        List<Pattern> retiredVersionPatterns = new ArrayList<>();
        retiredVersionPatterns.add(Pattern.compile("/aai/v15/service/.*"));

        when(retiredService.getRetiredAllVersionList()).thenReturn(retiredAllVersionPatterns);
        when(retiredService.getRetiredPatterns()).thenReturn(retiredVersionPatterns);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getAbsolutePath()).thenReturn(java.net.URI.create(requestURI)); // Mock URI

        // Execute the filter method
        retiredInterceptor.filter(containerRequestContext);

        // Verify that the request was aborted due to the retired URI
        verify(containerRequestContext).abortWith(any(Response.class));
    }

    @Test
    public void testFilter_withRetiredUri_specificVersion() throws Exception {
        // Mocking the request URI and the version
        String requestURI = "/aai/v15/service/retired";
        String version = "v15";

        // Mock retired patterns for all versions
        List<Pattern> retiredAllVersionPatterns = new ArrayList<>();
        retiredAllVersionPatterns.add(Pattern.compile("/aai/v.*service/retired"));

        // Mock retired patterns for a specific version
        List<Pattern> retiredVersionPatterns = new ArrayList<>();
        retiredVersionPatterns.add(Pattern.compile("/aai/v15/service/.*"));

        when(retiredService.getRetiredAllVersionList()).thenReturn(retiredAllVersionPatterns);
        when(retiredService.getRetiredPatterns()).thenReturn(retiredVersionPatterns);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getAbsolutePath()).thenReturn(java.net.URI.create(requestURI)); // Mock URI

        // Execute the filter method
        retiredInterceptor.filter(containerRequestContext);

        // Verify that the request was aborted due to the retired URI for specific version
        verify(containerRequestContext).abortWith(any(Response.class));
    }

    @Test
    public void testFilter_withActiveUri_doesNotAbort() throws Exception {
        // Mocking the request URI and the version
        String requestURI = "/aai/v15/service/active";
        String version = "v15";

        // Mock retired patterns for all versions that should NOT match the active URI
        List<Pattern> retiredAllVersionPatterns = new ArrayList<>();
        retiredAllVersionPatterns.add(Pattern.compile("/aai/v.*service/retired")); // This pattern should not match the active URI

        // Mock retired patterns for a specific version that should NOT match the active URI
        List<Pattern> retiredVersionPatterns = new ArrayList<>();
        retiredVersionPatterns.add(Pattern.compile("/aai/v15/service/retired")); // This pattern should not match the active URI

        when(retiredService.getRetiredAllVersionList()).thenReturn(retiredAllVersionPatterns);
        when(retiredService.getRetiredPatterns()).thenReturn(retiredVersionPatterns);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getAbsolutePath()).thenReturn(java.net.URI.create(requestURI)); // Mock URI

        // Execute the filter method
        retiredInterceptor.filter(containerRequestContext);

        // Verify that abortWith() was **not** called because the URI is active and no patterns match
        verify(containerRequestContext, never()).abortWith(any(Response.class));
    }

    @Test
    public void testFilter_withNoRetiredPatterns_doesNotAbort() throws Exception {
        // Mocking the request URI and the version
        String requestURI = "/aai/v15/service/active";
        String version = "v15";

        // No retired patterns
        List<Pattern> retiredAllVersionPatterns = new ArrayList<>();
        List<Pattern> retiredVersionPatterns = new ArrayList<>();

        when(retiredService.getRetiredAllVersionList()).thenReturn(retiredAllVersionPatterns);
        when(retiredService.getRetiredPatterns()).thenReturn(retiredVersionPatterns);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getAbsolutePath()).thenReturn(java.net.URI.create(requestURI)); // Mock URI

        // Execute the filter method
        retiredInterceptor.filter(containerRequestContext);

        // Verify that abortWith() was not called because there are no retired patterns
        verify(containerRequestContext, never()).abortWith(any(Response.class));
    }

    @Test
    public void testExtractVersionFromPath_withValidVersion() {
        // Test for extracting version from a URI
        String requestURI = "/aai/v15/service/active";
        String extractedVersion = retiredInterceptor.extractVersionFromPath(requestURI);

        // Assert that the correct version is extracted
        assertEquals("v15", extractedVersion);
    }

    @Test
    public void testExtractVersionFromPath_withNoVersion() {
        // Test for URI without a version
        String requestURI = "/aai/service/active";
        String extractedVersion = retiredInterceptor.extractVersionFromPath(requestURI);

        // Assert that the extracted version is null
        assertNull(extractedVersion);
    }
}
