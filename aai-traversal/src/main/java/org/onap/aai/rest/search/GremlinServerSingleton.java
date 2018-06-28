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

import org.onap.aai.logging.LogFormatTools;
import org.onap.aai.util.AAIConstants;
import org.onap.aai.util.FileWatcher;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.tinkerpop.gremlin.driver.Cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class GremlinServerSingleton {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(GremlinServerSingleton.class);

    private Cluster cluster;
    private boolean timerSet;
    private Timer timer;
    private GetCustomQueryConfig queryConfig;

    private static class Helper {

        private static final GremlinServerSingleton INSTANCE = new GremlinServerSingleton();
    }

    private GremlinServerSingleton() {
        init();
    }

    public static GremlinServerSingleton getInstance() {
        return Helper.INSTANCE;
    }

    /**
     * Initializes the gremlin server singleton
     * Loads the configuration of the gremlin server and creates a cluster
     * Loads the gremlin query file into the properties object
     * Then creates a file watcher to watch the file every ten seconds
     * and if there is a change in the file, then reloads the file into
     * the properties object
     */
    private void init() {

        try {
            cluster = Cluster.build(new File(AAIConstants.AAI_HOME_ETC_APP_PROPERTIES + "gremlin-server-config.yaml"))
                .maxContentLength(6537920)
                .create();
        } catch (FileNotFoundException e) {
            logger.error("Unable to find the file: " + LogFormatTools.getStackTop(e));
        }

        try {
            String filepath = GetCustomQueryConfig.AAI_HOME_ETC_QUERY_JSON;
            Path path = Paths.get(filepath);
            String customQueryConfigJson = new String(Files.readAllBytes(path));

            queryConfig = new GetCustomQueryConfig(customQueryConfigJson);
        } catch (IOException e) {
            logger.error("Error occurred during the processing of query json file: " + LogFormatTools.getStackTop(e));
        }

        TimerTask task = new FileWatcher(new File(GetCustomQueryConfig.AAI_HOME_ETC_QUERY_JSON)) {
            @Override
            protected void onChange(File file) {
                try {
                    String filepath = GetCustomQueryConfig.AAI_HOME_ETC_QUERY_JSON;
                    Path path = Paths.get(filepath);
                    String customQueryConfigJson = new String(Files.readAllBytes(path));
                    queryConfig = new GetCustomQueryConfig(customQueryConfigJson);
                } catch (IOException e) {
                    logger.error(
                        "Error occurred during the processing of query json file: " + LogFormatTools.getStackTop(e));
                }
            }
        };

        if (!timerSet) {
            timerSet = true;
            timer = new Timer();
            timer.schedule(task, new Date(), 10000);
        }

    }

    public Cluster getCluster() {
        return cluster;
    }

    /**
     * Gets the query using CustomQueryConfig
     */
    public String getStoredQueryFromConfig(String key) {
        CustomQueryConfig customQueryConfig = queryConfig.getStoredQuery(key);
        if (customQueryConfig == null) {
            return null;
        }
        return customQueryConfig.getQuery();
    }

    public CustomQueryConfig getCustomQueryConfig(String key) {
        return queryConfig.getStoredQuery(key);
    }


}
