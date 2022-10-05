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
package org.onap.aai.rest.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class ConvertQueryPropertiesToJson {

    private static final int MAX_FILE_SIZE = 256000;

    private void addStart(StringBuilder sb) {
        sb.append("{\n  \"stored-queries\":[{\n");
    }

    private void addRequiredQueryProperties(StringBuilder sb, List<String> rqd) {
        Iterator it = rqd.iterator();
        sb.append("      \"query\":{\n        \"required-properties\":[");
        while (it.hasNext()) {
            sb.append("\"" + it.next() + "\"");
            if (it.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("]\n      },\n");
    }

    private void addAnotherQuery(StringBuilder sb, String queryName, String query,
        List<String> rqd) {
        sb.append("    \"" + queryName + "\":{\n");
        if (!rqd.isEmpty()) {
            addRequiredQueryProperties(sb, rqd);
        }
        sb.append("      \"stored-query\":\"" + query + "\"\n    }\n  },{\n");
    }

    private void addLastQuery(StringBuilder sb, String queryName, String query, List<String> rqd) {
        sb.append("    \"" + queryName + "\":{\n");
        if (!rqd.isEmpty()) {
            addRequiredQueryProperties(sb, rqd);
        }
        sb.append("      \"stored-query\":\"" + query + "\"\n    }\n  }]\n}\n");
    }

    private String get2ndParameter(String paramString) {
        String endParams = paramString.substring(0, paramString.indexOf(')'));
        String result = endParams.substring(endParams.indexOf(',') + 1);
        String lastParam = result.trim();
        if (lastParam.startsWith("\\") || lastParam.startsWith("'")
            || lastParam.startsWith("new ")) {
            return null;
        }

        return lastParam;
    }

    private List<String> findRqdProperties(String query) {
        String[] parts = query.split("getVerticesByProperty");
        List<String> result = new ArrayList<>();
        if (parts.length == 1)
            return result;
        int count = 0;
        String foundRqdProperty;
        while (count++ < parts.length - 1) {
            foundRqdProperty = get2ndParameter(parts[count]);
            if (foundRqdProperty != null && !result.contains(foundRqdProperty)) {
                result.add(foundRqdProperty);
            }
        }
        return result;
    }

    public String convertProperties(Properties props) {
        Enumeration<?> e = props.propertyNames();
        StringBuilder sb = new StringBuilder(MAX_FILE_SIZE);
        String queryName;
        String query;
        addStart(sb);
        List<String> rqd;
        while (e.hasMoreElements()) {
            queryName = (String) e.nextElement();
            query = props.getProperty(queryName).trim().replace("\"", "\\\"");
            rqd = findRqdProperties(query);
            if (e.hasMoreElements()) {
                addAnotherQuery(sb, queryName, query, rqd);
            } else {
                addLastQuery(sb, queryName, query, rqd);
            }
        }

        return sb.toString();
    }
}
