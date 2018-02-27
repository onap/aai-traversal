/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.onap.aai.dbmap.DBConnectionType;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.introspection.Version;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.parsers.query.QueryParser;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.search.CustomQueryConfig;
import org.onap.aai.rest.search.GenericQueryProcessor;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.rest.search.QueryProcessorType;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.restcore.util.URITools;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;
import org.onap.aai.serialization.queryformats.FormatFactory;
import org.onap.aai.serialization.queryformats.Formatter;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.aai.logging.LoggingContext;
import org.onap.aai.logging.LoggingContext.StatusCode;
import org.onap.aai.logging.StopWatch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.onap.aai.util.AAIConstants;

@Path("{version: v9|v1[0123]}/query")
public class QueryConsumer extends RESTAPI {

    private static final String DEPTH = "depth";
	
	/** The introspector factory type. */
	private ModelType introspectorFactoryType = ModelType.MOXY;
	
	private QueryProcessorType processorType = QueryProcessorType.LOCAL_GROOVY;
	/** The query style. */
	private QueryStyle queryStyle = QueryStyle.TRAVERSAL;
	
	private static final String TARGET_ENTITY = "DB";
	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(QueryConsumer.class);
	
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response executeQuery(String content, @PathParam("version")String versionParam, @PathParam("uri") @Encoded String uri, @DefaultValue("graphson") @QueryParam("format") String queryFormat,@DefaultValue("no_op") @QueryParam("subgraph") String subgraph, @Context HttpHeaders headers, @Context UriInfo info, @Context HttpServletRequest req){
		return runner(AAIConstants.AAI_TRAVERSAL_TIMEOUT_ENABLED,
				AAIConstants.AAI_TRAVERSAL_TIMEOUT_APP,
				AAIConstants.AAI_TRAVERSAL_TIMEOUT_LIMIT,
				headers,
				info,
				HttpMethod.GET,
				new Callable<Response>() {
					@Override
					public Response call() {
						return processExecuteQuery(content, versionParam, uri, queryFormat, subgraph, headers, info, req);
					}
				}
		);
	}

	public Response processExecuteQuery(String content, @PathParam("version")String versionParam, @PathParam("uri") @Encoded String uri, @DefaultValue("graphson") @QueryParam("format") String queryFormat,@DefaultValue("no_op") @QueryParam("subgraph") String subgraph, @Context HttpHeaders headers, @Context UriInfo info, @Context HttpServletRequest req) {

		String methodName = "executeQuery";
		String sourceOfTruth = headers.getRequestHeaders().getFirst("X-FromAppId");
		String realTime = headers.getRequestHeaders().getFirst("Real-Time");
		String queryProcessor = headers.getRequestHeaders().getFirst("QueryProcessor");
		QueryProcessorType processorType = this.processorType;
		Response response = null;
		TransactionalGraphEngine dbEngine = null;
		try {
			LoggingContext.save();
			this.checkQueryParams(info.getQueryParameters());
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
			JsonElement dslElement = input.get("dsl");
			List<URI> startURIs = new ArrayList<>();
			String queryURI = "";
			String gremlin = "";
			String dsl = "";
			
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
			if (dslElement != null) {
				dsl = dslElement.getAsString();
			}
			URI queryURIObj = new URI(queryURI);
			
			CustomQueryConfig customQueryConfig = getCustomQueryConfig(queryURIObj);
			if ( customQueryConfig != null ) {
				List<String> missingRequiredQueryParameters =  checkForMissingQueryParameters( customQueryConfig.getQueryRequiredProperties(), URITools.getQueryMap(queryURIObj));
				if ( !missingRequiredQueryParameters.isEmpty() ) {
					return( createMessageMissingQueryRequiredParameters( missingRequiredQueryParameters, headers, info, req));
				}
			} else if ( queryElement != null ) {
				return( createMessageInvalidQuerySection( queryURI, headers, info, req));
			}
			

			GenericQueryProcessor processor = null;
			
			LoggingContext.targetEntity(TARGET_ENTITY);
			LoggingContext.targetServiceName(methodName);
			LoggingContext.startTime();
			StopWatch.conditionalStart();
			
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
			} else if(!dsl.equals("")){
				processor =  new GenericQueryProcessor.Builder(dbEngine)
						.queryFrom(dsl, "dsl")
						.processWith(processorType).create();
			}else {
				processor =  new GenericQueryProcessor.Builder(dbEngine)
						.queryFrom(gremlin, "gremlin")
						.processWith(processorType).create();
			}
			String result = "";
			List<Object> vertices = processor.execute(subGraphStyle);
		
			DBSerializer serializer = new DBSerializer(version, dbEngine, introspectorFactoryType, sourceOfTruth);
			FormatFactory ff = new FormatFactory(httpEntry.getLoader(), serializer);
			
			Formatter formater =  ff.get(format, info.getQueryParameters());
		
			result = formater.output(vertices).toString();

			double msecs = StopWatch.stopIfStarted();
			LoggingContext.elapsedTime((long)msecs,TimeUnit.MILLISECONDS);
			LoggingContext.successStatusFields();
			LOGGER.info ("Completed");
			
			
			response = Response.status(Status.OK)
					.type(MediaType.APPLICATION_JSON)
					.entity(result).build();
		
		} catch (AAIException e) {
			response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET, e);
		} catch (Exception e ) {
			AAIException ex = new AAIException("AAI_4000", e);
			response = consumerExceptionResponseGenerator(headers, info, HttpMethod.GET, ex);
		} finally {
			LoggingContext.restoreIfPossible();
			LoggingContext.successStatusFields();
			if (dbEngine != null) {
				dbEngine.rollback();
			}
			
		}
		
		return response;
	}
	
	public void checkQueryParams(MultivaluedMap<String, String> params) throws AAIException {
		
		if (params.containsKey(DEPTH) && params.getFirst(DEPTH).matches("\\d+")) {
			String depth = params.getFirst(DEPTH);
			Integer i = Integer.parseInt(depth);
			if (i > 1) {
				throw new AAIException("AAI_3303");
			}
		}
		
		
	}
	
	private List<String> checkForMissingQueryParameters( List<String> requiredParameters, MultivaluedMap<String, String> queryParams ) {
		List<String> result = new ArrayList<>();

		for ( String param : requiredParameters ) {
			if ( !queryParams.containsKey(param)) {
				result.add(param);
			}
		}
		return result;
	}
	
	private CustomQueryConfig getCustomQueryConfig(URI uriObj ) {
		
		GremlinServerSingleton gremlinServerSingleton;
		CustomQueryConfig customQueryConfig;
		String path = uriObj.getPath();

		String[] parts = path.split("/");
		boolean hasQuery = false;
		for ( String part:parts ) {
			if  ( hasQuery) {
				gremlinServerSingleton = GremlinServerSingleton.getInstance();
				return gremlinServerSingleton.getCustomQueryConfig(part);
			}
			if ( "query".equals(part)) {
				hasQuery = true;
			}
		}
		
		return null;
		
	}
	
	private Response createMessageMissingQueryRequiredParameters(List<String> missingRequiredQueryParams, HttpHeaders headers, UriInfo info, HttpServletRequest req) {
		AAIException e = new AAIException("AAI_3013");
		
		ArrayList<String> templateVars = new ArrayList<>();

		if (templateVars.isEmpty()) {
			templateVars.add(missingRequiredQueryParams.toString());
		}

		Response response = Response
				.status(e.getErrorObject().getHTTPResponseCode())
				.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, 
						templateVars)).build();	

		return response;
	} 
	
	private Response createMessageInvalidQuerySection(String invalidQuery, HttpHeaders headers, UriInfo info, HttpServletRequest req) {
		AAIException e = new AAIException("AAI_3014");
		
		ArrayList<String> templateVars = new ArrayList<>();

		if (templateVars.isEmpty()) {
			templateVars.add(invalidQuery);
		}

		Response response = Response
				.status(e.getErrorObject().getHTTPResponseCode())
				.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, 
						templateVars)).build();	

		return response;
	} 


}
