package org.onap.aai.service;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.eclipse.jetty.util.security.Password;
import org.onap.aai.util.AAIConstants;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class AuthorizationService {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AuthorizationService.class);

    private final Map<String, String> authorizedUsers = new HashMap<>();

    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    @PostConstruct
    public void init(){

        String basicAuthFile = getBasicAuthFilePath();

        try(Stream<String> stream = Files.lines(Paths.get(basicAuthFile))){
            stream.filter(line -> !line.startsWith("#")).forEach(str -> {
                byte [] bytes = null;

                String usernamePassword = null;
                String accessType = null;

                try {
                    String [] userAccessType = str.split(",");

                    if(userAccessType == null || userAccessType.length != 2){
                        throw new RuntimeException("Please check the realm.properties file as it is not conforming to the basic auth");
                    }

                    usernamePassword = userAccessType[0];
                    accessType       = userAccessType[1];

                    String[] usernamePasswordArray = usernamePassword.split(":");

                    if(usernamePasswordArray == null || usernamePasswordArray.length != 3){
                        throw new RuntimeException("Not a valid entry for the realm.properties entry: " + usernamePassword);
                    }

                    String username = usernamePasswordArray[0];
                    String password = null;

                    if(str.contains("OBF:")){
                        password = usernamePasswordArray[1] + ":" + usernamePasswordArray[2];
                        password = Password.deobfuscate(password);
                    }

                    bytes = ENCODER.encode((username + ":" + password).getBytes("UTF-8"));

                    authorizedUsers.put(new String(bytes), accessType);

                } catch (UnsupportedEncodingException e)
                {
                    logger.error("Unable to support the encoding of the file" + basicAuthFile);
                }

                authorizedUsers.put(new String(ENCODER.encode(bytes)), accessType);
            });
        } catch (IOException e) {
            logger.error("IO Exception occurred during the reading of realm.properties", e);
        }
    }

    public boolean checkIfUserAuthorized(String authorization){
        return authorizedUsers.containsKey(authorization) && "admin".equals(authorizedUsers.get(authorization));
    }

    public String getBasicAuthFilePath(){
        return AAIConstants.AAI_HOME_ETC_AUTH + AAIConstants.AAI_FILESEP + "realm.properties";
    }
}
