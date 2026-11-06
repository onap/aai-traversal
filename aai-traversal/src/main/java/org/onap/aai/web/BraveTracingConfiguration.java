package org.onap.aai.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.micrometer.tracing.brave.autoconfigure.BraveAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "management.tracing.enabled", havingValue = "true")
@Import(BraveAutoConfiguration.class)
public class BraveTracingConfiguration {
}