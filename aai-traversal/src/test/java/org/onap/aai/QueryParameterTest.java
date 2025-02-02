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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
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
public class QueryParameterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryParameterTest.class);

    private HttpTestUtil httpTestUtil;

    private String configurationId;

    private String configurationUri;

    private String configurationId2;

    private String configurationUri2;

    @Autowired
    RestTemplate restTemplate;

    @LocalServerPort
    int randomPort;

    private HttpEntity httpEntity;

    private HttpHeaders headers;

    private String baseUrl;
    private String customerId;
    private String customerUri;
    private String customerId2;
    private String customerUri2;

    private String vnfId;
    private String vnfUri;

    private String vnfId2;
    private String vnfUri2;

    private String serviceInstanceId;
    private String serviceInstanceName;
    private String serviceInstanceId2;
    private String serviceInstanceName2;

    @Before
    public void setup() throws Exception {

        httpTestUtil = new HttpTestUtil();

        configurationId = "test-" + UUID.randomUUID().toString();
        configurationUri = "/aai/v13/network/configurations/configuration/" + configurationId;
        Map<String, String> configurationMap = new HashMap<>();
        configurationMap.put("configuration-id", configurationId);
        String payload = PayloadUtil.getTemplatePayload("configuration.json", configurationMap);
        httpTestUtil.doPut(configurationUri, payload);

        configurationId2 = "test-" + UUID.randomUUID().toString();
        configurationUri2 = "/aai/v13/network/configurations/configuration/" + configurationId2;
        configurationMap.put("configuration-id", configurationId2);
        payload = PayloadUtil.getTemplatePayload("configuration.json", configurationMap);
        httpTestUtil.doPut(configurationUri2, payload);

        customerId = "test-" + UUID.randomUUID().toString();
        serviceInstanceId = "test-service-instance1";
        serviceInstanceName = "test service instance1";

        customerUri = "/aai/v13/business/network/customers/customer/" + customerId;
        Map<String, String> customerMap = new HashMap<>();
        customerMap.put("customer-id", customerId);
        customerMap.put("service-instance-id", serviceInstanceId);
        customerMap.put("configuration-id1", configurationId);
        customerMap.put("configuration-id2", configurationId2);
        customerMap.put("service-instance-name", serviceInstanceName);
        payload = PayloadUtil.getTemplatePayload("customer-with-configurations.json", customerMap);
        httpTestUtil.doPut(customerUri, payload);

        customerId2 = "test-" + UUID.randomUUID().toString();
        serviceInstanceId2 = "test-service-instance2";
        serviceInstanceName2 = "test service instance1";
        customerUri2 = "/aai/v13/business/customers/customer/" + customerId2;

        customerMap = new HashMap<>();
        customerMap.put("customer-id", customerId2);
        customerMap.put("service-instance-id", serviceInstanceId2);
        customerMap.put("service-instance-name", serviceInstanceName2);
        payload = PayloadUtil.getTemplatePayload("customer-with-serviceinstance.json", customerMap);
        httpTestUtil.doPut(customerUri2, payload);

        vnfId = "test-" + UUID.randomUUID().toString();
        vnfUri = "/aai/v13/network/generic-vnfs/generic-vnf/" + vnfId;
        Map<String, String> vnfMap = new HashMap<>();
        vnfMap.put("vnf-id", vnfId);
        vnfMap.put("configuration-id", configurationId);
        vnfMap.put("interface-name", "test-interface-name1");
        vnfMap.put("vlan-interface", "test-vlan-name1");
        payload = PayloadUtil.getTemplatePayload("generic-vnf-to-configuration.json", vnfMap);
        httpTestUtil.doPut(vnfUri, payload);

        vnfId2 = "test-" + UUID.randomUUID().toString();
        vnfUri2 = "/aai/v13/network/generic-vnfs/generic-vnf/" + vnfId2;
        vnfMap = new HashMap<>();
        vnfMap.put("vnf-id", vnfId2);
        vnfMap.put("configuration-id", configurationId2);
        vnfMap.put("interface-name", "test-interface-name2");
        vnfMap.put("vlan-interface", "test-vlan-name2");
        payload = PayloadUtil.getTemplatePayload("generic-vnf-to-configuration.json", vnfMap);
        httpTestUtil.doPut(vnfUri2, payload);

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
    public void testQueryApi() throws Exception {

        String endpoint = "/aai/v13/query?format=pathed";

        Map<String, String> customQueryMap = new HashMap<>();
        String service = serviceInstanceName.replaceAll(" ", "+");
        System.out.println("Service " + service);
        customQueryMap.put("start", "nodes/service-instances?service-instance-name=" + service);
        customQueryMap.put("query", "containment-path");

        String payload = PayloadUtil.getTemplatePayload("custom-query.json", customQueryMap);
        httpEntity = new HttpEntity(payload, headers);
        ResponseEntity responseEntity =
            restTemplate.exchange(baseUrl + endpoint, HttpMethod.PUT, httpEntity, String.class);
        LOGGER.info("Response of custom query : {}", responseEntity.getBody().toString());
        assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
        assertThat(responseEntity.getBody().toString(), containsString(customerUri2));
    }

}
