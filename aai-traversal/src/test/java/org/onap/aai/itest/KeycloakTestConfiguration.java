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
package org.onap.aai.itest;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class KeycloakTestConfiguration {

    @Bean
    public AdapterConfig adapterConfig() {
        return new KeycloakSpringBootProperties();
    }

    @Bean
    KeycloakContainer keycloakContainer(KeycloakTestProperties properties) {
        KeycloakContainer keycloak = new KeycloakContainer("jboss/keycloak:12.0.4")
                .withRealmImportFile(properties.realmJson)
                .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig().withPortBindings(new PortBinding(Ports.Binding.bindPort(Integer.parseInt(properties.port)), new ExposedPort(8080)))
                ));
        keycloak.start();
        return keycloak;
    }

    @Bean
    Keycloak keycloakAdminClient(KeycloakContainer keycloak, KeycloakTestProperties properties) {
        return KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm(properties.realm)
                .clientId(properties.adminCli)
                .username(keycloak.getAdminUsername())
                .password(keycloak.getAdminPassword())
                .build();
    }

    @Bean
    RoleHandler roleHandler(Keycloak adminClient, KeycloakTestProperties properties) {
        return new RoleHandler(adminClient, properties);
    }

    @Bean
    KeycloakTestProperties properties() {
        return new KeycloakTestProperties();
    }
}
