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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    void testValidate_LoopValidationPassedAll() {
        List<String> edges = List.of("edge1", "edge2", "edge1", "edge2");
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .loop("all", edges)
                .build();

        boolean result = validator.validate(rule);

        assertTrue(result);
    }


    private void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("src/test/resources/aaiconfig.properties")) {
            properties.load(input);

            String maxNodeCount = properties.getProperty("aai.dsl.max.nodecount", "100");
            System.setProperty("aai.dsl.max.nodecount", maxNodeCount);
            System.out.println("Loaded max node count: " + maxNodeCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testValidate_NodeCountValidationFailed() {
        int maxNodeCount = 100;

        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .nodeCount("nodeCount", maxNodeCount + 1)
                .build();

        boolean result = validator.validate(rule);

        assertFalse(result);
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
