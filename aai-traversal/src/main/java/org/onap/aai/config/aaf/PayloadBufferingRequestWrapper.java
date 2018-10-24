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
package org.onap.aai.config.aaf;

import org.apache.commons.io.IOUtils;
import org.onap.aaf.cadi.BufferedServletInputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * This class buffers the payload of the servlet request. The reason is that we access the payload multiple times,
 * which is not supported by the request per se.
 */

class PayloadBufferingRequestWrapper extends HttpServletRequestWrapper {

    private byte[] buffer;

    PayloadBufferingRequestWrapper(HttpServletRequest req) throws IOException {
        super(req);
        this.buffer = IOUtils.toByteArray(req.getInputStream());
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream bais = new ByteArrayInputStream(this.buffer);
        return new BufferedServletInputStream(bais);
    }
}
