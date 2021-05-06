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

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;

import java.util.Collections;

class RoleHandler {

     /**
        Following roles should be the same as given roles in multi-tenancy-realm json file
      */
    final static String OPERATOR = "operator";
    private final Keycloak adminClient;
    private final KeycloakTestProperties properties;

    RoleHandler(Keycloak adminClient, KeycloakTestProperties properties) {
        this.adminClient = adminClient;
        this.properties = properties;
    }

    void addToUser(String role, String username) {
        RealmResource realm = adminClient.realm(properties.realm);
        realm.users().get(username)
                .roles()
                .realmLevel()
                .add(Collections.singletonList(realm.roles().get(role).toRepresentation()));
    }

    void removeFromUser(String role, String username) {
        RealmResource realm = adminClient.realm(properties.realm);
        realm.users().get(username)
                .roles()
                .realmLevel()
                .remove(Collections.singletonList(realm.roles().get(role).toRepresentation()));
    }
}
