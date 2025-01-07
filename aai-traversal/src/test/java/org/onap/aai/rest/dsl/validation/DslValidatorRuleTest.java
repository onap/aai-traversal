/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2024 Deutsche Telekom. All rights reserved.
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

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class DslValidatorRuleTest {

    // Test Case 1: Test Builder Pattern - Default Initialization
    @Test
    public void testBuilder_DefaultInitialization() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();

        // Default values check
        assertEquals("", rule.getQuery());
        assertFalse(rule.isValidateLoop());
        assertFalse(rule.isValidateNodeCount());
        assertEquals(0, rule.getNodeCount());
        assertTrue(rule.getEdges().isEmpty());
    }

    // Test Case 2: Test Builder - Loop Validation Enabled
    @Test
    public void testBuilder_LoopValidationEnabled() {
        List<String> edges = List.of("edge1", "edge2");
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .loop("loop", edges)
                .build();

        // Check if loop validation is enabled and edges are set
        assertTrue(rule.isValidateLoop());
        assertEquals(edges, rule.getEdges());
    }

    // Test Case 3: Test Builder - Node Count Validation Enabled
    @Test
    public void testBuilder_NodeCountValidationEnabled() {
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .nodeCount("nodeCount", 5)
                .build();

        // Check if node count validation is enabled and the count is set correctly
        assertTrue(rule.isValidateNodeCount());
        assertEquals(5, rule.getNodeCount());
    }

    // Test Case 4: Test Builder - Query Set Correctly
    @Test
    public void testBuilder_QuerySetCorrectly() {
        String testQuery = "SELECT * FROM Nodes";
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query(testQuery)
                .build();

        // Check if the query is set correctly
        assertEquals(testQuery, rule.getQuery());
    }

    // Test Case 5: Test Builder - Invalid Node Count Validation (No Match)
    @Test
    public void testBuilder_InvalidNodeCountValidation() {
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .nodeCount("none", 5)
                .build();

        // Check if node count validation is not enabled when invalid value is passed
        assertFalse(rule.isValidateNodeCount());
    }

    // Test Case 6: Test Builder - Invalid Loop Validation (No Match)
    @Test
    public void testBuilder_InvalidLoopValidation() {
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .loop("none", List.of("edge1", "edge2"))
                .build();

        // Check if loop validation is not enabled when invalid value is passed
        assertFalse(rule.isValidateLoop());
    }

    // Test Case 7: Test Builder - Combining All Validations
    @Test
    public void testBuilder_CombiningAllValidations() {
        List<String> edges = List.of("edge1", "edge2");
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .loop("loop", edges)
                .nodeCount("nodeCount", 5)
                .build();

        // Check if both loop and node count validations are enabled and the values are set
        assertTrue(rule.isValidateLoop());
        assertTrue(rule.isValidateNodeCount());
        assertEquals(edges, rule.getEdges());
        assertEquals(5, rule.getNodeCount());
    }

    // Test Case 8: Test Setters - Set Query Method
    @Test
    public void testSetQuery() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();

        // Set a query after the object has been built
        String testQuery = "SELECT * FROM Nodes";
        rule.setQuery(testQuery);

        // Verify that the query has been set correctly
        assertEquals(testQuery, rule.getQuery());
    }

    // Test Case 9: Test Setters - Set Validate Loop Method
    @Test
    public void testSetValidateLoop() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();

        // Set validateLoop to true after the object has been built
        rule.setValidateLoop(true);

        // Verify that the validateLoop flag is set to true
        assertTrue(rule.isValidateLoop());
    }

    // Test Case 10: Test Setters - Set Validate Node Count Method
    @Test
    public void testSetValidateNodeCount() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();

        // Set validateNodeCount to true after the object has been built
        rule.setValidateNodeCount(true);

        // Verify that the validateNodeCount flag is set to true
        assertTrue(rule.isValidateNodeCount());
    }

    // Test Case 11: Test Setters - Set Node Count Method
    @Test
    public void testSetNodeCount() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();

        // Set a node count after the object has been built
        rule.setNodeCount(10);

        // Verify that the nodeCount value is set correctly
        assertEquals(10, rule.getNodeCount());
    }

    // Test Case 12: Test Setters - Set Edges Method
    @Test
    public void testSetEdges() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();

        // Set edges list after the object has been built
        List<String> edges = List.of("edge1", "edge2", "edge3");
        rule.setEdges(edges);

        // Verify that the edges list has been set correctly
        assertEquals(edges, rule.getEdges());
    }
}
