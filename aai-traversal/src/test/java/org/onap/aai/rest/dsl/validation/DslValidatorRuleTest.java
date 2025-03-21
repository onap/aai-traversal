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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class DslValidatorRuleTest {

    @Test
    public void testBuilder_DefaultInitialization() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();
        assertEquals("", rule.getQuery());
        assertFalse(rule.isValidateLoop());
        assertFalse(rule.isValidateNodeCount());
        assertEquals(0, rule.getNodeCount());
        assertTrue(rule.getEdges().isEmpty());
    }

    @Test
    public void testBuilder_LoopValidationEnabled() {
        List<String> edges = List.of("edge1", "edge2");
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .loop("loop", edges)
                .build();
        assertTrue(rule.isValidateLoop());
        assertEquals(edges, rule.getEdges());
    }

    @Test
    public void testBuilder_NodeCountValidationEnabled() {
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .nodeCount("nodeCount", 5)
                .build();
        assertTrue(rule.isValidateNodeCount());
        assertEquals(5, rule.getNodeCount());
    }

    @Test
    public void testBuilder_QuerySetCorrectly() {
        String testQuery = "SELECT * FROM Nodes";
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .query(testQuery)
                .build();
        assertEquals(testQuery, rule.getQuery());
    }

    @Test
    public void testBuilder_InvalidNodeCountValidation() {
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .nodeCount("none", 5)
                .build();
        assertFalse(rule.isValidateNodeCount());
    }

    @Test
    public void testBuilder_InvalidLoopValidation() {
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .loop("none", List.of("edge1", "edge2"))
                .build();
        assertFalse(rule.isValidateLoop());
    }

    @Test
    public void testBuilder_CombiningAllValidations() {
        List<String> edges = List.of("edge1", "edge2");
        DslValidatorRule rule = new DslValidatorRule.Builder()
                .loop("loop", edges)
                .nodeCount("nodeCount", 5)
                .build();
        assertTrue(rule.isValidateLoop());
        assertTrue(rule.isValidateNodeCount());
        assertEquals(edges, rule.getEdges());
        assertEquals(5, rule.getNodeCount());
    }

    @Test
    public void testSetQuery() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();
        String testQuery = "SELECT * FROM Nodes";
        rule.setQuery(testQuery);
        assertEquals(testQuery, rule.getQuery());
    }

    @Test
    public void testSetValidateLoop() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();
        rule.setValidateLoop(true);
        assertTrue(rule.isValidateLoop());
    }

    @Test
    public void testSetValidateNodeCount() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();
        rule.setValidateNodeCount(true);
        assertTrue(rule.isValidateNodeCount());
    }

    @Test
    public void testSetNodeCount() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();
        rule.setNodeCount(10);
        assertEquals(10, rule.getNodeCount());
    }

    @Test
    public void testSetEdges() {
        DslValidatorRule rule = new DslValidatorRule.Builder().build();
        List<String> edges = List.of("edge1", "edge2", "edge3");
        rule.setEdges(edges);
        assertEquals(edges, rule.getEdges());
    }
}
