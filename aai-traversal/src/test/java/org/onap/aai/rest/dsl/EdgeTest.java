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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.rest.enums.EdgeDirection;

public class EdgeTest {

    Edge edge;
    Edge edge2;

    @Before
    public void setUp() throws Exception {
        EdgeDirection dir = EdgeDirection.OUT;
        EdgeDirection both = EdgeDirection.BOTH;
        EdgeLabel label = new EdgeLabel("org.onap.relationships.inventory.Uses", true);
        List<EdgeLabel> labelList = new ArrayList<EdgeLabel>();
        edge = new Edge(dir, labelList);
        edge2 = new Edge(both, labelList);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getLabels() {
        assertNotNull(edge.getLabels());
    }

    @Test
    public void setLabels() {
        EdgeLabel label = new EdgeLabel("org.onap.relationships.inventory.Uses", true);
        List<EdgeLabel> labelList = new ArrayList<EdgeLabel>();
        labelList.add(label);
        edge.setLabels(labelList);
    }

    @Test
    public void getDirection() {
        assertEquals(edge2.getDirection(), EdgeDirection.BOTH);
        assertEquals(edge.getDirection(), EdgeDirection.OUT);
    }

    @Test
    public void setDirection() {
        edge.setDirection(EdgeDirection.OUT);
    }

    @Test
    public void testToString() {
        assertNotNull(edge.getDirection().toString());
        assertNotNull(edge.toString());
    }
}
