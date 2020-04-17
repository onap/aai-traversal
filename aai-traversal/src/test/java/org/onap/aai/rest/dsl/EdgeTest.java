package org.onap.aai.rest.dsl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.rest.enums.EdgeDirection;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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