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

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.onap.aai.concurrent.AaiCallable;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.parsers.query.QueryParser;
import org.onap.aai.query.builder.Pageable;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.search.CustomQueryConfig;
import org.onap.aai.rest.search.GenericQueryProcessor;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.rest.search.QueryProcessorType;
import org.onap.aai.rest.util.PaginationUtil;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.restcore.util.URITools;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;
import org.onap.aai.serialization.queryformats.FormatFactory;
import org.onap.aai.serialization.queryformats.Formatter;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.onap.aai.transforms.XmlFormatTransformer;
import org.onap.aai.util.TraversalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.micrometer.core.annotation.Timed;

@Timed
@Path("{version: v[1-9][0-9]*|latest}/query")
public class QueryConsumer extends TraversalConsumer {

    private QueryProcessorType processorType = QueryProcessorType.LOCAL_GROOVY;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryConsumer.class);

    private HttpEntry traversalUriHttpEntry;

    private SchemaVersions schemaVersions;

    private String basePath;

    private GremlinServerSingleton gremlinServerSingleton;

    private XmlFormatTransformer xmlFormatTransformer;

    @Autowired
    public QueryConsumer(HttpEntry traversalUriHttpEntry, SchemaVersions schemaVersions,
        GremlinServerSingleton gremlinServerSingleton, XmlFormatTransformer xmlFormatTransformer,
        @Value("${schema.uri.base.path}") String basePath) {
        this.traversalUriHttpEntry = traversalUriHttpEntry;
        this.schemaVersions = schemaVersions;
        this.gremlinServerSingleton = gremlinServerSingleton;
        this.basePath = basePath;
        this.xmlFormatTransformer = xmlFormatTransformer;
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response executeQuery(
        String content,
        @PathParam("version") String versionParam,
        @DefaultValue("graphson") @QueryParam("format") String queryFormat,
        @DefaultValue("no_op") @QueryParam("subgraph") String subgraph,
        @DefaultValue("-1") @QueryParam("resultIndex") int resultIndex,
        @DefaultValue("-1") @QueryParam("resultSize") int resultSize,
        @Context HttpHeaders headers,
        @Context HttpServletRequest req,
        @Context UriInfo info) {
        Set<String> roles = this.getRoles(req.getUserPrincipal());

        return runner(TraversalConstants.AAI_TRAVERSAL_TIMEOUT_ENABLED,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_APP,
            TraversalConstants.AAI_TRAVERSAL_TIMEOUT_LIMIT, headers, info, HttpMethod.GET,
            new AaiCallable<Response>() {
                @Override
                public Response process() {
                    return processExecuteQuery(content, req, versionParam, queryFormat, subgraph,
                        headers, info, new Pageable(resultIndex, resultSize), roles);
                }
            });
    }

    public Response processExecuteQuery(String content, HttpServletRequest req, String versionParam,
        String queryFormat, String subgraph, HttpHeaders headers, UriInfo info, Pageable pageable, Set<String> roles) {

        String sourceOfTruth = headers.getRequestHeaders().getFirst("X-FromAppId");
        String queryProcessor = headers.getRequestHeaders().getFirst("QueryProcessor");
        QueryProcessorType processorType = this.processorType;
        Response response;
        TransactionalGraphEngine dbEngine = null;

        try {
            this.checkQueryParams(info.getQueryParameters());
            Format format = Format.getFormat(queryFormat);
            if (queryProcessor != null) {
                processorType = QueryProcessorType.valueOf(queryProcessor);
            }
            SubGraphStyle subGraphStyle = SubGraphStyle.valueOf(subgraph);

            JsonObject input = JsonParser.parseString(content).getAsJsonObject();
            JsonElement startElement = input.get("start");
            JsonElement queryElement = input.get("query");
            JsonElement gremlinElement = input.get("gremlin");
            List<URI> startURIs = new ArrayList<>();
            String queryURI = "";
            String gremlin = "";

            SchemaVersion version = new SchemaVersion(versionParam);
            String serverBase =
                req.getRequestURL().toString().replaceAll("/(v[0-9]+|latest)/.*", "/");
            traversalUriHttpEntry.setHttpEntryProperties(version, serverBase);
            /*
             * Changes for Pagination
             */

            dbEngine = traversalUriHttpEntry.getDbEngine();

            if (startElement != null) {

                if (startElement.isJsonArray()) {
                    for (JsonElement element : startElement.getAsJsonArray()) {
                        startURIs.add(new URI(element.getAsString()));
                    }
                } else {
                    startURIs.add(new URI(startElement.getAsString()));
                }
            }
            if (queryElement != null) {
                queryURI = queryElement.getAsString();
            }
            if (gremlinElement != null) {
                gremlin = gremlinElement.getAsString();
            }
            URI queryURIObj = new URI(queryURI);

            CustomQueryConfig customQueryConfig = getCustomQueryConfig(queryURIObj);
            if (customQueryConfig != null) {
                List<String> missingRequiredQueryParameters =
                    checkForMissingQueryParameters(customQueryConfig.getQueryRequiredProperties(),
                        URITools.getQueryMap(queryURIObj));

                if (!missingRequiredQueryParameters.isEmpty()) {
                    return (createMessageMissingQueryRequiredParameters(
                        missingRequiredQueryParameters, headers));
                }

                List<String> invalidQueryParameters = checkForInvalidQueryParameters(
                    customQueryConfig, URITools.getQueryMap(queryURIObj));

                if (!invalidQueryParameters.isEmpty()) {
                    return (createMessageInvalidQueryParameters(invalidQueryParameters, headers));
                }

            } else if (queryElement != null) {
                return (createMessageInvalidQuerySection(queryURI, headers));
            }

            GenericQueryProcessor processor;

            if (isHistory(format)) {
                validateHistoryParams(format, info.getQueryParameters());
            }
            GraphTraversalSource traversalSource =
                getTraversalSource(dbEngine, format, info.getQueryParameters(), roles);
            QueryStyle queryStyle = getQueryStyle(format, traversalUriHttpEntry);

            if (!startURIs.isEmpty()) {
                Set<Vertex> vertexSet = new LinkedHashSet<>();
                QueryParser uriQuery;
                List<Vertex> vertices;
                for (URI startUri : startURIs) {
                    uriQuery = dbEngine.getQueryBuilder(queryStyle, traversalSource)
                        .createQueryFromURI(startUri, URITools.getQueryMap(startUri));
                    vertices = uriQuery.getQueryBuilder().toList();
                    vertexSet.addAll(vertices);
                }

                processor = new GenericQueryProcessor.Builder(dbEngine, gremlinServerSingleton)
                    .startFrom(vertexSet).queryFrom(queryURIObj).format(format)
                    .processWith(processorType).traversalSource(isHistory(format), traversalSource)
                    .create();
            } else if (!queryURI.equals("")) {
                processor = new GenericQueryProcessor.Builder(dbEngine, gremlinServerSingleton)
                    .queryFrom(queryURIObj).processWith(processorType)
                    .traversalSource(isHistory(format), traversalSource).create();
            } else {
                processor = new GenericQueryProcessor.Builder(dbEngine, gremlinServerSingleton)
                    .queryFrom(gremlin, "gremlin").processWith(processorType)
                    .traversalSource(isHistory(format), traversalSource).create();
            }
            List<Object> vertTemp = processor.execute(subGraphStyle);
            int fromIndex = pageable.getPage() * pageable.getPageSize();
            List<Object> vertices = PaginationUtil.hasValidPaginationParams(pageable)
                ? vertTemp.subList(fromIndex, fromIndex + pageable.getPageSize())
                : vertTemp;

            DBSerializer serializer =
                new DBSerializer(version, dbEngine, ModelType.MOXY, sourceOfTruth);
            FormatFactory ff = new FormatFactory(traversalUriHttpEntry.getLoader(), serializer,
                schemaVersions, this.basePath, serverBase);

            MultivaluedMap<String, String> mvm = new MultivaluedHashMap<>();
            mvm.putAll(info.getQueryParameters());
            if (isHistory(format)) {
                mvm.putSingle("startTs", Long.toString(getStartTime(format, mvm)));
                mvm.putSingle("endTs", Long.toString(getEndTime(mvm)));
            }
            Formatter formatter = ff.get(format, mvm);

            String result = formatter.output(vertices).toString();

            String acceptType = headers.getHeaderString("Accept");

            if (acceptType == null) {
                acceptType = MediaType.APPLICATION_JSON;
            }

            if (MediaType.APPLICATION_XML_TYPE.isCompatible(MediaType.valueOf(acceptType))) {
                result = xmlFormatTransformer.transform(result);
            }

            if (traversalUriHttpEntry.isPaginated()) {
                response = Response.status(Status.OK).type(acceptType)
                    .header("total-results", traversalUriHttpEntry.getTotalVertices())
                    .header("total-pages", traversalUriHttpEntry.getTotalPaginationBuckets())
                    .entity(result).build();
            } else {
                response = Response.status(Status.OK).type(acceptType).entity(result).build();
            }
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

    public void checkQueryParams(MultivaluedMap<String, String> params) throws AAIException {

        if (params.containsKey("depth") && params.getFirst("depth").matches("\\d+")) {
            String depth = params.getFirst("depth");
            int i = Integer.parseInt(depth);
            if (i > 1) {
                throw new AAIException("AAI_3303");
            }
        }

    }

    private List<String> checkForMissingQueryParameters(List<String> requiredParameters,
        MultivaluedMap<String, String> queryParams) {
        List<String> result = new ArrayList<>();

        for (String param : requiredParameters) {
            if (!queryParams.containsKey(param)) {
                result.add(param);
            }
        }
        return result;
    }

    private CustomQueryConfig getCustomQueryConfig(URI uriObj) {
        String path = uriObj.getPath();

        String[] parts = path.split("/");
        boolean hasQuery = false;
        for (String part : parts) {
            if (hasQuery) {
                return gremlinServerSingleton.getCustomQueryConfig(part);
            }
            if ("query".equals(part)) {
                hasQuery = true;
            }
        }

        return null;

    }

    private Response createMessageMissingQueryRequiredParameters(
        List<String> missingRequiredQueryParams, HttpHeaders headers) {
        AAIException e = new AAIException("AAI_3013");

        ArrayList<String> templateVars = new ArrayList<>();
        templateVars.add(missingRequiredQueryParams.toString());

        return Response
            .status(e.getErrorObject().getHTTPResponseCode()).entity(ErrorLogHelper
                .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
            .build();
    }

    private Response createMessageInvalidQuerySection(String invalidQuery, HttpHeaders headers) {
        AAIException e = new AAIException("AAI_3014");

        ArrayList<String> templateVars = new ArrayList<>();
        templateVars.add(invalidQuery);

        return Response
            .status(e.getErrorObject().getHTTPResponseCode()).entity(ErrorLogHelper
                .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
            .build();
    }

    private List<String> checkForInvalidQueryParameters(CustomQueryConfig customQueryConfig,
        MultivaluedMap<String, String> queryParams) {

        List<String> allParameters = new ArrayList<>();
        /*
         * Add potential Required and Optional to allParameters
         */
        Optional.ofNullable(customQueryConfig.getQueryOptionalProperties())
            .ifPresent(allParameters::addAll);
        Optional.ofNullable(customQueryConfig.getQueryRequiredProperties())
            .ifPresent(allParameters::addAll);

        if (queryParams.isEmpty()) {
            return new ArrayList<>();
        }
        return queryParams.keySet().stream().filter(param -> !allParameters.contains(param))
            .collect(Collectors.toList());
    }

    private Response createMessageInvalidQueryParameters(List<String> invalidQueryParams,
        HttpHeaders headers) {
        AAIException e = new AAIException("AAI_3022");

        ArrayList<String> templateVars = new ArrayList<>();
        templateVars.add(invalidQueryParams.toString());

        return Response
            .status(e.getErrorObject().getHTTPResponseCode()).entity(ErrorLogHelper
                .getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
            .build();

    }

}
