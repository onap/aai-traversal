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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMapOf;
import static org.mockito.Mockito.when;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.graphdb.types.system.BaseVertexLabel;
import org.janusgraph.graphdb.types.system.EmptyVertex;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.aai.AAISetup;
import org.onap.aai.db.DbMethHelper;
import org.onap.aai.db.props.AAIProperties;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ModelBasedProcessingInvalidDataTest extends AAISetup {

    @Mock
    private static TransactionalGraphEngine dbEngine;
    private static Loader loader;
    @Mock
    private static DBSerializer serializer;
    ModelBasedProcessing processor;

    @Mock
    private ModelBasedProcessing mockProcessor;

    @Mock
    private DbMethHelper dbMethHelper;

    @BeforeClass
    public static void configure() throws Exception {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");
    }

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        loader = loaderFactory.createLoaderForVersion(ModelType.MOXY,
            schemaVersions.getDefaultVersion());
        processor = new ModelBasedProcessing(loader, dbEngine, serializer);

        dbMethHelper = new DbMethHelper(loader, dbEngine);

    }

    @Test
    public void getStartNodesAndModVersionIdsTest() throws AAIException {

        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("test", new Object());
        startNodeFilterArrayOfHashes.add(map);
        Map<String, String> result = new HashMap<>();

        assertNotNull(result);

    }

    @Test(expected = NullPointerException.class)
    public void getStartNodesAndModVersionIdsTest2() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("test", new Object());
        startNodeFilterArrayOfHashes.add(map);
        Map<String, String> result1 = processor.getStartNodesAndModVersionIds("test", "test", "",
            "test", "test", "test", startNodeFilterArrayOfHashes, "test");
        assertNotNull(result1);
    }

    @Test(expected = NullPointerException.class)
    public void getStartNodesAndModVersionIdsTest3() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("test", new Object());
        startNodeFilterArrayOfHashes.add(map);
        Map<String, String> result1 = processor.getStartNodesAndModVersionIds("test", "test", "",
            "", "test", "test", startNodeFilterArrayOfHashes, "test");
        assertNotNull(result1);
    }

    @Test(expected = AAIException.class)
    public void getStartNodesAndModVersionIdsTest4() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("test", new Object());
        startNodeFilterArrayOfHashes.add(map);
        Map<String, String> result1 = processor.getStartNodesAndModVersionIds("test", "test", "",
            "", "", "test", startNodeFilterArrayOfHashes, "test");
        assertNotNull(result1);
    }

    @Test(expected = AAIException.class)
    public void getStartNodesAndModVersionIdsTest5() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("test", new Object());
        startNodeFilterArrayOfHashes.add(map);
        Map<String, String> result1 = processor.getStartNodesAndModVersionIds("test", "test", "",
            "", "", "", startNodeFilterArrayOfHashes, "test");
        assertNotNull(result1);
    }

    @Test(expected = AAIException.class)
    public void getStartNodesAndModVersionIdsNullTest() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();

        Map<String, String> result = null;
        result = processor.getStartNodesAndModVersionIds("test", "test", "", "", "", "",
            startNodeFilterArrayOfHashes, "test");

        assertNotNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void getStartNodesAndModVersionIdsNullTest1() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();

        Map<String, String> result = null;
        result = processor.getStartNodesAndModVersionIds("test", "test", "Test", "", "", "",
            startNodeFilterArrayOfHashes, "test");

        assertNotNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void getStartNodesAndModVersionIdsNullTest2() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();

        Map<String, String> result = null;
        result = processor.getStartNodesAndModVersionIds("test", "test", "", "test", "", "",
            startNodeFilterArrayOfHashes, "test");
        assertNotNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void getStartNodesAndModVersionIdsNullTest3() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();

        Map<String, String> result = null;
        result = processor.getStartNodesAndModVersionIds("test", "test", "", "", "test", "",
            startNodeFilterArrayOfHashes, "test");
        assertNotNull(result);
    }

    @Test(expected = NullPointerException.class)
    public void getModelVerTopWidgetTypeTest() throws AAIException {
        Vertex vertex = new EmptyVertex();
        // Mockito.when(mockProcessor.getModelVerTopWidgetType(Mockito.any(Vertex.class),
        // Mockito.any(String.class))).thenReturn("Sucess");
        String result = processor.getModelVerTopWidgetType(vertex, "test");
        assertEquals("result has -local tacked on the end as it should", "Sucess", result);

    }

    @Test(expected = NullPointerException.class)
    public void getModelVerTopWidgetType() throws AAIException {
        /*
         * Mockito.when(mockProcessor.getModelVerTopWidgetType(Mockito.any(String.class),
         * Mockito.any(String.class), Mockito.any(String.class),
         * Mockito.any(String.class), Mockito.any(String.class))
         * ).thenReturn("Sucess");
         */
        String result = processor.getModelVerTopWidgetType("test", "test", "test", "Test", "test");
        assertEquals("result has -local tacked on the end as it should", "Sucess", result);

    }

    @Test(expected = AAIException.class)
    public void queryByModel() throws AAIException {
        /*
         * Mockito.when(mockProcessor.getModelVerTopWidgetType(Mockito.any(String.class),
         * Mockito.any(String.class), Mockito.any(String.class),
         * Mockito.any(String.class), Mockito.any(String.class))
         * ).thenReturn("Sucess");
         */
        List<ResultSet> result = processor.queryByModel("test", "test", "test", "test", "test",
            "generic-vnf", null, "test");
        assertEquals("result has -local tacked on the end as it should", 0, result.size());

    }

    @Test(expected = NullPointerException.class)
    public void queryByModel_Timed() throws AAIException {
        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("test", new Object());
        startNodeFilterArrayOfHashes.add(map);
        List<ResultSet> result = processor.queryByModel_Timed("test", "test", "test", "test",
            "test", "test", startNodeFilterArrayOfHashes, "test");
        assertEquals("result has -local tacked on the end as it should", 0, result.size());

    }

    @Mock
    Map<String, Object> startNodeFilterHash;

    @Test(expected = NullPointerException.class)
    public void runDeleteByModel() throws AAIException {
        Map<String, String> resultMock = new HashMap<String, String>();

        // when(mockProcessor.getNodeUsingUniqueId(any(String.class),any(String.class),any(String.class),any(String.class),any(String.class))).thenReturn(vertex);
        when(mockProcessor.runDeleteByModel(any(String.class), any(String.class), any(String.class),
            any(String.class), anyMapOf(String.class, Object.class), any(String.class),
            any(String.class))).thenReturn(resultMock);
        Map<String, String> result = processor.runDeleteByModel("test", "test", "test", "test",
            startNodeFilterHash, "test", "test");
        assertEquals("result has -local tacked on the end as it should", result.size(),
            resultMock.size());

    }

    Optional<Vertex> vertext = Optional.empty();

    @Test(expected = AAIException.class)
    public void runDeleteByModelWithNullParams() throws AAIException {

        Map<String, String> result =
            processor.runDeleteByModel("test", "test", null, null, null, "test", "test");

        assertNotNull(result);

    }

    @Test(expected = NullPointerException.class)
    public void runDeleteByModelWithNullParams1() throws AAIException {

        Map<String, String> result1 =
            processor.runDeleteByModel("test", "test", null, "unknown", null, "test", "test");
        assertNotNull(result1);

    }

    @Test(expected = NullPointerException.class)
    public void runDeleteByModelWithNullParams2() throws AAIException {

        Map<String, String> result1 =
            processor.runDeleteByModel("test", "test", null, "unknown", null, "test", "test");
        assertNotNull(result1);

    }

    @Test(expected = AAIException.class)
    public void queryByNamedQuery() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        String namedQueryUuid = "test";
        ArrayList<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();
        String apiVer = "test";
        List<ResultSet> result = processor.queryByNamedQuery(transId, fromAppId, namedQueryUuid,
            startNodeFilterArrayOfHashes, apiVer);
        assertNotNull(result);
    }

    @Test(expected = AAIException.class)
    public void queryByNamedQuery1() throws AAIException {
        String transId = "teet";
        String fromAppId = "test";
        String namedQueryUuid = "test";
        String secondaryFilterCutPoint = "test";
        List<Map<String, Object>> startNodeFilterArrayOfHashes =
            new ArrayList<Map<String, Object>>();
        String apiVer = "test";
        Map<String, Object> secondaryFilterHash = new HashMap<String, Object>();
        List<ResultSet> result = processor.queryByNamedQuery(transId, fromAppId, namedQueryUuid,
            startNodeFilterArrayOfHashes, apiVer, secondaryFilterCutPoint, secondaryFilterHash);
        assertNotNull(result);
    }

    @Test
    public void deleteAsNeededFromResultSet() throws AAIException {
        Vertex vert = new BaseVertexLabel("Test");
        Map<String, String> resultMock = new HashMap<String, String>();
        ResultSet resultSet = new ResultSet();
        resultSet.setVert(null);

        Map<String, String> result = processor.deleteAsNeededFromResultSet("test", "test",
            resultSet, "test", "test", "test", resultMock);

        assertEquals(result.size(), 0);

        resultSet.setVert(vert);

        assertEquals(result.size(), 0);

    }

    @Test(expected = NullPointerException.class)
    public void pruneResultSetTest() throws AAIException {
        ResultSet rs = new ResultSet();
        Vertex v = new BaseVertexLabel(AAIProperties.NODE_TYPE);
        rs.setVert(v);
        List<ResultSet> rsList = new ArrayList<ResultSet>();
        ResultSet rs1 = new ResultSet();
        rsList.add(rs1);
        rs.setSubResultSet(rsList);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("test", new Object());
        ResultSet resultSet = processor.pruneResultSet(rs, "testr", map);
        assertNotNull(resultSet);

    }

    @Test(expected = NullPointerException.class)
    public void satisfiesFiltersTest() throws AAIException {
        ResultSet rs = new ResultSet();
        Vertex v = new BaseVertexLabel(AAIProperties.NODE_TYPE);
        rs.setVert(v);
        rs.getVert().property(AAIProperties.NODE_TYPE);
        List<ResultSet> rsList = new ArrayList<ResultSet>();
        ResultSet rs1 = new ResultSet();
        rsList.add(rs1);
        rs.setSubResultSet(rsList);
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("modern.vertex-id", new Object());

        boolean result = processor.satisfiesFilters(rs, map);
        assertEquals(result, true);
    }

    @Test
    public void satisfiesFiltersTest1() throws AAIException {
        ResultSet rs = new ResultSet();
        Vertex v = new BaseVertexLabel(AAIProperties.NODE_TYPE);
        rs.setVert(v);
        List<ResultSet> rsList = new ArrayList<ResultSet>();
        ResultSet rs1 = new ResultSet();
        rsList.add(rs1);
        rs.setSubResultSet(rsList);
        Map<String, Object> map = new HashMap<String, Object>();

        boolean result = processor.satisfiesFilters(rs, map);
        assertEquals(result, false);
    }

    @Test(expected = AAIException.class)
    public void satisfiesFiltersTest2() throws AAIException {
        ResultSet rs = new ResultSet();
        Vertex v = new BaseVertexLabel(AAIProperties.NODE_TYPE);
        rs.setVert(v);
        List<ResultSet> rsList = new ArrayList<ResultSet>();
        ResultSet rs1 = new ResultSet();
        rsList.add(rs1);
        rs.setSubResultSet(rsList);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("testfilter", new Object());

        boolean result = processor.satisfiesFilters(rs, map);
        assertEquals(result, false);
    }

    @Test
    public void collapseForDoNotOutputTest() throws AAIException {
        ResultSet rs = new ResultSet();
        rs.setDoNotOutputFlag("true");
        List<ResultSet> rsList = new ArrayList<ResultSet>();
        ResultSet rs1 = new ResultSet();
        rsList.add(rs1);
        rs.setSubResultSet(rsList);
        List<ResultSet> result = processor.collapseForDoNotOutput(rs);
        assertEquals(result.size(), 1);
    }

    @Test(expected = NullPointerException.class)
    public void collectInstanceDataTest() throws AAIException {
        EmptyVertex ev = new EmptyVertex();
        Vertex thisLevelElemVtx = ev;

        List<String> vidsTraversed = new ArrayList<String>();
        // only applies when collecting data using the default model for delete
        Multimap<String, String> validNextStepMap = ArrayListMultimap.create();
        Map<String, String> namedQueryElementHash = new HashMap<String, String>();
        namedQueryElementHash.put("test", "test");
        Map<String, String> delKeyHash = new HashMap<String, String>();

        ResultSet rs = processor.collectInstanceData("test", "test", thisLevelElemVtx, "test",
            validNextStepMap, vidsTraversed, 1, delKeyHash, namedQueryElementHash, "test");
    }

    @Test(expected = NullPointerException.class)
    public void genTopoMap4ModelVerTest() throws AAIException {
        Vertex vertext = new EmptyVertex();
        Multimap<String, String> map =
            processor.genTopoMap4ModelVer("test", "test", vertext, "test");
        assertNotEquals(map, null);
    }

    @Test(expected = AAIException.class)
    public void genTopoMap4ModelVerTestNull() throws AAIException {
        Vertex vertext = null;
        Multimap<String, String> map =
            processor.genTopoMap4ModelVer("test", "test", vertext, "test");
        assertNotEquals(map, null);
    }

    @Test
    public void makeSureItsAnArrayListTest() {
        String input = "model-versionId,modelTestID,modelTestid2;";
        List<String> result = processor.makeSureItsAnArrayList(input);
        assertTrue(result.size() > 0);
    }

    @Test(expected = AAIException.class)
    public void getModConstraintHashTest() throws AAIException {
        Vertex modelElementVtx = new EmptyVertex();
        Vertex modelElementVtx1 = new EmptyVertex();
        Map<String, Vertex> currentHash = new HashMap<String, Vertex>();
        currentHash.put("constraint", modelElementVtx1);
        Map<String, Vertex> result = processor.getModConstraintHash(modelElementVtx, currentHash);
        assertTrue(result.size() > 0);
    }

    @Test(expected = AAIException.class)
    public void getModConstraintHashTestNull() throws AAIException {
        Vertex modelElementVtx = null;
        Vertex modelElementVtx1 = null;
        Map<String, Vertex> currentHash = new HashMap<String, Vertex>();
        currentHash.put("constraint", modelElementVtx1);
        Map<String, Vertex> result = processor.getModConstraintHash(modelElementVtx, currentHash);
        assertTrue(result.size() > 0);
    }

    @Test(expected = NullPointerException.class)
    public void getTopElementForSvcOrResModelVerTest() throws AAIException {
        Vertex modelElementVtx = new EmptyVertex();
        Vertex modelElementVtx1 = new EmptyVertex();
        Map<String, Vertex> currentHash = new HashMap<String, Vertex>();
        currentHash.put("constraint", modelElementVtx1);
        Vertex result = processor.getTopElementForSvcOrResModelVer(modelElementVtx, "test");
        assertNotEquals(result, null);
    }

    @Test
    public void getNamedQueryPropOverRideTest() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        Vertex namedQueryElementVertex = new EmptyVertex();
        Vertex instanceVertex = new EmptyVertex();
        String apiVer = "test";

        namedQueryElementVertex.properties("property-collect-list", "");

        Map<String, Object> result = processor.getNamedQueryPropOverRide(transId, fromAppId,
            namedQueryElementVertex, instanceVertex, apiVer);
        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void getNamedQueryPropOverRideTestNull() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        Vertex namedQueryElementVertex = null;
        Vertex instanceVertex = new EmptyVertex();
        String apiVer = "test";

        namedQueryElementVertex.properties("property-collect-list", "");

        Map<String, Object> result = processor.getNamedQueryPropOverRide(transId, fromAppId,
            namedQueryElementVertex, instanceVertex, apiVer);
        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void namedQueryConstraintSaysStopTest() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        Vertex namedQueryElementVertex = new EmptyVertex();
        Vertex instanceVertex = new EmptyVertex();
        String apiVer = "test";

        namedQueryElementVertex.properties("property-collect-list", "");

        boolean result = processor.namedQueryConstraintSaysStop(transId, fromAppId,
            namedQueryElementVertex, instanceVertex, apiVer);
        assertTrue(result);
    }

    @Test(expected = NullPointerException.class)
    public void namedQueryConstraintSaysStopTestNull() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        Vertex namedQueryElementVertex = null;
        Vertex instanceVertex = new EmptyVertex();
        String apiVer = "test";

        namedQueryElementVertex.properties("property-collect-list", "");

        boolean result = processor.namedQueryConstraintSaysStop(transId, fromAppId,
            namedQueryElementVertex, instanceVertex, apiVer);
        assertTrue(result);
    }

    @Test(expected = AAIException.class)
    public void namedQuerynamedQueryElementVertexNullTest() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        Vertex namedQueryElementVertex = null;
        Vertex instanceVertex = null;
        String apiVer = "test";

        boolean result = processor.namedQueryConstraintSaysStop(transId, fromAppId,
            namedQueryElementVertex, instanceVertex, apiVer);
        assertTrue(result);
    }

    @Test(expected = NullPointerException.class)

    public void getNamedQueryExtraDataLookupTest() throws Exception {

        String transId = "test";
        String fromAppId = "test";
        Vertex namedQueryElementVertex = new EmptyVertex();
        Vertex instanceVertex = new EmptyVertex();
        String apiVer = "test";

        namedQueryElementVertex.properties("property-collect-list", "");

        Map<String, Object> result = processor.getNamedQueryExtraDataLookup(transId, fromAppId,
            namedQueryElementVertex, instanceVertex, apiVer);

        assertTrue(result.size() > 0);
    }

    @Test(expected = NullPointerException.class)
    public void collectNQElementHash() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        Map<String, String> currentHash = new HashMap<String, String>();
        ArrayList<String> vidsTraversed = new ArrayList<String>();
        int levelCounter = 1;

        Map<String, String> result = processor.collectNQElementHash(transId, fromAppId,
            thisLevelElemVtx, incomingTrail, currentHash, vidsTraversed, levelCounter);

        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void collectDeleteKeyHash() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        Map<String, String> currentHash = new HashMap<String, String>();
        ArrayList<String> vidsTraversed = new ArrayList<String>();
        int levelCounter = 1;
        Map<String, Vertex> modConstraintHash = new HashMap<String, Vertex>();
        String overRideModelId = "test";
        String overRideModelVersionId = "test";

        Map<String, String> result = processor.collectDeleteKeyHash(transId, fromAppId,
            thisLevelElemVtx, incomingTrail, currentHash, vidsTraversed, levelCounter,
            modConstraintHash, overRideModelId, overRideModelVersionId);

        assertNotEquals(result, null);
    }

    @Test
    public void getLinkageConnectNodeTypesTest() throws AAIException {
        List<String> linkagePtList = new ArrayList<String>();
        linkagePtList.add("modern\\|testdata\\|");
        Set<String> result = processor.getLinkageConnectNodeTypes(linkagePtList);
        assertNotEquals(result, null);

    }

    @Test(expected = AAIException.class)
    public void getLinkageConnectNodeTypesTest1() throws AAIException {

        Set<String> result1 = processor.getLinkageConnectNodeTypes(null);
        assertNotEquals(result1, null);

        List<String> linkagePtList = new ArrayList<String>();
        linkagePtList.add("moderntestdata");
        Set<String> result = processor.getLinkageConnectNodeTypes(linkagePtList);
        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void collectTopology4ModelVerTest() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        Multimap<String, String> thisMap = ArrayListMultimap.create();
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        Map<String, Vertex> currentHash = new HashMap<String, Vertex>();
        List<String> vidsTraversed = new ArrayList<String>();
        int levelCounter = 1;

        Multimap<String, String> result =
            processor.collectTopology4ModelVer(transId, fromAppId, thisLevelElemVtx, incomingTrail,
                thisMap, vidsTraversed, levelCounter, currentHash, "test", "test");

        assertNotEquals(result, null);
    }

    @Test(expected = AAIException.class)
    public void check4EdgeRuleTest() throws AAIException {
        processor.check4EdgeRule("test", "test");
    }

    @Test(expected = AAIException.class)
    public void collectTopology4LinkagePointTest() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        String linkagePointStrVal = "test";
        String incomingTrail = "test";
        Multimap<String, String> currentMap = ArrayListMultimap.create();

        Multimap<String, String> result = processor.collectTopology4LinkagePoint(transId, fromAppId,
            linkagePointStrVal, incomingTrail, currentMap);
        assertNotEquals(result, null);

    }

    @Test(expected = AAIException.class)
    public void getNextStepElementsFromSet() throws AAIException {
        Vertex constrElemSetVtx = new EmptyVertex();
        constrElemSetVtx.<String>property(AAIProperties.NODE_TYPE);
        Map<String, Object> result = processor.getNextStepElementsFromSet(constrElemSetVtx);
        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void genTopoMap4NamedQTest() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        Vertex queryVertex = new EmptyVertex();
        String namedQueryUuid = "E44533334343";
        Multimap<String, String> result =
            processor.genTopoMap4NamedQ(transId, fromAppId, queryVertex, namedQueryUuid);
        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void collectTopology4NamedQTest() throws AAIException {
        String transId = "test";
        String fromAppId = "test";
        Multimap<String, String> thisMap = ArrayListMultimap.create();
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        List<String> vidsTraversed = new ArrayList<String>();
        int levelCounter = 1;

        Multimap<String, String> result = processor.collectTopology4NamedQ(transId, fromAppId,
            thisLevelElemVtx, incomingTrail, thisMap, vidsTraversed, levelCounter);
        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void getModelThatNqElementRepresentsTest() throws AAIException {
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        Vertex vertex = processor.getModelThatNqElementRepresents(thisLevelElemVtx, incomingTrail);
        assertNotEquals(vertex, null);
    }

    @Test(expected = NullPointerException.class)
    public void getModelGivenModelVer() throws AAIException {
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        Vertex vertex = processor.getModelGivenModelVer(thisLevelElemVtx, incomingTrail);
        assertNotEquals(vertex, null);
    }

    @Test(expected = AAIException.class)
    public void getModelTypeFromModel() throws AAIException {
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        String vertex = processor.getModelTypeFromModel(thisLevelElemVtx, incomingTrail);
        assertNotEquals(vertex, null);
    }

    @Test(expected = NullPointerException.class)
    public void getModelTypeFromModelVer() throws AAIException {
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        String vertex = processor.getModelTypeFromModelVer(thisLevelElemVtx, incomingTrail);
        assertNotEquals(vertex, null);
    }

    @Test(expected = NullPointerException.class)
    public void getModelElementStepName() throws AAIException {
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        String vertex = processor.getModelElementStepName(thisLevelElemVtx, incomingTrail);
        assertNotEquals(vertex, null);
    }

    @Test(expected = AAIException.class)
    public void nodeTypeSupportsPersona() throws AAIException {
        String incomingTrail = "";
        boolean vertex = processor.nodeTypeSupportsPersona(incomingTrail);
        assertFalse(vertex);

        incomingTrail = "test";
        boolean vertex1 = processor.nodeTypeSupportsPersona(incomingTrail);
        assertTrue(vertex1);
    }

    @Test(expected = NullPointerException.class)
    public void getNqElementWidgetType() throws AAIException {
        String appId = "test";
        String transID = "test";
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        String vertex1 =
            processor.getNqElementWidgetType(appId, transID, thisLevelElemVtx, incomingTrail);
        assertNotEquals(vertex1, null);
    }

    @Test(expected = NullPointerException.class)
    public void getModElementWidgetType() throws AAIException {
        Vertex thisLevelElemVtx = new EmptyVertex();
        String incomingTrail = "test";
        String vertex1 = processor.getModElementWidgetType(thisLevelElemVtx, incomingTrail);
        assertNotEquals(vertex1, null);
    }

    @Test(expected = NullPointerException.class)
    public void getNodeUsingUniqueId() throws AAIException {
        String appId = "test";
        String transID = "test";
        String nodeType = "generic-vnf";
        String idPropertyName = "test";
        String uniqueIdVal = "test";
        Vertex vertex1 =
            processor.getNodeUsingUniqueId(transID, appId, nodeType, idPropertyName, uniqueIdVal);
        assertNotEquals(vertex1, null);
    }

    @Test(expected = AAIException.class)
    public void getNodeUsingUniqueIdNull() throws AAIException {
        String appId = "test";
        String transID = "test";
        String nodeType = "generic-vnf";
        String idPropertyName = "test";
        String uniqueIdVal = "";
        Vertex vertex1 = null;
        vertex1 =
            processor.getNodeUsingUniqueId(transID, appId, nodeType, idPropertyName, uniqueIdVal);
        assertNotEquals(vertex1, null);

    }

    @Test(expected = AAIException.class)
    public void getNodeUsingUniqueIdNull1() throws AAIException {
        String appId = "test";
        String transID = "test";
        String nodeType = "generic-vnf";
        String idPropertyName = "";
        String uniqueIdVal = "test";
        Vertex vertex1 = null;
        vertex1 =
            processor.getNodeUsingUniqueId(transID, appId, nodeType, idPropertyName, uniqueIdVal);
        assertNotEquals(vertex1, null);

    }

    @Test(expected = AAIException.class)
    public void getNodeUsingUniqueIdNull2() throws AAIException {
        String appId = "test";
        String transID = "test";
        String nodeType = "";
        String idPropertyName = "test";
        String uniqueIdVal = "test";
        Vertex vertex1 = null;
        vertex1 =
            processor.getNodeUsingUniqueId(transID, appId, nodeType, idPropertyName, uniqueIdVal);
        assertNotEquals(vertex1, null);

    }

    @Test(expected = NullPointerException.class)
    public void getModelVersUsingName() throws AAIException {
        String appId = "test";
        String transID = "test";
        String modelName = "test";

        List<Vertex> result = processor.getModelVersUsingName(transID, appId, modelName);
        assertNotEquals(result, null);
    }

    @Test(expected = AAIException.class)
    public void getModelVersUsingNameNull() throws AAIException {
        String appId = "test";
        String transID = "test";
        String modelName = "";

        List<Vertex> result = processor.getModelVersUsingName(transID, appId, modelName);
        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void getModVersUsingModelInvId() throws AAIException {
        String appId = "test";
        String transID = "test";
        String modelName = "test";

        Iterator<Vertex> result = processor.getModVersUsingModelInvId(transID, appId, modelName);
        assertNotEquals(result, null);
    }

    @Test(expected = AAIException.class)
    public void getModVersUsingModelInvIdNull() throws AAIException {
        String appId = "test";
        String transID = "test";
        String modelName = "";

        Iterator<Vertex> result = processor.getModVersUsingModelInvId(transID, appId, modelName);
        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void getModVersUsingModel() throws AAIException {
        String appId = "test";
        String transID = "test";
        String modelName = "test";
        Vertex thisLevelElemVtx = new EmptyVertex();
        List<Vertex> result = processor.getModVersUsingModel(transID, appId, thisLevelElemVtx);
        assertNotEquals(result, null);
    }

    @Test(expected = AAIException.class)
    public void getModVersUsingModel1() throws AAIException {
        String appId = "test";
        String transID = "test";

        Vertex thisLevelElemVtx = null;
        List<Vertex> result = processor.getModVersUsingModel(transID, appId, thisLevelElemVtx);
        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void getModelVerIdsUsingName() throws AAIException {
        String appId = "test";
        String transID = "test";

        String modelName = "test";
        List<String> result = processor.getModelVerIdsUsingName(transID, appId, modelName);
        assertNotEquals(result, null);
    }

    @Test(expected = AAIException.class)
    public void getModelVerIdsUsingName1() throws AAIException {
        String appId = "test";
        String transID = "test";

        String modelName = "";
        List<String> result = processor.getModelVerIdsUsingName(transID, appId, modelName);
        assertNotEquals(result, null);
    }

    @Test(expected = NullPointerException.class)
    public void validateModel() throws AAIException {
        String appId = "test";
        String transID = "test";

        String modelVersionId = "test";
        String modelName = "test";
        processor.validateModel(transID, appId, modelName, modelVersionId);

    }

}
