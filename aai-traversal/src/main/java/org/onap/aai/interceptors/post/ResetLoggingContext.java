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
