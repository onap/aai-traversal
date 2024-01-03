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
package org.onap.aai.dbgraphgen;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.aai.AAISetup;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.parsers.exceptions.AAIIdentityMapParseException;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.JanusGraphDBEngine;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.setup.SchemaVersion;

public class ModelBasedProcessingTest extends AAISetup {

    private SchemaVersion version;
    private static final ModelType introspectorFactoryType = ModelType.MOXY;
    private static final QueryStyle queryStyle = QueryStyle.TRAVERSAL;

    private static final String TRANSACTION_ID = "transaction-1";
    private static final String FROM_APP_ID = "JUNIT";
    private static final String API_VERSION = "1.0";
    private static final String AAI_NODE_TYPE = "aai-node-type";

    private static final String MODEL_VESION_NODE_VALUE = "model-ver";
    private static final String MODEL_VERSION_ID_KEY = "model-version-id";
    private static final String MODEL_VERSION_ID_VALUE = "model-version-id-1";

    private static final String MODEL_INVARIANT_ID_NODE_VALUE = "model-invariant-id-local";
    private static final String MODEL_INVARIANT_ID_KEY = "model-invariant-id-local";
    private static final String MODEL_INVARIANT_ID_VALUE = "model-invariant-id-1";

    private static final String MODEL_NAME_NODE_VALUE = "model-name";
    private static final String MODEL_NAME_ID_KEY = "model-name";
    private static final String MODEL_NAME_ID_VALUE = "generic-vnf";

    private static TransactionalGraphEngine dbEngine;
    private static TransactionalGraphEngine.Admin admin;
    DBSerializer serializer;
    private static Loader loader;

    ModelBasedProcessing modelBasedProcessor;

    GraphTraversalSource source;

    Graph graph;

    Vertex model;
    Vertex modelVersion;
    Vertex modelElement;
    Vertex constrainedElementSet;
    Vertex namedQueryElement;
    Vertex linkagePoints;
    Vertex namedQuery;

    @BeforeClass
    public static void configure() throws Exception {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");
    }

    @Before
    public void init() throws AAIException {
        MockitoAnnotations.openMocks(this);
        version = schemaVersions.getDefaultVersion();
        loader = loaderFactory.createLoaderForVersion(introspectorFactoryType, version);
        TransactionalGraphEngine newDbEngine = new JanusGraphDBEngine(queryStyle, loader);
        dbEngine = Mockito.spy(newDbEngine);
        serializer = new DBSerializer(version, dbEngine, introspectorFactoryType, "JUNIT");
        admin = Mockito.spy(dbEngine.asAdmin());
        modelBasedProcessor = new ModelBasedProcessing(loader, dbEngine, serializer);
        graph = TinkerGraph.open();
        source = createGraph();
    }

    private GraphTraversalSource createGraph() throws AAIException {
        model = graph.addVertex(T.label, "model", T.id, "0", AAI_NODE_TYPE, "model",
            "model-invariant-id", "model-invariant-id-1", "model-type", "widget");
        modelVersion = graph.addVertex(T.label, MODEL_VESION_NODE_VALUE, T.id, "1", AAI_NODE_TYPE,
            MODEL_VESION_NODE_VALUE, MODEL_VERSION_ID_KEY, MODEL_VERSION_ID_VALUE,
            MODEL_NAME_ID_KEY, MODEL_NAME_ID_VALUE, "model-version", "model-version-1");
        graph.addVertex(T.label, MODEL_INVARIANT_ID_NODE_VALUE, T.id, "2", MODEL_INVARIANT_ID_KEY,
            MODEL_INVARIANT_ID_VALUE, "model-version-id-local", MODEL_VERSION_ID_VALUE);
        namedQuery = graph.addVertex(T.label, "named-query", T.id, "3", "aai-node-type",
            "named-query", "named-query-uuid", "named-query-uuid-1");
        graph.addVertex(T.label, MODEL_NAME_NODE_VALUE, T.id, "4", AAI_NODE_TYPE,
            MODEL_NAME_NODE_VALUE, MODEL_NAME_ID_KEY, MODEL_NAME_ID_VALUE);
        modelElement =
            graph.addVertex(T.label, "model-element", T.id, "5", AAI_NODE_TYPE, "model-element");
        Vertex modelConstraint =
            graph.addVertex(T.label, "model-constraint", T.id, "6", AAI_NODE_TYPE,
                "model-constraint", "constrained-element-set-uuid-2-replace", "cesu2r-1");
        constrainedElementSet = graph.addVertex(T.label, "constrained-element-set", T.id, "7",
            AAI_NODE_TYPE, "constrained-element-set");
        Vertex elementChoiceSet = graph.addVertex(T.label, "element-choice-set", T.id, "8",
            AAI_NODE_TYPE, "element-choice-set");
        namedQueryElement = graph.addVertex(T.label, "named-query-element", T.id, "9",
            "aai-node-type", "named-query-element", "property-limit-desc", "show-all",
            "do-not-output", "true", "named-query-element-uuid", "named-query-element-uuid-1",
            "property-collect-list", "property-collect-list-1");
        linkagePoints = graph.addVertex(T.label, "linkage-points", T.id, "10", AAI_NODE_TYPE,
            "linkage-points", "linkage-points", getArrayListAsString(), "new-data-del-flag", "F");

        GraphTraversalSource g = graph.traversal();
        edgeSer.addTreeEdge(g, model, modelVersion);
        edgeSer.addTreeEdge(g, modelElement, modelConstraint);
        edgeSer.addTreeEdge(g, constrainedElementSet, modelConstraint);
        edgeSer.addTreeEdge(g, modelVersion, modelElement);
        edgeSer.addTreeEdge(g, modelElement, constrainedElementSet);
        edgeSer.addTreeEdge(g, constrainedElementSet, elementChoiceSet);
        edgeSer.addTreeEdge(g, modelElement, elementChoiceSet);
        edgeSer.addTreeEdge(g, namedQuery, namedQueryElement);
        edgeSer.addTreeEdge(g, namedQueryElement, namedQueryElement);
        edgeSer.addEdge(g, modelVersion, modelElement);
        edgeSer.addEdge(g, model, namedQueryElement);
        return g;
    }

    @Test(expected = AAIException.class)
    public void testGetStartNodesAndModVersionIds_NullId_ExpectException() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        String passedModelVerId = null;
        String passedModelInvId = null;
        String passedModelName = null;
        modelBasedProcessor.getStartNodesAndModVersionIds("9999", "postmen", passedModelVerId,
            passedModelInvId, passedModelName, "generic-vnf", startNodeFilterArrayOfHashes, "");
    }

    @Test
    public void testGetStartNodesAndModVersionIds_ModelVersion() throws AAIException {

        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        String passedModelInvId = null;
        String passedModelName = null;

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getStartNodesAndModVersionIds(TRANSACTION_ID, FROM_APP_ID,
            MODEL_VERSION_ID_VALUE, passedModelInvId, passedModelName, AAI_NODE_TYPE,
            startNodeFilterArrayOfHashes, API_VERSION);
    }

    @Test
    public void testGetStartNodesAndModVersionIds_ModelInId() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        String passedModelVersion = null;
        String passedModelName = null;

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getStartNodesAndModVersionIds(TRANSACTION_ID, FROM_APP_ID,
            passedModelVersion, MODEL_INVARIANT_ID_VALUE, passedModelName, AAI_NODE_TYPE,
            startNodeFilterArrayOfHashes, API_VERSION);
    }

    @Test
    public void testGetStartNodesAndModVersionIds_ModelName() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        String passedModelVersion = null;
        String passedModelInvId = null;
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getStartNodesAndModVersionIds(TRANSACTION_ID, FROM_APP_ID,
            passedModelVersion, passedModelInvId, MODEL_NAME_ID_VALUE, AAI_NODE_TYPE,
            startNodeFilterArrayOfHashes, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testGetStartNodesAndModVersionIds_NonEmptyHashMap_ModelVersion()
        throws AAIException {
        Map<String, Object> map = new HashMap<>();
        map.put("model-ver.model-name", "model-name");
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        startNodeFilterArrayOfHashes.add(map);
        String passedModelInvId = null;
        String passedModelName = null;

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getStartNodesAndModVersionIds(TRANSACTION_ID, FROM_APP_ID,
            MODEL_VERSION_ID_VALUE, passedModelInvId, passedModelName, MODEL_NAME_ID_VALUE,
            startNodeFilterArrayOfHashes, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testGetStartNodesAndModVersionIds_NonEmptyHashMap_ModelInvId() throws AAIException {
        Map<String, Object> map = new HashMap<>();
        map.put("model-ver.model-name", "model-name");
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        startNodeFilterArrayOfHashes.add(map);
        String passedModelVersion = null;
        String passedModelName = null;

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getStartNodesAndModVersionIds(TRANSACTION_ID, FROM_APP_ID,
            passedModelVersion, MODEL_INVARIANT_ID_VALUE, passedModelName, MODEL_NAME_ID_VALUE,
            startNodeFilterArrayOfHashes, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testGetStartNodesAndModVersionIds_NonEmptyHashMap_ModelName() throws AAIException {
        Map<String, Object> map = new HashMap<>();
        map.put("model-ver.model-name", "model-name");
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        startNodeFilterArrayOfHashes.add(map);
        String passedModelVersion = null;
        String passedModelInvId = null;
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getStartNodesAndModVersionIds(TRANSACTION_ID, FROM_APP_ID,
            passedModelVersion, passedModelInvId, MODEL_NAME_ID_VALUE, MODEL_NAME_ID_VALUE,
            startNodeFilterArrayOfHashes, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testQueryByModel() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        String passedModelInvId = null;
        String passedModelName = null;

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.queryByModel(TRANSACTION_ID, FROM_APP_ID, MODEL_VERSION_ID_VALUE,
            passedModelInvId, passedModelName, AAI_NODE_TYPE, startNodeFilterArrayOfHashes,
            API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testQueryByModel_Timed() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        String passedModelVersion = null;
        String passedModelName = null;

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.queryByModel_Timed(TRANSACTION_ID, FROM_APP_ID, passedModelVersion,
            MODEL_INVARIANT_ID_VALUE, passedModelName, MODEL_NAME_ID_VALUE,
            startNodeFilterArrayOfHashes, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testgetModelTypeFromModelVer_NullVertexArg() throws AAIException {
        Vertex nullVertex = null;
        modelBasedProcessor.getModelVerTopWidgetType(nullVertex, "");
    }

    @Test
    public void testValidateNamedQuery_FoundQuery() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);

        modelBasedProcessor.validateNamedQuery("9999", "JUNIT", "named-query-uuid-1", "1.0");
    }

    @Test(expected = AAIException.class)
    public void testValidateNamedQuery_NotFoundQuery() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);

        modelBasedProcessor.validateNamedQuery("9999", "JUNIT", "named-query-uuid", "1.0");
    }

    @Test(expected = AAIException.class)
    public void testGetNodeUsingUniqueId_NullUniqueId() throws AAIException {
        modelBasedProcessor.getNodeUsingUniqueId("9999", "postmen", "generic-vnf", "vnf-id", "");
        modelBasedProcessor.getNodeUsingUniqueId("9999", "postmen", "generic-vnf", "vnf-id", null);
    }

    @Test(expected = AAIException.class)
    public void testGetNodeUsingUniqueId_NullPropertyName() throws AAIException {
        modelBasedProcessor.getNodeUsingUniqueId("9999", "postmen", "generic-vnf", "", "vnf-id-1");
        modelBasedProcessor.getNodeUsingUniqueId("9999", "postmen", "generic-vnf", null,
            "vnf-id-1");
    }

    @Test(expected = AAIException.class)
    public void testGetNodeUsingUniqueId_NullNodeType() throws AAIException {
        modelBasedProcessor.getNodeUsingUniqueId("9999", "postmen", "", "vnf-id", "vnf-id-1");
        modelBasedProcessor.getNodeUsingUniqueId("9999", "postmen", null, "vnf-id", "vnf-id-1");
    }

    @Test(expected = AAIException.class)
    public void testValidateModel() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.validateModel("9999", "JUNIT", MODEL_VERSION_ID_VALUE, "1.0");
    }

    @Test
    public void testShowResultSet_NullVertex() {
        ResultSet rs = getResultSet();
        rs.setVert(null);
        modelBasedProcessor.showResultSet(rs, 1);
    }

    @Test
    public void testShowResultSet_NonEmptyOverrideHash() {
        ResultSet rs = getResultSet();
        modelBasedProcessor.showResultSet(rs, 2);
    }

    @Test
    public void testShowResultSet_EmptyOverrideHash() {
        ResultSet rs = getResultSet();
        rs.setPropertyOverRideHash(new HashMap<String, Object>());
        modelBasedProcessor.showResultSet(rs, 2);
    }

    private ResultSet getResultSet() {
        ResultSet rs = new ResultSet();
        rs.setVert(model);
        rs.setLocationInModelSubGraph("2");
        Map<String, Object> overrideHash = new HashMap<String, Object>();
        rs.setPropertyOverRideHash(overrideHash);
        overrideHash.put("key1", "value1");
        overrideHash.put("key2", "value2");
        overrideHash.put("key3", "value3");
        overrideHash.put("key4", "value4");

        Map<String, Object> extraHash = new HashMap<String, Object>();
        rs.setExtraPropertyHash(extraHash);
        extraHash.put("key1", "value1");
        extraHash.put("key2", "value2");
        extraHash.put("key3", "value3");
        extraHash.put("key4", "value4");

        return rs;
    }

    @Test
    public void testPruneResultSet_NotCutPointType() throws AAIException {

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);

        ResultSet rs = getResultSet();
        List<ResultSet> subResultSet = new ArrayList<>();
        subResultSet.add(getResultSet());
        rs.setSubResultSet(subResultSet);
        modelBasedProcessor.pruneResultSet(rs, "mdl", new HashMap<>());
    }

    @Test
    public void testPruneResultSet_CutPointType() throws AAIException {

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);

        ResultSet rs = getResultSet();
        Map<String, Object> startNodeFilterHash = new HashMap<>();
        startNodeFilterHash.put("model.model_type", "widget");
        startNodeFilterHash.put("named_query.named-query-uuid", "named-query-uuid-1");
        modelBasedProcessor.pruneResultSet(rs, "model", startNodeFilterHash);
    }

    @Test
    public void testCollapseForDoNotOutput_FlagTrue() throws AAIException {

        ResultSet rs = getResultSet();
        List<ResultSet> subResultSet = new ArrayList<>();
        subResultSet.add(getResultSet());
        rs.setSubResultSet(subResultSet);
        rs.setDoNotOutputFlag("true");
        modelBasedProcessor.collapseForDoNotOutput(rs);
    }

    @Test
    public void testCollapseForDoNotOutput_FlagFalse() throws AAIException {

        ResultSet rs = getResultSet();
        List<ResultSet> subResultSet = new ArrayList<>();
        subResultSet.add(getResultSet());
        rs.setSubResultSet(subResultSet);
        rs.setDoNotOutputFlag("false");
        modelBasedProcessor.collapseForDoNotOutput(rs);
    }

    @Test
    public void testMakeSureItsAnArrayList() {
        String listString = getArrayListAsString();
        modelBasedProcessor.makeSureItsAnArrayList(listString);
    }

    private String getArrayListAsString() {
        List<String> strList = new ArrayList<>();
        strList.add("1");
        strList.add("2");
        strList.add("3");
        return strList.toString();
    }

    @Test
    public void testGetModConstraintHash() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getModConstraintHash(modelElement, new HashMap<>());
    }

    @Test(expected = AAIException.class)
    public void testGenTopoMap4ModelVer_WidgetType() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.genTopoMap4ModelVer(TRANSACTION_ID, FROM_APP_ID, modelVersion,
            MODEL_VERSION_ID_VALUE);
    }

    @Test(expected = AAIException.class)
    public void testGenTopoMap4ModelVer_ServiceType() throws AAIException {

        Graph serviceGraph = TinkerGraph.open();
        Vertex modelV = serviceGraph.addVertex(T.label, "model", T.id, "20", AAI_NODE_TYPE, "model",
            "model-invariant-id", "model-invariant-id-1", "model-type", "service");
        Vertex modelVerV = serviceGraph.addVertex(T.label, MODEL_VESION_NODE_VALUE, T.id, "21",
            AAI_NODE_TYPE, MODEL_VESION_NODE_VALUE, MODEL_VERSION_ID_KEY, MODEL_VERSION_ID_VALUE,
            MODEL_NAME_ID_KEY, MODEL_NAME_ID_VALUE, "model-version", "model-version-1");
        Vertex modelElementV =
            graph.addVertex(T.label, "model-element", T.id, "22", AAI_NODE_TYPE, "model-element");
        GraphTraversalSource gts = serviceGraph.traversal();

        // EdgeRules rules4Service = EdgeRules.getInstance();
        edgeSer.addTreeEdge(gts, modelV, modelVerV);
        edgeSer.addTreeEdge(gts, modelElementV, modelVerV);
        edgeSer.addEdge(gts, modelElementV, modelVerV);
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.genTopoMap4ModelVer(TRANSACTION_ID, FROM_APP_ID, modelVerV,
            MODEL_VERSION_ID_VALUE);
    }

    @Test(expected = AAIException.class)
    public void testCollectTopology4ModelVer() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);

        Multimap<String, String> initialEmptyMap = ArrayListMultimap.create();
        List<String> vidsTraversed = new ArrayList<>();
        modelBasedProcessor.collectTopology4ModelVer(TRANSACTION_ID, FROM_APP_ID, modelElement, "",
            initialEmptyMap, vidsTraversed, 0, null, MODEL_INVARIANT_ID_VALUE,
            MODEL_VERSION_ID_VALUE);
    }

    @Test(expected = AAIException.class)
    public void testGetNextStepElementsFromSet_NullVertex() throws AAIException {
        modelBasedProcessor.getNextStepElementsFromSet(null);
    }

    @Test
    public void testRundeleteAsNeededFromResultSet() throws AAIException {

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);

        ResultSet rs = getResultSet();
        rs.setNewDataDelFlag("T");
        modelBasedProcessor.deleteAsNeededFromResultSet(TRANSACTION_ID, FROM_APP_ID, rs, "",
            API_VERSION, API_VERSION, new HashMap<>());
    }

    @Test(expected = AAIException.class)
    public void testQueryByNamedQuery() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.queryByNamedQuery(TRANSACTION_ID, FROM_APP_ID, "named-query-uuid-1",
            new ArrayList<>(), API_VERSION);
    }

    @Test
    public void testCollectInstanceData() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        Multimap<String, String> validNextStepMap = ArrayListMultimap.create();
        validNextStepMap.put("named-query-element-uuid-1", "named-query-element-uuid-1");
        List<String> vidsTraversed = new ArrayList<>();
        vidsTraversed.add("named-query-element-uuid-1");
        Map<String, String> namedQueryElementHash = new HashMap<>();
        namedQueryElementHash.put("named-query-element-uuid-1", "named-query-element-uuid-1");
        modelBasedProcessor.collectInstanceData(TRANSACTION_ID, FROM_APP_ID, modelElement,
            "named-query-element-uuid-1", validNextStepMap, vidsTraversed, 0, new HashMap<>(),
            namedQueryElementHash, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testNamedQueryConstraintSaysStop_NullNamedQueryVertex() throws AAIException {
        modelBasedProcessor.namedQueryConstraintSaysStop(TRANSACTION_ID, FROM_APP_ID, null, model,
            API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testNamedQueryConstraintSaysStop_NullInstanceVertex() throws AAIException {
        modelBasedProcessor.namedQueryConstraintSaysStop(TRANSACTION_ID, FROM_APP_ID, model, null,
            API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testNamedQueryConstraintSaysStop_NullContraintType() throws AAIException {

        Graph serviceGraph = TinkerGraph.open();
        Vertex namedQueryElementV = graph.addVertex(T.label, "named-query-element", T.id, "30",
            "aai-node-type", "named-query-element", "property-limit-desc", "show-all",
            "do-not-output", "true", "named-query-element-uuid", "named-query-element-uuid-1",
            "property-collect-list", "property-collect-list-1");
        Vertex propertyContraintV = graph.addVertex(T.label, "property-constraint", T.id, "31",
            AAI_NODE_TYPE, "property-constraint");
        Vertex instanceVertexV = graph.addVertex(T.label, "instance-vertex", T.id, "32",
            AAI_NODE_TYPE, "instance-vertex", "property-name", "property-name");
        GraphTraversalSource gts = serviceGraph.traversal();

        edgeSer.addTreeEdge(gts, namedQueryElementV, propertyContraintV);

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.namedQueryConstraintSaysStop(TRANSACTION_ID, FROM_APP_ID,
            namedQueryElementV, instanceVertexV, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testNamedQueryConstraintSaysStop_NullPropertyName() throws AAIException {

        Graph serviceGraph = TinkerGraph.open();
        Vertex namedQueryElementV = graph.addVertex(T.label, "named-query-element", T.id, "33",
            "aai-node-type", "named-query-element", "property-limit-desc", "show-all",
            "do-not-output", "true", "named-query-element-uuid", "named-query-element-uuid-1",
            "property-collect-list", "property-collect-list-1");
        Vertex propertyContraintV = graph.addVertex(T.label, "property-constraint", T.id, "34",
            AAI_NODE_TYPE, "property-constraint", "constraint-type", "EQUALS");
        Vertex instanceVertexV = graph.addVertex(T.label, "instance-vertex", T.id, "35",
            AAI_NODE_TYPE, "instance-vertex", "property-name", "property-name");
        GraphTraversalSource gts = serviceGraph.traversal();

        // EdgeRules rules4Service = EdgeRules.getInstance();
        edgeSer.addTreeEdge(gts, namedQueryElementV, propertyContraintV);

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.namedQueryConstraintSaysStop(TRANSACTION_ID, FROM_APP_ID,
            namedQueryElementV, instanceVertexV, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testNamedQueryConstraintSaysStop_NullPropertyValue() throws AAIException {

        Graph serviceGraph = TinkerGraph.open();
        Vertex namedQueryElementV = graph.addVertex(T.label, "named-query-element", T.id, "36",
            "aai-node-type", "named-query-element", "property-limit-desc", "show-all",
            "do-not-output", "true", "named-query-element-uuid", "named-query-element-uuid-1",
            "property-collect-list", "property-collect-list-1");
        Vertex propertyContraintV = graph.addVertex(T.label, "property-constraint", T.id, "37",
            AAI_NODE_TYPE, "property-constraint", "constraint-type", "EQUALS", "property-name",
            "property-name");
        Vertex instanceVertexV = graph.addVertex(T.label, "instance-vertex", T.id, "38",
            AAI_NODE_TYPE, "instance-vertex", "property-name", "property-name");
        GraphTraversalSource gts = serviceGraph.traversal();

        edgeSer.addTreeEdge(gts, namedQueryElementV, propertyContraintV);

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.namedQueryConstraintSaysStop(TRANSACTION_ID, FROM_APP_ID,
            namedQueryElementV, instanceVertexV, API_VERSION);
    }

    @Test
    public void testNamedQueryConstraintSaysStop_ConstraintTypeEquals() throws AAIException {

        Graph serviceGraph = TinkerGraph.open();
        Vertex namedQueryElementV = graph.addVertex(T.label, "named-query-element", T.id, "39",
            "aai-node-type", "named-query-element", "property-limit-desc", "show-all",
            "do-not-output", "true", "named-query-element-uuid", "named-query-element-uuid-1",
            "property-collect-list", "property-collect-list-1");
        Vertex propertyContraintV = graph.addVertex(T.label, "property-constraint", T.id, "40",
            AAI_NODE_TYPE, "property-constraint", "constraint-type", "EQUALS", "property-name",
            "property-name", "property-value", "property-value");
        Vertex instanceVertexV = graph.addVertex(T.label, "instance-vertex", T.id, "41",
            AAI_NODE_TYPE, "instance-vertex", "property-name", "property-name");
        GraphTraversalSource gts = serviceGraph.traversal();

        edgeSer.addTreeEdge(gts, namedQueryElementV, propertyContraintV);

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.namedQueryConstraintSaysStop(TRANSACTION_ID, FROM_APP_ID,
            namedQueryElementV, instanceVertexV, API_VERSION);
    }

    @Test
    public void testNamedQueryConstraintSaysStop_ConstraintTypeNotEquals() throws AAIException {

        Graph serviceGraph = TinkerGraph.open();
        Vertex namedQueryElementV = graph.addVertex(T.label, "named-query-element", T.id, "42",
            "aai-node-type", "named-query-element", "property-limit-desc", "show-all",
            "do-not-output", "true", "named-query-element-uuid", "named-query-element-uuid-1",
            "property-collect-list", "property-collect-list-1");
        Vertex propertyContraintV = graph.addVertex(T.label, "property-constraint", T.id, "43",
            AAI_NODE_TYPE, "property-constraint", "constraint-type", "NOT-EQUALS", "property-name",
            "property-name", "property-value", "property-value");
        Vertex instanceVertexV = graph.addVertex(T.label, "instance-vertex", T.id, "44",
            AAI_NODE_TYPE, "instance-vertex", "property-name", "property-name");
        GraphTraversalSource gts = serviceGraph.traversal();

        edgeSer.addTreeEdge(gts, namedQueryElementV, propertyContraintV);

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.namedQueryConstraintSaysStop(TRANSACTION_ID, FROM_APP_ID,
            namedQueryElementV, instanceVertexV, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testGetNamedQueryExtraDataLookup_TargetNodeTypeNull() throws AAIException {

        Graph serviceGraph = TinkerGraph.open();
        Vertex namedQueryElementV = graph.addVertex(T.label, "named-query-element", T.id, "45",
            "aai-node-type", "named-query-element", "property-limit-desc", "show-all",
            "do-not-output", "true", "named-query-element-uuid", "named-query-element-uuid-1",
            "property-collect-list", "property-collect-list-1");
        Vertex relatedLookUpV =
            graph.addVertex(T.label, "related-lookup", T.id, "46", AAI_NODE_TYPE, "related-lookup",
                "source-node-property", "source-node-property", "source-node-type", "generic-vnf");
        Vertex instanceVertexV = graph.addVertex(T.label, "instance-vertex", T.id, "47",
            AAI_NODE_TYPE, "instance-vertex", "property-name", "property-name");
        GraphTraversalSource gts = serviceGraph.traversal();
        edgeSer.addTreeEdge(gts, namedQueryElementV, relatedLookUpV);

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.getNamedQueryExtraDataLookup(TRANSACTION_ID, FROM_APP_ID,
            namedQueryElementV, instanceVertexV, API_VERSION);
    }

    @Test
    public void testGetNamedQueryExtraDataLookup_InvalidSourceProperty() throws AAIException {

        Graph serviceGraph = TinkerGraph.open();
        Vertex namedQueryElementV = graph.addVertex(T.label, "named-query-element", T.id, "51",
            "aai-node-type", "named-query-element", "property-limit-desc", "show-all",
            "do-not-output", "true", "named-query-element-uuid", "named-query-element-uuid-1",
            "property-collect-list", "property-collect-list-1");
        Vertex relatedLookUpV =
            graph.addVertex(T.label, "related-lookup", T.id, "52", AAI_NODE_TYPE, "related-lookup",
                "source-node-property", "source-node-property", "source-node-type", "generic-vnf",
                "target-node-type", "generic-vnf", "target-node-property", "generic-vnf",
                "property-collect-list", "property-collect-list");
        Vertex instanceVertexV = graph.addVertex(T.label, "instance-vertex", T.id, "53",
            AAI_NODE_TYPE, "instance-vertex", "property-name", "property-name");
        GraphTraversalSource gts = serviceGraph.traversal();
        edgeSer.addTreeEdge(gts, namedQueryElementV, relatedLookUpV);

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.getNamedQueryExtraDataLookup(TRANSACTION_ID, FROM_APP_ID,
            namedQueryElementV, instanceVertexV, API_VERSION);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNamedQueryExtraDataLookup_ValidSourceProperty() throws AAIException {

        Graph serviceGraph = TinkerGraph.open();
        Vertex namedQueryElementV = graph.addVertex(T.label, "named-query-element", T.id, "54",
            "aai-node-type", "named-query-element", "property-limit-desc", "show-all",
            "do-not-output", "true", "named-query-element-uuid", "named-query-element-uuid-1",
            "property-collect-list", "property-collect-list-1");
        Vertex relatedLookUpV =
            graph.addVertex(T.label, "related-lookup", T.id, "55", AAI_NODE_TYPE, "related-lookup",
                "source-node-property", "source-node-property", "source-node-type", "generic-vnf",
                "target-node-type", "generic-vnf", "target-node-property", "generic-vnf");
        Vertex instanceVertexV = graph.addVertex(T.label, "instance-vertex", T.id, "56",
            AAI_NODE_TYPE, "instance-vertex", "source-node-property", "source-node-property");
        GraphTraversalSource gts = serviceGraph.traversal();
        edgeSer.addTreeEdge(gts, namedQueryElementV, relatedLookUpV);

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.getNamedQueryExtraDataLookup(TRANSACTION_ID, FROM_APP_ID,
            namedQueryElementV, instanceVertexV, API_VERSION);
    }

    @Test
    public void testCollectNQElementHash() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.collectNQElementHash(TRANSACTION_ID, FROM_APP_ID, namedQueryElement, "",
            new HashMap<>(), new ArrayList<>(), 0);
    }

    @Test
    public void testCollectDeleteKeyHash() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.collectDeleteKeyHash(TRANSACTION_ID, FROM_APP_ID, linkagePoints, "",
            new HashMap<>(), new ArrayList<>(), 0, new HashMap<>(), "model-version-1",
            "model-version-id-local");
    }

    @Test(expected = AAIException.class)
    public void testCheck4EdgeRule_InvalidNodeA() throws AAIException {
        modelBasedProcessor.check4EdgeRule("model-version1", "model-ver");
    }

    @Test(expected = AAIException.class)
    public void testCheck4EdgeRule_InvalidNodeB() throws AAIException {
        modelBasedProcessor.check4EdgeRule("model-ver", "model-version1");
    }

    @Test(expected = AAIException.class)
    public void testCheck4EdgeRule_InvalidEdge() throws AAIException {
        modelBasedProcessor.check4EdgeRule("model-ver", "named-query");
    }

    @Test
    public void testCollectTopology4LinkagePoint() throws AAIException {
        String linkagePointStrVal = new String("model-ver|model");
        String incomingTrail = new String("model|model-ver");
        Multimap<String, String> currentMap = ArrayListMultimap.create();
        modelBasedProcessor.collectTopology4LinkagePoint(TRANSACTION_ID, FROM_APP_ID,
            linkagePointStrVal, incomingTrail, currentMap);
    }

    @Test
    public void testGenTopoMap4NamedQ() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.genTopoMap4NamedQ(TRANSACTION_ID, FROM_APP_ID, namedQuery,
            "named-query-uuid-1");
    }

    @Test
    public void testGetModelThatNqElementRepresents() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getModelThatNqElementRepresents(namedQueryElement, "");
    }

    @Test
    public void testGetModelVerThatElementRepresents() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getModelVerThatElementRepresents(modelElement, "");
    }

    @Test
    public void testGetModelTypeFromModel() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getModelTypeFromModel(model, "");
    }

    @Test
    public void testGetModelVerTopWidgetType() throws AAIException {
        Graph serviceGraph = TinkerGraph.open();
        Vertex modelV = serviceGraph.addVertex(T.label, "model", T.id, "57", AAI_NODE_TYPE, "model",
            "model-invariant-id", "model-invariant-id-1", "model-type", "service");
        Vertex modelVerV = serviceGraph.addVertex(T.label, MODEL_VESION_NODE_VALUE, T.id, "58",
            AAI_NODE_TYPE, MODEL_VESION_NODE_VALUE, MODEL_VERSION_ID_KEY, MODEL_VERSION_ID_VALUE,
            MODEL_NAME_ID_KEY, MODEL_NAME_ID_VALUE, "model-version", "model-version-1");
        Vertex modelElementV =
            graph.addVertex(T.label, "model-element", T.id, "59", AAI_NODE_TYPE, "model-element");
        GraphTraversalSource gts = serviceGraph.traversal();

        edgeSer.addTreeEdge(gts, modelV, modelVerV);
        edgeSer.addTreeEdge(gts, modelElementV, modelVerV);
        edgeSer.addEdge(gts, modelElementV, modelVerV);
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.getModelVerTopWidgetType(modelVerV, "");
    }

    @Test
    public void testGetModelElementStepName() throws AAIException {
        Graph serviceGraph = TinkerGraph.open();
        Vertex modelV = serviceGraph.addVertex(T.label, "model", T.id, "60", AAI_NODE_TYPE, "model",
            "model-invariant-id", "model-invariant-id-1", "model-type", "service");
        Vertex modelVerV = serviceGraph.addVertex(T.label, MODEL_VESION_NODE_VALUE, T.id, "61",
            AAI_NODE_TYPE, MODEL_VESION_NODE_VALUE, MODEL_VERSION_ID_KEY, MODEL_VERSION_ID_VALUE,
            MODEL_NAME_ID_KEY, MODEL_NAME_ID_VALUE, "model-version", "model-version-1");
        Vertex modelElementV =
            graph.addVertex(T.label, "model-element", T.id, "62", AAI_NODE_TYPE, "model-element");
        GraphTraversalSource gts = serviceGraph.traversal();

        edgeSer.addTreeEdge(gts, modelV, modelVerV);
        edgeSer.addTreeEdge(gts, modelElementV, modelVerV);
        edgeSer.addEdge(gts, modelElementV, modelVerV);
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.getModelElementStepName(modelElementV, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunDeleteByModel() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        Map<String, Object> map = new HashMap<>();
        map.put("generic-vnf.d", "relationshipdata");

        modelBasedProcessor.runDeleteByModel(TRANSACTION_ID, FROM_APP_ID, MODEL_VERSION_ID_VALUE,
            "model-ver", map, API_VERSION, API_VERSION);
    }

    @Test
    public void testSecondConstructor() {
        ModelBasedProcessing mdp = new ModelBasedProcessing();
    }

    @Test(expected = AAIIdentityMapParseException.class)
    public void testQueryByNamedQuery_NonEmptyMap() throws AAIException {
        Map<String, Object> map = new HashMap<>();
        map.put("model-ver.model-name", "model-name");
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        startNodeFilterArrayOfHashes.add(map);
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);

        modelBasedProcessor.queryByNamedQuery(TRANSACTION_ID, FROM_APP_ID, "named-query-uuid-1",
            startNodeFilterArrayOfHashes, API_VERSION, null, null);

    }

    @Test(expected = AAIIdentityMapParseException.class)
    public void testRunDeleteByModel_Service() throws AAIException {
        Graph serviceGraph = TinkerGraph.open();
        Vertex modelV = serviceGraph.addVertex(T.label, "model", T.id, "63", AAI_NODE_TYPE, "model",
            "model-invariant-id", "model-invariant-id-1", "model-type", "service");
        Vertex modelVerV = serviceGraph.addVertex(T.label, MODEL_VESION_NODE_VALUE, T.id, "64",
            AAI_NODE_TYPE, MODEL_VESION_NODE_VALUE, MODEL_VERSION_ID_KEY, MODEL_VERSION_ID_VALUE,
            MODEL_NAME_ID_KEY, MODEL_NAME_ID_VALUE, "model-version", "model-version-1");
        Vertex modelElementV =
            graph.addVertex(T.label, "model-element", T.id, "65", AAI_NODE_TYPE, "model-element");
        GraphTraversalSource gts = serviceGraph.traversal();

        edgeSer.addTreeEdge(gts, modelV, modelVerV);
        edgeSer.addTreeEdge(gts, modelElementV, modelVerV);
        edgeSer.addEdge(gts, modelElementV, modelVerV);
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);

        Map<String, Object> map = new HashMap<>();
        map.put("generic-vnf.d", "relationshipdata");

        modelBasedProcessor.runDeleteByModel(TRANSACTION_ID, FROM_APP_ID, MODEL_VERSION_ID_VALUE,
            "model-ver", map, API_VERSION, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testGetStartNodesAndModVersionIds_NonEmptyHashMap_AllEmpty() throws AAIException {
        Map<String, Object> map = new HashMap<>();
        map.put("model-ver.model-name", "model-name");
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        startNodeFilterArrayOfHashes.add(map);
        String passedModelInvId = null;
        String passedModelName = null;
        String passedModelVerId = null;

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getStartNodesAndModVersionIds(TRANSACTION_ID, FROM_APP_ID,
            passedModelVerId, passedModelInvId, passedModelName, "", startNodeFilterArrayOfHashes,
            API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testGetStartNodesAndModVersionIds_NonEmptyHashMap_ModelTypeNonNull()
        throws AAIException {
        Map<String, Object> map = new HashMap<>();
        map.put("model-ver.model-name", "model-name");
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        startNodeFilterArrayOfHashes.add(map);
        String passedModelInvId = null;
        String passedModelName = null;
        String passedModelVerId = null;

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.getStartNodesAndModVersionIds(TRANSACTION_ID, FROM_APP_ID,
            passedModelVerId, passedModelInvId, passedModelName, MODEL_NAME_ID_VALUE,
            startNodeFilterArrayOfHashes, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testQueryByModel_Timed_NonEmptyHash() throws AAIException {
        Map<String, Object> map = new HashMap<>();
        map.put("model-ver.model-name", "model-name");
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        startNodeFilterArrayOfHashes.add(map);
        String passedModelVersion = null;
        String passedModelName = null;

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        modelBasedProcessor.queryByModel_Timed(TRANSACTION_ID, FROM_APP_ID, passedModelVersion,
            MODEL_INVARIANT_ID_VALUE, passedModelName, MODEL_NAME_ID_VALUE,
            startNodeFilterArrayOfHashes, API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testRunDeleteByModel_NullVersionAndNode() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        Map<String, Object> map = new HashMap<>();
        map.put("generic-vnf.d", "relationshipdata");

        modelBasedProcessor.runDeleteByModel(TRANSACTION_ID, FROM_APP_ID, "", "", map, API_VERSION,
            API_VERSION);
    }

    @Test(expected = AAIException.class)
    public void testRunDeleteByModel_NullVersion() throws AAIException {
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(source);
        Map<String, Object> map = new HashMap<>();
        map.put("generic-vnf.d", "relationshipdata");

        modelBasedProcessor.runDeleteByModel(TRANSACTION_ID, FROM_APP_ID, "", "model-ver", map,
            API_VERSION, API_VERSION);
    }

    @Test
    public void testGenTopoMap4ModelVer_WidgetType_Map() throws AAIException {

        Graph serviceGraph = TinkerGraph.open();
        Vertex modelV = serviceGraph.addVertex(T.label, "model", T.id, "66", AAI_NODE_TYPE, "model",
            "model-invariant-id", "model-invariant-id-1", "model-type", "widget");
        Vertex modelVerV = serviceGraph.addVertex(T.label, MODEL_VESION_NODE_VALUE, T.id, "67",
            AAI_NODE_TYPE, MODEL_VESION_NODE_VALUE, MODEL_VERSION_ID_KEY, MODEL_VERSION_ID_VALUE,
            MODEL_NAME_ID_KEY, MODEL_NAME_ID_VALUE, "model-version", "model-version-1");
        GraphTraversalSource gts = serviceGraph.traversal();

        edgeSer.addTreeEdge(gts, modelV, modelVerV);
        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);

        Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
        Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(gts);
        modelBasedProcessor.genTopoMap4ModelVer(TRANSACTION_ID, FROM_APP_ID, modelVerV,
            MODEL_VERSION_ID_VALUE);
    }

}
