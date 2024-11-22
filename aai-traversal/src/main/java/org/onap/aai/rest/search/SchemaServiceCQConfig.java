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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.onap.aai.restclient.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

public class SchemaServiceCQConfig extends CQConfig {

    private static Logger logger = LoggerFactory.getLogger(SchemaServiceCQConfig.class);
    private static final String SCHEMA_SERVICE = "schema-service";

    @Value("${schema.service.custom.queries.endpoint}")
    private String customQueriesUri;

    @Qualifier("restClient")
    @Autowired
    private RestClient restClient;

    @PostConstruct
    public void initialize() {
        // Call SchemaService to get custom queries
        retrieveCustomQueries();
    }

    public void retrieveCustomQueries() {
        /*
         * Call Schema MS to get custom queries using RestTemplate
         */
        logger.info("Calling the SchemaService to retrieve stored queries");
        String content = "";
        Map<String, String> headersMap = new HashMap<>();

        ResponseEntity<String> schemaResponse =
            restClient.getGetRequest(content, customQueriesUri, headersMap);
        queryConfig = new GetCustomQueryConfig(schemaResponse.getBody());
    }
}
