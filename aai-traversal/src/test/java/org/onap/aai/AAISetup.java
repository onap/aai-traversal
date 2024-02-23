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
package org.onap.aai;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.onap.aai.config.*;
import org.onap.aai.dbgraphmap.SearchGraph;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.MoxyLoader;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.dsl.DslQueryProcessor;
import org.onap.aai.rest.dsl.V1DslQueryProcessor;
import org.onap.aai.rest.dsl.V2DslQueryProcessor;
import org.onap.aai.rest.dsl.v1.DslListener;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.serialization.db.EdgeSerializer;
import org.onap.aai.setup.AAIConfigTranslator;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

@ContextConfiguration(
    classes = {ConfigConfiguration.class, AAIConfigTranslator.class, EdgeIngestor.class,
        EdgeSerializer.class, NodeIngestor.class, SpringContextAware.class,
        IntrospectionConfig.class, RestBeanConfig.class, SearchConfiguration.class, XmlFormatTransformerConfiguration.class,
        GremlinServerSingleton.class, V1DslQueryProcessor.class, V2DslQueryProcessor.class, DslListener.class, org.onap.aai.rest.dsl.v2.DslListener.class})
@TestPropertySource(
    properties = {"schema.uri.base.path = /aai",
        "schema.ingest.file = src/test/resources/application-test.properties"})
public abstract class AAISetup {
    @Autowired
    protected NodeIngestor nodeIngestor;

    @Autowired
    protected LoaderFactory loaderFactory;

    @Autowired
    protected Map<SchemaVersion, MoxyLoader> moxyLoaderInstance;

    @Autowired
    protected HttpEntry traversalHttpEntry;

    @Autowired
    protected HttpEntry traversalUriHttpEntry;

    @Autowired
    protected SearchGraph searchGraph;

    @Autowired
    protected EdgeSerializer edgeSer;

    @Autowired
    protected EdgeIngestor edgeIngestor;

    @Autowired
    protected SchemaVersions schemaVersions;

    @Autowired
    protected GremlinServerSingleton gremlinServerSingleton;

    @Value("${schema.uri.base.path}")
    protected String basePath;

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @BeforeClass
    public static void setupBundleconfig() throws Exception {
        System.setProperty("AJSC_HOME", "./");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources/");

    }

    public String getPayload(String filename) throws IOException {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);

        String message = String.format("Unable to find the %s in src/test/resources", filename);
        assertNotNull(message, inputStream);

        String resource = IOUtils.toString(inputStream);
        return resource;
    }
}
