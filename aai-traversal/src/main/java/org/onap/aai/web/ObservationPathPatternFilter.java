/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2026 Deutsche Telekom. All rights reserved.
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
package org.onap.aai.web;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import java.util.List;
import java.util.regex.Pattern;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.uri.UriTemplate;
import org.springframework.web.filter.ServerHttpObservationFilter;

/**
 * Hands the matched Jersey resource template to the {@code http.server.requests} observation.
 * Spring only learns the path pattern from Spring MVC handler mappings, so without this every
 * Jersey request is tagged {@code uri=UNKNOWN} and its span is named just {@code http <method>}.
 */
@Priority(Priorities.USER)
public class ObservationPathPatternFilter implements ContainerRequestFilter {

    // {version: v[1-9][0-9]*|latest} -> {version}, tolerating one level of braces in the regex
    private static final Pattern TEMPLATE_REGEX =
        Pattern.compile("\\{\\s*([^\\s:{}]+)\\s*:[^{}]*(?:\\{[^{}]*}[^{}]*)*}");
    private static final Pattern REPEATED_SLASHES = Pattern.compile("/{2,}");

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ServerHttpObservationFilter.findObservationContext(servletRequest)
            .ifPresent(observationContext -> observationContext
                .setPathPattern(pathPattern((ExtendedUriInfo) requestContext.getUriInfo())));
    }

    static String pathPattern(ExtendedUriInfo uriInfo) {
        List<UriTemplate> templates = uriInfo.getMatchedTemplates();
        StringBuilder pattern = new StringBuilder();
        for (int i = templates.size() - 1; i >= 0; i--) {
            pattern.append('/').append(templates.get(i).getTemplate());
        }
        String withoutRegex = TEMPLATE_REGEX.matcher(pattern).replaceAll("{$1}");
        return REPEATED_SLASHES.matcher(withoutRegex).replaceAll("/");
    }
}
