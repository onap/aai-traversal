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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.search;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.aai.logging.LogFormatTools;
import org.onap.aai.util.AAIConstants;
import org.onap.aai.util.FileWatcher;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LocalCQConfig extends CQConfig {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(LocalCQConfig.class);

    @Value("${schema.queries.location}")
    private String storedQueriesLocation;

    private boolean timerSet;
    private Timer timer;

    @PostConstruct
    public void init() {

        try {
            String filepath = storedQueriesLocation + AAIConstants.AAI_FILESEP + "stored-queries.json";
            logger.info("Using the Local stored queries");
            Path path = Paths.get(filepath);
            String customQueryConfigJson = new String(Files.readAllBytes(path));
            queryConfig = new GetCustomQueryConfig(customQueryConfigJson);

        } catch (IOException e) {
            logger.error("Error occurred during the processing of query json file: " + LogFormatTools.getStackTop(e));
        }

        TimerTask task = new FileWatcher(new File(storedQueriesLocation)) {
            @Override
            protected void onChange(File file) {
                try {
                    String filepath = storedQueriesLocation;
                    Path path = Paths.get(filepath);
                    String customQueryConfigJson = new String(Files.readAllBytes(path));
                    queryConfig = new GetCustomQueryConfig(customQueryConfigJson);

                } catch (IOException e) {
                    logger.error("Error occurred during the processing of query json file: " + LogFormatTools.getStackTop(e));
                }
            }
        };

        if (!timerSet) {
            timerSet = true;
            timer = new Timer();
            timer.schedule(task, new Date(), 10000);
        }

    }

}
