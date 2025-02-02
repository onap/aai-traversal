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

package org.onap.aai.rest.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriInfo;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.onap.aai.domain.responseMessage.AAIResponseMessages;
import org.onap.aai.introspection.Loader;
import org.onap.aai.rest.db.DBRequest;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;

public class AAIExtensionMap {
    // =======================================================================
    // Attribute | Type
    // =======================================================================
    // message | java.lang.String (RW)
    // ----------------------------------------------------------------------
    // templateVars | java.lang.ArrayList<String> (RW)
    // -----------------------------------------------------------------------
    // preExtException | java.lang.Exception (RW)
    // -----------------------------------------------------------------------
    // preExtErrorCallback | java.lang.reflect.Method (RW)
    // -----------------------------------------------------------------------
    // postExtException | java.lang.Exception (RW)
    // -----------------------------------------------------------------------
    // postExtErrorCallback | java.lang.reflect.Method (RW)
    // -----------------------------------------------------------------------
    // servletRequest | javax.servlet.http.HttpServletRequest (RO)
    // -----------------------------------------------------------------------
    // headers | javax.ws.rs.core.HttpHeaders (RO)
    // -----------------------------------------------------------------------
    // objFromRequestType | String (ex. ?org.onap.aai.domain.yang.Vce?) (RO)
    // -----------------------------------------------------------------------
    // objFromRequest | $TYPE {ObjFromRequestType) (RO)
    // -----------------------------------------------------------------------
    // preExtFailOnError | java.lang.Boolean (RW)
    // -----------------------------------------------------------------------
    // postExtFailOnError | java.lang.Boolean (RW)
    // -----------------------------------------------------------------------
    // preExtSkipErrorCallback | java.lang.Boolean (RW)
    // -----------------------------------------------------------------------
    // postExtSkipErrorCallback | java.lang.Boolean (RW)
    // -----------------------------------------------------------------------
    // graph | org.janusgraph.core.JanusGraph (RW)
    // -----------------------------------------------------------------------
    // objectFromResponse | Object
    // -----------------------------------------------------------------------
    // precheckAddedList | java.util.HashMap
    // -----------------------------------------------------------------------
    // precheckResponseMessages | org.onap.aai.extensions.AAIResponseMessages
    // =======================================================================

    private String message;
    private ArrayList<String> templateVars;
    private Exception preExtException;
    private Exception postExtException;
    private Method preExtErrorCallback;
    private Method postExtErrorCallback;
    private HttpServletRequest servletRequest;
    private HttpHeaders httpHeaders;
    private String objectFromRequestType;
    private Object objectFromRequest;
    private boolean preExtFailOnError = true;
    private boolean postExtFailOnError = true;
    private boolean preExtSkipErrorCallback = true;
    private boolean postExtSkipErrorCallback = true;
    private String fromAppId;
    private String transId;
    private Graph graph;
    private Object objectFromResponse;
    private HashMap<String, Object> lookupHashMap;
    private HashMap<String, ArrayList<String>> precheckAddedList;
    private AAIResponseMessages precheckResponseMessages;
    private HashMap<String, Object> topology;
    private HashMap<String, Vertex> vertexCache;
    private String baseObject;
    private String namespace;
    private String fullResourceName;
    private String topObjectFullResourceName;
    private String uri;
    private String notificationUri;
    private String apiVersion;
    private long startTime;
    private long checkpointTime;
    private DynamicJAXBContext jaxbContext;
    private String objectFromResponseType;
    private String eventAction;
    private TransactionalGraphEngine dbEngine;
    private Loader loader;
    private UriInfo uriInfo;
    private DBRequest dbRequest;
    private HttpEntry httpEntry;

    /**
     * Sets the message.
     *
     * @param _message the new message
     */
    public void setMessage(String _message) {
        this.message = _message;
    }

    /**
     * Sets the template vars.
     *
     * @param _templateVars the new template vars
     */
    public void setTemplateVars(ArrayList<String> _templateVars) {
        this.templateVars = _templateVars;
    }

    /**
     * Sets the pre ext exception.
     *
     * @param _exception the new pre ext exception
     */
    public void setPreExtException(Exception _exception) {
        this.preExtException = _exception;
    }

    /**
     * Sets the pre ext error callback.
     *
     * @param _errorCallback the new pre ext error callback
     */
    public void setPreExtErrorCallback(Method _errorCallback) {
        this.preExtErrorCallback = _errorCallback;
    }

    /**
     * Sets the post ext exception.
     *
     * @param _exception the new post ext exception
     */
    public void setPostExtException(Exception _exception) {
        this.postExtException = _exception;
    }

    /**
     * Sets the post ext error callback.
     *
     * @param _errorCallback the new post ext error callback
     */
    public void setPostExtErrorCallback(Method _errorCallback) {
        this.postExtErrorCallback = _errorCallback;
    }

    /**
     * Sets the servlet request.
     *
     * @param _httpServletRequest the new servlet request
     */
    public void setServletRequest(HttpServletRequest _httpServletRequest) {
        this.servletRequest = _httpServletRequest;
    }

    /**
     * Sets the http headers.
     *
     * @param _httpHeaders the new http headers
     */
    public void setHttpHeaders(HttpHeaders _httpHeaders) {
        this.httpHeaders = _httpHeaders;
    }

    /**
     * Sets the object from request type.
     *
     * @param _objectFromRequestType the new object from request type
     */
    public void setObjectFromRequestType(String _objectFromRequestType) {
        this.objectFromRequestType = _objectFromRequestType;
    }

    /**
     * Sets the object from request.
     *
     * @param _objectFromRequest the new object from request
     */
    public void setObjectFromRequest(Object _objectFromRequest) {
        this.objectFromRequest = _objectFromRequest;
    }

    /**
     * Sets the object from response type.
     *
     * @param resourceClassName the new object from response type
     */
    public void setObjectFromResponseType(String resourceClassName) {
        // TODO Auto-generated method stub
        this.objectFromResponseType = resourceClassName;
    }

    /**
     * Gets the object from response type.
     *
     * @return the object from response type
     */
    public String getObjectFromResponseType() {
        // TODO Auto-generated method stub
        return this.objectFromResponseType;
    }

    /**
     * Sets the pre ext fail on error.
     *
     * @param _failOnError the new pre ext fail on error
     */
    public void setPreExtFailOnError(boolean _failOnError) {
        this.preExtFailOnError = _failOnError;
    }

    /**
     * Sets the post ext fail on error.
     *
     * @param _failOnError the new post ext fail on error
     */
    public void setPostExtFailOnError(boolean _failOnError) {
        this.postExtFailOnError = _failOnError;
    }

    /**
     * Gets the message.
     *
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Gets the template vars.
     *
     * @return the template vars
     */
    public ArrayList<String> getTemplateVars() {
        if (this.templateVars == null) {
            this.templateVars = new ArrayList<String>();
        }
        return this.templateVars;
    }

    /**
     * Gets the pre ext exception.
     *
     * @return the pre ext exception
     */
    public Exception getPreExtException() {
        return this.preExtException;
    }

    /**
     * Gets the pre ext error callback.
     *
     * @return the pre ext error callback
     */
    public Method getPreExtErrorCallback() {
        return this.preExtErrorCallback;
    }

    /**
     * Gets the post ext exception.
     *
     * @return the post ext exception
     */
    public Exception getPostExtException() {
        return this.postExtException;
    }

    /**
     * Gets the post ext error callback.
     *
     * @return the post ext error callback
     */
    public Method getPostExtErrorCallback() {
        return this.postExtErrorCallback;
    }

    /**
     * Gets the http servlet request.
     *
     * @return the http servlet request
     */
    public HttpServletRequest getHttpServletRequest() {
        return this.servletRequest;
    }

    /**
     * Gets the http headers.
     *
     * @return the http headers
     */
    public HttpHeaders getHttpHeaders() {
        return this.httpHeaders;
    }

    /**
     * Gets the object from request type.
     *
     * @return the object from request type
     */
    public String getObjectFromRequestType() {
        return this.objectFromRequestType;
    }

    /**
     * Gets the object from request.
     *
     * @return the object from request
     */
    public Object getObjectFromRequest() {
        return this.objectFromRequest;
    }

    /**
     * Gets the pre ext fail on error.
     *
     * @return the pre ext fail on error
     */
    public boolean getPreExtFailOnError() {
        return this.preExtFailOnError;
    }

    /**
     * Gets the post ext fail on error.
     *
     * @return the post ext fail on error
     */
    public boolean getPostExtFailOnError() {
        return this.postExtFailOnError;
    }

    /**
     * Gets the from app id.
     *
     * @return the from app id
     */
    public String getFromAppId() {
        return this.fromAppId;
    }

    /**
     * Sets the from app id.
     *
     * @param fromAppId the new from app id
     */
    public void setFromAppId(String fromAppId) {
        this.fromAppId = fromAppId;
    }

    /**
     * Gets the trans id.
     *
     * @return the trans id
     */
    public String getTransId() {
        return this.transId;
    }

    /**
     * Sets the trans id.
     *
     * @param transId the new trans id
     */
    public void setTransId(String transId) {
        this.transId = transId;
    }

    /**
     * Gets the pre ext skip error callback.
     *
     * @return the pre ext skip error callback
     */
    public boolean getPreExtSkipErrorCallback() {
        return preExtSkipErrorCallback;
    }

    /**
     * Sets the pre ext skip error callback.
     *
     * @param preExtSkipErrorCallback the new pre ext skip error callback
     */
    public void setPreExtSkipErrorCallback(boolean preExtSkipErrorCallback) {
        this.preExtSkipErrorCallback = preExtSkipErrorCallback;
    }

    /**
     * Gets the post ext skip error callback.
     *
     * @return the post ext skip error callback
     */
    public boolean getPostExtSkipErrorCallback() {
        return postExtSkipErrorCallback;
    }

    /**
     * Sets the post ext skip error callback.
     *
     * @param postExtSkipErrorCallback the new post ext skip error callback
     */
    public void setPostExtSkipErrorCallback(boolean postExtSkipErrorCallback) {
        this.postExtSkipErrorCallback = postExtSkipErrorCallback;
    }

    /**
     * Gets the graph.
     *
     * @return the graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Sets the graph.
     *
     * @param graph the new graph
     */
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    /**
     * Gets the object from response.
     *
     * @return the object from response
     */
    public Object getObjectFromResponse() {
        return objectFromResponse;
    }

    /**
     * Sets the object from response.
     *
     * @param objectFromResponse the new object from response
     */
    public void setObjectFromResponse(Object objectFromResponse) {
        this.objectFromResponse = objectFromResponse;
    }

    /**
     * Gets the lookup hash map.
     *
     * @return the lookup hash map
     */
    public HashMap<String, Object> getLookupHashMap() {
        if (this.lookupHashMap == null) {
            this.lookupHashMap = new HashMap<String, Object>();
        }
        return this.lookupHashMap;
    }

    /**
     * Sets the lookup hash map.
     *
     * @param lookupHashMap the lookup hash map
     */
    public void setLookupHashMap(HashMap<String, Object> lookupHashMap) {
        this.lookupHashMap = lookupHashMap;
    }

    /**
     * Gets the precheck added list.
     *
     * @return the precheck added list
     */
    public HashMap<String, ArrayList<String>> getPrecheckAddedList() {
        if (this.precheckAddedList == null) {
            this.precheckAddedList = new HashMap<String, ArrayList<String>>();
        }
        return precheckAddedList;
    }

    /**
     * Sets the precheck added list.
     *
     * @param precheckAddedList the precheck added list
     */
    public void setPrecheckAddedList(HashMap<String, ArrayList<String>> precheckAddedList) {
        this.precheckAddedList = precheckAddedList;
    }

    /**
     * Gets the precheck response messages.
     *
     * @return the precheck response messages
     */
    public AAIResponseMessages getPrecheckResponseMessages() {
        if (this.precheckResponseMessages == null) {
            this.precheckResponseMessages = new AAIResponseMessages();
        }
        return precheckResponseMessages;
    }

    /**
     * Sets the precheck response messages.
     *
     * @param precheckResponseData the new precheck response messages
     */
    public void setPrecheckResponseMessages(AAIResponseMessages precheckResponseData) {
        this.precheckResponseMessages = precheckResponseData;
    }

    /**
     * Gets the topology.
     *
     * @return the topology
     */
    public HashMap<String, Object> getTopology() {
        if (this.topology == null) {
            this.topology = new HashMap<String, Object>();
        }
        return topology;
    }

    /**
     * Gets the vertex cache.
     *
     * @return the vertex cache
     */
    public HashMap<String, Vertex> getVertexCache() {
        if (this.vertexCache == null) {
            this.vertexCache = new HashMap<String, Vertex>();
        }
        return vertexCache;
    }

    /**
     * Gets the base object.
     *
     * @return the base object
     */
    public String getBaseObject() {
        return baseObject;
    }

    /**
     * Sets the base object.
     *
     * @param baseObject the new base object
     */
    public void setBaseObject(String baseObject) {
        this.baseObject = baseObject;
    }

    /**
     * Gets the namespace.
     *
     * @return the namespace
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace.
     *
     * @param namespace the new namespace
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Gets the full resource name.
     *
     * @return the full resource name
     */
    public String getFullResourceName() {
        return fullResourceName;
    }

    /**
     * Sets the full resource name.
     *
     * @param fullResourceName the new full resource name
     */
    public void setFullResourceName(String fullResourceName) {
        this.fullResourceName = fullResourceName;
    }

    /**
     * Gets the top object full resource name.
     *
     * @return the top object full resource name
     */
    public String getTopObjectFullResourceName() {
        return topObjectFullResourceName;
    }

    /**
     * Sets the top object full resource name.
     *
     * @param topObjectFullResourceName the new top object full resource name
     */
    public void setTopObjectFullResourceName(String topObjectFullResourceName) {
        this.topObjectFullResourceName = topObjectFullResourceName;
    }

    /**
     * Gets the uri.
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the uri.
     *
     * @param uri the new uri
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the api version.
     *
     * @return the api version
     */
    public String getApiVersion() {
        return apiVersion;
    }

    /**
     * Sets the api version.
     *
     * @param apiVersion the new api version
     */
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * Sets the notification uri.
     *
     * @param uri the new notification uri
     */
    public void setNotificationUri(String uri) {
        this.notificationUri = uri;

    }

    /**
     * Gets the notification uri.
     *
     * @return the notification uri
     */
    public String getNotificationUri() {
        return this.notificationUri;

    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     *
     * @param startTime the new start time
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the checkpoint time.
     *
     * @return the checkpoint time
     */
    public long getCheckpointTime() {
        return checkpointTime;
    }

    /**
     * Sets the checkpoint time.
     *
     * @param checkpointTime the new checkpoint time
     */
    public void setCheckpointTime(long checkpointTime) {
        this.checkpointTime = checkpointTime;
    }

    /**
     * Gets the jaxb context.
     *
     * @return the jaxb context
     */
    public DynamicJAXBContext getJaxbContext() {
        return jaxbContext;
    }

    /**
     * Sets the jaxb context.
     *
     * @param jaxbContext the new jaxb context
     */
    public void setJaxbContext(DynamicJAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    /**
     * Sets the event action.
     *
     * @param eventAction the new event action
     */
    public void setEventAction(String eventAction) {
        this.eventAction = eventAction;
    }

    /**
     * Gets the event action.
     *
     * @return the event action
     */
    public String getEventAction() {
        return this.eventAction;
    }

    /**
     * Gets the transactional graph engine.
     *
     * @return the transactional graph engine
     */
    public TransactionalGraphEngine getTransactionalGraphEngine() {
        return this.dbEngine;

    }

    /**
     * Sets the transactional graph engine.
     *
     * @param dbEngine the new transactional graph engine
     */
    public void setTransactionalGraphEngine(TransactionalGraphEngine dbEngine) {
        this.dbEngine = dbEngine;

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
     * Sets the loader.
     *
     * @param loader the new loader
     */
    public void setLoader(Loader loader) {
        this.loader = loader;
    }

    /**
     * Gets the uri info.
     *
     * @return the uri info
     */
    public UriInfo getUriInfo() {
        return uriInfo;
    }

    /**
     * Sets the uri info.
     *
     * @param uriInfo the new uri info
     */
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public DBRequest getDbRequest() {
        return dbRequest;
    }

    public void setDbRequest(DBRequest dbRequest) {
        this.dbRequest = dbRequest;
    }

    public HttpEntry getHttpEntry() {
        return httpEntry;
    }

    public void setHttpEntry(HttpEntry httpEntry) {
        this.httpEntry = httpEntry;
    }
}
