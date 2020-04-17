package org.onap.aai.rest.dsl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EdgeLabelTest {

    private static EdgeLabel edgeLabel;
    private static EdgeLabel edgeLabel1;

    @Before
    public void setUp() throws Exception {
        edgeLabel =  new EdgeLabel("label", true);
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