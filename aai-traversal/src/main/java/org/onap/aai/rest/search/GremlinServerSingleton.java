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
import java.util.Timer;

public class GremlinServerSingleton {

    private boolean timerSet;
    private Timer timer;

    CQConfig customQueryInfo;

    /**
     * Initializes the gremlin server singleton
     * Loads the configuration of the gremlin server and creates a cluster
     * Loads the gremlin query file into the properties object
     * Then creates a file watcher to watch the file every ten seconds
     * and if there is a change in the file, then reloads the file into
     * the properties object
     *
     */
    public GremlinServerSingleton(CQConfig customQueryInfo) {
        this.customQueryInfo = customQueryInfo;
    }

    /**
     * Gets the query using CustomQueryConfig
     *
     * @param key
     * @return
     */
    public String getStoredQueryFromConfig(String key) {
        GetCustomQueryConfig queryConfig = customQueryInfo.getCustomQueryConfig();

        CustomQueryConfig customQueryConfig = queryConfig.getStoredQuery(key);
        if (customQueryConfig == null) {
            return null;
        }
        return customQueryConfig.getQuery();
    }

    public CustomQueryConfig getCustomQueryConfig(String key) {
        GetCustomQueryConfig queryConfig = customQueryInfo.getCustomQueryConfig();
        return queryConfig.getStoredQuery(key);
    }

}
