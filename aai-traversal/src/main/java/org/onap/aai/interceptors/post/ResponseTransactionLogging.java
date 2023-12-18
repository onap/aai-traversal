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
package org.onap.aai.interceptors.post;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.*;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.PathSegment;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.interceptors.AAIContainerFilter;
import org.onap.aai.interceptors.AAIHeaderProperties;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.util.AAIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Priority(AAIResponseFilterPriority.RESPONSE_TRANS_LOGGING)
public class ResponseTransactionLogging extends AAIContainerFilter
    implements ContainerResponseFilter {

    private static final Logger TRANSACTION_LOGGER =
        LoggerFactory.getLogger(ResponseTransactionLogging.class);

    private final static String QUERY_API_PATH_SEGMENT = "query";
    private final static String NODES_QUERY_API_PATH_SEGMENT = "nodes-query";
    private final static String GENERIC_QUERY_API_PATH_SEGMENT = "generic-query";
    private final static String DSL_API_PATH_SEGMENT = "dsl";
    private final static String RECENTS_API_PATH_SEGMENT = "recents";
    private final static Set<String> READ_ONLY_QUERIES = getReadOnlyQueries();

    @Override
    public void filter(ContainerRequestContext requestContext,
        ContainerResponseContext responseContext) throws IOException {

        this.transLogging(requestContext, responseContext);

    }

    private void transLogging(ContainerRequestContext requestContext,
        ContainerResponseContext responseContext) {

        String logValue;
        String isGetTransactionResponseLoggingEnabled;
        String postValue;

        try {

            logValue = AAIConfig.get("aai.transaction.logging");
            isGetTransactionResponseLoggingEnabled = AAIConfig.get("aai.transaction.logging.get");
            postValue = AAIConfig.get("aai.transaction.logging.post");
        } catch (AAIException e) {
            return;
        }
        String httpMethod = requestContext.getMethod();

        if (!Boolean.parseBoolean(logValue)) {
        } else if (!Boolean.parseBoolean(postValue) && "POST".equals(httpMethod)) {
        } else {

            String transId = requestContext.getHeaderString(AAIHeaderProperties.TRANSACTION_ID);
            String fromAppId = requestContext.getHeaderString(AAIHeaderProperties.FROM_APP_ID);
            String fullUri = requestContext.getUriInfo().getRequestUri().toString();
            String requestTs = (String) requestContext.getProperty(AAIHeaderProperties.AAI_REQUEST_TS);

            String status = Integer.toString(responseContext.getStatus());

            String request = (String) requestContext.getProperty(AAIHeaderProperties.AAI_REQUEST);
            String response = this.getResponseString(responseContext);

            JsonObject logEntry = new JsonObject();
            logEntry.addProperty("transactionId", transId);
            logEntry.addProperty("status", status);
            logEntry.addProperty("rqstDate", requestTs);
            logEntry.addProperty("respDate", this.genDate());
            logEntry.addProperty("sourceId", fromAppId + ":" + transId);
            logEntry.addProperty("resourceId", fullUri);
            logEntry.addProperty("resourceType", httpMethod);
            logEntry.addProperty("rqstBuf", Objects.toString(request, ""));

            boolean recordResponse = true;
            if (!Boolean.parseBoolean(isGetTransactionResponseLoggingEnabled)
                && "GET".equals(httpMethod)) {
                recordResponse = false;
            } else {
                /**
                 * Parse the uri path and see if it is a read-only query
                 * If it is, do not record the response in the logs
                 */

                List<PathSegment> pathSegmentList = requestContext.getUriInfo().getPathSegments();
                for (PathSegment queryType : pathSegmentList) {
                    if (READ_ONLY_QUERIES.contains(queryType.toString())) {
                        recordResponse = false;
                    }
                }

                if (recordResponse) {
                    logEntry.addProperty("respBuf", Objects.toString(response, ""));
                }

                try {
                    TRANSACTION_LOGGER.debug(logEntry.toString());
                } catch (Exception e) {
                    ErrorLogHelper.logError("AAI_4000", "Exception writing transaction log.");
                }
            }
        }

    }

    private String getResponseString(ContainerResponseContext responseContext) {
        JsonObject response = new JsonObject();
        response.addProperty("ID", responseContext.getHeaderString(AAIHeaderProperties.AAI_TX_ID));
        response.addProperty("Content-Type", responseContext.getHeaders().getFirst("Content-Type").toString());
        response.addProperty("Response-Code", responseContext.getStatus());
        response.addProperty("Headers", responseContext.getHeaders().toString());
        Optional<Object> entityOptional = Optional.ofNullable(responseContext.getEntity());
        if (entityOptional.isPresent()) {
            response.addProperty("Entity", entityOptional.get().toString());
        } else {
            response.addProperty("Entity", "");
        }
        return response.toString();
    }

    private static Set<String> getReadOnlyQueries() {
        Set<String> readOnlyQueries = new HashSet<String>();
        readOnlyQueries.add(NODES_QUERY_API_PATH_SEGMENT);
        readOnlyQueries.add(GENERIC_QUERY_API_PATH_SEGMENT);
        readOnlyQueries.add(RECENTS_API_PATH_SEGMENT);
        readOnlyQueries.add(QUERY_API_PATH_SEGMENT);
        readOnlyQueries.add(DSL_API_PATH_SEGMENT);
        return readOnlyQueries;
    }

}
