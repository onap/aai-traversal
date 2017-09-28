/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.interceptors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;

public class PreAaiAjscInterceptorTest {

    private PreAaiAjscInterceptor preAaiAjscInterceptor;

    @Before
    public void setup(){
        preAaiAjscInterceptor = new PreAaiAjscInterceptor();
    }

    @Test
    public void getInstance() throws Exception {
        PreAaiAjscInterceptor interceptor = PreAaiAjscInterceptor.getInstance();
        assertNotNull(interceptor);
    }

    @Test
    public void testAllowOrRejectIfSuccess() throws Exception {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(request.getRequestURI()).thenReturn("/fadsjoifj");
        Mockito.when(request.getHeader(anyString())).thenReturn("JUNIT-Test");
        Mockito.when(request.getMethod()).thenReturn("GET");

        boolean success = preAaiAjscInterceptor.allowOrReject(request, null, null);

        assertTrue("Expecting the post interceptor to return success regardless", success);
    }

}