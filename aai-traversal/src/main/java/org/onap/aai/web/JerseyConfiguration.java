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
package org.onap.aai.web;

import static java.lang.Boolean.parseBoolean;
import static java.util.Comparator.comparingInt;
import com.google.common.collect.Sets;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Priority;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.onap.aai.rest.*;
import org.onap.aai.rest.search.ModelAndNamedQueryRestProvider;
import org.onap.aai.rest.search.SearchProvider;
import org.onap.aai.rest.util.EchoResponse;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Configuration
public class JerseyConfiguration {

    private static final Logger log = Logger.getLogger(JerseyConfiguration.class.getName());
    private static final org.slf4j.Logger logger =
        LoggerFactory.getLogger(JerseyConfiguration.class.getName());

    private static final String LOGGING_ENABLED_PROPERTY = "aai.request.logging.enabled";
    private static final boolean ENABLE_RESPONSE_LOGGING = false;

    private final Environment environment;

    public JerseyConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public ResourceConfig resourceConfig() {
        ResourceConfig resourceConfig = new ResourceConfig();

        resourceConfig.property(ServletProperties.FILTER_FORWARD_ON_404, true);
        Set<Class<?>> classes = Sets.newHashSet(SearchProvider.class,
            ModelAndNamedQueryRestProvider.class, QueryConsumer.class, RecentAPIConsumer.class, EchoResponse.class, CQ2Gremlin.class, CQ2GremlinTest.class);
        Set<Class<?>> filterClasses =
            Sets.newHashSet(
                org.onap.aai.interceptors.pre.RequestTransactionLogging.class,
                org.onap.aai.interceptors.pre.HeaderValidation.class,
                org.onap.aai.interceptors.pre.HttpHeaderInterceptor.class,
                org.onap.aai.interceptors.pre.OneWaySslAuthorization.class,
                org.onap.aai.interceptors.pre.VersionLatestInterceptor.class,
                org.onap.aai.interceptors.pre.RetiredInterceptor.class,
                org.onap.aai.interceptors.pre.VersionInterceptor.class,
                org.onap.aai.interceptors.pre.RequestHeaderManipulation.class,
                org.onap.aai.interceptors.pre.RequestModification.class,
                org.onap.aai.interceptors.post.InvalidResponseStatus.class,
                
                org.onap.aai.interceptors.post.ResponseTransactionLogging.class,
                org.onap.aai.interceptors.post.ResponseHeaderManipulation.class
                );
        resourceConfig.registerClasses(classes);
        logger.debug("REGISTERED CLASSES " + classes.toString());

        throwIfPriorityAnnotationAbsent(filterClasses);
        filterClasses.stream().filter(this::isEnabledByActiveProfiles).sorted(priorityComparator())
            .forEach(resourceConfig::register);

        filterClasses.stream().filter(this::isEnabledByActiveProfiles).sorted(priorityComparator())
            .forEach(s -> logger.debug("REGISTERED FILTERS " + s.getName()));
        return resourceConfig;
    }

    private <T> void throwIfPriorityAnnotationAbsent(Collection<Class<? extends T>> classes) {
        for (Class<? extends T> clazz : classes) {
            if (!clazz.isAnnotationPresent(Priority.class)) {
                logger.debug("throwIfPriorityAnnotationAbsent: missing filter priority for : "
                    + clazz.getName());
                throw new MissingFilterPriorityException(clazz);
            }
        }
    }

    private <T> Comparator<Class<? extends T>> priorityComparator() {
        return comparingInt(clazz -> clazz.getAnnotation(Priority.class).value());
    }

    private boolean isLoggingEnabled() {
        return parseBoolean(environment.getProperty(LOGGING_ENABLED_PROPERTY));
    }

    private boolean isEnabledByActiveProfiles(AnnotatedElement annotatedElement) {
        boolean result = !annotatedElement.isAnnotationPresent(Profile.class)
            || environment.acceptsProfiles(Profiles.of(annotatedElement.getAnnotation(Profile.class).value()));
        logger.debug("isEnabledByActiveProfiles: annotatedElement: " + annotatedElement.toString()
            + " result=" + result);
        return result;
    }

    private class MissingFilterPriorityException extends RuntimeException {
        private MissingFilterPriorityException(Class<?> clazz) {
            super("Container filter " + clazz.getName() + " does not have @Priority annotation");
        }
    }
}
