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
package org.onap.aai;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.onap.aai.aailog.logs.AaiDebugLog;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.logging.LogFormatTools;
import org.onap.aai.nodes.NodeIngestor;
import org.onap.aai.util.AAIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@EnableConfigurationProperties
@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        CassandraDataAutoConfiguration.class,
        CassandraAutoConfiguration.class
    })
// Component Scan provides a way to look for spring beans
// It only searches beans in the following packages
// Any method annotated with @Bean annotation or any class
// with @Component, @Configuration, @Service will be picked up
@ComponentScan(
    basePackages = {"org.onap.aai.config", "org.onap.aai.web", "org.onap.aai.setup",
        "org.onap.aai.tasks", "org.onap.aai.service", "org.onap.aai.rest", "org.onap.aai.aaf",
        "org.onap.aai.aailog", "org.onap.aai.introspection", "org.onap.aai.rest.notification"})
public class TraversalApp {

    private static final Logger logger = LoggerFactory.getLogger(TraversalApp.class.getName());

    private static AaiDebugLog debugLog = new AaiDebugLog();
    static {
        debugLog.setupMDC();
    }

    private static final String APP_NAME = "aai-traversal";
    private static Map<String, String> contextMap;

    @Autowired
    private Environment env;

    @Autowired
    private NodeIngestor nodeIngestor;

    @Autowired
    private SpringContextAware context;

    @Autowired
    private SpringContextAware loaderFactory;

    @PostConstruct
    private void init() throws AAIException {
        System.setProperty("org.onap.aai.serverStarted", "false");
        setDefaultProps();

        contextMap = MDC.getCopyOfContextMap();

        logger.debug("AAI Server initialization started...");

        // Setting this property to allow for encoded slash (/) in the path parameter
        // This is only needed for tomcat keeping this as temporary
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");

        logger.debug("Starting AAIGraph connections and the NodeInjestor");

        if (env.acceptsProfiles(Profiles.of(TraversalProfiles.TWO_WAY_SSL))
            && env.acceptsProfiles(Profiles.of(TraversalProfiles.ONE_WAY_SSL))) {
            logger.debug("You have seriously misconfigured your application");
        }

        AAIConfig.init();

        AAIGraph.getInstance();
    }

    @PreDestroy
    public void cleanup() {
        logger.debug("Traversal MicroService stopped");
        logger.info("Shutting down both realtime and cached connections");
        AAIGraph.getInstance().graphShutdown();
    }

    public static void main(String[] args) throws AAIException {

        setDefaultProps();

        Environment env = null;
        AAIConfig.init();

        try {
            SpringApplication app = new SpringApplication(TraversalApp.class);
            app.setLogStartupInfo(false);
            app.setRegisterShutdownHook(true);
            env = app.run(args).getEnvironment();
        } catch (Exception ex) {
            AAIException aai = schemaServiceExceptionTranslator(ex);
            ErrorLogHelper.logException(aai);
            ErrorLogHelper.logError(aai.getCode(),
                ex.getMessage() + ", resolve and restart Traversal");
            throw aai;
        }

        MDC.setContextMap(contextMap);
        logger.info("Application '{}' is running on {}!",
            env.getProperty("spring.application.name"), env.getProperty("server.port"));

        logger.debug("Traversal MicroService Started");
        System.out.println("Traversal Microservice Started");
    }

    public static void setDefaultProps() {

        if (System.getProperty("file.separator") == null) {
            System.setProperty("file.separator", "/");
        }

        String currentDirectory = System.getProperty("user.dir");
        System.setProperty("aai.service.name", TraversalApp.class.getSimpleName());

        if (System.getProperty("AJSC_HOME") == null) {
            System.setProperty("AJSC_HOME", ".");
        }

        if (currentDirectory.contains(APP_NAME)) {
            if (System.getProperty("BUNDLECONFIG_DIR") == null) {
                System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");
            }
        } else {
            if (System.getProperty("BUNDLECONFIG_DIR") == null) {
                System.setProperty("BUNDLECONFIG_DIR", "aai-traversal/src/main/resources");
            }
        }

    }

    private static AAIException schemaServiceExceptionTranslator(Exception ex) {
        AAIException aai = null;
        logger.info("Error Message is {} details - {}", ExceptionUtils.getRootCause(ex).toString(),
            ExceptionUtils.getRootCause(ex).getMessage());
        if (ExceptionUtils.getRootCause(ex) == null
            || ExceptionUtils.getRootCause(ex).getMessage() == null) {
            aai = new AAIException("AAI_3025",
                "Error parsing exception - Please Investigate" + LogFormatTools.getStackTop(ex));
        } else {
            logger.info("Exception is " + ExceptionUtils.getRootCause(ex).getMessage()
                + "Root cause is" + ExceptionUtils.getRootCause(ex).toString());
            if (ExceptionUtils.getRootCause(ex).getMessage().contains("NodeIngestor")) {
                aai = new AAIException("AAI_3026",
                    "Error reading OXM from SchemaService - Investigate");
            } else if (ExceptionUtils.getRootCause(ex).getMessage().contains("EdgeIngestor")) {
                aai = new AAIException("AAI_3027",
                    "Error reading EdgeRules from SchemaService - Investigate");
            } else if (ExceptionUtils.getRootCause(ex).getMessage()
                .contains("Connection refused")) {
                aai =
                    new AAIException("AAI_3025", "Error connecting to SchemaService - Investigate");
            } else {
                aai = new AAIException("AAI_3025",
                    "Error connecting to SchemaService - Please Investigate");
            }
        }

        return aai;
    }
}
