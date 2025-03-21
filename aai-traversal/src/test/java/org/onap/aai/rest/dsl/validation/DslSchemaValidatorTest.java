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
        builder = new DslSchemaValidator.Builder();
    }

    @Test
    void testCreateDslSchemaValidatorWithBuilder() {
        builder.schema();
        DslSchemaValidator validator = (DslSchemaValidator) builder.create();
        assertTrue(validator instanceof DslSchemaValidator);
    }

    @Test
    void testValidateReturnsTrue() {
        builder.schema();
        DslSchemaValidator validator = (DslSchemaValidator) builder.create();
        DslValidatorRule rule = new DslValidatorRule.Builder().build();
        boolean result = validator.validate(rule);
        assertTrue(result);
    }

    @Test
    void testDslSchemaValidatorIsNotNull() {
        builder.schema();
        DslSchemaValidator validator = (DslSchemaValidator) builder.create();
        assertNotNull(validator);
    }

    @Test
    void testBuilderSetsCorrectFlag() {
        builder.schema();
        assertTrue(builder.isSchemaValidation);
    }
}
