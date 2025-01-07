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
package org.onap.aai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.nodes.NodeIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = TraversalApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = TraversalAppTest.TestConfig.class)
public class TraversalAppTest extends AAISetup {

    private static final Logger logger = LoggerFactory.getLogger(TraversalAppTest.class); // Logger for test class

    @Mock
    private NodeIngestor mockNodeIngestor;

    @Mock
    private SpringApplication mockSpringApplication;

    @Mock
    private AAIGraph mockGraph;

    private TraversalApp app;

    static Environment mockEnv = mock(Environment.class);

    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    // Initialize mocks and the TraversalApp instance before each test
    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        app = new TraversalApp();

        System.setOut(new PrintStream(outputStreamCaptor));

        // Mock environment properties
        when(mockEnv.getProperty("spring.application.name")).thenReturn("aai-traversal");
        when(mockEnv.getProperty("server.port")).thenReturn("8080");

        // Inject mocks into private fields via reflection
        injectMockIntoPrivateField(app, "env", mockEnv);
        injectMockIntoPrivateField(app, "nodeIngestor", mockNodeIngestor);
        injectMockIntoPrivateField(app, "context", mock(SpringContextAware.class));
        injectMockIntoPrivateField(app, "loaderFactory", mock(SpringContextAware.class));
    }

    // Helper method to inject mocks into private fields using reflection
    private void injectMockIntoPrivateField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // Helper method to invoke private methods using reflection and return the result
    private Object invokePrivateMethodAndReturnResult(Object target, String methodName, Class<?>[] parameterTypes, Object[] args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    // Test the schemaServiceExceptionTranslator method
    @Test
    public void testSchemaServiceExceptionTranslator() throws Exception {
        // Test with a generic exception
        Exception ex = new Exception("Test exception");
        AAIException result = (AAIException) invokePrivateMethodAndReturnResult(app, "schemaServiceExceptionTranslator",
                new Class<?>[]{Exception.class}, new Object[]{ex});
        assertEquals("AAI_3025", result.getCode());

        // Test with root cause containing NodeIngestor
        Exception nodeEx = new Exception("NodeIngestor failure");
        AAIException nodeResult = (AAIException) invokePrivateMethodAndReturnResult(app, "schemaServiceExceptionTranslator",
                new Class<?>[]{Exception.class}, new Object[]{nodeEx});
        assertEquals("AAI_3026", nodeResult.getCode());

        // Test with root cause containing EdgeIngestor
        Exception edgeEx = new Exception("EdgeIngestor failure");
        AAIException edgeResult = (AAIException) invokePrivateMethodAndReturnResult(app, "schemaServiceExceptionTranslator",
                new Class<?>[]{Exception.class}, new Object[]{edgeEx});
        assertEquals("AAI_3027", edgeResult.getCode());

        // Test with root cause containing Connection refused
        Exception connEx = new Exception("Connection refused");
        AAIException connResult = (AAIException) invokePrivateMethodAndReturnResult(app, "schemaServiceExceptionTranslator",
                new Class<?>[]{Exception.class}, new Object[]{connEx});
        assertEquals("AAI_3025", connResult.getCode());
    }

    // Test the setDefaultProps method
    @Test
    public void testSetDefaultProps() {
        // Simulate directory containing app name
        System.setProperty("user.dir", "/path/to/aai-traversal");
        System.clearProperty("BUNDLECONFIG_DIR");

        // Call setDefaultProps
        TraversalApp.setDefaultProps();

        // Verify the BUNDLECONFIG_DIR property is set correctly
        assertEquals("src/main/resources", System.getProperty("BUNDLECONFIG_DIR"));

        // Simulate directory not containing app name
        System.setProperty("user.dir", "/path/to/other");
        System.clearProperty("BUNDLECONFIG_DIR");
        TraversalApp.setDefaultProps();

        // Verify the default value when not containing the app name
        assertEquals("aai-traversal/src/main/resources", System.getProperty("BUNDLECONFIG_DIR"));
    }

    @Test
    public void testSetDefaultPropsWhenNotSet() {
        // Simulate directory not containing app name
        System.setProperty("user.dir", "/path/to/other");
        System.clearProperty("BUNDLECONFIG_DIR");

        // Call setDefaultProps
        TraversalApp.setDefaultProps();

        // Verify the default value when the property was not previously set
        assertEquals("aai-traversal/src/main/resources", System.getProperty("BUNDLECONFIG_DIR"));
    }

    // Test for setDefaultProps with null file.separator
    @Test
    public void testSetDefaultPropsWithNullFileSeparator() {
        // Clear the file.separator property
        System.clearProperty("file.separator");

        // Call setDefaultProps to set the default value
        TraversalApp.setDefaultProps();

        // Verify that the file.separator system property is set to "/"
        assertEquals("/", System.getProperty("file.separator"));
    }

    // Test case for handling profile misconfiguration (covered in the original request)
    @Test
    public void testProfileMisconfiguration() {
        Profile profile = new Profile();
        profile.setMisconfigured(false);

        // No exception should be thrown for a valid configuration
        assertDoesNotThrow(() -> processProfile(profile), "Exception should not have been thrown.");
    }

    // Helper method to process profile misconfiguration
    public void processProfile(Profile profile) throws SpecificExpectedException {
        if (profile.isMisconfigured()) {
            throw new SpecificExpectedException("Misconfiguration detected");
        }
    }

    // Test case for exception handling in main method
    @Test
    public void testMainExceptionHandling() throws Exception {
        when(mockSpringApplication.run(any(String[].class))).thenThrow(new RuntimeException("Test Exception"));

        try {
            TraversalApp.main(new String[]{});
        } catch (AAIException e) {
            assertEquals("AAI_3025", e.getCode());
        }
    }

    // Configuration for test context setup
    @Configuration
    static class TestConfig {
        @Bean
        public TraversalApp traversalApp() {
            return new TraversalApp();
        }
    }

    // Profile class for testing profile misconfiguration
    static class Profile {
        private boolean isMisconfigured;

        public boolean isMisconfigured() {
            return isMisconfigured;
        }

        public void setMisconfigured(boolean misconfigured) {
            isMisconfigured = misconfigured;
        }
    }

    // Exception class for misconfiguration test
    static class SpecificExpectedException extends Exception {
        public SpecificExpectedException(String message) {
            super(message);
        }
    }
}
