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

package org.openecomp.aai.util;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.springframework.web.util.UriUtils;

import org.openecomp.aai.domain.model.AAIResource;
import org.openecomp.aai.domain.model.AAIResourceKey;
import org.openecomp.aai.domain.model.AAIResourceKeys;
import org.openecomp.aai.domain.model.AAIResources;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.extensions.AAIExtensionMap;
import org.openecomp.aai.ingestModel.DbMaps;
import org.openecomp.aai.ingestModel.IngestModelMoxyOxm;
import com.google.common.base.CaseFormat;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanTransaction;
import com.thinkaurelius.titan.core.TitanVertex;

public class RestURL {
	
	
	/*
	 * method returns a REST URL for the given node based on its nodetype and key
	 * information 
	 */	

	/**
	 * Gets the.
	 *
	 * @param graph the graph
	 * @param node the node
	 * @param apiVersion the api version
	 * @param isLegacyVserverUEB the is legacy vserver UEB
	 * @param isCallbackurl the is callbackurl
	 * @return the string
	 * @throws AAIException the AAI exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static String get(TitanTransaction graph, TitanVertex node, String apiVersion, Boolean isLegacyVserverUEB, Boolean isCallbackurl) throws AAIException, UnsupportedEncodingException
	{
		String nodeType = node.<String>property("aai-node-type").orElse(null);
		String url = "";
     	String currentNodeType = nodeType;
     	Boolean noMoreDependentNodes = true;
     	TitanVertex currentNode = node;

     	// if the caller supplies an apiVersion we'll use it, otherwise we'll just
     	// reflect back from the called URI
       	if (apiVersion == null) { 
       		apiVersion = AAIApiVersion.get();
     	}
			
     	String nodeURI = null;
     	if (Boolean.parseBoolean(AAIConfig.get("aai.use.unique.key", "false")))
     		nodeURI = node.<String>property("aai-unique-key").orElse(null);
     	
		if (nodeURI != null && !nodeURI.equals("")) {
	    	if (isCallbackurl) {
	    		url = AAIConfig.get(AAIConstants.AAI_GLOBAL_CALLBACK_URL) + apiVersion + "/" + nodeURI;
	    		return url;
	    	} else {
	    		url = AAIApiServerURLBase.get() + apiVersion + "/" + nodeURI;
				return url;
	    	}
		}
		
       	// TODO
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
     	// add the url component for the dependent on nodes for the node passed in
     	while (noMoreDependentNodes) {
        	Collection <String> depNodeTypeColl =  dbMaps.NodeDependencies.get(currentNodeType);
         	Iterator <String> depNodeTypeListIterator = (Iterator<String>) depNodeTypeColl.iterator();
         	if (!depNodeTypeListIterator.hasNext()) {
         		noMoreDependentNodes = false;
         		break;
         	}
         	
		    // Look for IN edges for the current Node and find its Parent - and make it the current Node
         	boolean foundParent = false;
		  	Iterator <Edge> inEdges = currentNode.edges(Direction.IN);
	    	while( inEdges.hasNext() ){
	    		TitanEdge inEdge = (TitanEdge) inEdges.next();
	    		Boolean inEdgeIsParent = inEdge.<Boolean>property("isParent").orElse(null);
	    		if( inEdgeIsParent != null && inEdgeIsParent ){
	    			foundParent = true;
	        		currentNode = (TitanVertex) inEdge.otherVertex(currentNode);
	        		break;
	    		}
	    	}
	    	
	    	if (foundParent == false) { 
	    		break;
	    	}
	    	
         	// find the key(s) and add to the url
	    	// first see what type of node the parent is - note some nodes can have one of many kinds of parents
	    	String depNodeType = currentNode.<String>property("aai-node-type").orElse(null);
	    	Collection <String> keyProps =  dbMaps.NodeKeyProps.get(depNodeType);
		    Iterator <String> keyPropI = keyProps.iterator();
		    
		    String nodeUrl = null;
		    String depNodeTypePlural = dbMaps.NodePlural.get(depNodeType);

	    	if (depNodeTypePlural != null)
	    	{
		    		nodeUrl = depNodeTypePlural + "/" + depNodeType + "/";
		    }		    
		    
		    while (keyPropI.hasNext()) {
		    	Object nodeKey = currentNode.<Object>property(keyPropI.next()).orElse(null);
		    	nodeUrl += RestURLEncoder.encodeURL(nodeKey.toString()) + "/";
		    }
		    	    	
	        currentNodeType = depNodeType;	
	        
			url = nodeUrl + url;
		}
     	// use the name space of the highest level of unique node since lots of children node types
     	// are common ex. l-interface is in the path for pserver and vpe
		String urlNamespace = dbMaps.NodeNamespace.get(currentNodeType) + "/"; 
		urlNamespace = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, urlNamespace);
	    
     	// add the url component for the node passed in
    	Collection <String> keyProps =  dbMaps.NodeKeyProps.get(nodeType);
	    Iterator <String> keyPropI = keyProps.iterator();
	    
	    String nodeUrl = null;
		String nodeTypePlural = "";
		nodeTypePlural = dbMaps.NodePlural.get(nodeType);
	    
	    
    	if (nodeTypePlural != null && !nodeTypePlural.equals("")){
    		nodeUrl = nodeTypePlural + "/" + nodeType + "/";
    	} else {
    		nodeUrl = nodeType + "/";
    	}

	    if (nodeType.equals("ipaddress")) { // this has 2 keys but API only uses port-or -address in URL
	    	String nodeKey = node.<String>property("port-or-interface").orElse(null);
	    	nodeUrl += RestURLEncoder.encodeURL(nodeKey) + "/";
	    } else {
		    while (keyPropI.hasNext()) {
		    	Object nodeKey = node.<Object>property(keyPropI.next()).orElse(null);
		    	nodeUrl += RestURLEncoder.encodeURL(nodeKey.toString()) + "/";
		    }
	    }
	    if (isCallbackurl) {
    		url = AAIConfig.get(AAIConstants.AAI_GLOBAL_CALLBACK_URL) + apiVersion + "/" + urlNamespace + url + nodeUrl;
    	} else {
	    	url = AAIApiServerURLBase.get() + apiVersion + "/" + urlNamespace + url + nodeUrl;
	    }
		return url;
	}

	/**
	 * Gets the search url.
	 *
	 * @param graph the graph
	 * @param node the node
	 * @param apiVersion the api version
	 * @return the search url
	 * @throws AAIException the AAI exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static String getSearchUrl(TitanTransaction graph, TitanVertex node, String apiVersion) throws AAIException, UnsupportedEncodingException
	{
		String nodeType = node.<String>property("aai-node-type").orElse(null);
		String url = "";
     	String currentNodeType = nodeType;
     	Boolean noMoreDependentNodes = true;
     	TitanVertex currentNode = node;
     	Boolean hasCloudRegion = false;

     	// if the caller supplies an apiVersion we'll use it, otherwise we'll just
     	// reflect back from the called URI
       	if (apiVersion == null) { 
       		apiVersion = AAIApiVersion.get();
     	}
		
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP));
		
     	// add the url component for the dependent on nodes for the node passed in
     	while (noMoreDependentNodes) {
        	Collection <String> depNodeTypeColl =  dbMaps.NodeDependencies.get(currentNodeType);
         	Iterator <String> depNodeTypeListIterator = (Iterator<String>) depNodeTypeColl.iterator();
         	if (!depNodeTypeListIterator.hasNext()) {
         		noMoreDependentNodes = false;
         		break;
         	}
         	
		    // Look for IN edges for the current Node and find its Parent - and make it the current Node
         	boolean foundParent = false;
		  	Iterator <Edge> inEdges = currentNode.edges(Direction.IN);
	    	while( inEdges.hasNext() ){
	    		TitanEdge inEdge = (TitanEdge) inEdges.next();
	    		Boolean inEdgeIsParent = inEdge.<Boolean>property("isParent").orElse(null);
	    		if( inEdgeIsParent != null && inEdgeIsParent ){
	    			foundParent = true;
	        		currentNode = inEdge.otherVertex(currentNode);
	        		break;
	    		}
	    	}
	    	
	    	if (foundParent == false) { 
	    		break;
	    	}
	    	
         	// find the key(s) and add to the url
	    	// first see what type of node the parent is - note some nodes can have one of many kinds of parents
	    	String depNodeType = currentNode.<String>property("aai-node-type").orElse(null);
	    	Collection <String> keyProps =  dbMaps.NodeKeyProps.get(depNodeType);
		    Iterator <String> keyPropI = keyProps.iterator();
		    
		    String nodeUrl = null;
		    String depNodeTypePlural = dbMaps.NodePlural.get(depNodeType);

	    	if (depNodeTypePlural != null)
	    		nodeUrl = depNodeTypePlural + "/" + depNodeType + "/";

		    while (keyPropI.hasNext()) {
		    	Object nodeKey = currentNode.<Object>property(keyPropI.next()).orElse(null);
		    	nodeUrl += RestURLEncoder.encodeURL(nodeKey.toString()) + "/";
		    }
		    	    	
	        currentNodeType = depNodeType;	
	        
			url = nodeUrl + url;
		}
     	// use the name space of the highest level of unique node since lots of children node types
     	// are common ex. l-interface is in the path for pserver and vpe
		String urlNamespace = dbMaps.NodeNamespace.get(currentNodeType) + "/"; 
		urlNamespace = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, urlNamespace);
	    
     	// add the url component for the node passed in
    	Collection <String> keyProps =  dbMaps.NodeKeyProps.get(nodeType);
	    Iterator <String> keyPropI = keyProps.iterator();
	    
	    String nodeUrl = null;
		String nodeTypePlural = "";
		nodeTypePlural = dbMaps.NodePlural.get(nodeType);
	    
    	if (nodeTypePlural != null && !nodeTypePlural.equals(""))
    		nodeUrl = nodeTypePlural + "/" + nodeType + "/";
    	else
    		nodeUrl = nodeType + "/";

	    if (nodeType.equals("ipaddress")) { // this has 2 keys but API only uses port-or -address in URL
	    	String nodeKey = node.<String>property("port-or-interface").orElse(null);
	    	nodeUrl += RestURLEncoder.encodeURL(nodeKey) + "/";
	    } else {
		    while (keyPropI.hasNext()) {
		    	Object nodeKey = node.<Object>property(keyPropI.next()).orElse(null);
		    	nodeUrl += RestURLEncoder.encodeURL(nodeKey.toString()) + "/";
		    }
	    }

    	String nodeVersion = dbMaps.NodeVersionInfoMap.get(nodeType);	     	
    	String urlVersion = null;
    	int nodeVerNum = Integer.parseInt(nodeVersion.substring(1));
    	int apiVerNum  = Integer.parseInt(apiVersion.substring(1));
    	
		if (hasCloudRegion) {
    		if (apiVerNum < 7)
    			urlVersion = "v7"; // or set to the latest version?
    		else 
    			urlVersion = apiVersion;
    	} else {		    		
	    	if (nodeVerNum == apiVerNum || nodeVerNum < apiVerNum)
	    		urlVersion = apiVersion;			
	    	else 
	    		urlVersion = nodeVersion;
    	} 			
    	url = AAIApiServerURLBase.get() + urlVersion + "/" + urlNamespace + url + nodeUrl;
    	//remove the trailing "/"
    	url = url.substring(0, url.length()-1);
		return url;
	}
	
	/**
	 * Gets the.
	 *
	 * @param graph the graph
	 * @param node the node
	 * @return the string
	 * @throws AAIException the AAI exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static String get(TitanTransaction graph, TitanVertex node) throws AAIException, UnsupportedEncodingException
	{
		return get(graph, node, null, false, false);
	}
	
	/**
	 * Gets the.
	 *
	 * @param graph the graph
	 * @param node the node
	 * @param apiVersion the api version
	 * @return the string
	 * @throws AAIException the AAI exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static String get(TitanTransaction graph, TitanVertex node, String apiVersion) throws AAIException, UnsupportedEncodingException
	{
		return get(graph, node, apiVersion, false, false);
	}
	
	/**
	 * Gets the.
	 *
	 * @param graph the graph
	 * @param node the node
	 * @param apiVersion the api version
	 * @param isLegacyVserverUEB the is legacy vserver UEB
	 * @return the string
	 * @throws AAIException the AAI exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static String get(TitanTransaction graph, TitanVertex node, String apiVersion, Boolean isLegacyVserverUEB) throws AAIException, UnsupportedEncodingException
	{
		return get(graph, node, apiVersion, isLegacyVserverUEB, false);
	}
	
	/**
	 * Gets the key hashes.
	 *
	 * @param graph the graph
	 * @param node the node
	 * @return the key hashes
	 * @throws AAIException the AAI exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static LinkedHashMap<String, Object> getKeyHashes(TitanTransaction graph, TitanVertex node) throws AAIException, UnsupportedEncodingException
	{
		return getKeyHashes(graph, node, null);
	}
	
	/*
	 * method returns a Hash of Hashes for each parents keys for the given node based on its nodetype 
	 * Special cases for REST URLs:
	 *  - old URLS for vserver, ipaddress and volume node types for v2/v3
	 *  - images, flavor, vnic and l-interface node types will return new url
	 *  - nodetypes with multiple keys such as service capability
	 *  - nodetypes with multiple keys such as ipaddress where we use one key in the URL
	 *  - cvlan-tags and *list nodetypes - have special or no plurals - they get handled via the hash Map
	 */	

	/**
	 * Gets the key hashes.
	 *
	 * @param graph the graph
	 * @param node the node
	 * @param apiVersion the api version
	 * @return the key hashes
	 * @throws AAIException the AAI exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static LinkedHashMap <String,Object> getKeyHashes(TitanTransaction graph, TitanVertex node, String apiVersion) throws AAIException, UnsupportedEncodingException
	{
		String nodeType = node.<String>property("aai-node-type").orElse(null);
     	Boolean noMoreDependentNodes = true;
     	TitanVertex currentNode = node;
     
       	if (apiVersion == null || apiVersion.equals("")) { 
       		apiVersion = AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP);
     	}
       	
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(apiVersion);
		
       	// Hash of hashes of keys for each node and its ancestry
		LinkedHashMap <String,Object> returnHash = new LinkedHashMap <String,Object> ();
		
     	// create the hash for the keys for the node passed in
     	HashMap <String,Object> thisNodeHash = new HashMap <String,Object> ();
    	Collection <String> keyProps =  dbMaps.NodeKeyProps.get(nodeType);
	    Iterator <String> keyPropI = keyProps.iterator();
	    
	    if (nodeType.equals("ipaddress")) { // this has 2 keys but API only uses port-or -address in URL
	    	String nodeKeyValue = node.<String>property("port-or-interface").orElse(null);
	    	thisNodeHash.put("port-or-interface", nodeKeyValue);
	    } else {
		    while (keyPropI.hasNext()) {
		    	String nodeKeyName = keyPropI.next();
		    	Object nodeKeyValue = node.<Object>property(nodeKeyName).orElse(null);
		    	thisNodeHash.put(nodeKeyName, nodeKeyValue);
		    	nodeKeyName =  nodeType + "." + nodeKeyName;
		    }
	    }
	    returnHash.putAll(thisNodeHash);	    
			
     	// create and add the hashes for the dependent nodes for the node passed in
     	while (noMoreDependentNodes) {
//        	Collection <String> depNodeTypeColl =  DbRules.NodeDependencies.get(currentNodeType);
//         	Iterator <String> depNodeTypeListIterator = (Iterator<String>) depNodeTypeColl.iterator();
         	HashMap <String,Object> depNodeHash = new HashMap <String,Object> ();
//         	
//         	if (!depNodeTypeListIterator.hasNext()) {
//         		noMoreDependentNodes = false;
//         		break;
//         	}
         	
         	boolean foundParent = false;
         	
		    // Look for IN edges for the current Node and find its Parent - and make it the current Node
		  	Iterator <Edge> inEdges = currentNode.edges(Direction.IN);
	    	while( inEdges.hasNext() ){
	    		TitanEdge inEdge = (TitanEdge) inEdges.next();
	    		Boolean inEdgeIsParent = inEdge.<Boolean>property("isParent").orElse(null);
	    		if( inEdgeIsParent != null && inEdgeIsParent ){
	        		currentNode = inEdge.otherVertex(currentNode);
	        		foundParent = true;
	        		break;
	    		}
	    	}
	    	if (foundParent == false) { 
	    		break;
	    	}
	    	
         	// find the key(s) and add to the url
	    	// first see what type of node the parent is - note some nodes can have one of many kinds of parents
	    	String depNodeType = currentNode.<String>property("aai-node-type").orElse(null);
	    	keyProps =  dbMaps.NodeKeyProps.get(depNodeType);
		    keyPropI = keyProps.iterator();
		    		    
		    while (keyPropI.hasNext()) {
		    	String nodeKeyName = keyPropI.next();
		    	Object nodeKeyValue = currentNode.<Object>property(nodeKeyName).orElse(null);
		    	nodeKeyName = depNodeType + "." + nodeKeyName;
		    	// key name will be like tenant.tenant-id
		    	
		    	depNodeHash.put(nodeKeyName, nodeKeyValue);
		    }
		    returnHash.putAll(depNodeHash);		
		}
	    
		return returnHash;
	}
	
	/*
	 * method returns a Hash of Hashes for each parents keys for the given node based on its nodeURI
	 * Special cases for REST URLs:
	 *  - images, flavor, vnic and l-interface node types will return new url
	 *  - nodetypes with multiple keys such as service capability
	 *  - nodetypes with multiple keys such as ipaddress where we use one key in the URL
	 *  - cvlan-tags and *list nodetypes - have special or no plurals - they get handled via the hash Map
	 */	

	/**
	 * Gets the key hashes.
	 *
	 * @param nodeURI the node URI
	 * @return the key hashes
	 * @throws AAIException the AAI exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static LinkedHashMap <String,Object> getKeyHashes(String nodeURI) throws AAIException, UnsupportedEncodingException
	{
		return getKeyHashes(nodeURI, null);
		
	}
	
	/**
	 * Gets the key hashes.
	 *
	 * @param nodeURI the node URI
	 * @param apiVersion the api version
	 * @return the key hashes
	 * @throws AAIException the AAI exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public static LinkedHashMap <String,Object> getKeyHashes(String nodeURI, String apiVersion) throws AAIException, UnsupportedEncodingException
	{
		
		if (apiVersion == null || apiVersion.equals(""))
			apiVersion = AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP);
			
		DbMaps dbMaps = IngestModelMoxyOxm.dbMapsContainer.get(apiVersion);
		
       	// Hash of hashes of keys for each node and its ancestry
		LinkedHashMap <String,Object> returnHash = new LinkedHashMap <String,Object> ();
           	
       	String path = nodeURI.replaceFirst("^/", "");		
		Path p = Paths.get(path);
		int index = p.getNameCount() - 2; // index of where we expect the node type to be
		
		// if the node type has one key
		String currentNodeType = p.getName(index).toString();
		// if the node type has two keys - this assumes max 2 keys
		if (!dbMaps.NodeKeyProps.containsKey(currentNodeType))
			currentNodeType = p.getName(--index).toString();
	
     	// create the hash for the keys for the node passed in
     	LinkedHashMap <String,Object> thisNodeHash = new LinkedHashMap <String,Object> ();
    	Collection <String> keyProps =  dbMaps.NodeKeyProps.get(currentNodeType);
	    Iterator <String> keyPropI = keyProps.iterator();
    	
	    if (currentNodeType.equals("ipaddress")) { // this has 2 keys but API only uses port-or -address in URL
	    	String nodeKeyValue = p.getName(index + 1).toString();
	    	thisNodeHash.put("port-or-interface", nodeKeyValue);
	    } else {
	    	int j = 1;
		    while (keyPropI.hasNext()) {
		    	String nodeKeyName = currentNodeType + "." + keyPropI.next();
		    	String nodeKeyValue = p.getName(index + j++).toString();
		    	thisNodeHash.put(nodeKeyName, nodeKeyValue);
		    }
	    }
	    returnHash.putAll(thisNodeHash);	    
	    if (!currentNodeType.contains("-list"))
	    	index -= 3; 
	    else
	    	index -= 2; // no plural in this case
			

     	// create and add the hashes for the dependent nodes for the node passed in
	    LinkedHashMap <String,Object> depNodeHash = new LinkedHashMap <String,Object> (); 
	    String depNodeType = null;
     	while (index >= 2) {
     		if (depNodeType == null) depNodeType = p.getName(index).toString();
     		//System.out.println("index=" + index);        	
    		// if the node type has one key
    		currentNodeType = p.getName(index).toString();
    		// if the node type has two keys - this assumes max 2 keys
    		if (!dbMaps.NodeKeyProps.containsKey(currentNodeType))
    			currentNodeType = p.getName(--index).toString();
    	
        	keyProps =  dbMaps.NodeKeyProps.get(currentNodeType);
    	    keyPropI = keyProps.iterator();
        	
    	    if (currentNodeType.equals("ipaddress")) { // this has 2 keys but API only uses port-or -address in URL
    	    	String nodeKeyValue = p.getName(index + 1).toString();
    	    	depNodeHash.put("port-or-interface", nodeKeyValue);
    	    } else {
    	    	int j = 1;
    		    while (keyPropI.hasNext()) {
    		    	String nodeKeyName = currentNodeType + "." + keyPropI.next();
    		    	String nodeKeyValue = p.getName(index + j++).toString();
    		    	depNodeHash.put(nodeKeyName, nodeKeyValue);
    		    }
    	    }

    	    if (!currentNodeType.contains("-list"))
    	    	index -= 3; 
    	    else
    	    	index -= 2; // no plural in this case    	    
		}
     	if (depNodeType != null) 
     		returnHash.putAll(depNodeHash);
	    
		return returnHash;
	}
	
	/**
	 * Parses the uri.
	 *
	 * @param allKeys the all keys
	 * @param keyList the key list
	 * @param uri the uri
	 * @param aaiExtMap the aai ext map
	 * @return the AAI resource
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws AAIException the AAI exception
	 */
	public static AAIResource parseUri(HashMap<String, String> allKeys, LinkedHashMap<String, 
			LinkedHashMap<String,Object>> keyList, String uri, 
			AAIExtensionMap aaiExtMap) throws UnsupportedEncodingException, AAIException {		
		
		String[] ps = uri.split("/");
		
		String apiVersion = ps[0];
		aaiExtMap.setApiVersion(apiVersion);
		
		AAIResources aaiResources = org.openecomp.aai.ingestModel.IngestModelMoxyOxm.aaiResourceContainer.get(apiVersion);
		
		String namespace = ps[1];
		
		aaiExtMap.setNamespace(namespace);
		
		// /vces/vce/{vnf-id}/port-groups/port-group/{port-group-id}/cvlan-tag-entry/cvlan-tag/{cvlan-tag}
		
		// FullName -> /Vces/Vce/PortGroups/PortGroup/CvlanTagEntry/CvlanTag <- 

		String fullResourceName = "/" + CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, namespace);
		AAIResources theseResources = new AAIResources();
		
		StringBuffer thisUri = new StringBuffer();
		
		// the URI config option in the props file has a trailing slash
		thisUri.append("/" + namespace);
				
		boolean firstNode = true;
		
		AAIResource lastResource = null;
		
		for (int i = 2; i < ps.length; i++) { 
			
			AAIResource aaiRes;
			StringBuffer tmpResourceName = new StringBuffer();
				
			String p = ps[i];
			String seg =ps[i];
						
			thisUri.append("/" + seg);
			
			tmpResourceName.append(fullResourceName);
			
			if (seg.equals("cvlan-tag")) {
				seg = "cvlan-tag-entry";
			}
			tmpResourceName.append("/" + CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, seg));
			
			String tmpResource = tmpResourceName.toString();
			
			if (aaiResources.getAaiResources().containsKey(tmpResource)) {
				aaiRes = aaiResources.getAaiResources().get(tmpResource);
				lastResource = aaiRes;
				theseResources.getAaiResources().put(tmpResource, aaiRes);
				fullResourceName = tmpResource;
				if ("node".equals(aaiRes.getResourceType())) {
					
					if (firstNode == true) { 
						aaiExtMap.setTopObjectFullResourceName(fullResourceName);
						firstNode = false;
					}
							
					// get the keys, which will be in order and the next path segment(s)
					AAIResourceKeys keys = aaiRes.getAaiResourceKeys();
									
					LinkedHashMap<String,Object> subKeyList = new LinkedHashMap<String,Object>();
					
					// there might not be another path segment
					if ( (i + 1) < ps.length) { 

						for (AAIResourceKey rk : keys.getAaiResourceKey()) {
							String p1 = ps[++i];
							String encodedKey = p1.toString();
							thisUri.append("/" + encodedKey);
							String decodedKey =  UriUtils.decode(p1.toString(), "UTF-8");
							subKeyList.put(rk.getKeyName(), decodedKey);
						}
						keyList.put(tmpResource, subKeyList);
						// this is the key
						allKeys.put(tmpResource, thisUri.toString());
					}
				} else { // examples sit directly under the container level, should probably be query params!!!
					if ( (i + 1) < ps.length) { 
						String p1 = ps[i+1];
						if (p1.toString().equals("example") || p1.toString().equals("singletonExample")) { 
							LinkedHashMap<String,Object> subKeyList = new LinkedHashMap<String,Object>();
							subKeyList.put("container|example", p1.toString());
							keyList.put(tmpResource, subKeyList);
						}
					}
				}
			} else {
				if (p.equals("relationship-list")) { 
					LinkedHashMap<String,Object> subKeyList = new LinkedHashMap<String,Object>();
					subKeyList.put("container|relationship", p.toString());
					keyList.put(tmpResource, subKeyList);
				} else if ( p.toString().length() > 0 && !p.toString().equals("example") && !p.toString().equals("singletonExample") 
						&& !p.toString().equals("relationship") ) {
					// this means the URL will break the model, so we bail
					throw new AAIException("AAI_3001", "bad path");
				}
			}
		}
		aaiExtMap.setUri(AAIConfig.get("aai.global.callback.url")  + apiVersion + thisUri.toString());
		aaiExtMap.setNotificationUri(AAIConfig.get("aai.global.callback.url") + AAIConfig.get("aai.notification.current.version") + thisUri.toString());
		aaiExtMap.setFullResourceName(fullResourceName);
		return lastResource;
	}

}

