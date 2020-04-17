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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.Test;
import org.junit.Ignore;
import org.onap.aai.HttpTestUtil;
import org.onap.aai.PayloadUtil;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.util.AAIConfig;
import org.onap.aai.util.TraversalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

public class DslConsumerTest extends AbstractSpringRestTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(DslConsumerTest.class);

	@Override
	public void createTestGraph() {
		JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
		boolean success = true;
		try {
			GraphTraversalSource g = transaction.traversal();
			g.addV().property("aai-node-type", "pserver").property("hostname", "test-pserver-dsl")
					.property("in-maint", false).property("source-of-truth", "JUNIT")
					.property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver-dsl").next();
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
		headers.add("X-Dsl-Version", "V1");
		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity,
				String.class);
		LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint, responseEntity.getBody());
		System.out.println(responseEntity.getBody());
		assertNotNull("Response from /aai/v14/dsl is not null", responseEntity);
		assertEquals("Expected the response to be 200", HttpStatus.OK, responseEntity.getStatusCode());

		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
		httpEntity = new HttpEntity(payload, headers);
		responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity,
			String.class);
		LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint, responseEntity.getBody());
		assertNotNull("Response from /aai/v14/dsl is not null", responseEntity);
		assertEquals("Expected the response to be 200", HttpStatus.OK, responseEntity.getStatusCode());

		// Make sure that there are no two result <result><result>
		assertThat(responseEntity.getBody().toString(), is(not(containsString("<result><result>"))));
		assertThat(responseEntity.getBody().toString(), is(containsString("<results><result>")));
	}

	@Test
	public void testDslQueryV2() throws Exception {

		String endpoint = "/aai/v14/dsl?format=console";
		Map<String, String> dslQueryMap = new HashMap<>();
		dslQueryMap.put("dsl-query", "pserver*('hostname','test-pserver-dsl') > complex*");
		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
		headers.add("X-Dsl-Version", "V2");
		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity,
				String.class);
		LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint, responseEntity.getBody());
		assertNotNull("Response from /aai/v14/dsl is not null", responseEntity);
		assertEquals("Expected the response to be 200", HttpStatus.OK, responseEntity.getStatusCode());

		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
		httpEntity = new HttpEntity(payload, headers);
		responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity,
			String.class);
		LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint, responseEntity.getBody());
		assertNotNull("Response from /aai/v14/dsl is not null", responseEntity);
		assertEquals("Expected the response to be 200", HttpStatus.OK, responseEntity.getStatusCode());

		// Make sure that there are no two result <result><result>
		assertThat(responseEntity.getBody().toString(), is(not(containsString("<result><result>"))));
		assertThat(responseEntity.getBody().toString(), is(containsString("<results><result>")));
	}

	@Test
	public void testDslQueryV2Aggregate() throws Exception {
		String endpoint = "/aai/v17/dsl?format=aggregate";
		Map<String, String> dslQueryMap = new HashMap<>();
		dslQueryMap.put("dsl-query", "pserver*('hostname','test-pserver-dsl')");
		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
		System.out.println("Payload" + payload);
		headers.add("X-Dsl-Version", "V2");
		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity,
				String.class);
		LOGGER.debug("Response for PUT request with uri {} : {}", baseUrl + endpoint, responseEntity.getBody());
		System.out.println(responseEntity.getBody());
		assertNotNull("Response from /aai/v17/dsl is not null", responseEntity);
		assertEquals("Expected the response to be 200", HttpStatus.OK, responseEntity.getStatusCode());
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
		assertEquals("Expected the response to be 404", HttpStatus.BAD_REQUEST,
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
		assertEquals("Expected the response to be 404", HttpStatus.BAD_REQUEST,
				responseEntity.getStatusCode());
	}


	@Test
	public void testSelectedPropertiesNotRequiredOnDSLStartNode() throws Exception {
		Map<String, String> dslQuerymap = new HashMap<>();
		dslQuerymap.put("dsl-query", "pserver*('equip-model','abc')");

		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQuerymap);

		String endpoint = "/aai/v11/dsl?format=console";

		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);

		assertEquals("Expected the response to be " + HttpStatus.OK, HttpStatus.OK,
				responseEntity.getStatusCode());
	}

	@Test
	public void testAPropertyIsRequiredOnDSLStartNode() throws Exception {
		Map<String, String> dslQuerymap = new HashMap<>();
		dslQuerymap.put("dsl-query", "pserver*");

		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQuerymap);

		String endpoint = "/aai/v11/dsl?format=console";

		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);

		assertEquals("Expected the response to be " + HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST,
				responseEntity.getStatusCode());
	}


	@Test
	public void testDslQueryProcessingV2_WithSimpleFormat_WithAsTreeQueryParameter() throws Exception {
		Map<String, String> dslQueryMap = new HashMap<>();
		dslQueryMap.put("dsl-query", "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");
		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
		String endpoint = "/aai/v16/dsl?format=simple&depth=0&nodesOnly=true&as-tree=true";

		// Add header with V2 to use the {} feature as a part of dsl query
		headers.add("X-DslApiVersion","V2");
		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
		String responseString = responseEntity.getBody().toString();

		// Extract the properties array from the response and compare in assert statements
		JsonParser jsonParser = new JsonParser();
		JsonObject results = jsonParser.parse(responseString).getAsJsonObject();
		JsonArray resultsArray = results.get("results").getAsJsonArray();
		JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
		JsonObject properties = resultsValue.get("properties").getAsJsonObject();
		assertEquals(2, properties.size());
		assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
		assertTrue(properties.get("in-maint").toString().equals("false"));
		headers.remove("X-DslApiVersion");
	}

	@Test
	public void testDslQueryProcessingV2_WithSimpleFormat_WithoutAsTreeQueryParameter() throws Exception {
		Map<String, String> dslQueryMap = new HashMap<>();
		dslQueryMap.put("dsl-query", "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");

		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
		String endpoint = "/aai/v16/dsl?format=simple&depth=0&nodesOnly=true";

		// Add header with V2 to use the {} feature as a part of dsl query
		headers.add("X-DslApiVersion","V2");
		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
		String responseString = responseEntity.getBody().toString();
		// Extract the properties array from the response and compare in assert statements
		JsonParser jsonParser = new JsonParser();
		JsonObject results = jsonParser.parse(responseString).getAsJsonObject();
		JsonArray resultsArray = results.get("results").getAsJsonArray();
		JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
		JsonObject properties = resultsValue.get("properties").getAsJsonObject();
		assertEquals(2, properties.size());
		assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
		assertTrue(properties.get("in-maint").toString().equals("false"));
		headers.remove("X-DslApiVersion");
	}

	@Test
	public void testDslQueryProcessingV2_WithResourceFormat_WithAsTreeQueryParameter() throws Exception {
		Map<String, String> dslQueryMap = new HashMap<>();
		dslQueryMap.put("dsl-query", "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");

		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
		String endpoint = "/aai/v16/dsl?format=resource&depth=0&nodesOnly=true&as-tree=true";

		// Add header with V2 to use the {} feature as a part of dsl query
		headers.add("X-DslApiVersion","V2");
		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
		String responseString = responseEntity.getBody().toString();

		// Extract the properties array from the response and compare in assert statements
		JsonParser jsonParser = new JsonParser();
		JsonObject results = jsonParser.parse(responseString).getAsJsonObject();
		JsonArray resultsArray = results.get("results").getAsJsonArray();
		JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
		JsonObject properties = resultsValue.get("pserver").getAsJsonObject();
		assertEquals(2, properties.size());
		assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
		assertTrue(properties.get("in-maint").toString().equals("false"));
		headers.remove("X-DslApiVersion");
	}

	@Ignore
	@Test
	public void testDslQueryProcessingV2_WithResourceFormat_WithoutAsTreeQueryParameter() throws Exception {
		Map<String, String> dslQueryMap = new HashMap<>();
		dslQueryMap.put("dsl-query", "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");

		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
		String endpoint = "/aai/v16/dsl?format=resource&depth=0&nodesOnly=true";

		// Add header with V2 to use the {} feature as a part of dsl query
		headers.add("X-DslApiVersion","V2");
		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
		String responseString = responseEntity.getBody().toString();

		// Extract the properties array from the response and compare in assert statements
		JsonParser jsonParser = new JsonParser();
		JsonObject results = jsonParser.parse(responseString).getAsJsonObject();
		JsonArray resultsArray = results.get("results").getAsJsonArray();
		JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
		JsonObject properties = resultsValue.get("pserver").getAsJsonObject();
		assertEquals(2, properties.size());
		assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
		assertTrue(properties.get("in-maint").toString().equals("false"));
		headers.remove("X-DslApiVersion");
	}

	@Test
	public void testDslQueryProcessingV2_WithResourceAndUrlFormat_WithAsTreeQueryParameter() throws Exception {
		Map<String, String> dslQueryMap = new HashMap<>();
		dslQueryMap.put("dsl-query", "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");

		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
		String endpoint = "/aai/v16/dsl?format=resource_and_url&depth=0&nodesOnly=true&as-tree=true";

		// Add header with V2 to use the {} feature as a part of dsl query
		headers.add("X-DslApiVersion","V2");
		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
		String responseString = responseEntity.getBody().toString();

		// Extract the properties array from the response and compare in assert statements
		JsonParser jsonParser = new JsonParser();
		JsonObject results = jsonParser.parse(responseString).getAsJsonObject();
		JsonArray resultsArray = results.get("results").getAsJsonArray();
		JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
		JsonObject properties = resultsValue.get("pserver").getAsJsonObject();
		assertEquals(2, properties.size());
		assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
		assertTrue(properties.get("in-maint").toString().equals("false"));
		headers.remove("X-DslApiVersion");
	}

	@Ignore
	@Test
	public void testDslQueryProcessingV2_WithResourceAndUrlFormat_WithoutAsTreeQueryParameter() throws Exception {
		Map<String, String> dslQueryMap = new HashMap<>();
		dslQueryMap.put("dsl-query", "pserver{'hostname', 'ptnii-equip-name', 'in-maint'}('hostname','test-pserver-dsl')");

		String payload = PayloadUtil.getTemplatePayload("dsl-query.json", dslQueryMap);
		String endpoint = "/aai/v16/dsl?format=resource_and_url&depth=0&nodesOnly=true";

		// Add header with V2 to use the {} feature as a part of dsl query
		headers.add("X-DslApiVersion","V2");
		httpEntity = new HttpEntity(payload, headers);
		ResponseEntity responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
		String responseString = responseEntity.getBody().toString();

		// Extract the properties array from the response and compare in assert statements
		JsonParser jsonParser = new JsonParser();
		JsonObject results = jsonParser.parse(responseString).getAsJsonObject();
		JsonArray resultsArray = results.get("results").getAsJsonArray();
		JsonObject resultsValue = resultsArray.get(0).getAsJsonObject();
		JsonObject properties = resultsValue.get("pserver").getAsJsonObject();
		assertEquals(2, properties.size());
		assertTrue(properties.get("hostname").toString().equals("\"test-pserver-dsl\""));
		assertTrue(properties.get("in-maint").toString().equals("false"));
		headers.remove("X-DslApiVersion");
	}

}
