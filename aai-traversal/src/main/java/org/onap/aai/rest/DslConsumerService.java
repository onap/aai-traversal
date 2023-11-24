/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2023 Deutsche Telekom AG. All rights reserved.
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.SchemaViolationException;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.LoaderFactory;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class DslConsumerService extends TraversalConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DslConsumerService.class);
    private static final QueryProcessorType processorType = QueryProcessorType.LOCAL_GROOVY;
    private static final QueryVersion DEFAULT_VERSION = QueryVersion.V1;
    
    private final HttpEntry traversalUriHttpEntry;
    private final SchemaVersions schemaVersions;
    private final GremlinServerSingleton gremlinServerSingleton;
    private final EdgeIngestor edgeIngestor;
    private final LoaderFactory loaderFactory;
    private final String basePath;

    public DslConsumerService(HttpEntry traversalUriHttpEntry,
            SchemaVersions schemaVersions, GremlinServerSingleton gremlinServerSingleton,
            @Value("${schema.uri.base.path}") String basePath,
            EdgeIngestor edgeIngestor, LoaderFactory loaderFactory) {
        this.traversalUriHttpEntry = traversalUriHttpEntry;
        this.schemaVersions = schemaVersions;
        this.gremlinServerSingleton = gremlinServerSingleton;
        this.edgeIngestor = edgeIngestor;
        this.loaderFactory = loaderFactory;
        this.basePath = basePath;
    }

    public String executeQuery(String content, HttpServletRequest req, String queryFormat, String subgraph,
            String validate, MultivaluedMap<String, String> queryParameters, String resultIndex, String resultSize, Set<String> roles,
            final SchemaVersion version, final String sourceOfTruth, final String dslOverride, QueryVersion dslApiVersion)
            throws AAIException, FileNotFoundException {
        final String serverBase =
            req.getRequestURL().toString().replaceAll("/(v[0-9]+|latest)/.*", "/");
        traversalUriHttpEntry.setHttpEntryProperties(version, serverBase);
        traversalUriHttpEntry.setPaginationParameters(resultIndex, resultSize);

        JsonObject input = JsonParser.parseString(content).getAsJsonObject();
        JsonElement dslElement = input.get("dsl");
        String dsl = "";
        if (dslElement != null) {
            dsl = dslElement.getAsString();
        }

        boolean isDslOverride = dslOverride != null
                && !AAIConfig.get(TraversalConstants.DSL_OVERRIDE).equals("false")
                && dslOverride.equals(AAIConfig.get(TraversalConstants.DSL_OVERRIDE));

        Map<QueryVersion, ParseTreeListener> dslListeners = new HashMap<>();
        dslListeners.put(QueryVersion.V1,
            new org.onap.aai.rest.dsl.v1.DslListener(edgeIngestor, schemaVersions, loaderFactory));
        dslListeners.put(QueryVersion.V2,
            new org.onap.aai.rest.dsl.v2.DslListener(edgeIngestor, schemaVersions, loaderFactory));
        DslQueryProcessor dslQueryProcessor = new DslQueryProcessor(dslListeners);
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

        final TransactionalGraphEngine dbEngine = traversalUriHttpEntry.getDbEngine();
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
            List<Object> vertTempDedupedObjectList = dedupObjectsInAggregateFormat(vertTemp);
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

    private List<Object> dedupObjectsInAggregateFormat(List<Object> vertTemp) {
        return vertTemp.stream()
            .filter(o -> o instanceof ArrayList)
            .map(o -> ((ArrayList<?>) o).stream().distinct().collect(Collectors.toList()))
            .collect(Collectors.toList());
    }
}
