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
package org.onap.aai.rest.search;

import org.onap.aai.util.AAIConstants;
import org.onap.aai.util.FileWatcher;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.tinkerpop.gremlin.driver.Cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class GremlinServerSingleton {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(GremlinServerSingleton.class);

    private Cluster cluster;
    private boolean timerSet;
    private Timer timer;
    private Properties properties;

    private static class Helper {
        private static final GremlinServerSingleton INSTANCE = new GremlinServerSingleton();
    }

    public static GremlinServerSingleton getInstance() {
        return Helper.INSTANCE;
    }

    private GremlinServerSingleton(){
        init();
    }

    /**
     * Initializes the gremlin server singleton
     * Loads the configuration of the gremlin server and creates a cluster
     * Loads the gremlin query file into the properties object
     * Then creates a file watcher to watch the file every ten seconds
     * and if there is a change in the file, then reloads the file into
     * the properties object
     *
     */
    private void init() {

        properties = new Properties();

        try {
            cluster = Cluster.build(new File(AAIConstants.AAI_HOME_ETC_APP_PROPERTIES + "gremlin-server-config.yaml"))
                    .maxContentLength(6537920)
                    .create();
        } catch (FileNotFoundException e) {
            logger.error("Unable to find the file: " + e);
        }

        File queryFile = new File(AAIConstants.AAI_HOME_ETC_QUERY);

        try (FileInputStream fis = new FileInputStream(queryFile)){
            properties.load(fis);
        } catch (IOException e) {
            logger.error("Error occurred during the processing of query file: " + e);
        }


        TimerTask task = new FileWatcher(new File(AAIConstants.AAI_HOME_ETC_QUERY)) {
            @Override
            protected void onChange(File file) {
                File queryFile = new File(AAIConstants.AAI_HOME_ETC_QUERY);
                try (FileInputStream fis = new FileInputStream(queryFile)){
                    properties.load(fis);
                    logger.debug("File: " + file + " was changed so the cluster is rebuild for gremlin server");
                } catch (FileNotFoundException e) {
                    logger.error("Unable to find the file: " + e);
                } catch (IOException e) {
                    logger.error("Error occurred during the processing of query file: " + e);
                }
            }
        };

        if (!timerSet) {
            timerSet = true;
            timer = new Timer();
            timer.schedule( task , new Date(), 10000 );
        }

    }

    public Cluster getCluster(){
        return cluster;
    }

    /**
     * Gets the key if the properties contains that key
     *
     * Purposely not checking if the property exists due
     * to if you check for the property and then get the property
     * Then you are going to have to synchronize the method
     *
     * @param key the query to check if it exists in the file
     * @return string if the key exists or null if it doesn't
     */
    public String getStoredQuery(String key){
        return (String) properties.get(key);
    }

}
