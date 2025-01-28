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

import static org.junit.Assert.assertSame;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.onap.aai.edges.EdgeIngestor;

public class DslQueryBuilderTest {
    DslQueryBuilder dslQueryBuilder;

    @Before
    public void setUp() {
        EdgeIngestor edgeIngestor = new EdgeIngestor(new HashSet<>());
        dslQueryBuilder = new DslQueryBuilder(edgeIngestor, null);
    }

    @Test
    public void testQuery() {
        StringBuilder query = new StringBuilder();
        dslQueryBuilder.setQuery(query);
        assertSame(query, dslQueryBuilder.getQuery());
    }

    @Test
    public void testQueryException() {
        StringBuilder queryException = new StringBuilder();
        dslQueryBuilder.setQueryException(queryException);
        assertSame(queryException, dslQueryBuilder.getQueryException());
    }
}
