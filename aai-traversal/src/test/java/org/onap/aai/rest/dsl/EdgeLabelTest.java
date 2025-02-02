/**
 * ============LICENSE_START==================================================
 * org.onap.aai
 * ===========================================================================
 * Copyright © 2017-2020 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
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
 * ============LICENSE_END====================================================
 */
package org.onap.aai.rest.dsl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EdgeLabelTest {

    private static EdgeLabel edgeLabel;
    private static EdgeLabel edgeLabel1;

    @Before
    public void setUp() throws Exception {
        edgeLabel = new EdgeLabel("label", true);
        edgeLabel1 = new EdgeLabel("org.onap.relationships.inventory.Uses", false);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getLabel() {
        assertEquals(edgeLabel.getLabel(), "label");
    }

    @Test
    public void setLabel() {
        edgeLabel1.setLabel("org.onap.relationships.inventory.Uses");
    }

    @Test
    public void isExactMatch() {
        assertFalse(edgeLabel1.isExactMatch());
    }

    @Test
    public void setExactMatch() {
        edgeLabel1.setExactMatch(false);
    }
}
