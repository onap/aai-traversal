package org.onap.aai.interceptors.post;

import java.io.IOException;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.onap.aai.interceptors.AAIContainerFilter;
import org.onap.aai.logging.LoggingContext;
import org.onap.aai.logging.LoggingContext.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

@Priority(AAIResponseFilterPriority.RESET_LOGGING_CONTEXT)
public class ResetLoggingContext extends AAIContainerFilter implements ContainerResponseFilter {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(ResetLoggingContext.class);

	@Autowired
	private HttpServletRequest httpServletRequest;
	
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		this.cleanLoggingContext();

	}

	private void cleanLoggingContext() {
		final String responseCode = LoggingContext.responseCode();
		String url = httpServletRequest.getRequestURL().toString();

		if (responseCode != null && responseCode.startsWith("ERR.")) {
			LoggingContext.statusCode(StatusCode.ERROR);
			LOGGER.error(url + " call failed with responseCode=" + responseCode);
		} else {
			LoggingContext.statusCode(StatusCode.COMPLETE);
			LOGGER.info(url + " call succeeded");
		}

		LoggingContext.clear();
	}

}
