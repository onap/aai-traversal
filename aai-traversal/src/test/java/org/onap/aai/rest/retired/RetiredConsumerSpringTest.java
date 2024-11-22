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
package org.onap.aai.rest.retired;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.onap.aai.rest.AbstractSpringRestTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RetiredConsumerSpringTest extends AbstractSpringRestTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetiredConsumerSpringTest.class);

    private Map<String, HttpStatus> httpStatusMap;

    @Test
    public void testOldVersionsEndpointReturnRetired() {
        setupOldVersions();
        executeRestCalls();
    }

    protected void executeRestCalls() {
        httpStatusMap.forEach((url, status) -> {
            ResponseEntity responseEntity;
            responseEntity =
                restTemplate.exchange(baseUrl + url, HttpMethod.GET, httpEntity, String.class);
            LOGGER.debug("For url {} expected status {} actual status {} and body {}", url, status,
                responseEntity.getStatusCodeValue(), responseEntity.getBody());
            assertEquals(status, responseEntity.getStatusCode());
        });
    }

    private void setupOldVersions() {
        httpStatusMap = new HashMap<>();

        httpStatusMap.put("/aai/v2/search/generic-query", HttpStatus.GONE);
        httpStatusMap.put("/aai/v3/search/generic-query", HttpStatus.GONE);
        httpStatusMap.put("/aai/v4/search/generic-query", HttpStatus.GONE);
        httpStatusMap.put("/aai/v5/search/generic-query", HttpStatus.GONE);
        httpStatusMap.put("/aai/v6/search/generic-query", HttpStatus.GONE);
    }
}
