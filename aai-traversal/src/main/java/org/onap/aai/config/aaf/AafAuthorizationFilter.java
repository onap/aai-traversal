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
package org.onap.aai.config.aaf;

import org.onap.aai.Profiles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.filter.OrderedRequestContextFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

import static org.onap.aai.config.aaf.ResponseFormatter.errorResponse;

/**
 * AAF authorization filter
 */

@Component
@Profile(Profiles.AAF_AUTHENTICATION)
@PropertySource("file:${server.local.startpath}/aaf/permissions.properties")
public class AafAuthorizationFilter extends OrderedRequestContextFilter {

    private static final String ADVANCED = "advanced";
    private static final String BASIC = "basic";

    @Value("${permission.type}")
    String type;

    @Value("${permission.instance}")
    String instance;

    public AafAuthorizationFilter() {
        this.setOrder(FilterPriority.AAF_AUTHORIZATION.getPriority());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        if(request.getRequestURI().matches("^.*/util/echo$")){
            filterChain.doFilter(request, response);
        }

        boolean containsWordGremlin = request.getReader().lines().collect(Collectors.joining(System.lineSeparator())).contains("\"gremlin\"");
        //if the request contains the word "gremlin" it's an advanced query
        String queryType = containsWordGremlin ? ADVANCED : BASIC;
        String permission = String.format("%s|%s|%s", type, instance, queryType);

        if(!request.isUserInRole(permission)){
            errorResponse(request, response);
        }else{
            filterChain.doFilter(request,response);
        }
    }
}
