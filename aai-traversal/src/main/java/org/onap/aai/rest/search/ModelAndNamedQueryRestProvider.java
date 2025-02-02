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
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.onap.aai.aailog.logs.AaiDBTraversalMetricLog;
import org.onap.aai.concurrent.AaiCallable;
import org.onap.aai.dbgraphmap.SearchGraph;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.rest.util.AAIExtensionMap;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.setup.SchemaVersions;
import org.onap.aai.util.AAIConstants;
import org.onap.aai.util.TraversalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the search subdomain in the REST API. All API calls must include
 * X-FromAppId and X-TransactionId in the header.
 *
 */
@Path("/search")
public class ModelAndNamedQueryRestProvider extends RESTAPI {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(ModelAndNamedQueryRestProvider.class);

    public static final String NAMED_QUERY = "/named-query";

    public static final String MODEL_QUERY = "/model";

    private SearchGraph searchGraph;

    private SchemaVersions schemaVersions;

    public ModelAndNamedQueryRestProvider(SearchGraph searchGraph, SchemaVersions schemaVersions) {
        this.searchGraph = searchGraph;
        this.schemaVersions = schemaVersions;
    }

    /**
     * Gets the named query response.
     *
     * @param headers the headers
     * @param req the req
     * @param queryParameters the query parameters
     * @return the named query response
     */
    /* ---------------- Start Named Query --------------------- */
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path(NAMED_QUERY)
    public Response getNamedQueryResponse(@Context HttpHeaders headers,
        @Context HttpServletRequest req, String queryParameters, @Context UriInfo info) {
        return runner(TraversalConstants.AAI_TRAVERSAL_TIMEOUT_ENABLED,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_APP,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_LIMIT, headers, info, HttpMethod.GET,
            new AaiCallable<Response>() {
                @Override
                public Response process() {
                    return processNamedQueryResponse(headers, req, queryParameters);
                }
            });
    }

    public Response processNamedQueryResponse(@Context HttpHeaders headers,
        @Context HttpServletRequest req, String queryParameters) {
        AAIException ex = null;
        Response response;
        String fromAppId;
        String transId;

        ArrayList<String> templateVars = new ArrayList<>();
        try {
            fromAppId = getFromAppId(headers);
            transId = getTransId(headers);

            AAIExtensionMap aaiExtMap = new AAIExtensionMap();
            aaiExtMap.setHttpHeaders(headers);
            aaiExtMap.setServletRequest(req);
            aaiExtMap.setApiVersion(schemaVersions.getDefaultVersion().toString());
            // only consider header value for search

            AaiDBTraversalMetricLog metricLog =
                new AaiDBTraversalMetricLog(AAIConstants.AAI_TRAVERSAL_MS);
            String uriString = req.getRequestURI();
            Optional<URI> o;
            if (uriString != null) {
                o = Optional.of(new URI(uriString));
            } else {
                o = Optional.empty();
            }
            metricLog.pre(o);

            response = searchGraph.runNamedQuery(fromAppId, transId, queryParameters, aaiExtMap);
            metricLog.post();

        } catch (AAIException e) {
            // send error response
            ex = e;
            templateVars.add("POST Search");
            templateVars.add("getNamedQueryResponse");
            response = Response
                .status(e.getErrorObject().getHTTPResponseCode()).entity(ErrorLogHelper
                    .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
                .build();
        } catch (Exception e) {
            // send error response
            ex = new AAIException("AAI_4000", e);
            templateVars.add("POST Search");
            templateVars.add("getNamedQueryResponse");
            response = Response
                .status(Status.INTERNAL_SERVER_ERROR).entity(ErrorLogHelper
                    .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), ex, templateVars))
                .build();
        } finally {
            // log success or failure
            if (ex != null) {
                ErrorLogHelper.logException(ex);
            }
        }
        return response;
    }

    /**
     * Gets the model query response.
     *
     * @param headers the headers
     * @param req the req
     * @param inboundPayload the inbound payload
     * @param action the action
     * @return the model query response
     */
    /* ---------------- Start Named Query --------------------- */
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path(MODEL_QUERY)
    public Response getModelQueryResponse(@Context HttpHeaders headers,
        @Context HttpServletRequest req, String inboundPayload, @QueryParam("action") String action,
        @Context UriInfo info) {
        return runner(TraversalConstants.AAI_TRAVERSAL_TIMEOUT_ENABLED,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_APP,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_LIMIT, headers, info, HttpMethod.GET,
            new AaiCallable<Response>() {
                @Override
                public Response process() {
                    return processModelQueryResponse(headers, req, inboundPayload, action);
                }
            });
    }

    public Response processModelQueryResponse(@Context HttpHeaders headers,
        @Context HttpServletRequest req, String inboundPayload,
        @QueryParam("action") String action) {
        AAIException ex = null;
        Response response;
        String fromAppId;
        String transId;
        ArrayList<String> templateVars = new ArrayList<>();
        try {
            fromAppId = getFromAppId(headers);
            transId = getTransId(headers);

            AAIExtensionMap aaiExtMap = new AAIExtensionMap();
            aaiExtMap.setHttpHeaders(headers);
            aaiExtMap.setServletRequest(req);
            aaiExtMap.setApiVersion(schemaVersions.getDefaultVersion().toString());
            aaiExtMap.setFromAppId(fromAppId);
            aaiExtMap.setTransId(transId);

            // only consider header value for search

            AaiDBTraversalMetricLog metricLog =
                new AaiDBTraversalMetricLog(AAIConstants.AAI_TRAVERSAL_MS);
            String uriString = req.getRequestURI();
            Optional<URI> o;
            if (uriString != null) {
                o = Optional.of(new URI(uriString));
            } else {
                o = Optional.empty();
            }
            metricLog.pre(o);
            if (action != null && action.equalsIgnoreCase("DELETE")) {
                response = searchGraph.executeModelOperation(fromAppId, transId, inboundPayload,
                    true, aaiExtMap);
            } else {
                response = searchGraph.executeModelOperation(fromAppId, transId, inboundPayload,
                    false, aaiExtMap);
            }
            metricLog.post();

        } catch (AAIException e) {
            // send error response
            ex = e;
            templateVars.add("POST Search");
            templateVars.add("getModelQueryResponse");
            response = Response
                .status(e.getErrorObject().getHTTPResponseCode()).entity(ErrorLogHelper
                    .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
                .build();
        } catch (Exception e) {
            // send error response
            ex = new AAIException("AAI_4000", e);
            templateVars.add("POST Search");
            templateVars.add("getModelQueryResponse");
            response = Response
                .status(Status.INTERNAL_SERVER_ERROR).entity(ErrorLogHelper
                    .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), ex, templateVars))
                .build();
        } finally {
            // log success or failure
            if (ex != null) {
                ErrorLogHelper.logException(ex);
            }
        }
        return response;
    }

}
