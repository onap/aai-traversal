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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.search;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.AAISetup;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.setup.SchemaVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ModelAndNamedQueryRestProviderTest extends AAISetup{

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    private static final Set<Integer> VALID_HTTP_STATUS_CODES = new HashSet<>();

    private SchemaVersion version;
    private static final ModelType introspectorFactoryType = ModelType.MOXY;

    static {
        VALID_HTTP_STATUS_CODES.add(200);
        VALID_HTTP_STATUS_CODES.add(201);
        VALID_HTTP_STATUS_CODES.add(204);
    }

    private ModelAndNamedQueryRestProvider modelAndNamedQueryRestProvider;

    private HttpHeaders httpHeaders;

    private UriInfo uriInfo;

    private MultivaluedMap<String, String> headersMultiMap;
    private MultivaluedMap<String, String> queryParameters;

    private List<String> aaiRequestContextList;

    private List<MediaType> outputMediaTypes;

    private static final Logger logger = LoggerFactory.getLogger(ModelAndNamedQueryRestProviderTest.class.getName());

    @Before
    public void setup(){
        version = schemaVersions.getDefaultVersion();
        logger.info("Starting the setup for the integration tests of Rest Endpoints");
     
        modelAndNamedQueryRestProvider = new ModelAndNamedQueryRestProvider(searchGraph, schemaVersions);
        httpHeaders         = mock(HttpHeaders.class);
        uriInfo             = mock(UriInfo.class);

        when(uriInfo.getPath()).thenReturn("JUNITURI");

        headersMultiMap     = new MultivaluedHashMap<>();
        queryParameters     = Mockito.spy(new MultivaluedHashMap<>());

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
        when(httpHeaders.getRequestHeader("X-FromAppId")).thenReturn(Collections.singletonList("JUNIT"));
        when(httpHeaders.getRequestHeader("X-TransactionId")).thenReturn(Collections.singletonList("JUNIT"));

        when(httpHeaders.getRequestHeader("aai-request-context")).thenReturn(aaiRequestContextList);


        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(uriInfo.getQueryParameters(false)).thenReturn(queryParameters);

        // TODO - Check if this is valid since RemoveDME2QueryParameters seems to be very unreasonable
        Mockito.doReturn(null).when(queryParameters).remove(anyObject());

        when(httpHeaders.getMediaType()).thenReturn(APPLICATION_JSON);
    }

    @Test
    public void testNamedQueryWhenNoDataToBeFoundReturnHttpNotFound() throws Exception {

        String queryParameters = getPayload("payloads/named-queries/named-query.json");
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getContentType()).thenReturn("application/json");

        Response response = modelAndNamedQueryRestProvider.getNamedQueryResponse(
                httpHeaders,
                request,
                queryParameters,
                uriInfo
        );

        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testNamedQueryInvalidHeaders() throws Exception {

        httpHeaders = mock(HttpHeaders.class);

        when(httpHeaders.getRequestHeader("X-FromAppId")).thenThrow(IllegalArgumentException.class);
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);

        Response response = modelAndNamedQueryRestProvider.getNamedQueryResponse(
                httpHeaders,
                null,
                "cloud-region",
                uriInfo
        );

        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Ignore("This test is too dependent on the cpu time to timeout and will fail randomly")
    @Test
    public void testNamedQueryCallTimeoutThrown() throws Exception {

        String queryParameters = getPayload("payloads/named-queries/named-query.json");
        HttpServletRequest request = mock(HttpServletRequest.class);

        headersMultiMap.putSingle("X-FromAppId", "JUNITTESTAPP1");
        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);

        when(request.getContentType()).thenReturn("application/json");

        Response response = modelAndNamedQueryRestProvider.getNamedQueryResponse(
                httpHeaders,
                request,
                queryParameters,
                uriInfo
        );

        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testNamedQueryCallTimeoutBypassed() throws Exception {

        String queryParameters = getPayload("payloads/named-queries/named-query.json");
        HttpServletRequest request = mock(HttpServletRequest.class);

        headersMultiMap.putSingle("X-FromAppId", "JUNITTESTAPP2");
        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);

        when(request.getContentType()).thenReturn("application/json");

        Response response = modelAndNamedQueryRestProvider.getNamedQueryResponse(
                httpHeaders,
                request,
                queryParameters,
                uriInfo
        );

        assertNotNull(response);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    public String getPayload(String filename) throws IOException {

        InputStream inputStream = getClass()
                .getClassLoader()
                .getResourceAsStream(filename);

        String message = String.format("Unable to find the %s in src/test/resources", filename);
        assertNotNull(message, inputStream);

        return IOUtils.toString(inputStream);
    }
}
