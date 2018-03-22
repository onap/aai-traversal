/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
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
