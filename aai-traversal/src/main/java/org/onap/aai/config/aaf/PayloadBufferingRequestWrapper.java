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
