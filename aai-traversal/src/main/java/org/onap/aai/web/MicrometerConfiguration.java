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

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.jersey2.server.JerseyTags;
import io.micrometer.jersey2.server.JerseyTagsProvider;
import org.glassfish.jersey.server.ContainerResponse;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Class to add customized tags to http metrics scraped in /actuator/prometheus
 * endpoint
 */
@Configuration
@ConditionalOnProperty(value = "scrape.uri.metrics", havingValue = "true")
public class MicrometerConfiguration {
    private static final String TAG_AAI_URI = "aai_uri";
    private static final String NOT_AVAILABLE = "NOT AVAILABLE";

    @Bean
    public JerseyTagsProvider jerseyTagsProvider() {
        return new JerseyTagsProvider() {
            @Override
            public Iterable httpRequestTags(RequestEvent event) {
                ContainerResponse response = event.getContainerResponse();
                return Tags.of(JerseyTags.method(event.getContainerRequest()),
                    JerseyTags.exception(event), JerseyTags.status(response),
                    JerseyTags.outcome(response), getAaiUriTag(event));
            }

            private Tag getAaiUriTag(RequestEvent event) {
                String aai_uri = event.getUriInfo().getRequestUri().toString();
                if (aai_uri == null) {
                    aai_uri = NOT_AVAILABLE;
                }
                return Tag.of(TAG_AAI_URI, aai_uri);
            }

            @Override
            public Iterable<Tag> httpLongRequestTags(RequestEvent event) {
                return Tags.of(JerseyTags.method(event.getContainerRequest()),
                    JerseyTags.uri(event));
            }
        };
    }
}
