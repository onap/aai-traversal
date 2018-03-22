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
package org.onap.aai;

import com.jayway.jsonpath.JsonPath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.config.PropertyPasswordConfiguration;
import org.onap.aai.exceptions.AAIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * A sample junit test using spring boot that provides the ability to spin
 * up the application from the junit layer and run rest requests against
 * SpringBootTest annotation with web environment requires which spring boot
 * class to load and the random port starts the application on a random port
 * and injects back into the application for the field with annotation LocalServerPort
 * <p>
 *
 * This can be used to potentially replace a lot of the fitnesse tests since
 * they will be testing against the same thing except fitnesse uses hbase
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TraversalApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(initializers = PropertyPasswordConfiguration.class)
@Import(TraversalTestConfiguration.class)
public class AAIGremlinQueryTest {

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

        httpTestUtil = new HttpTestUtil();

        hostname = UUID.randomUUID().toString();

        pserverUri ="/aai/v11/cloud-infrastructure/pservers/pserver/" + hostname;

        Map<String, String> pserverMap = new HashMap<>();
        pserverMap.put("hostname", hostname);
        String payload = PayloadUtil.getTemplatePayload("pserver.json", pserverMap);
        httpTestUtil.doPut(pserverUri, payload);

        headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");

        baseUrl = "https://localhost:" + randomPort;
    }

    @Test
    public void testPserverCount() throws Exception {
        Map<String, String> gremlinQueryMap = new HashMap<>();
        gremlinQueryMap.put("gremlin-query", "g.V().has('hostname', '" + hostname + "').count()");

        String payload = PayloadUtil.getTemplatePayload("gremlin-query.json", gremlinQueryMap);

        ResponseEntity responseEntity = null;

        String endpoint = "/aai/v11/query?format=console";

        httpEntity = new HttpEntity(payload, headers);
        responseEntity = restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

        String result = JsonPath.read(responseEntity.getBody().toString(), "$.results[0].result");
        assertThat(result, is("1"));
    }

    @After
    public void tearDown() throws UnsupportedEncodingException, AAIException {

        Response response = httpTestUtil.doGet(pserverUri);

        assertNotNull("Expected the response to be returned", response);
        assertThat(response.getStatus(), is(200));

        String body = response.getEntity().toString();
        String resourceVersion = JsonPath.read(body, "$.resource-version");

        response = httpTestUtil.doDelete(pserverUri, resourceVersion);
        assertNotNull("Expected the response to be returned", response);
        assertThat(response.getStatus(), is(204));
    }
}
