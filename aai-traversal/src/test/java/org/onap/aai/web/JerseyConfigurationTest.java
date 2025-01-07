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
package org.onap.aai.web;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class JerseyConfigurationTest {

    private JerseyConfiguration jerseyConfiguration;

    @Mock
    private Environment environment;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jerseyConfiguration = new JerseyConfiguration(environment);
    }

    @Test
    public void testIsLoggingEnabled() {
        // Given: Mock the environment property
        when(environment.getProperty("aai.request.logging.enabled")).thenReturn("true");

        // When: Call the method
        boolean result = (boolean) invokePrivateMethod("isLoggingEnabled");

        // Then: Verify the result is true
        assertTrue(result);

        // Given: Mock the environment property to false
        when(environment.getProperty("aai.request.logging.enabled")).thenReturn("false");

        // When: Call the method
        result = (boolean) invokePrivateMethod("isLoggingEnabled");

        // Then: Verify the result is false
        assertFalse(result);
    }

    // Helper method to invoke private methods using reflection
    private Object invokePrivateMethod(String methodName, Object... params) {
        try {
            Method method = JerseyConfiguration.class.getDeclaredMethod(methodName, getParameterTypes(params));
            method.setAccessible(true);
            return method.invoke(jerseyConfiguration, params);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method", e);
        }
    }
    // Helper method to get parameter types from the params
    private Class<?>[] getParameterTypes(Object[] params) {
        if (params == null) {
            return new Class<?>[0];
        }
        Class<?>[] types = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            types[i] = params[i].getClass();
        }
        return types;
    }
}