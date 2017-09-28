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
package org.onap.aai.rest.search;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.onap.aai.dbgraphmap.SearchGraph;
import org.onap.aai.dbmap.DBConnectionType;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.extensions.AAIExtensionMap;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.util.AAIApiVersion;

/**
 * Implements the search subdomain in the REST API. All API calls must include
 * X-FromAppId and X-TransactionId in the header.
 * 
 
 *
 */

@Path("/search")
public class ModelAndNamedQueryRestProvider extends RESTAPI {
	
	protected static String authPolicyFunctionName = "search";
	
	public static final String NAMED_QUERY = "/named-query";
	
	public static final String MODEL_QUERY = "/model";
	
	/**
	 * Gets the named query response.
	 *
	 * @param headers the headers
	 * @param req the req
	 * @param queryParameters the query parameters
	 * @return the named query response
	 */
	/* ---------------- Start Named Query --------------------- */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(NAMED_QUERY)
	public Response getNamedQueryResponse(@Context HttpHeaders headers,
										@Context HttpServletRequest req,
										String queryParameters) {
		AAIException ex = null;
		Response response = null;
		String fromAppId = null;
		String transId = null;
		String rqstTm = genDate();
		ArrayList<String> templateVars = new ArrayList<String>();	
		try { 
			fromAppId = getFromAppId(headers);
			transId = getTransId(headers);
			
			AAIExtensionMap aaiExtMap = new AAIExtensionMap();						
			aaiExtMap.setHttpHeaders(headers);
			aaiExtMap.setServletRequest(req);
			aaiExtMap.setApiVersion(AAIApiVersion.get());
			String realTime = headers.getRequestHeaders().getFirst("Real-Time");
			//only consider header value for search		
			DBConnectionType type = this.determineConnectionType("force-cache", realTime);
			
			SearchGraph searchGraph = new SearchGraph();
			response = searchGraph.runNamedQuery(fromAppId, transId, queryParameters, type, aaiExtMap);
	
			String respTm = genDate();

		} catch (AAIException e) {
			// send error response
			ex = e;
			templateVars.add("POST Search");
			templateVars.add("getNamedQueryResponse");
			response =  Response
						.status(e.getErrorObject().getHTTPResponseCode())
						.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
						.build();
		} catch (Exception e) {
			// send error response
			ex = new AAIException("AAI_4000", e);
			templateVars.add("POST Search");
			templateVars.add("getNamedQueryResponse");
			response = Response
						.status(Status.INTERNAL_SERVER_ERROR)
						.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), ex, templateVars))
						.build();
		} finally {
			// log success or failure
			if (ex != null) {
				ErrorLogHelper.logException(ex);
			}
		}
		return response;
	}
	
	/**
	 * Gets the model query response.
	 *
	 * @param headers the headers
	 * @param req the req
	 * @param inboundPayload the inbound payload
	 * @param action the action
	 * @return the model query response
	 */
	/* ---------------- Start Named Query --------------------- */
	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(MODEL_QUERY)
	public Response getModelQueryResponse(@Context HttpHeaders headers,
										@Context HttpServletRequest req,
										String inboundPayload,
										@QueryParam("action") String action) {
		AAIException ex = null;
		Response response = null;
		String fromAppId = null;
		String transId = null;
		String rqstTm = genDate();
		ArrayList<String> templateVars = new ArrayList<String>();	
		try { 
			fromAppId = getFromAppId(headers);
			transId = getTransId(headers);
			
			AAIExtensionMap aaiExtMap = new AAIExtensionMap();						
			aaiExtMap.setHttpHeaders(headers);
			aaiExtMap.setServletRequest(req);
			aaiExtMap.setApiVersion(AAIApiVersion.get());
			aaiExtMap.setFromAppId(fromAppId);
			aaiExtMap.setTransId(transId);
			
			String realTime = headers.getRequestHeaders().getFirst("Real-Time");
			//only consider header value for search		
			DBConnectionType type = this.determineConnectionType("force-cache", realTime);
			
			SearchGraph searchGraph = new SearchGraph();
			if (action != null && action.equalsIgnoreCase("DELETE")) { 
				response = searchGraph.executeModelOperation(fromAppId, transId, inboundPayload, type, true, aaiExtMap);
			} else {
				response = searchGraph.executeModelOperation(fromAppId, transId, inboundPayload, type, false, aaiExtMap);
			}
			String respTm = genDate();
			
		} catch (AAIException e) {
			// send error response
			ex = e;
			templateVars.add("POST Search");
			templateVars.add("getModelQueryResponse");
			response =  Response
						.status(e.getErrorObject().getHTTPResponseCode())
						.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
						.build();
		} catch (Exception e) {
			// send error response
			ex = new AAIException("AAI_4000", e);
			templateVars.add("POST Search");
			templateVars.add("getModelQueryResponse");
			response = Response
						.status(Status.INTERNAL_SERVER_ERROR)
						.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), ex, templateVars))
						.build();
		} finally {
			// log success or failure
			if (ex != null) {
				ErrorLogHelper.logException(ex);
			}
		}
		return response;
	}

}
