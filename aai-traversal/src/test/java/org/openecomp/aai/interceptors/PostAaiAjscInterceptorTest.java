/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.aai.interceptors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.aai.logging.LoggingContext;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PostAaiAjscInterceptorTest {

    private PostAaiAjscInterceptor postAaiAjscInterceptor;

    @Before
    public void setup(){
        postAaiAjscInterceptor = new PostAaiAjscInterceptor();
    }

    @Test
    public void getInstance() throws Exception {
        PostAaiAjscInterceptor interceptor = PostAaiAjscInterceptor.getInstance();
        assertNotNull(interceptor);
    }

    @Test
    public void testAllowOrRejectIfSuccess() throws Exception {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        LoggingContext.put(LoggingContext.LoggingField.RESPONSE_CODE.toString(), "SUCCESS");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("/fadsjoifj"));

        boolean success = postAaiAjscInterceptor.allowOrReject(request, null, null);

        assertTrue("Expecting the post interceptor to return success regardless", success);
    }

    @Test
    public void testAllowOrRejectIfFailure() throws Exception {

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        LoggingContext.put(LoggingContext.LoggingField.RESPONSE_CODE.toString(), "ERR.");
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("/fadsjoifj"));

        boolean success = postAaiAjscInterceptor.allowOrReject(request, null, null);

        assertTrue("Expecting the post interceptor to return success regardless", success);
    }
}
