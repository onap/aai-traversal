/*
 * ============LICENSE_START=======================================================
 * org.onap.aai
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

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "aai.basic-auth.enabled=false")
public class BasicAuthDisabledTest extends AbstractSpringRestTest {

    @Test
    public void queryDoesNotRequireCredentialsWhenAuthenticationIsDisabled() {
        HttpHeaders anonymousHeaders = new HttpHeaders();
        anonymousHeaders.setContentType(MediaType.APPLICATION_JSON);
        anonymousHeaders.add("X-FromAppId", "JUNIT");
        anonymousHeaders.add("X-TransactionId", "JUNIT");
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/aai/v11/query?format=unsupported",
            HttpMethod.PUT, new HttpEntity<>("{\"gremlin\":\"g.V().count()\"}", anonymousHeaders), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void echoDoesNotRequireCredentialsWhenAuthenticationIsDisabled() {
        HttpHeaders anonymousHeaders = new HttpHeaders();
        anonymousHeaders.add("X-FromAppId", "JUNIT");
        anonymousHeaders.add("X-TransactionId", "JUNIT");
        assertEquals(HttpStatus.OK,
            restTemplate.exchange(baseUrl + "/aai/util/echo", HttpMethod.GET,
                new HttpEntity<>(anonymousHeaders), String.class).getStatusCode());
    }
}