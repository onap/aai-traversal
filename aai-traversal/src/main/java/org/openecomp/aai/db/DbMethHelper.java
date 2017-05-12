/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.aai.db;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.introspection.Introspector;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.exceptions.AAIUnknownObjectException;
import org.openecomp.aai.parsers.query.QueryParser;
import org.openecomp.aai.parsers.relationship.RelationshipToURI;
import org.openecomp.aai.query.builder.QueryBuilder;
import org.openecomp.aai.serialization.engines.TransactionalGraphEngine;

public class DbMethHelper {

	private final Loader loader;
	private final TransactionalGraphEngine engine;
	
	protected DbMethHelper() {
		this.loader = null;
		this.engine = null;
	}
	public DbMethHelper(Loader loader, TransactionalGraphEngine engine) {
		this.loader = loader;
		this.engine = engine;
	}
	/**
	 * 
	 * @param type
	 * @param map - form [{type}.{propname}:{value}]
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws AAIException
	 */
	public Optional<Vertex> searchVertexByIdentityMap(String type, Map<String, Object> map) throws AAIException {
		
		Introspector relationship = constructRelationship(type, map);
		RelationshipToURI parser;
		URI uri;
		QueryParser queryParser;
		try {
			parser = new RelationshipToURI(loader, relationship);
			uri = parser.getUri();
			queryParser = this.engine.getQueryBuilder().createQueryFromURI(uri);
		} catch (UnsupportedEncodingException e) {
			throw new AAIException("AAI_3000");
		}
		
		List<Vertex> results = queryParser.getQueryBuilder().toList();
		
		return reduceToSingleVertex(results, map);
	}
	
	/**
	 * @param type
	 * @param map - form [{propname}:{value}]
	 * @return
	 * @throws AAIException
	 */
	public Optional<Vertex> locateUniqueVertex(String type, Map<String, Object> map) throws AAIException {
	
		return reduceToSingleVertex(locateUniqueVertices(type, map), map);
	}
	
	public List<Vertex> locateUniqueVertices(String type, Map<String, Object> map) throws AAIException {
		Introspector obj = this.createIntrospectorFromMap(type, map);
		QueryBuilder builder = this.engine.getQueryBuilder().exactMatchQuery(obj);
		
		return builder.toList();
	}
	private Introspector constructRelationship(String type, Map<String, Object> map) throws AAIUnknownObjectException {
		final Introspector relationship = loader.introspectorFromName("relationship");
		relationship.setValue("related-to", type);
		final List<Object> data = relationship.getValue("relationship-data");
		for (Entry<String, Object> entry : map.entrySet()) {
			final Introspector dataObj = loader.introspectorFromName("relationship-data");
			dataObj.setValue("relationship-key", entry.getKey());
			dataObj.setValue("relationship-value", entry.getValue());
			data.add(dataObj.getUnderlyingObject());
		}
		
		return relationship;
	}
	
	private Introspector createIntrospectorFromMap(String targetNodeType, Map<String, Object> propHash) throws AAIUnknownObjectException {
		final Introspector result = loader.introspectorFromName(targetNodeType);
		for (Entry<String, Object> entry : propHash.entrySet()) {
			result.setValue(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	private Optional<Vertex> reduceToSingleVertex(List<Vertex> vertices, Map<String, Object> map) throws AAIException {
		if (vertices.isEmpty()){
			return Optional.empty();
		} else if (vertices.size() > 1) {
			throw new AAIException("AAI_6112", "More than one Node found by getUniqueNode for params: " + map);
		}
		
		return Optional.of(vertices.get(0));
	}
	public List<String> getVertexProperties(Vertex v) {
		List<String> retArr = new ArrayList<>();
		if( v == null ){
			retArr.add("null Node object passed to showPropertiesForNode()\n");
		}
		else {
			String nodeType;
			Object ob = v.<Object>property("aai-node-type").orElse(null);
			if( ob == null ){
				nodeType = "null";
			}
			else{
				nodeType = ob.toString();
			}
			
			retArr.add(" AAINodeType/VtxID for this Node = [" + nodeType + "/" + v.id() + "]");
			retArr.add(" Property Detail: ");
			Iterator<VertexProperty<Object>> pI = v.properties();
			while( pI.hasNext() ){
				VertexProperty<Object> tp = pI.next();
				Object val = tp.value();
				retArr.add("Prop: [" + tp.key() + "], val = [" + val + "] ");
			}
		}
		return retArr;
	}
}
