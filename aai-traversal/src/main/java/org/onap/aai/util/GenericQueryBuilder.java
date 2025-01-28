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

public class GenericQueryBuilder {

    private HttpHeaders headers;
    private String startNodeType;
    private List<String> startNodeKeyParams;
    private List<String> includeNodeTypes;
    private int depth;
    private TransactionalGraphEngine dbEngine;
    private Loader loader;
    private UrlBuilder urlBuilder;

    public HttpHeaders getHeaders() {
        return headers;
    }

    public String getStartNodeType() {
        return startNodeType;
    }

    public List<String> getStartNodeKeyParams() {
        return startNodeKeyParams;
    }

    public List<String> getIncludeNodeTypes() {
        return includeNodeTypes;
    }

    public int getDepth() {
        return depth;
    }

    public TransactionalGraphEngine getDbEngine() {
        return dbEngine;
    }

    public Loader getLoader() {
        return loader;
    }

    public UrlBuilder getUrlBuilder() {
        return urlBuilder;
    }

    public GenericQueryBuilder setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    public GenericQueryBuilder setStartNodeType(String startNodeType) {
        this.startNodeType = startNodeType;
        return this;
    }

    public GenericQueryBuilder setStartNodeKeyParams(List<String> startNodeKeyParams) {
        this.startNodeKeyParams = startNodeKeyParams;
        return this;
    }

    public GenericQueryBuilder setIncludeNodeTypes(List<String> includeNodeTypes) {
        this.includeNodeTypes = includeNodeTypes;
        return this;
    }

    public GenericQueryBuilder setDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public GenericQueryBuilder setDbEngine(TransactionalGraphEngine dbEngine) {
        this.dbEngine = dbEngine;
        return this;
    }

    public GenericQueryBuilder setLoader(Loader loader) {
        this.loader = loader;
        return this;
    }

    public GenericQueryBuilder setUrlBuilder(UrlBuilder urlBuilder) {
        this.urlBuilder = urlBuilder;
        return this;
    }
}
