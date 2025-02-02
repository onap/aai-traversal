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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.interceptors.pre;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.interceptors.AAIContainerFilter;
import org.onap.aai.interceptors.AAIHeaderProperties;
import org.onap.aai.util.AAIConfig;
import org.onap.aai.util.AAIConstants;
import org.onap.aai.util.HbaseSaltPrefixer;
import org.springframework.util.ObjectUtils;

@PreMatching
@Priority(AAIRequestFilterPriority.REQUEST_TRANS_LOGGING)
public class RequestTransactionLogging extends AAIContainerFilter
    implements ContainerRequestFilter {

    private static final String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_JSON;
    private static final String DEFAULT_RESPONSE_TYPE = MediaType.APPLICATION_XML;

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String ACCEPT = "Accept";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String WILDCARD = "*/*";
    private static final String APPLICATION_JSON = "application/json";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException { 
        String currentTimeStamp = genDate();
        String fullId = this.getAAITxIdToHeader(currentTimeStamp);
        requestContext.setProperty(AAIHeaderProperties.AAI_TX_ID, fullId);
        requestContext.setProperty(AAIHeaderProperties.AAI_REQUEST, this.getRequest(requestContext, fullId));
        requestContext.setProperty(AAIHeaderProperties.AAI_REQUEST_TS, currentTimeStamp);
        this.addDefaultContentType(requestContext);
    }

    private void addDefaultContentType(ContainerRequestContext requestContext) {

        String contentType = requestContext.getHeaderString(CONTENT_TYPE);
        String acceptType = requestContext.getHeaderString(ACCEPT);

        if (contentType == null || contentType.contains(TEXT_PLAIN)) {
            requestContext.getHeaders().putSingle(CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
        }

        if (WILDCARD.equals(acceptType) || ObjectUtils.isEmpty(acceptType)
            || acceptType.contains(TEXT_PLAIN)) {
            UriInfo uriInfo = requestContext.getUriInfo();
            if (uriInfo != null) {
                String path = uriInfo.getPath();
                if (path.endsWith("/dsl") || path.endsWith("/query")
                    || path.contains("/recents/")) {
                    requestContext.getHeaders().putSingle(ACCEPT, APPLICATION_JSON);
                } else {
                    requestContext.getHeaders().putSingle(ACCEPT, DEFAULT_RESPONSE_TYPE);
                }
            } else {
                requestContext.getHeaders().putSingle(ACCEPT, DEFAULT_RESPONSE_TYPE);
            }
        }
    }

    private String getAAITxIdToHeader(String currentTimeStamp) {
        String txId = UUID.randomUUID().toString();
        try {
            Random rand = new SecureRandom();
            int number = rand.nextInt(99999);
            txId = HbaseSaltPrefixer.getInstance().prependSalt(
                AAIConfig.get(AAIConstants.AAI_NODENAME) + "-" + currentTimeStamp + "-" + number); // new
                                                                                                   // Random(System.currentTimeMillis()).nextInt(99999)
        } catch (AAIException e) {
        }

        return txId;
    }

    private String getRequest(ContainerRequestContext requestContext, String fullId) throws IOException {

        JsonObject request = new JsonObject();
        request.addProperty("ID", fullId);
        request.addProperty("Http-Method", requestContext.getMethod());
        request.addProperty("Headers", requestContext.getHeaders().toString());

        // String requestEntity = IOUtils.toString(requestContext.getEntityStream(), Charsets.UTF_8);
        // InputStream in = IOUtils.toInputStream(requestEntity, Charset.defaultCharset());
        // requestContext.setEntityStream(in);

        return request.toString();
    }

}
