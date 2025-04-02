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
package org.onap.aai.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSetupNQElementsWithReflection() throws Exception {
        when(mockIntrospector.getWrappedValue("named-query-elements")).thenReturn(mockIntrospector);
        when(mockIntrospector.getValue("named-query-element")).thenReturn(new ArrayList<>());
        when(mockLoader.introspectorFromName("named-query-element")).thenReturn(mockIntrospector);
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("relationship-list")).thenReturn(mockRelationshipListIntrospector);
        when(mockRelationshipListIntrospector.getUnderlyingObject()).thenReturn(new ArrayList<>());

        Method setupNQElementsMethod = MakeNamedQuery.class.getDeclaredMethod("setupNQElements", Introspector.class, List.class);
        setupNQElementsMethod.setAccessible(true);

        List<Introspector> mockRelationships = new ArrayList<>();
        Introspector result = (Introspector) setupNQElementsMethod.invoke(makeNamedQuery, mockIntrospector, mockRelationships);

        assertNotNull(result);
    }

    @Test
    public void testSetupNQElementsElseBlock() throws Exception {
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("named-query-element")).thenReturn(mockIntrospector);
        when(mockIntrospector.getValue("named-query-element")).thenReturn(new ArrayList<>());
        when(mockIntrospector.getWrappedValue("named-query-elements")).thenReturn(mockIntrospector);
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("relationship-list")).thenReturn(mockIntrospector);

        List<Introspector> mockRelationships = new ArrayList<>();
        invokePrivateSetupNQElements(mockIntrospector, mockRelationships);

        assertNotNull(mockRelationships);

        verify(mockLoader).introspectorFromName("named-query-element");
    }

    private void invokePrivateSetupNQElements(Introspector introspector, List<Introspector> relationships) {
        try {
            Method method = MakeNamedQuery.class.getDeclaredMethod("setupNQElements", Introspector.class, List.class);
            method.setAccessible(true);
            method.invoke(makeNamedQuery, introspector, relationships);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred during reflection: " + e.getMessage());
        }
    }

    @Test
    public void testSetupNQElementsWithAAIUnknownObjectException() throws Exception {
        when(mockIntrospector.getWrappedValue("named-query-elements")).thenReturn(null);
        when(mockIntrospector.newIntrospectorInstanceOfProperty("named-query-elements")).thenThrow(AAIUnknownObjectException.class);

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
        when(mockIntrospector.getWrappedValue("named-query-elements")).thenReturn(null);
        when(mockIntrospector.newIntrospectorInstanceOfProperty("named-query-elements")).thenThrow(IllegalArgumentException.class);

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

        when(mockLoader.introspectorFromName("relationship")).thenReturn(mockIntrospector);
        when(mockIntrospector.getValue("relationship-data")).thenReturn(new ArrayList<>());
        when(mockLoader.introspectorFromName("named-query-element")).thenReturn(mockIntrospector);
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("relationship-list")).thenReturn(mockRelationshipListIntrospector);
        when(mockRelationshipListIntrospector.getUnderlyingObject()).thenReturn(new ArrayList<>());
        when(mockLoader.introspectorFromName("relationship-data")).thenReturn(mockNewRelationshipDatum1);

        doNothing().when(mockNewRelationshipDatum1).setValue("relationship-key", "model.model-invariant-id");
        doNothing().when(mockNewRelationshipDatum1).setValue("relationship-value", modelInvariantId);

        Method makeWidgetRelationshipMethod = MakeNamedQuery.class.getDeclaredMethod(
                "makeWidgetRelationship", Loader.class, String.class, String.class);

        makeWidgetRelationshipMethod.setAccessible(true);

        Introspector result = (Introspector) makeWidgetRelationshipMethod.invoke(null, mockLoader, modelInvariantId, modelVersionId);

        assertNotNull(result);
    }

    @Test
    public void testMakeWidgetRelationshipWithAAIUnknownObjectException() throws Exception {
        String modelInvariantId = "dummyModelInvariantId";
        String modelVersionId = "dummyModelVersionId";

        when(mockLoader.introspectorFromName("relationship")).thenThrow(AAIUnknownObjectException.class);

        Method makeWidgetRelationshipMethod = MakeNamedQuery.class.getDeclaredMethod(
                "makeWidgetRelationship", Loader.class, String.class, String.class);

        makeWidgetRelationshipMethod.setAccessible(true);

        try {
            makeWidgetRelationshipMethod.invoke(null, mockLoader, modelInvariantId, modelVersionId);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof AAIUnknownObjectException);
        }
    }

    @Test
    public void testMakeWidgetRelationshipWithIllegalArgumentException() throws Exception {
        String modelInvariantId = "dummyModelInvariantId";
        String modelVersionId = "dummyModelVersionId";

        when(mockLoader.introspectorFromName("relationship")).thenThrow(IllegalArgumentException.class);

        Method makeWidgetRelationshipMethod = MakeNamedQuery.class.getDeclaredMethod(
                "makeWidgetRelationship", Loader.class, String.class, String.class);

        makeWidgetRelationshipMethod.setAccessible(true);

        try {
            makeWidgetRelationshipMethod.invoke(null, mockLoader, modelInvariantId, modelVersionId);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testLoadNQElementWithAAIUnknownObjectException() throws Exception {
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("named-query-element")).thenThrow(AAIUnknownObjectException.class);

        Method loadNQElementMethod = MakeNamedQuery.class.getDeclaredMethod("loadNQElement", Introspector.class, List.class);

        loadNQElementMethod.setAccessible(true);

        List<Introspector> mockRelationships = new ArrayList<>();

        try {
            loadNQElementMethod.invoke(makeNamedQuery, mockIntrospector, mockRelationships);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof AAIUnknownObjectException);
        }
    }

    @Test
    public void testLoadNQElementWithIllegalArgumentException() throws Exception {
        when(mockIntrospector.getLoader()).thenReturn(mockLoader);
        when(mockLoader.introspectorFromName("named-query-element")).thenThrow(IllegalArgumentException.class);

        Method loadNQElementMethod = MakeNamedQuery.class.getDeclaredMethod("loadNQElement", Introspector.class, List.class);

        loadNQElementMethod.setAccessible(true);

        List<Introspector> mockRelationships = new ArrayList<>();

        try {
            loadNQElementMethod.invoke(makeNamedQuery, mockIntrospector, mockRelationships);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }
}
