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

import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class AAILogJAXRSOutInterceptorTest {

    private AAILogJAXRSOutInterceptor aaiLogJAXRSOutInterceptor;

    private Message message;
    private Exchange exchange;
    private OutputStream out;
    private Map<String, List<String>> headers;
    private Message outMessage;
    private Message inMessage;


    @Before
    public void setup(){

        aaiLogJAXRSOutInterceptor = new AAILogJAXRSOutInterceptor();

        message    = mock(Message.class);
        exchange   = spy(new ExchangeImpl());
        out        = mock(OutputStream.class);
        outMessage = mock(Message.class);
        inMessage  = mock(Message.class);


        headers = new HashMap<>();
        headers.put("X-FromAppId", Arrays.asList("JUNIT"));
        headers.put("X-TransactionId", Arrays.asList("JUNIT"));
        headers.put("Content-Type", Arrays.asList("application/json"));
        headers.put("Accept", Arrays.asList("application/json"));
    }

    @Test
    public void testHandleMessageWhenNotCamelRequest() throws IOException {

        when(message.getExchange()).thenReturn(exchange);
        when(message.getContent(OutputStream.class)).thenReturn(out);
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);
        when(message.get(Message.CONTENT_TYPE)).thenReturn("*/*");
        when(exchange.getOutMessage()).thenReturn(outMessage);
        when(outMessage.getContent(OutputStream.class)).thenReturn(out);
        when(exchange.containsKey("AAI_LOGGING_HBASE_ENABLED")).thenReturn(true);
        when(exchange.remove("AAI_LOGGING_HBASE_ENABLED")).thenReturn("");

        when(message.get("CamelHttpUrl")).thenReturn("/somestring");
        aaiLogJAXRSOutInterceptor.handleMessage(message);
    }

    @Test
    public void testLogCallBack(){

        when(message.getExchange()).thenReturn(exchange);
        when(message.getContent(OutputStream.class)).thenReturn(out);
        when(message.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);
        when(message.get(Message.CONTENT_TYPE)).thenReturn("*/*");
        when(exchange.getOutMessage()).thenReturn(outMessage);

        when(outMessage.getContent(OutputStream.class)).thenReturn(out);
        when(exchange.containsKey("AAI_LOGGING_HBASE_ENABLED")).thenReturn(true);
        when(exchange.remove("AAI_LOGGING_HBASE_ENABLED")).thenReturn("");
        when(exchange.getInMessage()).thenReturn(inMessage);

        when(inMessage.getExchange()).thenReturn(exchange);
        when(inMessage.getContent(OutputStream.class)).thenReturn(out);
        when(inMessage.get(Message.PROTOCOL_HEADERS)).thenReturn(headers);
        when(inMessage.get(Message.CONTENT_TYPE)).thenReturn("*/*");

        AAILogJAXRSOutInterceptor.LoggingCallback loggingCallback = new AAILogJAXRSOutInterceptor().new LoggingCallback(message, out);
        final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(out);
        loggingCallback.onClose(newOut);
    }

}