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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
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

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);
    }

    @Test
    public void testAllowedVersion() {
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        String uri = "/v1/test";
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        try {
            versionInterceptor.filter(mockRequestContext);
        } catch (Exception e) {
            fail("abortWith() was called unexpectedly for valid version");
        }
    }

    @Test
    public void testInvalidVersionFormat() {
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        String uri = "/invalid-version/test";
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        versionInterceptor.filter(mockRequestContext);

        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockRequestContext).abortWith(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testNoVersionInUri() {
        when(mockSchemaVersions.getVersions()).thenReturn(Arrays.asList(new SchemaVersion("v1"), new SchemaVersion("v2")));
        versionInterceptor = new VersionInterceptor(mockSchemaVersions);

        String uri = "/test";
        when(mockRequestContext.getUriInfo()).thenReturn(mockUriInfo);
        when(mockUriInfo.getPath()).thenReturn(uri);

        versionInterceptor.filter(mockRequestContext);
        ArgumentCaptor<Response> responseCaptor = ArgumentCaptor.forClass(Response.class);
        verify(mockRequestContext).abortWith(responseCaptor.capture());
        Response response = responseCaptor.getValue();
        assertEquals(400, response.getStatus());
    }
}
