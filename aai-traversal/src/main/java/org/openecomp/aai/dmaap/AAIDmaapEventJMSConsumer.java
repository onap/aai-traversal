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

package org.openecomp.aai.dmaap;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.MDC;
import org.eclipse.jetty.util.security.Password;
import org.json.JSONException;
import org.json.JSONObject;

import org.openecomp.aai.logging.ErrorLogHelper;
import org.openecomp.aai.util.AAIConstants;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
//import com.att.nsa.mr.client.MRBatchingPublisher;
//import com.att.nsa.mr.client.MRClientFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class AAIDmaapEventJMSConsumer implements MessageListener {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(AAIDmaapEventJMSConsumer.class);

	private final static String COMPONENT = "aaiDmaapEvent";
	//private MRBatchingPublisher adp = null;

	private Properties props;

	private String username;
	private String password;
	private String contentType;

	private String url;
	private Client client;

	public AAIDmaapEventJMSConsumer() throws org.apache.commons.configuration.ConfigurationException {
		//super();

		//if (this.adp == null) {
			//try {
				//FileReader reader = new FileReader(new File(AAIConstants.AAI_EVENT_DMAAP_PROPS));
				//props = new Properties();
				//props.load(reader);
				//props.setProperty("DME2preferredRouterFilePath", AAIConstants.AAI_HOME_ETC_APP_PROPERTIES + "preferredRoute.txt");
				//if (props.getProperty("password") != null && props.getProperty("password").startsWith("OBF:")) {
					//props.setProperty("password", Password.deobfuscate(props.getProperty("password")));
				//}
				//this.adp = MRClientFactory.createBatchingPublisher(props);

				//String host = props.getProperty("host");
				//String topic = props.getProperty("topic");
				//String protocol = props.getProperty("Protocol");

				//username = props.getProperty("username");
				//password = props.getProperty("password");
				//contentType = props.getProperty("contenttype");

				//url = protocol + "://" + host + "/events/" + topic;
				//client = Client.create();
				//client.addFilter(new HTTPBasicAuthFilter(username, password));

			//} catch (IOException e) {
				//ErrorLogHelper.logError("AAI_4000", "Error updating dmaap config file for aai event.");
			//}
		//}

	}

	@Override
	public void onMessage(Message message) {

		//String jsmMessageTxt = "";
		//String aaiEvent = "";
		//String transId = "";
		//String fromAppId = "";
		//String fullId = "";

		//if (message instanceof TextMessage) {
			//try {
				//jsmMessageTxt = ((TextMessage) message).getText();
				//JSONObject jo = new JSONObject(jsmMessageTxt);

				//if (jo.has("aaiEventPayload")) {
					//aaiEvent = jo.getJSONObject("aaiEventPayload").toString();
				//} else {
					//return;
				//}
				//if (jo.getString("transId") != null) {
					//MDC.put("requestId", jo.getString("transId"));
				//}
				//if (jo.getString("fromAppId") != null) {
					//MDC.put("partnerName", jo.getString("fromAppId"));
				//}
				//if (jo.getString("fullId") != null) {
					//fullId = jo.getString("fullId");
				//}

				//LOGGER.info(fullId + "|" + transId + "|" + fromAppId + "|" + aaiEvent);

				//String environment = System.getProperty("lrmRO");

				//if (environment == null) {
					//this.adp.send(aaiEvent);
				//} else {
					//if (environment.startsWith("dev") || environment.startsWith("testINT") || environment.startsWith("testEXT")) {

						//WebResource webResource = client.resource(url);

						//ClientResponse response = webResource.accept(contentType).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, aaiEvent);

						//if (response.getStatus() != 200) {
							//System.out.println("Failed : HTTP error code : " + response.getStatus());
						//}
					//} else {
						//this.adp.send(aaiEvent);
					//}
				//}

			//} catch (IOException e) {
				//if (e instanceof java.net.SocketException) {
					//if (e.getMessage().contains("Connection reset")) {
					//} else {
						//ErrorLogHelper.logError("AAI_7304", "Error reaching DMaaP to send event. " + aaiEvent);
					//}
				//} else {
					//ErrorLogHelper.logError("AAI_7304", "Error reaching DMaaP to send event. " + aaiEvent);
				//}
			//} catch (JMSException | JSONException e) {
				//ErrorLogHelper.logError("AAI_7350", "Error parsing aaievent jsm message for sending to dmaap. " + jsmMessageTxt);
			//}
		//}
	}
}
