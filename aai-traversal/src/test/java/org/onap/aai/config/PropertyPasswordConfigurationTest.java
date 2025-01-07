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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PropertyPasswordConfigurationTest {

    private PropertyPasswordConfiguration configuration;
    private ConfigurableApplicationContext applicationContext;
    private ConfigurableEnvironment environment;
    private Path tempDir;
    private EnumerablePropertySource<?> propertySource;
    private Map<String, Object> propertyOverrides;

    @BeforeEach
    void setUp() throws IOException {
        configuration = new PropertyPasswordConfiguration();
        applicationContext = mock(ConfigurableApplicationContext.class);
        environment = mock(ConfigurableEnvironment.class);

        when(applicationContext.getEnvironment()).thenReturn(environment);

        // Create temporary directory for test files
        tempDir = Files.createTempDirectory("cert-test");
        propertySource = mock(EnumerablePropertySource.class);
        propertyOverrides = new HashMap<>();
    }

    @Test
    void testInitializeWithCertFiles() throws IOException {
        // Setup test files
        String certPath = tempDir.toString() + "/cert";

        // Create .password file
        File passwordFile = new File(certPath + ".password");
        try (FileWriter writer = new FileWriter(passwordFile)) {
            writer.write("testPassword123");
        }

        // Create .passphrases file
        File passphrasesFile = new File(certPath + ".passphrases");
        try (FileWriter writer = new FileWriter(passphrasesFile)) {
            writer.write("cadi_truststore_password=trustPass123");
        }

        // Mock environment properties
        when(environment.getProperty("server.certs.location")).thenReturn(certPath);
        when(environment.getPropertySources()).thenReturn(new MutablePropertySources());

        // Execute
        configuration.initialize(applicationContext);

        // Verify
        verify(environment, times(1)).getProperty("server.certs.location");
        verify(environment, atLeastOnce()).getPropertySources();
    }

    @Test
    void testPasswordDecoding() {
        // Create test property source with encoded password
        PropertySource<?> source = mock(PropertySource.class);
        when(source.getProperty("test.password")).thenReturn("password(encodedValue)");

        MutablePropertySources propertySources = new MutablePropertySources();
        propertySources.addFirst(source);

        when(environment.getPropertySources()).thenReturn(propertySources);

        // Execute
        configuration.initialize(applicationContext);

        // Verify property sources were accessed
        verify(environment, atLeastOnce()).getPropertySources();
    }

    @Test
    void testInitializeWithoutCertPath() {
        // Mock environment with no cert path
        when(environment.getProperty("server.certs.location")).thenReturn(null);
        when(environment.getPropertySources()).thenReturn(new MutablePropertySources());

        // Execute
        configuration.initialize(applicationContext);

        // Verify
        verify(environment, times(1)).getProperty("server.certs.location");
        verify(environment, atLeastOnce()).getPropertySources();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temporary files
        Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
                .map(Path::toFile)
                .forEach(File::delete);
    }

    // Reflective test for private method
    @Test
    void testDecodePasswordsWithReflection() throws Exception {
        // Setup test data
        String[] propertyNames = {"test.password"};
        when(propertySource.getPropertyNames()).thenReturn(propertyNames);
        when(propertySource.getProperty("test.password")).thenReturn("password(encodedValue)");

        // Use reflection to access the private decodePasswords method
        Method decodePasswordsMethod = PropertyPasswordConfiguration.class.getDeclaredMethod("decodePasswords", PropertySource.class, Map.class);
        decodePasswordsMethod.setAccessible(true);

        // Execute decodePasswords through reflection
        decodePasswordsMethod.invoke(configuration, propertySource, propertyOverrides);

        // Verify that password decoding was done
        assertEquals(1, propertyOverrides.size());
        assertTrue(propertyOverrides.containsKey("test.password"));
    }

    @Test
    void testDecodePasswordsWithNonStringValues() {
        // Setup test data
        String[] propertyNames = {"intValue", "stringValue"};
        when(propertySource.getPropertyNames()).thenReturn(propertyNames);

        when(propertySource.getProperty("intValue")).thenReturn(123);
        when(propertySource.getProperty("stringValue")).thenReturn("enc(test)");

        // Use reflection to access the private decodePasswords method
        try {
            Method decodePasswordsMethod = PropertyPasswordConfiguration.class.getDeclaredMethod("decodePasswords", PropertySource.class, Map.class);
            decodePasswordsMethod.setAccessible(true);
            decodePasswordsMethod.invoke(configuration, propertySource, propertyOverrides);
        } catch (Exception e) {
            fail("Exception while reflecting on decodePasswords method: " + e.getMessage());
        }

        // Verify
        assertEquals(1, propertyOverrides.size());
        assertFalse(propertyOverrides.containsKey("intValue"));
        assertTrue(propertyOverrides.containsKey("stringValue"));
    }

    @Test
    void testDecodePasswordsWithEmptyPropertyNames() {
        // Setup test data
        String[] propertyNames = {};
        when(propertySource.getPropertyNames()).thenReturn(propertyNames);

        // Execute
        try {
            Method decodePasswordsMethod = PropertyPasswordConfiguration.class.getDeclaredMethod("decodePasswords", PropertySource.class, Map.class);
            decodePasswordsMethod.setAccessible(true);
            decodePasswordsMethod.invoke(configuration, propertySource, propertyOverrides);
        } catch (Exception e) {
            fail("Exception while reflecting on decodePasswords method: " + e.getMessage());
        }

        // Verify
        assertTrue(propertyOverrides.isEmpty());
    }

    @Test
    void testDecodePasswordsWithNullPropertySource() {
        // Setup
        PropertySource<?> nullPropertySource = null;

        // Execute
        try {
            Method decodePasswordsMethod = PropertyPasswordConfiguration.class.getDeclaredMethod("decodePasswords", PropertySource.class, Map.class);
            decodePasswordsMethod.setAccessible(true);
            decodePasswordsMethod.invoke(configuration, nullPropertySource, propertyOverrides);
        } catch (Exception e) {
            fail("Exception while reflecting on decodePasswords method: " + e.getMessage());
        }

        // Verify
        assertTrue(propertyOverrides.isEmpty());
    }

    @Test
    void testDecodePasswordsWithNonEnumerablePropertySource() {
        // Setup
        PropertySource<?> nonEnumerableSource = mock(PropertySource.class);

        // Execute
        try {
            Method decodePasswordsMethod = PropertyPasswordConfiguration.class.getDeclaredMethod("decodePasswords", PropertySource.class, Map.class);
            decodePasswordsMethod.setAccessible(true);
            decodePasswordsMethod.invoke(configuration, nonEnumerableSource, propertyOverrides);
        } catch (Exception e) {
            fail("Exception while reflecting on decodePasswords method: " + e.getMessage());
        }

        // Verify
        assertTrue(propertyOverrides.isEmpty());
    }

    @Test
    void testDecodePasswordsWithNullValues() {
        // Setup test data
        String[] propertyNames = {"nullValue", "stringValue"};
        when(propertySource.getPropertyNames()).thenReturn(propertyNames);

        when(propertySource.getProperty("nullValue")).thenReturn(null);
        when(propertySource.getProperty("stringValue")).thenReturn("enc(test)");

        // Use reflection to access the private decodePasswords method
        try {
            Method decodePasswordsMethod = PropertyPasswordConfiguration.class.getDeclaredMethod("decodePasswords", PropertySource.class, Map.class);
            decodePasswordsMethod.setAccessible(true);
            decodePasswordsMethod.invoke(configuration, propertySource, propertyOverrides);
        } catch (Exception e) {
            fail("Exception while reflecting on decodePasswords method: " + e.getMessage());
        }

        // Verify
        assertEquals(1, propertyOverrides.size());
        assertFalse(propertyOverrides.containsKey("nullValue"));
        assertTrue(propertyOverrides.containsKey("stringValue"));
    }


    @Test
        public void testDecodePasswordsInStringWithNullInput() throws Exception {
            // Create an instance of the class containing the method
            PropertyPasswordConfiguration config = new PropertyPasswordConfiguration();

            // Use reflection to get the private method decodePasswordsInString
            Method method = PropertyPasswordConfiguration.class.getDeclaredMethod("decodePasswordsInString", String.class);

            // Set the method accessible to bypass the private modifier
            method.setAccessible(true);

            // Invoke the method with a null input
            Object result = method.invoke(config, (Object) null);

            // Assert that the result is null
            assertNull(result);
        }

}
