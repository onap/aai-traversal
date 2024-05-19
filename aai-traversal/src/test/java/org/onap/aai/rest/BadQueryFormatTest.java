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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.HttpTestUtil;
import org.onap.aai.PayloadUtil;
import org.onap.aai.TraversalApp;
import org.onap.aai.TraversalTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = TraversalApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Import(TraversalTestConfiguration.class)
public class BadQueryFormatTest {

    private HttpTestUtil httpTestUtil;

    private String pserverUri;

    private String hostname;

    @Autowired
    RestTemplate restTemplate;

    @LocalServerPort
    int randomPort;

    private HttpEntity httpEntity;

    private HttpHeaders headers;

    private String baseUrl;

    @Before
    public void setup() throws Exception {

        headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");
        String authorization = Base64.getEncoder().encodeToString("AAI:AAI".getBytes("UTF-8"));
        headers.add("Authorization", "Basic " + authorization);

        baseUrl = "http://localhost:" + randomPort;
    }

    @Test
    public void testPserverCount() throws Exception {
        Map<String, String> gremlinQueryMap = new HashMap<>();
        gremlinQueryMap.put("gremlin-query", "g.V().has('hostname', '" + hostname + "').count()");

        String payload = PayloadUtil.getTemplatePayload("gremlin-query.json", gremlinQueryMap);

        ResponseEntity responseEntity = null;

        String endpoint = "/aai/v11/query?format=hello";

        httpEntity = new HttpEntity(payload, headers);
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);

        System.out.println(responseEntity.getBody());
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(responseEntity.getBody().toString(), containsString(
            "Bad Parameter Passed:Unsupported format query parameter hello in request"));
    }

}
