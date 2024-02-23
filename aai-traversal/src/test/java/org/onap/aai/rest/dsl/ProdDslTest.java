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

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.onap.aai.AAISetup;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.enums.QueryVersion;
import org.springframework.beans.factory.annotation.Autowired;

// TODO: Change this to read queries and their builder equivalent from a file
// TODO: Add queries run by SEs

public class ProdDslTest extends AAISetup {

    @Autowired
    protected V1DslQueryProcessor dslQueryProcessor;

    @Ignore
    @Test
    public void msoQueryTest1() throws AAIException {
        String aaiQuery =
            "cloud-region('cloud-owner', 'value')('cloud-region-id', 'value') > vlan-range > vlan-tag*('vlan-id-outer', '123')";

        String dslQuery =
            "builder.getVerticesByProperty('aai-node-type', 'cloud-region').getVerticesByProperty('cloud-owner','value')"
                + ".getVerticesByProperty('cloud-region-id','value').createEdgeTraversal(EdgeType.TREE, 'cloud-region','vlan-range').createEdgeTraversal(EdgeType.TREE, 'vlan-range','vlan-tag')"
                + ".getVerticesByProperty('vlan-id-outer',123).store('x').cap('x').unfold().dedup()";

        String query =
            dslQueryProcessor.parseAaiQuery(QueryVersion.V1, aaiQuery).get("query").toString();
        assertEquals(dslQuery, query);
    }

    @Test
    public void msoQueryTest2() throws AAIException {
        String aaiQuery =
            "pserver('hostname', 'pserver-1') > p-interface > sriov-pf*('pf-pci-id', '0000:ee:00.0')";

        String dslQuery =
            "builder.getVerticesByProperty('aai-node-type', 'pserver').getVerticesByProperty('hostname','pserver-1')"
                + ".createEdgeTraversal(EdgeType.TREE, 'pserver','p-interface')"
                + ".createEdgeTraversal(EdgeType.TREE, 'p-interface','sriov-pf').getVerticesByProperty('pf-pci-id','0000:ee:00.0').store('x').cap('x').unfold().dedup()";

        String query =
            dslQueryProcessor.parseAaiQuery(QueryVersion.V1, aaiQuery).get("query").toString();
        assertEquals(dslQuery, query);
    }

    @Test
    public void msoQueryTest3() throws AAIException {
        String aaiQuery = "l-interface ('interface-id', 'value') > sriov-vf > sriov-pf*";

        String dslQuery =
            "builder.getVerticesByProperty('aai-node-type', 'l-interface').getVerticesByProperty('interface-id','value')"
                + ".createEdgeTraversal(EdgeType.TREE, 'l-interface','sriov-vf')"
                + ".createEdgeTraversal(EdgeType.COUSIN, 'sriov-vf','sriov-pf').store('x').cap('x').unfold().dedup()";

        String query =
            dslQueryProcessor.parseAaiQuery(QueryVersion.V1, aaiQuery).get("query").toString();
        assertEquals(dslQuery, query);
    }

    // TODO : Get this from schema
    @Test
    public void msoQueryTest4() throws AAIException {
        // String aaiQuery = "l-interface ('interface-id', 'value') >
        // lag-interface('interface-name', 'bond1') > sriov-pf*";
        String aaiQuery =
            "l-interface ('interface-id', 'value') > lag-interface('interface-name', 'bond1') > p-interface > sriov-pf*";

        String dslQuery =
            "builder.getVerticesByProperty('aai-node-type', 'l-interface').getVerticesByProperty('interface-id','value')"
                + ".createEdgeTraversal( 'l-interface','lag-interface').getVerticesByProperty('interface-name','bond1')"
                + ".createEdgeTraversal(EdgeType.COUSIN, 'lag-interface','p-interface').createEdgeTraversal(EdgeType.TREE, 'p-interface','sriov-pf').store('x').cap('x').unfold().dedup()";

        String query =
            dslQueryProcessor.parseAaiQuery(QueryVersion.V1, aaiQuery).get("query").toString();
        assertEquals(dslQuery, query);
    }

    // TODO : Get this from schema
    @Test
    public void msoQueryTest5() throws AAIException {
        // String aaiQuery = "pserver ('hostname', 'value') > vserver ('vserver-name', 'value') >
        // l-interface > vlan-tag*";
        String aaiQuery =
            "pserver ('hostname', 'value') > vserver ('vserver-name', 'value') > l-interface > cp > vlan-tag*";
        String dslQuery =
            "builder.getVerticesByProperty('aai-node-type', 'pserver').getVerticesByProperty('hostname','value')"
                + ".createEdgeTraversal(EdgeType.COUSIN, 'pserver','vserver').getVerticesByProperty('vserver-name','value')"
                + ".createEdgeTraversal(EdgeType.TREE, 'vserver','l-interface').createEdgeTraversal(EdgeType.COUSIN, 'l-interface','cp').createEdgeTraversal(EdgeType.COUSIN, 'cp','vlan-tag').store('x').cap('x').unfold().dedup()";

        String query =
            dslQueryProcessor.parseAaiQuery(QueryVersion.V1, aaiQuery).get("query").toString();
        assertEquals(dslQuery, query);
    }

}
