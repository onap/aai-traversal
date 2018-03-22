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
package org.onap.aai;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.aai.config.PropertyPasswordConfiguration;
import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.ModelInjestor;
import org.onap.aai.logging.LoggingContext;
import org.onap.aai.util.AAIConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.UUID;

@SpringBootApplication
// Component Scan provides a way to look for spring beans
// It only searches beans in the following packages
// Any method annotated with @Bean annotation or any class
// with @Component, @Configuration, @Service will be picked up
@ComponentScan(basePackages = {
		"org.onap.aai.config",
		"org.onap.aai.web",
		"org.onap.aai.tasks",
		"org.onap.aai.rest",
		"com.att.ajsc.common"
})
@EnableAutoConfiguration(exclude = {
		DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class
})
public class TraversalApp {

	private static final EELFLogger logger = EELFManager.getInstance().getLogger(TraversalApp.class.getName());

	private static final String APP_NAME = "aai-traversal";

	@Autowired
	private Environment env;

	@PostConstruct
	private void init() throws AAIException {
		System.setProperty("org.onap.aai.serverStarted", "false");
		setDefaultProps();

		LoggingContext.save();
		LoggingContext.component("init");
		LoggingContext.partnerName("NA");
		LoggingContext.targetEntity(APP_NAME);
		LoggingContext.requestId(UUID.randomUUID().toString());
		LoggingContext.serviceName(APP_NAME);
		LoggingContext.targetServiceName("contextInitialized");

		logger.info("AAI Server initialization started...");

		// Setting this property to allow for encoded slash (/) in the path parameter
		// This is only needed for tomcat keeping this as temporary
		System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");

	    logger.info("Starting AAIGraph connections and the ModelInjestor");

	    if(env.acceptsProfiles(Profiles.TWO_WAY_SSL) && env.acceptsProfiles(Profiles.ONE_WAY_SSL)){
	        logger.warn("You have seriously misconfigured your application");
	    }

		AAIConfig.init();
		ModelInjestor.getInstance();
		AAIGraph.getInstance();
	}

	@PreDestroy
	public void cleanup(){
		logger.info("Shutting down both realtime and cached connections");
		AAIGraph.getInstance().graphShutdown();
	}

	public static void main(String[] args) {

	    setDefaultProps();
		SpringApplication app = new SpringApplication(TraversalApp.class);
		app.setRegisterShutdownHook(true);
		app.addInitializers(new PropertyPasswordConfiguration());
		Environment env = app.run(args).getEnvironment();

		logger.info(
				"Application '{}' is running on {}!" ,
				env.getProperty("spring.application.name"),
				env.getProperty("server.port")
		);

		logger.info("Traversal MicroService Started");
		logger.error("Traversal MicroService Started");
		logger.debug("Traversal MicroService Started");
		System.out.println("Traversal Microservice Started");
	}

	public static void setDefaultProps(){

		if (System.getProperty("file.separator") == null) {
			System.setProperty("file.separator", "/");
		}

		String currentDirectory = System.getProperty("user.dir");

		if (System.getProperty("AJSC_HOME") == null) {
			System.setProperty("AJSC_HOME", ".");
		}

		if(currentDirectory.contains(APP_NAME)){
			if (System.getProperty("BUNDLECONFIG_DIR") == null) {
				System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");
			}
		} else {
			if (System.getProperty("BUNDLECONFIG_DIR") == null) {
				System.setProperty("BUNDLECONFIG_DIR", "aai-traversal/src/main/resources");
			}
		}

	}
}
