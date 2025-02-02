/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2023 Deutsche Telekom. All rights reserved.
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

package org.onap.aai.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;

import org.janusgraph.core.SchemaViolationException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.aai.entities.AAIErrorResponse;
import org.onap.aai.exceptions.AAIException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GlobalExceptionHandlerTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private GlobalExceptionHandler springExceptionHandler;

    @Mock RequestContextHolder requestContextHolder;

    @Mock
    private WebRequest webRequest;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/aai/v14/dsl");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void testHandleJsonParseException() throws JsonMappingException, JsonProcessingException {
        JsonParser jsonParser = mock(JsonParser.class);
        JsonParseException exception = new JsonParseException(jsonParser, "");
        ResponseEntity<String> response = springExceptionHandler.handleJsonException(exception, webRequest);
        AAIErrorResponse responseEntity = objectMapper.readValue(response.getBody(), AAIErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertEquals("SVC3102",responseEntity.getRequestError().getServiceException().getMessageId());
        assertEquals("Error parsing input performing %1 on %2 (msg=%3) (ec=%4)",responseEntity.getRequestError().getServiceException().getText());
        assertEquals("PUT",responseEntity.getRequestError().getServiceException().getVariables().get(0));
        assertEquals("/aai/v14/dsl",responseEntity.getRequestError().getServiceException().getVariables().get(1));
        assertEquals("Input parsing error:com.fasterxml.jackson.core.JsonParseException: ",responseEntity.getRequestError().getServiceException().getVariables().get(2));
        assertEquals("ERR.5.4.4007",responseEntity.getRequestError().getServiceException().getVariables().get(3));
    }

    @Test
    public void testHandleSchemaViolationException() throws JsonMappingException, JsonProcessingException {
        SchemaViolationException exception = Mockito.mock(SchemaViolationException.class);
        ResponseEntity<String> response = springExceptionHandler.handleSchemaViolationException(exception, webRequest);
        AAIErrorResponse responseEntity = objectMapper.readValue(response.getBody(), AAIErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        assertEquals("SVC3002",responseEntity.getRequestError().getServiceException().getMessageId());
        assertEquals("Error writing output performing %1 on %2 (msg=%3) (ec=%4)",responseEntity.getRequestError().getServiceException().getText());
        assertEquals("PUT",responseEntity.getRequestError().getServiceException().getVariables().get(0));
        assertEquals("/aai/v14/dsl",responseEntity.getRequestError().getServiceException().getVariables().get(1));
        assertEquals("ERR.5.4.4020",responseEntity.getRequestError().getServiceException().getVariables().get(3));
    }

    @Test
    public void testHandleAAIException() throws JsonMappingException, JsonProcessingException {
        AAIException exception = new AAIException("AAI_4009");
        ResponseEntity<String> response = springExceptionHandler.handleAAIException(exception, webRequest);
        AAIErrorResponse responseEntity = objectMapper.readValue(response.getBody(), AAIErrorResponse.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("SVC3000",responseEntity.getRequestError().getServiceException().getMessageId());
        assertEquals("PUT",responseEntity.getRequestError().getServiceException().getVariables().get(0));
        assertEquals("/aai/v14/dsl",responseEntity.getRequestError().getServiceException().getVariables().get(1));
        assertEquals("Invalid X-FromAppId in header",responseEntity.getRequestError().getServiceException().getVariables().get(2));
        assertEquals("4.0.4009",responseEntity.getRequestError().getServiceException().getVariables().get(3));
    }

    @Test
    public void testHandleUnknownException() throws Exception {
        Exception exception = new Exception();
        ResponseEntity<String> response = springExceptionHandler.handleUnknownException(exception, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AAIErrorResponse responseEntity = objectMapper.readValue(response.getBody(), AAIErrorResponse.class);
        assertEquals("SVC3002",responseEntity.getRequestError().getServiceException().getMessageId());
        assertEquals("Error writing output performing %1 on %2 (msg=%3) (ec=%4)",responseEntity.getRequestError().getServiceException().getText());
        assertEquals("PUT",responseEntity.getRequestError().getServiceException().getVariables().get(0));
        assertEquals("/aai/v14/dsl",responseEntity.getRequestError().getServiceException().getVariables().get(1));
        assertEquals("Internal Error:java.lang.Exception",responseEntity.getRequestError().getServiceException().getVariables().get(2));
        assertEquals("ERR.5.4.4000",responseEntity.getRequestError().getServiceException().getVariables().get(3));
    }
}
