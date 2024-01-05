/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2023 Deutsche Telekom. All rights reserved.
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

package org.onap.aai.rest.queryprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.ElementHelper;
import org.janusgraph.core.JanusGraphTransaction;
import org.janusgraph.graphdb.vertices.CacheVertex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.AbstractSpringRestTest;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.dsl.DslQueryProcessor;
import org.onap.aai.rest.enums.QueryVersion;
import org.onap.aai.rest.search.GenericQueryProcessor;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.rest.search.QueryProcessorType;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.serialization.engines.JanusGraphDBEngine;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.onap.aai.setup.SchemaVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;

public class DslQueryProcessorTest extends AbstractSpringRestTest {
    
    private static final QueryProcessorType processorType = QueryProcessorType.LOCAL_GROOVY;
    private static final QueryVersion dslApiVersion = QueryVersion.V1;
    // private static final Format format = Format.console;

    @Autowired private GremlinServerSingleton gremlinServerSingleton;
    @Autowired private DslQueryProcessor dslQueryProcessor;
    @Autowired private HttpEntry traversalUriHttpEntry;
    private GenericQueryProcessor queryProcessor;

    private GraphTraversalSource traversal;

    @Before
    public void initTraversal() {
        traversal = AAIGraph.getInstance().getGraph().traversal();
    }

    @Override
    public void createTestGraph() {
        // JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        JanusGraphTransaction transaction = AAIGraph.getInstance().getGraph().newTransaction();
        boolean success = true;
        try {
            GraphTraversalSource g = transaction.traversal();
            Vertex p1 = g.addV().property("aai-node-type", "pserver")
                .property("hostname", "test-pserver-dsl").property("in-maint", false)
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver-dsl")
                .next();
            Vertex p2 = g.addV().property("aai-node-type", "pserver")
                .property("hostname", "test-pserver-dsl-02").property("in-maint", false)
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver-dsl-02")
                .next();
            Vertex p3 = g.addV().property("aai-node-type", "pserver")
                .property("hostname", "test-pserver-dsl-03").property("in-maint", false)
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver-dsl-03")
                .next();
            Vertex p4 = g.addV().property("aai-node-type", "pserver")
                .property("hostname", "test-pserver-dsl-04").property("in-maint", false)
                .property("source-of-truth", "JUNIT").property("number-of-cpus", 364)
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/pservers/pserver/test-pserver-dsl-04")
                .next();
            Vertex c1 = g.addV().property("aai-node-type", "complex")
                .property("physical-location-id", "test-complex-dsl").property("state", "NJ")
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/cloud-infrastructure/complexes/complex/test-complex-dsl")
                .next();
            Vertex cr1 = g.addV().property("aai-node-type", "cloud-region")
                .property("cloud-owner", "test-cloud-owner-01")
                .property("cloud-region-id", "test-cloud-region-id-01")
                .property("source-of-truth", "JUNIT")
                .property("aai-uri",
                    "/cloud-infrastructure/cloud-regions/cloud-region/test-cloud-owner-01/test-cloud-region-id-01")
                .next();
            Vertex pnf01 =
                g.addV().property("aai-node-type", "pnf").property("pnf-name", "test-pnf-name-01")
                    .property("in-maint", false).property("source-of-truth", "JUNIT")
                    .property("aai-uri", "/network/pnfs/pnf/test-pnf-name-01").next();
            Vertex vserver2 = g.addV().property("aai-node-type", "vserver")
                .property("vserver-id", "test-vserver-id-2")
                .property("vserver-name", "test-vserver-name-2").property("in-maint", "false")
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/vservers/vserver/test-vserver-id-2").next();
            Vertex tenant2 = g.addV().property("aai-node-type", "tenant")
                .property("tenant-id", "test-tenant-id-2")
                .property("tenant-name", "test-tenant-name-2").property("source-of-truth", "JUNIT")
                .property("aai-uri", "/tenants/tenant/test-tenant-id-2").next();
            Vertex linterface2 = g.addV().property("aai-node-type", "l-interface")
                .property("interface-name", "test-interface-name-02").property("priority", "123")
                .property("is-port-mirrored", "true").property("source-of-truth", "JUNIT")
                .property("aai-uri", "/l-interfaces/l-interface/test-interface-name-02").next();
            Vertex oamNetwork2 = g.addV().property("aai-node-type", "oam-network")
                .property("network-uuid", "test-network-uuid-02")
                .property("network-name", "test-network-name-02").property("cvlan-tag", "456")
                .property("source-of-truth", "JUNIT")
                .property("aai-uri", "/oam-networks/oam-network/test-network-uuid-02").next();
            Vertex cr2 = g.addV().property("aai-node-type", "cloud-region")
                .property("cloud-owner", "test-cloud-owner-02")
                .property("cloud-region-id", "test-cloud-region-id-02")
                .property("source-of-truth", "JUNIT")
                .property("aai-uri",
                    "/cloud-infrastructure/cloud-regions/cloud-region/test-cloud-owner-02/test-cloud-region-id-02")
                .next();

            // For adding edges, check the dbedgetules and the property from and to node
            // along with the other properties to populate information
            p1.addEdge("org.onap.relationships.inventory.LocatedIn", c1, "private", false,
                "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v", "NONE",
                "default", true);
            p1.addEdge("org.onap.relationships.inventory.LocatedIn", cr1, "private", false,
                "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v", "NONE",
                "default", true);
            p3.addEdge("org.onap.relationships.inventory.LocatedIn", c1, "private", false,
                "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v", "NONE",
                "default", true);
            p4.addEdge("org.onap.relationships.inventory.LocatedIn", c1, "private", false,
                "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v", "NONE",
                "default", true);
            tenant2.addEdge("org.onap.relationships.inventory.BelongsTo", cr2, "private", false,
                "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v", "NONE",
                "default", true);
            vserver2.addEdge("org.onap.relationships.inventory.BelongsTo", tenant2, "private",
                false, "prevent-delete", "NONE", "delete-other-v", "NONE", "contains-other-v",
                "NONE", "default", true);
            linterface2.addEdge("tosca.relationships.network.BindsTo", vserver2, "direction", "OUT",
                "multiplicity", "MANY2ONE", "contains-other-v", "!OUT", "delete-other-v", "!OUT",
                "prevent-delete", "NONE", "default", true);
            oamNetwork2.addEdge("org.onap.relationships.inventory.BelongsTo", cr2, "direction",
                "OUT", "multiplicity", "MANY2ONE", "contains-other-v", "!OUT", "delete-other-v",
                "NONE", "prevent-delete", "!OUT", "default", true);

        } catch (Exception ex) {
            success = false;
        } finally {
            if (success) {
                transaction.commit();
            } else {
                transaction.rollback();
                fail("Unable to setup the graph");
            }
        }
    }

    @Test
    public void executeDslQueryWithNoOpSubgraphStyle() throws FileNotFoundException, AAIException {
        traversalUriHttpEntry.setHttpEntryProperties(new SchemaVersion("v21"), "smth");
        final TransactionalGraphEngine graphEngine = traversalUriHttpEntry.getDbEngine();
        final GraphTraversalSource traversalSource = graphEngine.asAdmin().getTraversalSource();
        
        String dsl = "pserver*('hostname','test-pserver-dsl')";
        
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.add("format", Format.resource.toString());

        GenericQueryProcessor queryProcessor = new GenericQueryProcessor.Builder(graphEngine, gremlinServerSingleton)
            .queryFrom(dsl, "dsl").queryProcessor(dslQueryProcessor).version(dslApiVersion)
            .processWith(processorType).format(Format.resource).uriParams(queryParameters)
            .traversalSource(false, traversalSource)
            .create();

        List<Object> vertTemp = queryProcessor.execute(SubGraphStyle.no_op);
        
        Vertex vertex = (Vertex) vertTemp.get(0);
        Vertex pServer = traversal.V().has("hostname", "test-pserver-dsl").next();
        assertEquals(1, vertTemp.size());
        assertTrue(ElementHelper.areEqual(pServer, vertex));
        // assertNotNull(queryProcessor.getPropertiesMap());















    /**
     * .version(QueryVersion.V1)
     * .version(QueryVersion.V2)
     * 
     * .queryFrom("pserver*('hostname','test-pserver-dsl')", "dsl")
     * .queryFrom("", "dsl")
     * 
     * .processWith(QueryProcessorType.GREMLIN_SERVER)
     * .processWith(QueryProcessorType.LOCAL_GROOVY)
     * 
     * .format(Format.aggregate)
     * .format(Format.console)
     * .format(Format.graphson)
     * 
     * .uriParams(new MultivaluedHashMap<>(Collections.singletonMap("as-tree", "true")))
     * .uriParams(new MultivaluedHashMap<>(Collections.singletonMap("as-tree", "TRue")))
     * 
     * .
     */

    GenericQueryProcessor qp1 = new GenericQueryProcessor.Builder(graphEngine, gremlinServerSingleton)
        .queryFrom(dsl, "dsl")
        .queryProcessor(dslQueryProcessor)
        .version(QueryVersion.V1)
        .processWith(QueryProcessorType.LOCAL_GROOVY)
        .format(Format.graphson)
        .uriParams(new MultivaluedHashMap<>(Collections.singletonMap("as-tree", "true")))
        // .uriParams(Collections.singletonMap("as-tree", "true"))
        .traversalSource(false, traversalSource)
        .create();










    }

    @Test
    public void executeDslQueryWithStarSubgraphStyle() throws FileNotFoundException, AAIException {
        traversalUriHttpEntry.setHttpEntryProperties(new SchemaVersion("v21"), "smth");
        final TransactionalGraphEngine graphEngine = traversalUriHttpEntry.getDbEngine();
        final GraphTraversalSource traversalSource = graphEngine.asAdmin().getTraversalSource();
        
        String dsl = "pserver*('hostname','test-pserver-dsl')";
        
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.add("format", Format.resource.toString());

        GenericQueryProcessor queryProcessor = new GenericQueryProcessor.Builder(graphEngine, gremlinServerSingleton)
            .queryFrom(dsl, "dsl").queryProcessor(dslQueryProcessor).version(dslApiVersion)
            .processWith(processorType).format(Format.resource).uriParams(queryParameters)
            .traversalSource(false, traversalSource)
            .create();

        List<Object> vertTemp = queryProcessor.execute(SubGraphStyle.star);
        
        Vertex vertex = (Vertex) vertTemp.get(0);
        Vertex pServer = traversal.V().has("hostname", "test-pserver-dsl").next();
        assertEquals(1, vertTemp.size());
        assertTrue(ElementHelper.areEqual(pServer, vertex));
    }

    @Test
    public void executeDslQueryWithPruneSubgraphStyle() throws FileNotFoundException, AAIException {
        traversalUriHttpEntry.setHttpEntryProperties(new SchemaVersion("v21"), "smth");
        final TransactionalGraphEngine graphEngine = traversalUriHttpEntry.getDbEngine();
        final GraphTraversalSource traversalSource = graphEngine.asAdmin().getTraversalSource();
        
        String dsl = "pserver*('hostname','test-pserver-dsl')";
        
        MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();
        queryParameters.add("format", Format.resource.toString());

        GenericQueryProcessor queryProcessor = new GenericQueryProcessor.Builder(graphEngine, gremlinServerSingleton)
            .queryFrom(dsl, "dsl").queryProcessor(dslQueryProcessor).version(dslApiVersion)
            .processWith(processorType).format(Format.resource).uriParams(queryParameters)
            .traversalSource(false, traversalSource)
            .create();

        List<Object> vertTemp = queryProcessor.execute(SubGraphStyle.prune);
        
        Vertex vertex = (Vertex) vertTemp.get(0);
        Vertex pServer = traversal.V().has("hostname", "test-pserver-dsl").next();
        assertEquals(1, vertTemp.size());
        assertTrue(ElementHelper.areEqual(pServer, vertex));
    }

    @Test
    public void thatEmptyQueryReturnsEmptyList() {
        assertTrue(true); // TODO: Implement test
    }

    @Test
    public void thatResponseCanBeRetrievedInTreeFormat() {
        assertTrue(true); // TODO: Implement test
    }
}
