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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;

import org.openecomp.aai.dbmodel.DbEdgeRules;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.ingestModel.DbMaps;
import org.openecomp.aai.ingestModel.IngestModelMoxyOxm;
import org.openecomp.aai.serialization.db.EdgeRule;
import org.openecomp.aai.serialization.db.EdgeRules;
import org.openecomp.aai.util.AAIConfig;
import org.openecomp.aai.util.AAIConstants;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.net.InetAddresses;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TitanVertex;


/**
 * General Database-level Utility class.   These methods deal with the database one dataNode / Edge at a time.
 * Transactions are managed at a higher level by the calling classes by passing in a TitanTransaction object.
 */
public class DbMeth{

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(DbMeth.class);
	
	/**
	 * Patch aai node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propHash the prop hash
	 * @param depNodeVal the dep node val
	 * @param apiVersion the api version
	 * @return TitanVertex
	 * @throws AAIException the AAI exception
	 */
	public static TitanVertex patchAaiNode(String transId, String fromAppId, TitanTransaction graph, String nodeType, 
			HashMap <String,Object> propHash, TitanVertex depNodeVal, String apiVersion ) throws AAIException{
		// If they're calling patchAaiNode, then we only want to add/update the properties that they
		// pass us in the propHash.  If there are others already in the DB, we leave them alone.

		// Note: to be really official, we'd throw an error if the node wasn't already in the db.
		boolean[] objectExists = new boolean[1];
		objectExists[0] = true;
		Boolean patchOnly = true;
		TitanVertex tv = persistAaiNodeBASE(transId, fromAppId, graph, nodeType, propHash, depNodeVal, patchOnly, apiVersion, objectExists);
		return( tv );

	} // end of patchAaiNode()
	
	/**
	 * Patch aai node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propHash the prop hash
	 * @param depNodeVal the dep node val
	 * @return the titan vertex
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static TitanVertex patchAaiNode(String transId, String fromAppId, TitanTransaction graph, String nodeType, 
			HashMap <String,Object> propHash, TitanVertex depNodeVal) throws AAIException{
		return patchAaiNode( transId,  fromAppId,  graph,  nodeType, 
				propHash,  depNodeVal, null );
	}
	
	/**
	 * Persist aai node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propHash the prop hash
	 * @param depNodeVal the dep node val
	 * @param patchOnly the patch only
	 * @param apiVersion the api version
	 * @return the titan vertex
	 * @throws AAIException the AAI exception
	 */
	public static TitanVertex persistAaiNode(String transId, String fromAppId, TitanTransaction graph, String nodeType, 
			HashMap <String,Object> propHash, TitanVertex depNodeVal, Boolean patchOnly, String apiVersion) throws AAIException{
		boolean[] objectExists = new boolean[1];
		objectExists[0] = false;
		return persistAaiNodeBASE( transId,  fromAppId,  graph,  nodeType, 
				 propHash,  depNodeVal, patchOnly, apiVersion, objectExists);
	}
	
	/**
	 * Persist aai node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propHash the prop hash
	 * @param addIfNotFound the add if not found
	 * @param depNodeVal the dep node val
	 * @param apiVersion the api version
	 * @return the titan vertex
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static TitanVertex persistAaiNode(String transId, String fromAppId, TitanTransaction graph, String nodeType, 
			HashMap <String,Object> propHash, Boolean addIfNotFound, TitanVertex depNodeVal, String apiVersion) throws AAIException{
		// If they're calling persistAaiNode, then we want to make the Db look like whatever they pass us.  That is, if
		// there is already a record in the DB, but they do not pass some of the existing properties, they should
		// be cleared from the DB.    Since we want to take care of all properties, we pass patchOnly = false
		Boolean patchOnly = false;
		boolean[] objectExists = new boolean[1];
		objectExists[0] = false;
		TitanVertex tv = persistAaiNodeBASE(transId, fromAppId, graph, nodeType, propHash, depNodeVal, patchOnly, apiVersion, objectExists);
		return( tv );
	} 
	
	/**
	 * Persist aai node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propHash the prop hash
	 * @param addIfNotFound the add if not found
	 * @param depNodeVal the dep node val
	 * @return the titan vertex
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static TitanVertex persistAaiNode(String transId, String fromAppId, TitanTransaction graph, String nodeType, 
			HashMap <String,Object> propHash, Boolean addIfNotFound, TitanVertex depNodeVal) throws AAIException{
		// If they're calling persistAaiNode, then we want to make the Db look like whatever they pass us.  That is, if
		// there is already a record in the DB, but they do not pass some of the existing properties, they should
		// be cleared from the DB.    Since we want to take care of all properties, we pass patchOnly = false
		Boolean patchOnly = false;
		boolean[] objectExists = new boolean[1];
		objectExists[0] = false;
		TitanVertex tv = persistAaiNodeBASE(transId, fromAppId, graph, nodeType, propHash, depNodeVal, patchOnly, null, objectExists);
		return( tv );
	} // end of persistAaiNode()
	
	/**
	 * Persist aai node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propHash the prop hash
	 * @param addIfNotFound the add if not found
	 * @param depNodeVal the dep node val
	 * @param apiVersion the api version
	 * @param objectExists the object exists
	 * @return TitanVertex
	 * @throws AAIException the AAI exception
	 */
	public static TitanVertex persistAaiNode(String transId, String fromAppId, TitanTransaction graph, String nodeType, 
			HashMap <String,Object> propHash, Boolean addIfNotFound, TitanVertex depNodeVal, String apiVersion, boolean[] objectExists) throws AAIException{
		Boolean patchOnly = false;
		// If they're calling persistAaiNode, then we want to make the Db look like whatever they pass us.  That is, if
		// there is already a record in the DB, but they do not pass some of the existing properties, they should
		// be cleared from the DB.    Since we want to take care of all properties, we pass patchOnly = false
		TitanVertex tv = persistAaiNodeBASE(transId, fromAppId, graph, nodeType, propHash, depNodeVal, patchOnly, apiVersion, objectExists, null);
		return( tv );
	} 
	
	/**
	 * Persist aai node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propHash the prop hash
	 * @param addIfNotFound the add if not found
	 * @param depNodeVal the dep node val
	 * @param apiVersion the api version
	 * @param objectExists the object exists
	 * @param thisNodeVertex the this node vertex
	 * @return the titan vertex
	 * @throws AAIException the AAI exception
	 */
	public static TitanVertex persistAaiNode(String transId, String fromAppId, TitanTransaction graph, String nodeType, 
			HashMap <String,Object> propHash, Boolean addIfNotFound, TitanVertex depNodeVal, String apiVersion, boolean[] objectExists, TitanVertex thisNodeVertex) throws AAIException{
		Boolean patchOnly = false;
		// If they're calling persistAaiNode, then we want to make the Db look like whatever they pass us.  That is, if
		// there is already a record in the DB, but they do not pass some of the existing properties, they should
		// be cleared from the DB.    Since we want to take care of all properties, we pass patchOnly = false
		TitanVertex tv = persistAaiNodeBASE(transId, fromAppId, graph, nodeType, propHash, depNodeVal, patchOnly, apiVersion, objectExists, thisNodeVertex);
		return( tv );
	} 
	
	/**
	 * Persist aai node BASE.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propHash the prop hash
	 * @param depNodeVal the dep node val
	 * @param patchOnly the patch only
	 * @param apiVersion the api version
	 * @param objectExists the object exists
	 * @return the titan vertex
	 * @throws AAIException the AAI exception
	 */
	public static TitanVertex persistAaiNodeBASE(String transId, String fromAppId, TitanTransaction graph, String nodeType, 
			HashMap <String,Object> propHash, TitanVertex depNodeVal, Boolean patchOnly, 
			String apiVersion, boolean[] objectExists) throws AAIException{
		return persistAaiNodeBASE(transId, fromAppId, graph, nodeType, propHash, depNodeVal, patchOnly, apiVersion, objectExists, null);
	}
	
	/**
	 * Persist aai node BASE.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propHash the prop hash
	 * @param depNodeVal the dep node val
	 * @param patchOnly the patch only
	 * @param apiVersion the api version
	 * @param objectExists the object exists
	 * @param thisNodeVertex the this node vertex
	 * @return the titan vertex
	 * @throws AAIException the AAI exception
	 */
	public static TitanVertex persistAaiNodeBASE(String transId, String fromAppId, TitanTransaction graph, String nodeType, 
			HashMap <String,Object> propHash, TitanVertex depNodeVal, Boolean patchOnly, 
			String apiVersion, boolean[] objectExists, TitanVertex thisNodeVertex) throws AAIException{
		
		if( graph == null ){
			throw new AAIException("AAI_6101", "null graph object passed to persistAaiNodeBASE()"); 
		}
		
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
		boolean useDepNode = false;
		String resourceVersion = null;
		if( propHash.containsKey("resource-version") ){
			resourceVersion = (String)(propHash.get("resource-version")); 
		}	
		String aaiUniqueKeyVal = null;
		if( propHash.containsKey("aai-unique-key") ){
			// Note -- we are assuming that nobody is monkeying with this.   The 16-07 first-pass theory
			//    is that the REST layer is always gonna generate this or pass it through.
			aaiUniqueKeyVal = (String)(propHash.get("aai-unique-key")); 
			propHash.remove("aai-unique-key");
		}	
		
		if( needsADepNode4Uniqueness(transId, fromAppId, nodeType, apiVersion) ){
			// This kind of node needs a dependent node (for uniqueness)
			if( depNodeVal == null ){
				// They should have passed in the node that this one depends on
				throw new AAIException("AAI_6109", "null dependentNode object passed to persistAaiNodeBASE() but " + nodeType + " requires one."); 
			}
			else if( ! nodeTypeACanDependOnB(transId, fromAppId, nodeType, depNodeVal.<String>property("aai-node-type").orElse(null), apiVersion) ){
				// They should have passed in the right type of node as the dependent node
				throw new AAIException("AAI_6109", "dependentNode of type " + depNodeVal.<String>property("aai-node-type").orElse(null) + " passed to persistAaiNodeBASE() for nodeType" + nodeType + "."); 
			}
			useDepNode = true;
		}
		else {
			depNodeVal = null;
		}
		
		// Note: as of 1607, we no longer validate property names since that's covered by the REST layer.
		// Same goes for required fields (as of 1602)

		// Special ip-address validation for ipAddr nodes only...   This will go away when we go to YANG and
		// do validations like this up at that layer.
		if( nodeType.equals("ipaddress") ){
			// Note - this will throw an exception if the ipAddress is using a bad format
			ipAddressFormatOK( transId, fromAppId, (String)propHash.get("addr"), (String)propHash.get("version") );
		}

		// Use the key-fields/dependentNode to check if this is an add or an update
		// We assume that all NodeTypes at least one key-property defined.  A dependentNode is optional.
		if( ! dbMaps.NodeKeyProps.containsKey(nodeType) ){
			// Problem if no key Properties defined for this nodeType  
			String defVer = AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP);
			throw new AAIException("AAI_6105", "No node-key-properties defined in dbMaps for nodeType = " + nodeType + " (ver=" + defVer + ")"); 
		}

		Boolean hasAltKey1 = false;
		HashMap <String,Object>nodeAltKey1PropsHash = new HashMap<String,Object>();
		Collection <String> altKey1Props = getNodeAltKey1PropNames(transId, fromAppId, nodeType, apiVersion);
		if( altKey1Props != null ){
			Iterator <String> altKey1PropI = altKey1Props.iterator();
			while( altKey1PropI.hasNext() ){
				String propName = altKey1PropI.next();
				// NOTE: alt-keys are not always required fields.  If it is null or blank, we won't 
				//      do alt-key checks on it.
				Object value = propHash.get(propName); 
				if( value != null && !value.toString().equals("") ){
					hasAltKey1 = true;
					nodeAltKey1PropsHash.put(propName, value);
				}
			}
		}
		HashMap <String,Object>nodeKeyPropsHash = new HashMap<String,Object>();
		Collection <String> keyProps = getNodeKeyPropNames(transId, fromAppId, nodeType, apiVersion);
		Iterator <String> keyPropI = keyProps.iterator();
		while( keyPropI.hasNext() ){
			String propName = keyPropI.next();
			
			Object value = propHash.get(propName); 
			nodeKeyPropsHash.put(propName, value);
		}

		// Check if this node is already in the database based on the Primary Key Info
		TitanVertex existingVert = thisNodeVertex;
		boolean foundTheNodeInDb = true;
			
		if (existingVert == null) { 
			try {
				existingVert = getUniqueNode( transId, fromAppId, graph, nodeType, nodeKeyPropsHash, depNodeVal, apiVersion );
			}
			catch (AAIException e) {
				if (e.getErrorObject().getErrorCode().equals("6114")) {
					foundTheNodeInDb = false;
				}
				else {
					throw e;
				}
			}
		}
				
		// this is so the notification knows whether or not the operation was an UPDATE or a CREATe
		objectExists[0] = foundTheNodeInDb;
		if( foundTheNodeInDb ){
			// A record was found in the DB using the PK.  
			if( needToDoResourceVerCheck(apiVersion, patchOnly) ){
				// Need to check that they knew what they were updating
				String existingResVer = existingVert.<String>property("resource-version").orElse(null);
				if( resourceVersion == null || resourceVersion.equals("") ){
					throw new AAIException("AAI_6130", "Resource-version not passed for update of = " + nodeType + ", " + nodeKeyPropsHash.toString()); 
				}
				else if( (existingResVer != null) && !resourceVersion.equals(existingResVer) ){
					throw new AAIException("AAI_6131", "Resource-version " + resourceVersion + " MISMATCH WITH EXISTING " + existingResVer + " for update of = " + nodeType + ", " + nodeKeyPropsHash.toString()); 
				}
			}
			
			// Need to ensure that the Alternate key isn't changing to a value that points to a different existing node.   
			// It is ok if it points to nothing -- that would just be an update for this node.   It's also ok if 
			// it points to this (existing) node - that just means that it wasn't being updated.
			if( hasAltKey1 ){
				try {
					TitanVertex chkVert = getUniqueNode( transId, fromAppId, graph, nodeType, nodeAltKey1PropsHash, depNodeVal, apiVersion );
					if( ! chkVert.id().toString().equals(existingVert.id().toString()) ){
						throw new AAIException("AAI_6117", "In-Use AlternateKey value passed for update of nodeType = " + nodeType); 
					}
				}
				catch (AAIException e) {
					if(! e.getErrorObject().getErrorCode().equals("6114") ){
						throw e;
					}
				}
			}
		}
		else {
			// Note not in the DB -- This will be an ADD of a new node
			//		a) make sure they didn't say they were just doing "patchOnly" which cannot be an ADD.
			// 		b) if there is an alternate key, we need to make sure the AK isn't already in use by somebody else.
			if( patchOnly ){
				String depMsg = "";
				if( useDepNode ){
					depMsg = " plus dependent node. ";
				}
				throw new AAIException("AAI_6114", "Patch Request, but no Node of type " + nodeType + " found for properties: [" + propHash + "] " + depMsg);
			}
			
			if( needToDoResourceVerCheck(apiVersion, patchOnly) && (resourceVersion != null) && !resourceVersion.equals("") ){
				throw new AAIException("AAI_6131", "Resource-version was passed in, but this is an ADD of a " + nodeType + ", with these params: " + nodeKeyPropsHash.toString()); 
			}
			if( hasAltKey1 ){
				try {
					getUniqueNode( transId, fromAppId, graph, nodeType, nodeAltKey1PropsHash, depNodeVal, apiVersion );
					// Since the Primary Key for this nodeType wasn't found in the DB yet, the fact that
					// we are able to find a record (no "6114" exception thrown) using the Alternate-Key is an error.  
					// We can't create a new node that uses an AK that's already in use.
					throw new AAIException("AAI_6117", "Conflicting Key and Alternate-Key values passed for add of nodeType = " + nodeType); 
				}
				catch (AAIException e) {
					if(! e.getErrorObject().getErrorCode().equals("6114") ){
						throw e;
					}
				}
			}
		}

		// ------------- Done with checking.  Do the add or update to the dB -----------------------

		if( foundTheNodeInDb ){
			long unixTimeNow = System.currentTimeMillis() / 1000L;
			// ----- This is an UPDATE ------
			
			
			String existingSourceOfTruth = fromAppId;  // default value if we can't get the old one
			Object tmpOb = existingVert.<Object>property("source-of-truth").orElse(null);
			if( tmpOb != null ){
				existingSourceOfTruth = tmpOb.toString();
			}
			long existingCreateTs = unixTimeNow;  // default value if we can't get the old one
			tmpOb = existingVert.<Object>property("aai-created-ts").orElse(null);
			if( tmpOb != null ){
				existingCreateTs = (long) tmpOb;
			}
			
			String msg = "UPDATE vertex of type = [" + nodeType + "] "; 
			if( useDepNode ){
				String depNType = depNodeVal.<String>property("aai-node-type").orElse(null);
				HashMap <String, Object> depNodePropKeysHash = getNodeKeyPropHash(transId, fromAppId, graph, depNodeVal);
				LOGGER.info("UPDATE existing node: type = " + nodeType + ", key(s) = [" + nodeKeyPropsHash + 
						"] which rides on dependent node: type = " + depNType + ", with key(s) = [" + depNodePropKeysHash + "].");
			}
			else {
				LOGGER.info("UPDATE existing node: type = " + nodeType + ", key(s) = [" + nodeKeyPropsHash + "] (no dep. node).");
			}
			String removeList = "";
			if( ! patchOnly ){
				// They are updating an existing record, and they want us to "process all defined properties" (not just patch)   
				// So we will see if the node has any properties that were not passed-in.  Those need to be removed.
				Collection <String> propCol =  dbMaps.NodeProps.get(nodeType);
				Iterator <String> propIter = propCol.iterator();
				while( propIter.hasNext() ){
					String propName = propIter.next();
					if( ! propHash.containsKey(propName) && !DbEdgeRules.ReservedPropNames.containsKey(propName)){  
						if( thisPropertyWasPutByNewerVersionOfCode(apiVersion, nodeType, propName) ){
							//   we must be using an older version of code here - but the property that
							//   has not been passed in this persist call is one that this older version of
							//   the database did not know about.  So leave it alone.
						}
						else {
							removeList = removeList + "," + propName;
							existingVert.property(propName).remove();
						}
					}
				}
			}
			if( !removeList.equals("") ){
				LOGGER.info("Removed these props on update: [" + removeList + "]");
			}
			for( Map.Entry<String, Object> entry : propHash.entrySet() ){
				// update the parameters that have been passed in (except the key-properties)
				//                  taking away the key-property check.  We will now allow this since
				//                  the keys were used to identify this node, so they should be good and
				//                  there are times when Titan resolves conflicts by only using the 
				//                  data set in an update - and was losing our key info... 
				//                  Similar to the change noted below.
				//if( ! nodeKeyPropsHash.containsKey(entry.getKey()) ){
				//	existingVert.setProperty( entry.getKey(), entry.getValue() );
				//}
				if( ! entry.getKey().equals("resource-version") ){
					boolean nonSingleCardinality = false;
					boolean setSoNoDupes = false;
					if( checkPropCardinality(entry.getKey(), "Set") ){
						nonSingleCardinality = true;
						setSoNoDupes = true;
					}
					else if( checkPropCardinality(entry.getKey(), "List") ){
						nonSingleCardinality = true;
					}
					
					Iterator <Object> valIter = null;
					if( nonSingleCardinality ){
						String className = entry.getValue().getClass().getSimpleName();
						if( className.equals("ArrayList") ){
							valIter = ((ArrayList)(entry.getValue())).iterator();
						}
						else if( className.equals("List") ){
							valIter = ((List)(entry.getValue())).iterator();
						}
						else if( className.equals("Set") ){
							valIter = ((Set)(entry.getValue())).iterator();
						}
					}
					
					if( nonSingleCardinality ){
						// This property has Cardinality of List or Set - which need to be handled carefully
						// Note -- for Lists or Sets, we assume they are of dataType String - that is all
						//       the Rest layer supports at the moment (16-02)
						ArrayList <String> currentData = new ArrayList <String> ();
						if( patchOnly ){
							// When patching - gotta know what's already in the db
							Iterator<VertexProperty<Object>> existingPropsIter =  (existingVert.properties(entry.getKey()));
							if( existingPropsIter != null ){
								while( existingPropsIter.hasNext() ){
									String existingVal = existingPropsIter.next().value().toString();
									currentData.add( existingVal );
								}
							}
						}
						else {
							// Since this is not a patch-update, we first have to clear out what is currently in the db.
							existingVert.property(entry.getKey()).remove();
						}
						
						if( valIter != null ){
							while( valIter.hasNext() ){
								Object thisVal = valIter.next();	
								if( setSoNoDupes ){
									// For Sets, we need to check that the data isn't already in the db or wasn't passed
									// in to us twice in the propHash.  Otherwise Titan throws an exception (instead of just ignoring it...)
									if( !currentData.contains(thisVal) ){
										// We don't have this data yet, so add it to the Set
										existingVert.property( entry.getKey(), thisVal );
										currentData.add( thisVal.toString() );
									}
								}
								else {
									// For List data types, it's ok to have duplicate values in the db (why would we want this?)
									existingVert.property( entry.getKey(), thisVal );
								}
							}
						}
					}
					else {
						// This is a normal, "Cardinality = SINGLE" kind of property
						// ResourceVersion is not populated based on passed-in data, it is set along with other internal properties below.
						//Object cleanVal = convertTypeIfNeeded( entry.getKey(), entry.getValue() );
						//existingVert.setProperty( entry.getKey(),  cleanVal );
						// ********************************
						existingVert.property( entry.getKey(), entry.getValue() );
					}
				}
			}

			// DEBUG - trying to deal with the case where simultaneous PUTs
			//    cause our db to wind up with a vertex that does not have these three properties filled in.
			existingVert.property( "aai-node-type", nodeType );
			existingVert.property( "aai-created-ts", existingCreateTs );
			existingVert.property( "source-of-truth", existingSourceOfTruth );
			
			if( aaiUniqueKeyVal != null ){
				existingVert.property( "aai-unique-key", aaiUniqueKeyVal );
			}
			
			existingVert.property( "aai-last-mod-ts", unixTimeNow );
			String resVers = "" + unixTimeNow; 
			existingVert.property( "resource-version", resVers );
			existingVert.property( "last-mod-source-of-truth", fromAppId );
			
			LOGGER.info(msg + ", [aai-last-mod-ts]/[" + unixTimeNow + "]");
			
			return( existingVert );
		}
		else{ 
			// ----- Not found in the DB, This must be an ADD ------
			if( DbEdgeRules.NodeTypeCategory.containsKey(nodeType) ){
				throw new AAIException("AAI_6120", "nodeTypeCategory " + nodeType + " cannot be used to ADD a node.  Need to pass a valid nodeType"); 
			}

			TitanVertex tiVnew = graph.addVertex( nodeType );

			String msg = "ADD vertex of type = [" + nodeType + "] ";       
			if( depNodeVal != null ){
				String depNType = depNodeVal.<String>property("aai-node-type").orElse(null);
				HashMap <String, Object> depNodePropKeysHash = getNodeKeyPropHash(transId, fromAppId, graph, depNodeVal);
				msg = msg + " onto dependent node: type = " + depNType + ", which has key(s) = [" + depNodePropKeysHash + 
						"].  New Node Prop/values = ";
			}
			else {
				msg = msg + " Note: no dependent node.  New Node Prop/values = ";
			}
			boolean first = true;
			for( Map.Entry<String, Object> entry : propHash.entrySet() ){
				if( ! entry.getKey().equals("resource-version") ){
					if( first ){
						msg = msg + " [" + entry.getKey() + "]/[" + entry.getValue() + "]";
						first = false;
					}
					else {
						msg = msg + ", [" + entry.getKey() + "]/[" + entry.getValue() + "]";
					}
					
					boolean nonSingleCardinality = false;
					boolean setSoNoDupes = false;
					if( checkPropCardinality(entry.getKey(), "Set") ){
						nonSingleCardinality = true;
						setSoNoDupes = true;
					}
					else if( checkPropCardinality(entry.getKey(), "List") ){
						nonSingleCardinality = true;
					}
					
					Iterator <Object> valIter = null;
					if( nonSingleCardinality ){
						String className = entry.getValue().getClass().getSimpleName();
						if( className.equals("ArrayList") ){
							valIter = ((ArrayList)(entry.getValue())).iterator();
						}
						else if( className.equals("List") ){
							valIter = ((List)(entry.getValue())).iterator();
						}
						else if( className.equals("Set") ){
							valIter = ((Set)(entry.getValue())).iterator();
						}
					}
					
					if( nonSingleCardinality ){
						// This property has Cardinality of List or Set - which need to be handled carefully
						ArrayList <String> currentData = new ArrayList <String> ();
						if( valIter != null ){
							while( valIter.hasNext() ){
								Object thisVal = valIter.next();	
								if( setSoNoDupes ){
									// For Sets, we need to check that they're not passing us duplicate data in propHash.
									// Otherwise Titan throws an exception (instead of just ignoring it...)
									if( !currentData.contains(thisVal) ){
										// We don't have this data yet, so add it to the Set
										tiVnew.property( entry.getKey(), thisVal );
										currentData.add( thisVal.toString() );
									}
								}
								else {
									// For List data types, it's ok to have duplicate values in the db (why would we want this?)
									tiVnew.property( entry.getKey(), thisVal );
								}
							}
						}
					}
					else {
						// This is a normal, "Cardinality = SINGLE" kind of property
						// ResourceVersion is not populated based on passed-in data, it is set along with other internal properties below.
						tiVnew.property( entry.getKey(), entry.getValue() );
					}
				}
			}
			
			tiVnew.property( "aai-node-type", nodeType );
			//long unixTime = System.currentTimeMillis() / 1000L;
			long unixTime = System.currentTimeMillis();
			tiVnew.property( "aai-created-ts", unixTime );
			tiVnew.property( "aai-last-mod-ts", unixTime );
			String resVers = "" + unixTime; 
			tiVnew.property( "resource-version", resVers );
			tiVnew.property( "source-of-truth", fromAppId );
			tiVnew.property( "last-mod-source-of-truth", fromAppId );
			if( aaiUniqueKeyVal != null ){
				tiVnew.property( "aai-unique-key", aaiUniqueKeyVal );
			}
			
			LOGGER.info(msg + ", [aai-created-ts]/[" + unixTime + "]");
			return( tiVnew );
		}

	} // end of persistAaiNodeBASE()

	
	/**
	 * Need to do resource ver check.
	 *
	 * @param apiVersion the api version
	 * @param patchOnlyFlag the patch only flag
	 * @return the boolean
	 * @throws AAIException the AAI exception
	 */
	public static Boolean needToDoResourceVerCheck(String apiVersion, Boolean patchOnlyFlag)
			throws AAIException{
		
		if( patchOnlyFlag ){
			// we do not do resource checking for patch requests.
			return false;
		}
		
		String resourceCheckOnFlag = AAIConfig.get(AAIConstants.AAI_RESVERSION_ENABLEFLAG);
		
		int apiVerInt = cleanUpApiVersion(apiVersion);
		
		if( (resourceCheckOnFlag != null) && resourceCheckOnFlag.equals("true") ){
			// Only do the check if the resource enable flag is set to "true"
			if( apiVerInt > 4 ){
				// We're only doing the resource version checks for v5 and later
				return true;
			}
		}
		
		return false;
	}// End needToDoResourceVerCheck()

	
	/**
	 * Clean up api version.
	 *
	 * @param apiVersionString the api version string
	 * @return the int
	 * @throws AAIException the AAI exception
	 */
	private static int cleanUpApiVersion( String apiVersionString ) throws AAIException {
		// Note: we expect an apiVersion to start with the letter "v", followed by an integer. 
		
		int versionInt = 0;
		String verStr = apiVersionString;
		if( (apiVersionString == null) || (apiVersionString.length() < 2) ){
			// Passed in version doesn't look right
			verStr = org.openecomp.aai.util.AAIApiVersion.get();
		}
		versionInt = getVerNumFromVerString( verStr );
		
		return versionInt;
	}
	
	/**
	 * Gets the ver num from ver string.
	 *
	 * @param versionString the version string
	 * @return the ver num from ver string
	 * @throws AAIException the AAI exception
	 */
	private static int getVerNumFromVerString( String versionString )throws AAIException {
		int versionInt = 0;
		if( versionString == null || versionString.length() < 2 ){
			throw new AAIException("AAI_6121", " Bad Version (format) passed to getVerNumFromVerString: [" + versionString + "]."); 
		}
		
		int strLen = versionString.length();
		// We assume that a version looks like "v" followed by an integer
		if( ! versionString.substring(0,1).equals("v") ){
			String detail = " Bad Version (format) passed to getVerNumFromVerString: [" + versionString + "]."; 
			throw new AAIException("AAI_6121", detail); 
		}
		else {
			String intPart = versionString.substring(1,strLen);
			try {
				versionInt = Integer.parseInt( intPart );
			}
			catch( Exception e ){
				String detail = " Bad Version passed to getVerNumFromVerString: [" + versionString + "]."; 
				throw new AAIException("AAI_6121", detail);
			}
		}
		return versionInt;
	}

	
	/**
	 * Gets the node key prop names.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @param apiVersion the api version
	 * @return HashMap of keyProperties
	 * @throws AAIException the AAI exception
	 */
	public static Collection <String> getNodeKeyPropNames( String transId, String fromAppId, String nodeType, String apiVersion ) throws AAIException{
	
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
		Collection <String> keyProps = new ArrayList <String>();
		if( dbMaps.NodeKeyProps.containsKey(nodeType) ){
			keyProps = dbMaps.NodeKeyProps.get(nodeType);
		}
		else if( DbEdgeRules.NodeTypeCategory.containsKey(nodeType) ){
			// The passed-in nodeType was really a nodeCategory, so we need to look up the key params
			Collection <String> nTypeCatCol = DbEdgeRules.NodeTypeCategory.get(nodeType);
			Iterator <String> catItr = nTypeCatCol.iterator();
			String catInfo = "";
			if( catItr.hasNext() ){
				// For now, we only look for one.
				catInfo = catItr.next();
			}
			else {
				String defVer = AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP);
				throw new AAIException("AAI_6105", "Required Property name(s) not found for nodeType = " + nodeType+ " (ver=" + defVer + ")"); 
			}

			String [] flds = catInfo.split(",");
			if( flds.length != 4 ){
				throw new AAIException("AAI_6121", "Bad EdgeRule.NodeTypeCategory data for nodeType = [" + nodeType + "]."); 
			}

			String keyPropsString = flds[0];
			String [] propNames = keyPropsString.split("\\|");
			for( int i = 0; i < propNames.length; i++ ){
				keyProps.add(propNames[i]);
			}
		}
		else {
			String defVer = AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP);
			throw new AAIException("AAI_6105", "Required Property name(s) not found for nodeType = " + nodeType+ " (ver=" + defVer + ")"); 
		}

		return keyProps;

	}// end of getNodeKeyPropNames
	
	/**
	 * Gets the node key prop names.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @return the node key prop names
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static Collection <String> getNodeKeyPropNames( String transId, String fromAppId, String nodeType ) throws AAIException{
		return getNodeKeyPropNames(  transId,  fromAppId,  nodeType, null);
	}

	/**
	 * Gets the node alt key 1 prop names.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @param apiVersion the api version
	 * @return HashMap of keyProperties
	 * @throws AAIException the AAI exception
	 */
	public static Collection <String> getNodeAltKey1PropNames( String transId, String fromAppId, String nodeType, String apiVersion ) throws AAIException{

		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
		Collection <String> altKey1Props = new ArrayList <String>();
		if( dbMaps.NodeAltKey1Props.containsKey(nodeType) ){
			altKey1Props = dbMaps.NodeAltKey1Props.get(nodeType);
		}
		else if( DbEdgeRules.NodeTypeCategory.containsKey(nodeType) ){
			// The passed-in nodeType was really a nodeCategory, so we need to look up the key params
			Collection <String> nTypeCatCol = DbEdgeRules.NodeTypeCategory.get(nodeType);
			Iterator <String> catItr = nTypeCatCol.iterator();
			String catInfo = "";
			if( catItr.hasNext() ){
				catInfo = catItr.next();
				String [] flds = catInfo.split(",");
				if( flds.length != 4 ){
					throw new AAIException("AAI_6121", "Bad EdgeRule.NodeTypeCategory data (itemCount=" + flds.length + ") for nodeType = [" + nodeType + "]."); 
				}

				String altKeyPropsString = flds[1];
				String [] propNames = altKeyPropsString.split("\\|");
				for( int i = 0; i < propNames.length; i++ ){
					altKey1Props.add(propNames[i]);
				}
			}
		}

		return altKey1Props;

	}// end of getNodeAltKey1PropNames
	
	/**
	 * Gets the node alt key 1 prop names.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @return the node alt key 1 prop names
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static Collection <String> getNodeAltKey1PropNames( String transId, String fromAppId, String nodeType ) throws AAIException{
		return getNodeAltKey1PropNames(  transId,  fromAppId,  nodeType, null);
	}


	/**
	 * Gets the unique node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param keyPropsHash the key props hash
	 * @param depNodeVal the dep node val
	 * @param apiVersion the api version
	 * @return TitanVertex
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static TitanVertex getUniqueNode( String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> keyPropsHash, TitanVertex depNodeVal, String apiVersion ) 	 throws AAIException{
		
		// NOTE - this is really for use by the PersistNode method -- it is looking to see if
		//     a node exists in the database given either Primary or Alternate Key data and dependent
		//     node data (if required for uniqueness).

		// Note - the passed in nodeType could really be a nodeTypeCategory ---
		Boolean nodeTypeIsCategory = DbEdgeRules.NodeTypeCategory.containsKey(nodeType);

		Boolean useDepNode = false;
		if( needsADepNode4Uniqueness(transId, fromAppId, nodeType, apiVersion) ){
			// This kind of node depends on another node for uniqueness
			if( depNodeVal == null ){
				// They should have passed in the node that this one depends on
				throw new AAIException("AAI_6109", "null dependentNode object passed to getUniqueNode() but " + nodeType + " requires one."); 
			}
			else if( ! nodeTypeACanDependOnB(transId, fromAppId, nodeType, depNodeVal.<String>property("aai-node-type").orElse(null), apiVersion) ){	
				// They should have passed in the right type of node as the dependent node
				throw new AAIException("AAI_6109", "dependentNode of type " + depNodeVal.<String>property("aai-node-type").orElse(null) + " passed to getUniqueNode() for nodeType" + nodeType + ".\n"); 
			}
			useDepNode = true;
		}
		else {
			depNodeVal = null;
		}

		// We assume that all NodeTypes have at least one key-property defined.  A dependentNode is optional.
		// Note - instead of key-properties (the primary key properties), a user could pass
		//        alternate-key values if they are defined for the nodeType.
		ArrayList<String> kName = new ArrayList<String>();
		ArrayList<Object> kVal = new ArrayList<Object>();

		Collection <String> keyProps = getNodeKeyPropNames(transId, fromAppId, nodeType, apiVersion);
		Iterator <String> keyPropI = keyProps.iterator();
		Boolean haveSomePrimKeyProps = false;
		Boolean primaryKeyComplete = true;
		while( keyPropI.hasNext() ){
			haveSomePrimKeyProps = true;
			
			String propName = keyPropI.next();
			if( ! keyPropsHash.containsKey(propName) ){
				primaryKeyComplete = false;
			}
			else {
				Object valObj = keyPropsHash.get(propName);
				if( valObj == null ){
					primaryKeyComplete = false;
				}
				else {
					String value = valObj.toString();
					if( value == null || value.equals("") ){
						// They passed the property name, but no value
						primaryKeyComplete = false;
					}
				}
			}
		}
		
		int i = -1;
		if( haveSomePrimKeyProps && primaryKeyComplete ){
			keyPropI = keyProps.iterator();
			while( keyPropI.hasNext() ){
				String propName = keyPropI.next();
				String value = (keyPropsHash.get(propName)).toString();
				i++;
				kName.add(i, propName);
				kVal.add(i, (Object)value);
			}
		}
		else {
			// See if they're using the alternate key
			Collection <String> altKey1Props = getNodeAltKey1PropNames(transId, fromAppId, nodeType, apiVersion);
			Iterator <String> altKey1PropI = altKey1Props.iterator();
			Boolean haveSomeAltKey1Props = false;
			Boolean altKey1Complete = true;
			while( altKey1PropI.hasNext() ){
				haveSomeAltKey1Props = true;
				String propName = altKey1PropI.next();
				if( ! keyPropsHash.containsKey(propName) ){
					altKey1Complete = false;
				}
				else {
					Object valObj = keyPropsHash.get(propName);
					if( valObj == null ){
						altKey1Complete = false;
					}
					else {
						String value = valObj.toString();
						if( value == null || value.equals("") ){
							// They passed the property name, but no value
							altKey1Complete = false;
						}
					}
				}
			}
			if( haveSomeAltKey1Props && altKey1Complete ){
				altKey1PropI = altKey1Props.iterator();
				while( altKey1PropI.hasNext() ){
					String propName = altKey1PropI.next();
					String value = (keyPropsHash.get(propName)).toString();
					i++;
					kName.add(i, propName);
					kVal.add(i, (Object)value);
				}
			}
		}

		int topPropIndex = i;
		TitanVertex tiV = null;
		String propsAndValuesForMsg = "";
		if( !useDepNode ){ 
			// There is no node that this type of node depends on, so we can look for it based 
			//    solely on the Aai-defined key fields.
			Iterable <?> verts = null;

			if( topPropIndex == -1 ){
				// Problem if no key Properties defined for this nodeType  
				String defVer = AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP);
				throw new AAIException("AAI_6105", "Bad or Incomplete Key Property params: (" + keyPropsHash.toString() + 
						") for nodeType: " + nodeType + " (ver=" + defVer + ")"); 
			}
			else if( topPropIndex == 0 ){
				if (nodeTypeIsCategory) // dont know real type
					verts= graph.query().has(kName.get(0),kVal.get(0)).vertices();
				else // need this to find dvs switch: dvs.switch-name and port-group.switch-name issue
					verts= graph.query().has(kName.get(0),kVal.get(0)).has("aai-node-type",nodeType).vertices();
				propsAndValuesForMsg = " (" + kName.get(0) + " = " + kVal.get(0) + ") ";
			}	
			else if( topPropIndex == 1 ){
				verts =  graph.query().has(kName.get(0),kVal.get(0)).has(kName.get(1),kVal.get(1)).vertices();
				propsAndValuesForMsg = " (" + kName.get(0) + " = " + kVal.get(0) + ", " 
						+ kName.get(1) + " = " + kVal.get(1) + ") ";
			}	 		
			else if( topPropIndex == 2 ){
				verts= graph.query().has(kName.get(0),kVal.get(0)).has(kName.get(1),kVal.get(1)).has(kName.get(2),kVal.get(2)).vertices();
				propsAndValuesForMsg = " (" + kName.get(0) + " = " + kVal.get(0) + ", " 
						+ kName.get(1) + " = " + kVal.get(1) + ", " 
						+ kName.get(2) + " = " + kVal.get(2) +  ") ";
			}	
			else if( topPropIndex == 3 ){
				verts= graph.query().has(kName.get(0),kVal.get(0)).has(kName.get(1),kVal.get(1)).has(kName.get(2),kVal.get(2)).has(kName.get(3),kVal.get(3)).vertices();
				propsAndValuesForMsg = " (" + kName.get(0) + " = " + kVal.get(0) + ", " 
						+ kName.get(1) + " = " + kVal.get(1) + ", " 
						+ kName.get(2) + " = " + kVal.get(2) + ", " 
						+ kName.get(3) + " = " + kVal.get(3) +  ") ";
			}	 		
			else {
				String emsg = " We only support 4 keys per nodeType for now \n";
				throw new AAIException("AAI_6114", emsg); 
			}

			Iterator <?> vertI = verts.iterator();
			if( vertI != null && vertI.hasNext()) {
				// We found a vertex that meets the input criteria. 
				tiV = (TitanVertex) vertI.next();

				if( vertI.hasNext() ){
					// Since this routine is looking for a unique node for the given input values, if  
					// more than one is found - it's a problem.
					throw new AAIException("AAI_6112", "More than one Node found by getUniqueNode for params: " + propsAndValuesForMsg); 
				}
			} 
			else {
				// No Vertex was found for this key - throw a not-found exception
				throw new AAIException("AAI_6114", "No Node of type " + nodeType + " found for properties: " + propsAndValuesForMsg);
			}
		}
		else {
			// Need to use the dependent vertex to look for this one.
			// filter this to the actual keys because
			HashMap<String,Object> onlyKeysHash = new HashMap<String,Object>();
			
			Collection <String> onlyKeyProps = getNodeKeyPropNames(transId, fromAppId, nodeType, apiVersion);
			
			Iterator <String> onlyKeyPropsI = onlyKeyProps.iterator();
			
			while( onlyKeyPropsI.hasNext() ){
				String keyName = onlyKeyPropsI.next();
				onlyKeysHash.put(keyName, keyPropsHash.get(keyName));
			}

			propsAndValuesForMsg = onlyKeysHash.toString() + " combined with a Dependent [" + depNodeVal.<String>property("aai-node-type").orElse(null) + "] node."; 
			ArrayList<TitanVertex> resultList = DbMeth.getConnectedNodes(transId, fromAppId, graph, nodeType, onlyKeysHash, 
					depNodeVal, apiVersion, false);
			if( resultList.size() > 1 ){
				// More than one vertex found when we thought there should only be one.
				throw new AAIException("AAI_6112", "More than one Node found by getUniqueNode for params: " + propsAndValuesForMsg);  
			}
			else if( resultList.size() == 1 ){
				tiV = resultList.get(0);
			}
		}

		if( tiV == null ){
			// No Vertex was found for this key - throw a not-found exception
			throw new AAIException("AAI_6114", "No Node of type " + nodeType + " found for properties: " + propsAndValuesForMsg);
		}
		else {
			if( !DbEdgeRules.NodeTypeCategory.containsKey(nodeType) ){
				// The nodeType passed in was a real one, not a nodeTypeCategory, so we will
				// use it as part of the query to make sure we find the right type of node.
				// This can be an issue if they're using nodeTypes covered by a nodeTypeCategory but
				// pass in the wrong nodeType.  We don't want them to ask for one thing and get the other.
				String foundNodeType = tiV.<String>property("aai-node-type").orElse(null);
				if( foundNodeType != null && !foundNodeType.equals(nodeType) ){
					throw new AAIException("AAI_6114", "No Node of type " + nodeType + " found for properties: " + propsAndValuesForMsg + " (did find a " + foundNodeType + " though.)");
				}
			}
			
			return tiV;
		}

	}// End of getUniqueNode() 

	/**
	 * Gets the unique node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param keyPropsHash the key props hash
	 * @param depNodeVal the dep node val
	 * @return the unique node
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static TitanVertex getUniqueNode( String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> keyPropsHash, TitanVertex depNodeVal) 	 throws AAIException {
		return getUniqueNode( transId, fromAppId, graph, nodeType,
				keyPropsHash,  depNodeVal, null );
	}
	// End getUniqueNode()


	/**
	 * Gets the unique node with dep params.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param nodePropsHash the node props hash
	 * @param apiVersion the api version
	 * @return TitanVertex
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static TitanVertex getUniqueNodeWithDepParams( String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> nodePropsHash, String apiVersion ) 
					throws AAIException{
		/*
		 * This method uses the nodePropsHash to walk back over dependent nodes until it finds one that
		 * does not depend on any other for uniqueness.   It uses the getUniqueNode() method as it finds
		 * dependent nodes.   NOTE -- it is passed a hash of all the nodeProperties -- for itself and
		 * for any dependent nodes that it will need to find.   There are some types of nodes that can
		 * depend on more than one node, we assume that there wouldn't be a case where BOTH types of
		 * dependent nodes are in the trail that we need to traverse.  Ie. an ipaddress can depend on
		 * either a vserver or pserver.  NOTE this case can now happen -- nodePropsHash
		 * should now be sent as a LinkedHashMap in this case so we can search in order. 
		 */

		// NOTE ALSO -- We're currently supporting 6 layers of dependency.   We never thought there would be this
		//       many layers before hitting a node-type that would be uniquely identifiable on it's own.   So the
		//       code is a little ugly with all these nested if-then-else's.   Since we're supporting so many
		//       layers, it should be re-written so we can support "n" layers instead of having to go in hear
		//       and adding code...   But as of 15-07, we really don't NEED more than 5.

		// NOTE: The passed in nodeType could really be a nodeTypeCategory -- 
		//       The calls to figureDepNodeTypeForRequest() below will deal with it for the dep nodes, the 
		//       call to getUniqueNode() takes care of it for the node itself.

		TitanVertex nullVert = null;
		String depNodeType = figureDepNodeTypeForRequest( transId, fromAppId, nodeType, nodePropsHash, apiVersion );
		if( depNodeType.equals("")){
			// This kind of node does not depend on another node for uniqueness, so 
			// we can just use the "getUniqueNode()" method to get it.
			HashMap <String,Object> thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, nodeType, nodePropsHash, apiVersion);
			return( getUniqueNode(transId, fromAppId, graph, nodeType, thisNodeTypeParamHash, nullVert, apiVersion) );
		}
		else {
			// Will need to find the second-layer dependent node
			String secondLayerDepNodeType = figureDepNodeTypeForRequest( transId, fromAppId, depNodeType, nodePropsHash, apiVersion );
			if( secondLayerDepNodeType.equals("")){
				// This second-layer kind of node does not depend on another node for uniqueness.
				// So once we find the second-layer node, we can use it to get the top-layer guy.
				HashMap <String,Object> thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, depNodeType, nodePropsHash, apiVersion);
				TitanVertex secLayerDepVert = getUniqueNode(transId, fromAppId, graph, depNodeType, thisNodeTypeParamHash, nullVert, apiVersion);

				thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, nodeType, nodePropsHash, apiVersion);
				return( getUniqueNode(transId, fromAppId, graph, nodeType, thisNodeTypeParamHash, secLayerDepVert, apiVersion) );
			}
			else {
				// Will need to find the third-layer dependent node
				///  String thirdLayerDepNodeType = dbMaps.NodeDependencies.get(secondLayerDepNodeType);
				String thirdLayerDepNodeType = figureDepNodeTypeForRequest( transId, fromAppId, secondLayerDepNodeType, nodePropsHash, apiVersion );

				if( thirdLayerDepNodeType.equals("")){
					// This third-layer kind of node does not depend on another node for uniqueness.
					// So we can find it, and then use it to find the second-layer and then use that to find the top guy.
					HashMap <String,Object> thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, secondLayerDepNodeType, nodePropsHash, apiVersion);
					TitanVertex thirdLayerDepVert = getUniqueNode(transId, fromAppId, graph, secondLayerDepNodeType, thisNodeTypeParamHash, nullVert, apiVersion);

					thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, depNodeType, nodePropsHash, apiVersion);
					TitanVertex secLayerDepVert = getUniqueNode(transId, fromAppId, graph, depNodeType, thisNodeTypeParamHash, thirdLayerDepVert, apiVersion);

					thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, nodeType, nodePropsHash, apiVersion);

					return( getUniqueNode(transId, fromAppId, graph, nodeType, thisNodeTypeParamHash, secLayerDepVert, apiVersion) );
				}
				else {
					// Will need to find the third-layer dependent node
					String forthLayerDepNodeType = figureDepNodeTypeForRequest( transId, fromAppId, thirdLayerDepNodeType, nodePropsHash, apiVersion );
					if( forthLayerDepNodeType == null || forthLayerDepNodeType.equals("")){
						// This forth-layer kind of node does not depend on another node for uniqueness.
						// So we can find it, and then use it to find the third, then second-layer and then use that to find the top guy.
						HashMap <String,Object> thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, thirdLayerDepNodeType, nodePropsHash, apiVersion);
						TitanVertex forthLayerDepVert = getUniqueNode(transId, fromAppId, graph, thirdLayerDepNodeType, thisNodeTypeParamHash, nullVert, apiVersion);

						thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, secondLayerDepNodeType, nodePropsHash, apiVersion);
						TitanVertex thirdLayerDepVert = getUniqueNode(transId, fromAppId, graph, secondLayerDepNodeType, thisNodeTypeParamHash, forthLayerDepVert, apiVersion);

						thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, depNodeType, nodePropsHash, apiVersion);
						TitanVertex secLayerDepVert = getUniqueNode(transId, fromAppId, graph, depNodeType, thisNodeTypeParamHash, thirdLayerDepVert, apiVersion);

						thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, nodeType, nodePropsHash, apiVersion);
						return( getUniqueNode(transId, fromAppId, graph, nodeType, thisNodeTypeParamHash, secLayerDepVert, apiVersion) );
					}
					else {
						// Will need to find the forth-layer dependent node
						String fifthLayerDepNodeType = figureDepNodeTypeForRequest( transId, fromAppId, forthLayerDepNodeType, nodePropsHash, apiVersion );
						if( fifthLayerDepNodeType == null || fifthLayerDepNodeType.equals("")){
							// This fifth-layer kind of node does not depend on another node for uniqueness.
							// So we can find it, and then use it to find the forth, third, then second-layer and then use that to find the top guy.
							HashMap <String,Object> thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, forthLayerDepNodeType, nodePropsHash, apiVersion);
							TitanVertex fifthLayerDepVert = getUniqueNode(transId, fromAppId, graph, forthLayerDepNodeType, thisNodeTypeParamHash, nullVert, apiVersion);

							thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, thirdLayerDepNodeType, nodePropsHash, apiVersion);
							TitanVertex forthLayerDepVert = getUniqueNode(transId, fromAppId, graph, thirdLayerDepNodeType, thisNodeTypeParamHash, fifthLayerDepVert, apiVersion);

							thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, secondLayerDepNodeType, nodePropsHash, apiVersion);
							TitanVertex thirdLayerDepVert = getUniqueNode(transId, fromAppId, graph, secondLayerDepNodeType, thisNodeTypeParamHash, forthLayerDepVert, apiVersion);

							thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, depNodeType, nodePropsHash, apiVersion);
							TitanVertex secLayerDepVert = getUniqueNode(transId, fromAppId, graph, depNodeType, thisNodeTypeParamHash, thirdLayerDepVert, apiVersion);

							thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, nodeType, nodePropsHash, apiVersion);
							return( getUniqueNode(transId, fromAppId, graph, nodeType, thisNodeTypeParamHash, secLayerDepVert, apiVersion) );
						}
						else {
							// Will need to find the fifth-layer dependent node
							String sixthLayerDepNodeType = figureDepNodeTypeForRequest( transId, fromAppId, fifthLayerDepNodeType, nodePropsHash, apiVersion );
							if( sixthLayerDepNodeType == null || sixthLayerDepNodeType.equals("")){
								// This six-layer kind of node does not depend on another node for uniqueness.
								// So we can find it, and then use it to find the fifth, forth, third, then second-layer and then use that to find the top guy.
								HashMap <String,Object> thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, fifthLayerDepNodeType, nodePropsHash, apiVersion);
								TitanVertex sixthLayerDepVert = getUniqueNode(transId, fromAppId, graph, fifthLayerDepNodeType, thisNodeTypeParamHash, nullVert, apiVersion);

								thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, forthLayerDepNodeType, nodePropsHash, apiVersion);
								TitanVertex fifthLayerDepVert = getUniqueNode(transId, fromAppId, graph, forthLayerDepNodeType, thisNodeTypeParamHash, sixthLayerDepVert, apiVersion);

								thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, thirdLayerDepNodeType, nodePropsHash, apiVersion);
								TitanVertex forthLayerDepVert = getUniqueNode(transId, fromAppId, graph, thirdLayerDepNodeType, thisNodeTypeParamHash, fifthLayerDepVert, apiVersion);

								thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, secondLayerDepNodeType, nodePropsHash, apiVersion);
								TitanVertex thirdLayerDepVert = getUniqueNode(transId, fromAppId, graph, secondLayerDepNodeType, thisNodeTypeParamHash, forthLayerDepVert, apiVersion);

								thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, depNodeType, nodePropsHash, apiVersion);
								TitanVertex secLayerDepVert = getUniqueNode(transId, fromAppId, graph, depNodeType, thisNodeTypeParamHash, thirdLayerDepVert, apiVersion);

								thisNodeTypeParamHash = getThisNodeTypeParams(transId, fromAppId, nodeType, nodePropsHash, apiVersion);
								return( getUniqueNode(transId, fromAppId, graph, nodeType, thisNodeTypeParamHash, secLayerDepVert, apiVersion) );
							}
							else {
								// We don't currently support more layers.  We can later if we need to.
								// Hopefully, we'll never need to go this deep -- there should be unique keys in there somewhere! 
								throw new AAIException("AAI_6114", "CODE-LIMITATION - Can't resolve dependant node layers for nodeType = " + nodeType); 
							}
						}
					}
				}
			}
		}
	} // End getUniqueNodeWithDepParams()
	
	/**
	 * Gets the unique node with dep params.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param nodePropsHash the node props hash
	 * @return the unique node with dep params
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static TitanVertex getUniqueNodeWithDepParams( String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> nodePropsHash ) throws AAIException {
		return getUniqueNodeWithDepParams(transId, fromAppId, graph, nodeType, nodePropsHash, null);
	}


	/**
	 * Gets the this node type params.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param targetNodeType the target node type
	 * @param passedHash the passed hash
	 * @param apiVersion the api version
	 * @return the this node type params
	 * @throws AAIException the AAI exception
	 */
	private static HashMap <String, Object> getThisNodeTypeParams(String transId, String fromAppId, String targetNodeType, 
			HashMap<String,Object> passedHash, String apiVersion )throws AAIException{
		/*
		 * For the passed-in hash, each key is assumed to look like, "nodeType.paramName".  We want to 
		 * pick out the entries that match the targetNodeType and return those with the values they go with.  The
		 * returned keys will have the "nodeType." stripped off.   
		 * 
		 * NOTE  - the nodeType passed to this method could actually be a nodeTypeCategory.  Just keepin it ugly.
		 */

		if( passedHash == null ){
			throw new AAIException("AAI_6120", "Bad param:  null passedHash "); 
		}

		String targetNodeTypeCat = "";
		if( DbEdgeRules.NodeTypeCatMap.containsKey(targetNodeType) ){
			targetNodeTypeCat = DbEdgeRules.NodeTypeCatMap.get(targetNodeType);
		}

		HashMap <String,Object> returnHash = new HashMap <String,Object> ();
		Iterator <Map.Entry<String,Object>>it = passedHash.entrySet().iterator();
		while( it.hasNext() ){
			Map.Entry <String,Object>pairs = (Map.Entry<String,Object>)it.next();
			String k = (pairs.getKey()).toString();
			int periodLoc = k.indexOf(".");
			if( periodLoc <= 0 ){
				throw new AAIException("AAI_6120", "Bad filter param key passed in: [" + k + "].  Expected format = [nodeName.paramName]\n"); 
			}
			else {
				String nty = k.substring(0,periodLoc);
				String paramName = k.substring(periodLoc + 1);
				if( nty.equals(targetNodeType) || (!targetNodeTypeCat.equals("") && nty.equals(targetNodeTypeCat)) ){
					String newK = paramName;
					returnHash.put( newK,pairs.getValue() );
				}
			}
		}

		//aaiLogger.debug(logline, " - end ");
		return returnHash;

	}// End of getThisNodeTypeParams()
	
	/**
	 * Gets the this node type params.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param targetNodeType the target node type
	 * @param passedHash the passed hash
	 * @return the this node type params
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	private static HashMap <String, Object> getThisNodeTypeParams(String transId, String fromAppId, String targetNodeType, 
			HashMap<String,Object> passedHash )throws AAIException{
		return getThisNodeTypeParams( transId,  fromAppId, targetNodeType, 
				 passedHash, null);
		
	}

	/**
	 * Gets the dep node types.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @param apiVersion the api version
	 * @return the dep node types
	 * @throws AAIException the AAI exception
	 */
	public static ArrayList <String> getDepNodeTypes(String transId, String fromAppId, String nodeType, String apiVersion)throws AAIException{
		/*
		 * This returns any nodeTypes that this nodeType can be dependent on.  A particular instance of a node will only
		 * depend on one other node - we don't currently support dependence on multiple nodes.
		 */
			
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));

		ArrayList <String> depNodeTypeL = new ArrayList <String> ();
		if( !DbEdgeRules.NodeTypeCategory.containsKey(nodeType) ){
			// This is a good-ole nodeType
			Collection <String> depNTColl =  dbMaps.NodeDependencies.get(nodeType);
			Iterator <String> ntItr = depNTColl.iterator();
			while( ntItr.hasNext() ){
				depNodeTypeL.add(ntItr.next());
			}
		}
		else {
			// The passed-in nodeType must really be a nodeTypeCategory
			Collection <String> nTypeCatCol = DbEdgeRules.NodeTypeCategory.get(nodeType);
			Iterator <String> catItr = nTypeCatCol.iterator();
			String catInfo = "";
			if( catItr.hasNext() ){
				// For now, we only look for one.
				catInfo = catItr.next();
			}
			else {
				throw new AAIException("AAI_6121", "Error getting DbEdgeRules.NodeTypeCategory info for nodeTypeCat = " + nodeType); 
			}

			String [] flds = catInfo.split(",");
			if( flds.length != 4 ){
				throw new AAIException("AAI_6121", "Bad EdgeRule.NodeTypeCategory data (itemCount=" + flds.length + ") for nodeType = [" + nodeType + "]."); 
			}

			String nodeTypesString = flds[0];
			String  hasDepNodes = flds[3];
			if( hasDepNodes.equals("true") ){
				String [] ntNames = nodeTypesString.split("\\|");
				for( int i = 0; i < ntNames.length; i++ ){
					Collection <String> depNTColl =  dbMaps.NodeDependencies.get(nodeType);
					Iterator <String> ntItr = depNTColl.iterator();
					while( ntItr.hasNext() ){
						String depNode = ntItr.next();
						if( !depNodeTypeL.contains(depNode) ){
							depNodeTypeL.add(depNode);
						}
					}
				} 	
			}
		}


		return depNodeTypeL;

	}// End getDepNodeTypes()
	
	/**
	 * Gets the dep node types.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @return the dep node types
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static ArrayList <String> getDepNodeTypes(String transId, String fromAppId, String nodeType)throws AAIException{
		return getDepNodeTypes( transId,  fromAppId,  nodeType, null);
	}

	/**
	 * Gets the default delete scope.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @param apiVersion the api version
	 * @return the default delete scope
	 * @throws AAIException the AAI exception
	 */
	private static String getDefaultDeleteScope(String transId, String fromAppId, String nodeType, String apiVersion)throws AAIException{

		// At some point we may have different delete rules for different services, so this is
		// a list for now even thought there's only one scope per nodeType.
		Collection <String> scopeList = DbEdgeRules.DefaultDeleteScope.get(nodeType);
		if( scopeList.isEmpty() ){
			throw new AAIException("AAI_6121", "No default deleteScope found for nodeType = [" + nodeType + "] "); 
		}
		else {
			Iterator <String> ito = scopeList.iterator();
			return ito.next();
		}

	}// End getDefaultDeleteScope()
	
	/**
	 * Gets the default delete scope.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @return the default delete scope
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	private static String getDefaultDeleteScope(String transId, String fromAppId, String nodeType)throws AAIException{
		return getDefaultDeleteScope( transId,  fromAppId,  nodeType, null);
	}

	/**
	 * Needs A dep node 4 uniqueness.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @param apiVersion the api version
	 * @return the boolean
	 * @throws AAIException the AAI exception
	 */
	public static Boolean needsADepNode4Uniqueness(String transId, String fromAppId, String nodeType, String apiVersion)throws AAIException{
		// Note: the passed in nodeType could really be a nodeTypeCategory.  That is handled by getDepNodeTypes()

		ArrayList <String> depList = getDepNodeTypes(transId, fromAppId, nodeType, apiVersion);
		if( depList.isEmpty() ){
			return false;
		}
		else {
			return true;
		}

	}// End needsADepNode4Uniqueness()
	
	/**
	 * Needs A dep node 4 uniqueness.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @return the boolean
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	private static Boolean needsADepNode4Uniqueness(String transId, String fromAppId, String nodeType)throws AAIException{
		return needsADepNode4Uniqueness( transId,  fromAppId,  nodeType,  null);
	}

	/**
	 * Node type A can depend on B.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeTypeA the node type A
	 * @param nodeTypeB the node type B
	 * @param apiVersion the api version
	 * @return the boolean
	 * @throws AAIException the AAI exception
	 */
	public static Boolean nodeTypeACanDependOnB(String transId, String fromAppId, String nodeTypeA, String nodeTypeB, String apiVersion)
			throws AAIException{
		// Note: the passed in nodeType could really be a nodeTypeCategory.  That is handled by getDepNodeTypes()

		ArrayList <String> depList = getDepNodeTypes(transId, fromAppId, nodeTypeA, apiVersion);
		if( depList.isEmpty() ){
			return false;
		}
		else if( depList.contains(nodeTypeB) ){
			return true;
		}
		else { 
			return false;
		}

	}// End nodeTypeACanDependOnB()
	
	/**
	 * Node type A can depend on B.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeTypeA the node type A
	 * @param nodeTypeB the node type B
	 * @return the boolean
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	private static Boolean nodeTypeACanDependOnB(String transId, String fromAppId, String nodeTypeA, String nodeTypeB)
			throws AAIException{
		return nodeTypeACanDependOnB( transId,  fromAppId,  nodeTypeA,  nodeTypeB, null);
	}

	/**
	 * Figure dep node type for request.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @param requestParamHash the request param hash
	 * @param apiVersion the api version
	 * @return the string
	 * @throws AAIException the AAI exception
	 */
	public static String figureDepNodeTypeForRequest(String transId, String fromAppId, String nodeType, 
			HashMap<String,Object> requestParamHash, String apiVersion )throws AAIException{
		/*
		 * This is ugly.   But if the passed-in nodeType is dependent on another nodeType for 
		 * uniqueness, we need to return what that dependent node-type is.  The ugly comes in
		 * because a node can be dependent on more than one type of node.   So, to tell which one
		 * is going to apply, we root through the passed request parameters to see which of 
		 * the possible dependent node types is being used.
		 * Note -- if there comes a day when there are so many dependencies that the request could
		 * have more than one that match -- Then we need to think up something new.   But for now,
		 * we have to assume that if there are more than one legal dep-node-types, only one will
		 * be represented in the requestHash data.  >>> NOTE >>> That day has come.  For
		 * the upstreamers will send in a LinkedHashMap instead of just an unordered
		 * HashMap so we can look in order for the dependent node.
		 * 
		 */

		if( requestParamHash == null ){
			throw new AAIException("AAI_6120", "Bad param:  null requestParamHash "); 
		}

		ArrayList <String> depNodeTypes = getDepNodeTypes(transId, fromAppId, nodeType, apiVersion);
		if( depNodeTypes.isEmpty() ){
			// This kind of node is not dependent on any other
			//aaiLogger.debug(logline, " (not dependent) - end ");
			return "";
		}
		else if( depNodeTypes.size() == 1 ){
			// This kind of node can only depend on one other nodeType - so return that.
			//aaiLogger.debug(logline, " (depends on " + depNodeTypes.get(0) + " - end ");
			return depNodeTypes.get(0);
		}
		else {
			// We need to look to find the first of the dep-node types that is represented in the passed-in
			// request data.  That will be the one we need to use.

			// first find out what node-types are represented in the requestHash
			
			Iterator <Map.Entry<String,Object>>it = requestParamHash.entrySet().iterator();
			while( it.hasNext() ){
				Map.Entry <String,Object>pairs = (Map.Entry<String,Object>)it.next();
				String k = (pairs.getKey()).toString();
				int periodLoc = k.indexOf(".");
				if( periodLoc <= 0 ){
					throw new AAIException("AAI_6120", "Bad filter param key passed in: [" + k + "].  Expected format = [nodeName.paramName]\n"); 
				}
				else {
					String nty = k.substring(0,periodLoc);
					if( depNodeTypes.contains(nty) ){
						// This is the first possible dep. node type we've found for the passed in data set
						return nty;
					}
				}
			}

		}

		// It's not an error if none is found - the caller needs to deal with cases where there
		// should be a dep. node identified but isn't.
		//aaiLogger.debug(logline, " no dep NT found - end ");
		return "";

	}// End of figureDepNodeTypeForRequest()
	
	/**
	 * Figure dep node type for request.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @param requestParamHash the request param hash
	 * @return the string
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static String figureDepNodeTypeForRequest(String transId, String fromAppId, String nodeType, 
			HashMap<String,Object> requestParamHash )throws AAIException{
		 return figureDepNodeTypeForRequest( transId,  fromAppId,  nodeType, requestParamHash, null);
	}
	
	/**
	 * Detach connected nodes.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propFilterHash the prop filter hash
	 * @param startNodeVal the start node val
	 * @param autoDeleteOrphans the auto delete orphans
	 * @param apiVersion the api version
	 * @return deletedNodeCount
	 * @throws AAIException the AAI exception
	 */
	public static int detachConnectedNodes( String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> propFilterHash, TitanVertex startNodeVal, boolean autoDeleteOrphans, String apiVersion ) throws AAIException{

		/*  Find nodes that are attached to this node which meet the nodeType/filterParams criteria.
		 *  Remove the edges that go to those nodes.
		 *  If that turns any of the nodes into an orphan, then delete it if the autoDeleteOrphans flag is set.
		 *  Return a count of how many nodes were actually deleted (not just detached).
		 */

		int deletedCount = 0;

		if( startNodeVal == null ){
			// They should have passed in the node that this query starts from
			throw new AAIException("AAI_6109", "null startNode object passed to detachConnectedNodes()."); 
		}

		// We want to loop through the connected Nodes that we found.
		// For each connected Node, we'll get the all edges that start from that node and look for the one
		//     that connects back to our startNode.
		// Only delete the edge that connects back to our startNode.
		// then autoDeleteOrphans flag is set, then delete the connectedNode if it's now orphaned.
		// 	

		String startNodeVId =  startNodeVal.id().toString();
		ArrayList<TitanVertex> conNodeList = getConnectedNodes( transId, fromAppId, graph, nodeType, propFilterHash, startNodeVal, apiVersion, false );
		Iterator<TitanVertex> conVIter = conNodeList.iterator();
		while( conVIter.hasNext() ){     	
			TitanVertex connectedVert = conVIter.next();
			boolean isFirstOne = true;
			Iterator<Edge> eI = connectedVert.edges(Direction.BOTH);
			while( eI.hasNext() ){
				TitanEdge ed = (TitanEdge) eI.next();
				TitanVertex otherVtx = (TitanVertex) ed.otherVertex(connectedVert);
				String otherSideLookingBackVId =  otherVtx.id().toString();
				if( startNodeVId.equals(otherSideLookingBackVId) ){
					// This is an edge from the connected node back to our starting node
					if( isFirstOne && !eI.hasNext() && autoDeleteOrphans ){
						// This was the one and only edge for this connectedNode, so 
						// delete the node and edge since flag was set 
						String resVers = connectedVert.<String>property("resource-version").orElse(null);
						removeAaiNode( transId, fromAppId, graph, connectedVert,  "USE_DEFAULT", apiVersion, resVers);
						deletedCount = deletedCount + 1;
					}
					else {
						removeAaiEdge( transId, fromAppId, graph, ed );
					}
				}
				isFirstOne = false;
			}
		}
		return deletedCount;

	} // end of detachConnectedNodes()



	/**
	 * Detach connected nodes.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propFilterHash the prop filter hash
	 * @param startNodeVal the start node val
	 * @param autoDeleteOrphans the auto delete orphans
	 * @return the int
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static int detachConnectedNodes( String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> propFilterHash, TitanVertex startNodeVal, boolean autoDeleteOrphans ) throws AAIException{
		return detachConnectedNodes(  transId,  fromAppId,  graph,  nodeType,
				propFilterHash,  startNodeVal,  autoDeleteOrphans, null);
	}
	
	/**
	 * Gets the nodes.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propFilterHash the prop filter hash
	 * @param noFilterOnPurpose the no filter on purpose
	 * @param apiVersion the api version
	 * @return ArrayList<TitanVertex>
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static ArrayList<TitanVertex> getNodes( String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> propFilterHash, Boolean noFilterOnPurpose, String apiVersion ) throws AAIException{
		boolean skipGroomingFlag = true; 
		// we will only do real-time grooming if a system variable is set, telling us not to skip it.
		String skipGroomingStr = AAIConstants.AAI_SKIPREALTIME_GROOMING;
		if( skipGroomingStr.equals("false") ){
			skipGroomingFlag = false;
		}
		return( getNodes(transId, fromAppId, graph, nodeType, propFilterHash, noFilterOnPurpose, apiVersion, skipGroomingFlag) );
	}
	
	/**
	 * Gets the nodes.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propFilterHash the prop filter hash
	 * @param noFilterOnPurpose the no filter on purpose
	 * @param apiVersion the api version
	 * @param skipGroomCheck the skip groom check
	 * @return ArrayList<TitanVertex>
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static ArrayList<TitanVertex> getNodes( String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> propFilterHash, Boolean noFilterOnPurpose, String apiVersion, boolean skipGroomCheck ) 
					throws AAIException{
		//  Note - the skipGroomCheck flag is set to true when the DataGrooming tool is using this method to collect
		//     node data.  When the grooming tool is collecting data, we don't want any nodes skipped, because we
		//     want details about what nodes/edges are bad - more detail than the check in this method does
		//     as it checks if a node is ok to return to a caller.
		
		/* Use the nodeType + filterParams to find nodes.    
		 */
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
		ArrayList<TitanVertex> returnVertList = new ArrayList<TitanVertex>();
		if( nodeType == null || nodeType.equals("") ){
			// They should have passed in a nodeType
			throw new AAIException("AAI_6118", "Required field: nodeType not passed to getNodes()."); 
		}

		if( !noFilterOnPurpose && (propFilterHash == null || propFilterHash.isEmpty()) ){
			// They should have passed at least one property to filter on
			throw new AAIException("AAI_6118", "Required field: propFilterHash not passed to getNodes()."); 
		}

		ArrayList<String> kName = new ArrayList<String>();
		ArrayList<Object> kVal = new ArrayList<Object>();
		int i = -1;
		Collection <String> indexedProps =  dbMaps.NodeMapIndexedProps.get(nodeType); 
		// First loop through to pick up the indexed-properties if there are any being used

		if( propFilterHash != null ){
			Iterator <?> it = propFilterHash.entrySet().iterator();
			while( it.hasNext() ){
				Map.Entry<?,?> propEntry = (Map.Entry<?,?>) it.next();
				String propName = (propEntry.getKey()).toString();
				// Don't allow search on properties that do not have SINGLE cardinality
				if( !checkPropCardinality(propName, "Set") && !checkPropCardinality(propName, "List")  ){
					if( indexedProps.contains(propName) ){
						i++;
						kName.add(i, propName);
						kVal.add(i, (Object)propEntry.getValue());
					}
				}
			}

			// Now go through again and pick up the non-indexed properties
			it = propFilterHash.entrySet().iterator();
			while( it.hasNext() ){
				Map.Entry <?,?> propEntry = (Map.Entry<?,?>)it.next();
				String propName = (propEntry.getKey()).toString();
				// Don't allow search on properties that do not have SINGLE cardinality
				if( !checkPropCardinality(propName, "Set") && !checkPropCardinality(propName, "List")  ){
					if( ! indexedProps.contains(propName) ){
						i++;
						kName.add(i, propName);
						kVal.add(i, (Object)propEntry.getValue());
					}
				}
			}
		}

		Iterable <?> verts = null;
		String propsAndValuesForMsg = "";
		int topPropIndex = i;
		if( topPropIndex == -1 ){
			// No Filtering -- just go get them all
			verts= graph.query().has("aai-node-type",nodeType).vertices();
			propsAndValuesForMsg = " ( no filter props ) ";
		}	
		else if( topPropIndex == 0 ){
			verts= graph.query().has(kName.get(0),kVal.get(0)).has("aai-node-type",nodeType).vertices();
			propsAndValuesForMsg = " (" + kName.get(0) + " = " + kVal.get(0) + ") ";
		}	
		else if( topPropIndex == 1 ){
			verts= graph.query().has(kName.get(0),kVal.get(0)).has(kName.get(1),kVal.get(1)).has("aai-node-type",nodeType).vertices();
			propsAndValuesForMsg = " (" + kName.get(0) + " = " + kVal.get(0) + ", " 
					+ kName.get(1) + " = " + kVal.get(1) + ") ";
		}	 		
		else if( topPropIndex == 2 ){
			verts= graph.query().has(kName.get(0),kVal.get(0)).has(kName.get(1),kVal.get(1)).has(kName.get(2),kVal.get(2)).has("aai-node-type",nodeType).vertices();
			propsAndValuesForMsg = " (" + kName.get(0) + " = " + kVal.get(0) + ", " 
					+ kName.get(1) + " = " + kVal.get(1) + ", " 
					+ kName.get(2) + " = " + kVal.get(2) +  ") ";
		}	
		else if( topPropIndex == 3 ){
			verts= graph.query().has(kName.get(0),kVal.get(0)).has(kName.get(1),kVal.get(1)).has(kName.get(2),kVal.get(2)).has(kName.get(3),kVal.get(3)).has("aai-node-type",nodeType).vertices();
			propsAndValuesForMsg = " (" + kName.get(0) + " = " + kVal.get(0) + ", " 
					+ kName.get(1) + " = " + kVal.get(1) + ", " 
					+ kName.get(2) + " = " + kVal.get(2) + ", " 
					+ kName.get(3) + " = " + kVal.get(3) +  ") ";
		}	 		
		else {
			String emsg = " -- Sorry -- we only support 4 filter properties in getNodes() for now... \n";
			throw new AAIException("AAI_6114", emsg); 
		}   
		if( verts != null ){
			// We did find some matching vertices
			Iterator <?> it = verts.iterator();
			while( it.hasNext() ){
				TitanVertex v = (TitanVertex)it.next();
				
				if( skipGroomCheck ){
					// Good or bad, just return everything we find
					returnVertList.add( v );
				}
				else {
					// Weed out any bad vertices we find
					if( thisVertexNotReachable(transId, fromAppId, graph, v, apiVersion) ){
						LOGGER.info("IN-LINE GROOMING - Unreachable Node DETECTED > skipping it. ");
					}
					else if( thisVertexHasBadEdges(transId, fromAppId, graph, v, apiVersion) ){
						LOGGER.info("IN-LINE GROOMING - BAD EDGE DETECTED > skipping vtxId = [" + v.id() + "] ");
					}
					else if( thisVertexIsAPhantom(transId, fromAppId, graph, v, apiVersion) ){
						LOGGER.info("IN-LINE GROOMING - BAD NODE DETECTED > skipping vtxId = [" + v.id() + "] ");
					}
					else {
						returnVertList.add( v );
					}
				}
			}
		}
		
		return returnVertList;
	}
	
	/**
	 * Gets the nodes.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propFilterHash the prop filter hash
	 * @param noFilterOnPurpose the no filter on purpose
	 * @return the nodes
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static ArrayList<TitanVertex> getNodes( String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> propFilterHash, Boolean noFilterOnPurpose ) throws AAIException{
		return getNodes(transId,  fromAppId,  graph,  nodeType,
				propFilterHash,  noFilterOnPurpose,  null );
	}
	// End of getNodes()


	/**
	 * Gets the connected children.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param startVtx the start vtx
	 * @param limitToThisNodeType the limit to this node type
	 * @return ArrayList <TitanVertex>
	 * @throws AAIException the AAI exception
	 */
	public static ArrayList<TitanVertex> getConnectedChildren( String transId, String fromAppId, TitanTransaction graph, 
			TitanVertex startVtx, String limitToThisNodeType ) throws AAIException{
		
		// Just get child nodes (ie. other end of an OUT edge that is tagged as a parent/Child edge)
		 
		ArrayList <TitanVertex> childList = new ArrayList <TitanVertex> ();
		Boolean doNodeTypeCheck = false;
		if( limitToThisNodeType != null && ! limitToThisNodeType.equals("") ){
			doNodeTypeCheck = true;
		}
		
		
		List<Vertex> verts = graph.traversal().V(startVtx).union(__.outE().has("isParent-REV", true).inV(), __.inE().has("isParent", true).outV()).toList();
		TitanVertex tmpVtx = null;
		int vertsSize = verts.size();
		for (int i = 0; i < vertsSize; i++){
			tmpVtx = (TitanVertex) verts.get(i);
			if( ! doNodeTypeCheck ){ 
				childList.add(tmpVtx);
			}
			else {
				String tmpNT = tmpVtx.<String>property("aai-node-type").orElse(null);
				if( tmpNT != null && tmpNT.equals(limitToThisNodeType) ){
					childList.add(tmpVtx);
				}
			}
		}
		
		return childList;		

	}// End of getConnectedChildren()



	/**
	 * Gets the connected nodes.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propFilterHash the prop filter hash
	 * @param startNodeVal the start node val
	 * @param apiVersion the api version
	 * @param excludeRecurComingIn the exclude recur coming in
	 * @return ArrayList <TitanVertex>
	 * @throws AAIException the AAI exception
	 */
	public static ArrayList<TitanVertex> getConnectedNodes( String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> propFilterHash, TitanVertex startNodeVal, String apiVersion, Boolean excludeRecurComingIn ) throws AAIException{
		
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		/* Get (almost) all the nodes that are connected to this vertex.  
		 * Narrow down what is returned using optional filter parameters nodeType and propFilterHash
		 * NOTE - the default behavior has changed slightly.  For start-Nodes that
		 *        can be recursivly connected, this method will only bring back the same kind of
		 *        connected node by following an OUT edge.   Ie. if the start node is an "model-element", 
		 *        then this method will only follow OUT edges to get to other "model-element" type nodes.
		 */

		String startNodeNT = "";
		if( startNodeVal == null ){
			// They should have passed in the node that this query starts from
			throw new AAIException("AAI_6109", "null startNode object passed to getConnectedNodes()."); 
		}   	
		else {
			startNodeNT = startNodeVal.<String>property("aai-node-type").orElse(null);
		}
		
		boolean nodeTypeFilter = false;
		if( nodeType != null && !nodeType.equals("") ){
			// They want to filter using nodeType
			if( ! dbMaps.NodeProps.containsKey(nodeType) ){
				throw new AAIException("AAI_6115", "Unrecognized nodeType [" + nodeType + "] passed to getConnectedNodes()."); 
			}
			nodeTypeFilter = true;
		}  
		
		ArrayList <String> excludeVidList = new <String> ArrayList ();
		if( DbEdgeRules.CanBeRecursiveNT.containsKey(startNodeNT) && excludeRecurComingIn ){
			// If we're starting on a nodeType that supports recursion, then find any connected
			// nodes that are coming from IN edges so we can exclude them later.
			
			Iterable <?> vertsR = startNodeVal.query().direction(Direction.IN).vertices();
			Iterator <?> vertIR = vertsR.iterator();
			while( vertIR != null && vertIR.hasNext() ){
				TitanVertex tmpVertIN = (TitanVertex) vertIR.next();
				String tmpNT = tmpVertIN.<String>property("aai-node-type").orElse(null);
				if( tmpNT != null && tmpNT.equals(startNodeNT) ){
					// We're on a nodetype that supports recursion (like model-element) and we've
					// found an connected Node of this same type on an IN edge - put this 
					// on our excludeList.
					excludeVidList.add( tmpVertIN.id().toString() );
				}
			}
		}
	
		boolean propertyFilter = false;
		if( propFilterHash != null && !propFilterHash.isEmpty() ){
			// They want to filter using some properties
			Iterator <?> it = propFilterHash.entrySet().iterator();
			while( it.hasNext() ){
				Map.Entry<?,?> propEntry = (Map.Entry<?,?>)it.next();
				String propName = (propEntry.getKey()).toString();
				if( ! dbMaps.NodeProps.containsValue(propName) ){
					throw new AAIException("AAI_6116", "Unrecognized property name [" + propName + "] passed to getConnectedNodes()."); 
				}
				// Don't allow search on properties that do not have SINGLE cardinality
				if( !checkPropCardinality(propName, "Set") && !checkPropCardinality(propName, "List")  ){
					propertyFilter = true;
				}
			}
		}
		// If filter-properties were passed in, then look for nodes that have those values.
		ArrayList<TitanVertex> returnVertList = new ArrayList<TitanVertex>();
		Iterable<TitanVertex> qResult = null;
		Iterator<TitanVertex> resultI = null;
		try {
			qResult = startNodeVal.query().vertices();
			resultI = qResult.iterator();
		}
		catch( NullPointerException npe ){
			throw new AAIException("AAI_6125", "Titan null pointer exception trying to get nodes connected to vertexId = " + 
					startNodeVal.id() + ", aai-node-type = [" + startNodeVal.property("aai-node-type") + "]."); 
		}

		while( resultI != null && resultI.hasNext() ){
			boolean addThisOne = true;
			TitanVertex tmpV = (TitanVertex)resultI.next();			
			if( tmpV == null ){
				LOGGER.info("Titan gave a null vertex when looking for nodes connected to vertexId = " + 
						startNodeVal.id() + ", aai-node-type = [" + startNodeVal.property("aai-node-type") + "].");
				// Note - we will skip this one, but try to return any others that we find.
				addThisOne = false;
			}

		else {
				String tmpVid = tmpV.id().toString();
				if( nodeTypeFilter ){
					Object nto = tmpV.<Object>property("aai-node-type").orElse(null);
					if( nto == null || !nto.toString().equals(nodeType) ){ 
						//LOGGER.info("Found a connected vertex (vertexId = " + 
						//		tmpVid + "), but we will not collect it.  It had aai-node-type [" +
						//		nto + "], we are looking for [" + nodeType + "]. ");
						// Note - we will skip this one, but try to return any others that we find.
						addThisOne = false;
					}
				}
				
				if( excludeVidList.contains(tmpVid) ){
					LOGGER.info("Found a connected vertex (vertexId = " + 
							tmpVid + "), but will exclude it since it is on an IN edge and this nodeType " +
							startNodeNT + " can be recursively attached.");
					// Note - we will skip this one, but try to return any others that we find.
					addThisOne = false;
				}
			
				if( propertyFilter ){
					Iterator <?> it = propFilterHash.entrySet().iterator();
					while( it.hasNext() ){
						Map.Entry <?,?>propEntry = (Map.Entry<?,?>)it.next();
						String propName = (propEntry.getKey()).toString();
						if( checkPropCardinality(propName, "Set") || checkPropCardinality(propName, "List")  ){
							// Don't allow search on properties that do not have SINGLE cardinality
							continue;
						}
						Object propVal =  propEntry.getValue();
						Object foundVal = tmpV.<Object>property(propName).orElse(null);
						if( foundVal != null && propVal != null && !foundVal.toString().equals(propVal.toString()) ){
							addThisOne = false;
							break;
						}
						else if( (foundVal == null && propVal != null) || (foundVal != null && propVal == null) ){
							addThisOne = false;
							break;
						}
					}
				}
			}
			if( addThisOne ){
				// This node passed the tests -- put it on the return List
				returnVertList.add( (TitanVertex)tmpV );
			}
		}
		//aaiLogger.debug(logline, " end ");
		return returnVertList;

	}// End of getConnectedNodes()


	/**
	 * Gets the connected nodes.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propFilterHash the prop filter hash
	 * @param startNodeVal the start node val
	 * @param apiVersion the api version
	 * @return the connected nodes
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static ArrayList<TitanVertex> getConnectedNodes(String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> propFilterHash, TitanVertex startNodeVal, String apiVersion ) throws AAIException {
		return getConnectedNodes( transId,  fromAppId,  graph,  nodeType,
				propFilterHash,  startNodeVal,  apiVersion, true );
	}
	
	/**
	 * Gets the connected nodes.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param nodeType the node type
	 * @param propFilterHash the prop filter hash
	 * @param startNodeVal the start node val
	 * @return the connected nodes
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static ArrayList<TitanVertex> getConnectedNodes(String transId, String fromAppId, TitanTransaction graph, String nodeType,
			HashMap<String,Object> propFilterHash, TitanVertex startNodeVal ) throws AAIException {
		return getConnectedNodes( transId,  fromAppId,  graph,  nodeType,
				propFilterHash,  startNodeVal,  null, true );

	}
	
	/**
	 * Ip address format OK.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param addrVal the addr val
	 * @param addrVer the addr ver
	 * @param apiVersion the api version
	 * @return Boolean
	 * @throws AAIException the AAI exception
	 */
	public static Boolean ipAddressFormatOK(String transId, String fromAppId, String addrVal, String addrVer, String apiVersion) throws AAIException{

		/* NOTE -- the google methods we use do not allow leading zeros in ipV4 addresses. 
		 *     So it will reject, "22.33.44.001"
		 */

		if( addrVal == null ){
			throw new AAIException("AAI_6120", "Bad data (addrVal = null) passed to ipAddressFormatOK()"); 
		}
		else if( addrVer == null ){
			throw new AAIException("AAI_6120", "Bad data (addrType = null) passed to ipAddressFormatOK()"); 
		}

		Boolean retVal = false;
		Boolean lookingForV4 = false;
		Boolean lookingForV6 = false;
		InetAddress inetAddr = null;

		if( addrVer.equalsIgnoreCase("v4") || addrVer.equals("ipv4") || addrVer.equals("4")){
			lookingForV4 = true;
		}
		else if( addrVer.equalsIgnoreCase("v6") || addrVer.equals("ipv6") || addrVer.equals("6")){
			lookingForV6 = true;
		}
		else {
			throw new AAIException("AAI_6120", " Bad data for addressVersion [" + addrVer + "] passed to ipAddressFormatOK()"); 
		}

		try {
			inetAddr = InetAddresses.forString(addrVal);
			if( inetAddr instanceof Inet4Address ){
				if( lookingForV4 ){
					retVal = true;
				}
				else {
					throw new AAIException("AAI_6120", "Bad data. Address is a V4, but addressType said it should be V6.  [" 
							+ addrVal + "], [" + addrVer + "] passed to ipAddressFormatOK()"); 
				}
			}
			else if( inetAddr instanceof Inet6Address ){
				if( lookingForV6 ){
					retVal = true;
				}
				else {
					throw new AAIException("AAI_6120", "Bad data. Address is a V6, but addressType said it should be V4.  [" 
							+ addrVal + "], [" + addrVer + "] passed to ipAddressFormatOK()."); 
				}
			} 	 
		} 
		catch (IllegalArgumentException e) {
			throw new AAIException("AAI_6120", "Badly formed ip-address:  [" + addrVal + "] passed to ipAddressFormatOK()"); 
		}

		return retVal;

	}//end of ipAddressFormatOk()
	
	/**
	 * Ip address format OK.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param addrVal the addr val
	 * @param addrVer the addr ver
	 * @return the boolean
	 * @throws AAIException the AAI exception
	 */
	public static Boolean ipAddressFormatOK(String transId, String fromAppId, String addrVal, String addrVer) throws AAIException{
		return ipAddressFormatOK( transId,  fromAppId,  addrVal,  addrVer, null);
	}
	
	/**
	 * Save aai edge to db.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param edgeLabel the edge label
	 * @param outV the out V
	 * @param inV the in V
	 * @param propHash the prop hash
	 * @param apiVersion the api version
	 * @return TitanEdge
	 * @throws AAIException the AAI exception
	 */
	private static TitanEdge saveAaiEdgeToDb(String transId, String fromAppId, TitanTransaction graph, String edgeLabel, 
			TitanVertex outV, TitanVertex inV, HashMap <String,Object> propHash, String apiVersion) throws AAIException{

		// If this edge doesn't exist yet, then create it.

		// NOTE - the Titan javaDoc says that there might not always be an id for a node.
		//   This is the internal-titan-unique-id, not any of our data.
		//   Not sure how to know when it might be there and when it might not?!
		//   So far, it has worked for all my testing, but this might warrant some
		//   further investigation.

		TitanEdge existingEdge = null;
		String inVId =  inV.id().toString();
		Iterator <Edge> eI = outV.edges(Direction.BOTH, edgeLabel);
		while( eI.hasNext() ){
			TitanEdge ed = (TitanEdge) eI.next();
			TitanVertex otherVtx = (TitanVertex) ed.otherVertex(outV);
			if( (otherVtx.id().toString()).equals(inVId) ){
				// NOTE -?- Not sure -- at some point we might want to check the edgeLabels also since  we might
				//   want to allow two different-type edges between the same two vertexes?  (or maybe not.)
				existingEdge = ed;
				break;
			}
		}

		if( existingEdge != null ){
			// This is just an UPDATE
			for( Map.Entry<String, Object> entry : propHash.entrySet() ){
				LOGGER.debug("update edge property/val = [" + entry.getKey() + "]/[" + entry.getValue() + "]");
				existingEdge.property( entry.getKey(), entry.getValue() );
			}

			return( existingEdge );
		}
		else {
			// This is an ADD
			
			// Uniqueness double-check.   This is just to catch the possibility that at the transaction layer,
			//    if data came in for two identical nodes that point to the same dependant node (for uniqueness), 
			//    we would only be able to catch the problem at the time the edge to the second node is added.   
			//    For example - if they had a VM and then got a request to add two ipAddress nodes, but some 
			//    bad data was passed and those two ipAddress nodes were identical -- we'd want to catch it.
			String outV_NType = outV.<String>property("aai-node-type").orElse(null);
			String inV_NType = inV.<String>property("aai-node-type").orElse(null);
			if( needsADepNode4Uniqueness(transId, fromAppId, outV_NType, apiVersion)  
					&&  nodeTypeACanDependOnB(transId, fromAppId, outV_NType, inV_NType, apiVersion) ){
				// The out-Vertex has a uniqueness dependency on the in-vertex
				// Make sure we haven't already added an node/edge like this in this transaction
				HashMap <String, Object> nodeKeyPropsHash = getNodeKeyPropHash(transId, fromAppId, graph, outV); 
				ArrayList<TitanVertex> resultList = new ArrayList<TitanVertex>();
				resultList = DbMeth.getConnectedNodes("transId", "fromAppId", graph, outV_NType, nodeKeyPropsHash, inV, apiVersion, false);
				if( resultList.size() > 0 ){
					String propInfo = "";
					if( nodeKeyPropsHash != null ){
						propInfo = nodeKeyPropsHash.toString();
					}
					throw new AAIException("AAI_6117", "Failed to add edge.  This node (" + inV_NType + ") already has an edge to a " + outV_NType + 
							" node with kepProps [" + propInfo + "]");  
				}
			}
			else if( needsADepNode4Uniqueness(transId, fromAppId, inV_NType, apiVersion)  
					&&  nodeTypeACanDependOnB(transId, fromAppId, inV_NType, outV_NType, apiVersion) ){ 	
				// The in-Vertex has a uniqueness dependency on the out-vertex
				// Make sure we haven't already added an node/edge like this in this transaction
				HashMap <String, Object> nodeKeyPropsHash = getNodeKeyPropHash(transId, fromAppId, graph, inV);
				ArrayList<TitanVertex> resultList = new ArrayList<TitanVertex>();
				resultList = DbMeth.getConnectedNodes("transId", "fromAppId", graph, inV_NType, nodeKeyPropsHash, outV, apiVersion, false);
				if( resultList.size() > 0 ){
					String propInfo = "";
					if( nodeKeyPropsHash != null ){
						propInfo = nodeKeyPropsHash.toString();
					}
					throw new AAIException("AAI_6117", "Failed to add edge.  This node (" + outV_NType + ") already has an edge to a " + inV_NType + 
							" node with kepProps [" + propInfo + "]");  
				}
			}
			
			
			// We're good to go to add this edge

			TitanEdge tEdge =  outV.addEdge( edgeLabel, inV );
			// Add the properties to the new Edge
			for( Map.Entry<String, Object> entry : propHash.entrySet() ){
				tEdge.property( entry.getKey(), entry.getValue() );
			}
			
			// For (resource-id updates) we need to "touch" the vertices on each side of the edge so
			// anybody working on one of those vertices will know that something (ADDing this edge) has happened.
			touchVertex( transId, fromAppId, inV );
			touchVertex( transId, fromAppId, outV );
			
			return tEdge;
		}

	}// End saveAaiEdgeToDb()

	
	
	/**
	 * Derive edge rule key for this edge.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param tEdge the t edge
	 * @return String - key to look up edgeRule (fromNodeType|toNodeType)
	 * @throws AAIException the AAI exception
	 */
	public static String deriveEdgeRuleKeyForThisEdge( String transId, String fromAppId, TitanTransaction graph,  
			TitanEdge tEdge ) throws AAIException{

		TitanVertex fromVtx = tEdge.outVertex();
		TitanVertex toVtx = tEdge.inVertex();
		String startNodeType = fromVtx.<String>property("aai-node-type").orElse(null);
		String targetNodeType = toVtx.<String>property("aai-node-type").orElse(null);
		String key = startNodeType + "|" + targetNodeType;
		if( EdgeRules.getInstance().hasEdgeRule(startNodeType, targetNodeType) ){
			// We can use the node info in the order they were given
			return( key );
		}
		else {
			key = targetNodeType + "|" + startNodeType;
			if( EdgeRules.getInstance().hasEdgeRule(targetNodeType, startNodeType) ){
				return( key );
			}
			else {
				// Couldn't find a rule for this edge
				throw new AAIException("AAI_6120", "No EdgeRule found for passed nodeTypes: " + startNodeType + ", " 
						+ targetNodeType); 
			}
		}
	}// end of deriveEdgeRuleKeyForThisEdge()
	
	

	/**
	 * Save aai edge to db.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param edgeLabel the edge label
	 * @param outV the out V
	 * @param inV the in V
	 * @param propHash the prop hash
	 * @return the titan edge
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	private static TitanEdge saveAaiEdgeToDb(String transId, String fromAppId, TitanTransaction graph, String edgeLabel, 
			TitanVertex outV, TitanVertex inV, HashMap <String,Object> propHash) throws AAIException{
		return	saveAaiEdgeToDb( transId,  fromAppId,  graph,  edgeLabel, 
				outV,  inV, propHash, null);
	}
	
	/**
	 * Persist aai edge.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param startVert the start vert
	 * @param targetVert the target vert
	 * @param apiVersion the api version
	 * @return the titan edge
	 * @throws AAIException the AAI exception
	 */
	public static TitanEdge persistAaiEdge( String transId, String fromAppId, TitanTransaction graph,  
			TitanVertex startVert, TitanVertex targetVert, String apiVersion ) throws AAIException{
		TitanEdge returnEdge = persistAaiEdge(transId, fromAppId, graph, startVert, targetVert, apiVersion, "");
		return returnEdge;
	}
	
	/**
	 * Persist aai edge.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param startVert the start vert
	 * @param targetVert the target vert
	 * @param apiVersion the api version
	 * @param edgeType the edge type
	 * @return TitanEdge
	 * @throws AAIException the AAI exception
	 */
	public static TitanEdge persistAaiEdge( String transId, String fromAppId, TitanTransaction graph,  
			TitanVertex startVert, TitanVertex targetVert, String apiVersion, String edgeType ) throws AAIException{

		TitanVertex fromVtx = null;
		TitanVertex toVtx = null;
		String startNodeType = startVert.<String>property("aai-node-type").orElse(null);
		String targetNodeType = targetVert.<String>property("aai-node-type").orElse(null);
		String fwdRuleKey = startNodeType + "|" + targetNodeType;
		int fwdRuleCount = 0;
		String fwdRule = "";
		String fwdLabel = "";
		String revRuleKey = targetNodeType + "|" + startNodeType;
		int revRuleCount = 0;	
		String revRule = "";
		String revLabel = "";
		String edRule = "";
		String edLabel = "";
		
		Boolean checkType = false;
		if( (edgeType != null) && edgeType != "" ){
			checkType = true;
		}
		
		// As of 16-07, it is possible to have more than one kind of edge defined between a given 
		// pair of nodeTypes.   So we need to check to see if there is only one possibility, or if
		// we need to look at the edgeType to determine which to use.  
		// NOTE -- we're only supporting having 2 edges between a given pair of nodeTypes and
		//    one and only one of them would have to be a parent-child edge.
		
		if( DbEdgeRules.EdgeRules.containsKey(fwdRuleKey) ){
			Collection <String> edRuleColl = DbEdgeRules.EdgeRules.get(fwdRuleKey);
			Iterator <String> ruleItr = edRuleColl.iterator();
			while( ruleItr.hasNext() ){
				String tmpRule = ruleItr.next();
				String [] rules = tmpRule.split(",");
				String tmpLabel = rules[0];
				String tmpParChild = rules[3];
				if( !checkType 
						|| (checkType && tmpParChild.equals("true") && edgeType.equals("parentChild"))
						|| (checkType && tmpParChild.equals("false") && edgeType.equals("cousin"))   ){
					// Either they didn't want us to check the edgeType or it is a match
					fwdRuleCount++;
					if( fwdRuleCount > 1 ){
						// We found more than one with the given info
						throw new AAIException("AAI_6120", "Multiple EdgeRules found for nodeTypes: [" + startNodeType + "], [" 
								+ targetNodeType + "], edgeType = [" + edgeType + "]."); 
					}
					else {
						fwdRule = tmpRule;
						fwdLabel = tmpLabel;
					}
				}
			}
		}
		
		// Try it the other way also (unless this is the case of a nodeType recursively pointing to itself 
		// Ie. the edge rule:  "model-element|model-element"
		if( !revRuleKey.equals(fwdRuleKey) && DbEdgeRules.EdgeRules.containsKey(revRuleKey) ){
			Collection <String> edRuleColl = DbEdgeRules.EdgeRules.get(revRuleKey);
			Iterator <String> ruleItr = edRuleColl.iterator();
			while( ruleItr.hasNext() ){
				String tmpRule = ruleItr.next();
				String [] rules = tmpRule.split(",");
				String tmpLabel = rules[0];
				String tmpParChild = rules[3];
				if( !checkType 
						|| (checkType && tmpParChild.equals("true") && edgeType.equals("parentChild"))
						|| (checkType && tmpParChild.equals("false") && edgeType.equals("cousin"))   ){
					// Either they didn't want us to check the edgeType or it is a match
					revRuleCount++;
					if( revRuleCount > 1 ){
						// We found more than one with the given info
						throw new AAIException("AAI_6120", "Multiple EdgeRules found for nodeTypes: [" + targetNodeType + "], [" 
								+ startNodeType + "], edgeType = [" + edgeType + "]."); 
					}
					else {
						revRule = tmpRule;
						revLabel = tmpLabel;
					}
				}
			}
		}
			
		if( (fwdRuleCount == 1) && (revRuleCount == 0) ){
			// We can use the node info in the order they were given
			fromVtx = startVert;
			toVtx = targetVert;
			edRule = fwdRule;
			edLabel = fwdLabel;
		}
		else if( (fwdRuleCount == 0) && (revRuleCount == 1) ){
			// We need to switch the vertex order so the edge-direction is correct
			toVtx = startVert;
			fromVtx = targetVert;
			edRule = revRule;
			edLabel = revLabel;
		}
		else if( (fwdRuleCount == 0) && (revRuleCount == 0) ){
			// No edge rule found for this
			throw new AAIException("AAI_6120", "No EdgeRule found for passed nodeTypes: " + startNodeType + ", " + targetNodeType 
					+ "], checkLabelType = [" + edgeType + "]."); 
		}	
		else if( (fwdRuleCount > 0) && (revRuleCount > 0) ){
			// We found more than one with the given info
			throw new AAIException("AAI_6120", "Multiple EdgeRules (fwd and rev) found for nodeTypes: [" + startNodeType + "], [" 
					+ targetNodeType + "], checkLabelType = [" + edgeType + "]."); 
		}
		
		// If we got to this point, we now have a single edge label and we know to and from Vtx.
		
		HashMap <String,Object> edgeParamHash = getEdgeTagPropPutHash4Rule(transId, fromAppId, edRule);
		// We do "source-of-truth" for all edges
		edgeParamHash.put("source-of-truth", fromAppId );

		TitanEdge returnEdge = saveAaiEdgeToDb(transId, fromAppId, graph, edLabel, fromVtx, toVtx, edgeParamHash, apiVersion);

		return returnEdge;

	}
	
	/**
	 * Persist aai edge.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param startVert the start vert
	 * @param targetVert the target vert
	 * @return the titan edge
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static TitanEdge persistAaiEdge( String transId, String fromAppId, TitanTransaction graph,  
			TitanVertex startVert, TitanVertex targetVert ) throws AAIException{
		return persistAaiEdge( transId,  fromAppId,  graph,  
				startVert,  targetVert, null);
	}
	// End persistAaiEdge()


	/**
	 * Persist aai edge.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param edgeLabel the edge label
	 * @param startVert the start vert
	 * @param targetVert the target vert
	 * @param propHash the prop hash
	 * @param addIfNotFound the add if not found
	 * @return the titan edge
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static TitanEdge persistAaiEdge( String transId, String fromAppId, TitanTransaction graph,  
			String edgeLabel, TitanVertex startVert, TitanVertex targetVert, 
			HashMap <String,Object> propHash, Boolean addIfNotFound ) throws AAIException{  

		/*----- This method is depricated ------
		 *  We will ignore the parameters: edgeLabel, propHash and addIfNotFound
		 *  We will use the remaining params to call the newer version of this method
		 */
		TitanEdge returnEdge = persistAaiEdge(transId, fromAppId, graph, startVert, targetVert, null);

		return returnEdge;

	}// End depricated version of persistAaiEdge()


	/**
	 * Persist aai edge with dep params.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param startVert the start vert
	 * @param targetNodeType the target node type
	 * @param targetNodeParamHash the target node param hash
	 * @param apiVersion the api version
	 * @return TitanEdge
	 * @throws AAIException the AAI exception
	 */
	public static TitanEdge persistAaiEdgeWithDepParams( String transId, String fromAppId, TitanTransaction graph,  
			TitanVertex startVert, String targetNodeType, HashMap <String,Object> targetNodeParamHash, String apiVersion) throws AAIException{

		TitanVertex targetVert = getUniqueNodeWithDepParams( transId, fromAppId, graph, targetNodeType, targetNodeParamHash, apiVersion );
		TitanEdge returnEdge = persistAaiEdge(transId, fromAppId, graph, startVert, targetVert, apiVersion);

		return returnEdge;

	}// End persistAaiEdgeWithDepParams()
	
	// Version that lets you pass in an edgeType ("parentChild" or "cousin" since it sometimes cannot be determined 
	/**
	 * Persist aai edge with dep params.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param startVert the start vert
	 * @param targetNodeType the target node type
	 * @param targetNodeParamHash the target node param hash
	 * @param apiVersion the api version
	 * @param edgeType the edge type
	 * @return the titan edge
	 * @throws AAIException the AAI exception
	 */
	// from the two nodeTypes anymore (16-07)
	public static TitanEdge persistAaiEdgeWithDepParams( String transId, String fromAppId, TitanTransaction graph,  
			TitanVertex startVert, String targetNodeType, HashMap <String,Object> targetNodeParamHash, 
			String apiVersion, String edgeType) throws AAIException{
		TitanVertex targetVert = getUniqueNodeWithDepParams( transId, fromAppId, graph, targetNodeType, targetNodeParamHash, apiVersion );
		TitanEdge returnEdge = persistAaiEdge(transId, fromAppId, graph, startVert, targetVert, apiVersion, edgeType);

		return returnEdge;

	}// End persistAaiEdgeWithDepParams()
	
	/**
	 * Persist aai edge with dep params.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param startVert the start vert
	 * @param targetNodeType the target node type
	 * @param targetNodeParamHash the target node param hash
	 * @return the titan edge
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static TitanEdge persistAaiEdgeWithDepParams( String transId, String fromAppId, TitanTransaction graph,  
			TitanVertex startVert, String targetNodeType, HashMap <String,Object> targetNodeParamHash) throws AAIException{
		return persistAaiEdgeWithDepParams(  transId,  fromAppId,  graph,  
				 startVert,  targetNodeType,  targetNodeParamHash, null);
	}

	/**
	 * Gets the node key prop hash.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param vtx the vtx
	 * @return nodeKeyPropHash
	 * @throws AAIException the AAI exception
	 */
	public static HashMap <String, Object> getNodeKeyPropHash( String transId, String fromAppId, TitanTransaction graph, TitanVertex vtx) throws AAIException{

		if( vtx == null ){
			throw new AAIException("AAI_6109", "null node object passed to getNodeKeyPropHash().");
		}
		
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
		String nType = vtx.<String>property("aai-node-type").orElse(null);
		if( ! dbMaps.NodeKeyProps.containsKey(nType) ){
			// Problem if no key Properties defined for this nodeType
			String defVer = AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP);
			throw new AAIException("AAI_6105", "No node-key-properties defined in dbMaps for nodeType = " + nType + " (ver=" + defVer + ")"); 
		}

		HashMap <String,Object>nodeKeyPropsHash = new HashMap<String,Object>();
		Collection <String> keyProps =  dbMaps.NodeKeyProps.get(nType);
		Iterator <String> keyPropI = keyProps.iterator();
		while( keyPropI.hasNext() ){
			String propName = keyPropI.next();
			Object value = (Object) vtx.<Object>property(propName).orElse(null); 
			nodeKeyPropsHash.put(propName, value);
		}

		return nodeKeyPropsHash;

	}// End of getNodeKeyPropHash()

	/**
	 * Gets the node name prop hash.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param vtx the vtx
	 * @param apiVersion the api version
	 * @return nodeKeyPropHash
	 * @throws AAIException the AAI exception
	 */
	public static HashMap <String, Object> getNodeNamePropHash( String transId, String fromAppId, TitanTransaction graph, TitanVertex vtx, String apiVersion) throws AAIException{

		if( vtx == null ){
			throw new AAIException("AAI_6109", "null node object passed to getNodeNamePropHash()." );
		}

		String nType = vtx.<String>property("aai-node-type").orElse(null);
		HashMap <String,Object>nodeNamePropsHash = new HashMap<String,Object>();
		Collection <String> keyProps =  DbMeth.getNodeNameProps(transId, fromAppId, nType, apiVersion);
		Iterator <String> keyPropI = keyProps.iterator();
		while( keyPropI.hasNext() ){
			String propName = keyPropI.next();
			Object value = (Object) vtx.<Object>property(propName).orElse(null); 
			nodeNamePropsHash.put(propName, value);
		}

		return nodeNamePropsHash;

	}// End of getNodeNamePropHash()
	

	/**
	 * Removes the aai edge.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param tEdge the t edge
	 * @return void
	 */
	public static void removeAaiEdge( String transId, String fromAppId, TitanTransaction graph, TitanEdge tEdge){
		// Before removing the edge, touch the vertices on each side so their resource-versions will get updated 
		TitanVertex tmpVIn = tEdge.inVertex();
		touchVertex( transId, fromAppId, tmpVIn );
		 
		TitanVertex tmpVOut = tEdge.outVertex();
		touchVertex( transId, fromAppId, tmpVOut );

		// Remove the passed in edge.
		tEdge.remove();

	}// end of removeAaiEdge()


	/**
	 * Removes the aai node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param thisVtx the this vtx
	 * @param scopeParam the scope param
	 * @param apiVersion the api version
	 * @param resourceVersion the resource version
	 * @throws AAIException the AAI exception
	 */
	public static void removeAaiNode( String transId, String fromAppId, TitanTransaction graph, TitanVertex thisVtx, String scopeParam, 
			String apiVersion, String resourceVersion ) throws AAIException{
		// Note: the resource Version Override flag is only set to true when called by the Model Delete code which
		//    has no way to know the resource-versions of nodes at lower-levels of it's model topology.
		Boolean resVersionOverrideFlag = false;
		removeAaiNode( transId, fromAppId, graph, thisVtx, scopeParam, apiVersion, resourceVersion, resVersionOverrideFlag );
	}
	

	/**
	 *  <pre>
	 *  Possible values for deleteScope can be:
	 *  	USE_DEFAULT - Get the scope from ref data for this node
	 *  	THIS_NODE_ONLY (but should fail if it there are nodes that depend on it for uniqueness)
	 *  	CASCADE_TO_CHILDREN  - will look for OUT-Edges that have parentOf/hasDelTarget = true and follow those down
	 *      ERROR_4_IN_EDGES_OR_CASCADE - combo of error-if-any-IN-edges + CascadeToChildren
	 *  	ERROR_IF_ANY_IN_EDGES - Fail if this node has any existing IN edges 
	 *      ERROR_IF_ANY_EDGES - Fail if this node has any existing edges at all!
	 *  </pre>.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param thisVtx the this vtx
	 * @param scopeParam the scope param
	 * @param apiVersion the api version
	 * @param resourceVersion the resource version
	 * @param resVerOverride the res ver override
	 * @return void
	 * @throws AAIException the AAI exception
	 */
	public static void removeAaiNode( String transId, String fromAppId, TitanTransaction graph, TitanVertex thisVtx, String scopeParam, 
			String apiVersion, String resourceVersion, Boolean resVerOverride ) throws AAIException{
		String nodeType2Del = thisVtx.<String>property("aai-node-type").orElse(null);
		String deleteScope = scopeParam;
		if( scopeParam.equals("USE_DEFAULT") ){
			deleteScope =   getDefaultDeleteScope(transId, fromAppId, nodeType2Del, apiVersion);
		}
		
		if( !resVerOverride && needToDoResourceVerCheck(apiVersion, false) ){
			// Need to check that they knew what they were deleting
			String existingResVer = thisVtx.<String>property("resource-version").orElse(null);
			if( resourceVersion == null || resourceVersion.equals("") ){
				throw new AAIException("AAI_6130", "Resource-version not passed for delete of = " + nodeType2Del); 
			}
			else if( (existingResVer != null) && !resourceVersion.equals(existingResVer) ){
				throw new AAIException("AAI_6131", "Resource-version MISMATCH for delete of = " + nodeType2Del); 
			}
		}

		if( !deleteScope.equals("THIS_NODE_ONLY")
				&& !deleteScope.equals("CASCADE_TO_CHILDREN")
				&& !deleteScope.equals("ERROR_4_IN_EDGES_OR_CASCADE")
				&& !deleteScope.equals("ERROR_IF_ANY_EDGES")
				&& !deleteScope.equals("ERROR_IF_ANY_IN_EDGES") ){
			throw new AAIException("AAI_6120", "Unrecognized value in deleteScope:  [" + deleteScope + "]."); 
		}

		if( deleteScope.equals("ERROR_IF_ANY_EDGES") ){
			if ( thisVtx.edges(Direction.BOTH).hasNext() ) {
				throw new AAIException("AAI_6110", "Node cannot be deleted because it still has Edges and the ERROR_IF_ANY_EDGES scope was used."); 
			}
		} 
		else if( deleteScope.equals("ERROR_IF_ANY_IN_EDGES") || deleteScope.equals("ERROR_4_IN_EDGES_OR_CASCADE") ){
			Iterator <Edge> eI = thisVtx.edges(Direction.IN);
			boolean onlyHasParent = false;
			Edge temp = null;
			if( eI != null && eI.hasNext() ){
				temp = eI.next();
				Boolean isParent = temp.<Boolean>property("isParent").orElse(null);
				if (isParent != null && isParent && !eI.hasNext()) {
					onlyHasParent = true;
				}
				
				if (!onlyHasParent) {
					throw new AAIException("AAI_6110", "Node cannot be deleted because it still has Edges and the " + deleteScope + " scope was used.");
				}
			}
		}
		else if( deleteScope.equals("THIS_NODE_ONLY")){
			// Make sure nobody depends on this node.
			Iterator<Edge> eI = thisVtx.edges(Direction.BOTH);
			while( eI.hasNext() ){
				TitanEdge ed = (TitanEdge) eI.next();
				TitanVertex otherVtx = (TitanVertex) ed.otherVertex(thisVtx);
				String nodeTypeA = otherVtx.<String>property("aai-node-type").orElse(null);
				if( nodeTypeACanDependOnB(transId, fromAppId, nodeTypeA, nodeType2Del, apiVersion)){
					// We're only supposed to delete this node - but another node is dependant on it,
					// so we shouldn't delete this one.
					throw new AAIException("AAI_6110", "Node cannot be deleted using scope = " + deleteScope + 
							" another node (type = " + nodeTypeA + ") depends on it for uniqueness."); 
				}
			}
		}

		// We've passed our checks - so do some deleting of edges and maybe pass 
		//     the delete request down to children or delete-targets.

		// First we deal with the "IN"-Edges which can't have children/delete-targets which
		// by definition (of "IN") on the other end
		Iterator <Edge> eI_In = thisVtx.edges(Direction.IN);
		while( eI_In.hasNext() ){
			TitanEdge ed = (TitanEdge) eI_In.next();
			
			//- "touch" vertex on other side of this edge so it gets a fresh resource-version
			TitanVertex tmpVOther = ed.otherVertex(thisVtx);
			touchVertex( transId, fromAppId, tmpVOther );
			
			ed.remove();
		}

		// Now look at the "OUT"-edges which might include children or delete-targets
		String cascadeMsg = "This nt = " + nodeType2Del + ", Cascading del to: ";
		Iterator <Edge> eI_Out = thisVtx.edges(Direction.OUT);
		if( !eI_Out.hasNext() ){
			cascadeMsg = cascadeMsg + "[no children for this node]";
		}
		while( eI_Out.hasNext() ){
			TitanEdge ed = (TitanEdge) eI_Out.next();
			
			// "touch" vertex on other side of this edge so it gets a fresh resource-version
			TitanVertex tmpVOther = ed.otherVertex(thisVtx);
			touchVertex( transId, fromAppId, tmpVOther );

			Boolean otherVtxAChild = ed.<Boolean>property("isParent").orElse(null);
			if( otherVtxAChild == null ){
				otherVtxAChild = false;
			}

			Boolean otherVtxADeleteTarget = ed.<Boolean>property("hasDelTarget").orElse(null);
			if( otherVtxADeleteTarget == null ){
				otherVtxADeleteTarget = false;
			}

			if( (otherVtxAChild || otherVtxADeleteTarget) && 
					(deleteScope.equals("CASCADE_TO_CHILDREN") || deleteScope.equals("ERROR_4_IN_EDGES_OR_CASCADE")) ){
				// Delete the edge to the child and Pass the delete down to it.
				ed.remove();
				TitanVertex otherVtx = (TitanVertex) ed.otherVertex(thisVtx);
				String vid = otherVtx.id().toString();
				String nty = otherVtx.<String>property("aai-node-type").orElse(null);
				String resVers = otherVtx.<String>property("resource-version").orElse(null);
				cascadeMsg = cascadeMsg + "[" + nty + ":" + vid + "]";
				removeAaiNode(transId, fromAppId, graph, otherVtx, "CASCADE_TO_CHILDREN", apiVersion, resVers);
			}
			else {
				// The other node is not a child or deleteTarget.  Delete the edge to it if it is not
				// dependent (Should never be dependent since it's not a child/delTarget...  but 
				// someone could create a node that was dependent for Uniqueness without
				// being a child/target.

				// DEBUG -- eventually add the check for dependancy that isn't on a parent-type or delTarget-type edge
				ed.remove();
			}
		}
		
		LOGGER.info(cascadeMsg);

		Iterator<Edge> eI = thisVtx.edges(Direction.BOTH);
		if( ! eI.hasNext() ){
			// By this point, either there were no edges to deal with, or we have dealt with them.
			thisVtx.remove();
		}
		else {
			// Something went wrong and we couldn't delete all the edges for this guy.
			throw new AAIException("AAI_6110", "Node could be deleted because it unexpectedly still has Edges.\n"); 
		}
	}
	
	
	/**
	 * Removes the aai node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param thisVtx the this vtx
	 * @param scopeParam the scope param
	 * @return void
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static void removeAaiNode( String transId, String fromAppId, TitanTransaction graph, TitanVertex thisVtx, String scopeParam) throws AAIException{
		removeAaiNode(transId, fromAppId, graph, thisVtx, scopeParam, null, null);
	}
	
	/**
	 * Removes the aai node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param thisVtx the this vtx
	 * @param scopeParam the scope param
	 * @param apiVersion the api version
	 * @throws AAIException the AAI exception
	 */
	@Deprecated
	public static void removeAaiNode( String transId, String fromAppId, TitanTransaction graph, TitanVertex thisVtx, String scopeParam, 
			String apiVersion ) throws AAIException{
		removeAaiNode(transId, fromAppId, graph, thisVtx, scopeParam, apiVersion, null);
	}
	// end of removeAaiNode()


	/**
	 * Delete all graph data.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @return void
	 */
	public static void deleteAllGraphData( String transId, String fromAppId, TitanGraph graph ){
		/** ======================================================================
		 * WARNING -- this removes ALL the data that is currently in the graph.
		 * ======================================================================
		 **/
		 LOGGER.warn("deleteAllGraphData called! Run for the hills!");
		Iterator<Edge> edges = graph.edges(Direction.BOTH);
		graph.tx().commit();
		Edge edge = null;
		while (edges.hasNext()) {
			edge = edges.next();
			edges.remove();
		}
		graph.tx().commit();
		Iterator<Vertex> vertices = graph.vertices();
		graph.tx().commit();
		Vertex vertex = null;
		while (vertices.hasNext()) {
			vertex = vertices.next();
			vertex.remove();
		}
		graph.tx().commit();
	}


	/**
	 * Show all edges for node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param tVert the t vert
	 * @return the array list
	 */
	public static ArrayList <String> showAllEdgesForNode( String transId, String fromAppId, TitanVertex tVert ){ 

		ArrayList <String> retArr = new ArrayList <String> ();
		Iterator <Edge> eI = tVert.edges(Direction.IN);
		if( ! eI.hasNext() ){
			retArr.add("No IN edges were found for this vertex. ");
		}
		while( eI.hasNext() ){
			TitanEdge ed = (TitanEdge) eI.next();
			String lab = ed.label();
			TitanVertex vtx = (TitanVertex) ed.otherVertex(tVert);
			if( vtx == null ){
				retArr.add(" >>> COULD NOT FIND VERTEX on the other side of this edge edgeId = " + ed.id() + " <<< ");
			}
			else {
				String nType = vtx.<String>property("aai-node-type").orElse(null);
				String vid = vtx.id().toString();
				retArr.add("Found an IN edge (" + lab + ") to this vertex from a [" + nType + "] node with VtxId = " + vid );
				//DEBUG ---
				//showPropertiesForEdge(  transId, fromAppId, ed );
			}
		}
		
		eI = tVert.edges(Direction.OUT);
		if( ! eI.hasNext() ){
			retArr.add("No OUT edges were found for this vertex. ");
		}
		while( eI.hasNext() ){
			TitanEdge ed = (TitanEdge) eI.next();
			String lab = ed.label();
			TitanVertex vtx = (TitanVertex) ed.otherVertex(tVert);
			if( vtx == null ){
				retArr.add(" >>> COULD NOT FIND VERTEX on the other side of this edge edgeId = " + ed.id() + " <<< ");
			}
			else {
				String nType = vtx.<String>property("aai-node-type").orElse(null);
				String vid = vtx.id().toString();
				retArr.add("Found an OUT edge (" + lab + ") from this vertex to a [" + nType + "] node with VtxId = " + vid );
				//DEBUG ---
				//showPropertiesForEdge(  transId, fromAppId, ed );
			}
		}
		return retArr;
	}

	
	/**
	 * Show properties for node.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param tVert the t vert
	 * @return the array list
	 */
	public static ArrayList <String> showPropertiesForNode( String transId, String fromAppId, TitanVertex tVert ){ 

		ArrayList <String> retArr = new ArrayList <String> ();
		if( tVert == null ){
			retArr.add("null Node object passed to showPropertiesForNode()\n");
		}
		else {
			String nodeType = "";
			//String datType = "";
			Object ob = tVert.<Object>property("aai-node-type").orElse(null);
			if( ob == null ){
				nodeType = "null";
			}
			else{
				nodeType = ob.toString();
				//datType = ob.getClass().getSimpleName();
			}
			
			retArr.add(" AAINodeType/VtxID for this Node = [" + nodeType + "/" + tVert.id() + "]");
			retArr.add(" Property Detail: ");
			Iterator<VertexProperty<Object>> pI = tVert.properties();
			while( pI.hasNext() ){
				VertexProperty<Object> tp = pI.next();
				Object val = tp.value();
				//retArr.add("Prop: [" + tp.getPropertyKey() + "], val = [" + val + "], dataType = " + val.getClass() );
				retArr.add("Prop: [" + tp.key() + "], val = [" + val + "] ");
			}
		}
		return retArr;
	}

	
	/**
	 * Gets the node name props.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param nodeType the node type
	 * @param apiVersion the api version
	 * @return HashMap of keyProperties
	 * @throws AAIException the AAI exception
	 */
	public static Collection <String> getNodeNameProps( String transId, String fromAppId, String nodeType, String apiVersion ) throws AAIException{

		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
		Collection <String> nameProps = new ArrayList <String>();
		if( dbMaps.NodeNameProps.containsKey(nodeType) ){
			nameProps = dbMaps.NodeNameProps.get(nodeType);
		}
		else if( DbEdgeRules.NodeTypeCategory.containsKey(nodeType) ){
			// The passed-in nodeType was really a nodeCategory, theoretically, all the guys in the same 
			// category should have the same name property -- so if they just give us the category, we will
			// just give the name info from the first nodeType we encounter of that category.
			Collection <String> nTypeCatCol = DbEdgeRules.NodeTypeCategory.get(nodeType);
			Iterator <String> catItr = nTypeCatCol.iterator();
			String catInfo = "";
			if( catItr.hasNext() ){
				// For now, we only look for one.
				catInfo = catItr.next();
			}
			else {
				throw new AAIException("AAI_6105", "Required Property name(s) not found for nodeType = " + nodeType); 
			}

			String [] flds = catInfo.split(",");
			if( flds.length != 4 ){
				throw new AAIException("AAI_6121", "Bad EdgeRule.NodeTypeCategory data (itemCount=" + flds.length + ") for nodeType = [" + nodeType + "]."); 
			}

			String nodeTypesString = flds[0];
			String [] nodeTypeNames = nodeTypesString.split("\\|");
			if( nodeTypeNames != null && nodeTypeNames.length > 0 ){
				// We'll just use the first one
				String nt = nodeTypeNames[0];
				nameProps = dbMaps.NodeNameProps.get(nt);
			}
		}
		
		
		// Note - it's ok if there was no defined name property for this nodeType.
		
		return nameProps;

	}// end of getNodeKeyPropNames
	
	
	/**
	 * Gets the edge tag prop put hash 4 rule.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param edRule the ed rule
	 * @return the edge tag prop put hash 4 rule
	 * @throws AAIException the AAI exception
	 */
	public static HashMap <String,Object> getEdgeTagPropPutHash4Rule( String transId, String fromAppId, String edRule ) 
			throws AAIException{ 
		// For a given edgeRule - already pulled out of DbEdgeRules.EdgeRules --  parse out the "tags" that 
		//     need to be set for this kind of edge.  
		// These are the Boolean properties like, "isParent", "usesResource" etc.  
		HashMap <String,Object> retEdgePropPutMap = new HashMap <String,Object>();
		
		if( (edRule == null) || edRule.equals("") ){
			// No edge rule found for this
			throw new AAIException("AAI_6120", "blank edRule passed to getEdgeTagPropPutHash4Rule()"); 
		}
			
		int tagCount = DbEdgeRules.EdgeInfoMap.size();
		String [] rules = edRule.split(",");
		if( rules.length != tagCount ){
			throw new AAIException("AAI_6121", "Bad EdgeRule data (itemCount =" + rules.length + ") for rule = [" + edRule  + "]."); 
		}

		// In DbEdgeRules.EdgeRules -- What we have as "edRule" is a comma-delimited set of strings.
		// The first item is the edgeLabel.
		// The second in the list is always "direction" which is always OUT for the way we've implemented it.
		// Items starting at "firstTagIndex" and up are all assumed to be booleans that map according to 
		// tags as defined in EdgeInfoMap.
		// Note - if they are tagged as 'reverse', that means they get the tag name with "-REV" on it
		for( int i = DbEdgeRules.firstTagIndex; i < tagCount; i++ ){
			String booleanStr = rules[i];
			Integer mapKey = new Integer(i);
			String propName = DbEdgeRules.EdgeInfoMap.get(mapKey);
			String revPropName = propName + "-REV";
			
			if( booleanStr.equals("true") ){
				retEdgePropPutMap.put(propName, true);
				retEdgePropPutMap.put(revPropName,false);
			}
			else if( booleanStr.equals("false") ){
				retEdgePropPutMap.put(propName, false);
				retEdgePropPutMap.put(revPropName,false);
			}
			else if( booleanStr.equals("reverse") ){
				retEdgePropPutMap.put(propName, false);
				retEdgePropPutMap.put(revPropName,true);
			}
			else {
				throw new AAIException("AAI_6121", "Bad EdgeRule data for rule = [" + edRule + "], val = [" + booleanStr + "]."); 
			}
			
		}

		return retEdgePropPutMap;
		
	} // End of getEdgeTagPropPutHash()


	
	/**
	 * Gets the edge tag prop put hash.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param edgeRuleKey the edge rule key
	 * @return the edge tag prop put hash
	 * @throws AAIException the AAI exception
	 */
	public static Map<String, EdgeRule> getEdgeTagPropPutHash( String transId, String fromAppId, String edgeRuleKey ) 
			throws AAIException{ 
		// For a given edgeRuleKey (nodeTypeA|nodeTypeB), look up the rule that goes with it in
		// DbEdgeRules.EdgeRules and parse out the "tags" that need to be set on each edge.  
		// These are the Boolean properties like, "isParent", "usesResource" etc.  
		// Note - this code is also used by the updateEdgeTags.java code

		String[] edgeRuleKeys = edgeRuleKey.split("\\|");
		
		if (edgeRuleKeys.length < 2 || ! EdgeRules.getInstance().hasEdgeRule(edgeRuleKeys[0], edgeRuleKeys[1])) {
			throw new AAIException("AAI_6120", "Could not find an DbEdgeRule entry for passed edgeRuleKey (nodeTypeA|nodeTypeB): " + edgeRuleKey + "."); 
		}
		
		Map<String, EdgeRule> edgeRules = EdgeRules.getInstance().getEdgeRules(edgeRuleKeys[0], edgeRuleKeys[1]);
		
		return edgeRules;
		
	} // End of getEdgeTagPropPutHash()

	
	/**
	 * This property was put by newer version of code.
	 *
	 * @param apiVersionStr the api version str
	 * @param nodeType the node type
	 * @param propName the prop name
	 * @return true, if successful
	 * @throws AAIException the AAI exception
	 */
	private static boolean  thisPropertyWasPutByNewerVersionOfCode( String apiVersionStr, 
					String nodeType, String propName) throws AAIException{
		// We want to return True if the nodeType + property-name combo was introduced AFTER the apiVersion passed.
		
		int apiVerInt = 0;
		int propIntroVerInt = 0;
		
		if( apiVersionStr == null || apiVersionStr.equals("") ){
			apiVersionStr = org.openecomp.aai.util.AAIApiVersion.get();
		}
		apiVerInt = getVerNumFromVerString(apiVersionStr);
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		String propIntroKey = nodeType + "|" + propName;
		if( propName.equals("prov-status") ){
			// This is a special case -- The dbMaps from v2 has it in there, but it was introduced half way through.  So
			// it needs to be catogorized as v3.
			propIntroVerInt = 3;
		}
		else if( ! dbMaps.PropertyVersionInfoMap.containsKey(propIntroKey) ){
			String detail = propIntroKey + " [" + propIntroKey + "] not found in dbMaps.PropertyVersionInfoMap."; 
			throw new AAIException("AAI_6121", detail); 
		}
		else {
			String propIntroVerString = dbMaps.PropertyVersionInfoMap.get(propIntroKey);
			propIntroVerInt = getVerNumFromVerString( propIntroVerString );
		}
		
		if( propIntroVerInt > apiVerInt ){
			return true;
		}
		else {
			return false;
		}
		
	} // End of thisPropertyWasPutByNewerVersionOfCode()

	
	/**
	 * Touch vertex.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param v the v
	 * @return void
	 */
	public static void touchVertex( String transId, String fromAppId, TitanVertex v ){
		// We want to "touch" the vertex -- Ie. update it's last-mod-date, last-mod- resource-version to the current date/time
		if( v != null ){
			long unixTimeNow = System.currentTimeMillis() / 1000L;
			String timeNowInSec = "" + unixTimeNow;
			v.property( "aai-last-mod-ts", timeNowInSec );
			v.property( "resource-version", timeNowInSec );
			v.property( "last-mod-source-of-truth", fromAppId );
		}
	} // End of touchVertex()
	
	
	/**
	 * Check prop cardinality.
	 *
	 * @param propName the prop name
	 * @param cardinalityType the cardinality type
	 * @return boolean
	 * @throws AAIException the AAI exception
	 */
	public static boolean checkPropCardinality( String propName, String cardinalityType ) throws AAIException {
		
		// Return true if the named property is tagged in our dbMaps PropetyDataTypeMap as 
		// having the passed in cardinality type.  
		// NOTE: supported cardinality types in dbMaps = "Set" or "List"
		// In Titan (and ex5.json), those go in as "SET" and "LIST"
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
		if( dbMaps.PropertyDataTypeMap.containsKey(propName) ){
			String propDataType = dbMaps.PropertyDataTypeMap.get(propName);
			if( propDataType != null && propDataType.startsWith(cardinalityType) ){
				return true;
			}
		}
		return false;
		
	} // End of checkPropCardinality()
	
	/**
	 * Convert type if needed.
	 *
	 * @param propName the prop name
	 * @param val the val
	 * @return convertedValue (if it was a String but needed to be a Boolean)
	 * @throws AAIException the AAI exception
	 */
	public static Object convertTypeIfNeeded( String propName, Object val )
			throws AAIException {
		// Make sure the dataType of the passed-in Object matches what the DB expects
		
		// NOTE: since this is a fix very late in our dev cycle, we'll just fix the scenarios that
		//   we're having trouble with which is Strings getting into the db which should be going in as 
		//   Booleans or Integers.
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
		if( dbMaps.PropertyDataTypeMap.containsKey(propName) ){
			String dbExpectedDataType = dbMaps.PropertyDataTypeMap.get(propName);
			if( dbExpectedDataType != null 
					&& dbExpectedDataType.equals("Boolean") 
					&& val != null
					&& !(val instanceof Boolean) ){
				String valStr = val.toString().trim();
				if( valStr.equals("true") || valStr.equals("True") || valStr.equals("TRUE") ){
					return Boolean.valueOf("true");
				}
				else if( valStr.equals("false") || valStr.equals("False") || valStr.equals("FALSE") ){
					return Boolean.valueOf("false");
				}
				else {
					String emsg = "Error trying to convert value: [" + valStr + "] to a Boolean for property + " + propName + "\n";
					throw new AAIException("AAI_6120", emsg); 
				}
			}
			else if( dbExpectedDataType != null 
					&& dbExpectedDataType.equals("Integer") 
					&& val != null 
					&& !(val.toString().trim().equals("")) 
					&& !(val instanceof Integer) ){
				String valStr = val.toString().trim();
				Integer newInt;
				try {
					newInt = Integer.valueOf(valStr);
					return newInt;
				}
				catch( Exception e ){
					String emsg = "Error trying to convert value: [" + valStr + "] to an Integer for property + " + propName + "\n";
					throw new AAIException("AAI_6120", emsg); 
				}
			}
		}
		
		// If it didn't need to be converted, just return it.
		return val;
	
	} // End of convertTypeIfNeeded()

	
	
	/**
	 * This vertex not reachable.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param v the v
	 * @param version the version
	 * @return boolean
	 */
	public static boolean thisVertexNotReachable( String transId, String fromAppId, TitanTransaction graph, TitanVertex v, String version){
		if( v == null ){
			return true;   
		}
		else {
			try {
				 v.id().toString();
			}
			catch( Exception ex ){
				// Could not get this -- sometimes we're holding a vertex object that has gotten deleted, so
				// when we try to get stuff from it, we get an "Element Has Been Removed" error from Titan
				return true;
			}
		}
		
		return false;
		
	} // End of thisVertexNotReachable()

	/**
	 * This vertex has bad edges.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param v the v
	 * @param version the version
	 * @return boolean
	 */
	public static boolean thisVertexHasBadEdges( String transId, String fromAppId, TitanTransaction graph, TitanVertex v, String version){
		
		Iterator <Edge> eItor = v.edges(Direction.BOTH);
		while( eItor.hasNext() ){
			Edge e = null;
			e = eItor.next();
			if( e == null ){
				return true;
			}
			Vertex vIn = e.inVertex();
			if( (vIn == null) || (vIn.<String>property("aai-node-type").orElse(null) == null) ){
				 // this is a bad edge because it points to a vertex that isn't there anymore
				 return true;
			}
			
			Vertex vOut = e.outVertex();
			if( (vOut == null) || (vOut.<String>property("aai-node-type").orElse(null) == null) ){
				 // this is a bad edge because it points to a vertex that isn't there anymore
				 return true;
			}
		}
		
		// If we made it to here, the vertex's edges must be ok.
		return false;
		
	} // End of thisVertexHasBadEdges()
	
	
	/**
	 * This vertex is A phantom.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param v the v
	 * @param version the version
	 * @return boolean
	 * @throws AAIException the AAI exception
	 */
	public static boolean thisVertexIsAPhantom( String transId, String fromAppId, TitanTransaction graph, TitanVertex v, String version ) 
			throws AAIException {
		
		
		// The kind of Phantom we're looking for is the kind that we sometimes get when we do a select without
		// using key properties.  They can be in the database as a vertex, but the indexes that should point to 
		// them are not working -- so they cannot be used by normal interfaces (like the REST API) which means
		// that if we return it, it can mess up a caller who tries to use it.
		if( v == null ){
			return true;   
		}
		String thisVid =  v.id().toString();
		
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
		Object propOb = v.<Object>property("aai-node-type").orElse(null);
		if( propOb == null ){
			// This vertex does not have an aai-node-type ---> it is messed up
			return true;
		}
		String nType = propOb.toString();
		if(  ! dbMaps.NodeKeyProps.containsKey(nType) ){
			// This node Type does not have keys defined
			// This could just be bad reference data, so we will not flag this guy, but we
			// can't really do our test...
			return false;
		}
		
		HashMap <String,Object> propHashWithKeys = new HashMap<String, Object>();
		Collection <String> keyProps = null;
		try {
			keyProps = getNodeKeyPropNames(transId, fromAppId, nType, version);
		}
		catch (AAIException ex) {
			// something wrong with getting this guy's key property names - we'll abandon this test...
			return false;
		}
	
        Iterator <String> keyPropI = keyProps.iterator();
        while( keyPropI.hasNext() ){
        	String propName = keyPropI.next();
        	String propVal = "";
        	Object ob = v.<Object>property(propName).orElse(null);
        	if( ob != null ){
        		propVal = ob.toString();
        	}
        	propHashWithKeys.put(propName, propVal);
        }
        try {
        	// Note - We can get more than one back since some nodes need a dep. node for uniqueness.
        	//   We don't care about that -- we just want to make sure we can get this vertex back when
        	//   we're searching with it's indexed fields. 
        	// NOTE - we're passing the skipGroomCheck to getNodes so we don't wind up in an infinite loop
        	ArrayList <TitanVertex> vertList2 = getNodes( transId, fromAppId, graph, nType, propHashWithKeys, false, version, true ); 
        	Iterator<TitanVertex> iter2 = vertList2.iterator(); 
            while( iter2.hasNext() ){ 
            	TitanVertex tvx2 = iter2.next(); 
            	String foundId = tvx2.id().toString();
            	if( foundId.equals( thisVid ) ){
            		// We could get back the vertex by looking it up using key properties...  That's good.
            		return false;
            	}
            }
        }
        catch (Exception e2) {
			//String msg = " Error encountered for this vertex id: [" + thisVid + 
			//		"]. Caught this exception: " + e2.toString();
			// Something messed up - but that doesn't prove that this is a phantom.
			return false;
		}
		
        // If we dropped down to here, we have looked but could not pull the vertex out of the
        //    db using it's key fields, so it gets flagged as a Phantom.
		return true;
	
	} // End of thisVertexIsAPhantom()
	
	
	/**
	 * Gets the node by unique key.
	 *
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param graph the graph
	 * @param aaiUniquekey the aai uniquekey
	 * @return the node by unique key
	 */
	public TitanVertex getNodeByUniqueKey(String transId, String fromAppId, TitanTransaction graph, String aaiUniquekey) {
		
		TitanVertex vert = null;
		
		Iterator<?> vertI =  graph.query().has("aai-unique-key", aaiUniquekey).vertices().iterator(); 
						
		if( vertI != null && vertI.hasNext()) {
			// We found a vertex that meets the input criteria. 
			vert = (TitanVertex) vertI.next();
		}
		
		return vert;
	}



}

