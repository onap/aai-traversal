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

package org.openecomp.aai.dbgraphgen;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.openecomp.aai.dbgen.DbMeth;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.exceptions.AAIExceptionWithInfo;
import org.openecomp.aai.ingestModel.DbMaps;
import org.openecomp.aai.ingestModel.IngestModelMoxyOxm;
import org.openecomp.aai.serialization.db.EdgeRule;
import org.openecomp.aai.serialization.db.EdgeRules;
import org.openecomp.aai.serialization.db.EdgeType;
import org.openecomp.aai.serialization.db.MultiplicityRule;
import org.openecomp.aai.serialization.db.exceptions.NoEdgeRuleFoundException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TitanVertex;

public class DbEdgeGroup {
	
	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(DbEdgeGroup.class);
	
	/**
	 * Replace edge group.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param startVert the start vert
	 * @param scope the scope
	 * @param relatedNodesMultiMap the related nodes multi map
	 * @param apiVersion the api version
	 * @throws AAIException the AAI exception
	 */
	public static void replaceEdgeGroup( String transId, 
										String fromAppId, 
										TitanTransaction graph,  
										TitanVertex startVert, 
										String scope, 
										MultiValueMap relatedNodesMultiMap, // supports multiple entries for same nodetype
										String apiVersion ) throws AAIException{

	// --------------------------------------------------------------------
	// NOTE --- This routine is only used for "cousin" relationships. 
	// --------------------------------------------------------------------

	/*
	 *  scope can be one of these:  
	 *    a) "ALL_COUSIN_REL"  
	 *    b) "ONLY_PASSED_COUSINS_REL" (only look at the edge types that are
	 *                                 represented in the passed list of relationships)
	 *        
	 *   Given a startNode and a list of relationshipInfo, we need to check the database and then, 
	 *   1) Delete any in-scope db relationships which are not represented in the relationshipList.
	 *      So, for ALL_COUSIN_REL, we would delete any db relationship that had an edge with
	 *         parentOf = false if it was not represented in the passed Relationship List.
	 *      For ONLY_PASSED_COUSINS_REL - we'd do the same as above, but only for edges that match the 
	 *         type in the passed relationshipList.  We'd leave any others alone.
	 *   2) Then, Persist (add/update) all the remaining passed-in relationships.        
	 */

	if( !scope.equals("ALL_COUSIN_REL") && !scope.equals("ONLY_PASSED_COUSINS_REL")  ){
		throw new AAIException("AAI_6120", "Illegal scope parameter passed: [" + scope + "]."); 
	}

	HashMap <String,String> vidToNodeTypeInDbHash = new HashMap <String,String>();
	HashMap <String,String> nodeTypeInReqHash = new HashMap <String,String>();
	HashMap <String,TitanEdge> vidToEdgeInDbHash = new HashMap <String,TitanEdge>();
	HashMap <String,TitanVertex> vidToTargetVertexHash = new HashMap <String,TitanVertex>();

	//------------------------------------------------------------------------------------------------------------
	// 1) First -- look what is currently in the db -- 
	//        "cousins" => grab all nodes connected to startVertex that have edges with param: isParent = false.
	//------------------------------------------------------------------------------------------------------------
	GraphTraversalSource conPipeTraversal = startVert.graph().traversal();
	GraphTraversal<Vertex, Edge> conPipe = conPipeTraversal.V(startVert).bothE().has("isParent",false);
	// Note - it's ok if nothing is found
	if( conPipe != null ){
		while( conPipe.hasNext() ){
			TitanEdge ed = (TitanEdge) conPipe.next();
			TitanVertex cousinV = ed.otherVertex(startVert);
			String vid = cousinV.id().toString();
			String noTy = cousinV.<String>property("aai-node-type").orElse(null);
			vidToNodeTypeInDbHash.put(vid, noTy);
			vidToEdgeInDbHash.put(vid, ed);

			LOGGER.info("Found connected cousin vid(s) in db: [" + cousinV.id().toString() + "]");
		}
	}

	//------------------------------------------------------------------------------------------------------------
	//2) Get a List of the Titan nodes that the end-state list wants to be connected to		
	//------------------------------------------------------------------------------------------------------------
	ArrayList <TitanVertex> targetConnectedToVertList = new ArrayList<TitanVertex>();		
	if( relatedNodesMultiMap != null ) {
		
        Set entrySet = relatedNodesMultiMap.entrySet();
        Iterator it = entrySet.iterator();
        //System.out.println("  Object key  Object value");
        while (it.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) it.next();
			String rel2Nt = (String) mapEntry.getKey();
			int i = 0;
			ArrayList <HashMap<String, Object>> propList = ((ArrayList<HashMap<String, Object>>)relatedNodesMultiMap.get(rel2Nt));
			while (i < propList.size()) {
				HashMap<String, Object> propFilterHash = (HashMap<String, Object>) propList.get(i++);
				
				TitanVertex targetVert;
				
				try {
					targetVert = DbMeth.getUniqueNodeWithDepParams( transId, 
																	fromAppId, 
																	graph, 
																	rel2Nt, 
																	propFilterHash, 
																	apiVersion );
				} catch (AAIException e) {
					if (e.getErrorObject().getErrorCode().equals("6114"))
						throw new AAIExceptionWithInfo("AAI_6129", 
														e, 
														"Node of type " + rel2Nt + " not found for properties:" + propFilterHash.toString(),
														propFilterHash,
														rel2Nt);
					else 
						throw e;
				}
			
				targetConnectedToVertList.add(targetVert);
	
				String vid = targetVert.id().toString();
				String noTy = targetVert.<String>property("aai-node-type").orElse(null);
				nodeTypeInReqHash.put(noTy, "");
				vidToTargetVertexHash.put(vid, targetVert);
	
				LOGGER.info("They request edges to these vids:[" + targetVert.id().toString() + "]");
			}
        }
	}

	//-------------------------------------------------------------------------------------------------------------------
	// 3) Compare what is in the DB with what they are requesting as an end-state. 
	//    If any are found in the db-list but not the new-list, delete them from the db (conditionally - based on scope)
	//-------------------------------------------------------------------------------------------------------------------
	String startVtxNT = startVert.<String>property("aai-node-type").orElse(null);
	for( Map.Entry<String, TitanEdge> entry : vidToEdgeInDbHash.entrySet() ){
		String vertId = entry.getKey();
		TitanEdge dbEd = entry.getValue();
		if( ! vidToTargetVertexHash.containsKey(vertId) ){    
			if( scope.equals("ALL_COUSIN_REL") ){
				LOGGER.info("We will DELETE existing DB-edge to vids = [" + entry.getKey() + "]");
				DbMeth.removeAaiEdge(transId, fromAppId, graph, dbEd);
			}
			else if( scope.equals("ONLY_PASSED_COUSINS_REL") ){
				// If they use "ONLY_PASSED_COUSINS_REL" scope, they want us to delete an edge ONLY if:
				//      a) this edge is the same type that they passed in (but goes to a different endpoint)
				//  AND b) this additional edge would break the multiplicity edge rule.  
				String ntInDb = vidToNodeTypeInDbHash.get(vertId);
				if( nodeTypeInReqHash.containsKey(ntInDb) && additionalEdgeWouldBreakMultEdgeRule(startVtxNT, ntInDb) ){
					LOGGER.info("We will DELETE existing DB-edge to vids = [" + entry.getKey() + "]");
					DbMeth.removeAaiEdge(transId, fromAppId, graph, dbEd);
				}
			}
		}
	}

	//---------------------------------------------------------------
	// 4) add/update (persist) all the relations on the new-list
	//---------------------------------------------------------------
	for( Map.Entry<String, TitanVertex> entry : vidToTargetVertexHash.entrySet() ){
		LOGGER.info("Call persistAaiEdge on edge(s) going to vids = " + "[" + entry.getKey() + "]");
		TitanVertex targV = entry.getValue();
		DbMeth.persistAaiEdge(transId, fromAppId, graph, startVert, targV, apiVersion, "cousin");
	}

	return;

}// End replaceEdgeGroup()


/**
 * Additional edge would break mult edge rule.
 *
 * @param startNodeType the start node type
 * @param endNodeType the end node type
 * @return the boolean
 * @throws AAIException the AAI exception
 */
private static Boolean additionalEdgeWouldBreakMultEdgeRule( String startNodeType, String endNodeType )
	throws AAIException {
	// Return true if a second edge from the startNodeType to the endNodeType would
	// break a multiplicity edge rule.
	// Ie.  Adding an edge to a second tenant (endNode) from a vserver (startNode) node would be flagged by this
	//   if we have our multiplicity rule set up for the "vserver-tenant" edge set up as "Many2One" or if
	//   it was set up the other way, "tenant-vserver" as "One2Many" or even if it was "One2One".  In any of 
	//   those scenarios, the addition of an edge from a particular vserver to an additional tenant node
	//   would break the rule.
	
	EdgeRule edgeRule = null;
	boolean reversed = false;
	
	if (EdgeRules.getInstance().hasEdgeRule(startNodeType, endNodeType)) {
	} else if (EdgeRules.getInstance().hasEdgeRule(endNodeType, startNodeType)) {
		reversed = true;
	}
	
	try {
		edgeRule = EdgeRules.getInstance().getEdgeRule(EdgeType.COUSIN, startNodeType, endNodeType);
	} catch (NoEdgeRuleFoundException e) {
		return false;
	}


	if (edgeRule.getMultiplicityRule().equals(MultiplicityRule.ONE2ONE)) {
		return true;
	} else if (reversed && edgeRule.getMultiplicityRule().equals(MultiplicityRule.ONE2MANY)) {
		return true;
	} else if (!reversed && edgeRule.getMultiplicityRule().equals(MultiplicityRule.MANY2ONE)) {
		return true;
	} else {
		return false;
	}

	
}// end of additionalEdgeWouldBreakMultEdgeRule()

/**
 * Delete edge group.
 *
 * @param transId the trans id
 * @param fromAppId the from app id
 * @param graph the graph
 * @param startVert the start vert
 * @param relatedNodesMultiMap the related nodes multi map
 * @param apiVersion the api version
 * @return void
 * @throws AAIException the AAI exception
 */
public static void deleteEdgeGroup( String transId, 
									String fromAppId, 
									TitanTransaction graph,  
									TitanVertex startVert, 
									MultiValueMap relatedNodesMultiMap, 
									String apiVersion ) throws AAIException{
	// --------------------------------------------------------------------
	// NOTE - This routine is only used for "cousin" relationships. 
	// ALSO - an edge deletion will fail if that edge was needed by
	//        the node on one of the sides for uniqueness.  We're just 
	//        being careful here - so far, I don't think a cousin-edge
	//        is ever used for this kind of dependency.
	// --------------------------------------------------------------------

	HashMap <String,TitanEdge> cousinVidToEdgeInDbHash = new HashMap <String,TitanEdge>();
	String startVertNT = startVert.<String>property("aai-node-type").orElse(null);
	String startVertVid = startVert.id().toString();
	DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(apiVersion);
	Collection <String> startVertDepNTColl =  dbMaps.NodeDependencies.get(startVertNT);

	//-----------------------------------------------------------------------------------------------------
	// Get a list of vertexes that are attached to the startVert as "cousins" and the connecting edges
	//-----------------------------------------------------------------------------------------------------
	GraphTraversalSource conPipeTraversal = startVert.graph().traversal();
	GraphTraversal<Vertex, Edge> conPipe = conPipeTraversal.V(startVert).bothE().has("isParent",false);
	if( conPipe != null ){
		while( conPipe.hasNext() ){
			TitanEdge ed = (TitanEdge) conPipe.next();
			TitanVertex cousinV = ed.otherVertex(startVert);
			String vid = cousinV.id().toString();
			cousinVidToEdgeInDbHash.put(vid, ed);
		}
	}

	//-------------------------------------------------------------
	// 	Look through the Relationship info passed in.
	//  Delete edges as requested if they check-out as cousins.
	//-------------------------------------------------------------
	Boolean isFirst = true;
	String msg = "Deleting edges from vid = " + startVertVid + "(" + startVertNT + "), to these: [";
	if( relatedNodesMultiMap != null ) {			
        Set entrySet = relatedNodesMultiMap.entrySet();
        Iterator it = entrySet.iterator();
        //System.out.println("  Object key  Object value");
        while (it.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) it.next();
			String rel2Nt = (String) mapEntry.getKey();
			HashMap<String, Object> propFilterHash = (HashMap<String, Object>)((ArrayList) relatedNodesMultiMap.get(rel2Nt)).get(0);
			TitanVertex otherEndVert = DbMeth.getUniqueNodeWithDepParams( transId, fromAppId, graph, rel2Nt, propFilterHash, apiVersion ); 
			String otherNT = otherEndVert.<String>property("aai-node-type").orElse(null);
			String reqDelConnectedVid = otherEndVert.id().toString();
			if( !cousinVidToEdgeInDbHash.containsKey(reqDelConnectedVid) ){
				throw new AAIException("AAI_6127", "COUSIN Edge between " + startVertVid + " (" + startVertNT + ") and " + reqDelConnectedVid +
						"(" + otherNT + ") not found. "); 
			}
			else {
				// This was a cousin edge.   But before we delete it, we will check to make
				// sure it doesn't have a unique-dependency issue (need to check in two directions)
				Iterator <String> ntItr1 = startVertDepNTColl.iterator();
				if( ntItr1.hasNext() ){
					while( ntItr1.hasNext() ){
						if( ntItr1.next().equals(otherNT) ){
							throw new AAIException("AAI_6126", "Edge between " + startVertVid + " and " + reqDelConnectedVid +
									" cannot be deleted because of a uniqueness-dependancy between nodeTypes, " +
									startVertNT + " and " + otherNT); 
						}
					}
				}

				Collection <String> depNTColl =  dbMaps.NodeDependencies.get(otherNT);
				Iterator <String> ntItr2 = depNTColl.iterator();
				if( ntItr2.hasNext() ){
					while( ntItr2.hasNext() ){
						if( ntItr2.next().equals(startVertNT) ){
							throw new AAIException("AAI_6126", "Edge between " + startVertVid + " and " + reqDelConnectedVid +
									" cannot be deleted because of a uniqueness-dependancy between nodeTypes: " +
									otherNT + " and " + startVertNT); 
						}
					}
				}

				// It's OK to delete this edge as requested.
				if( ! isFirst ){
					msg = msg + ", ";
				}
				isFirst = false;
				msg = msg + reqDelConnectedVid + "(" + otherNT + ")";
				TitanEdge targetDelEdge = cousinVidToEdgeInDbHash.get(reqDelConnectedVid);
				DbMeth.removeAaiEdge(transId, fromAppId, graph, targetDelEdge);
			}
		}
	}

	msg = msg + "]";

	LOGGER.info(msg);
	
	return;

}// End deleteEdgeGroup()


/**
 * Gets the edge group.
 *
 * @param transId the trans id
 * @param fromAppId the from app id
 * @param graph the graph
 * @param startVert the start vert
 * @param vidToNodeTypeHash the vid to node type hash
 * @param vidToVertexHash the vid to vertex hash
 * @param scope the scope
 * @param apiVersion the api version
 * @return void
 * @throws AAIException the AAI exception
 * @throws UnsupportedEncodingException the unsupported encoding exception
 */
public static void getEdgeGroup( String transId, 
								String fromAppId, 
								TitanTransaction graph,  
								TitanVertex startVert, 
								HashMap <String,String> vidToNodeTypeHash,
								HashMap <String,TitanVertex> vidToVertexHash,
								String scope, 
								String apiVersion ) throws AAIException, UnsupportedEncodingException{

	LOGGER.debug("scope={}", scope);
	LOGGER.debug("vid={}", startVert.id().toString());
	LOGGER.debug("nodetype={}", startVert.property("aai-node-type").orElse(null).toString());

	/*
	 *  scope can be one of these:  
	 *    1) "ONLY_COUSIN_REL"   <-- This is the only one supported for now
	 *    2) "ALL_COUSIN_AND_CHILDREN_REL"
	 *    3) "ONLY_CHILDREN" 
	 *    4) "USES_RESOURCE"
	 *        
	 *   Given a startNode and the scope, we need to return relationships that we find in the DB
	 */

	if( !scope.equals("ONLY_COUSIN_REL") ){
		throw new AAIException("AAI_6120", "Illegal scope parameter passed: [" + scope + "]."); 
	}

	//------------------------------------------------------------------------------------------------------------
	// Grab "first-layer" vertexes from the in the db -- 
	//        "cousins" => grab all nodes connected to startVertex that have edges with param: isParent = false.
	//        "children" => grab nodes via out-edge with isParent = true    (NOT YET SUPPORTED)
	//------------------------------------------------------------------------------------------------------------
	Iterable<Vertex> qResult = startVert.query().has("isParent",false).vertices();
	Iterator <Vertex> resultI = qResult.iterator();

	while( resultI.hasNext() ){
		TitanVertex cousinV = (TitanVertex)resultI.next();
		//showPropertiesForNode( transId, fromAppId, cousinV );

		String vid = cousinV.id().toString();
		String noTy = cousinV.<String>property("aai-node-type").orElse(null);
		vidToNodeTypeHash.put(vid, noTy);
		vidToVertexHash.put(vid, cousinV);

		LOGGER.debug("Found connected cousin vid(s) in db: " + "[" + cousinV.id().toString() + "]");
	}

}// End getEdgeGroup()


}
