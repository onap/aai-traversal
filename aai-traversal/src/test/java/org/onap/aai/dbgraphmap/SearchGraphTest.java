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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.dbgraphmap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.janusgraph.graphdb.types.system.EmptyVertex;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.AAISetup;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.parsers.relationship.RelationshipToURI;
import org.onap.aai.rest.util.AAIExtensionMap;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.JanusGraphDBEngine;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.utils.UrlBuilder;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.util.GenericQueryBuilder;
import org.onap.aai.util.NodesQueryBuilder;

public class SearchGraphTest extends AAISetup {

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    private static final Set<Integer> VALID_HTTP_STATUS_CODES = new HashSet<>();

    private SchemaVersion version;
    private final static ModelType introspectorFactoryType = ModelType.MOXY;
    private final static QueryStyle queryStyle = QueryStyle.TRAVERSAL;

    static {
        VALID_HTTP_STATUS_CODES.add(200);
        VALID_HTTP_STATUS_CODES.add(201);
        VALID_HTTP_STATUS_CODES.add(204);
    }

    private HttpHeaders httpHeaders;

    private UriInfo uriInfo;

    private MultivaluedMap<String, String> headersMultiMap;
    private MultivaluedMap<String, String> queryParameters;

    private List<String> aaiRequestContextList;

    private List<MediaType> outputMediaTypes;

    private Loader loader;

    private TransactionalGraphEngine dbEngine;

    @Before
    public void setup() {

        version = schemaVersions.getDefaultVersion();
        httpHeaders = mock(HttpHeaders.class);
        uriInfo = mock(UriInfo.class);

        headersMultiMap = new MultivaluedHashMap<>();
        queryParameters = Mockito.spy(new MultivaluedHashMap<>());

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
        when(httpHeaders.getRequestHeader("X-FromAppId"))
            .thenReturn(Collections.singletonList("JUNIT"));
        when(httpHeaders.getRequestHeader("X-TransactionId"))
            .thenReturn(Collections.singletonList("JUNIT"));

        when(httpHeaders.getRequestHeader("aai-request-context")).thenReturn(aaiRequestContextList);

        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(uriInfo.getQueryParameters(false)).thenReturn(queryParameters);

        // TODO - Check if this is valid since RemoveDME2QueryParameters seems to be very
        // unreasonable
        Mockito.doReturn(null).when(queryParameters).remove(anyObject());

        when(httpHeaders.getMediaType()).thenReturn(APPLICATION_JSON);
        loader = loaderFactory.createLoaderForVersion(introspectorFactoryType, version);
        dbEngine = new JanusGraphDBEngine(queryStyle, loader);
    }

    @Test(expected = AAIException.class)
    public void runNodesQuery() throws AAIException {
        DBSerializer serializer =
            new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        UrlBuilder urlBuilder = new UrlBuilder(version, serializer, schemaVersions, basePath);
        searchGraph.runNodesQuery(new NodesQueryBuilder().setHeaders(httpHeaders)
            .setTargetNodeType("").setEdgeFilterParams(null).setFilterParams(null)
            .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
    }

    @Test(expected = AAIException.class)
    public void runNodesQueryNull() throws AAIException {
        DBSerializer serializer =
            new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        UrlBuilder urlBuilder = new UrlBuilder(version, serializer, schemaVersions, basePath);
        searchGraph.runNodesQuery(new NodesQueryBuilder().setHeaders(httpHeaders)
            .setTargetNodeType("nnn").setEdgeFilterParams(null).setFilterParams(null)
            .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
    }

    @Test(expected = AAIException.class)
    public void testRunGenericQueryFailWhenInvalidRelationshipList() throws AAIException {

        List<String> keys = new ArrayList<>();
        keys.add("cloud-region.cloud-owner:test-aic");

        List<String> includeStrings = new ArrayList<>();
        includeStrings.add("cloud-region");

        DBSerializer serializer =
            new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        UrlBuilder urlBuilder = new UrlBuilder(version, serializer, schemaVersions, basePath);
        Response response = searchGraph.runGenericQuery(
            new GenericQueryBuilder().setHeaders(httpHeaders).setStartNodeType("service-instance")
                .setStartNodeKeyParams(keys).setIncludeNodeTypes(includeStrings).setDepth(1)
                .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
        System.out.println(response);
    }

    @Test(expected = AAIException.class)
    public void testRunGenericQueryFailWhenInvalidRelationshipList1() throws AAIException {

        List<String> keys = new ArrayList<>();
        keys.add("cloud-region.cloud-owner:test-aic");

        List<String> includeStrings = new ArrayList<>();
        includeStrings.add("cloud-region");

        DBSerializer serializer =
            new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        UrlBuilder urlBuilder = new UrlBuilder(version, serializer, schemaVersions, basePath);
        Response response = searchGraph.runGenericQuery(
            new GenericQueryBuilder().setHeaders(httpHeaders).setStartNodeType(null)
                .setStartNodeKeyParams(keys).setIncludeNodeTypes(includeStrings).setDepth(1)
                .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
        System.out.println(response);
    }

    @Test(expected = AAIException.class)
    public void testRunGenericQueryFailWhenInvalidRelationshipList2() throws AAIException {

        List<String> includeStrings = new ArrayList<>();
        includeStrings.add("cloud-region");

        DBSerializer serializer =
            new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        UrlBuilder urlBuilder = new UrlBuilder(version, serializer, schemaVersions, basePath);
        Response response = searchGraph
            .runGenericQuery(new GenericQueryBuilder().setHeaders(httpHeaders).setStartNodeType("")
                .setStartNodeKeyParams(null).setIncludeNodeTypes(includeStrings).setDepth(1)
                .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
        System.out.println(response);
    }

    @Test(expected = AAIException.class)
    public void testRunGenericQueryFailWhenInvalidRelationshipList3() throws AAIException {

        List<String> keys = new ArrayList<>();
        keys.add("cloud-region.cloud-owner:test-aic");

        DBSerializer serializer =
            new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        UrlBuilder urlBuilder = new UrlBuilder(version, serializer, schemaVersions, basePath);
        Response response =
            searchGraph.runGenericQuery(new GenericQueryBuilder().setHeaders(httpHeaders)
                .setStartNodeType("").setStartNodeKeyParams(keys).setIncludeNodeTypes(null)
                .setDepth(1).setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
        System.out.println(response);
    }

    @Test
    public void createSearchResults1() throws Exception {

        List<Vertex> keys = new ArrayList<>();
        Vertex vertex = new EmptyVertex();
        keys.add(vertex);
        UrlBuilder urlBuilder = mock(UrlBuilder.class);
        when(urlBuilder.pathed(vertex)).thenReturn("cloud-region");
        Stream<Vertex> stream = mock(Stream.class);
        when(stream.isParallel()).thenReturn(true);

        RelationshipToURI relationshipToURI = mock(RelationshipToURI.class);
        URI uri = new URI("");
        when(relationshipToURI.getUri()).thenReturn(uri);

        Introspector response = searchGraph.createSearchResults(loader, urlBuilder, keys);
        System.out.println(response);
    }

    @Test(expected = AAIException.class)
    public void executeModelOperationTest() throws Exception {
        Vertex vertex = new EmptyVertex();
        vertex.property("model-name");
        AAIExtensionMap map = mock(AAIExtensionMap.class);
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        when(map.getHttpServletRequest()).thenReturn(servletRequest);
        when(servletRequest.getContentType()).thenReturn("application/json");
        DynamicEntity modelAndNamedQuerySearch = mock(DynamicEntity.class);
        when(modelAndNamedQuerySearch.isSet("topNodeType")).thenReturn(true);

        searchGraph.executeModelOperation("", "", "", true, map);
    }

    @Test(expected = AAIException.class)
    public void executeModelOperationXMLTest() throws Exception {
        Vertex vertex = new EmptyVertex();
        vertex.property("model-name");
        AAIExtensionMap map = mock(AAIExtensionMap.class);
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);

        when(map.getHttpServletRequest()).thenReturn(servletRequest);
        when(servletRequest.getContentType()).thenReturn("application/xml");

        DynamicEntity modelAndNamedQuerySearch = mock(DynamicEntity.class);
        when(modelAndNamedQuerySearch.isSet("queryParameters")).thenReturn(true);

        searchGraph.executeModelOperation("", "", "", true, map);
    }

    @Test
    public void runNodesQueryTest() throws AAIException {
        UrlBuilder urlBuilder = mock(UrlBuilder.class);
        List<String> filter = new ArrayList<>();
        filter.add("model:EQUALS:DOES-NOT-EXIST:AAI");
        List<String> edgeFilter = new ArrayList<>();
        edgeFilter.add("model:DOES-NOT-EXIST:DOES-NOT-EXIST:AAI");
        Response response = searchGraph.runNodesQuery(
            new NodesQueryBuilder().setHeaders(httpHeaders).setTargetNodeType("model-ver")
                .setEdgeFilterParams(edgeFilter).setFilterParams(filter).setDbEngine(dbEngine)
                .setLoader(loader).setUrlBuilder(urlBuilder));
        Assert.assertNotNull(response);
    }

    @Test
    public void runNodesQueryExistsTest() throws AAIException {
        UrlBuilder urlBuilder = mock(UrlBuilder.class);
        List<String> filter = new ArrayList<String>();
        filter.add("model:EQUALS:DOES-NOT-EXIST:AAI");
        List<String> edgeFilter = new ArrayList<String>();
        edgeFilter.add("model:EXISTS:DOES-NOT-EXIST:AAI");
        Response response = searchGraph.runNodesQuery(
            new NodesQueryBuilder().setHeaders(httpHeaders).setTargetNodeType("model-ver")
                .setEdgeFilterParams(edgeFilter).setFilterParams(filter).setDbEngine(dbEngine)
                .setLoader(loader).setUrlBuilder(urlBuilder));
        Assert.assertNotNull(response);
    }

    @Test
    public void runNodesQueryTargetNodeTypeNullTest() throws AAIException {
        UrlBuilder urlBuilder = mock(UrlBuilder.class);
        List<String> filter = new ArrayList<String>();
        filter.add("model:EQUALS:DOES-NOT-EXIST:AAI");
        List<String> edgeFilter = new ArrayList<String>();
        edgeFilter.add("model:EXISTS:DOES-NOT-EXIST:AAI");

        assertThrows(AAIException.class,
                () -> searchGraph.runNodesQuery(
                        new NodesQueryBuilder().setHeaders(httpHeaders).setTargetNodeType(null)
                                .setEdgeFilterParams(edgeFilter).setFilterParams(filter).setDbEngine(dbEngine)
                                .setLoader(loader).setUrlBuilder(urlBuilder)));
    }

    @Test
    public void runNodesQueryTestDOESNOTEQUAL() throws AAIException {
        UrlBuilder urlBuilder = mock(UrlBuilder.class);
        List<String> filter = new ArrayList<String>();
        filter.add("model:DOES-NOT-EQUAL:DOES-NOT-EXIST");
        List<String> edgeFilter = new ArrayList<>();
        Response response = searchGraph.runNodesQuery(new NodesQueryBuilder().setHeaders(httpHeaders)
                .setTargetNodeType("model-ver").setEdgeFilterParams(edgeFilter).setFilterParams(filter)
                .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
        Assert.assertNotNull(response);
    }

    @Test
    public void runNodesQueryTestGreaterThan3() throws AAIException {
        UrlBuilder urlBuilder = mock(UrlBuilder.class);
        List<String> filter = new ArrayList<>();
        filter.add("model:DOES-NOT-EQUAL:DOES-NOT-EXIST:AAI");
        List<String> edgeFilter = new ArrayList<>();
        Response response = searchGraph.runNodesQuery(new NodesQueryBuilder().setHeaders(httpHeaders)
                .setTargetNodeType("model-ver").setEdgeFilterParams(edgeFilter).setFilterParams(filter)
                .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
        Assert.assertNotNull(response);
    }

    @Test
    public void runNodesQueryTestNoFilterParam() throws AAIException {
        UrlBuilder urlBuilder = mock(UrlBuilder.class);
        List<String> filter = new ArrayList<>();
        //filter.add("model:DOES-NOT-EQUAL:DOES-NOT-EXIST:AAI");
        List<String> edgeFilter = new ArrayList<>();
        Response response = searchGraph.runNodesQuery(new NodesQueryBuilder().setHeaders(httpHeaders)
                .setTargetNodeType("model-ver").setEdgeFilterParams(edgeFilter).setFilterParams(filter)
                .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
        Assert.assertNotNull(response);
    }

    @Test
    public void runNodesQueryTestGreaterThanExists() throws AAIException {
        UrlBuilder urlBuilder = mock(UrlBuilder.class);
        List<String> filter = new ArrayList<>();
        filter.add("model:EXISTS:DOES-NOT-EXIST:AAI");
        List<String> edgeFilter = new ArrayList<>();
        Response response = searchGraph.runNodesQuery(new NodesQueryBuilder().setHeaders(httpHeaders)
                .setTargetNodeType("model-ver").setEdgeFilterParams(edgeFilter).setFilterParams(filter)
                .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
        Assert.assertNotNull(response);
    }

    @Test(expected = AAIException.class)
    public void runNodesQueryTestGreaterThanDoesNotExists() throws AAIException {
        UrlBuilder urlBuilder = mock(UrlBuilder.class);
        List<String> filter = new ArrayList<>();
        filter.add("model:DOES_NOT_EXIST:DOES-NOT-EXIST:AAI");
        List<String> edgeFilter = new ArrayList<>();
        Response response = searchGraph.runNodesQuery(new NodesQueryBuilder().setHeaders(httpHeaders)
                .setTargetNodeType("model-ver").setEdgeFilterParams(edgeFilter).setFilterParams(filter)
                .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));
        Assert.assertNotNull(response);
    }
}
