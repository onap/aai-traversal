/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Deutsche Telekom SA.
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
package org.onap.aai.rest;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.enums.QueryVersion;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.transforms.XmlFormatTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.annotation.Timed;

@Timed
@RestController
@RequestMapping("/{version:v[1-9][0-9]*|latest}/dsl")
public class DslConsumer extends TraversalConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DslConsumer.class);
    private static final QueryVersion DEFAULT_VERSION = QueryVersion.V1;
    private final DslConsumerService dslConsumerService;
    private final HttpEntry traversalUriHttpEntry;
    private final XmlFormatTransformer xmlFormatTransformer;

    @Autowired
    public DslConsumer(DslConsumerService dslConsumerService, HttpEntry traversalUriHttpEntry, XmlFormatTransformer xmlFormatTransformer) {
        this.dslConsumerService = dslConsumerService;
        this.traversalUriHttpEntry = traversalUriHttpEntry;
        this.xmlFormatTransformer = xmlFormatTransformer;
    }

    @PutMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> executeQuery(@RequestBody String dslQuery,
                                               @PathVariable("version") String versionParam,
                                               @RequestParam(defaultValue = "graphson") String format,
                                               @RequestParam(defaultValue = "no_op") String subgraph,
                                               @RequestParam(defaultValue = "all") String validate,
                                               @RequestParam(defaultValue = "-1") String resultIndex,
                                               @RequestParam(defaultValue = "-1") String resultSize,
                                               @RequestHeader HttpHeaders headers,
                                               HttpServletRequest request) throws FileNotFoundException, AAIException {
        Set<String> roles = this.getRoles(request.getUserPrincipal());

        return processExecuteQuery(dslQuery, request, versionParam, format, subgraph,
                validate, headers, resultIndex, resultSize, roles);
    }

    public ResponseEntity<String> processExecuteQuery(String dslQuery, HttpServletRequest request, String versionParam,
            String queryFormat, String subgraph, String validate, HttpHeaders headers,
            String resultIndex, String resultSize, Set<String> roles) throws FileNotFoundException, AAIException {

        final SchemaVersion version = new SchemaVersion(versionParam);
        final String sourceOfTruth = headers.getFirst("X-FromAppId");
        final String dslOverride = headers.getFirst("X-DslOverride");
        final MultivaluedMap<String,String> queryParams = toMultivaluedMap(request.getParameterMap());

        Optional<String> dslApiVersionHeader =
            Optional.ofNullable(headers.getFirst("X-DslApiVersion"));
        QueryVersion dslApiVersion = DEFAULT_VERSION;
        if (dslApiVersionHeader.isPresent()) {
            try {
                dslApiVersion = QueryVersion.valueOf(dslApiVersionHeader.get());
            } catch (IllegalArgumentException e) {
                LOGGER.debug("Defaulting DSL Api Version to  " + DEFAULT_VERSION);
            }
        }

        String result = dslConsumerService.executeQuery(dslQuery, request, queryFormat, subgraph, validate, queryParams, resultIndex, resultSize,
                roles, version, sourceOfTruth, dslOverride, dslApiVersion);
        MediaType acceptType = headers.getAccept().stream()
            .filter(Objects::nonNull)
            .filter(header -> !header.equals(MediaType.ALL))
            .findAny()
            .orElse(MediaType.APPLICATION_JSON);

        if (MediaType.APPLICATION_XML.isCompatibleWith(acceptType)) {
            result = xmlFormatTransformer.transform(result);
        }

        if (traversalUriHttpEntry.isPaginated()) {
            return ResponseEntity.ok()
                .header("total-results", String.valueOf(traversalUriHttpEntry.getTotalVertices()))
                .header("total-pages", String.valueOf(traversalUriHttpEntry.getTotalPaginationBuckets()))
                .body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }

    private MultivaluedMap<String, String> toMultivaluedMap(Map<String, String[]> map) {
        MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();

        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            for (String val : entry.getValue())
            multivaluedMap.add(entry.getKey(), val);
        }

        return multivaluedMap;
    }
}
