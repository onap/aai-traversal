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
package org.onap.aai.util;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.onap.aai.dbmap.AAIGraph;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.ModelInjestor;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.logging.LogFormatTools;
import org.onap.aai.logging.LoggingContext;
import org.onap.aai.logging.LoggingContext.StatusCode;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class AAIAppServletContextListener implements ServletContextListener {

	private static final String MICRO_SVC="aai-traversal";
	private static final String ACTIVEMQ_TCP_URL = "tcp://localhost:61446";
	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(AAIAppServletContextListener.class.getName());	

	/**
	 * Destroys Context
	 * 
	 * @param arg0 the ServletContextEvent
	 */
	public void contextDestroyed(ServletContextEvent arg0) {		
	}

	/**
	 * Initializes Context
	 * 
	 * @param arg0 the ServletContextEvent
	 */
	public void contextInitialized(ServletContextEvent arg0) {
		System.setProperty("org.onap.aai.serverStarted", "false");
		System.setProperty("aai.service.name", "traversal");
		
		LoggingContext.save();
		LoggingContext.component("init");
		LoggingContext.partnerName("NA");
		LoggingContext.targetEntity(MICRO_SVC);
		LoggingContext.requestId(UUID.randomUUID().toString());
		LoggingContext.serviceName(MICRO_SVC);
		LoggingContext.targetServiceName("contextInitialized");
		LoggingContext.statusCode(StatusCode.COMPLETE);
		LOGGER.info("AAI Server initialization started...");
		try {
			LOGGER.info("Loading aaiconfig.properties");
			AAIConfig.init();

			LOGGER.info("Loading error.properties");
			ErrorLogHelper.loadProperties();

			LOGGER.info("Loading graph database");

			AAIGraph.getInstance();
			ModelInjestor.getInstance();

			LOGGER.info("A&AI Server initialization succcessful.");
			System.setProperty("activemq.tcp.url", ACTIVEMQ_TCP_URL);
			System.setProperty("org.onap.aai.serverStarted", "true");

			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					LOGGER.info("AAIGraph shutting down");
					AAIGraph.getInstance().graphShutdown();
					LOGGER.info("AAIGraph shutdown");
					System.out.println("Shutdown hook triggered.");
				}
			});

		} catch (AAIException e) {
			ErrorLogHelper.logException(e);
			throw new RuntimeException("AAIException caught while initializing A&AI server", e);
		} catch (IOException e) {
			ErrorLogHelper.logError("AAI_4000", e.getMessage());
			throw new RuntimeException("IOException caught while initializing A&AI server", e);
		} catch (Exception e) {
			LOGGER.error("Unknown failure while initializing A&AI Server" + LogFormatTools.getStackTop(e));
			throw new RuntimeException("Unknown failure while initializing A&AI server", e);
		}

		LOGGER.info("Graph-Query MicroService Started");
		LOGGER.debug("Graph-Query MicroService Started");
		LoggingContext.restore();

	}
}
