/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2026 Deutsche Telekom. All rights reserved.
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
import static org.junit.Assert.assertNotEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aai.TraversalApp;
import org.onap.aai.TraversalTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

// JUnit 4 like the other web tests here, not JUnit 5: a JUnit 5 variant of this class runs in the
// separate jupiter provider pass and builds its own application context, which leaves the shared
// in-memory graph in a state that makes AAIGremlinQueryTest and QueryParameterTest fail later.
@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = TraversalApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@EnableAutoConfiguration(exclude={CassandraDataAutoConfiguration.class, CassandraAutoConfiguration.class}) // there is no running cassandra instance for the test
@Import(TraversalTestConfiguration.class)
public class BasicAuthTest {

    private static final String SECURED_ENDPOINT = "/aai/v11/query?format=unsupported";
    private static final String PERMITTED_ENDPOINT = "/aai/util/echo";
    private static final String PAYLOAD = "{\"gremlin\":\"g.V().count()\"}";

    @Autowired
    RestTemplate restTemplate;

    @LocalServerPort
    int randomPort;

    private String baseUrl;

    @Before
    public void setup() {
        baseUrl = "http://localhost:" + randomPort;
    }

    @Test
    public void testRequestWithoutCredentialsIsRejected() {
        assertEquals(HttpStatus.UNAUTHORIZED, put(SECURED_ENDPOINT, null).getStatusCode());
    }

    @Test
    public void testRequestWithWrongPasswordIsRejected() {
        assertEquals(HttpStatus.UNAUTHORIZED,
            put(SECURED_ENDPOINT, credentials("AAI", "not-the-password")).getStatusCode());
    }

    @Test
    public void testRequestWithUnknownUserIsRejected() {
        assertEquals(HttpStatus.UNAUTHORIZED,
            put(SECURED_ENDPOINT, credentials("not-a-user", "AAI")).getStatusCode());
    }

    @Test
    public void testRequestWithConfiguredCredentialsIsAuthenticated() {
        // rejected for the unsupported format, which is only reached once authentication passed
        assertEquals(HttpStatus.BAD_REQUEST,
            put(SECURED_ENDPOINT, credentials("AAI", "AAI")).getStatusCode());
    }

    @Test
    public void testEchoEndpointNeedsNoCredentials() {
        assertNotEquals(HttpStatus.UNAUTHORIZED,
            exchange(PERMITTED_ENDPOINT, HttpMethod.GET, null, null).getStatusCode());
    }

    private HttpHeaders credentials(String username, String password) {
        HttpHeaders credentials = new HttpHeaders();
        credentials.setBasicAuth(username, password);
        return credentials;
    }

    private ResponseEntity<String> put(String endpoint, HttpHeaders credentials) {
        return exchange(endpoint, HttpMethod.PUT, PAYLOAD, credentials);
    }

    private ResponseEntity<String> exchange(String endpoint, HttpMethod method, String body,
        HttpHeaders credentials) {
        HttpHeaders headers = new HttpHeaders();
        if (credentials != null) {
            headers.addAll(credentials);
        }
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");

        return restTemplate.exchange(baseUrl + endpoint, method, new HttpEntity<>(body, headers),
            String.class);
    }
}
