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
package org.onap.aai.interceptors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

public class AAIContainerFilterTest {

    private class TestAAIContainerFilter extends AAIContainerFilter {
        // This subclass allows us to test the protected methods
    }

    @Test
    public void testGenDate() {
        TestAAIContainerFilter filter = new TestAAIContainerFilter();

        // Get the generated date-time string
        String generatedDate = filter.genDate();

        assertNotNull(generatedDate);
        assertTrue(generatedDate.matches("\\d{6}-\\d{2}:\\d{2}:\\d{2}:\\d{3}"));
    }

    @Test
    public void testIsValidUUID_ValidUUID() {
        TestAAIContainerFilter filter = new TestAAIContainerFilter();
        // Test with a valid UUID
        String validUUID = UUID.randomUUID().toString();
        assertTrue(filter.isValidUUID(validUUID));
    }

    @Test
    public void testIsValidUUID_InvalidUUID() {
        TestAAIContainerFilter filter = new TestAAIContainerFilter();
        // Test with an invalid UUID (not a valid UUID string)
        String invalidUUID = "invalid-uuid-string";
        assertFalse(filter.isValidUUID(invalidUUID));
    }

}
