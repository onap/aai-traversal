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

package org.openecomp.aai.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import org.openecomp.aai.dbmap.DBConnectionType;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.introspection.ModelType;
import org.openecomp.aai.introspection.Version;
import org.openecomp.aai.parsers.query.QueryParser;
import org.openecomp.aai.rest.db.HttpEntry;
import org.openecomp.aai.rest.search.GenericQueryProcessor;
import org.openecomp.aai.rest.search.QueryProcessorType;
import org.openecomp.aai.restcore.HttpMethod;
import org.openecomp.aai.restcore.RESTAPI;
import org.openecomp.aai.restcore.util.URITools;
import org.openecomp.aai.serialization.db.DBSerializer;
import org.openecomp.aai.serialization.engines.QueryStyle;
import org.openecomp.aai.serialization.engines.TransactionalGraphEngine;
import org.openecomp.aai.serialization.queryformats.Format;
import org.openecomp.aai.serialization.queryformats.FormatFactory;
import org.openecomp.aai.serialization.queryformats.Formatter;
import org.openecomp.aai.serialization.queryformats.SubGraphStyle;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Path("{version: v9|v1[01]}/query")
public class QueryConsumer extends RESTAPI {
	
	/** The introspector factory type. */
	private ModelType introspectorFactoryType = ModelType.MOXY;
	
	private QueryProcessorType processorType = QueryProcessorType.GREMLIN_SERVER;
	/** The query style. */
	private QueryStyle queryStyle = QueryStyle.TRAVERSAL;
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response executeQuery(String content, @PathParam("version")String versionParam, @PathParam("uri") @Encoded String uri, @DefaultValue("graphson") @QueryParam("format") String queryFormat,@DefaultValue("no_op") @QueryParam("subgraph") String subgraph, @Context HttpHeaders headers, @Context UriInfo info, @Context HttpServletRequest req) {
		
		String sourceOfTruth = headers.getRequestHeaders().getFirst("X-FromAppId");
		String realTime = headers.getRequestHeaders().getFirst("Real-Time");
		String queryProcessor = headers.getRequestHeaders().getFirst("QueryProcessor");
		QueryProcessorType processorType = this.processorType;
		Response response = null;
		TransactionalGraphEngine dbEngine = null;
		try {

			Format format = Format.valueOf(queryFormat);
			if (queryProcessor != null) {
				processorType = QueryProcessorType.valueOf(queryProcessor);
			}
			SubGraphStyle subGraphStyle = SubGraphStyle.valueOf(subgraph);
			JsonParser parser = new JsonParser();
			
			JsonObject input = parser.parse(content).getAsJsonObject();
			
			JsonElement startElement = input.get("start");
			JsonElement queryElement = input.get("query");
			JsonElement gremlinElement = input.get("gremlin");
			List<URI> startURIs = new ArrayList<>();
			String queryURI = "";
			String gremlin = "";
			
			Version version = Version.valueOf(versionParam);
			DBConnectionType type = this.determineConnectionType(sourceOfTruth, realTime);
			HttpEntry httpEntry = new HttpEntry(version, introspectorFactoryType, queryStyle, type);
			dbEngine = httpEntry.getDbEngine();

			if (startElement != null) {
	
				if (startElement.isJsonArray()) {
					for (JsonElement element : startElement.getAsJsonArray()) {
						startURIs.add(new URI(element.getAsString()));
					}
				} else {
					startURIs.add(new URI(startElement.getAsString()));
				}
			}
			
			if (queryElement != null) {
				queryURI = queryElement.getAsString();
			}
			if (gremlinElement != null) {
				gremlin = gremlinElement.getAsString();
			}
			URI queryURIObj = new URI(queryURI);
			GenericQueryProcessor processor = null;
			
			if (!startURIs.isEmpty()) {
				Set<Vertex> vertexSet = new LinkedHashSet<>();
				QueryParser uriQuery;
				List<Vertex> vertices;
				for (URI startUri : startURIs) {
					uriQuery = dbEngine.getQueryBuilder().createQueryFromURI(startUri, URITools.getQueryMap(startUri));
					vertices = uriQuery.getQueryBuilder().toList();
					vertexSet.addAll(vertices);
				}

				processor = new GenericQueryProcessor.Builder(dbEngine)
						.startFrom(vertexSet).queryFrom(queryURIObj)
						.processWith(processorType).create();
			} else if (!queryURI.equals("")){
				processor =  new GenericQueryProcessor.Builder(dbEngine)
						.queryFrom(queryURIObj)
						.processWith(processorType).create();
			} else {
				processor =  new GenericQueryProcessor.Builder(dbEngine)
						.queryFrom(gremlin)
						.processWith(processorType).create();
			}
			String result = "";
			List<Object> vertices = processor.execute(subGraphStyle);
			DBSerializer serializer = new DBSerializer(version, dbEngine, introspectorFactoryType, sourceOfTruth);
			FormatFactory ff = new FormatFactory(httpEntry.getLoader(), serializer);
			
			Formatter formater =  ff.get(format);
		
			result = formater.output(vertices).toString();

			response = Response.status(Status.OK)
					.type(MediaType.APPLICATION_JSON)
					.entity(result).build();
		
		} catch (AAIException e) {
			response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET, e);
		} catch (Exception e ) {
			AAIException ex = new AAIException("AAI_4000", e);
			
			response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET, ex);
		} finally {
			if (dbEngine != null) {
				dbEngine.rollback();
			}
		}
		
		return response;
	}

}
