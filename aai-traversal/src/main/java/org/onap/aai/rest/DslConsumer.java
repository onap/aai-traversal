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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.onap.aai.concurrent.AaiCallable;
import org.onap.aai.dbmap.DBConnectionType;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.logging.LoggingContext;
import org.onap.aai.logging.StopWatch;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.dsl.DslQueryProcessor;
import org.onap.aai.rest.search.GenericQueryProcessor;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.rest.search.QueryProcessorType;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;
import org.onap.aai.serialization.queryformats.FormatFactory;
import org.onap.aai.serialization.queryformats.Formatter;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.onap.aai.util.AAIConfig;
import org.onap.aai.util.TraversalConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Path("{version: v[1-9][0-9]*|latest}/dsl")
public class DslConsumer extends RESTAPI {

	private HttpEntry traversalUriHttpEntry;

	private QueryProcessorType processorType = QueryProcessorType.LOCAL_GROOVY;

	private static final String TARGET_ENTITY = "DB";
	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(DslConsumer.class);

	private DslQueryProcessor dslQueryProcessor;

	private SchemaVersions schemaVersions;

	private String basePath;

	private GremlinServerSingleton gremlinServerSingleton;

	@Autowired
	public DslConsumer(HttpEntry traversalUriHttpEntry, DslQueryProcessor dslQueryProcessor,
                       SchemaVersions schemaVersions, GremlinServerSingleton gremlinServerSingleton,
                       @Value("${schema.uri.base.path}") String basePath) {
		this.traversalUriHttpEntry = traversalUriHttpEntry;
		this.dslQueryProcessor = dslQueryProcessor;
		this.schemaVersions = schemaVersions;
		this.gremlinServerSingleton = gremlinServerSingleton;
		this.basePath = basePath;
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response executeQuery(String content, @PathParam("version") String versionParam,
			@PathParam("uri") @Encoded String uri, @DefaultValue("graphson") @QueryParam("format") String queryFormat,
			@DefaultValue("no_op") @QueryParam("subgraph") String subgraph, @Context HttpHeaders headers,
			@Context UriInfo info, @Context HttpServletRequest req, @DefaultValue("-1") @QueryParam("resultIndex") String resultIndex, @DefaultValue("-1") @QueryParam("resultSize") String resultSize) {
		return runner(TraversalConstants.AAI_TRAVERSAL_DSL_TIMEOUT_ENABLED,
				TraversalConstants.AAI_TRAVERSAL_DSL_TIMEOUT_APP, TraversalConstants.AAI_TRAVERSAL_DSL_TIMEOUT_LIMIT,
				headers, info, HttpMethod.PUT, new AaiCallable<Response>() {
					@Override
					public Response process() {
						return processExecuteQuery(content, versionParam, uri, queryFormat, subgraph, headers, info,
								req, resultIndex, resultSize);
					}
				});
	}

	public Response processExecuteQuery(String content, @PathParam("version") String versionParam,
			@PathParam("uri") @Encoded String uri, @DefaultValue("graphson") @QueryParam("format") String queryFormat,
			@DefaultValue("no_op") @QueryParam("subgraph") String subgraph, @Context HttpHeaders headers,
			@Context UriInfo info, @Context HttpServletRequest req, @DefaultValue("-1") @QueryParam("resultIndex") String resultIndex, @DefaultValue("-1") @QueryParam("resultSize") String resultSize) {

		String methodName = "executeDslQuery";
		String sourceOfTruth = headers.getRequestHeaders().getFirst("X-FromAppId");
		String dslOverride = headers.getRequestHeaders().getFirst("X-DslOverride");
		String realTime = headers.getRequestHeaders().getFirst("Real-Time");
		Response response;
		SchemaVersion version = new SchemaVersion(versionParam);

		TransactionalGraphEngine dbEngine = null;
		try {
			LoggingContext.save();
			DBConnectionType type = this.determineConnectionType(sourceOfTruth, realTime);
			traversalUriHttpEntry.setHttpEntryProperties(version, type);
			traversalUriHttpEntry.setPaginationParameters(resultIndex, resultSize);
			dbEngine = traversalUriHttpEntry.getDbEngine();
			JsonObject input = new JsonParser().parse(content).getAsJsonObject();
			JsonElement dslElement = input.get("dsl");
			String dsl = "";
			if (dslElement != null) {
				dsl = dslElement.getAsString();
			}

			LoggingContext.targetEntity(TARGET_ENTITY);
			LoggingContext.targetServiceName(methodName);
			LoggingContext.startTime();
			StopWatch.conditionalStart();

			boolean isDslOverride = dslOverride != null && !AAIConfig.get(TraversalConstants.DSL_OVERRIDE).equals("false")
					&& dslOverride.equals(AAIConfig.get(TraversalConstants.DSL_OVERRIDE));
			
			if(isDslOverride)
				dslQueryProcessor.setValidationFlag(false);
			
			GenericQueryProcessor processor = new GenericQueryProcessor.Builder(dbEngine, gremlinServerSingleton)
					.queryFrom(dsl, "dsl").queryProcessor(dslQueryProcessor).processWith(processorType).create();
			
			String result = "";
			SubGraphStyle subGraphStyle = SubGraphStyle.valueOf(subgraph);
			List<Object> vertTemp = processor.execute(subGraphStyle);
			List<Object> vertices = traversalUriHttpEntry.getPaginatedVertexList(vertTemp);
			DBSerializer serializer = new DBSerializer(version, dbEngine, ModelType.MOXY, sourceOfTruth);
			Format format = Format.getFormat(queryFormat);
			FormatFactory ff = new FormatFactory(traversalUriHttpEntry.getLoader(), serializer, schemaVersions,
					this.basePath);
			
			Formatter formater = ff.get(format, info.getQueryParameters());

			result = formater.output(vertices).toString();
			
			double msecs = StopWatch.stopIfStarted();
			LoggingContext.elapsedTime((long) msecs, TimeUnit.MILLISECONDS);
			LoggingContext.successStatusFields();
			LOGGER.info("Completed");
			
			if(traversalUriHttpEntry.isPaginated()){
				response = Response.status(Status.OK)
						.type(MediaType.APPLICATION_JSON)
						.header("total-results", traversalUriHttpEntry.getTotalVertices())
						.header("total-pages", traversalUriHttpEntry.getTotalPaginationBuckets())
						.entity(result)
						.build();
			}else {
				response = Response.status(Status.OK)
						.type(MediaType.APPLICATION_JSON)
						.entity(result).build();
			}
			
		} catch (AAIException e) {
			response = consumerExceptionResponseGenerator(headers, info, HttpMethod.PUT, e);
		} catch (Exception e) {
			AAIException ex = new AAIException("AAI_4000", e);
			response = consumerExceptionResponseGenerator(headers, info, HttpMethod.PUT, ex);
		} finally {
			LoggingContext.restoreIfPossible();
			LoggingContext.successStatusFields();
			if (dbEngine != null) {
				dbEngine.rollback();
			}

		}

		return response;
	}
}
