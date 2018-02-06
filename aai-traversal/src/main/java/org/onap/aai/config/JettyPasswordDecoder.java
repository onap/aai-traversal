package org.onap.aai.config;

import org.eclipse.jetty.util.security.Password;

public class JettyPasswordDecoder implements PasswordDecoder {

    @Override
    public String decode(String input) {
        if (input.startsWith("OBF:")) {
            return Password.deobfuscate(input);
        }
        return Password.deobfuscate("OBF:" + input);
    }
}
