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
package org.onap.aai.util;

public final class TraversalConstants {
	public static final int AAI_QUERY_PORT = 8446;
	
	public static final String AAI_TRAVERSAL_TIMEOUT_LIMIT = "aai.traversal.timeoutlimit";
	public static final String AAI_TRAVERSAL_TIMEOUT_ENABLED = "aai.traversal.timeoutenabled";
	public static final String AAI_TRAVERSAL_TIMEOUT_APP = "aai.traversal.timeout.appspecific";
	
	public static final String AAI_TRAVERSAL_DSL_TIMEOUT_LIMIT = "aai.traversal.dsl.timeoutlimit";
	public static final String AAI_TRAVERSAL_DSL_TIMEOUT_ENABLED = "aai.traversal.dsl.timeoutenabled";
	public static final String AAI_TRAVERSAL_DSL_TIMEOUT_APP = "aai.traversal.dsl.timeout.appspecific";
	public static final String DSL_NOVALIDATION_CLIENTS = "aai.traversal.dsl.novalidation.clients";
	public static final String DSL_OVERRIDE = "aai.dsl.override";
	
    public static final long HISTORY_MAX_HOURS = 192;
	
	private TraversalConstants() {
		// prevent instantiation
	}

}
