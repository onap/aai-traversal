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
package org.onap.aai.interceptors;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class AAILogJAXRSInInterceptorTest {

    private AAILogJAXRSInInterceptor aaiLogJAXRSInInterceptor;

    private Message message;
    private Exchange exchange;
    private InputStream is;
    private Map<String, List<String>> headers;


    @Before
    public void setup(){

        aaiLogJAXRSInInterceptor = new AAILogJAXRSInInterceptor();

        message  = mock(Message.class);
        exchange = spy(new ExchangeImpl());

        is = getClass().getClassLoader().getResourceAsStream("logback.xml");

        headers = new HashMap<>();
        headers.put("X-FromAppId", Arrays.asList("JUNIT"));
        headers.put("X-TransactionId", Arrays.asList("JUNIT"));
        headers.put("Content-Type", Arrays.asList("application/json"));
        headers.put("Accept", Arrays.asList("application/json"));
    }

    @Test
    public void testHandleMessageWhenNotCamelRequest() throws IOException {

        when(message.getExchange()).thenReturn(exchange);
        when(message.getContent(InputStream.class)).thenReturn(is);
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);
        when(message.get(Message.CONTENT_TYPE)).thenReturn("*/*");

        when(message.get("CamelHttpUrl")).thenReturn("/somestring");
        aaiLogJAXRSInInterceptor.handleMessage(message);
    }

    @Test
    public void testHandleMessageWhenUUIDHasMultiple() throws IOException {

        Map<String, List<String>> headers = new HashMap<>();

        headers.put("X-FromAppId", Arrays.asList("JUNIT"));
        headers.put("X-TransactionId", Arrays.asList("jfasodjf:fjaosjfidsaj:afsidjfaodfja"));
        headers.put("Content-Type", Arrays.asList("application/json"));
        headers.put("Accept", Arrays.asList("application/json"));

        when(message.getExchange()).thenReturn(exchange);
        when(message.getContent(InputStream.class)).thenReturn(is);
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);
        when(message.get(Message.CONTENT_TYPE)).thenReturn("*/*");

        when(message.get("CamelHttpUrl")).thenReturn("/somestring");
        aaiLogJAXRSInInterceptor.handleMessage(message);
    }

    @Test
    public void testHandleMessageWhenMissingTransactionId() throws IOException {

        Map<String, List<String>> headers = new HashMap<>();

        headers.put("X-FromAppId", Arrays.asList("JUNIT"));
        headers.put("Content-Type", Arrays.asList("application/json"));
        headers.put("Accept", Arrays.asList("application/json"));

        when(message.getExchange()).thenReturn(exchange);
        when(message.getContent(InputStream.class)).thenReturn(is);
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);
        when(message.get(Message.CONTENT_TYPE)).thenReturn("*/*");

        when(message.get("CamelHttpUrl")).thenReturn("/somestring");
        aaiLogJAXRSInInterceptor.handleMessage(message);
    }

    @Test
    public void testHandleMessageWhenMissingContentType() throws IOException {

        Map<String, List<String>> headers = new HashMap<>();

        headers.put("X-FromAppId", Arrays.asList("JUNIT"));
        headers.put("X-TransactionId", Arrays.asList("jfasodjf:fjaosjfidsaj:afsidjfaodfja"));
        headers.put("Accept", Arrays.asList("application/json"));

        when(message.getExchange()).thenReturn(exchange);
        when(message.getContent(InputStream.class)).thenReturn(is);
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);
        when(message.get(Message.CONTENT_TYPE)).thenReturn("*/*");

        when(message.get("CamelHttpUrl")).thenReturn("/somestring");
        aaiLogJAXRSInInterceptor.handleMessage(message);
    }

    @Test
    public void testHandleMessageWhenQueryExistsAndUriEcho() throws IOException {

        Map<String, List<String>> headers = new HashMap<>();

        headers.put("X-FromAppId", Arrays.asList("JUNIT"));
        headers.put("X-TransactionId", Arrays.asList("jfasodjf:fjaosjfidsaj:afsidjfaodfja"));
        headers.put("Content-Type", Arrays.asList("application/json"));
        headers.put("Accept", Arrays.asList("application/json"));

        when(message.getExchange()).thenReturn(exchange);
        when(message.getContent(InputStream.class)).thenReturn(is);
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);
        when(message.get(Message.CONTENT_TYPE)).thenReturn("*/*");
        when(message.get(Message.QUERY_STRING)).thenReturn(null);
        when(exchange.containsKey("AAI_LOGGING_HBASE_ENABLED")).thenReturn(true);
        when(exchange.remove("AAI_LOGGING_HBASE_ENABLED")).thenReturn("");
        when(exchange.containsKey("AAI_LOGGING_TRACE_ENABLED")).thenReturn(true);
        when(exchange.remove("AAI_LOGGING_TRACE_ENABLED")).thenReturn("");
        when(message.get("CamelHttpUrl")).thenReturn("/util/echo");
        aaiLogJAXRSInInterceptor.handleMessage(message);
    }

    @Test
    public void testHandleMessageWhenQueryExistsAndUriTranslog() throws IOException {

        Map<String, List<String>> headers = new HashMap<>();

        headers.put("X-FromAppId", Arrays.asList("JUNIT"));
        headers.put("X-TransactionId", Arrays.asList("jfasodjf:fjaosjfidsaj:afsidjfaodfja"));
        headers.put("Content-Type", Arrays.asList("application/json"));
        headers.put("Accept", Arrays.asList("application/json"));

        when(message.getExchange()).thenReturn(exchange);
        when(message.getContent(InputStream.class)).thenReturn(is);
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);
        when(message.get(Message.CONTENT_TYPE)).thenReturn("*/*");
        when(message.get(Message.QUERY_STRING)).thenReturn(null);
        when(exchange.containsKey("AAI_LOGGING_HBASE_ENABLED")).thenReturn(true);
        when(exchange.remove("AAI_LOGGING_HBASE_ENABLED")).thenReturn("");
        when(exchange.containsKey("AAI_LOGGING_TRACE_ENABLED")).thenReturn(true);
        when(exchange.remove("AAI_LOGGING_TRACE_ENABLED")).thenReturn("");
        when(message.get("CamelHttpUrl")).thenReturn("/translog/");
        aaiLogJAXRSInInterceptor.handleMessage(message);
    }

    @Test
    public void testHandleMessageWhenPutMessageKeyReturnsException() throws IOException {

        Map<String, List<String>> headers = new HashMap<>();

        headers.put("X-FromAppId", Arrays.asList("JUNIT"));
        headers.put("X-TransactionId", Arrays.asList("jfasodjf:fjaosjfidsaj:afsidjfaodfja"));
        headers.put("Content-Type", Arrays.asList("application/json"));
        headers.put("Accept", Arrays.asList("application/json"));

        when(message.getExchange()).thenReturn(exchange);
        when(message.getContent(InputStream.class)).thenReturn(is);
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);
        when(message.get(Message.CONTENT_TYPE)).thenReturn("*/*");
        when(message.get(Message.QUERY_STRING)).thenReturn(null);
        when(exchange.containsKey("AAI_LOGGING_HBASE_ENABLED")).thenReturn(true);
        when(exchange.remove("AAI_LOGGING_HBASE_ENABLED")).thenReturn("");
        when(exchange.containsKey("AAI_LOGGING_TRACE_ENABLED")).thenReturn(true);
        when(exchange.remove("AAI_LOGGING_TRACE_ENABLED")).thenReturn("");
        when(message.get("CamelHttpUrl")).thenReturn("/translog/");
        when(message.get(Message.ENCODING)).thenReturn("http");
        when(message.get(Message.RESPONSE_CODE)).thenReturn(200);

        aaiLogJAXRSInInterceptor.handleMessage(message);
    }
}