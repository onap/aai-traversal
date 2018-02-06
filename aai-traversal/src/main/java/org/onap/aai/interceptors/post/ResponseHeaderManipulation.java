package org.onap.aai.interceptors.post;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.onap.aai.interceptors.AAIContainerFilter;
import org.onap.aai.interceptors.AAIHeaderProperties;

@Priority(AAIResponseFilterPriority.HEADER_MANIPULATION)
public class ResponseHeaderManipulation extends AAIContainerFilter implements ContainerResponseFilter {


	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		updateResponseHeaders(requestContext, responseContext);

	}

	private void updateResponseHeaders(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) {
		responseContext.getHeaders().add(AAIHeaderProperties.AAI_TX_ID, requestContext.getProperty(AAIHeaderProperties.AAI_TX_ID));
	}

}
