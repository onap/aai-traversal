/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.search;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.AAISetup;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.setup.SchemaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchProviderTest extends AAISetup {

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    private static final Set<Integer> VALID_HTTP_STATUS_CODES = new HashSet<>();

    private SchemaVersion version;
    private static final ModelType introspectorFactoryType = ModelType.MOXY;

    private Loader loader;

    static {
        VALID_HTTP_STATUS_CODES.add(200);
        VALID_HTTP_STATUS_CODES.add(201);
        VALID_HTTP_STATUS_CODES.add(204);
    }

    private SearchProvider searchProvider;

    private HttpHeaders httpHeaders;

    private UriInfo uriInfo;

    private MultivaluedMap<String, String> headersMultiMap;
    private MultivaluedMap<String, String> queryParameters;

    private List<String> aaiRequestContextList;

    private List<MediaType> outputMediaTypes;

    private static final Logger logger =
        LoggerFactory.getLogger(SearchProviderTest.class.getName());

    @Before
    public void setup() {
        logger.info("Starting the setup for the integration tests of Rest Endpoints");
        version = schemaVersions.getDefaultVersion();

        searchProvider = new SearchProvider(loaderFactory, searchGraph, schemaVersions, basePath);
        httpHeaders = mock(HttpHeaders.class);
        uriInfo = mock(UriInfo.class);

        when(uriInfo.getPath()).thenReturn("JUNITURI");

        headersMultiMap = new MultivaluedHashMap<>();
        queryParameters = Mockito.spy(new MultivaluedHashMap<>());

        headersMultiMap.add("X-FromAppId", "JUNIT");
        headersMultiMap.add("X-TransactionId", UUID.randomUUID().toString());
        headersMultiMap.add("Real-Time", "true");
        headersMultiMap.add("Accept", "application/json");
        headersMultiMap.add("aai-request-context", "");

        outputMediaTypes = new ArrayList<>();
        outputMediaTypes.add(APPLICATION_JSON);

        aaiRequestContextList = new ArrayList<>();
        aaiRequestContextList.add("");

        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);
        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);
        when(httpHeaders.getRequestHeader("X-FromAppId"))
            .thenReturn(Collections.singletonList("JUNIT"));
        when(httpHeaders.getRequestHeader("X-TransactionId"))
            .thenReturn(Collections.singletonList("JUNIT"));

        when(httpHeaders.getRequestHeader("aai-request-context")).thenReturn(aaiRequestContextList);

        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(uriInfo.getQueryParameters(false)).thenReturn(queryParameters);

        // TODO - Check if this is valid since RemoveDME2QueryParameters seems to be very
        // unreasonable
        Mockito.doReturn(null).when(queryParameters).remove(anyObject());

        when(httpHeaders.getMediaType()).thenReturn(APPLICATION_JSON);
        loader = loaderFactory.createLoaderForVersion(introspectorFactoryType, version);
    }

    @Test
    public void testNodesQueryInvalidHeaders() throws Exception {

        List<String> keys = new ArrayList<>();
        keys.add("cloud-region.cloud-owner:test-aic");

        List<String> includeStrings = new ArrayList<>();
        includeStrings.add("cloud-region");

        httpHeaders = mock(HttpHeaders.class);

        when(httpHeaders.getRequestHeader("X-FromAppId")).thenThrow(IllegalArgumentException.class);
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);

        Response response = searchProvider.getNodesQueryResponse(httpHeaders, null, "cloud-region",
            keys, includeStrings, version.toString(), uriInfo);

        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    // TODO fix test
    @Ignore("Test has a time dependency and fails based on system perf")
    @Test
    public void testNodesQueryTimeoutThrown() throws Exception {

        List<String> keys = new ArrayList<>();
        keys.add("cloud-region.cloud-owner:test-aic");

        List<String> includeStrings = new ArrayList<>();
        includeStrings.add("cloud-region");

        httpHeaders = mock(HttpHeaders.class);

        headersMultiMap.putSingle("X-FromAppId", "JUNITTESTAPP1");
        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);

        Response response = searchProvider.getNodesQueryResponse(httpHeaders, null, "cloud-region",
            keys, includeStrings, version.toString(), uriInfo);

        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertThat(response.getEntity().toString(), containsString("7406"));
    }

    @Test
    public void testNodesQueryTimeoutBypassed() throws Exception {

        List<String> keys = new ArrayList<>();
        keys.add("cloud-region.cloud-owner:test-aic");

        List<String> includeStrings = new ArrayList<>();
        includeStrings.add("cloud-region");

        httpHeaders = mock(HttpHeaders.class);

        headersMultiMap.putSingle("X-FromAppId", "JUNITTESTAPP2");
        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);

        Response response = searchProvider.getNodesQueryResponse(httpHeaders, null, "cloud-region",
            keys, includeStrings, version.toString(), uriInfo);

        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertThat(response.getEntity().toString(), containsString("4009"));
    }

    // TODO fix test
    @Ignore("Test has a time dependency and fails based on system perf")
    @Test
    public void testGenericQueryTimeoutThrown() throws Exception {

        List<String> keys = new ArrayList<>();
        keys.add("cloud-region.cloud-owner:test-aic");

        List<String> includeStrings = new ArrayList<>();
        includeStrings.add("cloud-region");

        httpHeaders = mock(HttpHeaders.class);

        headersMultiMap.putSingle("X-FromAppId", "JUNITTESTAPP1");
        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);

        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);

        Response response = searchProvider.getGenericQueryResponse(httpHeaders, null,
            "cloud-region", keys, includeStrings, 0, version.toString(), uriInfo);

        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertThat(response.getEntity().toString(), containsString("7406"));
    }

}
