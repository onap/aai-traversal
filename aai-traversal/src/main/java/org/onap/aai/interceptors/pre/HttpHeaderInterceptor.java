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

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;

import org.onap.aai.interceptors.AAIContainerFilter;
import org.onap.aai.interceptors.AAIHeaderProperties;

/**
 * The Class HttpHeaderInterceptor
 */
@PreMatching
@Priority(AAIRequestFilterPriority.HTTP_HEADER)
public class HttpHeaderInterceptor extends AAIContainerFilter implements ContainerRequestFilter {
    public static final String patchMethod = "PATCH";

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String overrideMethod =
            containerRequestContext.getHeaderString(AAIHeaderProperties.HTTP_METHOD_OVERRIDE);
        String httpMethod = containerRequestContext.getMethod();

        if (HttpMethod.POST.equalsIgnoreCase(httpMethod)
            && patchMethod.equalsIgnoreCase(overrideMethod)) {
            containerRequestContext.setMethod(patchMethod);
        }
    }

}
