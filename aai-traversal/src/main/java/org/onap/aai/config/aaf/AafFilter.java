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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.filter.CadiFilter;
import org.onap.aai.Profiles;
import org.onap.aai.TraversalApp;
import org.springframework.boot.web.filter.OrderedRequestContextFilter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

import static org.onap.aai.config.aaf.ResponseFormatter.errorResponse;

/**
 * AAF authentication filter
 */

@Component
@Profile(Profiles.AAF_AUTHENTICATION)
public class AafFilter extends OrderedRequestContextFilter {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(AafFilter.class.getName());

    private final CadiFilter cadiFilter;

    public AafFilter() throws IOException, ServletException {
        Properties cadiProperties = new Properties();
        cadiProperties.load(TraversalApp.class.getClassLoader().getResourceAsStream("cadi.properties"));
        cadiFilter = new CadiFilter(new PropAccess(cadiProperties));
        this.setOrder(FilterPriority.AAF_AUTHENTICATION.getPriority());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if(!request.getRequestURI().matches("^.*/util/echo$")) {
            cadiFilter.doFilter(request, response, filterChain);
            if (response.getStatus() == 401 || response.getStatus() == 403) {
                log.info("User does not have permissions to run the query" );
                errorResponse(request, response);
            }
        }
    }


}
