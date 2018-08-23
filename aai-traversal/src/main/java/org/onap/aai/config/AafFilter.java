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
package org.onap.aai.config;

import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.filter.CadiFilter;
import org.onap.aai.Profiles;
import org.onap.aai.TraversalApp;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

/**
 * AAF authentication filter
 */

@Order(1)
@Component
@Profile(Profiles.AAF_AUTHENTICATION)
public class AafFilter extends OncePerRequestFilter {

    private static final String ACCEPT_HEADER = "accept";
    private final CadiFilter cadiFilter;

    public AafFilter() throws IOException, ServletException {
        Properties cadiProperties = new Properties();
        cadiProperties.load(TraversalApp.class.getClassLoader().getResourceAsStream("cadi.properties"));
        cadiFilter = new CadiFilter(new PropAccess(cadiProperties));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        cadiFilter.doFilter(request, response, filterChain);
        if(response.getStatus() >=400 && response.getStatus() < 500){
                errorResponse(request, response);
        }
    }

    private void errorResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accept = request.getHeader(ACCEPT_HEADER) == null ? MediaType.APPLICATION_XML : request.getHeader(ACCEPT_HEADER);
        AAIException aaie = new AAIException("AAI_3300");
        response.setStatus(aaie.getErrorObject().getHTTPResponseCode().getStatusCode());
        response.getWriter().write(ErrorLogHelper.getRESTAPIErrorResponse(Collections.singletonList(MediaType.valueOf(accept)), aaie, new ArrayList<>()));
        response.getWriter().flush();
        response.getWriter().close();
    }
}
