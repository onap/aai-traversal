/*-
 * ============LICENSE_START=======================================================
 * org.onap.aai
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

package org.onap.aai.interceptors.pre;

import java.io.IOException;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Priority;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;

import org.onap.aai.interceptors.AAIContainerFilter;
import org.onap.aai.interceptors.AAIHeaderProperties;
import org.springframework.beans.factory.annotation.Autowired;

@PreMatching
@Priority(AAIRequestFilterPriority.HEADER_MANIPULATION)
public class RequestHeaderManipulation extends AAIContainerFilter implements ContainerRequestFilter {

	@Autowired
	private HttpServletRequest httpServletRequest;

	private static final Pattern versionedEndpoint = Pattern.compile("^/aai/(v\\d+)");
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		String uri = httpServletRequest.getRequestURI();
		this.addRequestContext(uri, requestContext.getHeaders());

	}
	
	private void addRequestContext(String uri, MultivaluedMap<String, String> requestHeaders) {

		String rc = "";

        Matcher match = versionedEndpoint.matcher(uri);
        if (match.find()) {
            rc = match.group(1);
        }

		if (requestHeaders.containsKey(AAIHeaderProperties.REQUEST_CONTEXT)) {
			requestHeaders.remove(AAIHeaderProperties.REQUEST_CONTEXT);
		}
		requestHeaders.put(AAIHeaderProperties.REQUEST_CONTEXT, Collections.singletonList(rc));
	}

}
