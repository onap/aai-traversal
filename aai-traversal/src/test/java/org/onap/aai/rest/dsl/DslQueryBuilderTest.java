/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2019 IBM.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.rest.dsl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.introspection.Loader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DslQueryBuilderTest {

    DslQueryBuilder dslQueryBuilder;

    @BeforeEach
    void setUp() {
        EdgeIngestor edgeIngestor = new EdgeIngestor(new HashSet<>());
        dslQueryBuilder = new DslQueryBuilder(edgeIngestor, null);
    }

    @Test
    void testQuery() {
        StringBuilder query = new StringBuilder();
        dslQueryBuilder.setQuery(query);
        assertSame(query, dslQueryBuilder.getQuery());
    }

    @Test
    void testQueryException() {
        StringBuilder queryException = new StringBuilder();
        dslQueryBuilder.setQueryException(queryException);
        assertSame(queryException, dslQueryBuilder.getQueryException());
    }

    @Test
    void testEndInstance() {
        DslQueryBuilder result = dslQueryBuilder.endInstance();
        assertSame(result, dslQueryBuilder);
    }

    @Test
    void testTrimSingleQuotes() throws Exception {
        EdgeIngestor edgeIngestor = new EdgeIngestor(new HashSet<>());
        DslQueryBuilder dslQueryBuilder = new DslQueryBuilder(edgeIngestor, null);

        Method trimSingleQuotesMethod = DslQueryBuilder.class.getDeclaredMethod("trimSingleQuotes", String.class);
        trimSingleQuotesMethod.setAccessible(true);

        assertEquals("value", trimSingleQuotesMethod.invoke(dslQueryBuilder, "'value'"));
        assertEquals("value", trimSingleQuotesMethod.invoke(dslQueryBuilder, "value"));
        assertEquals("value", trimSingleQuotesMethod.invoke(dslQueryBuilder, "value"));
        assertEquals("", trimSingleQuotesMethod.invoke(dslQueryBuilder, "''"));
        assertEquals("", trimSingleQuotesMethod.invoke(dslQueryBuilder, ""));
    }

    @Test
    void testTrimSingleQuotesEdgeCases() throws Exception {
        EdgeIngestor edgeIngestor = new EdgeIngestor(new HashSet<>());
        DslQueryBuilder dslQueryBuilder = new DslQueryBuilder(edgeIngestor, null);

        Method trimSingleQuotesMethod = DslQueryBuilder.class.getDeclaredMethod("trimSingleQuotes", String.class);
        trimSingleQuotesMethod.setAccessible(true);

        Object result = trimSingleQuotesMethod.invoke(dslQueryBuilder, (Object) null);
        assertNull(result);

        result = trimSingleQuotesMethod.invoke(dslQueryBuilder, "");
        assertEquals("", result);
    }

    @Test
    void testFilterPropertyStartWithTrueBoolean() {
        EdgeIngestor edgeIngestorMock = mock(EdgeIngestor.class);
        Loader loaderMock = mock(Loader.class);

        DslQueryBuilder dslQueryBuilder = new DslQueryBuilder(edgeIngestorMock, loaderMock);

        List<String> values = new ArrayList<>();
        values.add("true");

        StringBuilder query = new StringBuilder();
        dslQueryBuilder.filterPropertyStart(false, values);
        assertFalse(query.toString().contains(".getVerticesByBooleanProperty("));
    }
}
