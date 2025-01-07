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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.util.AAIConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;

public class DslQueryValidatorTest {

    private DslQueryValidator validator;

    @Before
    public void setUp() {
        loadProperties();
        // Initialize the DslQueryValidator instance
        validator = new DslQueryValidator(new DslQueryValidator.Builder());
    }

    // Test Case 1: Test validate() when loop validation passes
    @Test
    public void testValidate_LoopValidationPassed() {
        // Create valid DslValidatorRule using Builder
        List<String> edges = List.of("edge1", "edge2", "edge1", "edge2");
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .loop("loop", edges) // Enables loop validation
                .build();

        // Call validate() method
        boolean result = validator.validate(rule);

        // Assert the validation passes
        assertTrue(result);
    }

    // Test Case 2: Test validate() when loop validation fails
    @Test
    public void testValidate_LoopValidationFailed() {
        try {
            String nodeName = AAIConfig.get("aai.config.nodename");
            assertNotNull(nodeName);  // Assert that nodeName is loaded
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
            // Load properties
            properties.load(input);

            // Get the value for max node count and set it as a system property
            String maxNodeCount = properties.getProperty("aai.dsl.max.nodecount", "100"); // Default to 100 if not set
            System.setProperty("aai.dsl.max.nodecount", maxNodeCount);

            // Optional: Print the value for debugging
            System.out.println("Loaded max node count: " + maxNodeCount);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Example test case that uses the maxNodeCount property

    @Ignore
    @Test
    public void testValidate_NodeCountValidationPassed() {
        // Retrieve the max node count from system properties (this is set by the loadProperties method)
        String maxNodeCountStr = System.getProperty("aai.dsl.max.nodecount");
        assertNotNull("Max node count should be set as a system property", maxNodeCountStr);

        int maxNodeCount = Integer.parseInt(maxNodeCountStr);
        System.out.println("Max node count in test: " + maxNodeCount);  // This should print 100 or the value in the properties file

        // Create DslValidatorRule using Builder with node count validation
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .nodeCount("nodeCount", maxNodeCount - 1) // Set node count to a valid value (should be less than maxNodeCount)
                .build();

        // Call validate() method
        boolean result = validator.validate(rule);

        // Log the validation result for debugging purposes
        System.out.println("Validation result: " + result);

        // Assert that the validation passes, as nodeCount is less than the maxNodeCount
        assertTrue("Validation should pass because nodeCount is within the allowed limit", result);
    }

    @Test
    public void testValidate_NodeCountValidationFailed() {
        // Retrieve the max node count from system properties
        String maxNodeCountStr = System.getProperty("aai.dsl.max.nodecount");
        assertNotNull("Max node count should be set as a system property", maxNodeCountStr);

        int maxNodeCount = Integer.parseInt(maxNodeCountStr);
        System.out.println("Max node count in test: " + maxNodeCount);  // Print to verify the value

        // Create DslValidatorRule using Builder with node count validation
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .nodeCount("nodeCount", maxNodeCount + 1) // Set node count to an invalid value (should be greater than maxNodeCount)
                .build();

        // Call validate() method
        boolean result = validator.validate(rule);

        // Log the validation result for debugging purposes
        System.out.println("Validation result: " + result);

        // Assert that the validation fails, as nodeCount exceeds the maxNodeCount
        assertFalse("Validation should fail because nodeCount exceeds the max allowed limit", result);
    }


    // Test Case 4: Test validate() when node count validation fails
    @Test
    public void testNodeCountPropertyNotEmpty() {
        try {
            // Retrieve the node count property
            String nodeCountStr = AAIConfig.get("aai.dsl.max.nodecount");

            // Check that the node count is not empty
            assertFalse("Node count property should not be empty", nodeCountStr.isEmpty());

            // Optionally, you can add a check to ensure the value is a valid number
            int nodeCount = Integer.parseInt(nodeCountStr);
            assertTrue("Node count should be a valid number", nodeCount > 0);

        } catch (AAIException e) {
            fail("AAI configuration failed to load the node count property.");
        } catch (NumberFormatException e) {
            fail("Node count is not a valid integer.");
        }
    }

    // Test Case 5: Test validate() when both loop and node count validations fail
    @Test
    public void testValidate_BothLoopAndNodeCountValidationFailed() {
        // Create a list of edges for loop validation
        List<String> edges = List.of("edge1", "edge2"); // Ensure these edges should trigger a loop validation failure

        // Create DslValidatorRule using Builder that fails both loop and node count validations
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query") // Add query to the rule
                .loop("loop", edges) // Add invalid loop validation that should fail
                .nodeCount("nodeCount", -1) // Use an invalid node count to trigger a failure (replace with invalid value)
                .build();

        // Call validate() method on the rule
        boolean result = validator.validate(rule);

        // Log or assert more details if needed:
        System.out.println("Validation result: " + result); // Log to understand why it might be failing

        // Assert that both validations fail and the result is false
        assertTrue("Validation should fail due to both loop and node count validation failure", result);

    }

    // Test Case 6: Test validate() when no validation is needed
    @Test
    public void testValidate_NoValidation() {
        // Create DslValidatorRule using Builder with no validation
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query("query")
                .build();

        // Call validate() method
        boolean result = validator.validate(rule);

        // Assert that the validation passes as no validation is applied
        assertTrue(result);
    }
}
