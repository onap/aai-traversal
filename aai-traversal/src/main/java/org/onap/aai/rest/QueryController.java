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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.parsers.query.QueryParser;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.search.CustomQueryConfig;
import org.onap.aai.rest.search.GenericQueryProcessor;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.rest.search.QueryProcessorType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/{version:v[1-9][0-9]*|latest}/query")
public class QueryController extends TraversalConsumer {

    private QueryProcessorType processorType = QueryProcessorType.LOCAL_GROOVY;

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

    private HttpEntry traversalUriHttpEntry;

    private SchemaVersions schemaVersions;

    private String basePath;

    private GremlinServerSingleton gremlinServerSingleton;

    private XmlFormatTransformer xmlFormatTransformer;

    @Autowired
    public QueryController(HttpEntry traversalUriHttpEntry, SchemaVersions schemaVersions,
            GremlinServerSingleton gremlinServerSingleton, XmlFormatTransformer xmlFormatTransformer,
            @Value("${schema.uri.base.path}") String basePath) {
        this.traversalUriHttpEntry = traversalUriHttpEntry;
        this.schemaVersions = schemaVersions;
        this.gremlinServerSingleton = gremlinServerSingleton;
        this.basePath = basePath;
        this.xmlFormatTransformer = xmlFormatTransformer;
    }

    @PutMapping(produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> executeQuery(@RequestBody String content,
            @PathVariable("version") String versionParam,
            @RequestParam(defaultValue = "graphson") String format,
            @RequestParam(defaultValue = "no_op") String subgraph,
            @RequestParam(defaultValue = "-1") String resultIndex,
            @RequestParam(defaultValue = "-1") String resultSize,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) throws FileNotFoundException, UnsupportedEncodingException, AAIException, URISyntaxException {
        Set<String> roles = this.getRoles(request.getUserPrincipal());

        return processExecuteQuery(content, request, versionParam, format, subgraph,
                headers, resultIndex, resultSize, roles);
    }

    public ResponseEntity<String> processExecuteQuery(String content, HttpServletRequest request, String versionParam,
            String queryFormat, String subgraph, HttpHeaders headers, String resultIndex,
            String resultSize, Set<String> roles) throws FileNotFoundException, AAIException, UnsupportedEncodingException, URISyntaxException {

        String sourceOfTruth = headers.getFirst("X-FromAppId");
        String queryProcessor = headers.getFirst("QueryProcessor");
        final MultivaluedMap<String, String> queryParams = toMultivaluedMap(request.getParameterMap());
        QueryProcessorType processorType = this.processorType;
        ResponseEntity<String> response;
        TransactionalGraphEngine dbEngine = null;

        this.checkQueryParams(queryParams);
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
        String serverBase = request.getRequestURL().toString().replaceAll("/(v[0-9]+|latest)/.*", "/");
        traversalUriHttpEntry.setHttpEntryProperties(version, serverBase);
        /*
         * Changes for Pagination
         */

        traversalUriHttpEntry.setPaginationParameters(resultIndex, resultSize);
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
            List<String> missingRequiredQueryParameters = checkForMissingQueryParameters(
                    customQueryConfig.getQueryRequiredProperties(),
                    URITools.getQueryMap(queryURIObj));

            if (!missingRequiredQueryParameters.isEmpty()) {
                throw new AAIException("AAI_3013");
            }

            List<String> invalidQueryParameters = checkForInvalidQueryParameters(
                    customQueryConfig, URITools.getQueryMap(queryURIObj));

            if (!invalidQueryParameters.isEmpty()) {
                throw new AAIException("AAI_3022");
            }

        } else if (queryElement != null) {
            throw new AAIException("AAI_3014");
        }

        GenericQueryProcessor processor;

        if (isHistory(format)) {
            validateHistoryParams(format, queryParams);
        }
        GraphTraversalSource traversalSource = getTraversalSource(dbEngine, format, queryParams, roles);
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
        List<Object> vertices = traversalUriHttpEntry.getPaginatedVertexList(vertTemp);

        DBSerializer serializer = new DBSerializer(version, dbEngine, ModelType.MOXY, sourceOfTruth);
        FormatFactory ff = new FormatFactory(traversalUriHttpEntry.getLoader(), serializer,
                schemaVersions, this.basePath, serverBase);

        MultivaluedMap<String, String> mvm = new MultivaluedHashMap<>();
        mvm.putAll(queryParams);
        if (isHistory(format)) {
            mvm.putSingle("startTs", Long.toString(getStartTime(format, mvm)));
            mvm.putSingle("endTs", Long.toString(getEndTime(mvm)));
        }
        Formatter formatter = ff.get(format, mvm);

        String result = formatter.output(vertices).toString();

        MediaType acceptType = headers.getAccept().stream()
                .filter(Objects::nonNull)
                .filter(header -> !header.equals(MediaType.ALL))
                .findAny()
                .orElse(MediaType.APPLICATION_JSON);

        if (MediaType.APPLICATION_XML.isCompatibleWith(acceptType)) {
            result = xmlFormatTransformer.transform(result);
        }

        if (traversalUriHttpEntry.isPaginated()) {
            return ResponseEntity.ok()
                    .header("total-results", String.valueOf(traversalUriHttpEntry.getTotalVertices()))
                    .header("total-pages", String.valueOf(traversalUriHttpEntry.getTotalPaginationBuckets()))
                    .body(result);
        } 
        return ResponseEntity.ok(result);
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

    private MultivaluedMap<String, String> toMultivaluedMap(Map<String, String[]> map) {
        MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            for (String val : entry.getValue())
                multivaluedMap.add(entry.getKey(), val);
        }

        return multivaluedMap;
    }

}
