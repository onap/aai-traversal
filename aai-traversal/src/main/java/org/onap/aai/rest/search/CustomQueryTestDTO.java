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

/*-
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomQueryTestDTO {

    @JsonProperty("stored-query")
    private String storedQuery;

    @JsonProperty("vertices")
    private List<LinkedHashMap<String, String>> verticesDtos;

    @JsonProperty("edges")
    private List<LinkedHashMap<String, String>> edgesDtos;

    @JsonProperty("optional-properties")
    private Map<String, String> queryOptionalProperties = new HashMap<>();

    @JsonProperty("required-properties")
    private Map<String, String> queryRequiredProperties = new HashMap<>();

    @JsonProperty("expected-result")
    private ExpectedResultsDto expectedResultsDtos;

    public String getStoredQuery() {
        return storedQuery;
    }

    public void setStoredQuery(String storedQuery) {
        this.storedQuery = storedQuery;
    }

    public List<LinkedHashMap<String, String>> getVerticesDtos() {
        return verticesDtos;
    }

    public void setVerticesDtos(List<LinkedHashMap<String, String>> verticesDtos) {
        this.verticesDtos = verticesDtos;
    }

    public List<LinkedHashMap<String, String>> getEdgesDtos() {
        return edgesDtos;
    }

    public void setEdgesDtos(List<LinkedHashMap<String, String>> edgesDtos) {
        this.edgesDtos = edgesDtos;
    }

    public Map<String, String> getQueryOptionalProperties() {
        return queryOptionalProperties;
    }

    public void setQueryOptionalProperties(Map<String, String> queryOptionalProperties) {
        this.queryOptionalProperties = queryOptionalProperties;
    }

    public Map<String, String> getQueryRequiredProperties() {
        return queryRequiredProperties;
    }

    public void setQueryRequiredProperties(Map<String, String> queryRequiredProperties) {
        this.queryRequiredProperties = queryRequiredProperties;
    }

    public ExpectedResultsDto getExpectedResultsDtos() {
        return expectedResultsDtos;
    }

    public void setExpectedResultsDtos(ExpectedResultsDto expectedResultsDtos) {
        this.expectedResultsDtos = expectedResultsDtos;
    }

}
