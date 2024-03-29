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
package org.onap.aai;

public final class TraversalProfiles {

    public static final String DMAAP = "dmaap";
    public static final String DME2 = "dme2";

    public static final String ONE_WAY_SSL = "one-way-ssl";
    // AAF Basic Auth
    public static final String AAF_AUTHENTICATION = "aaf-auth";
    // AAF Auth with Client Certs
    public static final String AAF_CERT_AUTHENTICATION = "aaf-cert-auth";
    public static final String TWO_WAY_SSL = "two-way-ssl";

    private TraversalProfiles() {
    }
}
