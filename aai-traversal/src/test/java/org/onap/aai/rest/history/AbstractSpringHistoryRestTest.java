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
package org.onap.aai.rest.history;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphTransaction;
import org.junit.*;
import org.onap.aai.TraversalApp;
import org.onap.aai.TraversalTestConfiguration;
import org.onap.aai.config.PropertyPasswordConfiguration;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.util.AAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Collections;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = TraversalApp.class)
@TestPropertySource(
        locations = "classpath:application-test.properties",
        properties = {
            "history.enabled=true",
            "history.truncate.window.days = " + Integer.MAX_VALUE
        }
)
@ContextConfiguration(initializers = PropertyPasswordConfiguration.class)
@Import(TraversalTestConfiguration.class)
public abstract class AbstractSpringHistoryRestTest {

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected NodeIngestor nodeIngestor;
    
    @LocalServerPort
    protected int randomPort;

    protected HttpEntity httpEntity;

    protected String baseUrl;
    protected HttpHeaders headers ;

    @BeforeClass
    public static void setupConfig() throws AAIException {
        System.setProperty("AJSC_HOME", "./");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources/");
    }

    @Before
    public void setup() throws AAIException, UnsupportedEncodingException {

        AAIConfig.init();
        AAIGraph.getInstance();

        createTestGraph();
        headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Real-Time", "true");
        headers.add("X-FromAppId", "JUNIT");
        headers.add("X-TransactionId", "JUNIT");
        String authorization = Base64.getEncoder().encodeToString("AAI:AAI".getBytes("UTF-8"));
        headers.add("Authorization", "Basic " + authorization);
        httpEntity = new HttpEntity(headers);
        baseUrl = "http://localhost:" + randomPort;
    }

    /*
     * Inheritors please override this one
     */
    public void createTestGraph(){
    	
    }

    @After
    public void tearDown(){

        JanusGraph janusGraph = AAIGraph.getInstance().getGraph();
        JanusGraphTransaction transaction = janusGraph.newTransaction();

        boolean success = true;

        try {
            GraphTraversalSource g = transaction.traversal();
            g.V().toList()
                    .forEach(Vertex::remove);
        } catch(Exception ex){
            success = false;
        } finally {
            if(success){
                transaction.commit();
            } else {
                transaction.rollback();
            }
        }
    }
}
