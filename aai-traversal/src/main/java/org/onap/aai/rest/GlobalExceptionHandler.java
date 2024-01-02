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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.janusgraph.core.SchemaViolationException;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String NODE_NODE_FOUND_CODE = "AAI_6148";
  private static final String AAI_4007 = "AAI_4007";

  @ExceptionHandler({JsonParseException.class, JsonMappingException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleJsonException(
  JsonParseException exception,
  WebRequest request
  ){
    return buildAAIExceptionResponse(new AAIException(AAI_4007, exception));
  }

  @ExceptionHandler({SchemaViolationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleSchemaViolationException(
  SchemaViolationException exception,
  WebRequest request
  ){
    return buildAAIExceptionResponse(new AAIException("AAI_4020", exception));
  }

  @ExceptionHandler({AAIException.class})
  public ResponseEntity<String> handleAAIException(
  AAIException exception,
  WebRequest request
  ){
    // This specific error code should result in a 404 error response
    if (NODE_NODE_FOUND_CODE.equals(exception.getCode())) {
      return buildAAIExceptionResponse(exception, HttpStatus.NOT_FOUND);
    }
    return buildAAIExceptionResponse(exception);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handleUnknownException(
      Exception exception,
      WebRequest request) {
    return defaultException(exception);
  }

  private ResponseEntity<String> buildAAIExceptionResponse(AAIException exception) {
    return buildAAIExceptionResponse(exception, HttpStatus.BAD_REQUEST);
  }

  private ResponseEntity<String> buildAAIExceptionResponse(AAIException exception, HttpStatus status) {
    String body = getResponseBody(exception);
    return new ResponseEntity<>(body, new HttpHeaders(), status);
  }

  private String getResponseBody(AAIException exception) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
        .getRequest();
    ArrayList<String> templateVars = new ArrayList<>();
    templateVars.add(request.getMethod());
    templateVars.add(request.getRequestURI());

    List<MediaType> mediaTypes = Collections.singletonList(MediaType.APPLICATION_JSON_TYPE);
    String body = ErrorLogHelper.getRESTAPIErrorResponse(
        mediaTypes, exception, templateVars);
    return body;
  }

  private ResponseEntity<String> defaultException(Exception exception) {
    return buildAAIExceptionResponse(new AAIException("AAI_4000", exception));
  }
}
