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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.dsl.DslQueryProcessor;
import org.onap.aai.rest.dsl.V1DslQueryProcessor;
import org.onap.aai.rest.dsl.V2DslQueryProcessor;
import org.onap.aai.rest.dsl.v1.DslListener;
import org.onap.aai.rest.enums.QueryVersion;
import org.onap.aai.rest.search.GenericQueryProcessor;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.rest.search.QueryProcessorType;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.micrometer.core.annotation.Timed;

@Timed
@RestController
@RequestMapping("/{version:v[1-9][0-9]*|latest}/dsl")
public class DslConsumer extends TraversalConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DslConsumer.class);
    private static final QueryProcessorType processorType = QueryProcessorType.LOCAL_GROOVY;
    private static final QueryVersion DEFAULT_VERSION = QueryVersion.V1;

    private final HttpEntry httpEntry;
    private final SchemaVersions schemaVersions;
    private final String basePath;
    private final GremlinServerSingleton gremlinServerSingleton;
    private final XmlFormatTransformer xmlFormatTransformer;
    private final DslListener v1DslListener;
    private final org.onap.aai.rest.dsl.v2.DslListener v2DslListener;

    private QueryVersion dslApiVersion = DEFAULT_VERSION;
    Map<QueryVersion, DslQueryProcessor> dslQueryProcessors;

    @Autowired
    public DslConsumer(@Qualifier("requestScopedTraversalUriHttpEntry") HttpEntry requestScopedTraversalUriHttpEntry,
            SchemaVersions schemaVersions, GremlinServerSingleton gremlinServerSingleton,
            XmlFormatTransformer xmlFormatTransformer,
            @Value("${schema.uri.base.path}") String basePath, DslListener v1DslListener, org.onap.aai.rest.dsl.v2.DslListener v2DslListener) {
        this.httpEntry = requestScopedTraversalUriHttpEntry;
        this.schemaVersions = schemaVersions;
        this.gremlinServerSingleton = gremlinServerSingleton;
        this.xmlFormatTransformer = xmlFormatTransformer;
        this.basePath = basePath;
        this.v1DslListener = v1DslListener;
        this.v2DslListener = v2DslListener;
    }

    @PutMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> executeQuery(@RequestBody String dslQuery,
                                               @PathVariable("version") String versionParam,
                                               @RequestParam(defaultValue = "graphson") String format,
                                               @RequestParam(defaultValue = "no_op") String subgraph,
                                               @RequestParam(defaultValue = "all") String validate,
                                               @RequestParam(defaultValue = "-1") String resultIndex,
                                               @RequestParam(defaultValue = "-1") String resultSize,
                                               @RequestHeader HttpHeaders headers,
                                               HttpServletRequest request) throws FileNotFoundException, AAIException {
        Set<String> roles = this.getRoles(request.getUserPrincipal());

        return processExecuteQuery(dslQuery, request, versionParam, format, subgraph,
                validate, headers, resultIndex, resultSize, roles);
    }

    public ResponseEntity<String> processExecuteQuery(String dslQuery, HttpServletRequest request, String versionParam,
            String queryFormat, String subgraph, String validate, HttpHeaders headers,
            String resultIndex, String resultSize, Set<String> roles) throws FileNotFoundException, AAIException {

        final SchemaVersion version = new SchemaVersion(versionParam);
        final String sourceOfTruth = headers.getFirst("X-FromAppId");
        final String dslOverride = headers.getFirst("X-DslOverride");
        final MultivaluedMap<String,String> queryParams = toMultivaluedMap(request.getParameterMap());

        Optional<String> dslApiVersionHeader =
            Optional.ofNullable(headers.getFirst("X-DslApiVersion"));
        if (dslApiVersionHeader.isPresent()) {
            try {
                dslApiVersion = QueryVersion.valueOf(dslApiVersionHeader.get());
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Defaulting DSL Api Version to  " + DEFAULT_VERSION);
            }
        }

        String result = executeQuery(dslQuery, request, queryFormat, subgraph, validate, queryParams, resultIndex, resultSize,
                roles, version, sourceOfTruth, dslOverride);
        MediaType acceptType = headers.getAccept().stream()
            .filter(Objects::nonNull)
            .filter(header -> !header.equals(MediaType.ALL))
            .findAny()
            .orElse(MediaType.APPLICATION_JSON);

        if (MediaType.APPLICATION_XML.isCompatibleWith(acceptType)) {
            result = xmlFormatTransformer.transform(result);
        }

        if (httpEntry.isPaginated()) {
            return ResponseEntity.ok()
                .header("total-results", String.valueOf(httpEntry.getTotalVertices()))
                .header("total-pages", String.valueOf(httpEntry.getTotalPaginationBuckets()))
                .body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }

    private String executeQuery(String content, HttpServletRequest req, String queryFormat, String subgraph,
            String validate, MultivaluedMap<String, String> queryParameters, String resultIndex, String resultSize, Set<String> roles,
            final SchemaVersion version, final String sourceOfTruth, final String dslOverride)
            throws AAIException, FileNotFoundException {
        final String serverBase =
            req.getRequestURL().toString().replaceAll("/(v[0-9]+|latest)/.*", "/");
        httpEntry.setHttpEntryProperties(version, serverBase);
        httpEntry.setPaginationParameters(resultIndex, resultSize);

        JsonObject input = JsonParser.parseString(content).getAsJsonObject();
        JsonElement dslElement = input.get("dsl");
        String dsl = "";
        if (dslElement != null) {
            dsl = dslElement.getAsString();
        }

        boolean isDslOverride = dslOverride != null
                && !AAIConfig.get(TraversalConstants.DSL_OVERRIDE).equals("false")
                && dslOverride.equals(AAIConfig.get(TraversalConstants.DSL_OVERRIDE));

        DslQueryProcessor dslQueryProcessor = dslApiVersion.equals(QueryVersion.V1)
            ? new V1DslQueryProcessor()
            : new V2DslQueryProcessor();
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

        final TransactionalGraphEngine dbEngine = httpEntry.getDbEngine();
        GraphTraversalSource traversalSource =
            getTraversalSource(dbEngine, format, queryParameters, roles);
        
        GenericQueryProcessor processor =
            new GenericQueryProcessor.Builder(dbEngine, gremlinServerSingleton)
                .queryFrom(dsl, "dsl").queryProcessor(dslQueryProcessor).version(dslApiVersion)
                .processWith(processorType).format(format).uriParams(queryParameters)
                .traversalSource(isHistory(format), traversalSource).create();

        SubGraphStyle subGraphStyle = SubGraphStyle.valueOf(subgraph);
        List<Object> vertTemp = processor.execute(subGraphStyle);

        List<Object> vertices;
        if (isAggregate(format)) {
            // Dedup if duplicate objects are returned in each array in the aggregate format
            // scenario.
            List<Object> vertTempDedupedObjectList = dedupObjectInAggregateFormatResult(vertTemp);
            vertices = httpEntry
                    .getPaginatedVertexListForAggregateFormat(vertTempDedupedObjectList);
        } else {
            vertices = httpEntry.getPaginatedVertexList(vertTemp);
        }

        DBSerializer serializer =
            new DBSerializer(version, dbEngine, ModelType.MOXY, sourceOfTruth);
        FormatFactory ff = new FormatFactory(httpEntry.getLoader(), serializer,
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

    private List<Object> dedupObjectInAggregateFormatResultStreams(List<Object> vertTemp) {
        return vertTemp.stream()
            .filter(o -> o instanceof ArrayList)
            .map(o -> ((ArrayList<?>) o).stream().distinct().collect(Collectors.toList()))
            .collect(Collectors.toList());
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

    private MultivaluedMap<String, String> toMultivaluedMap(Map<String, String[]> map) {
        MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            for (String val : entry.getValue())
            multivaluedMap.add(entry.getKey(), val);
        }

        return multivaluedMap;
    }
}
