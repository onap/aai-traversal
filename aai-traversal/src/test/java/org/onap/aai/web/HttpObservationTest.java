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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Collections;
import java.util.List;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.uri.UriTemplate;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

// Same context configuration as BasicAuthTest, so both share one cached application context.
@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = TraversalApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@EnableAutoConfiguration(exclude={CassandraDataAutoConfiguration.class, CassandraAutoConfiguration.class})
@Import(TraversalTestConfiguration.class)
public class HttpObservationTest {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    MeterRegistry meterRegistry;

    @LocalServerPort
    int randomPort;

    private String baseUrl;

    @Before
    public void setup() {
        baseUrl = "http://localhost:" + randomPort;
    }

    @Test
    public void testRequestIsTaggedWithMatchedJerseyTemplate() {
        HttpHeaders credentials = new HttpHeaders();
        credentials.setBasicAuth("AAI", "AAI");
        exchange("/aai/v11/query?format=unsupported", HttpMethod.PUT,
            "{\"gremlin\":\"g.V().count()\"}", credentials);

        assertNotNull(meterRegistry.find("http.server.requests")
            .tags("method", "PUT", "uri", "/{version}/query").timer());
    }

    @Test
    public void testSecurityObservationsAreDisabled() {
        exchange("/aai/util/echo", HttpMethod.GET, null, null);

        assertNotNull(meterRegistry.find("http.server.requests").tag("uri", "/util/echo").timer());
        assertTrue(meterRegistry.getMeters().stream()
            .map(Meter::getId)
            .noneMatch(id -> id.getName().startsWith("spring.security.")));
    }

    @Test
    public void testPathPatternStripsTemplateRegex() {
        ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);
        when(uriInfo.getMatchedTemplates()).thenReturn(List.of(
            new UriTemplate("/{nodeType: .+}"),
            new UriTemplate("/recents/{version: v[1-9][0-9]*|latest}")));

        assertEquals("/recents/{version}/{nodeType}",
            ObservationPathPatternFilter.pathPattern(uriInfo));
    }

    private void exchange(String endpoint, HttpMethod method, String body, HttpHeaders credentials) {
        HttpHeaders headers = new HttpHeaders();
        if (credentials != null) {
            headers.addAll(credentials);
        }
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");

        restTemplate.exchange(baseUrl + endpoint, method, new HttpEntity<>(body, headers),
            String.class);
    }
}
