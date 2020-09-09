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
package org.onap.aai.web;

import ch.qos.logback.access.jetty.RequestLogImpl;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class LocalHostAccessLog {

    @Bean
    public AbstractServletWebServerFactory jettyConfigBean(
            @Value("${jetty.threadPool.maxThreads:200}") final String maxThreads,
            @Value("${jetty.threadPool.minThreads:8}") final String minThreads
    ){

        JettyServletWebServerFactory jef = new JettyServletWebServerFactory();
        jef.addServerCustomizers((org.springframework.boot.web.embedded.jetty.JettyServerCustomizer) server -> {

            HandlerCollection handlers = new HandlerCollection();

            Arrays.stream(server.getHandlers()).forEach(handlers::addHandler);

            RequestLogHandler requestLogHandler = new RequestLogHandler();
            requestLogHandler.setServer(server);

            RequestLogImpl requestLogImpl = new RequestLogImpl();
            requestLogImpl.setResource("/localhost-access-logback.xml");
            requestLogImpl.start();

            requestLogHandler.setRequestLog(requestLogImpl);
            handlers.addHandler(requestLogHandler);
            server.setHandler(handlers);

            final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
            threadPool.setMaxThreads(Integer.valueOf(maxThreads));
            threadPool.setMinThreads(Integer.valueOf(minThreads));
        });
        return jef;
    }
}
