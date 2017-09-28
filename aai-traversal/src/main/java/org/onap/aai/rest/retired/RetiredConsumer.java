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
package org.onap.aai.rest.retired;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.PATCH;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.util.AAIConfig;

/**
 * The Class RetiredConsumer.
 */
public abstract class RetiredConsumer extends RESTAPI {

	/**
	 * Creates the message get.
	 *
	 * @param versionParam the version param
	 * @param headers the headers
	 * @param info the info
	 * @param req the req
	 * @return the response
	 */
	@GET
	@Path("/{uri:.*}")
	public Response createMessageGet(@PathParam("version")String versionParam, @Context HttpHeaders headers, @Context UriInfo info, @Context HttpServletRequest req) {
		return createMessage(versionParam, headers, info, req);
	}
	
	/**
	 * Creates the message delete.
	 *
	 * @param versionParam the version param
	 * @param headers the headers
	 * @param info the info
	 * @param req the req
	 * @return the response
	 */
	@DELETE
	@Path("/{uri:.*}")
	public Response createMessageDelete(@PathParam("version")String versionParam, @Context HttpHeaders headers, @Context UriInfo info, @Context HttpServletRequest req) {
		return createMessage(versionParam, headers, info, req);
	}
	
	/**
	 * Creates the message post.
	 *
	 * @param versionParam the version param
	 * @param headers the headers
	 * @param info the info
	 * @param req the req
	 * @return the response
	 */
	@POST
	@Path("/{uri:.*}")
	public Response createMessagePost(@PathParam("version")String versionParam, @Context HttpHeaders headers, @Context UriInfo info, @Context HttpServletRequest req) {
		return createMessage(versionParam, headers, info, req);
	}
	
	@PATCH
	@Path("/{uri:.*}")
	public Response createMessagePatch(@PathParam("version")String versionParam, @Context HttpHeaders headers, @Context UriInfo info, @Context HttpServletRequest req) {
		return createMessage(versionParam, headers, info, req);
	}
	/**
	 * Creates the message put.
	 *
	 * @param versionParam the version param
	 * @param headers the headers
	 * @param info the info
	 * @param req the req
	 * @return the response
	 */
	@PUT
	@Path("/{uri:.*}")
	public Response createMessagePut(@PathParam("version")String versionParam, @Context HttpHeaders headers, @Context UriInfo info, @Context HttpServletRequest req) {
		return createMessage(versionParam, headers, info, req);
	}
	
	
	/**
	 * Creates the message.
	 *
	 * @param versionParam the version param
	 * @param headers the headers
	 * @param info the info
	 * @param req the req
	 * @return the response
	 */
	private Response createMessage(String versionParam, HttpHeaders headers, UriInfo info, HttpServletRequest req) {
		AAIException e = new AAIException("AAI_3007");
		
		ArrayList<String> templateVars = new ArrayList<String>();

		if (templateVars.size() == 0) {
			templateVars.add("PUT");
			templateVars.add(info.getPath().toString());
			templateVars.add(versionParam);
			templateVars.add(AAIConfig.get("aai.default.api.version", ""));
		}
				
		Response response = Response
				.status(e.getErrorObject().getHTTPResponseCode())
				.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, 
						templateVars)).build();	
		
		return response;
	}
}
