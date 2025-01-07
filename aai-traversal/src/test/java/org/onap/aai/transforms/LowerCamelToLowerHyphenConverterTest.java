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
package org.onap.aai.transforms;

import org.junit.Test;
import static org.junit.Assert.*;

public class LowerCamelToLowerHyphenConverterTest {

    private final LowerCamelToLowerHyphenConverter converter = new LowerCamelToLowerHyphenConverter();

    // Test for valid input (camelCase to lower-hyphen)
    @Test
    public void testConvert_validInput() {
        assertEquals("my-variable-name", converter.convert("myVariableName"));
        assertEquals("this-is-a-test", converter.convert("thisIsATest"));
    }

    // Test for null input
    @Test
    public void testConvert_nullInput() {
        assertNull(converter.convert(null));
    }

    // Test for an empty string
    @Test
    public void testConvert_emptyString() {
        assertEquals("", converter.convert(""));
    }

    // Test for no camel case (lowercase input should remain unchanged)
    @Test
    public void testConvert_lowercaseInput() {
        assertEquals("lowercase", converter.convert("lowercase"));
    }

    // Test for single-word input (should remain unchanged)
    @Test
    public void testConvert_singleWord() {
        assertEquals("test", converter.convert("test"));
    }

    // Test for string with hyphens (should still convert camel-case portions)
    @Test
    public void testConvert_inputWithHyphens() {
        assertEquals("my-variable-name-test", converter.convert("myVariableNameTest"));
        assertEquals("already-hyphenated-case", converter.convert("alreadyHyphenatedCase"));
    }
}
