package org.onap.aai.rest.search;

/*-
 * ============LICENSE_START=======================================================
 * org.onap.aai
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.onap.aai.util.AAIConstants;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class GetCustomQueryConfig {

	private JsonArray storedQueries = null;
	private CustomQueryConfig customQueryConfig;
	
	
	private final static String QUERY_CONFIG = "query";
	private final static String REQUIRED_CONFIG = "required-properties";
	private final static String OPTIONAL_CONFIG = "optional-properties";
	private final static String STORED_QUERIES_CONFIG = "stored-queries";
	private final static String STORED_QUERY_CONFIG = "stored-query";
	
	public static final String AAI_HOME_ETC_QUERY_JSON = AAIConstants.AAI_HOME_ETC + "query" + AAIConstants.AAI_FILESEP + "stored-queries.json";
	
	public GetCustomQueryConfig(String customQueryJson ) {
		init(customQueryJson);
	}
	
	private void init( String customQueryJson) {
		JsonParser parser = new JsonParser();
		JsonObject queriesObject = parser.parse(customQueryJson).getAsJsonObject();
		if (queriesObject.has(STORED_QUERIES_CONFIG)) {
			
			storedQueries = queriesObject.getAsJsonArray(STORED_QUERIES_CONFIG);
		}
	}

	private List<String> toStringList(JsonArray array) {
	   Gson converter = new Gson(); 
	   Type listType = new TypeToken<List<String>>() {}.getType();
	   return converter.fromJson(array, listType);
	}
	
	private List<String> getPropertyList(JsonObject configObject, String config ) {
		JsonElement subqueryConfig;
		JsonArray props;
		
		if ( configObject.has(config)) {
			subqueryConfig = configObject.get(config);
			if ( subqueryConfig != null && !subqueryConfig.isJsonNull() ) {
				props = subqueryConfig.getAsJsonArray();
				if ( props != null ) {
					return toStringList(props);
				}
			}
		}
		return toStringList(null);
	}
	
	private String getPropertyString(JsonObject configObject, String config) {
		JsonElement subqueryConfig;
		
		if ( configObject.has(config)) {
			subqueryConfig = configObject.get(config);
			if ( subqueryConfig != null && !subqueryConfig.isJsonNull() ) {
				return subqueryConfig.getAsString();
			}
		}
		return null;
	}
	
	private void getStoredQueryBlock( JsonObject configObject, String config ) {
		if ( !configObject.has(config)) {
			return;
		}
		
		JsonElement queryConfig;
		JsonObject subObject;
		String multipleStartNodes;
		List<String> propertyList;

		queryConfig = configObject.get(config);
		subObject = queryConfig.getAsJsonObject();
		propertyList = getPropertyList(subObject, REQUIRED_CONFIG);
		if ( QUERY_CONFIG.equals(config)) {
			customQueryConfig.setQueryRequiredProperties( propertyList );
		} else {
			customQueryConfig.setQueryRequiredProperties( null );
		}

		propertyList = getPropertyList(subObject, OPTIONAL_CONFIG);
		if ( QUERY_CONFIG.equals(config)) {
			customQueryConfig.setQueryOptionalProperties( propertyList );
		} else {
			customQueryConfig.setQueryOptionalProperties( null );
		}
			
	}
	
	
	public CustomQueryConfig getStoredQuery(String queryName ) {
	
		customQueryConfig = null;
		JsonObject configObject;
		JsonElement query;
		JsonElement queryConfig;
		String queryString;

		for (JsonElement storedQuery : storedQueries) {
			if (storedQuery.isJsonObject()) {
				JsonObject queryObject = storedQuery.getAsJsonObject();
				query = queryObject.get(queryName);
				if ( query != null ) {
					customQueryConfig = new CustomQueryConfig();
					configObject = query.getAsJsonObject();
					getStoredQueryBlock(configObject, QUERY_CONFIG);
					if ( configObject.has(STORED_QUERY_CONFIG)) {
						queryConfig = configObject.get(STORED_QUERY_CONFIG);
						customQueryConfig.setQuery(queryConfig.getAsString());
					}
					break;
				} 
			}
		}

		return customQueryConfig;
		
	}



}
