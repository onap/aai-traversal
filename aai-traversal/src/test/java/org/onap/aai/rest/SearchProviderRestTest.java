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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

public class SearchProviderRestTest extends AbstractSpringRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchProviderRestTest.class);

    @Test
    public void testNodesQueryInvalidData() {

        String endpoint = "/aai/latest/search/nodes-query";

        httpEntity = new HttpEntity(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint)
            .queryParam("key", "cloud-region.cloud-owner:test-aic")
            .queryParam("include", "cloud-region");

        ResponseEntity responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);

        LOGGER.debug("Response for GET request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());

        assertNotNull("Response from /aai/latest/search is null", responseEntity);
        assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST,
            responseEntity.getStatusCode());
    }

    @Test
    public void testGenericQueryInvalidData() {

        String endpoint = "/aai/latest/search/generic-query";

        httpEntity = new HttpEntity(headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint)
            .queryParam("key", "cloud-region.cloud-owner:test-aic")
            .queryParam("include", "cloud-region");

        ResponseEntity responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);

        LOGGER.debug("Response for GET request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());

        assertNotNull("Response from /aai/latest/search is null", responseEntity);
        assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST,
            responseEntity.getStatusCode());
    }

    @Test
    public void testGenericQueryBypassTimeout() {
        headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Real-Time", "true");
        headers.add("X-TransactionId", "JUNIT");

        httpEntity = new HttpEntity(headers);
        String endpoint = "/aai/latest/search/generic-query";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint)
            .queryParam("key", "cloud-region.cloud-owner:test-aic")
            .queryParam("include", "cloud-region").queryParam("start-node-type", "cloud-region");

        ResponseEntity responseEntity =
            restTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String.class);

        LOGGER.debug("Response for GET request with uri {} : {}", builder.toUriString(),
            responseEntity.getBody());

        assertNotNull("Response from /aai/latest/search is null", responseEntity);
        assertEquals("Expected the response to be 400", HttpStatus.BAD_REQUEST,
            responseEntity.getStatusCode());
        assertThat(responseEntity.getBody().toString(), containsString("4009"));
    }
}
