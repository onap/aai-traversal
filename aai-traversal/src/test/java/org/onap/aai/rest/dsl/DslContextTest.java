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

import static org.junit.Assert.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DslContextTest {
    DslContext dslContext;

    @Before
    public void setUp() {

        dslContext = new DslContext();
        dslContext.setCtx(null);
    }

    @Test
    public void testGetCtx() {
        assertNull(dslContext.getCtx());
    }

    @Test
    public void testSetUnionStart() {
        dslContext.setUnionStart(true);
    }

    @Test
    public void testGetLimitQuery() {
        assertNotNull(dslContext.getLimitQuery());
    }

    @Test
    public void testSetLimitQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append("abc");
        dslContext.setLimitQuery(builder);
        dslContext.setStartNodeFlag(true);
        dslContext.setUnionStart(true);
        dslContext.setUnionStartNodes(null);
        assertNotNull(dslContext);
    }

    @Test
    public void testIsStartNode() {
        Assert.assertFalse(dslContext.isStartNode());
    }

    @Test
    public void testGetStartNode() {
        dslContext.setStartNode("xyz");
        Assert.assertEquals(dslContext.getStartNode(), "xyz");
    }

    @Test
    public void testGetStartNodeKeys() {
        Assert.assertNotNull(dslContext.getStartNodeKeys());
    }

    @Test
    public void testGetCurrentNode() {
        dslContext.setCurrentNode("blah");
        Assert.assertEquals(dslContext.getCurrentNode(), "blah");
    }

    @Test
    public void testGetPreviousNode() {
        dslContext.setPreviousNode("blah");
        Assert.assertEquals(dslContext.getPreviousNode(), "blah");
    }

    @Test
    public void testisTraversal() {
        dslContext.setTraversal(false);
        Assert.assertFalse(dslContext.isTraversal());
    }

    @Test
    public void testGetWhereQuery() {
        dslContext.setWhereQuery(true);
        Assert.assertTrue(dslContext.isWhereQuery());
    }

    @Test
    public void testIsUnionQuery() {
        dslContext.setUnionQuery(true);
        Assert.assertTrue(dslContext.isUnionQuery());
    }

    @Test
    public void testIsUnionStart() {
        dslContext.setUnionStart(true);
        Assert.assertTrue(dslContext.isUnionStart());
    }

    @Test
    public void testUnionStart() throws Exception {
        assertNotNull(dslContext.getUnionStartNodes());
    }

    @Test
    public void testGetWhereStartNode() {
        dslContext.setWhereStartNode("blah");
        Assert.assertEquals(dslContext.getWhereStartNode(), "blah");
    }

    @Test
    public void testIsValidationFlag() {
        Assert.assertTrue(dslContext.isValidationFlag());
    }

    @Test
    public void testSetValidationFlag() {
        dslContext.setValidationFlag(true);
        Assert.assertTrue(dslContext.isValidationFlag());
    }

    @Test
    public void testUnionStartNodes() {
        dslContext.setUnionStartNodes(null);
        assertNull(dslContext.getUnionStartNodes());
    }
}
