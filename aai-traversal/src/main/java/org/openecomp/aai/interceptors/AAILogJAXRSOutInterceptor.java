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

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.jaxrs.interceptor.JAXRSOutInterceptor;
import org.apache.cxf.message.Message;

import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.logging.ErrorLogHelper;
import org.openecomp.aai.util.AAIConfig;
import org.openecomp.aai.util.AAIConstants;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

// right after the request is complete, there may be content
public class AAILogJAXRSOutInterceptor extends JAXRSOutInterceptor {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(AAILogJAXRSOutInterceptor.class);
	
	protected final String COMPONENT = "aairest";
	protected final String CAMEL_REQUEST = "CamelHttpUrl";

	/**
	 * {@inheritDoc}
	 */
	public void handleMessage(Message message) {

		String fullId = (String) message.getExchange().get(LoggingMessage.ID_KEY);

		Map<String, List<String>> headers = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
		if (headers == null) {
			headers = new HashMap<String, List<String>>();
		}

		headers.put("X-AAI-TXID", Collections.singletonList(fullId));
		message.put(Message.PROTOCOL_HEADERS, headers);

		Message outMessage = message.getExchange().getOutMessage();
		final OutputStream os = outMessage.getContent(OutputStream.class);
		if (os == null) {
			return;
		}

		// we only want to register the callback if there is good reason for it.
		if (message.getExchange().containsKey("AAI_LOGGING_HBASE_ENABLED") || message.getExchange().containsKey("AAI_LOGGING_TRACE_ENABLED")) {

			final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
			message.setContent(OutputStream.class, newOut);
			newOut.registerCallback(new LoggingCallback(message, os));
		}

	}

	class LoggingCallback implements CachedOutputStreamCallback {

		private final Message message;
		private final OutputStream origStream;

		public LoggingCallback(final Message msg, final OutputStream os) {
			this.message = msg;
			this.origStream = os;
		}

		public void onFlush(CachedOutputStream cos) {

		}

		public void onClose(CachedOutputStream cos) {

			String getValue = "";
			String postValue = "";
			String logValue = "";

			try {
				logValue = AAIConfig.get("aai.transaction.logging");
				getValue = AAIConfig.get("aai.transaction.logging.get");
				postValue = AAIConfig.get("aai.transaction.logging.post");
			} catch (AAIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (!message.getExchange().containsKey("AAI_LOGGING_HBASE_ENABLED") && !message.getExchange().containsKey("AAI_LOGGING_TRACE_ENABLED")) {
				return;
			}

			String fullId = (String) message.getExchange().get(LoggingMessage.ID_KEY);

			Message inMessage = message.getExchange().getInMessage();
			String transId = null;
			String fromAppId = null;

			Map<String, List<String>> headersList = CastUtils.cast((Map<?, ?>) inMessage.get(Message.PROTOCOL_HEADERS));
			if (headersList != null) {
				List<String> xt = headersList.get("X-TransactionId");
				if (xt != null) {
					for (String transIdValue : xt) {
						transId = transIdValue;
					}
				}
				List<String> fa = headersList.get("X-FromAppId");
				if (fa != null) {
					for (String fromAppIdValue : fa) {

						fromAppId = fromAppIdValue;
					}
				}
			}

			String httpMethod = (String) inMessage.get(Message.HTTP_REQUEST_METHOD);

			String uri = (String) inMessage.get(CAMEL_REQUEST);
			String fullUri = uri;
			if (uri != null) {
				String query = (String) message.get(Message.QUERY_STRING);
				if (query != null) {
					fullUri = uri + "?" + query;
				}
			}

			String request = (String) message.getExchange().get(fullId + "_REQUEST");

			Message outMessage = message.getExchange().getOutMessage();

			final LoggingMessage buffer = new LoggingMessage("OUTMessage", fullId);

			// should we check this, and make sure it's not an error?
			Integer responseCode = (Integer) outMessage.get(Message.RESPONSE_CODE);
			if (responseCode == null) {
				responseCode = 200; // this should never happen, but just in
									// case we don't get one
			}
			buffer.getResponseCode().append(responseCode);

			String encoding = (String) outMessage.get(Message.ENCODING);

			if (encoding != null) {
				buffer.getEncoding().append(encoding);
			}

			String ct = (String) outMessage.get(Message.CONTENT_TYPE);
			if (ct != null) {
				buffer.getContentType().append(ct);
			}

			Object headers = outMessage.get(Message.PROTOCOL_HEADERS);
			if (headers != null) {
				buffer.getHeader().append(headers);
			}

			Boolean ss = false;
			if (responseCode >= 200 && responseCode <= 299) {
				ss = true;
			}
			String response = buffer.toString();

			// this should have been set by the in interceptor
			String rqstTm = (String) message.getExchange().get("AAI_RQST_TM");

			// just in case it wasn't, we'll put this here. not great, but it'll
			// have a val.
			if (rqstTm == null) {
				rqstTm = genDate();
			}


			String respTm = genDate();

			try {
				String actualRequest = request;
				StringBuilder builder = new StringBuilder();
				cos.writeCacheTo(builder, 100000);
				// here comes my xml:
				String payload = builder.toString();

				String actualResponse = response;
				if (payload == null) {

				} else {
					actualResponse = response + payload;
				}

				// we only log to AAI log if it's eanbled in the config props
				// file
				if (message.getExchange().containsKey("AAI_LOGGING_TRACE_ENABLED")) {

					if (message.getExchange().containsKey("AAI_LOGGING_TRACE_LOGREQUEST")) {

						// strip newlines from request
						String traceRequest = actualRequest;
						traceRequest = traceRequest.replace("\n", " ");
						traceRequest = traceRequest.replace("\r", "");
						traceRequest = traceRequest.replace("\t", "");
						LOGGER.debug(traceRequest);
					}
					if (message.getExchange().containsKey("AAI_LOGGING_TRACE_LOGRESPONSE")) {
						// strip newlines from response
						String traceResponse = actualResponse;
						traceResponse = traceResponse.replace("\n", " ");
						traceResponse = traceResponse.replace("\r", "");
						traceResponse = traceResponse.replace("\t", "");

						LOGGER.debug(traceResponse);
					}
				}

				// we only log to HBASE if it's enabled in the config props file
				// TODO: pretty print XML/JSON. we might need to get the payload
				// and envelope seperately
				if (message.getExchange().containsKey("AAI_LOGGING_HBASE_ENABLED")) {
					if (!message.getExchange().containsKey("AAI_LOGGING_HBASE_LOGREQUEST")) {
						actualRequest = "loggingDisabled";
					}
					if (!message.getExchange().containsKey("AAI_LOGGING_HBASE_LOGRESPONSE")) {
						actualResponse = "loggingDisabled";
					}

					LOGGER.debug("action={}, urlin={}, HbTransId={}", httpMethod, fullUri, fullId);

					if (logValue.equals("false")) {
					} else if (getValue.equals("false") && httpMethod.equals("GET")) {
					} else if (postValue.equals("false") && httpMethod.equals("POST")) {
					} else {
						putTransaction(transId, responseCode.toString(), rqstTm, respTm, fromAppId + ":" + transId, fullUri, httpMethod, request, response, actualResponse);

					}
				}
			} catch (Exception ex) {
				// ignore
			}

			message.setContent(OutputStream.class, origStream);
			
			LOGGER.auditEvent("HTTP Response Code: {}", responseCode.toString());
		}

	}

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

	public String putTransaction(String tid, String status, String rqstTm, String respTm, String srcId, String rsrcId, String rsrcType, String rqstBuf, String respBuf, String actualResponse) {
		String tm = null;
		String fromAppId = srcId.substring(0, srcId.indexOf(':'));
		String transId = srcId.substring(srcId.indexOf(':') + 1);

		if (tid == null || "".equals(tid)) {
			Date date = new Date();
			DateFormat formatter = null;
			try {
				formatter = new SimpleDateFormat(AAIConfig.get(AAIConstants.HBASE_TABLE_TIMESTAMP_FORMAT));
			} catch (Exception e) {
				formatter = new SimpleDateFormat("YYYYMMdd-HH:mm:ss:SSS");
			}
			tm = formatter.format(date);	
			tid = tm + "-";
		}

		String htid = tid;

		if (rqstTm == null || "".equals(rqstTm)) {
			rqstTm = tm;
		}

		if (respTm == null || "".equals(respTm)) {
			respTm = tm;
		}

		try {
			LOGGER.debug(" transactionId:" + tid + " status: " + status + " rqstDate: " + rqstTm + " respDate: " + respTm + " sourceId: " + srcId + " resourceId: "
					+ rsrcId + " resourceType: " + rsrcType + " payload rqstBuf: " + rqstBuf + " payload respBuf: " + respBuf + " Payload Error Messages: " + actualResponse);
			return htid;
		} catch (Exception e) {
			ErrorLogHelper.logError("AAI_4000", "Exception updating HBase:");
			return htid;
		}

	}
}
