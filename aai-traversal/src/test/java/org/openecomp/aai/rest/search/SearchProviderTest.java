/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.aai.rest.search;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.aai.dbmap.DBConnectionType;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.LoaderFactory;
import org.openecomp.aai.introspection.ModelType;
import org.openecomp.aai.introspection.Version;
import org.openecomp.aai.serialization.db.DBSerializer;
import org.openecomp.aai.serialization.engines.QueryStyle;
import org.openecomp.aai.serialization.engines.TitanDBEngine;
import org.openecomp.aai.serialization.engines.TransactionalGraphEngine;
import org.openecomp.aai.serialization.queryformats.utils.UrlBuilder;

import javax.ws.rs.core.*;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchProviderTest {

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    private static final Set<Integer> VALID_HTTP_STATUS_CODES = new HashSet<>();

    private static final Version version = Version.getLatest();
    private static final ModelType introspectorFactoryType = ModelType.MOXY;
    private static final QueryStyle queryStyle = QueryStyle.TRAVERSAL;
    private static final DBConnectionType type = DBConnectionType.REALTIME;

    private Loader loader;
    private TransactionalGraphEngine dbEngine;

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

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(SearchProviderTest.class.getName());

    @Before
    public void setup(){
        logger.info("Starting the setup for the integration tests of Rest Endpoints");
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("BUNDLECONFIG_DIR", "bundleconfig-local");

        searchProvider      = new SearchProvider();
        httpHeaders         = mock(HttpHeaders.class);
        uriInfo             = mock(UriInfo.class);

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
        when(httpHeaders.getRequestHeader("X-FromAppId")).thenReturn(Arrays.asList("JUNIT"));
        when(httpHeaders.getRequestHeader("X-TransactionId")).thenReturn(Arrays.asList("JUNIT"));

        when(httpHeaders.getRequestHeader("aai-request-context")).thenReturn(aaiRequestContextList);


        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(uriInfo.getQueryParameters(false)).thenReturn(queryParameters);

        // TODO - Check if this is valid since RemoveDME2QueryParameters seems to be very unreasonable
        Mockito.doReturn(null).when(queryParameters).remove(anyObject());

        when(httpHeaders.getMediaType()).thenReturn(APPLICATION_JSON);
        loader = LoaderFactory.createLoaderForVersion(introspectorFactoryType, version);
        dbEngine = new TitanDBEngine(
                queryStyle,
                type,
                loader);
    }

    @Test
    public void testNodesQueryInvalidData() throws Exception {

        List<String> keys = new ArrayList<>();
        keys.add("cloud-region.cloud-owner:test-aic");

        List<String> includeStrings = new ArrayList<>();
        includeStrings.add("cloud-region");

        DBSerializer serializer = new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        UrlBuilder urlBuilder = new UrlBuilder(version, serializer);

        Response response = searchProvider.getNodesQueryResponse(
                httpHeaders,
                null,
                "cloud-region",
                keys,
                includeStrings,
                version.toString()
        );

        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        response = searchProvider.getNodesQueryResponse(
                httpHeaders,
                null,
                "cloud-region",
                keys,
                includeStrings,
                "latest"
        );

        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
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

        DBSerializer serializer = new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        UrlBuilder urlBuilder = new UrlBuilder(version, serializer);

        Response response = searchProvider.getNodesQueryResponse(
                httpHeaders,
                null,
                "cloud-region",
                keys,
                includeStrings,
                version.toString()
        );

        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGenericQueryInvalidData() throws Exception {

        List<String> keys = new ArrayList<>();
        keys.add("cloud-region.cloud-owner:test-aic");

        List<String> includeStrings = new ArrayList<>();
        includeStrings.add("cloud-region");

        DBSerializer serializer = new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        UrlBuilder urlBuilder = new UrlBuilder(version, serializer);

        Response response = searchProvider.getGenericQueryResponse(
                httpHeaders,
                null,
                "cloud-region",
                keys,
                includeStrings,
                0,
                version.toString()
        );

        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        response = searchProvider.getNodesQueryResponse(
                httpHeaders,
                null,
                "cloud-region",
                keys,
                includeStrings,
                "latest"
        );

        assertNotNull(response);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    public void testGenericQueryInvalidHeaders() throws Exception {

        List<String> keys = new ArrayList<>();
        keys.add("cloud-region.cloud-owner:test-aic");

        List<String> includeStrings = new ArrayList<>();
        includeStrings.add("cloud-region");

        httpHeaders = mock(HttpHeaders.class);

        when(httpHeaders.getRequestHeader("X-FromAppId")).thenThrow(IllegalArgumentException.class);
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);

        DBSerializer serializer = new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        UrlBuilder urlBuilder = new UrlBuilder(version, serializer);

        Response response = searchProvider.getGenericQueryResponse(
                httpHeaders,
                null,
                "cloud-region",
                keys,
                includeStrings,
                0,
                version.toString()
        );

        assertNotNull(response);
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

}
