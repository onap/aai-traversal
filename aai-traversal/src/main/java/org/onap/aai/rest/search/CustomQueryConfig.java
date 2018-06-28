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

import java.util.List;

public class CustomQueryConfig {

    private String query;
    private List<String> queryOptionalProperties;
    private List<String> queryRequiredProperties;

    public CustomQueryConfig() {
        // used by GetCustomQueryConfig
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery() {
        return this.query;
    }

    public void setQueryOptionalProperties(List<String> queryOptionalProperties) {
        this.queryOptionalProperties = queryOptionalProperties;
    }

    public List<String> getQueryOptionalProperties() {
        return queryOptionalProperties;
    }

    public void setQueryRequiredProperties(List<String> queryRequiredProperties) {
        this.queryRequiredProperties = queryRequiredProperties;
    }

    public List<String> getQueryRequiredProperties() {
        return queryRequiredProperties;
    }
}
