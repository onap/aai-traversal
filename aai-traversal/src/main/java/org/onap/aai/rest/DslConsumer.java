/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Deutsche Telekom SA.
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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.janusgraph.core.SchemaViolationException;
import org.onap.aai.concurrent.AaiCallable;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.dsl.DslQueryProcessor;
import org.onap.aai.rest.enums.QueryVersion;
import org.onap.aai.rest.search.GenericQueryProcessor;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.rest.search.QueryProcessorType;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;
import org.onap.aai.serialization.queryformats.FormatFactory;
import org.onap.aai.serialization.queryformats.Formatter;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.onap.aai.transforms.XmlFormatTransformer;
import org.onap.aai.util.AAIConfig;
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
@Path("{version: v[1-9][0-9]*|latest}/dsl")
public class DslConsumer extends TraversalConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DslConsumer.class);
    private static final QueryProcessorType processorType = QueryProcessorType.LOCAL_GROOVY;
    private static final QueryVersion DEFAULT_VERSION = QueryVersion.V1;

    private final HttpEntry traversalUriHttpEntry;
    private final DslQueryProcessor dslQueryProcessor;
    private final SchemaVersions schemaVersions;
    private final String basePath;
    private final GremlinServerSingleton gremlinServerSingleton;
    private final XmlFormatTransformer xmlFormatTransformer;

    private QueryVersion dslApiVersion = DEFAULT_VERSION;

    @Autowired
    public DslConsumer(HttpEntry traversalUriHttpEntry, DslQueryProcessor dslQueryProcessor,
            SchemaVersions schemaVersions, GremlinServerSingleton gremlinServerSingleton,
            XmlFormatTransformer xmlFormatTransformer,
            @Value("${schema.uri.base.path}") String basePath) {
        this.traversalUriHttpEntry = traversalUriHttpEntry;
        this.dslQueryProcessor = dslQueryProcessor;
        this.schemaVersions = schemaVersions;
        this.gremlinServerSingleton = gremlinServerSingleton;
        this.xmlFormatTransformer = xmlFormatTransformer;
        this.basePath = basePath;
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response executeQuery(String dslQuery, @PathParam("version") String versionParam,
            @DefaultValue("graphson") @QueryParam("format") String queryFormat,
            @DefaultValue("no_op") @QueryParam("subgraph") String subgraph,
            @DefaultValue("all") @QueryParam("validate") String validate,
            @DefaultValue("-1") @QueryParam("resultIndex") String resultIndex,
            @DefaultValue("-1") @QueryParam("resultSize") String resultSize,
            @Context HttpHeaders headers,
            @Context HttpServletRequest req,
            @Context UriInfo info) throws FileNotFoundException, AAIException {
        Set<String> roles = this.getRoles(req.getUserPrincipal());

        return processExecuteQuery(dslQuery, req, versionParam, queryFormat, subgraph,
                validate, headers, info, resultIndex, resultSize, roles);
    }

    public Response processExecuteQuery(String dslQuery, HttpServletRequest request, String versionParam,
            String queryFormat, String subgraph, String validate, HttpHeaders headers, UriInfo info,
            String resultIndex, String resultSize, Set<String> roles) throws FileNotFoundException, AAIException {

        final SchemaVersion version = new SchemaVersion(versionParam);
        final String sourceOfTruth = headers.getRequestHeaders().getFirst("X-FromAppId");
        final String dslOverride = headers.getRequestHeaders().getFirst("X-DslOverride");

        Optional<String> dslApiVersionHeader =
            Optional.ofNullable(headers.getRequestHeaders().getFirst("X-DslApiVersion"));
        if (dslApiVersionHeader.isPresent()) {
            try {
                dslApiVersion = QueryVersion.valueOf(dslApiVersionHeader.get());
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Defaulting DSL Api Version to  " + DEFAULT_VERSION);
            }
        }

        String result = executeQuery(dslQuery, request, queryFormat, subgraph, validate, info.getQueryParameters(), resultIndex, resultSize,
                roles, version, sourceOfTruth, dslOverride);

        String acceptType = headers.getHeaderString("Accept");
        if (acceptType == null) {
            acceptType = MediaType.APPLICATION_JSON;
        }

        if (MediaType.APPLICATION_XML_TYPE.isCompatible(MediaType.valueOf(acceptType))) {
            result = xmlFormatTransformer.transform(result);
        }

        if (traversalUriHttpEntry.isPaginated()) {
            return Response.status(Status.OK).type(acceptType)
                    .header("total-results", traversalUriHttpEntry.getTotalVertices())
                    .header("total-pages", traversalUriHttpEntry.getTotalPaginationBuckets())
                    .entity(result).build();
        } else {
            return Response.status(Status.OK).type(acceptType).entity(result).build();
        }
    }

    private String executeQuery(String content, HttpServletRequest req, String queryFormat, String subgraph,
            String validate, MultivaluedMap<String, String> queryParameters, String resultIndex, String resultSize, Set<String> roles,
            final SchemaVersion version, final String sourceOfTruth, final String dslOverride)
            throws AAIException, FileNotFoundException {
        final String serverBase =
            req.getRequestURL().toString().replaceAll("/(v[0-9]+|latest)/.*", "/");
        traversalUriHttpEntry.setHttpEntryProperties(version, serverBase);
        traversalUriHttpEntry.setPaginationParameters(resultIndex, resultSize);
        final TransactionalGraphEngine dbEngine = traversalUriHttpEntry.getDbEngine();

        JsonObject input = JsonParser.parseString(content).getAsJsonObject();
        JsonElement dslElement = input.get("dsl");
        String dsl = "";
        if (dslElement != null) {
            dsl = dslElement.getAsString();
        }

        boolean isDslOverride = dslOverride != null
                && !AAIConfig.get(TraversalConstants.DSL_OVERRIDE).equals("false")
                && dslOverride.equals(AAIConfig.get(TraversalConstants.DSL_OVERRIDE));

        if (isDslOverride) {
            dslQueryProcessor.setStartNodeValidationFlag(false);
        }

        dslQueryProcessor.setValidationRules(validate);

        Format format = Format.getFormat(queryFormat);

        if (isAggregate(format)) {
            dslQueryProcessor.setAggregate(true);
        }

        if (isHistory(format)) {
            validateHistoryParams(format, queryParameters);
        }

        GraphTraversalSource traversalSource =
            getTraversalSource(dbEngine, format, queryParameters, roles);

        GenericQueryProcessor processor =
            new GenericQueryProcessor.Builder(dbEngine, gremlinServerSingleton)
                .queryFrom(dsl, "dsl").queryProcessor(dslQueryProcessor).version(dslApiVersion)
                .processWith(processorType).format(format).uriParams(queryParameters)
                .traversalSource(isHistory(format), traversalSource).create();

        SubGraphStyle subGraphStyle = SubGraphStyle.valueOf(subgraph);
        List<Object> vertTemp = processor.execute(subGraphStyle);

        // Dedup if duplicate objects are returned in each array in the aggregate format
        // scenario.
        List<Object> vertTempDedupedObjectList = dedupObjectInAggregateFormatResult(vertTemp);

        List<Object> vertices;
        if (isAggregate(format)) {
            vertices = traversalUriHttpEntry
                    .getPaginatedVertexListForAggregateFormat(vertTempDedupedObjectList);
        } else {
            vertices = traversalUriHttpEntry.getPaginatedVertexList(vertTemp);
        }

        DBSerializer serializer =
            new DBSerializer(version, dbEngine, ModelType.MOXY, sourceOfTruth);
        FormatFactory ff = new FormatFactory(traversalUriHttpEntry.getLoader(), serializer,
                schemaVersions, this.basePath, serverBase);

        MultivaluedMap<String, String> mvm = new MultivaluedHashMap<>();
        mvm.putAll(queryParameters);
        if (isHistory(format)) {
            mvm.putSingle("startTs", Long.toString(getStartTime(format, mvm)));
            mvm.putSingle("endTs", Long.toString(getEndTime(mvm)));
        }
        Formatter formatter = ff.get(format, mvm);

        final Map<String, List<String>> propertiesMap = processor.getPropertiesMap();
        String result = "";
        if (propertiesMap != null && !propertiesMap.isEmpty()) {
            result = formatter.output(vertices, propertiesMap).toString();
        } else {
            result = formatter.output(vertices).toString();
        }
        return result;
    }

    private List<Object> dedupObjectInAggregateFormatResult(List<Object> vertTemp) {
        List<Object> vertTempDedupedObjectList = new ArrayList<Object>();
        Iterator<Object> itr = vertTemp.listIterator();
        while (itr.hasNext()) {
            Object o = itr.next();
            if (o instanceof ArrayList) {
                vertTempDedupedObjectList
                        .add(((ArrayList) o).stream().distinct().collect(Collectors.toList()));
            }
        }
        return vertTempDedupedObjectList;
    }
}
