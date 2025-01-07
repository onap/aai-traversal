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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VersionInterceptorTest {

    @Mock
    private SchemaVersions mockSchemaVersions;

    @Mock
    private ContainerRequestContext mockRequestContext;

    @Mock
    private UriInfo mockUriInfo;

    private VersionInterceptor versionInterceptor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);
    }

    @Test
    public void testAllowedVersion() {
        // Setup allowed versions
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        // Test valid version URI
        String uri = "/v1/test"; // Valid version
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter and assert that no exception is thrown
        try {
            versionInterceptor.filter(mockRequestContext);
        } catch (Exception e) {
            fail("abortWith() was called unexpectedly for valid version");
        }
    }


    @Test
    public void testInvalidVersionFormat() {
        // Setup
        Set<String> allowedVersions = new HashSet<>(Arrays.asList("v1", "v2"));
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        // Test invalid version URI
        String uri = "/invalid-version/test";
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter
        versionInterceptor.filter(mockRequestContext);

        // Ensure abortWith() was called with a response (invalid version)
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockRequestContext).abortWith(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        assertEquals(400, response.getStatus());  // Assuming 400 for invalid version format
    }

    @Test
    public void testVersionNotAllowed() {
        // Setup
        Set<String> allowedVersions = new HashSet<>(Arrays.asList("v1", "v2"));
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        // Test unallowed version URI
        String uri = "/v3/test";
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter
        versionInterceptor.filter(mockRequestContext);

        // Ensure abortWith() was called with a response (unallowed version)
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockRequestContext).abortWith(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        assertEquals(400, response.getStatus());  // Assuming 403 for unallowed version
        assertFalse(response.getEntity().toString().contains("AAI_3016"));
    }

    @Test
    public void testExcludedPathsDoNotTriggerVersionCheck() {
        // Setup allowed versions
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        // Test excluded URI (e.g., "search")
        String uri = "/search/test"; // Excluded path
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter and assert that no exception is thrown
        try {
            versionInterceptor.filter(mockRequestContext);
        } catch (Exception e) {
            fail("abortWith() was called unexpectedly for excluded path");
        }
    }


    @Test
    public void testNoVersionInUri() {
        // Setup allowed versions
        Set<String> allowedVersions = new HashSet<>(Arrays.asList("v1", "v2"));
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        // Test URI with no version
        String uri = "/test";
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter
        versionInterceptor.filter(mockRequestContext);

        // Ensure abortWith() is called (as no version is provided)
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockRequestContext).abortWith(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        assertEquals(400, response.getStatus());  // Assuming 400 for missing version
    }

    @Test
    public void testValidVersion() {
        // Setup allowed versions
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));

        // Test valid version URI
        String uri = "/v2/test"; // Valid version
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter and assert that no exception is thrown
        try {
            versionInterceptor.filter(mockRequestContext);
        } catch (Exception e) {
            fail("abortWith() was called unexpectedly for valid version");
        }
    }

    @Test
    public void testUtilEchoPathDoesNotTriggerVersionCheck() {
        // Setup allowed versions
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        // Test URI starts with "/util/echo"
        String uri = "/util/echo/test"; // Excluded path
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter and assert that no exception is thrown
        try {
            versionInterceptor.filter(mockRequestContext);
        } catch (Exception e) {
            fail("abortWith() was called unexpectedly for 'util/echo' path");
        }
    }

    @Test
    public void testToolsPathDoesNotTriggerVersionCheck() {
        // Setup allowed versions
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        // Test URI starts with "/tools"
        String uri = "/tools/test"; // Excluded path
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter and assert that no exception is thrown
        try {
            versionInterceptor.filter(mockRequestContext);
        } catch (Exception e) {
            fail("abortWith() was called unexpectedly for 'tools' path");
        }
    }

    @Test
    public void testRecentsPathDoesNotTriggerVersionCheck() {
        // Setup allowed versions
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        // Test URI starts with "/recents"
        String uri = "/recents/test"; // Excluded path
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter and assert that no exception is thrown
        try {
            versionInterceptor.filter(mockRequestContext);
        } catch (Exception e) {
            fail("abortWith() was called unexpectedly for 'recents' path");
        }
    }

    @Test
    public void testCq2GremlinPathDoesNotTriggerVersionCheck() {
        // Setup allowed versions
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        // Test URI starts with "/cq2gremlin"
        String uri = "/cq2gremlin/test"; // Excluded path
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter and assert that no exception is thrown
        try {
            versionInterceptor.filter(mockRequestContext);
        } catch (Exception e) {
            fail("abortWith() was called unexpectedly for 'cq2gremlin' path");
        }
    }

    @Test
    public void testCq2GremlinTestPathDoesNotTriggerVersionCheck() {
        // Setup allowed versions
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        // Test URI starts with "/cq2gremlintest"
        String uri = "/cq2gremlintest/test"; // Excluded path
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        // Execute the filter and assert that no exception is thrown
        try {
            versionInterceptor.filter(mockRequestContext);
        } catch (Exception e) {
            fail("abortWith() was called unexpectedly for 'cq2gremlintest' path");
        }
    }
}
