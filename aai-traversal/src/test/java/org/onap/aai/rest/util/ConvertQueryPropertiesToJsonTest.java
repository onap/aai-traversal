package org.onap.aai.rest.util;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;

public class ConvertQueryPropertiesToJsonTest {
    @Test
    public void testRqdProperty(){

        ConvertQueryPropertiesToJson convert = new ConvertQueryPropertiesToJson();
        Properties props = new Properties();
        props.setProperty("queryName1", "builder.getVerticesByProperty('rqdProp', rqdPropId).getVerticesByProperty('rqdProp2', rqdPropId2).createEdgeTraversal(EdgeType.TREE, 'node1', 'child-node1')");
        props.setProperty("lastQueryName", "builder.getVerticesByProperty('notRqdProp', \"OUT\").createEdgeTraversal(EdgeType.TREE, 'node2', 'child-node2')");
        String json = convert.convertProperties(props);
        assertNotNull(json);
    }
 
    @Test
    public void testLastQueryRqdProperty(){

        ConvertQueryPropertiesToJson convert = new ConvertQueryPropertiesToJson();
        Properties props = new Properties();
        props.setProperty("queryName1", "builder.createEdgeTraversal(EdgeType.TREE, 'node1', 'child-node1')");
        props.setProperty("lastQueryName", "builder.getVerticesByProperty('rqdProp', rqdPropId).createEdgeTraversal(EdgeType.TREE, 'node2', 'child-node2')");
        String json = convert.convertProperties(props);
        assertNotNull(json);
    }
}
