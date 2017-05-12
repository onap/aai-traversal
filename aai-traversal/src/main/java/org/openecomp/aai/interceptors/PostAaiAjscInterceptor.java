/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
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

package org.openecomp.aai.interceptors;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openecomp.aai.logging.LoggingContext;
import org.openecomp.aai.logging.LoggingContext.StatusCode;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import ajsc.beans.interceptors.AjscInterceptor;

public class PostAaiAjscInterceptor implements AjscInterceptor {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(PostAaiAjscInterceptor.class);

	private static class LazyAaiAjscInterceptor {
    	public static final PostAaiAjscInterceptor INSTANCE = new PostAaiAjscInterceptor();
	}

    public static PostAaiAjscInterceptor getInstance() {
    	return LazyAaiAjscInterceptor.INSTANCE;
    }

	@Override
	public boolean allowOrReject(HttpServletRequest req, HttpServletResponse resp, Map<?, ?> paramMap)
			throws Exception {
		final String responseCode = LoggingContext.responseCode();

		if (responseCode != null && responseCode.startsWith("ERR.")) {
			LoggingContext.statusCode(StatusCode.ERROR);
			LOGGER.error(req.getRequestURL() + " call failed with responseCode=" + responseCode);
		} else {
			LoggingContext.statusCode(StatusCode.COMPLETE);
			LOGGER.info(req.getRequestURL() + " call succeeded");
		}

		LoggingContext.clear();
		return true;
	}
}
