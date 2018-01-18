/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.interceptors;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aai.logging.LoggingContext;

import ajsc.beans.interceptors.AjscInterceptor;

public class PreAaiAjscInterceptor implements AjscInterceptor {
	private final static String TARGET_ENTITY = "aai-traversal";
	
	private static class LazyAaiAjscInterceptor {
    	public static final PreAaiAjscInterceptor INSTANCE = new PreAaiAjscInterceptor();
	}

    public static PreAaiAjscInterceptor getInstance() {
    	return LazyAaiAjscInterceptor.INSTANCE;
    }
     
	@Override
	public boolean allowOrReject(HttpServletRequest req, HttpServletResponse resp, Map<?, ?> paramMap)
			throws Exception {

		LoggingContext.init();

		String serviceName = req.getMethod() + " " + req.getRequestURI().toString();
		LoggingContext.partnerName(req.getHeader("X-FromAppId"));
		String queryStr = req.getQueryString();
		if ( queryStr != null ) {
			serviceName = serviceName + "?" + queryStr;
		}
		LoggingContext.serviceName(serviceName);
		LoggingContext.targetEntity(TARGET_ENTITY);
		LoggingContext.targetServiceName("allowOrReject");
		LoggingContext.requestId(req.getHeader("X-TransactionId"));
		LoggingContext.successStatusFields();
		return true;
	}
}
