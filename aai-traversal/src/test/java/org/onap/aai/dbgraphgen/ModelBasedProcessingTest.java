/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.dbgraphgen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.bazaarvoice.jolt.modifier.DataType;
import com.google.common.collect.Multimap;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.exceptions.MockitoLimitations;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aai.db.DbMethHelper;
import org.onap.aai.db.props.AAIProperties;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.introspection.exceptions.AAIUnknownObjectException;
import org.onap.aai.parsers.exceptions.AAIIdentityMapParseException;
import org.onap.aai.query.builder.GraphTraversalBuilder;
import org.onap.aai.query.builder.QueryBuilder;
import org.onap.aai.query.builder.TraversalQuery;
import org.onap.aai.schema.enums.PropertyMetadata;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.db.EdgeType;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ModelBasedProcessingTest {

	@Mock private static TransactionalGraphEngine dbEngine;
	private static Loader loader;
	@Mock private static DBSerializer serializer;
	@Mock private static TransactionalGraphEngine.Admin admin;
	ModelBasedProcessing mockProcessor;
	@Mock
	private DbMethHelper dbMethHelper;

	@BeforeClass
	public static void configure() throws Exception {
		System.setProperty("AJSC_HOME", ".");
		System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");
		loader = LoaderFactory.createLoaderForVersion(ModelType.MOXY, AAIProperties.LATEST);

	}

	@Before
	public void init() {
		mockProcessor = new ModelBasedProcessing(loader, dbEngine, serializer);

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testPropNameChange1() throws AAIUnknownObjectException {
		String result;
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		result = processor.getPropNameWithAliasIfNeeded("generic-vnf", "model-invariant-id");
		assertEquals("result has -local tacked on the end as it should", "model-invariant-id" + AAIProperties.DB_ALIAS_SUFFIX, result);
		result = processor.getPropNameWithAliasIfNeeded("generic-vnf", "vnf-id");
		assertEquals("result does NOT have -local tacked on the end as it should", "vnf-id", result);
		result = processor.getPropNameWithAliasIfNeeded("generic-vnf", "model-invariant-id" + AAIProperties.DB_ALIAS_SUFFIX);
		assertEquals("property not modified because it already includes the right suffix", "model-invariant-id" + AAIProperties.DB_ALIAS_SUFFIX, result);
	}

	@Mock
	GraphTraversal<Vertex, Vertex> v;
	@Mock
	GraphTraversal<Vertex, Vertex> graphTraversal;
	@Mock
	GraphTraversalSource graphTraversalSource;
	@Mock Iterable <?> uniqVerts;
	List<Vertex> vertexList=new ArrayList<>();
	@Mock Vertex vertex;
	@Mock Vertex vertex1;
	@Mock
	QueryBuilder<Vertex> queryBuilder;

	EdgeType treeType;
	@Test(expected = NullPointerException.class)
	public void getStartNodesAndModVersionIds() throws AAIException{

		vertex.property("model-ver","model-version-id");
		vertex1.property(AAIProperties.NODE_TYPE,"model-ver");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		//vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"model-ver")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("model-ver","model-version-id")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(vertexList);
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		//this.engine.getQueryBuilder(startV).createEdgeTraversal(treeType, startV, loader.introspectorFromName(connectedNodeType));

		queryBuilder.toList().add(vertex);
		Mockito.when(dbEngine.getQueryBuilder(vertex)).thenReturn(queryBuilder);

		Introspector obj=loader.introspectorFromName("model-ver");

		Mockito.when(queryBuilder.createEdgeTraversal(EdgeType.TREE,vertex,obj)).thenReturn(queryBuilder);
		//Vertex result=processor.getNodeUsingUniqueId("9999", "postmen","model-ver","model-version-id","vnf-id-1");


		List<Map<String,Object>> startNodeFilterArrayOfHashes=new ArrayList<>();

		Map<String,String> result1=processor.getStartNodesAndModVersionIds("9999","postmen","vnf-id-1","vnf-id-1",
				"vnf-id","generic-vnf",startNodeFilterArrayOfHashes,"");
	}


	@Test(expected = AAIException.class)
	public void getStartNodesAndModVersionIds1() throws AAIException{

		vertex.property("model-version-id","vnf-id-1");
		vertex1.property(AAIProperties.NODE_TYPE,"model-ver");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		//vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"model-ver")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("model-version-id","vnf-id-1")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(vertexList);
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		//this.engine.getQueryBuilder(startV).createEdgeTraversal(treeType, startV, loader.introspectorFromName(connectedNodeType));

		queryBuilder.toList().add(vertex);
		Mockito.when(dbEngine.getQueryBuilder(vertex)).thenReturn(queryBuilder);

		Introspector obj=loader.introspectorFromName("generic-vnf");
		Mockito.when(queryBuilder.createEdgeTraversal(EdgeType.TREE,vertex,obj)).thenReturn(queryBuilder);
		//Vertex result=processor.getNodeUsingUniqueId("9999", "postmen","model-ver","model-version-id","vnf-id-1");


		List<Map<String,Object>> startNodeFilterArrayOfHashes=new ArrayList<>();

		Map<String,String> result1=processor.getStartNodesAndModVersionIds("9999","postmen","vnf-id-1","vnf-id-1",
				"vnf-id","generic-vnf",startNodeFilterArrayOfHashes,"");
	}

	@Test
	public void  getNodeUsingUniqueIdTest() throws AAIException{
		vertex.property("vnf-id","vnf-id-1");
		vertex1.property(AAIProperties.NODE_TYPE,"generic-vnf");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		//vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"generic-vnf")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("vnf-id","vnf-id-1")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(vertexList);
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		Vertex result=processor.getNodeUsingUniqueId("9999", "postmen","generic-vnf","vnf-id","vnf-id-1");

		assertNotNull(result);
	}

	@Test
	public void  getNodeUsingUniqueIdTest1() throws AAIException{
		vertex.property("named-query-uui","vnf-id-1");
		vertex1.property(AAIProperties.NODE_TYPE,"named-query");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		//vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"named-query")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("named-query-uui","vnf-id-1")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(vertexList);
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		Vertex result=processor.getNodeUsingUniqueId("9999", "postmen","named-query","named-query-uui","vnf-id-1");

		assertNotNull(result);
	}

	@Test(expected = AAIException.class)
	public void getModelVersUsingNameTest() throws  AAIException{

		vertex.property(AAIProperties.NODE_TYPE,"generic-vnf");
		vertex1.property("generic-vnf","generic-vnf");
		graphTraversal.addV(vertex1);
		v.addV(vertex1);
		vertexList.add(vertex);
		vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"model-ver")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("model-name","generic-vnf")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(vertexList);
		//Mockito.when(vertexList.listIterator().hasNext()).thenReturn(true);
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		List<Vertex> result=processor.getModelVersUsingName("9999","postment","generic-vnf");

		assertTrue(result.size()>0);
	}

	//uniqueIdVal  Null Expecting AAI Excpetion
	@Test(expected = AAIException.class)
	public void  getNodeUsingUniqueIdTestNull() throws AAIException{
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		Vertex result=processor.getNodeUsingUniqueId("9999", "postmen","generic-vnf","vnf-id","");


	}

	//idPropertyName   Null Expecting AAI Excpetion
	@Test(expected = AAIException.class)
	public void  getNodeUsingUniqueIdTestNull1() throws AAIException{
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		Vertex result=processor.getNodeUsingUniqueId("9999", "postmen","generic-vnf","","vnf-id-1");


	}

	//idPropertyName   Null Expecting AAI Excpetion
	@Test(expected = AAIException.class)
	public void  getNodeUsingUniqueIdTestNull2() throws AAIException{
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		Vertex result=processor.getNodeUsingUniqueId("9999", "postmen","","vnf-id","vnf-id-1");


	}

	@Test(expected = AAIException.class)
	public void  getNodeUsingUniqueIdTestTwoVertex() throws AAIException{
		vertex.property("vnf-id","vnf-id-1");
		vertex1.property(AAIProperties.NODE_TYPE,"generic-vnf");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"generic-vnf")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("vnf-id","vnf-id-1")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(vertexList);
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		Vertex result=processor.getNodeUsingUniqueId("9999", "postmen","generic-vnf","vnf-id","vnf-id-1");

		assertNotNull(result);
	}

	//uniqVerts Null Expection AAI Exception
	@Test(expected = AAIException.class)
	public void  getNodeUsingUniqueIdTestVertexNull() throws AAIException{
		vertex.property("vnf-id","vnf-id-1");
		vertex1.property(AAIProperties.NODE_TYPE,"generic-vnf");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		//vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"generic-vnf")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("vnf-id","vnf-id-1")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(null);
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		Vertex result=processor.getNodeUsingUniqueId("9999", "postmen","generic-vnf","vnf-id","vnf-id-1");

		assertNotNull(result);
	}

	//uniqVerts Null Expection AAI Exception
	@Test(expected = AAIException.class)
	public void  getNodeUsingUniqueIdTestVertexHasNot() throws AAIException{
		vertex.property("vnf-id","vnf-id-1");
		vertex1.property(AAIProperties.NODE_TYPE,"generic-vnf");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		//vertexList.add(vertex);
		//vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"generic-vnf")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("vnf-id","vnf-id-1")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(vertexList);
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		Vertex result=processor.getNodeUsingUniqueId("9999", "postmen","generic-vnf","vnf-id","vnf-id-1");

		assertNotNull(result);
	}

	@Test(expected = AAIIdentityMapParseException.class)
	public  void runDeleteByModelTest() throws  AAIException{
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		Optional<Vertex> vertex=Optional.empty();
		Map<String,Object> startNodeFilterHash=new HashMap<>();
		startNodeFilterHash.put("related-link.data","relationshipdata");
		startNodeFilterHash.put("generic-vnf.d","relationshipdata");
		Mockito.when(dbMethHelper.searchVertexByIdentityMap("relationship-data",startNodeFilterHash)).thenReturn(vertex);
		Map<String,String> re	=processor.runDeleteByModel("9999","postmen","","relationship-data",startNodeFilterHash,"vnf-id","vnf-id");
		assertNotNull(re);


	}

	@Test(expected = AAIException.class)
	public void getModelGivenModelVerTest() throws AAIException{
		vertex.property("named-query-uuid","vnf-id-1");
		vertex1.property(AAIProperties.NODE_TYPE,"named-query");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		//vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"named-query")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("named-query-uuid","vnf-id-1")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(vertexList);

		QueryBuilder qub=Mockito.mock(QueryBuilder.class);
		qub.toList().addAll(vertexList);
		Mockito.when(dbEngine.getQueryBuilder(Mockito.any(Vertex.class))).thenReturn(queryBuilder);

		Mockito.when(queryBuilder.createEdgeTraversal(Mockito.any(EdgeType.class),Mockito.any(Vertex.class),Mockito.any(Introspector.class))).thenReturn(qub);

		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		Vertex result=processor.getModelGivenModelVer(vertex,"");
		assertNotNull(result);

	}

	@Test(expected = AAIException.class)
	public void queryByNamedQuery_TimedTest() throws  AAIException{
		vertex.property("named-query-uuid","named-query-element");
		vertex1.property(AAIProperties.NODE_TYPE,"named-query");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		//vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"named-query")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("named-query-uuid","named-query-element")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(vertexList);

		QueryBuilder qub=Mockito.mock(QueryBuilder.class);
		qub.toList().addAll(vertexList);
		Mockito.when(dbEngine.getQueryBuilder(Mockito.any(Vertex.class))).thenReturn(queryBuilder);

		Mockito.when(queryBuilder.createEdgeTraversal(Mockito.any(EdgeType.class),Mockito.any(Vertex.class),Mockito.any(Introspector.class))).thenReturn(qub);

		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		List<Map<String,Object>> startNodeFilterArrayOfHashes=new ArrayList<>();
		Map<String,Object> secondaryFilterHash=new HashMap<>();

		List<ResultSet>  res=processor.queryByNamedQuery_Timed("99999","postmen","named-query-element",startNodeFilterArrayOfHashes,"vnf","vnf",
				secondaryFilterHash);


	}

	@Test(expected = AAIException.class)
	public void genTopoMap4NamedQTest() throws  AAIException{
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		vertex.property("named-query-uuid","named-query-element");
		vertex1.property(AAIProperties.NODE_TYPE,"named-query-element");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		QueryBuilder qub=Mockito.mock(QueryBuilder.class);
		qub.toList().addAll(vertexList);
		Mockito.when(dbEngine.getQueryBuilder(Mockito.any(Vertex.class))).thenReturn(queryBuilder);

		Mockito.when(queryBuilder.createEdgeTraversal(Mockito.any(EdgeType.class),Mockito.any(Vertex.class),Mockito.any(Introspector.class))).thenReturn(qub);

		Multimap<String, String> map =processor.genTopoMap4NamedQ("9999","postmen",vertex,"named-query-element");
	}

	@Test(expected = AAIException.class)
	public void genTopoMap4NamedQTest1() throws  AAIException{
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		vertex.property("named-query-uuid","named-query-element");
		vertex1.property(AAIProperties.NODE_TYPE,"named-query-element");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		QueryBuilder qub=Mockito.mock(QueryBuilder.class);
		qub.toList().addAll(vertexList);
		Mockito.when(dbEngine.getQueryBuilder(Mockito.any(Vertex.class))).thenReturn(queryBuilder);

		Mockito.when(queryBuilder.createEdgeTraversal(Mockito.any(EdgeType.class),Mockito.any(Vertex.class),Mockito.any(Introspector.class))).thenReturn(qub);

		Multimap<String, String> map =processor.genTopoMap4NamedQ("9999","postmen",null,"named-query-element");
	}

	@Test(expected = AAIException.class)
	public void getModelThatNqElementRepresentsTest() throws  AAIException{
		vertex.property("model-ver","named-query-element");
		vertex1.property(AAIProperties.NODE_TYPE,"named-query-element");
		graphTraversal.addV(vertex);
		v.addV(vertex1);
		vertexList.add(vertex);
		QueryBuilder qub=Mockito.mock(QueryBuilder.class);
		qub.toList().addAll(vertexList);
		Mockito.when(dbEngine.getQueryBuilder(Mockito.any(Vertex.class))).thenReturn(queryBuilder);

		Mockito.when(queryBuilder.createEdgeTraversal(Mockito.any(EdgeType.class),Mockito.any(Vertex.class),Mockito.any(Introspector.class))).thenReturn(qub);
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		Vertex  v=processor.getModelThatNqElementRepresents(vertex,"g");
	}

	@Test(expected = NullPointerException.class)
	public void getModelTypeFromModel() throws  AAIException{
		Vertex vt=Mockito.mock(Vertex.class);
		vt.property("model-type","named-query-element");
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		String  v=processor.getModelTypeFromModel(vt,"g");
	}

	@Test(expected = AAIException.class)
	public void getModelTypeFromModel1() throws  AAIException{
		Vertex vt=Mockito.mock(Vertex.class);
		vt.property("model-type","named-query-element");
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		String  v=processor.getModelTypeFromModel(null,"g");
	}

	@Test(expected = NullPointerException.class)
	public void getModVersUsingModelInvId() throws  AAIException{

		vertex.property(AAIProperties.NODE_TYPE,"model-invariant-id");
		vertex1.property("model","model-invariant-id");
		graphTraversal.addV(vertex1);
		v.addV(vertex1);
		vertexList.add(vertex);
		vertexList.add(vertex1);
		Mockito.when(dbEngine.asAdmin()).thenReturn(admin);
		Mockito.when(admin.getReadOnlyTraversalSource()).thenReturn(graphTraversalSource);
		Mockito.when(graphTraversalSource.V()).thenReturn(v);
		Mockito.when(v.has(AAIProperties.NODE_TYPE,"model-invariant-id")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.has("model","model-invariant-id")).thenReturn(graphTraversal);
		Mockito.when(graphTraversal.toList()).thenReturn(vertexList);
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		Iterator<Vertex> result=processor.getModVersUsingModelInvId("9999","postment","model");
	}

	@Test(expected = AAIException.class)
	public void getNamedQueryExtraDataLookupTest() throws AAIException{
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		Map<String,Object>  re=processor.getNamedQueryExtraDataLookup("","",null,vertex,
				"");
	}

	@Test(expected = AAIException.class)
	public void getNamedQueryExtraDataLookupTest1() throws AAIException{
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);

		Map<String,Object>  re=processor.getNamedQueryExtraDataLookup("","",vertex,null,
				"");
	}

	@Test(expected = NullPointerException.class)
	public  void showResultSet() throws  AAIException{
		vertex.property("model-ver","model-versionId");
		vertex.property("aai","model-versionId");


		ResultSet rs= Mockito.mock(ResultSet.class);
		Mockito.when(rs.getVert()).thenReturn(vertex);

		List<VertexProperty<String>> vb=new ArrayList<>();
		VertexProperty<String> v=Mockito.mock(VertexProperty.class);
		v.property("model-ver","1");
		vb.add(v);
		v.property("aai","5");
		vb.add(v);
		v.property("source-of-truth","6");
		vb.add(v);

		vertex.properties("model-ver","aai");
		Mockito.when(vertex.<String>property(AAIProperties.NODE_TYPE)).thenReturn(v);
		//Mockito.when(vertex.properties()).thenReturn(Mockito.any());
		ModelBasedProcessing processor = new ModelBasedProcessing(loader, dbEngine, serializer);
		processor.showResultSet(rs,8);

	}
}
