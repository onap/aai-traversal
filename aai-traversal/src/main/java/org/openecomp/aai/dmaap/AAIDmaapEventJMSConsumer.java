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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.log4j.MDC;
import org.json.JSONException;
import org.json.JSONObject;
import org.openecomp.aai.logging.ErrorLogHelper;
import org.openecomp.aai.util.AAIConstants;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class AAIDmaapEventJMSConsumer implements MessageListener {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(AAIDmaapEventJMSConsumer.class);

	private Client httpClient;

	private Properties aaiEventProps;
	private String aaiEventUrl = "";

	public AAIDmaapEventJMSConsumer() throws org.apache.commons.configuration.ConfigurationException {
		super();
		try {

			if (this.httpClient == null) {
				FileReader reader = new FileReader(new File(AAIConstants.AAI_EVENT_DMAAP_PROPS));
				aaiEventProps = new Properties();
				aaiEventProps.load(reader);
				reader.close();

				String host = aaiEventProps.getProperty("host");
				String topic = aaiEventProps.getProperty("topic");
				String protocol = aaiEventProps.getProperty("Protocol");

				aaiEventUrl = protocol + "://" + host + "/events/" + topic;
				httpClient = Client.create();
			}

		} catch (IOException e) {
			ErrorLogHelper.logError("AAI_4000", "Error updating dmaap config file for aai event.");
		}

	}

	@Override
	public void onMessage(Message message) {

		String jsmMessageTxt = "";
		String aaiEvent = "";
		String eventName = "";

		if (message instanceof TextMessage) {
			try {
				jsmMessageTxt = ((TextMessage) message).getText();
				JSONObject jo = new JSONObject(jsmMessageTxt);

				if (jo.has("aaiEventPayload")) {
					aaiEvent = jo.getJSONObject("aaiEventPayload").toString();
				} else {
					return;
				}
				if (jo.getString("transId") != null) {
					MDC.put("requestId", jo.getString("transId"));
				}
				if (jo.getString("fromAppId") != null) {
					MDC.put("partnerName", jo.getString("fromAppId"));
				}
				if (jo.getString("event-topic") != null) {
					eventName = jo.getString("event-topic");
				}

				LOGGER.info(eventName + "|" + aaiEvent);
				if ("AAI-EVENT".equals(eventName)) {
					this.sentWithHttp(this.httpClient, this.aaiEventUrl, aaiEvent);
				}
			} catch (java.net.SocketException e) {
				if (!e.getMessage().contains("Connection reset")) {
					LOGGER.error("AAI_7304 Error reaching DMaaP to send event. " + aaiEvent, e);
				}
			} catch (IOException e) {
				LOGGER.error("AAI_7304 Error reaching DMaaP to send event. " + aaiEvent, e);
			} catch (JMSException | JSONException e) {
				LOGGER.error("AAI_7350 Error parsing aaievent jsm message for sending to dmaap. " + jsmMessageTxt, e);
			} catch (Exception e) {
				LOGGER.error("AAI_7350 Error sending message to dmaap. " + jsmMessageTxt, e);
			}
		}

	}

	private boolean sentWithHttp(Client client, String url, String aaiEvent) throws IOException {

		WebResource webResource = client.resource(url);

		ClientResponse response = webResource
				.accept(MediaType.APPLICATION_JSON)
				.type(MediaType.APPLICATION_JSON)
				.post(ClientResponse.class, aaiEvent);

		if (response.getStatus() != 200) {
			LOGGER.info("Failed : HTTP error code : " + response.getStatus());
			return false;
		}
		return true;
	}
}
