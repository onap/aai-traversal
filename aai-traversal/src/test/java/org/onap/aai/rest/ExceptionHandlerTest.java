/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Deutsche Telekom SA.
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
package org.onap.aai.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sun.istack.SAXParseException2;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.entities.AAIErrorResponse;

public class ExceptionHandlerTest {

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private HttpHeaders httpHeaders;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ExceptionHandler handler = new ExceptionHandler();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        MultivaluedHashMap<String, String> headersMultiMap = new MultivaluedHashMap<>();

        headersMultiMap.add("X-FromAppId", "JUNIT");
        headersMultiMap.add("X-TransactionId", UUID.randomUUID().toString());
        headersMultiMap.add("Real-Time", "true");
        headersMultiMap.add("Accept", "application/json");
        headersMultiMap.add("aai-request-context", "");

        List<MediaType> outputMediaTypes = new ArrayList<>();
        outputMediaTypes.add(APPLICATION_JSON);
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);
        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/aai/v14/dsl");
    }

    @Test
    public void testConversionOfWebApplicationResponse() throws Exception {

        Exception exception = new WebApplicationException();
        Response response = handler.toResponse(exception);

        assertNotNull(response);
        assertNull(response.getEntity());
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void testConversionOfWebApplicationResponseWhenUmarshalExceptionResultBadRequest()
        throws Exception {

        SAXParseException2 mockSaxParseException = mock(SAXParseException2.class);
        Exception exception = new WebApplicationException(mockSaxParseException);
        Response response = handler.toResponse(exception);
        AAIErrorResponse responseEntity = objectMapper.readValue(response.getEntity().toString(), AAIErrorResponse.class);

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("SVC3102",responseEntity.getRequestError().getServiceException().getMessageId());
        assertEquals("Error parsing input performing %1 on %2 (msg=%3) (ec=%4)",responseEntity.getRequestError().getServiceException().getText());
        assertEquals("PUT",responseEntity.getRequestError().getServiceException().getVariables().get(0));
        assertEquals("/aai/v14/dsl",responseEntity.getRequestError().getServiceException().getVariables().get(1));
        assertEquals("Input parsing error:javax.ws.rs.WebApplicationException: HTTP 500 Internal Server Error",responseEntity.getRequestError().getServiceException().getVariables().get(2));
        assertEquals("ERR.5.4.4007",responseEntity.getRequestError().getServiceException().getVariables().get(3));
    }

    @Test
    public void testConversionWhenJsonParseExceptionResultBadRequest() throws Exception {

        JsonParser jsonParser = mock(JsonParser.class);
        Exception exception = new JsonParseException(jsonParser, "");
        Response response = handler.toResponse(exception);
        AAIErrorResponse responseEntity = objectMapper.readValue(response.getEntity().toString(), AAIErrorResponse.class);

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("SVC3102",responseEntity.getRequestError().getServiceException().getMessageId());
        assertEquals("Error parsing input performing %1 on %2 (msg=%3) (ec=%4)",responseEntity.getRequestError().getServiceException().getText());
        assertEquals("PUT",responseEntity.getRequestError().getServiceException().getVariables().get(0));
        assertEquals("/aai/v14/dsl",responseEntity.getRequestError().getServiceException().getVariables().get(1));
        assertEquals("Input parsing error:com.fasterxml.jackson.core.JsonParseException: ",responseEntity.getRequestError().getServiceException().getVariables().get(2));
        assertEquals("ERR.5.4.4007",responseEntity.getRequestError().getServiceException().getVariables().get(3));
    }

    @Test
    public void testConversionWhenJsonMappingExceptionResultBadRequest() throws Exception {
        JsonParser jsonParser = mock(JsonParser.class);
        Exception exception = JsonMappingException.from(jsonParser,"");
        Response response = handler.toResponse(exception);
        AAIErrorResponse responseEntity = objectMapper.readValue(response.getEntity().toString(), AAIErrorResponse.class);

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("SVC3102",responseEntity.getRequestError().getServiceException().getMessageId());
        assertEquals("Error parsing input performing %1 on %2 (msg=%3) (ec=%4)",responseEntity.getRequestError().getServiceException().getText());
        assertEquals("PUT",responseEntity.getRequestError().getServiceException().getVariables().get(0));
        assertEquals("/aai/v14/dsl",responseEntity.getRequestError().getServiceException().getVariables().get(1));
        assertEquals("Input parsing error:com.fasterxml.jackson.databind.JsonMappingException: ",responseEntity.getRequestError().getServiceException().getVariables().get(2));
        assertEquals("ERR.5.4.4007",responseEntity.getRequestError().getServiceException().getVariables().get(3));
    }

    @Test
    public void testJsonDefaultErrorResponse()
        throws Exception {
        Exception exception = new Exception();
        Response response = handler.toResponse(exception);
        AAIErrorResponse responseEntity = objectMapper.readValue(response.getEntity().toString(), AAIErrorResponse.class);

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("SVC3002",responseEntity.getRequestError().getServiceException().getMessageId());
        assertEquals("Error writing output performing %1 on %2 (msg=%3) (ec=%4)",responseEntity.getRequestError().getServiceException().getText());
        assertEquals("PUT",responseEntity.getRequestError().getServiceException().getVariables().get(0));
        assertEquals("/aai/v14/dsl",responseEntity.getRequestError().getServiceException().getVariables().get(1));
        assertEquals("Internal Error:java.lang.Exception",responseEntity.getRequestError().getServiceException().getVariables().get(2));
        assertEquals("ERR.5.4.4000",responseEntity.getRequestError().getServiceException().getVariables().get(3));
    }

    @Test
    public void testXmlDefaultErrorResponse()
        throws Exception {
        List<MediaType> outputMediaTypes = new ArrayList<>();
        outputMediaTypes.add(MediaType.APPLICATION_XML_TYPE);
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);
        Exception exception = new Exception();
        Response response = handler.toResponse(exception);
        XmlMapper xmlMapper = new XmlMapper();
        AAIErrorResponse responseEntity = xmlMapper.readValue(response.getEntity().toString(), AAIErrorResponse.class);

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("SVC3002",responseEntity.getRequestError().getServiceException().getMessageId());
        assertEquals("Error writing output performing %1 on %2 (msg=%3) (ec=%4)",responseEntity.getRequestError().getServiceException().getText());
        assertEquals("PUT",responseEntity.getRequestError().getServiceException().getVariables().get(0));
        assertEquals("/aai/v14/dsl",responseEntity.getRequestError().getServiceException().getVariables().get(1));
        assertEquals("Internal Error:java.lang.Exception",responseEntity.getRequestError().getServiceException().getVariables().get(2));
        assertEquals("ERR.5.4.4000",responseEntity.getRequestError().getServiceException().getVariables().get(3));
    }

    @Test
    public void testConversionWhenUnknownExceptionResultBadRequest() throws Exception {

        Exception exception = mock(Exception.class);
        Response response = handler.toResponse(exception);

        when(request.getMethod()).thenReturn("GET");

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testConversionWhenUnknownExceptionResultBadRequestForXmlResponseType()
        throws Exception {

        List<MediaType> outputMediaTypes = new ArrayList<>();
        outputMediaTypes.add(MediaType.valueOf("application/xml"));
        when(request.getMethod()).thenReturn("GET");
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);

        Exception exception = mock(Exception.class);
        Response response = handler.toResponse(exception);

        assertNotNull(response);
        assertNotNull(response.getEntity());
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }
}
