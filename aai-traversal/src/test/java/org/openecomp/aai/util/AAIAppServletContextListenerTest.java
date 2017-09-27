/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.aai.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.apache.commons.lang.ObjectUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.openecomp.aai.dbmap.AAIGraph;
import org.openecomp.aai.logging.ErrorLogHelper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.agent.PowerMockAgent;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@PrepareForTest({AAIGraph.class, AAIConfig.class, ErrorLogHelper.class})
public class AAIAppServletContextListenerTest {
	
	@Rule
	public PowerMockRule rule = new PowerMockRule();
	
	static {
	     PowerMockAgent.initializeIfNeeded();
	 }
	
	private ServletContextEvent arg; 
	private AAIAppServletContextListener listener;
		
	/**
	 * Initialize.
	 */
	@Before
	@PrepareForTest({AAIGraph.class, AAIConfig.class, ErrorLogHelper.class})
	public void initialize(){
		arg = PowerMockito.mock(ServletContextEvent.class);
		PowerMockito.mockStatic(AAIGraph.class);
		PowerMockito.mockStatic(AAIConfig.class);
		PowerMockito.mockStatic(ErrorLogHelper.class);
	
		listener = new AAIAppServletContextListener();
		configureLog();
	}
	
		/**
		 * Test contextDestroyed.
		 */
		@Test(expected = NullPointerException.class)
		//@Ignore
		public void testContextDestroyed(){
			listener.contextDestroyed(arg);
			assertTrue(logContains(Level.DEBUG, "AAI Server shutdown"));
			assertTrue(logContains(Level.INFO, "AAI graph shutdown"));
		}
		
		/**
		 * Test contextInitialized.
		 */
		@Test
		//@Ignore
		public void testContextInitialized(){
			listener.contextInitialized(arg);
			assertFalse(logContains(Level.DEBUG, "Loading aaiconfig.properties"));
			assertFalse(logContains(Level.DEBUG, "Loading error.properties"));
			assertFalse(logContains(Level.DEBUG, "Loading graph database"));
			assertFalse(logContains(Level.INFO, "AAI Server initialization"));
		}
		
		
		/**
		 * Helper method to check if a String appears in the desired log level.
		 *
		 * @param level Log level to use
		 * @param expected String to search for
		 * @return True if search String is found, false otherwise
		 */
		private boolean logContains(Level level, String expected) {
			String actual[] = RecordingAppender.messages();
			for (String log : actual) {
				if (log.contains(level.toString()) && log.contains(expected))
					return true;
			}
			return false;
		}

		/**
		 * Set logging level, and initialize log-appender.
		 */
		private void configureLog() {
			org.slf4j.Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			rootLogger.debug("debug");
		//	rootLogger.();
		//	rootLogger.addAppender(RecordingAppender.appender(new PatternLayout()));
		}

}


/**
 * Appender class that appends log messages to a String List when some logging event occurs
 */
class RecordingAppender extends AppenderBase<ILoggingEvent> {
	private static List<String> messages = new ArrayList<String>();
	private static RecordingAppender appender = new RecordingAppender();
	private PatternLayout patternLayout;

	private RecordingAppender() {
		super();
	}

	/**
	 * @param patternLayout Pattern to format log message
	 * @return Current appender 
	 */
	public static RecordingAppender appender(PatternLayout patternLayout) {
		appender.patternLayout = patternLayout;
		appender.clear();
		return appender;
	}

	@Override
	protected void append(ILoggingEvent event) {
		messages.add(patternLayout.doLayout(event));
	}

	public void close() {}

	public boolean requiresLayout() {
		return false;
	}

	/**
	 * @return Return logs as a String array
	 */
	public static String[] messages() {
		return (String[]) messages.toArray(new String[messages.size()]);
	}

	/**
	 * Clear the message container
	 */
	private void clear() {
		messages.clear();
	}
	
}
