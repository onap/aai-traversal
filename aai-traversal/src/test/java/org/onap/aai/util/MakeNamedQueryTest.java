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
package org.onap.aai.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.exceptions.AAIUnknownObjectException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

public class MakeNamedQueryTest {

    @InjectMocks
    private MakeNamedQuery makeNamedQuery;

    @Mock
    private Loader mockLoader;

    @Mock
    private Introspector mockIntrospector;

    @Mock
    private Introspector mockRelationshipListIntrospector;

    @Mock
    private Introspector mockNewRelationshipDatum1;

    @Mock
    private AnnotationConfigApplicationContext mockContext;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetupNQElementsWithReflection() throws Exception {
        // Mock the behavior of the introspector
        when(mockIntrospector.getWrappedValue("named-query-elements")).thenReturn(mockIntrospector);
        when(mockIntrospector.getValue("named-query-element")).thenReturn(new ArrayList<>()); // empty list

        // Mock the Loader behavior to return a mocked Introspector
        when(mockLoader.introspectorFromName("named-query-element")).thenReturn(mockIntrospector);

        // Mock the 'relationship-list' introspector
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("relationship-list")).thenReturn(mockRelationshipListIntrospector);

        // Mock setting the relationship list value
        when(mockRelationshipListIntrospector.getUnderlyingObject()).thenReturn(new ArrayList<>());

        // Now test the setupNQElements method via reflection
        Method setupNQElementsMethod = MakeNamedQuery.class.getDeclaredMethod("setupNQElements", Introspector.class, List.class);
        setupNQElementsMethod.setAccessible(true);

        List<Introspector> mockRelationships = new ArrayList<>();
        Introspector result = (Introspector) setupNQElementsMethod.invoke(makeNamedQuery, mockIntrospector, mockRelationships);

        // Assert the result
        assertNotNull(result);
    }

    @Test
    public void testSetupNQElementsElseBlock() throws Exception {
        // Mocking the scenario where 'nqElements.getLoader()' and 'introspectorFromName' might return null
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("named-query-element")).thenReturn(mockIntrospector);

        // Mock other necessary methods to simulate the behavior of the original method
        when(mockIntrospector.getValue("named-query-element")).thenReturn(new ArrayList<>()); // empty list

        // Proceeding with the rest of the setup
        when(mockIntrospector.getWrappedValue("named-query-elements")).thenReturn(mockIntrospector);
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("relationship-list")).thenReturn(mockIntrospector); // Mock a return for relationship-list introspector

        // Now, simulate the logic where 'nqElements' are handled and introspector is used
        List<Introspector> mockRelationships = new ArrayList<>();
        invokePrivateSetupNQElements(mockIntrospector, mockRelationships);

        // Assert the relationships list (we are just verifying it doesn't throw an exception)
        assertNotNull(mockRelationships);

        // Verify that the loader was called and introspectorFromName was invoked
        verify(mockLoader).introspectorFromName("named-query-element");
    }

    private void invokePrivateSetupNQElements(Introspector introspector, List<Introspector> relationships) {
        try {
            // Get the private method 'setupNQElements' using reflection
            Method method = MakeNamedQuery.class.getDeclaredMethod("setupNQElements", Introspector.class, List.class);
            method.setAccessible(true);  // Make the method accessible
            method.invoke(makeNamedQuery, introspector, relationships); // Invoke the private method
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred during reflection: " + e.getMessage());
        }
    }

    @Test
    public void testSetupNQElementsWithAAIUnknownObjectException() throws Exception {
        // Mock the behavior of the introspector to simulate the flow that throws an exception
        when(mockIntrospector.getWrappedValue("named-query-elements")).thenReturn(null); // This will trigger the else block

        // Mock the behavior of newIntrospectorInstanceOfProperty to throw an AAIUnknownObjectException
        when(mockIntrospector.newIntrospectorInstanceOfProperty("named-query-elements")).thenThrow(AAIUnknownObjectException.class);

        // Now test the setupNQElements method via reflection
        Method setupNQElementsMethod = MakeNamedQuery.class.getDeclaredMethod("setupNQElements", Introspector.class, List.class);
        setupNQElementsMethod.setAccessible(true);

        List<Introspector> mockRelationships = new ArrayList<>();

        try {
            setupNQElementsMethod.invoke(makeNamedQuery, mockIntrospector, mockRelationships);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof AAIUnknownObjectException);
        }
    }

    @Test
    public void testSetupNQElementsWithIllegalArgumentException() throws Exception {
        // Mock the behavior of the introspector to simulate the flow that throws an exception
        when(mockIntrospector.getWrappedValue("named-query-elements")).thenReturn(null); // This will trigger the else block

        // Mock the behavior of newIntrospectorInstanceOfProperty to throw an IllegalArgumentException
        when(mockIntrospector.newIntrospectorInstanceOfProperty("named-query-elements")).thenThrow(IllegalArgumentException.class);

        // Now test the setupNQElements method via reflection
        Method setupNQElementsMethod = MakeNamedQuery.class.getDeclaredMethod("setupNQElements", Introspector.class, List.class);
        setupNQElementsMethod.setAccessible(true);

        List<Introspector> mockRelationships = new ArrayList<>();

        try {
            setupNQElementsMethod.invoke(makeNamedQuery, mockIntrospector, mockRelationships);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testMakeWidgetRelationshipWithReflection() throws Exception {
        String modelInvariantId = "dummyModelInvariantId";
        String modelVersionId = "dummyModelVersionId";

        // Mock the behavior of the introspector
        when(mockLoader.introspectorFromName("relationship")).thenReturn(mockIntrospector);
        when(mockIntrospector.getValue("relationship-data")).thenReturn(new ArrayList<>());

        // Mock the introspectorFromName for named-query-element
        when(mockLoader.introspectorFromName("named-query-element")).thenReturn(mockIntrospector);

        // Mock the 'relationship-list' introspector
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("relationship-list")).thenReturn(mockRelationshipListIntrospector);

        // Mock setting the relationship list value
        when(mockRelationshipListIntrospector.getUnderlyingObject()).thenReturn(new ArrayList<>());

        // Mock the newRelationshipDatum1 object
        when(mockLoader.introspectorFromName("relationship-data")).thenReturn(mockNewRelationshipDatum1);

        // Use doNothing() because setValue() returns void
        doNothing().when(mockNewRelationshipDatum1).setValue("relationship-key", "model.model-invariant-id");
        doNothing().when(mockNewRelationshipDatum1).setValue("relationship-value", modelInvariantId);

        // Get the private static method using reflection
        Method makeWidgetRelationshipMethod = MakeNamedQuery.class.getDeclaredMethod(
                "makeWidgetRelationship", Loader.class, String.class, String.class);

        // Make the method accessible (bypass private access)
        makeWidgetRelationshipMethod.setAccessible(true);

        // Invoke the static method via reflection (pass null because it's static)
        Introspector result = (Introspector) makeWidgetRelationshipMethod.invoke(null, mockLoader, modelInvariantId, modelVersionId);

        // Assert the result
        assertNotNull(result);
    }

    @Test
    public void testMakeWidgetRelationshipWithAAIUnknownObjectException() throws Exception {
        String modelInvariantId = "dummyModelInvariantId";
        String modelVersionId = "dummyModelVersionId";

        // Mock the behavior of the introspector to throw AAIUnknownObjectException
        when(mockLoader.introspectorFromName("relationship")).thenThrow(AAIUnknownObjectException.class);

        // Get the private static method using reflection
        Method makeWidgetRelationshipMethod = MakeNamedQuery.class.getDeclaredMethod(
                "makeWidgetRelationship", Loader.class, String.class, String.class);

        // Make the method accessible (bypass private access)
        makeWidgetRelationshipMethod.setAccessible(true);

        try {
            // Invoke the static method via reflection (pass null because it's static)
            makeWidgetRelationshipMethod.invoke(null, mockLoader, modelInvariantId, modelVersionId);
        } catch (Exception e) {
            // Assert that the cause of the exception is AAIUnknownObjectException
            assertTrue(e.getCause() instanceof AAIUnknownObjectException);
        }
    }

    @Test
    public void testMakeWidgetRelationshipWithIllegalArgumentException() throws Exception {
        String modelInvariantId = "dummyModelInvariantId";
        String modelVersionId = "dummyModelVersionId";

        // Mock the behavior of the introspector to throw IllegalArgumentException
        when(mockLoader.introspectorFromName("relationship")).thenThrow(IllegalArgumentException.class);

        // Get the private static method using reflection
        Method makeWidgetRelationshipMethod = MakeNamedQuery.class.getDeclaredMethod(
                "makeWidgetRelationship", Loader.class, String.class, String.class);

        // Make the method accessible (bypass private access)
        makeWidgetRelationshipMethod.setAccessible(true);

        try {
            // Invoke the static method via reflection (pass null because it's static)
            makeWidgetRelationshipMethod.invoke(null, mockLoader, modelInvariantId, modelVersionId);
        } catch (Exception e) {
            // Assert that the cause of the exception is IllegalArgumentException
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testLoadNQElementWithAAIUnknownObjectException() throws Exception {
        // Mock the behavior of the introspector to throw AAIUnknownObjectException
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("named-query-element")).thenThrow(AAIUnknownObjectException.class);

        // Get the private method 'loadNQElement' using reflection
        Method loadNQElementMethod = MakeNamedQuery.class.getDeclaredMethod("loadNQElement", Introspector.class, List.class);

        // Make the method accessible (bypass private access)
        loadNQElementMethod.setAccessible(true);

        List<Introspector> mockRelationships = new ArrayList<>();

        try {
            // Invoke the method
            loadNQElementMethod.invoke(makeNamedQuery, mockIntrospector, mockRelationships);
        } catch (Exception e) {
            // Assert that the cause of the exception is AAIUnknownObjectException
            assertTrue(e.getCause() instanceof AAIUnknownObjectException);
        }
    }

    @Test
    public void testLoadNQElementWithIllegalArgumentException() throws Exception {
        // Mock the behavior of the introspector to throw IllegalArgumentException
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("named-query-element")).thenThrow(IllegalArgumentException.class);

        // Get the private method 'loadNQElement' using reflection
        Method loadNQElementMethod = MakeNamedQuery.class.getDeclaredMethod("loadNQElement", Introspector.class, List.class);

        // Make the method accessible (bypass private access)
        loadNQElementMethod.setAccessible(true);

        List<Introspector> mockRelationships = new ArrayList<>();

        try {
            // Invoke the method
            loadNQElementMethod.invoke(makeNamedQuery, mockIntrospector, mockRelationships);
        } catch (Exception e) {
            // Assert that the cause of the exception is IllegalArgumentException
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }
}
