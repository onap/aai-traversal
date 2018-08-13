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
package org.onap.aai.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class RecentApiTest extends AbstractSpringRestTest {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(RecentApiTest.class);

	@Test
	public void testRecentsQuery() {

		String endpoint = "/aai/recents/v14/pserver";
		httpEntity = new HttpEntity(headers);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint).queryParam("hours", "190");
		ResponseEntity responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity,
				String.class);
		LOGGER.debug("Response for PUT request with uri {} : {}", builder.toUriString(), responseEntity.getBody());
		assertNotNull("Response from /aai/recents/v14/pserver is null", responseEntity);
		assertEquals("Expected the response to be 400", HttpStatus.OK, responseEntity.getStatusCode());

	}

	@Test
	public void testRecentsQueryException() {
		String endpoint = "/aai/recents/v14/xserver";
		httpEntity = new HttpEntity(headers);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint).queryParam("hours", "190");
		ResponseEntity responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity,
				String.class);

		LOGGER.debug("Response for PUT request with uri {} : {}", builder.toUriString(), responseEntity.getBody());
		assertNotNull("Response from /aai/recents/v14/pserver is null", responseEntity);
		assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
	}

	@Test
	public void testRecentsQueryExceptionHours() {
		String endpoint = "/aai/recents/v14/pserver";
		httpEntity = new HttpEntity(headers);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint).queryParam("hours", "200");

		ResponseEntity responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity,
				String.class);
		LOGGER.debug("Response for PUT request with uri {} : {}", builder.toUriString(), responseEntity.getBody());
		assertNotNull("Response from /aai/recents/v14/pserver is null", responseEntity);
		assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST,
				responseEntity.getStatusCode());

	}

	@Test
	public void testRecentsQueryExceptionDateTime() {
		String endpoint = "/aai/recents/v14/pserver";
		httpEntity = new HttpEntity(headers);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint).queryParam("date-time",
				"200");

		ResponseEntity responseEntity = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity,
				String.class);
		LOGGER.debug("Response for PUT request with uri {} : {}", builder.toUriString(), responseEntity.getBody());
		assertNotNull("Response from /aai/recents/v14/pserver is null", responseEntity);
		assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST,
				responseEntity.getStatusCode());
	}
}
