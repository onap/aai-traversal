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

package org.openecomp.aai.rest.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.springframework.web.util.UriUtils;

/**
 * The Class ValidateEncoding.
 */
public class ValidateEncoding {

	private final String encoding = "UTF-8";
	
	/**
	 * Instantiates a new validate encoding.
	 */
	private ValidateEncoding() {
		
	}
	
	/**
	 * The Class Helper.
	 */
	private static class Helper {
		
		/** The Constant INSTANCE. */
		private static final ValidateEncoding INSTANCE = new ValidateEncoding();
	}
	
	/**
	 * Gets the single instance of ValidateEncoding.
	 *
	 * @return single instance of ValidateEncoding
	 */
	public static ValidateEncoding getInstance() {
		return Helper.INSTANCE;
	}	
	
	/**
	 * Validate.
	 *
	 * @param uri the uri
	 * @return true, if successful
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public boolean validate(URI uri) throws UnsupportedEncodingException {
		boolean result = true;
		if (!validatePath(uri.getRawPath())) {
			result = false;
		}
		/*if (!validateQueryParams(uri.getRawQuery())) {
			result = false;
		} //TODO
		*/
		
		return result;
	}
	
	/**
	 * Validate.
	 *
	 * @param info the info
	 * @return true, if successful
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public boolean validate(UriInfo info) throws UnsupportedEncodingException {
		boolean result = true;
		if (!validatePath(info.getPath(false))) {
			result = false;
		}
		if (!validateQueryParams(info.getQueryParameters(false))) {
			result = false;
		}
		
		return result;
	}
	
	/**
	 * Validate path.
	 *
	 * @param path the path
	 * @return true, if successful
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	private boolean validatePath(String path) throws UnsupportedEncodingException {
		String[] segments = path.split("/");
		boolean valid = true;
		for (String segment : segments) {
			if (!this.checkEncoding(segment)) {
				valid = false;
			}
		}
		
		return valid;
		
	}
	
	/**
	 * Validate query params.
	 *
	 * @param params the params
	 * @return true, if successful
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	private boolean validateQueryParams(MultivaluedMap<String, String> params) throws UnsupportedEncodingException {
		boolean valid = true;
		
		for (String key : params.keySet()) {
			if (!this.checkEncoding(key)) {
				valid = false;
			}
			for (String item : params.get(key)) {
				if (!this.checkEncoding(item)) {
					valid = false;
				}
			}
		}
		return valid;
	}
	
	/**
	 * Check encoding.
	 *
	 * @param segment the segment
	 * @return true, if successful
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	private boolean checkEncoding(String segment) throws UnsupportedEncodingException {
		boolean result = false;
		String decode = UriUtils.decode(segment, encoding);
		String encode = UriUtils.encode(decode, encoding);
		result = segment.equals(encode);
		
		return result;
	}
}
