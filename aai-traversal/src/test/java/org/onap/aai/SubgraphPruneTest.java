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
package org.onap.aai;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.*;

import jakarta.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
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
@EnableAutoConfiguration(exclude={CassandraDataAutoConfiguration.class, CassandraAutoConfiguration.class}) // there is no running cassandra instance for the test
@Import(TraversalTestConfiguration.class)
public class SubgraphPruneTest {

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

    private String vserverId;

    @Before
    public void setup() throws Exception {

        httpTestUtil = new HttpTestUtil();

        hostname = "test-" + UUID.randomUUID().toString();
        pserverUri = "/aai/v11/cloud-infrastructure/pservers/pserver/" + hostname;
        Map<String, String> pserverMap = new HashMap<>();
        pserverMap.put("hostname", hostname);
        String payload = PayloadUtil.getTemplatePayload("pserver.json", pserverMap);
        httpTestUtil.doPut(pserverUri, payload);

        Map<String, String> cloudRegionMap = new HashMap<>();

        vserverId = "some-vserver-id-id1111";

        cloudRegionMap.put("cloud-owner", "some-owner-id1111");
        cloudRegionMap.put("cloud-region-id", "some-region-id1111");
        cloudRegionMap.put("tenant-id", "some-tenant-id1111");
        cloudRegionMap.put("tenant-name", "some-tenant-name-id1111");
        cloudRegionMap.put("vserver-id", vserverId);
        cloudRegionMap.put("vserver-name", "some-vserver-name-id1111");
        cloudRegionMap.put("pserver-uri", pserverUri);

        String cloudRegionPayload =
            PayloadUtil.getTemplatePayload("cloud-region-with-vserver.json", cloudRegionMap);
        String cloudRegionUri =
            "/aai/v11/cloud-infrastructure/cloud-regions/cloud-region/some-owner-id1111/some-region-id1111";

        Response response = httpTestUtil.doPut(cloudRegionUri, cloudRegionPayload);

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
    public void testSubgraphPruneWorksAsExpectedWithValidResults() throws Exception {

        Map<String, String> gremlinQueryMap = new HashMap<>();
        // Having the cap('x') here causes the subgraph to fail
        gremlinQueryMap.put("gremlin-query", "g.V().has('vserver-id', '" + vserverId
            + "').store('x').out().has('aai-node-type', 'pserver').store('x').cap('x').unfold()");

        String payload = PayloadUtil.getTemplatePayload("gremlin-query.json", gremlinQueryMap);
        String endpoint = "/aai/v13/query?format=console&subgraph=prune";

        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity = null;
        responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        System.out.println(responseEntity.getBody().toString());
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
    }

}
