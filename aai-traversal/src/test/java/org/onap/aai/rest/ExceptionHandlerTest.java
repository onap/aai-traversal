/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.rest;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sun.istack.SAXParseException2;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExceptionHandlerTest {

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    @Mock
    private HttpHeaders httpHeaders;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ExceptionHandler handler = new ExceptionHandler();

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        MultivaluedHashMap headersMultiMap     = new MultivaluedHashMap<>();

        headersMultiMap.add("X-FromAppId", "JUNIT");
        headersMultiMap.add("X-TransactionId", UUID.randomUUID().toString());
        headersMultiMap.add("Real-Time", "true");
        headersMultiMap.add("Accept", "application/json");
        headersMultiMap.add("aai-request-context", "");

        List<MediaType> outputMediaTypes = new ArrayList<>();
        outputMediaTypes.add(APPLICATION_JSON);
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);
        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);
    }

    @Test
    public void testConversionOfWebApplicationResponse() throws Exception {

        Exception exception = new WebApplicationException();
        Response response = handler.toResponse(exception);

        assertNotNull(response);
        assertNull(response.getEntity());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),  response.getStatus());
    }

    @Test
    public void testConversionOfWebApplicationResponseWhenUmarshalExceptionResultBadRequest() throws Exception {

        SAXParseException2 mockSaxParseException = mock(SAXParseException2.class);
        Exception exception = new WebApplicationException(mockSaxParseException);
        Response response = handler.toResponse(exception);

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),  response.getStatus());
    }

    @Test
    public void testConversionWhenJsonParseExceptionResultBadRequest() throws Exception {

        JsonLocation jsonLocation = mock(JsonLocation.class);
        Exception exception = new JsonParseException("", jsonLocation);
        Response response = handler.toResponse(exception);

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),  response.getStatus());
    }

    @Test
    public void testConversionWhenJsonMappingExceptionResultBadRequest() throws Exception {

        JsonLocation jsonLocation = mock(JsonLocation.class);
        Exception exception = new JsonMappingException("", jsonLocation);
        Response response = handler.toResponse(exception);

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),  response.getStatus());
    }

    @Test
    public void testConversionWhenUnknownExceptionResultBadRequest() throws Exception {

        Exception exception = mock(Exception.class);
        Response response = handler.toResponse(exception);

        when(request.getMethod()).thenReturn("GET");

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),  response.getStatus());


    }

    @Test
    public void testConversionWhenUnknownExceptionResultBadRequestForXmlResponseType() throws Exception {

        List<MediaType> outputMediaTypes = new ArrayList<>();
        outputMediaTypes.add(MediaType.valueOf("application/xml"));
        when(request.getMethod()).thenReturn("GET");
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);

        Exception exception = mock(Exception.class);
        Response response = handler.toResponse(exception);

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),  response.getStatus());
    }
}