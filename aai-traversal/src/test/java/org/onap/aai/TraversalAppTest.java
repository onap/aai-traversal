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
import org.mockito.Mock;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.nodes.NodeIngestor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(
        classes = TraversalApp.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class TraversalAppTest extends AAISetup {

    @Mock
    private NodeIngestor mockNodeIngestor;

    private TraversalApp app;

    static Environment mockEnv = mock(Environment.class);

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        app = new TraversalApp();
        when(mockEnv.getProperty("spring.application.name")).thenReturn("aai-traversal");
        when(mockEnv.getProperty("server.port")).thenReturn("8080");
        injectMockIntoPrivateField(app, "env", mockEnv);
        injectMockIntoPrivateField(app, "nodeIngestor", mockNodeIngestor);
        injectMockIntoPrivateField(app, "context", mock(SpringContextAware.class));
        injectMockIntoPrivateField(app, "loaderFactory", mock(SpringContextAware.class));
    }

    private void injectMockIntoPrivateField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object invokePrivateMethodAndReturnResult(Object target, String methodName, Class<?>[] parameterTypes, Object[] args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    @Test
    public void testSchemaServiceExceptionTranslator() throws Exception {
        Exception ex = new Exception("Test exception");
        AAIException result = (AAIException) invokePrivateMethodAndReturnResult(app, "schemaServiceExceptionTranslator",
                new Class<?>[]{Exception.class}, new Object[]{ex});
        assertEquals("AAI_3025", result.getCode());

        Exception nodeEx = new Exception("NodeIngestor failure");
        AAIException nodeResult = (AAIException) invokePrivateMethodAndReturnResult(app, "schemaServiceExceptionTranslator",
                new Class<?>[]{Exception.class}, new Object[]{nodeEx});
        assertEquals("AAI_3026", nodeResult.getCode());

        Exception edgeEx = new Exception("EdgeIngestor failure");
        AAIException edgeResult = (AAIException) invokePrivateMethodAndReturnResult(app, "schemaServiceExceptionTranslator",
                new Class<?>[]{Exception.class}, new Object[]{edgeEx});
        assertEquals("AAI_3027", edgeResult.getCode());

        Exception connEx = new Exception("Connection refused");
        AAIException connResult = (AAIException) invokePrivateMethodAndReturnResult(app, "schemaServiceExceptionTranslator",
                new Class<?>[]{Exception.class}, new Object[]{connEx});
        assertEquals("AAI_3025", connResult.getCode());
    }

    @Test
    public void testSetDefaultProps() {
        System.setProperty("user.dir", "/path/to/aai-traversal");
        System.clearProperty("BUNDLECONFIG_DIR");
        TraversalApp.setDefaultProps();
        assertEquals("src/main/resources", System.getProperty("BUNDLECONFIG_DIR"));

        System.setProperty("user.dir", "/path/to/other");
        System.clearProperty("BUNDLECONFIG_DIR");
        TraversalApp.setDefaultProps();
        assertEquals("aai-traversal/src/main/resources", System.getProperty("BUNDLECONFIG_DIR"));
    }

    @Test
    public void testSetDefaultPropsWhenNotSet() {
        System.setProperty("user.dir", "/path/to/other");
        System.clearProperty("BUNDLECONFIG_DIR");
        TraversalApp.setDefaultProps();
        assertEquals("aai-traversal/src/main/resources", System.getProperty("BUNDLECONFIG_DIR"));
    }

    @Test
    public void testSetDefaultPropsWithNullFileSeparator() {
        System.clearProperty("file.separator");
        TraversalApp.setDefaultProps();
        assertEquals("/", System.getProperty("file.separator"));
    }
}
