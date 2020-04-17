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
package org.onap.aai.rest;

import org.onap.aai.config.SpringContextAware;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.search.CustomQueryConfigDTO;
import org.onap.aai.rest.search.CustomQueryDTO;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.restcore.search.GroovyQueryBuilder;
import org.onap.aai.serialization.db.EdgeSerializer;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.setup.SchemaVersions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Path("/cq2gremlin")
public class CQ2Gremlin extends RESTAPI {

	private HttpEntry traversalUriHttpEntry;


	@Autowired
	protected LoaderFactory loaderFactory;
	
	@Autowired
	protected EdgeSerializer rules;
	
	
	@Autowired
	public CQ2Gremlin(
		HttpEntry traversalUriHttpEntry,
		@Value("${schema.uri.base.path}") String basePath
	){
		this.traversalUriHttpEntry  = traversalUriHttpEntry;
	}
	
	@PUT
	@Path("")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getC2Qgremlin(@RequestBody Map<String, CustomQueryConfigDTO> content,@Context HttpHeaders headers, @Context UriInfo info) {
		if(content.size() == 0){
			return Response.status(HttpStatus.BAD_REQUEST.value()).entity("At least one custom query should be passed").build();
		}
		return processGremlinQuery(content.values().toArray(new CustomQueryConfigDTO[0])[0], info, headers);
	}
	
	protected Response processGremlinQuery(CustomQueryConfigDTO content, UriInfo info,
			HttpHeaders headers) {
		try{
			LinkedHashMap <String, Object> params;
			CustomQueryDTO queryDTO = content.getQueryDTO();
			String query = queryDTO.getQuery();
			params = new LinkedHashMap <>();
			
			List<String> optionalParameters = queryDTO.getQueryOptionalProperties();
			if (!optionalParameters.isEmpty()){
				for ( String key : optionalParameters ) {
					params.put(key, key);
				}
			}
			
			List<String> requiredParameters = queryDTO.getQueryRequiredProperties();
			if (!requiredParameters.isEmpty()){
				for ( String key : requiredParameters ) {
					params.put(key, key);
				}
			}
			
			SchemaVersions schemaVersions = SpringContextAware.getBean(SchemaVersions.class);
			traversalUriHttpEntry.setHttpEntryProperties(schemaVersions.getDefaultVersion());
			traversalUriHttpEntry.setPaginationParameters("-1", "-1");
			
			TransactionalGraphEngine dbEngine = traversalUriHttpEntry.getDbEngine();
			
			query = new GroovyQueryBuilder().executeTraversal(dbEngine,query, params);
			query = "g" + query;
			return Response.ok(query).build();
		}
		catch(Exception ex){
			return Response.status(500).entity("Query conversion failed with following reason: " + ex.toString()).build();
		}
		
	}
}
