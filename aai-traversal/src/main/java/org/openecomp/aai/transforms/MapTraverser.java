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

package org.openecomp.aai.transforms;


import joptsimple.internal.Objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapTraverser {

    private Converter converter;

    public MapTraverser(Converter converter){
        this.converter = converter;
    }

    public Map<String, Object> convertKeys(Map<String, Object> map){

        Objects.ensureNotNull(map);

        Map<String, Object> modifiedMap = new HashMap<String, Object>();
        convertKeys(map, modifiedMap);

        return modifiedMap;
    }

    private Map<String, Object> convertKeys(Map<String, Object> original, Map<String, Object> modified){

        for(Map.Entry<String, Object> entry : original.entrySet()){
            String key = entry.getKey();
            key = converter.convert(key);
            Object value = entry.getValue();
            if(value instanceof Map){
                modified.put(key, convertKeys((Map<String, Object>)value, new HashMap<String, Object>()));
            } else if(value instanceof List){
                modified.put(key, convertKeys((List<Object>) value));
            } else {
                modified.put(key, value);
            }
        }

        return modified;
    }

    public List<Object> convertKeys(List<Object> list){

        List<Object> modifiedList = new ArrayList<Object>();
        if(list != null && list.size() > 0){

            for(Object o : list){
                if(o instanceof Map){
                    Map<String, Object> map = (Map<String, Object>) o;
                    modifiedList.add(convertKeys(map));
                } else if(o instanceof List){
                    List<Object> l = (List<Object>) o;
                    modifiedList.add(convertKeys(l));
                } else {
                    modifiedList.add(o);
                }
            }
        }

        return modifiedList;
    }
}
