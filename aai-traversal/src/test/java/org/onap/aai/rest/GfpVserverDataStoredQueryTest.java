/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.rest;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.jayway.jsonpath.JsonPath;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanTransaction;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.HttpTestUtil;
import org.onap.aai.PayloadUtil;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.introspection.Version;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.*;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GfpVserverDataStoredQueryTest {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(GfpVserverDataStoredQueryTest.class);

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    private HttpHeaders httpHeaders;

    private MultivaluedMap<String, String> headersMultiMap;
    private MultivaluedMap<String, String> queryParameters;

    private List<String> aaiRequestContextList;

    private List<MediaType> outputMediaTypes;

    private HttpTestUtil httpTestUtil;

    private QueryConsumer queryConsumer;

    private static final Version VERSION = Version.v11;
    private static final String CLOUD_REGION_URI = "/aai/" + VERSION.toString()
                                                 + "/cloud-infrastructure/cloud-regions/"
                                                 + "cloud-region/testOwner1/testRegion1";

    @Before
    public void setup() throws Exception {
        httpTestUtil = new HttpTestUtil();

        Map<String, String> templateValues = new HashMap<>();

        templateValues.put("cloud-owner", "testOwner1");
        templateValues.put("cloud-region-id", "testRegion1");
        templateValues.put("tenant-id", "testTenant1");
        templateValues.put("tenant-name", "testTenantName1");
        templateValues.put("vserver-id", "testVserver1");
        templateValues.put("vserver-name", "junit-vservers");
        templateValues.put("interface-name", "testlInterfaceName1");
        templateValues.put("ipv4-address", "192.33.233.233");
        templateValues.put("ipv6-address", "2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        templateValues.put("vlan-interface", "vlan-interface1");

        String cloudRegionPayload = PayloadUtil.
                getTemplatePayload("cloud-region-with-linterface.json", templateValues);

        Response response = httpTestUtil.doPut(CLOUD_REGION_URI, cloudRegionPayload);
        logger.info("Response status received {}", response.getEntity());

        assertNotNull("Expected the response to be not null", response);
        assertEquals("Expecting the cloud region to be created", 201, response.getStatus());
        logger.info("Successfully created the cloud region with linterface");

        queryConsumer = new QueryConsumer();

        httpHeaders         = mock(HttpHeaders.class);

        headersMultiMap     = new MultivaluedHashMap<>();
        queryParameters     = Mockito.spy(new MultivaluedHashMap<>());

        headersMultiMap.add("X-FromAppId", "JUNIT");
        headersMultiMap.add("X-TransactionId", UUID.randomUUID().toString());
        headersMultiMap.add("Real-Time", "true");
        headersMultiMap.add("Accept", "application/json");
        headersMultiMap.add("aai-request-context", "");

        outputMediaTypes = new ArrayList<>();
        outputMediaTypes.add(APPLICATION_JSON);

        aaiRequestContextList = new ArrayList<>();
        aaiRequestContextList.add("");

        when(httpHeaders.getAcceptableMediaTypes()).thenReturn(outputMediaTypes);
        when(httpHeaders.getRequestHeaders()).thenReturn(headersMultiMap);
        when(httpHeaders.getRequestHeader("X-FromAppId")).thenReturn(Arrays.asList("JUNIT"));
        when(httpHeaders.getRequestHeader("X-TransactionId")).thenReturn(Arrays.asList("JUNIT"));
        when(httpHeaders.getRequestHeader("aai-request-context")).thenReturn(aaiRequestContextList);

        Mockito.doReturn(null).when(queryParameters).remove(anyObject());

        when(httpHeaders.getMediaType()).thenReturn(APPLICATION_JSON);
    }

    @Test
    public void testStoredQueryVerifyDoesNotThrowMethodTooLargeWhenLargeNumberOfStartingVertexes() throws Exception {

        // Add hundred thousand vserver vertexes to properly
        // test the scenario where the application was
        // failing with method too large
        addVservers(1000000);

        Map<String, String> templateValues = new HashMap<>();

        // Purposefully putting the filter to the testVserver1 as
        // since this is a junit test other junit tests could put
        // vserver and not properly clean up after the test
        // so doing this to ensure that this is testing against the particular vserver
        // as not to fail when another unit test decide to put vserver and not clean up
        templateValues.put("start", "nodes/vservers?vserver-name=junit-vservers");
        templateValues.put("query", "gfp-vserver-data");

        String payload = PayloadUtil.getTemplatePayload("custom-query.json", templateValues);
        String query = String.format("/aai/%s/query?format=resource_and_url", VERSION.toString());

        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        queryParameters.add("format", "resource_and_url");
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(uriInfo.getPath()).thenReturn(query);

        Response response = queryConsumer.executeQuery(
            payload,
            VERSION.toString(),
            query,
            "resource_and_url", "" +
            "no_op",
            httpHeaders,
            uriInfo,
            httpServletRequest
        );

        String entity = response.getEntity().toString();
        assertEquals("Expected the response to be 200 but got this returned: " + response.getEntity().toString(),
                200, response.getStatus());
        List<String> urls = JsonPath.read(entity, "$.results[*].url");
        assertEquals("Expected the urls to be 3", 3, urls.size());
        removeVertexes();
    }

    @Test
    public void testStoredQueryWhenQueryDoesNotExistShouldReturnBadRequest() throws Exception {

        Map<String, String> templateValues = new HashMap<>();

        templateValues.put("start", "nodes/vservers");
        templateValues.put("query", "fake-query");

        String payload = PayloadUtil.getTemplatePayload("custom-query.json", templateValues);
        String query = String.format("/aai/%s/query?format=resource_and_url", VERSION.toString());

        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        queryParameters.add("format", "resource_and_url");
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(uriInfo.getPath()).thenReturn(query);

        Response response = queryConsumer.executeQuery(
                payload,
                VERSION.toString(),
                query,
                "resource_and_url", "" +
                        "no_op",
                httpHeaders,
                uriInfo,
                httpServletRequest
        );

        String entity = response.getEntity().toString();

        assertEquals("Expected the response to be 400 but got this returned: " + entity,
                400, response.getStatus());

        assertThat("Expecting error message since query doesn't exist", entity,
                containsString("Query payload is invalid"));
    }

    @Test
    public void testStoredQueryWhenStartFilterReturnsZeroVertexesItShouldHandleProperly() throws Exception {

        Map<String, String> templateValues = new HashMap<>();

        templateValues.put("start", "nodes/vservers?vserver-name=nonexistent-filter");
        templateValues.put("query", "gfp-vserver-data");

        String payload = PayloadUtil.getTemplatePayload("custom-query.json", templateValues);
        String query = String.format("/aai/%s/query?format=resource_and_url", VERSION.toString());

        UriInfo uriInfo = Mockito.mock(UriInfo.class);
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);

        queryParameters.add("format", "resource_and_url");
        Mockito.when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        when(uriInfo.getPath()).thenReturn(query);

        Response response = queryConsumer.executeQuery(
                payload,
                VERSION.toString(),
                query,
                "resource_and_url", "" +
                        "no_op",
                httpHeaders,
                uriInfo,
                httpServletRequest
        );

        String entity = response.getEntity().toString();

        assertEquals("Expected the response to be 500 but got this returned: " + entity,
                500, response.getStatus());

        assertThat(entity, containsString("Internal Error:groovy.lang.MissingPropertyException"));
    }

    @After
    public void tearDown(){
        removeVertexes();
    }

    private void removeVertexes(){

        TitanGraph titanGraph = AAIGraph.getInstance().getGraph();
        TitanTransaction transaction = titanGraph.newTransaction();

        boolean success = true;

        try {
            GraphTraversalSource g = transaction.traversal();
            g.V().has("source-of-truth", "JUNIT").toList().stream()
                    .forEach((vertex) -> vertex.remove());
        } catch(Exception ex){
            success = false;
            logger.error("Unable to remove all of the junit vservers due to {}", ex);
        } finally {
            if(success){
                transaction.commit();
            } else {
                transaction.rollback();
            }
        }

    }

    private void addVservers(int vserversCount){

        TitanGraph titanGraph = AAIGraph.getInstance().getGraph();
        TitanTransaction transaction = titanGraph.newTransaction();

        boolean success = true;

        try {

            GraphTraversalSource g = transaction.traversal();
            for(int index = 0; index < vserversCount; index++){
                String randomVserverId = UUID.randomUUID().toString();
                g.addV().property("aai-node-type", "vserver")
                        .property( "vserver-id", "random-" + randomVserverId)
                        .property( "vserver-name", "junit-vservers")
                        .property( "source-of-truth", "JUNIT")
                        .next();
            }

        } catch(Exception ex){
            success = false;
            logger.error("Unable to add all of the vservers due to {}", ex);
        } finally {
            if(success){
                transaction.commit();
            } else {
                transaction.rollback();
            }
        }
    }
}
