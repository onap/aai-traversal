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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.PayloadUtil;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.util.AAIConfig;
import org.onap.aai.util.TraversalConstants;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.jayway.jsonpath.JsonPath;

public class DslConsumerTest extends AbstractSpringRestTest {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(DslConsumerTest.class);

	@Override
	public void createTestGraph() {
		JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
		boolean success = true;
		try {
			GraphTraversalSource g = transaction.traversal();
			g.addV().property("aai-node-type", "pserver").property("hostname", "test-pserver-dsl")
					.property("in-maint", false).property("source-of-truth", "JUNIT")
					.property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver").next();
		} catch (Exception ex) {
			success = false;
		} finally {
			if (success) {
				transaction.commit();
			} else {
				transaction.rollback();
				fail("Unable to setup the graph");
			}
		}
	}

	@Test
	public void testDslQuery() throws Exception {

		String endpoint = "/aai/v14/dsl?format=console";
		Map<String, String> dslQueryMap = new HashMap<>();
		dslQueryMap.put("dsl-query", "pserver*('hostname','test-pserver-dsl')");
		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity,
				String.class);
		LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint, responseEntity.getBody());
		System.out.println(responseEntity.getBody());
		assertNotNull("Response from /aai/v14/dsl is not null", responseEntity);
		assertEquals("Expected the response to be 500", HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void testDslQueryException() throws Exception {
		Map<String, String> dslQuerymap = new HashMap<>();
		dslQuerymap.put("dsl-query", "xserver");

		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQuerymap);

		ResponseEntity responseEntity = null;

		String endpoint = "/aai/v11/dsl?format=console";

		httpEntity = new HttpEntity(payload, headers);
		responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
		assertEquals("Expected the response to be 400", HttpStatus.INTERNAL_SERVER_ERROR,
				responseEntity.getStatusCode());
	}
	
	@Test
	public void testDslQueryOverride() throws Exception {
		Map<String, String> dslQuerymap = new HashMap<>();
		dslQuerymap.put("dsl-query", "pserver*");

		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQuerymap);

		ResponseEntity responseEntity = null;

		String endpoint = "/aai/v11/dsl?format=console";

		headers.add("X-DslOverride", AAIConfig.get(TraversalConstants.DSL_OVERRIDE));
		httpEntity = new HttpEntity(payload, headers);
		responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
		assertEquals("Expected the response to be 404", HttpStatus.NOT_FOUND,
				responseEntity.getStatusCode());
	}

}
