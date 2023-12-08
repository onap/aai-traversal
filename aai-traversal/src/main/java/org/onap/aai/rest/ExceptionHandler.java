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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sun.istack.SAXParseException2;

import java.util.ArrayList;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.janusgraph.core.SchemaViolationException;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;

/**
 * The Class ExceptionHandler.
 */
@Provider
@Priority(10000)
public class ExceptionHandler implements ExceptionMapper<Exception> {

    private static final String AAI_4007 = "AAI_4007";

    @Context
    private HttpServletRequest request;

    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(Exception exception) {
        // the general case is that cxf will give us a WebApplicationException
        // with a linked exception
        if (exception instanceof WebApplicationException) {
            if (exception.getCause() instanceof SAXParseException2) {
                return buildAAIExceptionResponse(new AAIException(AAI_4007, exception));
            } else {
                return ((WebApplicationException) exception).getResponse();
            }
        } else if (exception instanceof JsonParseException) {
            // jackson does it differently so we get the direct JsonParseException
            return buildAAIExceptionResponse(new AAIException(AAI_4007, exception));
        } else if (exception instanceof JsonMappingException) {
            // jackson does it differently so we get the direct JsonParseException
            return buildAAIExceptionResponse(new AAIException(AAI_4007, exception));
        } else if (exception instanceof SchemaViolationException) {
            return buildAAIExceptionResponse(new AAIException("AAI_4020", exception));
        // it didn't get set above, we wrap a general fault here
        } else if (exception instanceof AAIException) {
            return buildAAIExceptionResponse((AAIException) exception);
        } else {
            return defaultException(exception);
        }
    }

    private Response buildAAIExceptionResponse(AAIException exception) {
        ArrayList<String> templateVars = new ArrayList<>();
        templateVars.add(request.getMethod());
        templateVars.add(request.getRequestURI());

        // prefer xml, use json otherwise
        return headers.getAcceptableMediaTypes().stream()
            .filter(MediaType.APPLICATION_ATOM_XML_TYPE::isCompatible)
            .findAny()
            .map(xmlType -> 
                Response.status(400).type(MediaType.APPLICATION_XML_TYPE)
                    .entity(ErrorLogHelper.getRESTAPIErrorResponse(
                        headers.getAcceptableMediaTypes(), exception, templateVars))
                    .build())
            .orElseGet(() -> 
                Response.status(400).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(ErrorLogHelper.getRESTAPIErrorResponse(
                    headers.getAcceptableMediaTypes(), exception, templateVars))
                .build());
    }

    private Response defaultException(Exception exception) {
        return buildAAIExceptionResponse(new AAIException("AAI_4000", exception));
    }
}