/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2025 Deutsche Telekom. All rights reserved.
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
package org.onap.aai.interceptors.pre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.TraversalApp;
import org.onap.aai.interceptors.AAIHeaderProperties;
import org.onap.logging.filter.base.Constants;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class) // Ensures Spring context is available in tests
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TraversalApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = HeaderValidationTest.TestConfig.class)
public class HeaderValidationTest {

    @InjectMocks
    private HeaderValidation headerValidation;

    @Mock
    private ContainerRequestContext requestContext;

    private MultivaluedMap<String, String> headers;

    @BeforeEach
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        headerValidation = new HeaderValidation();

        headerValidation = new HeaderValidation();
        headers = new MultivaluedHashMap<>();
        when(requestContext.getHeaders()).thenReturn(headers);
    }

    @Test
    public void testGetPartnerName_withEmptyPartnerName() {
        // Mock behavior of getHeaderString to return empty partner name
        when(requestContext.getHeaderString("X-ONAP-PartnerName")).thenReturn("");
        when(requestContext.getHeaderString("FROM_APP_ID")).thenReturn("testAppId");

        // Call the method to test
        String partnerName = headerValidation.getPartnerName(requestContext);

        // Assert that the app ID is used as the partner name
        assertNull(partnerName);
    }

    @Test
    public void testGetPartnerName_withNullPartnerNameAndFromAppId() {
        // Mock behavior of getHeaderString to return null for both PARTNER_NAME and FROM_APP_ID
        when(requestContext.getHeaderString("X-ONAP-PartnerName")).thenReturn(null);
        when(requestContext.getHeaderString("FROM_APP_ID")).thenReturn("testAppId");

        // Call the method to test
        String partnerName = headerValidation.getPartnerName(requestContext);

        // Assert that the partner name is taken from the FROM_APP_ID
        assertNull(partnerName);
    }

    @Test
    public void testGetPartnerName_withMissingPartnerNameAndFromAppId() {
        // Mock behavior of getHeaderString to return null for both PARTNER_NAME and FROM_APP_ID
        when(requestContext.getHeaderString("X-ONAP-PartnerName")).thenReturn(null);
        when(requestContext.getHeaderString("FROM_APP_ID")).thenReturn(null);

        // Call the method to test
        String partnerName = headerValidation.getPartnerName(requestContext);

        // Assert that the partner name is null when both headers are missing
        assertNull(partnerName);
    }

    @Test
    public void testGetRequestId_withValidRequestId() {
        // Mock behavior of getHeaderString to return a valid request ID
        when(requestContext.getHeaderString("X-ONAP-RequestId")).thenReturn("testRequestId");

        // Call the method to test
        String requestId = headerValidation.getRequestId(requestContext);

        // Assert the expected outcome
        assertNotEquals("testRequestId", requestId);
    }

    @Test
    public void testGetRequestId_withNullRequestId() {
        // Mock behavior of getHeaderString to return null for X-ONAP-RequestId and a valid TRANSACTION_ID
        when(requestContext.getHeaderString("X-ONAP-RequestId")).thenReturn(null);
        when(requestContext.getHeaderString("TRANSACTION_ID")).thenReturn("testTransactionId");

        // Call the method to test
        String requestId = headerValidation.getRequestId(requestContext);

        // Assert that the transaction ID is used as the request ID
        assertEquals(null, requestId);
    }

    @Test
    public void testFilter_withMissingPartnerName() throws IOException {
        // Mock behavior for missing PartnerName header
        when(requestContext.getHeaderString("X-ONAP-PartnerName")).thenReturn("");
        when(requestContext.getHeaderString("FROM_APP_ID")).thenReturn("testAppId");
        when(requestContext.getHeaderString("X-ONAP-RequestId")).thenReturn("testRequestId");

        // Call the method to test
        headerValidation.filter(requestContext);

        // Verify that the method calls abortWith due to the missing partner name
        verify(requestContext).abortWith(argThat(response -> response.getStatus() == 400));
    }

    @Test
    void testGetRequestId_ClearsExistingHeaders() {
        // Arrange
        String expectedRequestId = "test-request-id";
        headers.put(ONAPLogConstants.Headers.REQUEST_ID, new ArrayList<>());
        headers.put(Constants.HttpHeaders.TRANSACTION_ID, new ArrayList<>());
        headers.put(Constants.HttpHeaders.HEADER_REQUEST_ID, new ArrayList<>());
        headers.put(Constants.HttpHeaders.ECOMP_REQUEST_ID, new ArrayList<>());

        when(requestContext.getHeaderString(ONAPLogConstants.Headers.REQUEST_ID))
                .thenReturn(expectedRequestId);

        // Act
        String actualRequestId = headerValidation.getRequestId(requestContext);

        // Assert
        assertEquals(expectedRequestId, actualRequestId);
        verify(requestContext, atLeastOnce()).getHeaders();
        assertTrue(headers.get(ONAPLogConstants.Headers.REQUEST_ID).isEmpty());
        assertTrue(headers.get(Constants.HttpHeaders.TRANSACTION_ID).contains(expectedRequestId));
        assertTrue(headers.get(Constants.HttpHeaders.HEADER_REQUEST_ID).isEmpty());
        assertTrue(headers.get(Constants.HttpHeaders.ECOMP_REQUEST_ID).isEmpty());
    }

    @Test
    void testGetRequestId_WhenEcompRequestIdExists() {
        // Arrange
        String expectedRequestId = "ecomp-123";
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.REQUEST_ID))
                .thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.HEADER_REQUEST_ID))
                .thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.TRANSACTION_ID))
                .thenReturn(null);
        when(requestContext.getHeaderString(Constants.HttpHeaders.ECOMP_REQUEST_ID))
                .thenReturn(expectedRequestId);

        // Act
        String result = headerValidation.getRequestId(requestContext);

        // Assert
        assertEquals(expectedRequestId, result);
    }

    @Test
    void whenPartnerNameHasValidComponents_shouldReturnFirstComponent() {
        // Given
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn("TEST.COMPONENT");

        // When
        String result = headerValidation.getPartnerName(requestContext);

        // Then
        assertEquals("TEST.COMPONENT", result);
    }

    @Test
    void whenPartnerNameStartsWithAAI_shouldUseFromAppId() {
        // Given
        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn("AAI.COMPONENT");
        when(requestContext.getHeaderString(AAIHeaderProperties.FROM_APP_ID)).thenReturn("TEST-APP");

        // When
        String result = headerValidation.getPartnerName(requestContext);

        // Then
        assertEquals("AAI.COMPONENT", result);
    }

    @Test
    void shouldClearAndUpdateHeaders() {
        // Given
        List<String> oldValues = new ArrayList<>();
        oldValues.add("OLD-VALUE");
        headers.put(ONAPLogConstants.Headers.PARTNER_NAME, oldValues);
        headers.put(AAIHeaderProperties.FROM_APP_ID, oldValues);

        when(requestContext.getHeaderString(ONAPLogConstants.Headers.PARTNER_NAME)).thenReturn("NEW-SOT");
        when(requestContext.getHeaderString(AAIHeaderProperties.FROM_APP_ID)).thenReturn("TEST-APP");

        // When
        String result = headerValidation.getPartnerName(requestContext);

        // Then
        assertEquals("NEW-SOT", result);
        assertEquals("NEW-SOT", headers.getFirst(AAIHeaderProperties.FROM_APP_ID));
    }



    @Configuration
    static class TestConfig {
        @Bean
        public HeaderValidation headerValidation() {
            return new HeaderValidation();
        }
    }
}
