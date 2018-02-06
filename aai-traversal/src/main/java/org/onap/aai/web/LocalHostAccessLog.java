package org.onap.aai.web;

import ch.qos.logback.access.jetty.RequestLogImpl;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class LocalHostAccessLog {

	@Bean
	public EmbeddedServletContainerFactory jettyConfigBean(){
		JettyEmbeddedServletContainerFactory jef = new JettyEmbeddedServletContainerFactory();
		jef.addServerCustomizers((JettyServerCustomizer) server -> {

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
        });
		return jef;
	}
}
