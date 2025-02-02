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
package org.onap.aai;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.javatuples.Pair;
import org.mockito.Mockito;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.parsers.query.QueryParser;
import org.onap.aai.parsers.uri.URIToObject;
import org.onap.aai.rest.db.DBRequest;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTestUtil extends RESTAPI {

    private static final Logger logger = LoggerFactory.getLogger(HttpTestUtil.class);

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    private static final String EMPTY = "";

    protected HttpHeaders httpHeaders;
    protected UriInfo uriInfo;

    protected MultivaluedMap<String, String> headersMultiMap;
    protected MultivaluedMap<String, String> queryParameters;

    protected List<String> aaiRequestContextList;
    protected List<MediaType> outputMediaTypes;

    private SchemaVersions schemaVersions;

    public void init() {

        schemaVersions = (SchemaVersions) SpringContextAware.getBean("schemaVersions");
        httpHeaders = Mockito.mock(HttpHeaders.class);
        uriInfo = Mockito.mock(UriInfo.class);

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

        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);
        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);

        when(httpHeaders.getRequestHeader("aai-request-context")).thenReturn(aaiRequestContextList);

        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(uriInfo.getQueryParameters(false)).thenReturn(queryParameters);

        doReturn(null).when(queryParameters).remove(anyObject());
        when(httpHeaders.getMediaType()).thenReturn(APPLICATION_JSON);
    }

    public Response doPut(String uri, String payload)
        throws UnsupportedEncodingException, AAIException {

        this.init();
        Response response = null;
        boolean success = true;
        TransactionalGraphEngine dbEngine = null;

        try {

            uri = uri.replaceAll("/aai/", "");
            logger.info("Starting the put request for the uri {} with payload {}", uri, payload);

            String[] arr = uri.split("/");

            SchemaVersion version = null;

            if (arr.length > 1) {
                if (arr[0].matches("^v\\d+")) {
                    version = new SchemaVersion(arr[0]);
                    uri = uri.replaceAll("^v\\d+", "");
                }
            }

            if (version == null) {
                version = schemaVersions.getDefaultVersion();
            }
            Mockito.when(uriInfo.getPath()).thenReturn(uri);

            HttpEntry resourceHttpEntry =
                SpringContextAware.getBean("traversalHttpEntry", HttpEntry.class);
            resourceHttpEntry.setHttpEntryProperties(version);
            Loader loader = resourceHttpEntry.getLoader();
            dbEngine = resourceHttpEntry.getDbEngine();

            URI uriObject = UriBuilder.fromPath(uri).build();
            URIToObject uriToObject = new URIToObject(loader, uriObject);

            String objType = uriToObject.getEntityName();
            QueryParser uriQuery = dbEngine.getQueryBuilder().createQueryFromURI(uriObject);

            logger.info("Unmarshalling the payload to this {}", objType);

            Introspector obj;
            HttpMethod httpMethod;
            if (uri.contains("/relationship-list/relationship")) {
                obj = loader.unmarshal("relationship", payload,
                    org.onap.aai.restcore.MediaType.getEnum("application/json"));
                httpMethod = HttpMethod.PUT_EDGE;
            } else {
                obj = loader.unmarshal(objType, payload,
                    org.onap.aai.restcore.MediaType.getEnum("application/json"));
                httpMethod = HttpMethod.PUT;
                this.validateIntrospector(obj, loader, uriObject, httpMethod);
            }

            DBRequest dbRequest = new DBRequest.Builder(httpMethod, uriObject, uriQuery, obj,
                httpHeaders, uriInfo, "JUNIT-TRANSACTION").rawRequestContent(payload).build();

            List<DBRequest> dbRequestList = new ArrayList<>();
            dbRequestList.add(dbRequest);

            Pair<Boolean, List<Pair<URI, Response>>> responsesTuple =
                resourceHttpEntry.process(dbRequestList, "JUNIT");
            response = responsesTuple.getValue1().get(0).getValue1();

        } catch (AAIException e) {
            response =
                this.consumerExceptionResponseGenerator(httpHeaders, uriInfo, HttpMethod.PUT, e);
            success = false;
        } catch (Exception e) {
            AAIException ex = new AAIException("AAI_4000", e);
            response =
                this.consumerExceptionResponseGenerator(httpHeaders, uriInfo, HttpMethod.PUT, ex);
            success = false;
        } finally {
            if (success) {
                if (response != null) {
                    if ((response.getStatus() / 100) == 2) {
                        logger.info(
                            "Successfully completed the PUT request with status {} and committing it to DB",
                            response.getStatus());
                    } else {
                        logFailure(HttpMethod.PUT, response);
                    }
                }
                dbEngine.commit();
            } else {
                if (response != null) {
                    logFailure(HttpMethod.PUT, response);
                }
                dbEngine.rollback();
            }
        }

        return response;
    }

    public Response doGet(String uri) throws UnsupportedEncodingException, AAIException {

        this.init();
        Response response = null;
        boolean success = true;
        TransactionalGraphEngine dbEngine = null;

        try {

            uri = uri.replaceAll("/aai/", "");
            logger.info("Starting the GET request for the uri {} with depth {}", uri, "all");

            String[] arr = uri.split("/");

            SchemaVersion version = null;

            if (arr.length > 1) {
                if (arr[0].matches("^v\\d+")) {
                    version = new SchemaVersion(arr[0]);
                    uri = uri.replaceAll("^v\\d+", "");
                }
            }

            if (version == null) {
                version = schemaVersions.getDefaultVersion();
            }

            HttpEntry resourceHttpEntry =
                SpringContextAware.getBean("traversalHttpEntry", HttpEntry.class);
            resourceHttpEntry.setHttpEntryProperties(version);

            Loader loader = resourceHttpEntry.getLoader();
            dbEngine = resourceHttpEntry.getDbEngine();

            URI uriObject = UriBuilder.fromPath(uri).build();
            URIToObject uriToObject = new URIToObject(loader, uriObject);

            String objType = uriToObject.getEntityName();
            queryParameters.add("depth", "all");
            QueryParser uriQuery =
                dbEngine.getQueryBuilder().createQueryFromURI(uriObject, queryParameters);

            Mockito.when(uriInfo.getPath()).thenReturn(uri);

            logger.info("Unmarshalling the payload to this {}", objType);

            Introspector obj = loader.introspectorFromName(objType);

            DBRequest dbRequest = new DBRequest.Builder(HttpMethod.GET, uriObject, uriQuery, obj,
                httpHeaders, uriInfo, "JUNIT-TRANSACTION").build();

            List<DBRequest> dbRequestList = new ArrayList<>();
            dbRequestList.add(dbRequest);

            Pair<Boolean, List<Pair<URI, Response>>> responsesTuple =
                resourceHttpEntry.process(dbRequestList, "JUNIT");
            response = responsesTuple.getValue1().get(0).getValue1();

        } catch (AAIException e) {
            response =
                this.consumerExceptionResponseGenerator(httpHeaders, uriInfo, HttpMethod.PUT, e);
            success = false;
        } catch (Exception e) {
            AAIException ex = new AAIException("AAI_4000", e);
            response =
                this.consumerExceptionResponseGenerator(httpHeaders, uriInfo, HttpMethod.PUT, ex);
            success = false;
        } finally {
            if (success) {
                if (response != null) {
                    if ((response.getStatus() / 100) == 2) {
                        logger.info(
                            "Successfully completed the GET request with status {} and committing it to DB",
                            response.getStatus());
                    } else {
                        logFailure(HttpMethod.GET, response);
                    }
                }
                dbEngine.commit();
            } else {
                logFailure(HttpMethod.GET, response);
                dbEngine.rollback();
            }
        }

        return response;
    }

    public Response doDelete(String uri, String resourceVersion)
        throws UnsupportedEncodingException, AAIException {

        this.init();
        Response response = null;
        boolean success = true;
        TransactionalGraphEngine dbEngine = null;

        try {

            uri = uri.replaceAll("/aai/", "");
            logger.info("Starting the delete request for the uri {} with resource version {}", uri,
                resourceVersion);

            String[] arr = uri.split("/");

            SchemaVersion version = null;

            if (arr.length > 1) {
                if (arr[0].matches("^v\\d+")) {
                    version = new SchemaVersion(arr[0]);
                    if (!uri.contains("relationship-list/relationship")) {
                        uri = uri.replaceAll("^v\\d+", "");
                    }
                }
            }

            if (version == null) {
                version = schemaVersions.getDefaultVersion();
            }

            Mockito.when(uriInfo.getPath()).thenReturn(uri);
            HttpEntry resourceHttpEntry =
                SpringContextAware.getBean("traversalHttpEntry", HttpEntry.class);
            resourceHttpEntry.setHttpEntryProperties(version);
            Loader loader = resourceHttpEntry.getLoader();
            dbEngine = resourceHttpEntry.getDbEngine();

            URI uriObject = UriBuilder.fromPath(uri).build();
            URIToObject uriToObject = new URIToObject(loader, uriObject);

            String objType = uriToObject.getEntityName();
            queryParameters.add("resource-version", resourceVersion);
            QueryParser uriQuery =
                dbEngine.getQueryBuilder().createQueryFromURI(uriObject, queryParameters);

            logger.info("Unmarshalling the payload to this {}", objType);

            Introspector obj;
            HttpMethod httpMethod;
            if (uri.contains("/relationship-list/relationship")) {
                obj = loader.introspectorFromName("relationship");
                httpMethod = HttpMethod.DELETE_EDGE;
            } else {
                obj = loader.introspectorFromName(objType);
                httpMethod = HttpMethod.DELETE;
            }

            DBRequest dbRequest = new DBRequest.Builder(httpMethod, uriObject, uriQuery, obj,
                httpHeaders, uriInfo, "JUNIT-TRANSACTION").build();

            List<DBRequest> dbRequestList = new ArrayList<>();
            dbRequestList.add(dbRequest);

            Pair<Boolean, List<Pair<URI, Response>>> responsesTuple =
                resourceHttpEntry.process(dbRequestList, "JUNIT");
            response = responsesTuple.getValue1().get(0).getValue1();

        } catch (AAIException e) {
            response =
                this.consumerExceptionResponseGenerator(httpHeaders, uriInfo, HttpMethod.PUT, e);
            success = false;
        } catch (Exception e) {
            AAIException ex = new AAIException("AAI_4000", e);
            response =
                this.consumerExceptionResponseGenerator(httpHeaders, uriInfo, HttpMethod.PUT, ex);
            success = false;
        } finally {
            if (success) {
                if (response != null) {
                    if ((response.getStatus() / 100) == 2) {
                        logger.info(
                            "Successfully completed the DELETE request with status {} and committing it to DB",
                            response.getStatus());
                    } else {
                        logFailure(HttpMethod.DELETE, response);
                    }
                }
                dbEngine.commit();
            } else {
                logFailure(HttpMethod.DELETE, response);
                dbEngine.rollback();
            }
        }

        return response;
    }

    public static void logFailure(HttpMethod httpMethod, Response response) {
        logger.info("Unable to complete the {} request with status {} and rolling back",
            httpMethod.toString(), response.getStatus());
        logger.info("Response body of failed request {}", response.getEntity());

    }
}
