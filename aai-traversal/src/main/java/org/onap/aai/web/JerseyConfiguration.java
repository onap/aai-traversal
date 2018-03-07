package org.onap.aai.web;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.onap.aai.rest.QueryConsumer;
import org.onap.aai.rest.retired.V3ThroughV7Consumer;
import org.onap.aai.rest.search.ModelAndNamedQueryRestProvider;
import org.onap.aai.rest.search.SearchProvider;
import org.onap.aai.rest.util.EchoResponse;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Priority;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
@ApplicationPath("/aai")
public class JerseyConfiguration extends ResourceConfig {

    private static final Logger log = Logger.getLogger(JerseyConfiguration.class.getName());

    private Environment env;

    @Autowired
    public JerseyConfiguration(Environment env) {

        this.env = env;

        register(SearchProvider.class);
        register(ModelAndNamedQueryRestProvider.class);
        register(QueryConsumer.class);

        register(V3ThroughV7Consumer.class);
        register(EchoResponse.class);

        //Request Filters
        registerFiltersForRequests();
        // Response Filters
        registerFiltersForResponses();

        property(ServletProperties.FILTER_FORWARD_ON_404, true);

        // Following registers the request headers and response headers
        // If the LoggingFilter second argument is set to true, it will print response value as well
        if ("true".equalsIgnoreCase(env.getProperty("aai.request.logging.enabled"))) {
            register(new LoggingFilter(log, false));
        }
    }

    public void registerFiltersForRequests() {

        // Find all the classes within the interceptors package
        Reflections reflections = new Reflections("org.onap.aai.interceptors");
        // Filter them based on the clazz that was passed in
        Set<Class<? extends ContainerRequestFilter>> filters = reflections.getSubTypesOf(ContainerRequestFilter.class);


        // Check to ensure that each of the filter has the @Priority annotation and if not throw exception
        for (Class filterClass : filters) {
            if (filterClass.getAnnotation(Priority.class) == null) {
                throw new RuntimeException("Container filter " + filterClass.getName() + " does not have @Priority annotation");
            }
        }

        // Turn the set back into a list
        List<Class<? extends ContainerRequestFilter>> filtersList = filters
                .stream()
                .filter(f -> {
                    if (f.isAnnotationPresent(Profile.class)
                            && !env.acceptsProfiles(f.getAnnotation(Profile.class).value())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Sort them by their priority levels value
        filtersList.sort((c1, c2) -> Integer.valueOf(c1.getAnnotation(Priority.class).value()).compareTo(c2.getAnnotation(Priority.class).value()));

        // Then register this to the jersey application
        filtersList.forEach(this::register);
    }

    public void registerFiltersForResponses() {

        // Find all the classes within the interceptors package
        Reflections reflections = new Reflections("org.onap.aai.interceptors");
        // Filter them based on the clazz that was passed in
        Set<Class<? extends ContainerResponseFilter>> filters = reflections.getSubTypesOf(ContainerResponseFilter.class);


        // Check to ensure that each of the filter has the @Priority annotation and if not throw exception
        for (Class filterClass : filters) {
            if (filterClass.getAnnotation(Priority.class) == null) {
                throw new RuntimeException("Container filter " + filterClass.getName() + " does not have @Priority annotation");
            }
        }

        // Turn the set back into a list
        List<Class<? extends ContainerResponseFilter>> filtersList = filters.stream()
                .filter(f -> {
                    if (f.isAnnotationPresent(Profile.class)
                            && !env.acceptsProfiles(f.getAnnotation(Profile.class).value())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Sort them by their priority levels value
        filtersList.sort((c1, c2) -> Integer.valueOf(c1.getAnnotation(Priority.class).value()).compareTo(c2.getAnnotation(Priority.class).value()));

        // Then register this to the jersey application
        filtersList.forEach(this::register);
    }

}
