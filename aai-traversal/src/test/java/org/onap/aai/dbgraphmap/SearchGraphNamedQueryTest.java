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
package org.onap.aai.dbgraphmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aai.AAISetup;
import org.onap.aai.HttpTestUtil;
import org.onap.aai.PayloadUtil;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.util.AAIExtensionMap;
import org.onap.aai.util.AAIConstants;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SearchGraphNamedQueryTest extends AAISetup {

    protected static final MediaType APPLICATION_JSON = MediaType.valueOf("application/json");

    private static final Set<Integer> VALID_HTTP_STATUS_CODES = new HashSet<>();

    static {
        VALID_HTTP_STATUS_CODES.add(200);
        VALID_HTTP_STATUS_CODES.add(201);
        VALID_HTTP_STATUS_CODES.add(204);
    }

    private HttpHeaders httpHeaders;

    private MultivaluedMap<String, String> headersMultiMap;
    private MultivaluedMap<String, String> queryParameters;

    private List<String> aaiRequestContextList;

    private List<MediaType> outputMediaTypes;

    private static boolean ranOnce = false;

    private HttpTestUtil httpTestUtil;

    private String getJsonValue(String json, String key) {
        JsonObject jsonObj = JsonParser.parseString(json).getAsJsonObject();
        String strValue = "";
        if (jsonObj.isJsonObject()) {
            strValue = jsonObj.get(key).getAsString();
        }
        return strValue;
    }

    private void addWidgets() {
        String widgetPath = "." + AAIConstants.AAI_FILESEP + "src/main/resources"
            + AAIConstants.AAI_FILESEP + "etc" + AAIConstants.AAI_FILESEP + "scriptdata"
            + AAIConstants.AAI_FILESEP + "widget-model-json";

        File dir = new File(widgetPath);
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            try {
                Path path = Path.of(widgetPath + AAIConstants.AAI_FILESEP + file.getName());
                String widgetPayload = new String(Files.readAllBytes(path));
                String modelInvariantId = getJsonValue(widgetPayload, "model-invariant-id");
                String widgetUri =
                    "/aai/v12/service-design-and-creation/models/model/" + modelInvariantId;
                Response response = httpTestUtil.doPut(widgetUri, widgetPayload);
                assertEquals("Expected the named-query to be created", 201, response.getStatus());
            } catch (AAIException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    private void addNamedQueries() {
        String namedQueryPath = "." + AAIConstants.AAI_FILESEP + "src/main/resources"
            + AAIConstants.AAI_FILESEP + "etc" + AAIConstants.AAI_FILESEP + "scriptdata"
            + AAIConstants.AAI_FILESEP + "named-query-json";

        File dir = new File(namedQueryPath);
        File[] files = dir.listFiles();
        assert files != null;
        for (File file : files) {
            try {
                Path path = Path.of(namedQueryPath + AAIConstants.AAI_FILESEP + file.getName());
                String namedQueryPayload = new String(Files.readAllBytes(path));
                String namedQueryUuid = getJsonValue(namedQueryPayload, "named-query-uuid");
                String namedQueryUri =
                    "/aai/v12/service-design-and-creation/named-queries/named-query/"
                        + namedQueryUuid;

                Response response = httpTestUtil.doPut(namedQueryUri, namedQueryPayload);
                assertEquals("Expected the named-query to be created", 201, response.getStatus());
            } catch (AAIException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    private String addVersionToUri(String uri) {
        return "/aai/" + schemaVersions.getDefaultVersion() + "/" + uri;
    }

    @Before
    public void setup() {

        httpTestUtil = new HttpTestUtil();

        httpHeaders = mock(HttpHeaders.class);

        headersMultiMap = new MultivaluedHashMap<>();
        queryParameters = Mockito.spy(new MultivaluedHashMap<>());

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
        when(httpHeaders.getRequestHeader("X-FromAppId"))
            .thenReturn(Collections.singletonList("JUNIT"));
        when(httpHeaders.getRequestHeader("X-TransactionId"))
            .thenReturn(Collections.singletonList("JUNIT"));

        when(httpHeaders.getRequestHeader("aai-request-context")).thenReturn(aaiRequestContextList);

        // TODO - Check if this is valid since RemoveDME2QueryParameters seems to be very
        // unreasonable
        Mockito.doReturn(null).when(queryParameters).remove(anyObject());

        when(httpHeaders.getMediaType()).thenReturn(APPLICATION_JSON);

        if (!ranOnce) {
            ranOnce = true;
            addWidgets();
            addNamedQueries();
        }
    }

    @Test
    public void getDHVLogicalLinkByCircuitId_1_0_Test() throws Exception {

        AAIExtensionMap aaiExtMap = new AAIExtensionMap();
        aaiExtMap.setHttpHeaders(httpHeaders);
        String queryParameters =
            PayloadUtil.getNamedQueryPayload("query-payload.DHVLogicalLinkByCircuitId-1.0.json");
        String putPayload =
            PayloadUtil.getNamedQueryPayload("logical-link.DHVLogicalLinkByCircuitId-1.0.json");

        String linkName = getJsonValue(putPayload, "link-name");
        String putUri = addVersionToUri("network/logical-links/logical-link/" + linkName);

        Response response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the logical-link to be created", 201, response.getStatus());

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getContentType()).thenReturn("application/json");

        aaiExtMap.setUri("/search/named-query");
        aaiExtMap.setApiVersion(schemaVersions.getDefaultVersion().toString());
        aaiExtMap.setServletRequest(request);

        response = searchGraph.runNamedQuery("JUNIT", "JUNIT", queryParameters, aaiExtMap);
        System.out.println("response was\n" + response.getEntity().toString());
        assertEquals("Expected success from query", 200, response.getStatus());
        boolean hasLinkName = response.getEntity().toString().indexOf(linkName) > 0;
        assertTrue("Response contains linkName", hasLinkName);
    }

    @Test
    public void getSvcSubscriberModelInfo_1_0_Test() throws Exception {

        AAIExtensionMap aaiExtMap = new AAIExtensionMap();
        aaiExtMap.setHttpHeaders(httpHeaders);
        String queryParameters =
            PayloadUtil.getNamedQueryPayload("query-payload.SvcSubscriberModelInfo-1.0.json");

        String putPayload =
            PayloadUtil.getNamedQueryPayload("model.SvcSubscriberModelInfo-1.0.json");
        String modelInvariantId = getJsonValue(putPayload, "model-invariant-id");
        String putUri =
            addVersionToUri("service-design-and-creation/models/model/" + modelInvariantId);
        Response response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the model to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("customer.SvcSubscriberModelInfo-1.0.json");
        String globalCustomerId = getJsonValue(putPayload, "global-customer-id");
        putUri = addVersionToUri("business/customers/customer/" + globalCustomerId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the customer to be created", 201, response.getStatus());

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getContentType()).thenReturn("application/json");

        aaiExtMap.setUri("/search/named-query");
        aaiExtMap.setApiVersion(schemaVersions.getDefaultVersion().toString());
        aaiExtMap.setServletRequest(request);

        response = searchGraph.runNamedQuery("JUNIT", "JUNIT", queryParameters, aaiExtMap);
        assertEquals("Expected success from query", 200, response.getStatus());
        boolean hasModelName = response.getEntity().toString().indexOf("junit-model-name") > 0;
        assertTrue("Response contains modelName from model-ver", hasModelName);
    }

    @Test
    public void getClosedLoopNamedQuery_1_0_Test() throws Exception {

        AAIExtensionMap aaiExtMap = new AAIExtensionMap();
        aaiExtMap.setHttpHeaders(httpHeaders);
        String queryParameters =
            PayloadUtil.getNamedQueryPayload("query-payload.closed-loop-named-query-1.0.json");

        String putPayload =
            PayloadUtil.getNamedQueryPayload("model.closed-loop-named-query-1.0.json");
        String modelInvariantId = getJsonValue(putPayload, "model-invariant-id");
        String putUri =
            addVersionToUri("service-design-and-creation/models/model/" + modelInvariantId);
        Response response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the model to be created", 201, response.getStatus());

        putPayload =
            PayloadUtil.getNamedQueryPayload("cloud-region.closed-loop-named-query-1.0.json");
        String cloudOwner = getJsonValue(putPayload, "cloud-owner");
        String cloudRegionId = getJsonValue(putPayload, "cloud-region-id");
        putUri = addVersionToUri(
            "cloud-infrastructure/cloud-regions/cloud-region/" + cloudOwner + "/" + cloudRegionId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the cloud-region to be created", 201, response.getStatus());

        putPayload =
            PayloadUtil.getNamedQueryPayload("cloud-region2.closed-loop-named-query-1.0.json");
        String cloudOwner2 = getJsonValue(putPayload, "cloud-owner");
        String cloudRegionId2 = getJsonValue(putPayload, "cloud-region-id");
        putUri = addVersionToUri("cloud-infrastructure/cloud-regions/cloud-region/" + cloudOwner2
            + "/" + cloudRegionId2);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the cloud-region2 to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("tenant.closed-loop-named-query-1.0.json");
        String tenantId = getJsonValue(putPayload, "tenant-id");
        putUri = addVersionToUri("cloud-infrastructure/cloud-regions/cloud-region/" + cloudOwner
            + "/" + cloudRegionId + "/tenants/tenant/" + tenantId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the tenant to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("tenant2.closed-loop-named-query-1.0.json");
        String tenantId2 = getJsonValue(putPayload, "tenant-id");
        putUri = addVersionToUri("cloud-infrastructure/cloud-regions/cloud-region/" + cloudOwner2
            + "/" + cloudRegionId2 + "/tenants/tenant/" + tenantId2);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the tenant2 to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("vserver.closed-loop-named-query-1.0.json");
        String vserverId = getJsonValue(putPayload, "vserver-id");
        putUri =
            addVersionToUri("cloud-infrastructure/cloud-regions/cloud-region/" + cloudOwner + "/"
                + cloudRegionId + "/tenants/tenant/" + tenantId + "/vservers/vserver/" + vserverId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the vserver to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("vserver2.closed-loop-named-query-1.0.json");
        String vserverId2 = getJsonValue(putPayload, "vserver-id");
        putUri = addVersionToUri(
            "cloud-infrastructure/cloud-regions/cloud-region/" + cloudOwner2 + "/" + cloudRegionId2
                + "/tenants/tenant/" + tenantId2 + "/vservers/vserver/" + vserverId2);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the vserver2 to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("customer.closed-loop-named-query-1.0.json");
        String globalCustomerId = getJsonValue(putPayload, "global-customer-id");
        putUri = addVersionToUri("business/customers/customer/" + globalCustomerId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the customer to be created", 201, response.getStatus());

        putPayload =
            PayloadUtil.getNamedQueryPayload("generic-vnf.closed-loop-named-query-1.0.json");
        String vnfId = getJsonValue(putPayload, "vnf-id");
        putUri = addVersionToUri("network/generic-vnfs/generic-vnf/" + vnfId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the generic-vnf to be created", 201, response.getStatus());

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getContentType()).thenReturn("application/json");

        aaiExtMap.setUri("/search/named-query");
        aaiExtMap.setApiVersion(schemaVersions.getDefaultVersion().toString());
        aaiExtMap.setServletRequest(request);

        response = searchGraph.runNamedQuery("JUNIT", "JUNIT", queryParameters, aaiExtMap);
        assertEquals("Expected success from query", 200, response.getStatus());
        boolean hasModelName =
            response.getEntity().toString().indexOf("example-model-name-val-closed-loop") > 0;
        assertTrue("Response contains modelName from model-ver", hasModelName);
    }

    @Test
    public void getComponentList_1_2_Test() throws Exception {

        AAIExtensionMap aaiExtMap = new AAIExtensionMap();
        aaiExtMap.setHttpHeaders(httpHeaders);
        String queryParameters =
            PayloadUtil.getNamedQueryPayload("query-payload.ComponentList-1.2.json");

        String putPayload = PayloadUtil.getNamedQueryPayload("model.ComponentList-1.2.json");
        String modelInvariantId = getJsonValue(putPayload, "model-invariant-id");
        String putUri =
            addVersionToUri("service-design-and-creation/models/model/" + modelInvariantId);
        Response response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the model to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("customer.ComponentList-1.2.json");
        String globalCustomerId = getJsonValue(putPayload, "global-customer-id");
        putUri = addVersionToUri("business/customers/customer/" + globalCustomerId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the customer to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("generic-vnf.ComponentList-1.2.json");
        String vnfId = getJsonValue(putPayload, "vnf-id");
        putUri = addVersionToUri("network/generic-vnfs/generic-vnf/" + vnfId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the generic-vnf to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("vf-module.ComponentList-1.2.json");
        String vfModuleId = getJsonValue(putPayload, "vf-module-id");
        putUri = addVersionToUri(
            "network/generic-vnfs/generic-vnf/" + vnfId + "/vf-modules/vf-module/" + vfModuleId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the vf-module to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("cloud-region.ComponentList-1.2.json");
        String cloudOwner = getJsonValue(putPayload, "cloud-owner");
        String cloudRegionId = getJsonValue(putPayload, "cloud-region-id");
        putUri = addVersionToUri(
            "cloud-infrastructure/cloud-regions/cloud-region/" + cloudOwner + "/" + cloudRegionId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the cloud-region to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("tenant.ComponentList-1.2.json");
        String tenantId = getJsonValue(putPayload, "tenant-id");
        putUri = addVersionToUri("cloud-infrastructure/cloud-regions/cloud-region/" + cloudOwner
            + "/" + cloudRegionId + "/tenants/tenant/" + tenantId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the tenant to be created", 201, response.getStatus());

        putPayload = PayloadUtil.getNamedQueryPayload("vserver.ComponentList-1.2.json");
        String vserverId = getJsonValue(putPayload, "vserver-id");
        putUri =
            addVersionToUri("cloud-infrastructure/cloud-regions/cloud-region/" + cloudOwner + "/"
                + cloudRegionId + "/tenants/tenant/" + tenantId + "/vservers/vserver/" + vserverId);
        response = httpTestUtil.doPut(putUri, putPayload);
        assertEquals("Expected the vserver to be created", 201, response.getStatus());

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getContentType()).thenReturn("application/json");

        aaiExtMap.setUri("/search/named-query");
        aaiExtMap.setApiVersion(schemaVersions.getDefaultVersion().toString());
        aaiExtMap.setServletRequest(request);

        response = searchGraph.runNamedQuery("JUNIT", "JUNIT", queryParameters, aaiExtMap);
        assertEquals("Expected success from query", 200, response.getStatus());
        boolean hasModelName =
            response.getEntity().toString().indexOf("example-model-name-val-component-list") > 0;
        assertTrue("Response contains modelName from model-ver", hasModelName);
    }
}
