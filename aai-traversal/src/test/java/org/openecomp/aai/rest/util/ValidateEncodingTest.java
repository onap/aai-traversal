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

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.mockito.Mockito;

public class ValidateEncodingTest {

	
	@Test
	public void badPath() throws UnsupportedEncodingException {
		String badPath = "/aai/v6/network/vces/vce/blahh::blach/others/other/jklfea{}";
		UriInfo mockUriInfo = getMockUriInfo(badPath, new MultivaluedHashMap<String, String>());
		ValidateEncoding validator = ValidateEncoding.getInstance();
		
		assertEquals(false, validator.validate(mockUriInfo));
	}
	
	@Test
	public void goodPath() throws UnsupportedEncodingException {
		String goodPath = "/aai/v6/network/vces/vce/blahh%3A%3Ablach/others/other/jklfea%7B%7D";
		UriInfo mockUriInfo = getMockUriInfo(goodPath, new MultivaluedHashMap<String, String>());
		ValidateEncoding validator = ValidateEncoding.getInstance();
		
		assertEquals(true, validator.validate(mockUriInfo));	
	}
	
	@Test
	public void badQueryParamsKey() throws UnsupportedEncodingException {
		MultivaluedHashMap<String, String> map = new MultivaluedHashMap<String, String>();
		map.putSingle("blahblah", "test");
		map.putSingle("blahblah", "test2");
		map.putSingle("bad::bad", "test3");
		UriInfo mockUriInfo = getMockUriInfo("", map);

		ValidateEncoding validator = ValidateEncoding.getInstance();
		
		assertEquals(false, validator.validate(mockUriInfo));
		
	}
	@Test
	public void badQueryParamsValue() throws UnsupportedEncodingException {
		MultivaluedHashMap<String, String> map = new MultivaluedHashMap<String, String>();
		map.putSingle("blahblah", "test");
		map.putSingle("blahblah", "test//:2");
		map.putSingle("badbad", "test3");
		UriInfo mockUriInfo = getMockUriInfo("", map);

		ValidateEncoding validator = ValidateEncoding.getInstance();
		
		assertEquals(false, validator.validate(mockUriInfo));
	}
	@Test
	public void goodQueryParams() throws UnsupportedEncodingException {
		MultivaluedHashMap<String, String> map = new MultivaluedHashMap<String, String>();
		map.putSingle("blahblah", "test");
		map.putSingle("blahblah", "test2");
		map.putSingle("badbad", "~test%2F%2F%3A3");
		UriInfo mockUriInfo = getMockUriInfo("", map);

		ValidateEncoding validator = ValidateEncoding.getInstance();
		
		assertEquals(true, validator.validate(mockUriInfo));
	}
	
	private UriInfo getMockUriInfo(String path, MultivaluedMap<String, String> map) {
		UriInfo mockUriInfo = Mockito.mock(UriInfo.class);
		Mockito.when(mockUriInfo.getPath(false)).thenReturn(path);
		Mockito.when(mockUriInfo.getQueryParameters(false)).thenReturn(map);
		
		return mockUriInfo;
	}
	
}
