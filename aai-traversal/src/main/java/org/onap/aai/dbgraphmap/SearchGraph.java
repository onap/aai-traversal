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

import com.google.common.base.CaseFormat;
import com.google.common.collect.Multimap;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.exceptions.DynamicException;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.onap.aai.db.DbMethHelper;
import org.onap.aai.db.props.AAIProperties;
import org.onap.aai.dbgen.PropertyLimitDesc;
import org.onap.aai.dbgraphgen.ModelBasedProcessing;
import org.onap.aai.dbgraphgen.ResultSet;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.EdgeRule;
import org.onap.aai.edges.EdgeRuleQuery;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.*;
import org.onap.aai.introspection.exceptions.AAIUnknownObjectException;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.parsers.relationship.RelationshipToURI;
import org.onap.aai.query.builder.QueryBuilder;
import org.onap.aai.rest.util.AAIExtensionMap;
import org.onap.aai.schema.enums.ObjectMetadata;
import org.onap.aai.schema.enums.PropertyMetadata;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.JanusGraphDBEngine;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.exceptions.AAIFormatVertexException;
import org.onap.aai.serialization.queryformats.utils.UrlBuilder;
import org.onap.aai.setup.SchemaVersions;
import org.onap.aai.util.GenericQueryBuilder;
import org.onap.aai.util.NodesQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database Mapping class which acts as the middle man between the REST interface objects for the
 * Search namespace
 * 
 */
public class SearchGraph {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchGraph.class);

    private LoaderFactory loaderFactory;

    private EdgeIngestor edgeIngestor;

    private SchemaVersions schemaVersions;

    public SearchGraph(LoaderFactory loaderFactory, EdgeIngestor edgeIngestor,
        SchemaVersions schemaVersions) {
        this.loaderFactory = loaderFactory;
        this.edgeIngestor = edgeIngestor;
        this.schemaVersions = schemaVersions;
    }

    /**
     * Get the search result based on the includeNodeType and depth provided.
     *
     * @param genericQueryBuilder
     */
    public Response runGenericQuery(GenericQueryBuilder genericQueryBuilder) throws AAIException {
        Response response = null;
        boolean success = true;
        String result = "";
        try {
            genericQueryBuilder.getDbEngine().startTransaction();

            if (genericQueryBuilder.getStartNodeType() == null) {
                throw new AAIException("AAI_6120",
                    "null start-node-type passed to the generic query");
            }

            if (genericQueryBuilder.getStartNodeKeyParams() == null) {
                throw new AAIException("AAI_6120", "no key param passed to the generic query");
            }

            if (genericQueryBuilder.getIncludeNodeTypes() == null) {
                throw new AAIException("AAI_6120", "no include params passed to the generic query");
            }

            if (genericQueryBuilder.getDepth() > 6) {
                throw new AAIException("AAI_6120",
                    "The maximum depth supported by the generic query is 6");
            }
            final QueryBuilder queryBuilder;

            // there is an issue with service-instance - it is a unique node but still dependent
            // for now query it directly without attempting to craft a valid URI
            if (genericQueryBuilder.getStartNodeType().equalsIgnoreCase("service-instance")
                && genericQueryBuilder.getStartNodeKeyParams().size() == 1) {
                genericQueryBuilder.getLoader()
                    .introspectorFromName(genericQueryBuilder.getStartNodeType());
                // Build a hash with keys to uniquely identify the start Node
                String keyName;
                String keyValue;

                QueryBuilder builder = genericQueryBuilder.getDbEngine().getQueryBuilder()
                    .getVerticesByIndexedProperty(AAIProperties.NODE_TYPE, "service-instance");
                for (String keyData : genericQueryBuilder.getStartNodeKeyParams()) {
                    int colonIndex = keyData.indexOf(':');
                    if (colonIndex <= 0) {
                        throw new AAIException("AAI_6120",
                            "Bad key param passed in: [" + keyData + "]");
                    } else {
                        keyName = keyData.substring(0, colonIndex).split("\\.")[1];
                        keyValue = keyData.substring(colonIndex + 1);
                        builder.getVerticesByProperty(keyName, keyValue);
                    }
                }

                queryBuilder = builder;
            } else {
                URI uri = craftUriFromQueryParams(genericQueryBuilder.getLoader(),
                    genericQueryBuilder.getStartNodeType(),
                    genericQueryBuilder.getStartNodeKeyParams());
                queryBuilder = genericQueryBuilder.getDbEngine().getQueryBuilder()
                    .createQueryFromURI(uri).getQueryBuilder();
            }
            List<Vertex> results = queryBuilder.toList();
            if (results.isEmpty()) {
                String errorMessage = String.format("No Node of type %s ",
                    genericQueryBuilder.getStartNodeType(), " found for properties: %s",
                    genericQueryBuilder.getStartNodeKeyParams().toString());
                throw new AAIException("AAI_6114", errorMessage);
            } else if (results.size() > 1) {
                String detail = "More than one Node found by getUniqueNode for params: "
                    + genericQueryBuilder.getStartNodeKeyParams().toString() + "\n";
                throw new AAIException("AAI_6112", detail);
            }

            Vertex startNode = results.get(0);

            Collection<Vertex> ver = new HashSet<>();
            List<Vertex> queryResults;
            GraphTraversalSource traversalSource =
                genericQueryBuilder.getDbEngine().asAdmin().getReadOnlyTraversalSource();
            GraphTraversal<Vertex, Vertex> traversal;
            if (genericQueryBuilder.getIncludeNodeTypes()
                .contains(genericQueryBuilder.getStartNodeType())
                || genericQueryBuilder.getDepth() == 0
                || genericQueryBuilder.getIncludeNodeTypes().contains("all"))
                ver.add(startNode);

            // Now look for a node of includeNodeType within a given depth
            traversal = traversalSource.withSideEffect("x", ver).V(startNode)
                .times(genericQueryBuilder.getDepth()).repeat(__.both().store("x")).cap("x")
                .unfold();

            if (!genericQueryBuilder.getIncludeNodeTypes().contains("all")) {
                traversal.where(__.has(AAIProperties.NODE_TYPE,
                    P.within(genericQueryBuilder.getIncludeNodeTypes())));
            }
            queryResults = traversal.toList();

            if (queryResults.isEmpty()) {
                AAIException aaiException =
                    new AAIException("AAI_6114", "No nodes found - apipe was null/empty");
                ErrorLogHelper.logException(aaiException);
            } else {

                Introspector searchResults = createSearchResults(genericQueryBuilder.getLoader(),
                    genericQueryBuilder.getUrlBuilder(), queryResults);

                String outputMediaType =
                    getMediaType(genericQueryBuilder.getHeaders().getAcceptableMediaTypes());
                org.onap.aai.introspection.MarshallerProperties properties =
                    new org.onap.aai.introspection.MarshallerProperties.Builder(
                        org.onap.aai.restcore.MediaType.getEnum(outputMediaType)).build();

                result = searchResults.marshal(properties);
                response = Response.ok().entity(result).build();

                LOGGER.debug("{} node(s) traversed, {} found", ver.size(), queryResults.size());
            }
            success = true;
        } catch (AAIException e) {
            success = false;
            throw e;
        } catch (Exception e) {
            success = false;
            throw new AAIException("AAI_5105", e);
        } finally {
            if (genericQueryBuilder.getDbEngine() != null) {
                if (success) {
                    genericQueryBuilder.getDbEngine().commit();
                } else {
                    genericQueryBuilder.getDbEngine().rollback();
                }
            }

        }

        return response;
    }

    private URI craftUriFromQueryParams(Loader loader, String startNodeType,
        List<String> startNodeKeyParams) throws UnsupportedEncodingException, AAIException {
        Introspector relationship = loader.introspectorFromName("relationship");

        relationship.setValue("related-to", startNodeType);
        List<Object> relationshipDataList = relationship.getValue("relationship-data");

        for (String keyData : startNodeKeyParams) {
            int colonIndex = keyData.indexOf(':');
            if (colonIndex <= 0) {
                throw new AAIException("AAI_6120", "Bad key param passed in: [" + keyData + "]");
            } else {
                Introspector data = loader.introspectorFromName("relationship-data");
                data.setValue("relationship-key", keyData.substring(0, colonIndex));
                data.setValue("relationship-value", keyData.substring(colonIndex + 1));
                relationshipDataList.add(data.getUnderlyingObject());
            }
        }

        RelationshipToURI parser = new RelationshipToURI(loader, relationship);

        return parser.getUri();
    }

    /**
     * Run nodes query.
     *
     * @param nodesQuery
     * @throws AAIException the AAI exception
     */
    public Response runNodesQuery(NodesQueryBuilder nodesQuery) throws AAIException {

        Response response = null;
        boolean success = true;
        String result = "";
        final String EQUALS = "EQUALS";
        final String DOES_NOT_EQUAL = "DOES-NOT-EQUAL";
        final String EXISTS = "EXISTS";
        final String DOES_NOT_EXIST = "DOES-NOT-EXIST";
        try {

            nodesQuery.getDbEngine().startTransaction();

            Introspector target;

            if (StringUtils.isBlank(nodesQuery.getTargetNodeType())) {
                throw new AAIException("AAI_6120",
                    "null or empty target-node-type passed to the node query");
            }

            try {
                target =
                    nodesQuery.getLoader().introspectorFromName(nodesQuery.getTargetNodeType());
            } catch (AAIUnknownObjectException e) {
                throw new AAIException("AAI_6115", "Unrecognized nodeType ["
                    + nodesQuery.getTargetNodeType() + "] passed to node query.");
            }

            if (nodesQuery.getFilterParams().isEmpty()
                && nodesQuery.getEdgeFilterParams().isEmpty()) {
                // For now, it's ok to pass no filter params. We'll just return ALL the nodes of the
                // requested type.
                LOGGER.debug("No filters passed to the node query");
            }

            GraphTraversal<Vertex, Vertex> traversal =
                nodesQuery.getDbEngine().asAdmin().getReadOnlyTraversalSource().V()
                    .has(AAIProperties.NODE_TYPE, nodesQuery.getTargetNodeType());

            for (String filter : nodesQuery.getFilterParams()) {
                String[] pieces = filter.split(":");
                if (pieces.length < 2) {
                    throw new AAIException("AAI_6120",
                        "bad filter passed to node query: [" + filter + "]");
                } else {
                    String propName = this.findDbPropName(target, pieces[0]);
                    String filterType = pieces[1];
                    if (filterType.equals(EQUALS)) {
                        if (pieces.length < 3) {
                            throw new AAIException("AAI_6120",
                                "No value passed for filter: [" + filter + "]");
                        }
                        String value = "?";
                        if (pieces.length == 3) {
                            value = pieces[2];
                        } else { // length > 3
                            // When a ipv6 address comes in as a value, it has colons in it which
                            // require us to
                            // pull the "value" off the end of the filter differently
                            int startPos4Value = propName.length() + filterType.length() + 3;
                            value = filter.substring(startPos4Value);
                        }
                        traversal.has(propName, value);
                    } else if (filterType.equals(DOES_NOT_EQUAL)) {
                        if (pieces.length < 3) {
                            throw new AAIException("AAI_6120",
                                "No value passed for filter: [" + filter + "]");
                        }
                        String value = "?";
                        if (pieces.length == 3) {
                            value = pieces[2];
                        } else { // length > 3
                            // When a ipv6 address comes in as a value, it has colons in it which
                            // require us to
                            // pull the "value" off the end of the filter differently
                            int startPos4Value = propName.length() + filterType.length() + 3;
                            value = filter.substring(startPos4Value);
                        }
                        traversal.not(__.has(propName, value));
                    } else if (filterType.equals(EXISTS)) {
                        traversal.has(propName);
                    } else if (filterType.equals(DOES_NOT_EXIST)) {
                        traversal.hasNot(propName);
                    } else {
                        throw new AAIException("AAI_6120",
                            "bad filterType passed: [" + filterType + "]");
                    }
                }
            }

            if (!nodesQuery.getEdgeFilterParams().isEmpty()) {
                // edge-filter=pserver:EXISTS: OR pserver:EXISTS:hostname:XXX
                // edge-filter=pserver:DOES-NOT-EXIST: OR pserver:DOES-NOT-EXIST:hostname:XXX
                String filter = nodesQuery.getEdgeFilterParams().get(0); // we process and allow
                                                                         // only one edge filter
                                                                         // for now
                String[] pieces = filter.split(":");
                if (pieces.length < 2 || pieces.length == 3 || pieces.length > 4) {
                    throw new AAIException("AAI_6120", "bad edge-filter passed: [" + filter + "]");
                } else {
                    String nodeType = pieces[0].toLowerCase();
                    String filterType = pieces[1].toUpperCase();
                    Introspector otherNode;
                    if (!filterType.equals(EXISTS) && !filterType.equals(DOES_NOT_EXIST)) {
                        throw new AAIException("AAI_6120",
                            "bad filterType passed: [" + filterType + "]");
                    }
                    try {
                        otherNode = nodesQuery.getLoader().introspectorFromName(nodeType);
                    } catch (AAIUnknownObjectException e) {
                        throw new AAIException("AAI_6115",
                            "Unrecognized nodeType [" + nodeType + "] passed to node query.");
                    }
                    String propName = null;
                    String propValue = null;
                    if (pieces.length >= 3) {
                        propName = this.findDbPropName(otherNode, pieces[2].toLowerCase());
                        propValue = pieces[3];
                    }
                    String[] edgeLabels = getEdgeLabel(nodesQuery.getTargetNodeType(), nodeType);

                    GraphTraversal<Vertex, Vertex> edgeSearch = __.start();

                    edgeSearch.both(edgeLabels).has(AAIProperties.NODE_TYPE, nodeType);
                    if (propName != null) {
                        // check for matching property
                        if (propValue != null) {
                            edgeSearch.has(propName, propValue);
                        } else {
                            edgeSearch.has(propName);
                        }
                    }

                    if (filterType.equals(DOES_NOT_EXIST)) {
                        traversal.where(__.not(edgeSearch));
                    } else {
                        traversal.where(edgeSearch);
                    }
                }
            }

            List<Vertex> results = traversal.toList();
            Introspector searchResults =
                createSearchResults(nodesQuery.getLoader(), nodesQuery.getUrlBuilder(), results);

            String outputMediaType =
                getMediaType(nodesQuery.getHeaders().getAcceptableMediaTypes());
            org.onap.aai.introspection.MarshallerProperties properties =
                new org.onap.aai.introspection.MarshallerProperties.Builder(
                    org.onap.aai.restcore.MediaType.getEnum(outputMediaType)).build();

            result = searchResults.marshal(properties);
            response = Response.ok().entity(result).build();

            success = true;
        } catch (AAIException e) {
            success = false;
            throw e;
        } catch (Exception e) {
            success = false;
            throw new AAIException("AAI_5105", e);
        } finally {
            if (nodesQuery.getDbEngine() != null) {
                if (success) {
                    nodesQuery.getDbEngine().commit();
                } else {
                    nodesQuery.getDbEngine().rollback();
                }
            }
        }

        return response;
    }

    protected Introspector createSearchResults(Loader loader, UrlBuilder urlBuilder,
        List<Vertex> results) throws AAIUnknownObjectException {
        Introspector searchResults = loader.introspectorFromName("search-results");
        List<Object> resultDataList = searchResults.getValue("result-data");
        Stream<Vertex> stream;
        if (results.size() >= 50) {
            stream = results.parallelStream();
        } else {
            stream = results.stream();
        }
        boolean isParallel = stream.isParallel();
        stream.forEach(v -> {
            String nodeType = v.<String>property(AAIProperties.NODE_TYPE).orElse(null);

            String thisNodeURL;
            try {
                thisNodeURL = urlBuilder.pathed(v);
                Introspector resultData = loader.introspectorFromName("result-data");

                resultData.setValue("resource-type", nodeType);
                resultData.setValue("resource-link", thisNodeURL);
                if (isParallel) {
                    synchronized (resultDataList) {
                        resultDataList.add(resultData.getUnderlyingObject());
                    }
                } else {
                    resultDataList.add(resultData.getUnderlyingObject());
                }
            } catch (AAIException | AAIFormatVertexException e) {
                throw new RuntimeException(e);
            }

        });
        return searchResults;
    }

    private String findDbPropName(Introspector obj, String propName) {

        Optional<String> result = obj.getPropertyMetadata(propName, PropertyMetadata.DB_ALIAS);
        if (result.isPresent()) {
            return result.get();
        } else {
            return propName;
        }
    }

    /**
     * Gets the edge label.
     *
     * @param targetNodeType the target node type
     * @param nodeType the node type
     * @return the edge label
     * @throws EdgeRuleNotFoundException The Edge Rule Not Found Exception
     */
    public String[] getEdgeLabel(String targetNodeType, String nodeType)
        throws EdgeRuleNotFoundException {

        EdgeRuleQuery query = new EdgeRuleQuery.Builder(targetNodeType, nodeType).build();
        Multimap<String, EdgeRule> edgeRules = edgeIngestor.getRules(query);

        return edgeRules.values().stream().map(EdgeRule::getLabel).toArray(String[]::new);
    }

    /**
     * Run named query.
     *
     * @param fromAppId the from app id
     * @param transId the trans id
     * @param queryParameters the query parameters
     * @param aaiExtMap the aai ext map
     * @return the response
     * @throws JAXBException the JAXB exception
     * @throws AAIException the AAI exception
     */
    public Response runNamedQuery(String fromAppId, String transId, String queryParameters,
        AAIExtensionMap aaiExtMap) throws AAIException {

        Introspector inventoryItems;
        boolean success = true;
        TransactionalGraphEngine dbEngine = null;
        try {

            MoxyLoader loader = (MoxyLoader) loaderFactory.createLoaderForVersion(ModelType.MOXY,
                schemaVersions.getDefaultVersion());
            DynamicJAXBContext jaxbContext = loader.getJAXBContext();
            dbEngine = new JanusGraphDBEngine(QueryStyle.TRAVERSAL, loader);
            DBSerializer serializer = new DBSerializer(schemaVersions.getDefaultVersion(), dbEngine,
                ModelType.MOXY, fromAppId);
            ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

            dbEngine.startTransaction();
            org.onap.aai.restcore.MediaType mediaType =
                org.onap.aai.restcore.MediaType.APPLICATION_JSON_TYPE;
            String contentType = aaiExtMap.getHttpServletRequest().getContentType();
            if (contentType != null && contentType.contains("application/xml")) {
                mediaType = org.onap.aai.restcore.MediaType.APPLICATION_XML_TYPE;
            }

            if (queryParameters.length() == 0) {
                queryParameters = "{}";
            }

            DynamicEntity modelAndNamedQuerySearch = (DynamicEntity) loader
                .unmarshal("ModelAndNamedQuerySearch", queryParameters, mediaType)
                .getUnderlyingObject();
            if (modelAndNamedQuerySearch == null) {
                throw new AAIException("AAI_5105");
            }
            Map<String, Object> namedQueryLookupHash = new HashMap<>();

            DynamicEntity qp = modelAndNamedQuerySearch.get("queryParameters");
            String namedQueryUuid = null;
            if ((qp != null) && qp.isSet("namedQuery")) {
                DynamicEntity namedQuery = (DynamicEntity) qp.get("namedQuery");

                if (namedQuery.isSet("namedQueryUuid")) {
                    namedQueryUuid = namedQuery.get("namedQueryUuid");
                }
                if (namedQuery.isSet("namedQueryName")) {
                    namedQueryLookupHash.put("named-query-name", namedQuery.get("namedQueryName"));
                }
                if (namedQuery.isSet("namedQueryVersion")) {
                    namedQueryLookupHash.put("named-query-version",
                        namedQuery.get("namedQueryVersion"));
                }
            }

            if (namedQueryUuid == null) {

                DbMethHelper dbMethHelper = new DbMethHelper(loader, dbEngine);
                List<Vertex> namedQueryVertices =
                    dbMethHelper.locateUniqueVertices("named-query", namedQueryLookupHash);
                for (Vertex vert : namedQueryVertices) {
                    namedQueryUuid = vert.<String>property("named-query-uuid").orElse(null);
                    // there should only be one, we'll pick the first if not
                    break;
                }
            }

            String secondaryFilterCutPoint = null;

            if (modelAndNamedQuerySearch.isSet("secondaryFilterCutPoint")) {
                secondaryFilterCutPoint = modelAndNamedQuerySearch.get("secondaryFilterCutPoint");
            }

            List<Map<String, Object>> startNodeFilterHash = new ArrayList<>();

            mapInstanceFilters(modelAndNamedQuerySearch.get("instanceFilters"), startNodeFilterHash,
                jaxbContext);

            Map<String, Object> secondaryFilterHash = new HashMap<>();

            mapSecondaryFilters(modelAndNamedQuerySearch.get("secondaryFilts"), secondaryFilterHash,
                jaxbContext);

            List<ResultSet> resultSet =
                processor.queryByNamedQuery(transId, fromAppId, namedQueryUuid, startNodeFilterHash,
                    aaiExtMap.getApiVersion(), secondaryFilterCutPoint, secondaryFilterHash);

            inventoryItems = loader.introspectorFromName("inventory-response-items");

            List<Object> invItemList = unpackResultSet(resultSet, dbEngine, loader, serializer);

            inventoryItems.setValue("inventory-response-item", invItemList);
            success = true;
        } catch (AAIException e) {
            success = false;
            throw e;
        } catch (Exception e) {
            success = false;
            throw new AAIException("AAI_5105", e);
        } finally {
            if (dbEngine != null) {
                if (success) {
                    dbEngine.commit();
                } else {
                    dbEngine.rollback();
                }
            }
        }

        return getResponseFromIntrospector(inventoryItems, aaiExtMap.getHttpHeaders());
    }

    /**
     * Execute model operation.
     *
     * @param fromAppId the from app id
     * @param transId the trans id
     * @param queryParameters the query parameters
     * @param isDelete the is delete
     * @param aaiExtMap the aai ext map
     * @return the response
     * @throws JAXBException the JAXB exception
     * @throws AAIException the AAI exception
     * @throws DynamicException the dynamic exception
     * @throws UnsupportedEncodingException the unsupported encoding exception
     */
    public Response executeModelOperation(String fromAppId, String transId, String queryParameters,
        boolean isDelete, AAIExtensionMap aaiExtMap) throws AAIException {
        Response response;
        boolean success = true;
        TransactionalGraphEngine dbEngine = null;
        try {

            MoxyLoader loader = (MoxyLoader) loaderFactory.createLoaderForVersion(ModelType.MOXY,
                schemaVersions.getDefaultVersion());
            DynamicJAXBContext jaxbContext = loader.getJAXBContext();
            dbEngine = new JanusGraphDBEngine(QueryStyle.TRAVERSAL, loader);
            DBSerializer serializer = new DBSerializer(schemaVersions.getDefaultVersion(), dbEngine,
                ModelType.MOXY, fromAppId);
            ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
            dbEngine.startTransaction();

            org.onap.aai.restcore.MediaType mediaType =
                org.onap.aai.restcore.MediaType.APPLICATION_JSON_TYPE;
            String contentType = aaiExtMap.getHttpServletRequest().getContentType();
            if (contentType != null && contentType.contains("application/xml")) {
                mediaType = org.onap.aai.restcore.MediaType.APPLICATION_XML_TYPE;
            }

            if (queryParameters.length() == 0) {
                queryParameters = "{}";
            }

            DynamicEntity modelAndNamedQuerySearch = (DynamicEntity) loader
                .unmarshal("ModelAndNamedQuerySearch", queryParameters, mediaType)
                .getUnderlyingObject();
            if (modelAndNamedQuerySearch == null) {
                throw new AAIException("AAI_5105");
            }

            String modelVersionId = null;
            String modelName = null;
            String modelInvariantId = null;
            String modelVersion;
            String topNodeType = null;

            if (modelAndNamedQuerySearch.isSet("topNodeType")) {
                topNodeType = modelAndNamedQuerySearch.get("topNodeType");
            }

            // the ways to get a model:

            // 1. model-version-id (previously model-name-version-id
            // 2. model-invariant-id (previously model-id) + model-version
            // 3. model-name + model-version

            // we will support both using the OverloadedModel object in the v9 oxm. This allows us
            // to unmarshal
            // either an old-style model or new-style model + model-ver object
            if (modelAndNamedQuerySearch.isSet("queryParameters")) {
                DynamicEntity qp = modelAndNamedQuerySearch.get("queryParameters");

                if (qp.isSet("model")) {
                    DynamicEntity model = qp.get("model");

                    // on an old-style model object, the following 4 attrs were all present
                    if (model.isSet("modelNameVersionId")) {
                        modelVersionId = model.get("modelNameVersionId");
                    }
                    if (model.isSet("modelId")) {
                        modelInvariantId = model.get("modelId");
                    }
                    if (model.isSet("modelName")) {
                        modelName = model.get("modelName");
                    }
                    if (model.isSet("modelVersion")) {
                        modelVersion = model.get("modelVersion");
                    }

                    // new style splits model-invariant-id from the other 3 attrs. This is
                    // the only way to directly look up the model object
                    if (model.isSet("modelInvariantId")) {
                        modelInvariantId = model.get("modelInvariantId");
                    }

                    if (model.isSet("modelVers")) {
                        // we know that this is new style, because modelVers was not an option
                        // before v9
                        DynamicEntity modelVers = model.get("modelVers");
                        if (modelVers.isSet("modelVer")) {
                            List<DynamicEntity> modelVerList = modelVers.get("modelVer");
                            // if they send more than one, too bad, they get the first one
                            DynamicEntity modelVer = modelVerList.get(0);
                            if (modelVer.isSet("modelName")) {
                                modelName = modelVer.get("modelName");
                            }
                            if (modelVer.isSet("modelVersionId")) {
                                modelVersionId = modelVer.get("modelVersionId");
                            }
                            if (modelVer.isSet("modelVersion")) {
                                modelVersion = modelVer.get("modelVersion");
                            }
                        }
                    }
                }
            }

            List<Map<String, Object>> startNodeFilterHash = new ArrayList<>();

            String resourceVersion = mapInstanceFilters(
                modelAndNamedQuerySearch.get("instanceFilters"), startNodeFilterHash, jaxbContext);

            if (isDelete) {

                List<ResultSet> resultSet =
                    processor.queryByModel(transId, fromAppId, modelVersionId, modelInvariantId,
                        modelName, topNodeType, startNodeFilterHash, aaiExtMap.getApiVersion());

                unpackResultSet(resultSet, dbEngine, loader, serializer);

                ResultSet rs = resultSet.get(0);

                Vertex firstVert = rs.getVert();
                String restURI = serializer.getURIForVertex(firstVert).toString();
                String notificationVersion = schemaVersions.getDefaultVersion().toString();
                Map<String, String> delResult =
                    processor.runDeleteByModel(transId, fromAppId, modelVersionId, topNodeType,
                        startNodeFilterHash.get(0), aaiExtMap.getApiVersion(), resourceVersion);

                String resultStr = "";
                for (Map.Entry<String, String> ent : delResult.entrySet()) {
                    resultStr += "v[" + ent.getKey() + "] " + ent.getValue() + ",\n";
                }

                // Note - notifications are now done down in the individual "remove" calls done in
                // runDeleteByModel() above.

                response = Response.ok(resultStr.trim()).build();

            } else {
                List<ResultSet> resultSet =
                    processor.queryByModel(transId, fromAppId, modelVersionId, modelInvariantId,
                        modelName, topNodeType, startNodeFilterHash, aaiExtMap.getApiVersion());

                Introspector inventoryItems =
                    loader.introspectorFromName("inventory-response-items");

                List<Object> invItemList = unpackResultSet(resultSet, dbEngine, loader, serializer);

                inventoryItems.setValue("inventory-response-item", invItemList);

                response = getResponseFromIntrospector(inventoryItems, aaiExtMap.getHttpHeaders());
            }
            success = true;
        } catch (AAIException e) {
            success = false;
            throw e;
        } catch (Exception e) {
            success = false;
            throw new AAIException("AAI_5105", e);
        } finally {
            if (dbEngine != null) {
                if (success) {
                    dbEngine.commit();
                } else {
                    dbEngine.rollback();
                }
            }
        }

        return response;
    }

    private Response getResponseFromIntrospector(Introspector obj, HttpHeaders headers) {
        boolean isJson = false;
        for (MediaType mt : headers.getAcceptableMediaTypes()) {
            if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mt)) {
                isJson = true;
                break;
            }
        }
        org.onap.aai.introspection.MarshallerProperties properties;
        if (isJson) {
            properties = new org.onap.aai.introspection.MarshallerProperties.Builder(
                org.onap.aai.restcore.MediaType.APPLICATION_JSON_TYPE).build();
        } else {
            properties = new org.onap.aai.introspection.MarshallerProperties.Builder(
                org.onap.aai.restcore.MediaType.APPLICATION_XML_TYPE).build();
        }

        String marshalledObj = obj.marshal(properties);
        return Response.ok().entity(marshalledObj).build();
    }

    /**
     * Map instance filters.
     *
     * @param instanceFilters the instance filters
     * @param startNodeFilterHash the start node filter hash
     * @param jaxbContext the jaxb context
     * @return the string
     */
    private String mapInstanceFilters(DynamicEntity instanceFilters,
        List<Map<String, Object>> startNodeFilterHash, DynamicJAXBContext jaxbContext) {

        if (instanceFilters == null || !instanceFilters.isSet("instanceFilter")) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<DynamicEntity> instanceFilter =
            (ArrayList<DynamicEntity>) instanceFilters.get("instanceFilter");
        String resourceVersion = null;

        for (DynamicEntity instFilt : instanceFilter) {
            List<DynamicEntity> any = instFilt.get("any");
            HashMap<String, Object> thisNodeFilterHash = new HashMap<>();
            for (DynamicEntity anyEnt : any) {
                String clazz = anyEnt.getClass().getCanonicalName();
                String simpleClazz = anyEnt.getClass().getSimpleName();

                String nodeType = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, simpleClazz);

                DynamicType anyEntType = jaxbContext.getDynamicType(clazz);

                for (String propName : anyEntType.getPropertiesNames()) {
                    // hyphencase the prop and throw it on the hash
                    if (anyEnt.isSet(propName)) {
                        thisNodeFilterHash.put(
                            nodeType + "."
                                + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, propName),
                            anyEnt.get(propName));
                        if (propName.equals("resourceVersion") && resourceVersion == null) {
                            resourceVersion = (String) anyEnt.get(propName);
                        }
                    }
                }
            }
            startNodeFilterHash.add(thisNodeFilterHash);
        }
        return resourceVersion;
    }

    /**
     * Map secondary filters.
     *
     * @param secondaryFilts the secondary filters
     * @param secondaryFilterHash the secondary filter hash
     * @param jaxbContext the jaxb context
     * @return the string
     */
    private void mapSecondaryFilters(DynamicEntity secondaryFilts,
        Map<String, Object> secondaryFilterHash, DynamicJAXBContext jaxbContext) {

        if (secondaryFilts == null || !secondaryFilts.isSet("secondaryFilt")) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<DynamicEntity> secondaryFilter =
            (ArrayList<DynamicEntity>) secondaryFilts.get("secondaryFilt");

        for (DynamicEntity secondaryFilt : secondaryFilter) {
            List<DynamicEntity> any = secondaryFilt.get("any");

            for (DynamicEntity anyEnt : any) {
                String clazz = anyEnt.getClass().getCanonicalName();
                String simpleClazz = anyEnt.getClass().getSimpleName();

                String nodeType = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, simpleClazz);

                DynamicType anyEntType = jaxbContext.getDynamicType(clazz);

                for (String propName : anyEntType.getPropertiesNames()) {
                    // hyphencase the prop and throw it on the hash
                    if (anyEnt.isSet(propName)) {
                        secondaryFilterHash.put(
                            nodeType + "."
                                + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_HYPHEN, propName),
                            anyEnt.get(propName));
                    }
                }
            }
        }
    }

    /**
     * Remap inventory items.
     *
     * @param invResultItem the inv result item
     * @param jaxbContext the jaxb context
     * @param includeTheseVertices the include these vertices
     * @param objectToVertMap the object to vert map
     * @param aaiExtMap the aai ext map
     * @return the dynamic entity
     */
    private DynamicEntity remapInventoryItems(DynamicEntity invResultItem,
        DynamicJAXBContext jaxbContext, Map<String, String> includeTheseVertices,
        Map<Object, String> objectToVertMap, AAIExtensionMap aaiExtMap) {

        DynamicEntity inventoryItem = jaxbContext.newDynamicEntity(
            "inventory.aai.onap.org." + aaiExtMap.getApiVersion() + ".InventoryResponseItem");
        Object item = invResultItem.get("item");
        inventoryItem.set("modelName", invResultItem.get("modelName"));
        inventoryItem.set("item", item);
        inventoryItem.set("extraProperties", invResultItem.get("extraProperties"));

        String vertexId = "";

        if (objectToVertMap.containsKey(item)) {
            vertexId = objectToVertMap.get(item);
        }

        if (includeTheseVertices.containsKey(vertexId)
            && invResultItem.isSet("inventoryResponseItems")) {
            List<DynamicEntity> invItemList = new ArrayList<>();
            DynamicEntity inventoryItems = jaxbContext.newDynamicEntity(
                "inventory.aai.att.com." + aaiExtMap.getApiVersion() + ".InventoryResponseItems");
            DynamicEntity subInventoryResponseItems = invResultItem.get("inventoryResponseItems");
            List<DynamicEntity> subInventoryResponseItemList =
                subInventoryResponseItems.get("inventoryResponseItem");
            for (DynamicEntity ent : subInventoryResponseItemList) {
                DynamicEntity invItem = remapInventoryItems(ent, jaxbContext, includeTheseVertices,
                    objectToVertMap, aaiExtMap);
                if (invItem != null) {
                    invItemList.add(invItem);
                }
            }
            inventoryItems.set("inventoryResponseItem", invItemList);
            inventoryItem.set("inventoryResponseItems", inventoryItems);
        }
        return inventoryItem;
    }

    /**
     * Unpack result set.
     *
     * @param resultSetList The result set list
     * @param engine The engine
     * @param loader The loader
     * @param serializer The database serializer
     * @return the array list - should return list of inventoryItems
     * @throws AAIException The AAI Exception
     */
    private List<Object> unpackResultSet(List<ResultSet> resultSetList,
        TransactionalGraphEngine engine, Loader loader, DBSerializer serializer)
        throws AAIException {

        List<Object> resultList = new ArrayList<>();

        for (ResultSet resultSet : resultSetList) {

            if (resultSet.getVert() == null) {
                continue;
            }

            Introspector inventoryItem = loader.introspectorFromName("inventory-response-item");
            Introspector inventoryItems = loader.introspectorFromName("inventory-response-items");
            // add this inventoryItem to the resultList for this level
            resultList.add(inventoryItem.getUnderlyingObject());

            Vertex vert = resultSet.getVert();

            String aaiNodeType = vert.<String>property("aai-node-type").orElse(null);

            if (aaiNodeType != null) {
                Introspector thisObj = loader.introspectorFromName(aaiNodeType);

                if (resultSet.getExtraPropertyHash() != null) {
                    Map<String, Object> extraProperties = resultSet.getExtraPropertyHash();

                    Introspector extraPropertiesEntity =
                        loader.introspectorFromName("extra-properties");

                    List<Object> extraPropsList = extraPropertiesEntity.getValue("extra-property");

                    for (Map.Entry<String, Object> ent : extraProperties.entrySet()) {
                        String propName = ent.getKey();
                        Object propVal = ent.getValue();

                        Introspector extraPropEntity =
                            loader.introspectorFromName("extra-property");

                        extraPropEntity.setValue("property-name", propName);
                        extraPropEntity.setValue("property-value", propVal);

                        extraPropsList.add(extraPropEntity.getUnderlyingObject());

                    }
                    inventoryItem.setValue("extra-properties",
                        extraPropertiesEntity.getUnderlyingObject());
                }

                try {
                    serializer.dbToObject(Collections.singletonList(vert), thisObj, 0, true,
                        "false");
                } catch (UnsupportedEncodingException e1) {
                    throw new AAIException("AAI_5105");
                }
                PropertyLimitDesc propertyLimitDesc = resultSet.getPropertyLimitDesc();

                if (propertyLimitDesc != null) {

                    if (PropertyLimitDesc.SHOW_NONE.equals(propertyLimitDesc)) {
                        HashMap<String, Object> emptyPropertyOverRideHash = new HashMap<>();
                        for (String key : thisObj.getAllKeys()) {
                            emptyPropertyOverRideHash.put(key, null);
                        }
                        filterProperties(thisObj, emptyPropertyOverRideHash);
                    } else if (PropertyLimitDesc.SHOW_ALL.equals(propertyLimitDesc)) {
                        // keep everything
                    } else if (PropertyLimitDesc.SHOW_NAME_AND_KEYS_ONLY
                        .equals(propertyLimitDesc)) {
                        HashMap<String, Object> keysAndNamesPropHash = new HashMap<>();

                        for (String key : thisObj.getAllKeys()) {
                            keysAndNamesPropHash.put(key, null);
                        }
                        String namePropMetaData = thisObj.getMetadata(ObjectMetadata.NAME_PROPS);
                        if (namePropMetaData != null) {
                            String[] nameProps = namePropMetaData.split(",");
                            for (String names : nameProps) {
                                keysAndNamesPropHash.put(names, null);
                            }
                        }
                        filterProperties(thisObj, keysAndNamesPropHash);
                    }
                } else {
                    if (resultSet.getPropertyOverRideHash() != null
                        && resultSet.getPropertyOverRideHash().size() > 0) {
                        Map<String, Object> propertyOverRideHash =
                            resultSet.getPropertyOverRideHash();
                        if (propertyOverRideHash.containsKey("persona-model-id")) {
                            propertyOverRideHash.remove("persona-model-id");
                            propertyOverRideHash.put("model-invariant-id", null);
                        }
                        for (String key : thisObj.getAllKeys()) {
                            propertyOverRideHash.put(key, null);
                        }
                        filterProperties(thisObj, propertyOverRideHash);
                    } else {
                        // keep everything
                    }
                }

                if (thisObj != null) {
                    inventoryItem.setValue("item", thisObj.getUnderlyingObject());

                    String modelName = null;
                    try {
                        // Try to get the modelName if we can. Otherwise, do not fail, just return
                        // what we have already.
                        String modelInvariantIdLocal =
                            vert.<String>property("model-invariant-id-local").orElse(null); // this
                                                                                            // one
                                                                                            // points
                                                                                            // at a
                                                                                            // model
                        String modelVersionIdLocal =
                            vert.<String>property("model-version-id-local").orElse(null); // this
                                                                                          // one
                                                                                          // points
                                                                                          // at a
                                                                                          // model-ver

                        if ((modelInvariantIdLocal != null && modelVersionIdLocal != null)
                            && (modelInvariantIdLocal.length() > 0
                                && modelVersionIdLocal.length() > 0)) {
                            Introspector modelVer = loader.introspectorFromName("model-ver");
                            modelVer.setValue("model-version-id", modelVersionIdLocal);
                            QueryBuilder builder = engine.getQueryBuilder().createDBQuery(modelVer);
                            List<Vertex> modelVerVerts = builder.toList();
                            if ((modelVerVerts != null) && (modelVerVerts.size() == 1)) {
                                Vertex modelVerVert = modelVerVerts.get(0);
                                modelName =
                                    modelVerVert.<String>property("model-name").orElse(null);
                                if (modelName != null && modelName.length() > 0) {
                                    inventoryItem.setValue("model-name", modelName);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // it's ok, dynamic object might not have these fields
                        // it's ok, couldn't find a matching model
                    }

                    if (resultSet.getSubResultSet() != null) {
                        List<ResultSet> subResultSet = resultSet.getSubResultSet();
                        if (subResultSet != null && !subResultSet.isEmpty()) {
                            List<Object> res =
                                unpackResultSet(subResultSet, engine, loader, serializer);
                            if (!res.isEmpty()) {
                                inventoryItems.setValue("inventory-response-item", res);
                                inventoryItem.setValue("inventory-response-items",
                                    inventoryItems.getUnderlyingObject());
                            }
                        }
                    }
                }
            }
        }

        return resultList;
    }

    private void filterProperties(Introspector thisObj, Map<String, Object> override) {

        thisObj.getProperties().stream().filter(x -> !override.containsKey(x)).forEach(prop -> {
            if (thisObj.isSimpleType(prop)) {
                thisObj.setValue(prop, null);
            }
        });

    }

    /**
     * Gets the media type.
     *
     * @param mediaTypeList the media type list
     * @return the media type
     */
    protected String getMediaType(List<MediaType> mediaTypeList) {
        String mediaType = MediaType.APPLICATION_JSON; // json is the default
        for (MediaType mt : mediaTypeList) {
            if (MediaType.APPLICATION_XML_TYPE.isCompatible(mt)) {
                mediaType = MediaType.APPLICATION_XML;
            }
        }
        return mediaType;
    }
}
