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

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;

/**
 * Responsible for dealing with uri that doesn't start with basePath
 * All of the other interceptors will handle any uri that starts with basePath
 * So we need this to ensure that these cases are properly handled
 */
@Order(1)
@Component
public class ErrorHandler extends OncePerRequestFilter {

    private String basePath;

    public ErrorHandler(@Value("${schema.uri.base.path}") String basePath){
        this.basePath = basePath;
        if(!basePath.endsWith("/")){
            this.basePath = basePath + "/";
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        String uri = httpServletRequest.getRequestURI();

        if (uri != null && !(uri.startsWith(basePath))) {

            AAIException e = new AAIException("AAI_3012");
            ArrayList<String> templateVars = new ArrayList<>();

            List<MediaType> mediaTypeList = new ArrayList<>();

            String acceptHeader = httpServletRequest.getHeader("Accept");
            if (acceptHeader == null) {
                mediaTypeList.add(MediaType.APPLICATION_XML_TYPE);
            } else {
                mediaTypeList.add(MediaType.valueOf(acceptHeader));
            }

            String message = ErrorLogHelper.getRESTAPIErrorResponse(mediaTypeList, e, templateVars);

            httpServletResponse.setStatus(400);
            httpServletResponse.setContentType(mediaTypeList.get(0).toString());
            httpServletResponse.getWriter().print(message);
            httpServletResponse.getWriter().close();
            return;
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}
