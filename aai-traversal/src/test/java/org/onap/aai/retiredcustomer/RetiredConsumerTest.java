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
package org.onap.aai.retiredcustomer;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.introspection.Version;
import org.onap.aai.rest.retired.RetiredConsumer;

import javax.ws.rs.core.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

public abstract class RetiredConsumerTest {

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    private static final Set<Integer> VALID_HTTP_STATUS_CODES = new HashSet<>();

    static {
        VALID_HTTP_STATUS_CODES.add(200);
        VALID_HTTP_STATUS_CODES.add(201);
        VALID_HTTP_STATUS_CODES.add(204);
    }

    protected RetiredConsumer retiredConsumer;
    protected HttpHeaders httpHeaders;
    protected UriInfo uriInfo;

    private MultivaluedMap<String, String> headersMultiMap;
    private MultivaluedMap<String, String> queryParameters;

    private List<String> aaiRequestContextList;

    private List<MediaType> outputMediaTypes;

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RetiredConsumer.class.getName());

    @Before
    public void setup(){
        logger.info("Starting the setup for the integration tests of Rest Endpoints");

        retiredConsumer     = getRetiredConsumer();
        httpHeaders         = Mockito.mock(HttpHeaders.class);
        uriInfo             = Mockito.mock(UriInfo.class);

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

        when(httpHeaders.getRequestHeader("aai-request-context")).thenReturn(aaiRequestContextList);


        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(uriInfo.getQueryParameters(false)).thenReturn(queryParameters);

        // TODO - Check if this is valid since RemoveDME2QueryParameters seems to be very unreasonable
        Mockito.doReturn(null).when(queryParameters).remove(anyObject());

        when(httpHeaders.getMediaType()).thenReturn(APPLICATION_JSON);
    }

    @Test
    public void testRetiredForAllEndPoints(){
        when(uriInfo.getPath()).thenReturn("/aai/v3/cloud-infrastructure/pservers/pserver/test-pserver1");

        Response response = retiredConsumer.createMessageGet(Version.getLatest().toString(), httpHeaders, uriInfo, null);
        assertNotNull(response);
        assertEquals(Response.Status.GONE.getStatusCode(), response.getStatus());

        response = retiredConsumer.createMessagePost(Version.getLatest().toString(), httpHeaders, uriInfo, null);
        assertNotNull(response);
        assertEquals(Response.Status.GONE.getStatusCode(), response.getStatus());

        response = retiredConsumer.createMessagePatch(Version.getLatest().toString(), httpHeaders, uriInfo, null);
        assertNotNull(response);
        assertEquals(Response.Status.GONE.getStatusCode(), response.getStatus());

        response = retiredConsumer.createMessagePut(Version.getLatest().toString(), httpHeaders, uriInfo, null);
        assertNotNull(response);
        assertEquals(Response.Status.GONE.getStatusCode(), response.getStatus());

        response = retiredConsumer.createMessageDelete(Version.getLatest().toString(), httpHeaders, uriInfo, null);
        assertNotNull(response);
        assertEquals(Response.Status.GONE.getStatusCode(), response.getStatus());
    }

    public abstract RetiredConsumer getRetiredConsumer();
}
