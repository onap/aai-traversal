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

package org.openecomp.aai.interceptors;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.jaxrs.interceptor.JAXRSInInterceptor;
import org.apache.cxf.message.Message;

import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.logging.ErrorLogHelper;
import org.openecomp.aai.rest.util.EchoResponse;
import org.openecomp.aai.util.AAIConfig;
import org.openecomp.aai.util.AAIConstants;
import org.openecomp.aai.util.HbaseSaltPrefixer;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class AAILogJAXRSInInterceptor extends JAXRSInInterceptor {

	protected final String COMPONENT = "aairest";
	protected final String CAMEL_REQUEST ="CamelHttpUrl";
	private static final Pattern uuidPattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$");
	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(AAILogJAXRSInInterceptor.class);
	
	/**
	 * {@inheritDoc}
	 */
	public void handleMessage(Message message) {

		boolean go = false;
		String uri = null;
		String query = null;
		try {
		
			uri = (String)message.get(CAMEL_REQUEST);
			if (uri != null) { 
				query = (String)message.get(Message.QUERY_STRING);
			}
			
			if (AAIConfig.get(AAIConstants.AAI_LOGGING_HBASE_INTERCEPTOR).equalsIgnoreCase("true") &&
					AAIConfig.get(AAIConstants.AAI_LOGGING_HBASE_ENABLED).equalsIgnoreCase("true")) {
				go = true;
				message.getExchange().put("AAI_LOGGING_HBASE_ENABLED", 1);
				if (AAIConfig.get(AAIConstants.AAI_LOGGING_HBASE_LOGREQUEST).equalsIgnoreCase("true") ) {
					message.getExchange().put("AAI_LOGGING_HBASE_LOGREQUEST", 1);
				}
				if (AAIConfig.get(AAIConstants.AAI_LOGGING_HBASE_LOGRESPONSE).equalsIgnoreCase("true") ) {
					message.getExchange().put("AAI_LOGGING_HBASE_LOGRESPONSE", 1);
				}
			}
			if (AAIConfig.get(AAIConstants.AAI_LOGGING_TRACE_ENABLED).equalsIgnoreCase("true") ) {
				go = true;
				message.getExchange().put("AAI_LOGGING_TRACE_ENABLED", 1);
				if (AAIConfig.get(AAIConstants.AAI_LOGGING_TRACE_LOGREQUEST).equalsIgnoreCase("true") ) {
					message.getExchange().put("AAI_LOGGING_TRACE_LOGREQUEST", 1);
				}
				if (AAIConfig.get(AAIConstants.AAI_LOGGING_TRACE_LOGRESPONSE).equalsIgnoreCase("true") ) {
					message.getExchange().put("AAI_LOGGING_TRACE_LOGRESPONSE", 1);
				}
			}
		} catch (AAIException e1) {
			ErrorLogHelper.logException(e1);
		}
		
		if (uri.contains(EchoResponse.echoPath)) {
			// if it's a health check, we don't want to log ANYTHING if it's a lightweight one
			if (query == null) {
				if (message.getExchange().containsKey("AAI_LOGGING_HBASE_ENABLED")) {
					message.getExchange().remove("AAI_LOGGING_HBASE_ENABLED");
				}
				if (message.getExchange().containsKey("AAI_LOGGING_TRACE_ENABLED")) { 
					message.getExchange().remove("AAI_LOGGING_TRACE_ENABLED");
				}
				go = false;
			}
		}
		else if (uri.contains("/translog/")) {
			// if it's a translog query, we don't want to log the responses
			if (message.getExchange().containsKey("AAI_LOGGING_HBASE_LOGRESPONSE")) {
				message.getExchange().remove("AAI_LOGGING_HBASE_LOGRESPONSE");
			}
			if (message.getExchange().containsKey("AAI_LOGGING_TRACE_LOGRESPONSE")) {
				message.getExchange().remove("AAI_LOGGING_TRACE_LOGRESPONSE");
			}
		}
		
		if (go == false) { // there's nothing to do 
			return;
		}

		// DONE: get a TXID based on hostname, time (YYYYMMDDHHMMSSMILLIS,  and LoggingMessage.nextId(); 20150326145301-1
		String now = genDate();

		message.getExchange().put("AAI_RQST_TM", now);

		String id = (String)message.getExchange().get(LoggingMessage.ID_KEY);

		String fullId = null;
		try {
			if (id == null) {
				id = LoggingMessage.nextId();
			}
			fullId = AAIConfig.get(AAIConstants.AAI_NODENAME) + "-" + now + "-" + id;
			fullId = HbaseSaltPrefixer.getInstance().prependSalt(fullId);
			message.getExchange().put(LoggingMessage.ID_KEY, fullId);
		} catch (AAIException e1) {
			LOGGER.debug("config problem", e1);
		}
		
		if (fullId == null) { 
			fullId = now + "-" + id;
			fullId = HbaseSaltPrefixer.getInstance().prependSalt(fullId);
		}
		message.put(LoggingMessage.ID_KEY, fullId);
		final LoggingMessage buffer = new LoggingMessage("Message", fullId);

		Integer responseCode = (Integer)message.get(Message.RESPONSE_CODE);
		if (responseCode != null) {
			buffer.getResponseCode().append(responseCode);
		}

		String encoding = (String)message.get(Message.ENCODING);

		if (encoding != null) {
			buffer.getEncoding().append(encoding);
		}
		String httpMethod = (String)message.get(Message.HTTP_REQUEST_METHOD);
		if (httpMethod != null) {
			buffer.getHttpMethod().append(httpMethod);
		}

		String ct = (String)message.get(Message.CONTENT_TYPE);
		if (ct != null) {
			if ("*/*".equals(ct)) {
				message.put(Message.CONTENT_TYPE, MediaType.APPLICATION_JSON);
				ct = MediaType.APPLICATION_JSON;
			}
			buffer.getContentType().append(ct);

		}
		Object headers = message.get(Message.PROTOCOL_HEADERS);
		if (headers != null) {
			buffer.getHeader().append(headers);
			
			Map<String, List<String>> headersList = CastUtils.cast((Map<?, ?>)message.get(Message.PROTOCOL_HEADERS));
			String transId = "";
			List<String> xt = headersList.get("X-TransactionId");
			String newTransId = transId;
			boolean missingTransId = false;
			boolean replacedTransId = false;
			String logMsg = null;
			if (xt != null) {
				for (String transIdValue : xt) {
					transId = transIdValue;
				}	
				Matcher matcher = uuidPattern.matcher(transId);
				if (!matcher.find()) {
					replacedTransId = true;
					// check if there's a colon, and check the first group?
					if (transId.contains(":")) {
						String[] uuidParts = transId.split(":");
						Matcher matcher2 = uuidPattern.matcher(uuidParts[0]);
						if (matcher2.find()) { 
							newTransId = uuidParts[0];
						} else {
							// punt, we tried to find it, it has a colon but no UUID-1
							newTransId = UUID.randomUUID().toString();
						}
					} else {
						newTransId = UUID.randomUUID().toString();
					}
				}
			} else { 
				newTransId = UUID.randomUUID().toString();
				missingTransId = true;
			}
						
			if (missingTransId || replacedTransId) {
				List<String> txList = new ArrayList<String>();
				txList.add(newTransId);
				headersList.put("X-TransactionId", txList);
				if (missingTransId) { 
					logMsg = "Missing requestID. Assigned " + newTransId;
				} else if (replacedTransId) { 
					logMsg = "Replaced invalid requestID of " + transId + " Assigned " + newTransId;
				}
			} 
			
			List<String> contentType = headersList.get("Content-Type");
			if (contentType == null) {
				ct = (String)message.get(Message.CONTENT_TYPE);
				headersList.put(Message.CONTENT_TYPE, Collections.singletonList(ct));
			}
			
			LOGGER.auditEvent("REST " + httpMethod + " " + ((query != null)? uri+"?"+query : uri) + " HbaseTxId=" + fullId);
			LOGGER.info(logMsg);
		}


		if (uri != null) {
			buffer.getAddress().append(uri);
			if (query != null) {
				buffer.getAddress().append("?").append(query);
			}
		}

		InputStream is = message.getContent(InputStream.class);
		if (is != null) {
			try {
				String currentPayload = IOUtils.toString(is, "UTF-8");
				IOUtils.closeQuietly(is);
				buffer.getPayload().append(currentPayload);
				is = IOUtils.toInputStream(currentPayload, "UTF-8");
				message.setContent(InputStream.class, is);
				IOUtils.closeQuietly(is);
			} catch (Exception e) { 
				// It's ok to not have request input content
				// throw new Fault(e); 
			}
		}

		// this will be saved in the message exchange, and can be pulled out later...
		message.getExchange().put(fullId + "_REQUEST", buffer.toString());
	}

	/**
	 * Gen date.
	 *
	 * @param aaiLogger the aai logger
	 * @param logline the logline
	 * @return the string
	 */
	protected String genDate() {
		Date date = new Date();
		DateFormat formatter = null;
		try {
			formatter = new SimpleDateFormat(AAIConfig.get(AAIConstants.HBASE_TABLE_TIMESTAMP_FORMAT));
		} catch (AAIException ex) {
			ErrorLogHelper.logException(ex);
		} finally {
			if (formatter == null) {
				formatter = new SimpleDateFormat("YYMMdd-HH:mm:ss:SSS");
			}
		}

		return formatter.format(date);
	}

}
