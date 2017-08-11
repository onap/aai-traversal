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

package org.openecomp.aai.rest.db;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.javatuples.Pair;

import org.openecomp.aai.db.props.AAIProperties;
import org.openecomp.aai.dbmap.DBConnectionType;
import org.openecomp.aai.domain.responseMessage.AAIResponseMessage;
import org.openecomp.aai.domain.responseMessage.AAIResponseMessageDatum;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.extensions.AAIExtensionMap;
import org.openecomp.aai.extensions.ExtensionController;
import org.openecomp.aai.introspection.Introspector;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.LoaderFactory;
import org.openecomp.aai.introspection.MarshallerProperties;
import org.openecomp.aai.introspection.ModelInjestor;
import org.openecomp.aai.introspection.ModelType;
import org.openecomp.aai.introspection.Version;
import org.openecomp.aai.introspection.exceptions.AAIUnknownObjectException;
import org.openecomp.aai.logging.ErrorLogHelper;
import org.openecomp.aai.parsers.query.QueryParser;
import org.openecomp.aai.parsers.uri.URIToExtensionInformation;
import org.openecomp.aai.rest.ueb.UEBNotification;
import org.openecomp.aai.restcore.HttpMethod;
import org.openecomp.aai.schema.enums.ObjectMetadata;
import org.openecomp.aai.serialization.db.DBSerializer;
import org.openecomp.aai.serialization.engines.QueryStyle;
import org.openecomp.aai.serialization.engines.TitanDBEngine;
import org.openecomp.aai.serialization.engines.TransactionalGraphEngine;
import org.openecomp.aai.serialization.engines.query.QueryEngine;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.thinkaurelius.titan.core.TitanException;

/**
 * The Class HttpEntry.
 */
public class HttpEntry {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(HttpEntry.class);

	private final ModelType introspectorFactoryType;
	
	private final QueryStyle queryStyle;
	
	private final Version version;
	
	private final Loader loader;
	
	private final TransactionalGraphEngine dbEngine;
		
	private boolean processSingle = true;

	/**
	 * Instantiates a new http entry.
	 *
	 * @param version the version
	 * @param modelType the model type
	 * @param queryStyle the query style
	 * @param llBuilder the ll builder
	 */
	public HttpEntry(Version version, ModelType modelType, QueryStyle queryStyle, DBConnectionType connectionType) {
		this.introspectorFactoryType = modelType;
		this.queryStyle = queryStyle;
		this.version = version;
		this.loader = LoaderFactory.createLoaderForVersion(introspectorFactoryType, version);
		this.dbEngine = new TitanDBEngine(
				queryStyle,
				connectionType,
				loader);
		//start transaction on creation
		dbEngine.startTransaction();

	}
	
	/**
	 * Gets the introspector factory type.
	 *
	 * @return the introspector factory type
	 */
	public ModelType getIntrospectorFactoryType() {
		return introspectorFactoryType;
	}

	/**
	 * Gets the query style.
	 *
	 * @return the query style
	 */
	public QueryStyle getQueryStyle() {
		return queryStyle;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Gets the loader.
	 *
	 * @return the loader
	 */
	public Loader getLoader() {
		return loader;
	}

	/**
	 * Gets the db engine.
	 *
	 * @return the db engine
	 */
	public TransactionalGraphEngine getDbEngine() {
		return dbEngine;
	}

	public Pair<Boolean, List<Pair<URI, Response>>> process (List<DBRequest> requests, String sourceOfTruth) throws AAIException {
		return this.process(requests, sourceOfTruth, true);
	}
	/**
	 * Process.
	 * @param requests the requests
	 * @param sourceOfTruth the source of truth
	 *
	 * @return the pair
	 * @throws AAIException the AAI exception
	 */
	public Pair<Boolean, List<Pair<URI, Response>>> process (List<DBRequest> requests, String sourceOfTruth, boolean enableResourceVersion) throws AAIException {
		DBSerializer serializer = new DBSerializer(version, dbEngine, introspectorFactoryType, sourceOfTruth);
		Response response = null;
		Status status = Status.NOT_FOUND;
		Introspector obj = null;
		QueryParser query = null;
		URI uri = null;
		String transactionId = null;
		UEBNotification	notification = new UEBNotification(loader);
		int depth = AAIProperties.MAXIMUM_DEPTH;
		List<Pair<URI,Response>> responses = new ArrayList<>();
		MultivaluedMap<String, String> params = null;
		HttpMethod method = null;
		String uriTemp = "";
		Boolean success = true;
		QueryEngine queryEngine = dbEngine.getQueryEngine();
		int maxRetries = 10;
		int retry = 0;
		for (DBRequest request : requests) {
			try {
				for (retry = 0; retry < maxRetries; ++retry) {
					try {
						method = request.getMethod();
						obj = request.getIntrospector();
						query = request.getParser();
						transactionId = request.getTransactionId();
						uriTemp = request.getUri().getRawPath().replaceFirst("^v\\d+/", "");
						uri = UriBuilder.fromPath(uriTemp).build();
						List<Vertex> vertices = query.getQueryBuilder().toList();
						boolean isNewVertex = false;
						String outputMediaType = getMediaType(request.getHeaders().getAcceptableMediaTypes());
						String result = null;
						params = request.getInfo().getQueryParameters(false);
						depth = setDepth(obj, params.getFirst("depth"));
						String cleanUp = params.getFirst("cleanup");
						String requestContext = "";
						List<String> requestContextList = request.getHeaders().getRequestHeader("aai-request-context");
						if (requestContextList != null) {
							requestContext = requestContextList.get(0);
						}
					
						if (cleanUp == null) {
							cleanUp = "false";
						}
						if (vertices.size() > 1 && processSingle && !method.equals(HttpMethod.GET)) {
							if (method.equals(HttpMethod.DELETE)) {
								throw new AAIException("AAI_6138");
							} else {
								throw new AAIException("AAI_6137");
							}
						}
						if (method.equals(HttpMethod.PUT)) {
							String resourceVersion = (String)obj.getValue("resource-version");
							if (vertices.size() == 1) {
								if (enableResourceVersion) {
									serializer.verifyResourceVersion("update", query.getResultType(), (String)vertices.get(0).<String>property("resource-version").orElse(null), resourceVersion, obj.getURI());
								}
								isNewVertex = false;
							} else {
								if (enableResourceVersion) {
									serializer.verifyResourceVersion("create", query.getResultType(), "", resourceVersion, obj.getURI());
								}
								isNewVertex = true;
							}
						} else {
							if (vertices.size() == 0) {
								String msg = createNotFoundMessage(query.getResultType(), request.getUri());
								throw new AAIException("AAI_6114", msg);
							} else {
								isNewVertex = false;
							}
						}
						Vertex v = null;
						if (!isNewVertex) {
							v = vertices.get(0);
						}
						HashMap<String, Introspector> relatedObjects = new HashMap<>();
						switch (method) {
							case GET:
								String nodeOnly = params.getFirst("nodes-only");
								boolean isNodeOnly = nodeOnly != null;
								
								obj = this.getObjectFromDb(vertices, serializer, query, obj, request.getUri(), depth, isNodeOnly, cleanUp);
								if (obj != null) {
									status = Status.OK;
									MarshallerProperties properties;
									if (!request.getMarshallerProperties().isPresent()) {
										properties = 
												new MarshallerProperties.Builder(org.openecomp.aai.restcore.MediaType.getEnum(outputMediaType)).build();
									} else {
										properties = request.getMarshallerProperties().get();
									}
									result = obj.marshal(properties);
								}
								
								break;
							case PUT: 
								response = this.invokeExtension(dbEngine, this.dbEngine.tx(), method, request, sourceOfTruth, version, loader, obj, uri, true);
								if (isNewVertex) {
									v = serializer.createNewVertex(obj);
								} else {
									serializer.touchStandardVertexProperties(v, false);
								}
								serializer.serializeToDb(obj, v, query, uri.getRawPath(), requestContext);
								this.invokeExtension(dbEngine, this.dbEngine.tx(), HttpMethod.PUT, request, sourceOfTruth, version, loader, obj, uri, false);
								status = Status.OK;
								if (isNewVertex) {
									status = Status.CREATED;
								}
								obj = serializer.getLatestVersionView(v);
								if (query.isDependent()) {
									relatedObjects = this.getRelatedObjects(serializer, queryEngine, v);
								}
								notification.createNotificationEvent(transactionId, sourceOfTruth, status, uri, obj, relatedObjects);
								
								break;
							case PUT_EDGE:
								serializer.touchStandardVertexProperties(v, false);
								serializer.createEdge(obj, v);
								status = Status.OK;
								break;
							case MERGE_PATCH:
								Introspector existingObj = (Introspector) obj.clone();
								existingObj = this.getObjectFromDb(vertices, serializer, query, existingObj, request.getUri(), 0, false, cleanUp);
								String existingJson = existingObj.marshal(false);
								String newJson;
								
								if (request.getRawRequestContent().isPresent()) {
									newJson = request.getRawRequestContent().get();
								} else {
									newJson = "";
								}
								Object relationshipList = request.getIntrospector().getValue("relationship-list");
								ObjectMapper mapper = new ObjectMapper();
								try {
									JsonNode existingNode = mapper.readTree(existingJson);
									JsonNode newNode = mapper.readTree(newJson);
									JsonMergePatch patch = JsonMergePatch.fromJson(newNode);
									JsonNode completed = patch.apply(existingNode);
									String patched = mapper.writeValueAsString(completed);
									Introspector patchedObj = loader.unmarshal(existingObj.getName(), patched);
									if (relationshipList == null) {
										//if the caller didn't touch the relationship-list, we shouldn't either
										patchedObj.setValue("relationship-list", null);
									}
									serializer.touchStandardVertexProperties(v, false);
									serializer.serializeToDb(patchedObj, v, query, uri.getRawPath(), requestContext);
									status = Status.OK;
									patchedObj = serializer.getLatestVersionView(v);
									if (query.isDependent()) {
										relatedObjects = this.getRelatedObjects(serializer, queryEngine, v);
									}
									notification.createNotificationEvent(transactionId, sourceOfTruth, status, uri, patchedObj, relatedObjects);
								} catch (IOException | JsonPatchException e) {
									throw new AAIException("AAI_3000", "could not perform patch operation");
								}
								break;
							case DELETE:
								String resourceVersion = params.getFirst("resource-version");
								obj = serializer.getLatestVersionView(v);
								if (query.isDependent()) {
									relatedObjects = this.getRelatedObjects(serializer, queryEngine, v);
								}
								this.invokeExtension(dbEngine, this.dbEngine.tx(), method, request, sourceOfTruth, version, loader, obj, uri, true);
								serializer.delete(v, resourceVersion, enableResourceVersion);
								this.invokeExtension(dbEngine, this.dbEngine.tx(), method, request, sourceOfTruth, version, loader, obj, uri, false);
								status = Status.NO_CONTENT;
								notification.createNotificationEvent(transactionId, sourceOfTruth, status, uri, obj, relatedObjects);
								break;
							case DELETE_EDGE:
								serializer.touchStandardVertexProperties(v, false);
								serializer.deleteEdge(obj, v);
								status = Status.NO_CONTENT;
								break;
							default:
								break;
						}
						
						
						/* temporarily adding vertex id to the headers
						 * to be able to use for testing the vertex id endpoint functionality
						 * since we presently have no other way of generating those id urls
						*/
						if (response == null && v != null && (
							method.equals(HttpMethod.PUT)
							|| method.equals(HttpMethod.GET)
							|| method.equals(HttpMethod.MERGE_PATCH))
						) {
							String myvertid = v.id().toString();
							response = Response.status(status)
									.header("vertex-id", myvertid)
									.entity(result)
									.type(outputMediaType).build();
						} else if (response == null) {
							response = Response.status(status)
									.type(outputMediaType).build();
						} else {
							//response already set to something
						}
						Pair<URI,Response> pairedResp = Pair.with(request.getUri(), response);
						responses.add(pairedResp);
						//break out of retry loop
						break;
					} catch (TitanException e) {
						this.dbEngine.rollback();
						AAIException ex = new AAIException("AAI_6142", e);
						ErrorLogHelper.logException(ex);
						Thread.sleep((retry + 1) * 20);
						this.dbEngine.startTransaction();
						queryEngine = dbEngine.getQueryEngine();
						serializer = new DBSerializer(version, dbEngine, introspectorFactoryType, sourceOfTruth);
					}
				
					if (retry == maxRetries) {
						throw new AAIException("AAI_6134");
					}
				}
			} catch (AAIException e) {
				success = false;
				ArrayList<String> templateVars = new ArrayList<String>();
				templateVars.add(request.getMethod().toString()); //GET, PUT, etc
				templateVars.add(request.getUri().getPath().toString());
				templateVars.addAll(e.getTemplateVars());
				
				response = Response
						.status(e.getErrorObject().getHTTPResponseCode())
						.entity(ErrorLogHelper.getRESTAPIErrorResponse(request.getHeaders().getAcceptableMediaTypes(), e, templateVars))
						.build();
				Pair<URI,Response> pairedResp = Pair.with(request.getUri(), response);
				responses.add(pairedResp);
				continue;
			} catch (Exception e) {
				success = false;
				e.printStackTrace();
				AAIException ex = new AAIException("AAI_4000", e);
				ArrayList<String> templateVars = new ArrayList<String>();
				templateVars.add(request.getMethod().toString()); //GET, PUT, etc
				templateVars.add(request.getUri().getPath().toString());

				response = Response
						.status(ex.getErrorObject().getHTTPResponseCode())
						.entity(ErrorLogHelper.getRESTAPIErrorResponse(request.getHeaders().getAcceptableMediaTypes(), ex, templateVars))
						.build();
				Pair<URI, Response> pairedResp = Pair.with(request.getUri(), response);
				responses.add(pairedResp);
				continue;
			}
		}
		
		notification.triggerEvents();
		Pair<Boolean, List<Pair<URI, Response>>> tuple = Pair.with(success, responses);
		return tuple;
	}

	/**
	 * Gets the media type.
	 *
	 * @param mediaTypeList the media type list
	 * @return the media type
	 */
	private String getMediaType(List <MediaType> mediaTypeList) {
		String mediaType = MediaType.APPLICATION_JSON;  // json is the default    
		for (MediaType mt : mediaTypeList) {
			if (MediaType.APPLICATION_XML_TYPE.isCompatible(mt)) {
				mediaType = MediaType.APPLICATION_XML;
			} 
		}
		return mediaType;
	}
	
	/**
	 * Gets the object from db.
	 *
	 * @param serializer the serializer
	 * @param g the g
	 * @param query the query
	 * @param obj the obj
	 * @param uri the uri
	 * @param depth the depth
	 * @param cleanUp the clean up
	 * @return the object from db
	 * @throws AAIException the AAI exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws InvocationTargetException the invocation target exception
	 * @throws SecurityException the security exception
	 * @throws InstantiationException the instantiation exception
	 * @throws NoSuchMethodException the no such method exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws MalformedURLException the malformed URL exception
	 * @throws AAIUnknownObjectException 
	 * @throws URISyntaxException 
	 */
	private Introspector getObjectFromDb(List<Vertex> results, DBSerializer serializer, QueryParser query, Introspector obj, URI uri, int depth, boolean nodeOnly, String cleanUp) throws AAIException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, InstantiationException, NoSuchMethodException, UnsupportedEncodingException, AAIUnknownObjectException, URISyntaxException {
        
        //nothing found
        if (results.size() == 0) {
        	String msg = createNotFoundMessage(query.getResultType(), uri);
			throw new AAIException("AAI_6114", msg);
        }

        obj = serializer.dbToObject(results, obj, depth, nodeOnly, cleanUp);
        
        return obj;
	}
	
	/**
	 * Invoke extension.
	 *
	 * @param dbEngine the db engine
	 * @param g the g
	 * @param httpMethod the http method
	 * @param transId the trans id
	 * @param fromAppId the from app id
	 * @param apiVersion the api version
	 * @param loader the loader
	 * @param obj the obj
	 * @param uri the uri
	 * @param headers the headers
	 * @param isPreprocess the is preprocess
	 * @return the response
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws AAIException the AAI exception
	 */
	private Response invokeExtension(TransactionalGraphEngine dbEngine, Graph g, HttpMethod httpMethod, DBRequest request, String fromAppId, Version apiVersion, Loader loader, Introspector obj, URI uri, boolean isPreprocess) throws IllegalArgumentException, UnsupportedEncodingException, AAIException {
		AAIExtensionMap aaiExtMap = new AAIExtensionMap();
		ModelInjestor injestor = ModelInjestor.getInstance();
		Response response = null;
		URIToExtensionInformation extensionInformation = new URIToExtensionInformation(loader, uri);
		aaiExtMap.setHttpEntry(this);
		aaiExtMap.setDbRequest(request);
		aaiExtMap.setTransId(request.getTransactionId());
		aaiExtMap.setFromAppId(fromAppId);
		aaiExtMap.setGraph(g);
		aaiExtMap.setApiVersion(apiVersion.toString());
		aaiExtMap.setObjectFromRequest(obj);
		aaiExtMap.setObjectFromRequestType(obj.getJavaClassName());
		aaiExtMap.setObjectFromResponse(obj);
		aaiExtMap.setObjectFromResponseType(obj.getJavaClassName());
		aaiExtMap.setJaxbContext(injestor.getContextForVersion(apiVersion));
		aaiExtMap.setUri(uri.getRawPath());
		aaiExtMap.setTransactionalGraphEngine(dbEngine);
		aaiExtMap.setLoader(loader);
		aaiExtMap.setNamespace(extensionInformation.getNamespace());
		
		ExtensionController ext = new ExtensionController();
		ext.runExtension(aaiExtMap.getApiVersion(),
				extensionInformation.getNamespace(),
				extensionInformation.getTopObject(),
				extensionInformation.getMethodName(httpMethod, isPreprocess),
				aaiExtMap,
				isPreprocess);
		
		if (aaiExtMap.getPrecheckAddedList().size() > 0) {
			response = notifyOnSkeletonCreation(aaiExtMap, obj, request.getHeaders());
		}
		
		return response;
	}
	
	/**
	 * Notify on skeleton creation.
	 *
	 * @param aaiExtMap the aai ext map
	 * @param input the input
	 * @param headers the headers
	 * @return the response
	 */
	//Legacy support
	private Response notifyOnSkeletonCreation(AAIExtensionMap aaiExtMap, Introspector input, HttpHeaders headers) {
		Response response = null;
		HashMap<AAIException, ArrayList<String>> exceptionList = new HashMap<AAIException, ArrayList<String>>();

		String keyString = "";

		Set<String> resourceKeys = input.getKeys();
		for (String key : resourceKeys) { 
			keyString += key + "=" + input.getValue(key) + " ";
		}

		for (AAIResponseMessage msg : aaiExtMap.getPrecheckResponseMessages().getAAIResponseMessage()) {
			ArrayList<String> templateVars = new ArrayList<String>();

			templateVars.add("PUT " + input.getDbName());
			templateVars.add(keyString);
			List<String> keys = new ArrayList<String>();
			templateVars.add(msg.getAaiResponseMessageResourceType());
			for (AAIResponseMessageDatum dat : msg.getAaiResponseMessageData().getAAIResponseMessageDatum()) {
				keys.add(dat.getAaiResponseMessageDatumKey() + "=" + dat.getAaiResponseMessageDatumValue());
			}
			templateVars.add(StringUtils.join(keys, ", "));
			exceptionList.put(new AAIException("AAI_0004", msg.getAaiResponseMessageResourceType()),
					templateVars);
		}
		response = Response
				.status(Status.ACCEPTED).entity(ErrorLogHelper
						.getRESTAPIInfoResponse(headers.getAcceptableMediaTypes(), exceptionList))
						.build();
		
		return response;
	}
	
	/**
	 * Creates the not found message.
	 *
	 * @param resultType the result type
	 * @param uri the uri
	 * @return the string
	 */
	private String createNotFoundMessage(String resultType, URI uri) {
		
    	String msg = "No Node of type " + resultType + " found at: " + uri.getPath();

    	return msg;
	}
	
	/**
	 * Sets the depth.
	 *
	 * @param depthParam the depth param
	 * @return the int
	 * @throws AAIException the AAI exception
	 */
	protected int setDepth(Introspector obj, String depthParam) throws AAIException {
			int depth = AAIProperties.MAXIMUM_DEPTH;

        if(depthParam == null){
			if(this.version.compareTo(Version.v9) >= 0){
				depth = 0;
			} else {
                depth = AAIProperties.MAXIMUM_DEPTH;
			}
		} else {
			if (depthParam.length() > 0 && !depthParam.equals("all")){
				try {
					depth = Integer.valueOf(depthParam);
				} catch (Exception e) {
					throw new AAIException("AAI_4016");
				}

			}
		}
        String maxDepth = obj.getMetadata(ObjectMetadata.MAXIMUM_DEPTH);
        
		int maximumDepth = AAIProperties.MAXIMUM_DEPTH;

		if(maxDepth != null){
            try {
                maximumDepth = Integer.parseInt(maxDepth);
            } catch(Exception ex){
                throw new AAIException("AAI_4018");
            }
		}

		if(depth > maximumDepth){
			throw new AAIException("AAI_3303");
		}

		return depth;
	}
	
	/**
	 * Checks if is modification method.
	 *
	 * @param method the method
	 * @return true, if is modification method
	 */
	private boolean isModificationMethod(HttpMethod method) {
		boolean result = false;
		
		if (method.equals(HttpMethod.PUT) || method.equals(HttpMethod.PUT_EDGE) || method.equals(HttpMethod.DELETE_EDGE) || method.equals(HttpMethod.MERGE_PATCH)) {
			result = true;
		}
		
		return result;
		
	}
	
	private HashMap<String, Introspector> getRelatedObjects(DBSerializer serializer, QueryEngine queryEngine, Vertex v) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException, InstantiationException, NoSuchMethodException, UnsupportedEncodingException, AAIException, URISyntaxException {
		HashMap<String, Introspector> relatedVertices = new HashMap<>();
		List<Vertex> vertexChain = queryEngine.findParents(v);
		for (Vertex vertex : vertexChain) {
			try {
				final Introspector vertexObj = serializer.getVertexProperties(vertex);
				relatedVertices.put(vertexObj.getObjectId(), vertexObj);
			} catch (AAIUnknownObjectException e) {
				LOGGER.warn("Unable to get vertex properties, partial list of related vertices returned");
			}
			
		}
		
		return relatedVertices;
	}
 	
}
