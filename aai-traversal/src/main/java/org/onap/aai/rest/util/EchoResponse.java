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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.util;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.restcore.RESTAPI;

/**
 * The Class EchoResponse.
 */
@Path("/util")
public class EchoResponse extends RESTAPI {
	
	protected static String authPolicyFunctionName = "util";
		
	public static final String ECHO_PATH = "/echo";

	/**
	 * Simple health-check API that echos back the X-FromAppId and X-TransactionId to clients.
	 * If there is a query string, a transaction gets logged into hbase, proving the application is connected to the data store.
	 * If there is no query string, no transacction logging is done to hbase.
	 *
	 * @param headers the headers
	 * @param req the req
	 * @param myAction if exists will cause transaction to be logged to hbase
	 * @return the response
	 */
	@GET
	@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path(ECHO_PATH)
	public Response echoResult(@Context HttpHeaders headers, @Context HttpServletRequest req,
			@QueryParam("action") String myAction) {
		AAIException ex = null;
		Response response;
		String fromAppId;
		String transId;
		
		try { 
			fromAppId = getFromAppId(headers );
			transId = getTransId(headers);
		} catch (AAIException e) { 
			ArrayList<String> templateVars = new ArrayList<>();
			templateVars.add("PUT uebProvider");
			templateVars.add("addTopic");
			return Response
					.status(e.getErrorObject().getHTTPResponseCode())
					.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
					.build();
		}
		
		try {
			
			HashMap<AAIException, ArrayList<String>> exceptionList = new HashMap<>();
					
			ArrayList<String> templateVars = new ArrayList<>();
			templateVars.add(fromAppId);
			templateVars.add(transId);
		
			exceptionList.put(new AAIException("AAI_0002", "OK"), templateVars);
				
			response = Response.status(Status.OK)
					.entity(ErrorLogHelper.getRESTAPIInfoResponse(
							headers.getAcceptableMediaTypes(), exceptionList))
							.build();
			
		} catch (Exception e) {
			ex = new AAIException("AAI_4000", e);
			ArrayList<String> templateVars = new ArrayList<>();
			templateVars.add(Action.GET.name());
			templateVars.add(fromAppId +" "+transId);

			response = Response
					.status(Status.INTERNAL_SERVER_ERROR)
					.entity(ErrorLogHelper.getRESTAPIErrorResponse(
							headers.getAcceptableMediaTypes(), ex,
							templateVars)).build();

		} finally {
			if (ex != null) {
				ErrorLogHelper.logException(ex);
			}

		}
		
		return response;
	}

}
