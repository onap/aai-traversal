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
import org.onap.aai.logging.LoggingContext.StatusCode;
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
		final int httpStatusCode = resp.getStatus();
		LoggingContext.responseCode(Integer.toString(httpStatusCode));
		if ( httpStatusCode < 200 || httpStatusCode > 299 ) {
			LoggingContext.statusCode(StatusCode.ERROR);
			LoggingContext.responseDescription("Error");
			LOGGER.error(req.getRequestURL() + " call failed with responseCode=" + httpStatusCode);
		}
		else {
			LoggingContext.responseDescription(LoggingContext.responseMap.get(LoggingContext.SUCCESS));
			LoggingContext.statusCode(StatusCode.COMPLETE);
			LOGGER.info(req.getRequestURL() + " call succeeded");
		}
		LoggingContext.clear();
		return true;
	}
}
