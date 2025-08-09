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
package org.onap.aai.rest;

import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.onap.aai.concurrent.AaiCallable;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.introspection.exceptions.AAIUnknownObjectException;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.search.GenericQueryProcessor;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.rest.search.QueryProcessorType;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;
import org.onap.aai.serialization.queryformats.FormatFactory;
import org.onap.aai.serialization.queryformats.Formatter;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.onap.aai.transforms.XmlFormatTransformer;
import org.onap.aai.util.AAIConstants;
import org.onap.aai.util.TraversalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("/recents/{version: v[1-9][0-9]*|latest}")
@Timed
@Tag(name = "Recent Data Queries", description = "Retrieve recently updated nodes within a specified time range.")
public class RecentAPIConsumer extends RESTAPI {

    private static final String AAI_3021 = "AAI_3021";

    /** The introspector factory type. */
    private ModelType introspectorFactoryType = ModelType.MOXY;

    private QueryProcessorType processorType = QueryProcessorType.LOCAL_GROOVY;
    /** The query style. */

    private static final Logger LOGGER = LoggerFactory.getLogger(RecentAPIConsumer.class);

    private HttpEntry traversalUriHttpEntry;

    private SchemaVersions schemaVersions;

    private String basePath;

    private GremlinServerSingleton gremlinServerSingleton;

    private XmlFormatTransformer xmlFormatTransformer;

    @Autowired
    public RecentAPIConsumer(HttpEntry traversalUriHttpEntry, SchemaVersions schemaVersions,
            GremlinServerSingleton gremlinServerSingleton, XmlFormatTransformer xmlFormatTransformer,
            @Value("${schema.uri.base.path}") String basePath) {
        this.traversalUriHttpEntry = traversalUriHttpEntry;
        this.schemaVersions = schemaVersions;
        this.gremlinServerSingleton = gremlinServerSingleton;
        this.xmlFormatTransformer = xmlFormatTransformer;
        this.basePath = basePath;
    }

    @GET
    @Path("/{nodeType: .+}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Operation(summary = "Get recent data for a given node type", description = "Fetches nodes updated within the given hours or date-time for the specified node type and schema version.", parameters = {
            @Parameter(name = "version", description = "Schema version or 'latest'", required = true),
            @Parameter(name = "nodeType", description = "Type of the node to query", required = true),
            @Parameter(name = "hours", description = "Time range in hours", required = false),
            @Parameter(name = "date-time", description = "Epoch milliseconds for start time", required = false)
    }, responses = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of recent data"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
            @ApiResponse(responseCode = "404", description = "Node type not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getRecentData(@PathParam("version") String versionParam,
            @PathParam("nodeType") String nodeType, @Context HttpHeaders headers,
            @Context HttpServletRequest req, @Context UriInfo info) {

        return runner(TraversalConstants.AAI_TRAVERSAL_TIMEOUT_ENABLED,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_APP,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_LIMIT, headers, info, HttpMethod.GET,
            new AaiCallable<Response>() {
                @Override
                public Response process() {
                    return processRecentData(req, versionParam, nodeType, info, headers);
                }
            });

    }

    public Response processRecentData(HttpServletRequest req,
        @PathParam("version") String versionParam, @PathParam("nodeType") String nodeType,
        @Context UriInfo info, @Context HttpHeaders headers) {

        String sourceOfTruth = headers.getRequestHeaders().getFirst("X-FromAppId");
        String queryProcessor = headers.getRequestHeaders().getFirst("QueryProcessor");
        QueryProcessorType processorType = this.processorType;
        Response response;
        TransactionalGraphEngine dbEngine = null;
        try {

            if (queryProcessor != null) {
                processorType = QueryProcessorType.valueOf(queryProcessor);
            }

            SchemaVersion version = new SchemaVersion(versionParam);
            this.checkVersion(version);

            String serverBase =
                req.getRequestURL().toString().replaceAll("/(v[0-9]+|latest)/.*", "/");
            traversalUriHttpEntry.setHttpEntryProperties(version, serverBase);
            dbEngine = traversalUriHttpEntry.getDbEngine();

            /*
             * Check for mandatory parameters here
             */

            this.checkNodeType(nodeType);
            this.checkQueryParams(info.getQueryParameters());

            GenericQueryProcessor processor = null;

            processor = new GenericQueryProcessor.Builder(dbEngine, gremlinServerSingleton)
                .queryFrom(nodeType, "nodeQuery").uriParams(info.getQueryParameters())
                .processWith(processorType).create();

            String result = "";
            SubGraphStyle subGraphStyle = null;
            List<Object> vertices = processor.execute(subGraphStyle);

            DBSerializer serializer =
                new DBSerializer(version, dbEngine, introspectorFactoryType, sourceOfTruth);
            FormatFactory ff = new FormatFactory(traversalUriHttpEntry.getLoader(), serializer,
                schemaVersions, this.basePath, serverBase);
            Format format = Format.pathed_resourceversion;

            Formatter formater = ff.get(format, info.getQueryParameters());

            result = formater.output(vertices).toString();

            // LOGGER.info("Completed");

            String acceptType = headers.getHeaderString("Accept");

            if (acceptType == null) {
                acceptType = MediaType.APPLICATION_JSON;
            }

            if (MediaType.APPLICATION_XML_TYPE.isCompatible(MediaType.valueOf(acceptType))) {
                result = xmlFormatTransformer.transform(result);
            }

            response = Response.status(Status.OK).type(acceptType).entity(result).build();

        } catch (AAIException e) {
            response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET, e);
        } catch (Exception e) {
            AAIException ex = new AAIException("AAI_4000", e);
            response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET, ex);
        } finally {

            if (dbEngine != null) {
                dbEngine.rollback();
            }

        }

        return response;
    }

    private void checkVersion(SchemaVersion version) throws AAIException {
        if (!schemaVersions.getVersions().contains(version)) {
            throw new AAIException(AAI_3021, "Schema Version is not valid");
        }
    }

    public void checkNodeType(String nodeType) throws AAIException {
        try {
            traversalUriHttpEntry.getLoader().introspectorFromName(nodeType);
        } catch (AAIUnknownObjectException e) {
            throw new AAIException("AAI_6115",
                "Unrecognized nodeType [" + nodeType + "] passed to recents query.");
        }
    }

    public void checkQueryParams(MultivaluedMap<String, String> params) throws AAIException {

        boolean isHoursParameter = false;
        boolean isDateTimeParameter = false;

        if (params != null && params.containsKey("hours")
            && params.getFirst("hours").matches("-?\\d+")) {
            isHoursParameter = true;
            long hours;
            try {
                hours = Long.parseLong(params.getFirst("hours"));
            } catch (NumberFormatException ex) {
                throw new AAIException(AAI_3021, " Invalid Hours. Valid values for hours are 1 to "
                    + AAIConstants.HISTORY_MAX_HOURS);
            }
            if (hours < 1 || hours > AAIConstants.HISTORY_MAX_HOURS) {
                throw new AAIException(AAI_3021,
                    " Valid values for hours are 1 to " + AAIConstants.HISTORY_MAX_HOURS);
            }
        }
        if (params != null && params.containsKey("date-time")
            && params.getFirst("date-time").matches("-?\\d+")) {
            isDateTimeParameter = true;
            Long minStartTime = System.currentTimeMillis()
                - TimeUnit.HOURS.toMillis(AAIConstants.HISTORY_MAX_HOURS);
            Long startTime;
            try {
                startTime = Long.parseLong(params.getFirst("date-time"));
            } catch (NumberFormatException ex) {
                throw new AAIException(AAI_3021,
                    " Invalid Data-time. Valid values for date-time are " + minStartTime + " to "
                        + System.currentTimeMillis());
            }
            if (startTime < minStartTime) {
                throw new AAIException(AAI_3021, " Valid values for date-time are " + minStartTime
                    + " to " + System.currentTimeMillis());
            }
        }

        if (!isHoursParameter && !isDateTimeParameter) {
            throw new AAIException(AAI_3021,
                "Send valid hours or date-time to specify the timebounds");
        }

        if (isHoursParameter && isDateTimeParameter) {
            throw new AAIException(AAI_3021,
                "Send either hours or date-time and not both to specify the timebounds");
        }

    }

}
