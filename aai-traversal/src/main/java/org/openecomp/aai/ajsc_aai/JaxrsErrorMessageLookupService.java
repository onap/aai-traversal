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

package org.openecomp.aai.ajsc_aai;

//import java.util.HashMap;
//import java.util.Map;

//import javax.ws.rs.GET;
//import javax.ws.rs.HeaderParam;
//import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;
//import javax.ws.rs.Produces;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.context.ContextLoader;
//import org.springframework.web.context.WebApplicationContext;

//import ajsc.ErrorMessageLookupService;

//@Path("/errormessage")
//public class JaxrsErrorMessageLookupService {

	//private final static Logger logger = LoggerFactory
			//.getLogger(ErrorMessageLookupService.class);

	//*
	 //Gets the message.
	
	 //@param input the input
	 //@param errorCode the error code
	 //@param appId the app id
	 //@param operation the operation
	 //@param messageText the message text
	 //@param isRESTService the is REST service
	 //@param faultEntity the fault entity
	 //@param ConvID the conv ID
	 //@return the message
	//@GET
	//@Path("/emls")
	//@Produces("text/plain")
	//public String getMessage(@PathParam("input") String input,
			//@HeaderParam("errorCode") String errorCode,
			//@HeaderParam("appId") String appId,
			//@HeaderParam("operation") String operation,
			//@HeaderParam("messageText") String messageText,
			//@HeaderParam("isRESTService") String isRESTService,
			//@HeaderParam("faultEntity") String faultEntity,
			//@HeaderParam("ConvID") String ConvID) {

		//Map<String, String> headers = new HashMap<String, String>();
		//headers.put(errorCode, errorCode);
		//headers.put(appId, appId);
		//headers.put(operation, operation);
		//headers.put(messageText, messageText);
		//headers.put(isRESTService, isRESTService);
		//headers.put(faultEntity, faultEntity);
		//headers.put(ConvID, ConvID);
		
		//WebApplicationContext applicationContext = ContextLoader
				//.getCurrentWebApplicationContext();

		//ErrorMessageLookupService e = (ErrorMessageLookupService) applicationContext
				//.getBean("errorMessageLookupService");

		//String message = e.getExceptionDetails(appId, operation, errorCode,
				//messageText,isRESTService, faultEntity, ConvID);

		//System.out.println("Error code = " + errorCode);
		//System.out.println("appId = " + appId);
		//System.out.println("operation = " + operation);
		//System.out.println("messageText = " + messageText);
		//System.out.println("isRESTService = " + isRESTService);
		//System.out.println("faultEntity = " + faultEntity);
		//System.out.println("ConvID = " + ConvID);
		//return "The exception message is:\n " + message;
	//}

//}

