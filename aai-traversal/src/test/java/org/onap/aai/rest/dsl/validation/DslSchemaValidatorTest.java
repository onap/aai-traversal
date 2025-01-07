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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DslSchemaValidatorTest {

    private DslSchemaValidator.Builder builder;

    @BeforeEach
    void setUp() {
        // Initialize the builder before each test
        builder = new DslSchemaValidator.Builder();
    }

    @Test
    void testCreateDslSchemaValidatorWithBuilder() {
        // Given: a builder with schema validation enabled
        builder.schema();  // Assuming this method enables schema validation

        // When: create the DslSchemaValidator using the builder
        DslSchemaValidator validator = (DslSchemaValidator) builder.create(); // Assuming create() returns an instance of DslSchemaValidator

        // Then: the created object should be an instance of DslSchemaValidator
        assertTrue(validator instanceof DslSchemaValidator);
    }

    @Test
    void testValidateReturnsTrue() {
        // Given: a DslSchemaValidator instance with schema validation enabled
        builder.schema();  // Enable schema validation
        DslSchemaValidator validator = (DslSchemaValidator) builder.create();  // Create the validator

        // When: validating with a DslValidatorRule (using the Builder to create DslValidatorRule)
        DslValidatorRule rule = new DslValidatorRule.Builder().build();  // Build the rule using the Builder

        boolean result = validator.validate(rule);  // Call validate() on the validator

        // Then: the validation should return true, as per the current implementation of validate()
        assertTrue(result);
    }

    @Test
    void testDslSchemaValidatorIsNotNull() {
        // Given: a DslSchemaValidator created using the builder
        builder.schema();  // Enable schema validation
        DslSchemaValidator validator = (DslSchemaValidator) builder.create();  // Create the validator

        // When: the DslSchemaValidator is created

        // Then: the validator should not be null
        assertNotNull(validator);
    }

    @Test
    void testBuilderSetsCorrectFlag() {
        // Given: a builder object
        DslSchemaValidator.Builder builder = new DslSchemaValidator.Builder();

        // When: call schema() method on builder to enable schema validation
        builder.schema();

        // Then: the builder should have schema validation enabled
        // We assume the builder has a flag called isSchemaValidation (or similar) to track this
        assertTrue(builder.isSchemaValidation);
    }
}
