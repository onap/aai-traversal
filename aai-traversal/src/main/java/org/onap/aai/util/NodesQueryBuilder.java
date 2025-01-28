/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Samsung Electronics Co., Ltd. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.util;

import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import org.onap.aai.introspection.Loader;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.utils.UrlBuilder;

/**
 * Builder Class used to minimize number of formal parameters.
 */

public class NodesQueryBuilder {

    private HttpHeaders headers;
    private List<String> edgeFilterParams;
    private List<String> filterParams;
    private TransactionalGraphEngine dbEngine;
    private Loader loader;
    private UrlBuilder urlBuilder;
    private String targetNodeType;

    public HttpHeaders getHeaders() {
        return headers;
    }

    public NodesQueryBuilder setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public String getTargetNodeType() {
        return targetNodeType;
    }

    public NodesQueryBuilder setTargetNodeType(String targetNodeType) {
        this.targetNodeType = targetNodeType;
        return this;
    }

    public List<String> getEdgeFilterParams() {
        return edgeFilterParams;
    }

    public NodesQueryBuilder setEdgeFilterParams(List<String> edgeFilterParams) {
        this.edgeFilterParams = edgeFilterParams;
        return this;
    }

    public List<String> getFilterParams() {
        return filterParams;
    }

    public NodesQueryBuilder setFilterParams(List<String> filterParams) {
        this.filterParams = filterParams;
        return this;
    }

    public TransactionalGraphEngine getDbEngine() {
        return dbEngine;
    }

    public NodesQueryBuilder setDbEngine(TransactionalGraphEngine dbEngine) {
        this.dbEngine = dbEngine;
        return this;
    }

    public Loader getLoader() {
        return loader;
    }

    public NodesQueryBuilder setLoader(Loader loader) {
        this.loader = loader;
        return this;
    }

    public UrlBuilder getUrlBuilder() {
        return urlBuilder;
    }

    public NodesQueryBuilder setUrlBuilder(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
        return this;
    }

}
