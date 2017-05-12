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

package org.openecomp.aai.dbgraphmap;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;

import org.openecomp.aai.dbgen.DbMeth;
import org.openecomp.aai.dbgraphgen.DbEdgeGroup;
import org.openecomp.aai.domain.model.AAIResources;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.extensions.AAIExtensionMap;
import org.openecomp.aai.util.AAIConfig;
import org.openecomp.aai.util.AAIConstants;
import org.openecomp.aai.util.RestURL;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TitanVertex;

public class RelationshipGraph {
	
	/**
	 * this method processes the one relationship for the startVertex that is
	 * sent.
	 *
	 * @param g the g
	 * @param startVertex the start vertex
	 * @param jaxbContext the jaxb context
	 * @param rel the rel
	 * @param aaiExtMap the aai ext map
	 * @throws AAIException the AAI exception
	 */
	public static void updRelationship(TitanTransaction g, TitanVertex startVertex, 
										DynamicJAXBContext jaxbContext,
										DynamicEntity rel,
										AAIExtensionMap aaiExtMap)
		throws AAIException {
		String apiVersion = aaiExtMap.getApiVersion();
		String transId = aaiExtMap.getTransId();
		String fromAppId = aaiExtMap.getFromAppId();
		MultiValueMap relatedNodesMap = new MultiValueMap();
		
		if( rel != null ){
				HashMap<String, Object> propFilterHash = new HashMap<String, Object>();
				poplatePropertyHashWithRelData(rel, apiVersion, propFilterHash);
				String relNodeType = (String)rel.get("relatedTo");
				relatedNodesMap.put(relNodeType, propFilterHash);
		}
		DbEdgeGroup.replaceEdgeGroup(transId, fromAppId, g, startVertex,
						"ONLY_PASSED_COUSINS_REL", relatedNodesMap, apiVersion);

	}

	/**
	 * Poplate property hash with rel data.
	 *
	 * @param rel the rel
	 * @param apiVersion the api version
	 * @param propFilterHash the prop filter hash
	 * @throws AAIException the AAI exception
	 */
	private static void poplatePropertyHashWithRelData(DynamicEntity rel, String apiVersion,
			HashMap<String, Object> propFilterHash) throws AAIException {
		
		for( DynamicEntity relData: (List<DynamicEntity>)rel.get("relationshipData")) {
			String prop = ((String)relData.get("relationshipKey")).toLowerCase().trim();
			propFilterHash.put(prop, ((String)relData.get("relationshipValue")).trim()); 
		} 
	}

	/**
	 * this method gets any relationships for the startVertex being processed
	 * and sets the related-link.
	 *
	 * @param g the g
	 * @param startVertex the start vertex
	 * @param apiVersion the api version
	 * @param aaiExtMap the aai ext map
	 * @return the relationships
	 * @throws AAIException the AAI exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static DynamicEntity getRelationships(TitanTransaction g, TitanVertex startVertex, 
												String apiVersion, AAIExtensionMap aaiExtMap)
			throws AAIException, UnsupportedEncodingException {
		
		DynamicType relationshipsType = null;
		DynamicType relationshipType = null;
		DynamicType relationshipDataType = null;
		DynamicType relatedToPropertyType = null;
		
		Boolean setRelatedToProperty = true;
		
		AAIResources aaiResources = org.openecomp.aai.ingestModel.IngestModelMoxyOxm.aaiResourceContainer
				.get(apiVersion);

		DynamicJAXBContext jaxbContext = aaiResources.getJaxbContext();
				
		//String apiVersion = aaiExtMap.getApiVersion();
		String transId = aaiExtMap.getTransId();
		String fromAppId = aaiExtMap.getFromAppId();
		
		HashMap <String, String>      vidToNodeTypeHash = new HashMap <String, String>();
		HashMap <String, TitanVertex> vidToVertexHash   = new HashMap <String, TitanVertex>();
		
		if ("v2".equals( apiVersion)) { 
			relationshipsType = jaxbContext.getDynamicType("inventory.aai.openecomp.org.RelationshipList");
			relationshipType = jaxbContext.getDynamicType(".org.Relationship");
			relationshipDataType = jaxbContext.getDynamicType("inventory.aai.openecomp.org.RelationshipData");
			setRelatedToProperty = false;
		} else { 
			relationshipsType = jaxbContext.getDynamicType("inventory.aai.openecomp.org." + apiVersion + ".RelationshipList");
			relationshipType = jaxbContext.getDynamicType("inventory.aai.openecomp.org." + apiVersion + ".Relationship");
			relationshipDataType = jaxbContext.getDynamicType("inventory.aai.openecomp.org." + apiVersion + ".RelationshipData");
			relatedToPropertyType = jaxbContext.getDynamicType("inventory.aai.openecomp.org." + apiVersion + ".RelatedToProperty");
			if (relatedToPropertyType == null) { 
				setRelatedToProperty = false; // some versions do not support this
			}
		}
		
		DynamicEntity relationships = relationshipsType.newDynamicEntity();
		List<DynamicEntity> listOfRelationships = new ArrayList<DynamicEntity>();
			
		DbEdgeGroup.getEdgeGroup(transId,
				fromAppId, 
				g, 
				startVertex, 
				vidToNodeTypeHash,
				vidToVertexHash,
				"ONLY_COUSIN_REL", 
				AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		// Convert the found relationships to a RelationshipList DynamicEntity
		for( Map.Entry<String, TitanVertex> entry : vidToVertexHash.entrySet() ){
			
			List<DynamicEntity> relationshipDataList = new ArrayList<DynamicEntity>();
			List<DynamicEntity> relatedToPropertyList = new ArrayList<DynamicEntity>();
			
			DynamicEntity relationship = relationshipType.newDynamicEntity();
			
			TitanVertex relNode = entry.getValue();
			String relNodeVid = entry.getKey();
			String relNodeType = vidToNodeTypeHash.get(relNodeVid);
			String relNodeURL = RestURL.get(g, relNode, apiVersion);
			
			HashMap <String, Object> nodeKeyPropsHash = RestURL.getKeyHashes(g, relNode, AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
			Iterator  <Map.Entry<String,Object>>keyIterator = nodeKeyPropsHash.entrySet().iterator();
			while( keyIterator.hasNext() ){
				DynamicEntity relationshipData = relationshipDataType.newDynamicEntity();
				Map.Entry <String,Object>pair = (Map.Entry<String,Object>)keyIterator.next();
				String key = pair.getKey();
				
				if (!key.contains(".")) {
					key = relNodeType + "." + key;
				}
				
				String value = "";
				if( pair.getValue() != null ){
					value = pair.getValue().toString();
				}
				
				relationshipData.set("relationshipKey", key);
				relationshipData.set("relationshipValue", value);
	
				relationshipDataList.add(relationshipData);
			}
			
			if (setRelatedToProperty) {
				HashMap <String, Object> nodeNamePropsHash = DbMeth.getNodeNamePropHash(transId, fromAppId, g, relNode, AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
				Iterator  <Map.Entry<String,Object>>nameIterator = nodeNamePropsHash.entrySet().iterator();
				while( nameIterator.hasNext() ){
					DynamicEntity relatedToProperty = relatedToPropertyType.newDynamicEntity();
					Map.Entry <String,Object>pair = (Map.Entry<String,Object>)nameIterator.next();
					String key = pair.getKey();
					
					if (!key.contains(".")) {
						key = relNodeType + "." + key;
					}
					
					String value = "";
					if( pair.getValue() != null ){
						value = pair.getValue().toString();
					}
					relatedToProperty.set("propertyKey", key);
					relatedToProperty.set("propertyValue", value);
	
					relatedToPropertyList.add(relatedToProperty);
	
				}
				relationship.set("relatedToProperty", relatedToPropertyList);
			}
			relationship.set("relatedTo", relNodeType);
			relationship.set("relatedLink", relNodeURL);
			relationship.set("relationshipData", relationshipDataList);

			listOfRelationships.add(relationship);
		}
		relationships.set("relationship",  listOfRelationships);
		return relationships;
	}

	/**
	 * this method processes any relationships for the startVertex being
	 * processed.
	 *
	 * @param g the g
	 * @param startVertex the start vertex
	 * @param jaxbContext the jaxb context
	 * @param relationshipList the relationship list
	 * @param aaiExtMap the aai ext map
	 * @throws AAIException the AAI exception
	 */
	public static void updRelationships(TitanTransaction g, TitanVertex startVertex, 
										DynamicJAXBContext jaxbContext,
										DynamicEntity relationshipList,
										AAIExtensionMap aaiExtMap) 
	throws AAIException {
		 
			String apiVersion = aaiExtMap.getApiVersion();
			String transId = aaiExtMap.getTransId();
			String fromAppId = aaiExtMap.getFromAppId();
			MultiValueMap relatedNodesMap = new MultiValueMap();
			if (relationshipList != null) { 
				if( relationshipList.get("relationship") != null ){
					List <DynamicEntity> relListTmp = relationshipList.get("relationship");
					for( DynamicEntity rel: relListTmp) {
						HashMap<String, Object> propFilterHash = new HashMap<String, Object>();
						poplatePropertyHashWithRelData(rel, apiVersion, propFilterHash);
						String relNodeType = (String)rel.get("relatedTo");
						relatedNodesMap.put(relNodeType, propFilterHash);
						

					}
				}
				DbEdgeGroup.replaceEdgeGroup(transId, fromAppId, g, startVertex,
						"ALL_COUSIN_REL", relatedNodesMap, apiVersion);
			}
	}

	/**
	 * this method deletes the relationship sent in for the startVertex being
	 * processed.
	 *
	 * @param g the g
	 * @param startVertex the start vertex
	 * @param jaxbContext the jaxb context
	 * @param rel the rel
	 * @param aaiExtMap the aai ext map
	 * @throws AAIException the AAI exception
	 */
	public static void delRelationship(TitanTransaction g, TitanVertex startVertex, 
				DynamicJAXBContext jaxbContext,
				DynamicEntity rel,
				AAIExtensionMap aaiExtMap) 
	throws AAIException {

		String apiVersion = aaiExtMap.getApiVersion();
		String transId = aaiExtMap.getTransId();
		String fromAppId = aaiExtMap.getFromAppId();
		MultiValueMap relatedNodesMap = new MultiValueMap();
		
		if( rel != null ){
			HashMap<String, Object> propFilterHash = new HashMap<String, Object>();
			poplatePropertyHashWithRelData(rel, apiVersion, propFilterHash);
			String relNodeType = (String)rel.get("relatedTo");
			relatedNodesMap.put(relNodeType, propFilterHash);
	}

		DbEdgeGroup.deleteEdgeGroup(transId, fromAppId, g, startVertex,
				relatedNodesMap, apiVersion);

	}

}
