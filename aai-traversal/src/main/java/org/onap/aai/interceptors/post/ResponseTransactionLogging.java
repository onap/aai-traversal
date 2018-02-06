package org.onap.aai.interceptors.post;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.interceptors.AAIContainerFilter;
import org.onap.aai.interceptors.AAIHeaderProperties;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.util.AAIConfig;
import org.springframework.beans.factory.annotation.Autowired;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.gson.JsonObject;

@Priority(AAIResponseFilterPriority.RESPONSE_TRANS_LOGGING)
public class ResponseTransactionLogging extends AAIContainerFilter implements ContainerResponseFilter {

	private static final EELFLogger TRANSACTION_LOGGER = EELFManager.getInstance().getLogger(ResponseTransactionLogging.class);

	@Autowired
	private HttpServletResponse httpServletResponse;

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		this.transLogging(requestContext, responseContext);

	}

	private void transLogging(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

		String logValue;
		String getValue;
		String postValue;
		
		try {
			logValue = AAIConfig.get("aai.transaction.logging");
			getValue = AAIConfig.get("aai.transaction.logging.get");
			postValue = AAIConfig.get("aai.transaction.logging.post");
		} catch (AAIException e) {
			return;
		}

		String transId = requestContext.getHeaderString(AAIHeaderProperties.TRANSACTION_ID);
		String fromAppId = requestContext.getHeaderString(AAIHeaderProperties.FROM_APP_ID);
		String fullUri = requestContext.getUriInfo().getRequestUri().toString();
		String requestTs = (String)requestContext.getProperty(AAIHeaderProperties.AAI_REQUEST_TS);

		String httpMethod = requestContext.getMethod();

		String status = Integer.toString(responseContext.getStatus());
		
		String request = (String)requestContext.getProperty(AAIHeaderProperties.AAI_REQUEST);
		String response = this.getResponseString(responseContext);

		if (!Boolean.parseBoolean(logValue)) {
		} else if (!Boolean.parseBoolean(getValue) && "GET".equals(httpMethod)) {
		} else if (!Boolean.parseBoolean(postValue) && "POST".equals(httpMethod)) {
		} else {
			
			JsonObject logEntry = new JsonObject();
			logEntry.addProperty("transactionId", transId);
			logEntry.addProperty("status", status);
			logEntry.addProperty("rqstDate", requestTs);
			logEntry.addProperty("respDate", this.genDate());
			logEntry.addProperty("sourceId", fromAppId + ":" + transId);
			logEntry.addProperty("resourceId", fullUri);
			logEntry.addProperty("resourceType", httpMethod);
			logEntry.addProperty("rqstBuf", Objects.toString(request, ""));
			logEntry.addProperty("respBuf", Objects.toString(response, ""));
			
			try {
				TRANSACTION_LOGGER.debug(logEntry.toString());
			} catch (Exception e) {
				ErrorLogHelper.logError("AAI_4000", "Exception writing transaction log.");
			}
		}

	}

	private String getResponseString(ContainerResponseContext responseContext) {
		JsonObject response = new JsonObject();
		response.addProperty("ID", responseContext.getHeaderString(AAIHeaderProperties.AAI_TX_ID));
		response.addProperty("Content-Type", this.httpServletResponse.getContentType());
		response.addProperty("Response-Code", responseContext.getStatus());
		response.addProperty("Headers", responseContext.getHeaders().toString());
		Optional<Object> entityOptional = Optional.ofNullable(responseContext.getEntity());
		if(entityOptional.isPresent()){
			response.addProperty("Entity", entityOptional.get().toString());
		} else {
			response.addProperty("Entity", "");
		}
		return response.toString();
	}

}
