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
package org.onap.aai.rest.dsl.validation;

import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.util.AAIConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class DslQueryValidatorTest {

    private DslQueryValidator validator;

    @BeforeEach
    void setUp() {
        loadProperties();
        validator = new DslQueryValidator(new DslQueryValidator.Builder());
    }

    @Test
    void testValidate_LoopValidationPassed() {
        List<String> edges = List.of("edge1", "edge2", "edge1", "edge2");
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .loop("loop", edges)
                .build();

        boolean result = validator.validate(rule);

        assertTrue(result);
    }

    @Test
    void testValidate_LoopValidationFailed() {
        try {
            String nodeName = AAIConfig.get("aai.config.nodename");
            assertNotNull(nodeName);
        } catch (AAIException e) {
            fail("Configuration property 'aai.config.nodename' is missing.");
        }
    }

    private void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("src/test/resources/aaiconfig.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find aaiconfig.properties");
                return;
            }
            properties.load(input);

            String maxNodeCount = properties.getProperty("aai.dsl.max.nodecount", "100");
            System.setProperty("aai.dsl.max.nodecount", maxNodeCount);
            System.out.println("Loaded max node count: " + maxNodeCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @EnabledIfSystemProperty(named = "aai.dsl.max.nodecount", matches = ".*")
    @Test
    void testValidate_NodeCountValidationPassed() {
        String maxNodeCountStr = System.getProperty("aai.dsl.max.nodecount");
        assertNotNull(maxNodeCountStr);

        int maxNodeCount = Integer.parseInt(maxNodeCountStr);

        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .nodeCount("nodeCount", maxNodeCount - 1)
                .build();

        boolean result = validator.validate(rule);

        assertFalse(result);
    }

    @EnabledIfSystemProperty(named = "aai.dsl.max.nodecount", matches = ".*")
    @Test
    void testValidate_NodeCountValidationFailed() {
        String maxNodeCountStr = System.getProperty("aai.dsl.max.nodecount");
        assertNotNull(maxNodeCountStr);

        int maxNodeCount = Integer.parseInt(maxNodeCountStr);

        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .nodeCount("nodeCount", maxNodeCount + 1)
                .build();

        boolean result = validator.validate(rule);

        assertFalse(result);
    }

    @Test
    void testNodeCountPropertyNotEmpty() {
        try {
            String nodeCountStr = AAIConfig.get("aai.dsl.max.nodecount");

            assertFalse(nodeCountStr.isEmpty());

            int nodeCount = Integer.parseInt(nodeCountStr);
            assertTrue(nodeCount > 0);
        } catch (AAIException e) {
            fail("AAI configuration failed to load the node count property.");
        } catch (NumberFormatException e) {
            fail("Node count is not a valid integer.");
        }
    }

    @Test
    void testValidate_BothLoopAndNodeCountValidationFailed() {
        List<String> edges = List.of("edge1", "edge2");

        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .loop("loop", edges)
                .nodeCount("nodeCount", -1)
                .build();

        boolean result = validator.validate(rule);

        assertTrue(result);
    }

    @Test
    void testValidate_NoValidation() {
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .build();

        boolean result = validator.validate(rule);

        assertTrue(result);
    }
}
