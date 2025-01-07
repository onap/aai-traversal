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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DslValidatorTest {

    private DslValidator.Builder builder;

    @BeforeEach
    void setUp() {
        builder = new DslValidator.Builder();
    }

    @Test
    void testCreateDslSchemaValidatorWhenSchemaIsTrue() {
        // Given: builder with schema validation enabled
        builder.schema();

        // When: create the DslValidator
        DslValidator validator = builder.create();

        // Then: the validator should be an instance of DslSchemaValidator
        assertTrue(validator instanceof DslSchemaValidator);
    }

    @Test
    void testCreateDslQueryValidatorWhenSchemaIsFalse() {
        // Given: builder with no schema validation
        // (schema() not called, defaults to false)

        // When: create the DslValidator
        DslValidator validator = builder.create();

        // Then: the validator should be an instance of DslQueryValidator
        assertTrue(validator instanceof DslQueryValidator);
    }

    @Test
    void testErrorMessageInitialization() {
        // Given: a new DslValidator object
        DslValidator validator = builder.create();

        // When: get the error message
        String errorMessage = validator.getErrorMessage();

        // Then: it should be an empty string
        assertEquals("", errorMessage);
    }

    @Test
    void testSchemaMethodSetsFlag() {
        // Given: a builder object
        DslValidator.Builder builder = new DslValidator.Builder();

        // When: call schema() method
        builder.schema();

        // Then: the schema validation flag should be set to true
        assertTrue(builder.isSchemaValidation);
    }

}
