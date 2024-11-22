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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest;

import static org.junit.Assert.*;

import java.util.Collections;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.Test;
import org.onap.aai.dbmap.AAIGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

public class RecentApiTest extends AbstractSpringRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecentApiTest.class);

    @Override
    public void createTestGraph() {
        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        boolean success = true;
        try {
            GraphTraversalSource g = transaction.traversal();
            g.addV().property("aai-node-type", "pserver")
                .property("hostname", "test-pserver-recents").property("in-maint", false)
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver-recents")
                .next();
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
    public void testRecentsQuery() {

        String endpoint = "/aai/recents/v14/pserver";
        httpEntity = new HttpEntity(headers);
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint).queryParam("hours", "190");
        ResponseEntity responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);
        LOGGER.debug("Response for GET request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());
        assertNotNull("Response from /aai/recents/v14/pserver is not null", responseEntity);
        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());

        // Check different application xml headers for accept
        headers.set("Accept", "application/xml");
        httpEntity = new HttpEntity(headers);

        responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);
        LOGGER.debug("Response for GET request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());
        assertNotNull("Response from /aai/recents/v14/pserver is not null", responseEntity);
        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());

        headers.set("Accept", "application/xml; charset=UTF-8");
        httpEntity = new HttpEntity(headers);

        responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);
        LOGGER.debug("Response for GET request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());
        assertNotNull("Response from /aai/recents/v14/pserver is not null", responseEntity);
        assertEquals("Expected the response to be 200", HttpStatus.OK,
            responseEntity.getStatusCode());
    }

    @Test
    public void testRecentsHoursWrongNumber() {
        String endpoint = "/aai/recents/v14/pserver";
        httpEntity = new HttpEntity(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint)
            .queryParam("hours", "1900000000000000000000000000000000000000000000000");
        ResponseEntity responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);

        LOGGER.debug("Response for PUT request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());
        assertNotNull("Response from /aai/recents/v14/pserver is null", responseEntity);
        assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST,
            responseEntity.getStatusCode());
    }

    @Test
    public void testRecentsStartTimeWrongNumber() {
        String endpoint = "/aai/recents/v14/pserver";
        httpEntity = new HttpEntity(headers);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint)
            .queryParam("date-time", "190000000000000000000000000000000000000000000");
        ResponseEntity responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);

        LOGGER.debug("Response for PUT request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());
        assertNotNull("Response from /aai/recents/v14/pserver is null", responseEntity);
        assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST,
            responseEntity.getStatusCode());
    }

    @Test
    public void testRecentsQueryException() {
        String endpoint = "/aai/recents/v14/xserver";
        httpEntity = new HttpEntity(headers);
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint).queryParam("hours", "190");
        ResponseEntity responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);

        LOGGER.debug("Response for PUT request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());
        assertNotNull("Response from /aai/recents/v14/pserver is null", responseEntity);
        assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST,
            responseEntity.getStatusCode());
    }

    @Test
    public void testRecentsQueryExceptionHours() {
        String endpoint = "/aai/recents/v14/pserver";
        httpEntity = new HttpEntity(headers);
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint).queryParam("hours", "200");

        ResponseEntity responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);
        LOGGER.debug("Response for PUT request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());
        assertNotNull("Response from /aai/recents/v14/pserver is null", responseEntity);
        assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST,
            responseEntity.getStatusCode());

    }

    @Test
    public void testRecentsQueryExceptionDateTime() {
        String endpoint = "/aai/recents/v14/pserver";
        httpEntity = new HttpEntity(headers);
        UriComponentsBuilder builder =
            UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint).queryParam("date-time", "200");

        ResponseEntity responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);
        LOGGER.debug("Response for PUT request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());
        assertNotNull("Response from /aai/recents/v14/pserver is null", responseEntity);
        assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST,
            responseEntity.getStatusCode());
    }
}
