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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.restcore.RESTAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Class EchoResponse.
 */
@Path("/util")
@Component
public class EchoResponse extends RESTAPI {

	protected static String authPolicyFunctionName = "util";

	private final AaiGraphChecker aaiGraphChecker;

	@Autowired
	public EchoResponse(AaiGraphChecker aaiGraphChecker) {
		this.aaiGraphChecker = aaiGraphChecker;
	}

	private static final String UP_RESPONSE="{\"status\":\"UP\",\"groups\":[\"liveness\",\"readiness\"]}";

	/**
	 * Simple health-check API that echos back the X-FromAppId and X-TransactionId
	 * to clients.
	 * If there is a query string, the healthcheck will also check for database connectivity.
	 *
	 * @param headers  the headers
	 * @param req      the request
	 * @param myAction if exists will cause database connectivity check
	 * @return the response
	 */
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@Path("/echo")
	public Response echoResult(@Context HttpHeaders headers, @Context HttpServletRequest req,
			@QueryParam("action") String myAction) {

		String fromAppId;
		String transId;
		ArrayList<String> templateVars = new ArrayList<>();

		try {
			fromAppId = getFromAppId(headers);
			transId = getTransId(headers);
		} catch (AAIException aaiException) {
			templateVars.add("PUT uebProvider");
			templateVars.add("addTopic");
			ErrorLogHelper.logException(aaiException);
			return generateFailureResponse(headers, templateVars, aaiException);
		}

		templateVars.add(fromAppId);
		templateVars.add(transId);
		if (myAction != null) {
			try {
				if (!aaiGraphChecker.isAaiGraphDbAvailable()) {
					throw new AAIException("AAI_5105", "Error establishing a database connection");
				}
				return generateSuccessResponse();
			} catch (AAIException aaiException) {
				ErrorLogHelper.logException(aaiException);
				return generateFailureResponse(headers, templateVars, aaiException);
			} catch (Exception e) {
				AAIException aaiException = new AAIException("AAI_4000", e);
				ErrorLogHelper.logException(aaiException);
				return generateFailureResponse(headers, templateVars, aaiException);
			}
		}
		return generateSuccessResponse();
	}

	private Response generateSuccessResponse() {
    	return Response.status(Status.OK)
    			.entity(UP_RESPONSE)
    			.build();
    }

    private Response generateFailureResponse(HttpHeaders headers, ArrayList<String> templateVariables,
    		AAIException aaiException) {
    	return Response.status(aaiException.getErrorObject().getHTTPResponseCode()).entity(ErrorLogHelper
    			.getRESTAPIErrorResponseWithLogging(headers.getAcceptableMediaTypes(), aaiException, templateVariables))
    			.build();
    }
}
