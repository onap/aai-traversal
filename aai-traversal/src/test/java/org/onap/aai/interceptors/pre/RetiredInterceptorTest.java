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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.service.RetiredService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RetiredInterceptorTest {

    @Mock
    private RetiredService retiredService;

    @Mock
    private ContainerRequestContext containerRequestContext;

    @Mock
    private UriInfo uriInfo;

    private RetiredInterceptor retiredInterceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        String basePath = "/aai/v15";
        retiredInterceptor = new RetiredInterceptor(retiredService, basePath);
    }

    @Test
    void testFilter_withRetiredUri_allVersions() throws Exception {
        String requestURI = "/aai/v15/service/retired";
        String version = "v15";

        List<Pattern> retiredAllVersionPatterns = new ArrayList<>();
        retiredAllVersionPatterns.add(Pattern.compile("/aai/v.*service/retired"));

        List<Pattern> retiredVersionPatterns = new ArrayList<>();
        retiredVersionPatterns.add(Pattern.compile("/aai/v15/service/.*"));

        when(retiredService.getRetiredAllVersionList()).thenReturn(retiredAllVersionPatterns);
        when(retiredService.getRetiredPatterns()).thenReturn(retiredVersionPatterns);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getAbsolutePath()).thenReturn(java.net.URI.create(requestURI));

        retiredInterceptor.filter(containerRequestContext);

        verify(containerRequestContext).abortWith(any(Response.class));
    }

    @Test
    void testFilter_withActiveUri_doesNotAbort() throws Exception {
        String requestURI = "/aai/v15/service/active";
        String version = "v15";

        List<Pattern> retiredAllVersionPatterns = new ArrayList<>();
        retiredAllVersionPatterns.add(Pattern.compile("/aai/v.*service/retired"));

        List<Pattern> retiredVersionPatterns = new ArrayList<>();
        retiredVersionPatterns.add(Pattern.compile("/aai/v15/service/retired"));

        when(retiredService.getRetiredAllVersionList()).thenReturn(retiredAllVersionPatterns);
        when(retiredService.getRetiredPatterns()).thenReturn(retiredVersionPatterns);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getAbsolutePath()).thenReturn(java.net.URI.create(requestURI));

        retiredInterceptor.filter(containerRequestContext);

        verify(containerRequestContext, never()).abortWith(any(Response.class));
    }

    @Test
    void testExtractVersionFromPath_withValidVersion() {
        String requestURI = "/aai/v15/service/active";
        String extractedVersion = retiredInterceptor.extractVersionFromPath(requestURI);
        assertEquals("v15", extractedVersion);
    }

    @Test
    void testExtractVersionFromPath_withNoVersion() {
        String requestURI = "/aai/service/active";
        String extractedVersion = retiredInterceptor.extractVersionFromPath(requestURI);
        assertEquals(null, extractedVersion);
    }
}
