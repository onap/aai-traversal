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
package org.onap.aai.interceptors.pre;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.glassfish.jersey.server.ContainerException;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.interceptors.AAIContainerFilter;
import org.onap.aai.interceptors.AAIHeaderProperties;
import org.onap.aai.util.AAIConfig;
import org.onap.aai.util.AAIConstants;
import org.onap.aai.util.HbaseSaltPrefixer;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;

@PreMatching
@Priority(AAIRequestFilterPriority.REQUEST_TRANS_LOGGING)
public class RequestTransactionLogging extends AAIContainerFilter implements ContainerRequestFilter {

	@Autowired
	private HttpServletRequest httpServletRequest;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		String currentTimeStamp = genDate();
		String fullId = this.getAAITxIdToHeader(currentTimeStamp);
		this.addToRequestContext(requestContext, AAIHeaderProperties.AAI_TX_ID, fullId);
		this.addToRequestContext(requestContext, AAIHeaderProperties.AAI_REQUEST, this.getRequest(requestContext, fullId));
		this.addToRequestContext(requestContext, AAIHeaderProperties.AAI_REQUEST_TS, currentTimeStamp);
	}

	private void addToRequestContext(ContainerRequestContext requestContext, String name, String aaiTxIdToHeader) {
		requestContext.setProperty(name, aaiTxIdToHeader);
	}

	private String getAAITxIdToHeader(String currentTimeStamp) {
		String txId = UUID.randomUUID().toString();
		try {
			txId = HbaseSaltPrefixer.getInstance().prependSalt(AAIConfig.get(AAIConstants.AAI_NODENAME) + "-"
					+ currentTimeStamp + "-" + new Random(System.currentTimeMillis()).nextInt(99999));
		} catch (AAIException e) {
		}

		return txId;
	}

	private String getRequest(ContainerRequestContext requestContext, String fullId) {

		JsonObject request = new JsonObject();
		request.addProperty("ID", fullId);
		request.addProperty("Http-Method", requestContext.getMethod());
		String contentType = httpServletRequest.getContentType();

		if(contentType == null){
			contentType = MediaType.APPLICATION_JSON;
			requestContext.getHeaders().add("Content-Type", contentType);
		}

		request.addProperty("Content-Type", contentType);
		request.addProperty("Headers", requestContext.getHeaders().toString());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = requestContext.getEntityStream();

		try {
			if (in.available() > 0) {
				ReaderWriter.writeTo(in, out);
				byte[] requestEntity = out.toByteArray();
				request.addProperty("Payload", new String(requestEntity, "UTF-8"));
				requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
			}
		} catch (IOException ex) {
			throw new ContainerException(ex);
		}

		return request.toString();
	}

}
