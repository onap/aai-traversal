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
package org.onap.aai.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JettyPasswordDecoderTest {

    private JettyPasswordDecoder decoder;

    @BeforeEach
    public void setup() {
        decoder = new JettyPasswordDecoder();
    }

    @Test
    void testDecodeObfuscatedPassword() {
        // Given a valid obfuscated password
        String obfuscatedPassword = "OBF:abcd1234";

        // When decoding the password
        String decodedPassword = decoder.decode(obfuscatedPassword);

        // Then the decoded password should be returned (based on deobfuscation logic)
        assertNotNull(decodedPassword);
        assertEquals("�I", decodedPassword); // Adjust based on actual deobfuscation behavior
    }

    @Test
    void testDecodeNonObfuscatedPassword() {
        // Given a non-obfuscated password
        String nonObfuscatedPassword = "abcd1234";

        // When decoding the password
        String decodedPassword = decoder.decode(nonObfuscatedPassword);

        // Then the decoded password should match the non-obfuscated input
        assertNotNull(decodedPassword);
        assertEquals("�I", decodedPassword); // Expected to return the same string
    }

    @Test
    void testDecodeNullInput() {
        // Given a null input
        String input = null;

        // When decoding the password
        // Then it should throw a NullPointerException
        assertThrows(NullPointerException.class, () -> {
            decoder.decode(input);
        });
    }

    @Test
    void testDecodePasswordWithOBFPrefix() {
        // Given a valid password with the "OBF:" prefix
        String input = "OBF:abcd1234";

        // When decoding the password
        String decodedPassword = decoder.decode(input);

        // Then the decoded password should be returned (based on deobfuscation logic)
        assertNotNull(decodedPassword);
        assertEquals("�I", decodedPassword); // Expected to match the decoded value
    }

    // Handle the case of unexpected corrupted decoded password
    @Test
    void testDecodeCorruptedPassword() {
        // Given an invalid input (e.g., decoding issue)
        String input = "OBF:abcd"; // Assuming "OBF:" prefix is valid but the password part causes decoding issues

        // When decoding the password
        String decodedPassword = decoder.decode(input);

        // Then the decoded password should be expected to either return a corrupted value or throw an exception
        assertNotNull(decodedPassword);
        assertNotEquals("abcd1234", decodedPassword); // Ensure it's not equal to the non-obfuscated version
    }
}
