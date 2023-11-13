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
package org.onap.aai.rest.search;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.onap.aai.aailog.logs.AaiDBTraversalMetricLog;
import org.onap.aai.concurrent.AaiCallable;
import org.onap.aai.dbgraphmap.SearchGraph;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.JanusGraphDBEngine;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.utils.UrlBuilder;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.onap.aai.util.AAIConstants;
import org.onap.aai.util.GenericQueryBuilder;
import org.onap.aai.util.NodesQueryBuilder;
import org.onap.aai.util.TraversalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.micrometer.core.annotation.Timed;

/**
 * Implements the search subdomain in the REST API. All API calls must include X-FromAppId and
 * X-TransactionId in the header.
 */
@Path("/{version: v[1-9][0-9]*|latest}/search")
@Timed
public class SearchProvider extends RESTAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchProvider.class);

    public static final String GENERIC_QUERY = "/generic-query";

    public static final String NODES_QUERY = "/nodes-query";

    private SearchGraph searchGraph;

    private LoaderFactory loaderFactory;

    private SchemaVersions schemaVersions;

    private String basePath;

    @Autowired
    public SearchProvider(LoaderFactory loaderFactory, SearchGraph searchGraph,
        SchemaVersions schemaVersions, @Value("${schema.uri.base.path}") String basePath) {
        this.loaderFactory = loaderFactory;
        this.searchGraph = searchGraph;
        this.schemaVersions = schemaVersions;
        this.basePath = basePath;
    }

    /**
     * Gets the generic query response.
     *
     * @param headers the headers
     * @param req the req
     * @param startNodeType the start node type
     * @param startNodeKeyParams the start node key params
     * @param includeNodeTypes the include node types
     * @param depth the depth
     * @return the generic query response
     */
    /* ---------------- Start Generic Query --------------------- */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path(GENERIC_QUERY)
    public Response getGenericQueryResponse(@Context HttpHeaders headers,
        @Context HttpServletRequest req, @QueryParam("start-node-type") final String startNodeType,
        @QueryParam("key") final List<String> startNodeKeyParams,
        @QueryParam("include") final List<String> includeNodeTypes,
        @QueryParam("depth") final int depth, @PathParam("version") String versionParam,
        @Context UriInfo info) {
        return runner(TraversalConstants.AAI_TRAVERSAL_TIMEOUT_ENABLED,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_APP,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_LIMIT, headers, info, HttpMethod.GET,
            new AaiCallable<Response>() {
                @Override
                public Response process() {
                    return processGenericQueryResponse(headers, req, startNodeType,
                        startNodeKeyParams, includeNodeTypes, depth, versionParam);
                }
            });
    }

    public Response processGenericQueryResponse(@Context HttpHeaders headers,
        @Context HttpServletRequest req, @QueryParam("start-node-type") final String startNodeType,
        @QueryParam("key") final List<String> startNodeKeyParams,
        @QueryParam("include") final List<String> includeNodeTypes,
        @QueryParam("depth") final int depth, @PathParam("version") String versionParam) {

        AAIException ex = null;
        Response searchResult;
        String fromAppId;
        ArrayList<String> templateVars = new ArrayList<>();
        try {

            fromAppId = getFromAppId(headers);
            getTransId(headers);
            // only consider header value for search

            final SchemaVersion version = new SchemaVersion(versionParam);

            final ModelType factoryType = ModelType.MOXY;
            Loader loader = loaderFactory.createLoaderForVersion(factoryType, version);
            TransactionalGraphEngine dbEngine =
                new JanusGraphDBEngine(QueryStyle.TRAVERSAL, loader);
            DBSerializer dbSerializer = new DBSerializer(version, dbEngine, factoryType, fromAppId);
            UrlBuilder urlBuilder =
                new UrlBuilder(version, dbSerializer, schemaVersions, this.basePath);

            AaiDBTraversalMetricLog metricLog =
                new AaiDBTraversalMetricLog(AAIConstants.AAI_TRAVERSAL_MS);
            metricLog.pre(Optional.of(new URI(req.getRequestURI())));

            searchResult = searchGraph.runGenericQuery(new GenericQueryBuilder().setHeaders(headers)
                .setStartNodeType(startNodeType).setStartNodeKeyParams(startNodeKeyParams)
                .setIncludeNodeTypes(includeNodeTypes).setDepth(depth).setDbEngine(dbEngine)
                .setLoader(loader).setUrlBuilder(urlBuilder));

            metricLog.post();

        } catch (AAIException e) {
            // send error response
            ex = e;
            templateVars.add("GET Search");
            templateVars.add("getGenericQueryResponse");
            searchResult = Response
                .status(e.getErrorObject().getHTTPResponseCode()).entity(ErrorLogHelper
                    .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
                .build();
        } catch (Exception e) {
            // send error response
            ex = new AAIException("AAI_4000", e);
            templateVars.add("GET Search");
            templateVars.add("getGenericQueryResponse");
            searchResult = Response
                .status(Status.INTERNAL_SERVER_ERROR).entity(ErrorLogHelper
                    .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), ex, templateVars))
                .build();
        } finally {
            // log success or failure
            if (ex != null) {
                ErrorLogHelper.logException(ex);
            }
        }

        return searchResult;
    }

    /* ---------------- End Generic Query --------------------- */

    /**
     * Gets the nodes query response.
     *
     * @param headers the headers
     * @param req the req
     * @param searchNodeType the search node type
     * @param edgeFilterList the edge filter list
     * @param filterList the filter list
     * @return the nodes query response
     */
    /* ---------------- Start Nodes Query --------------------- */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path(NODES_QUERY)
    public Response getNodesQueryResponse(@Context HttpHeaders headers,
        @Context HttpServletRequest req,
        @QueryParam("search-node-type") final String searchNodeType,
        @QueryParam("edge-filter") final List<String> edgeFilterList,
        @QueryParam("filter") final List<String> filterList,
        @PathParam("version") String versionParam, @Context UriInfo info)

    {
        return runner(TraversalConstants.AAI_TRAVERSAL_TIMEOUT_ENABLED,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_APP,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_LIMIT, headers, info, HttpMethod.GET,
            new AaiCallable<Response>() {
                @Override
                public Response process() {
                    return processNodesQueryResponse(headers, req, searchNodeType, edgeFilterList,
                        filterList, versionParam);
                }
            });
    }

    public Response processNodesQueryResponse(@Context HttpHeaders headers,
        @Context HttpServletRequest req,
        @QueryParam("search-node-type") final String searchNodeType,
        @QueryParam("edge-filter") final List<String> edgeFilterList,
        @QueryParam("filter") final List<String> filterList,
        @PathParam("version") String versionParam) {

        AAIException ex = null;
        Response searchResult;
        String fromAppId;
        ArrayList<String> templateVars = new ArrayList<>();
        try {

            fromAppId = getFromAppId(headers);
            getTransId(headers);

            // only consider header value for search

            final SchemaVersion version = new SchemaVersion(versionParam);

            final ModelType factoryType = ModelType.MOXY;
            Loader loader = loaderFactory.createLoaderForVersion(factoryType, version);
            TransactionalGraphEngine dbEngine =
                new JanusGraphDBEngine(QueryStyle.TRAVERSAL, loader);
            DBSerializer dbSerializer = new DBSerializer(version, dbEngine, factoryType, fromAppId);
            UrlBuilder urlBuilder =
                new UrlBuilder(version, dbSerializer, schemaVersions, this.basePath);

            AaiDBTraversalMetricLog metricLog =
                new AaiDBTraversalMetricLog(AAIConstants.AAI_TRAVERSAL_MS);
            metricLog.pre(Optional.of(new URI(req.getRequestURI())));
            searchResult = searchGraph.runNodesQuery(
                new NodesQueryBuilder().setHeaders(headers).setTargetNodeType(searchNodeType)
                    .setEdgeFilterParams(edgeFilterList).setFilterParams(filterList)
                    .setDbEngine(dbEngine).setLoader(loader).setUrlBuilder(urlBuilder));

            metricLog.post();

        } catch (AAIException e) {
            // send error response
            ex = e;
            templateVars.add("GET Search");
            templateVars.add("getNodesQueryResponse");
            searchResult = Response
                .status(e.getErrorObject().getHTTPResponseCode()).entity(ErrorLogHelper
                    .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
                .build();
        } catch (Exception e) {
            // send error response
            ex = new AAIException("AAI_4000", e);
            templateVars.add("GET Search");
            templateVars.add("getNodesQueryResponse");
            searchResult = Response
                .status(Status.INTERNAL_SERVER_ERROR).entity(ErrorLogHelper
                    .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), ex, templateVars))
                .build();
        } finally {
            // log success or failure
            if (ex != null) {
                ErrorLogHelper.logException(ex);
            }
        }
        return searchResult;
    }

}
