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
package org.onap.aai.rest.search;

import java.util.Map;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.serialization.db.exceptions.NoEdgeRuleFoundException;

public class PserverFromFqdnFirstTokenTest extends QueryTest {

    public PserverFromFqdnFirstTokenTest() throws AAIException, NoEdgeRuleFoundException {
        super();
    }

    @Test
    public void test() {
        super.run();
    }

    @Override
    protected void createGraph() throws AAIException, NoEdgeRuleFoundException {
        // Set up the test graph
        Vertex pserver1 = graph.addVertex(T.label, "pserver", T.id, "1", "aai-node-type", "pserver",
            "hostname", "hostname-10", "fqdn", "fqdn-1.abc.com", "source-of-truth", "RCT");
        Vertex pserver2 = graph.addVertex(T.label, "pserver", T.id, "5", "aai-node-type", "pserver",
            "hostname", "hostname-20", "fqdn", "fqdn-2.abc.com", "source-of-truth", "RCT");
        Vertex pserver3 = graph.addVertex(T.label, "pserver", T.id, "6", "aai-node-type", "pserver",
            "hostname", "hostname-30", "fqdn", "fqdn-13.abc.com", "source-of-truth", "AAIRctFeed");
        Vertex pserver4 = graph.addVertex(T.label, "pserver", T.id, "7", "aai-node-type", "pserver",
            "hostname", "hostname-40", "fqdn", "fqdn-12.abc.com", "source-of-truth", "RO");
        Vertex pserver5 = graph.addVertex(T.label, "pserver", T.id, "8", "aai-node-type", "pserver",
            "hostname", "hostname-50", "fqdn", "fqdn-20.abc.com", "source-of-truth", "RO");

        expectedResult.add(pserver1);
        expectedResult.add(pserver3);

    }

    @Override
    protected String getQueryName() {
        return "pserver-fromFqdnFirstToken";
    }

    @Override
    protected void addStartNode(GraphTraversal<Vertex, Vertex> g) {
        g.has("aai-node-type", "pserver");
    }

    @Override
    protected void addParam(Map<String, Object> params) {
        params.put("fqdnFirstToken", "fqdn-1");
        params.put("sourcesOfTruth", "RCT', 'AAIRctFeed"); // placement of single quotes is
                                                           // intentional, values between the first
                                                           // and last values must be in single
                                                           // quotes

    }

}
