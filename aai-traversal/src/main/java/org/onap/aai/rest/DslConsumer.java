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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.onap.aai.concurrent.AaiCallable;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.rest.dsl.DslQueryProcessor;
import org.onap.aai.rest.enums.QueryVersion;
import org.onap.aai.rest.search.GenericQueryProcessor;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.rest.search.QueryProcessorType;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;
import org.onap.aai.serialization.queryformats.FormatFactory;
import org.onap.aai.serialization.queryformats.Formatter;
import org.onap.aai.serialization.queryformats.SubGraphStyle;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.onap.aai.transforms.XmlFormatTransformer;
import org.onap.aai.util.AAIConfig;
import org.onap.aai.util.TraversalConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("{version: v[1-9][0-9]*|latest}/dsl")
public class DslConsumer extends TraversalConsumer {

	private HttpEntry traversalUriHttpEntry;

	private QueryProcessorType processorType = QueryProcessorType.LOCAL_GROOVY;

	private static final Logger LOGGER = LoggerFactory.getLogger(DslConsumer.class);

	private DslQueryProcessor dslQueryProcessor;

	private SchemaVersions schemaVersions;

	private String basePath;

	private GremlinServerSingleton gremlinServerSingleton;
	private final QueryVersion DEFAULT_VERSION = QueryVersion.V1;
	private QueryVersion dslApiVersion = DEFAULT_VERSION;

	private XmlFormatTransformer xmlFormatTransformer;

	@Autowired
	public DslConsumer(HttpEntry traversalUriHttpEntry, DslQueryProcessor dslQueryProcessor,
					   SchemaVersions schemaVersions, GremlinServerSingleton gremlinServerSingleton,
					   XmlFormatTransformer xmlFormatTransformer,
					   @Value("${schema.uri.base.path}") String basePath) {
		this.traversalUriHttpEntry = traversalUriHttpEntry;
		this.dslQueryProcessor = dslQueryProcessor;
		this.schemaVersions = schemaVersions;
		this.gremlinServerSingleton = gremlinServerSingleton;
		this.xmlFormatTransformer = xmlFormatTransformer;
		this.basePath = basePath;
	}

	@PUT
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response executeQuery(String content,
								 @PathParam("version") String versionParam,
								 @DefaultValue("graphson") @QueryParam("format") String queryFormat,
								 @DefaultValue("no_op") @QueryParam("subgraph") String subgraph,
								 @DefaultValue("all") @QueryParam("validate") String validate,
								 @Context HttpHeaders headers,
								 @Context UriInfo info,
								 @DefaultValue("-1") @QueryParam("resultIndex") String resultIndex,
								 @DefaultValue("-1") @QueryParam("resultSize") String resultSize) {
		return runner(TraversalConstants.AAI_TRAVERSAL_DSL_TIMEOUT_ENABLED,
				TraversalConstants.AAI_TRAVERSAL_DSL_TIMEOUT_APP,
				TraversalConstants.AAI_TRAVERSAL_DSL_TIMEOUT_LIMIT,
				headers,
				info,
				HttpMethod.PUT,
				new AaiCallable() {
					@Override
					public Response process() throws Exception {
						return (processExecuteQuery(content, versionParam, queryFormat, subgraph, validate, headers, info,
								resultIndex, resultSize));
					}
				}
		);
	}

	public Response processExecuteQuery(String content, String versionParam, String queryFormat, String subgraph,
										String validate, HttpHeaders headers, UriInfo info, String resultIndex,
										String resultSize) {

		String sourceOfTruth = headers.getRequestHeaders().getFirst("X-FromAppId");
		String dslOverride = headers.getRequestHeaders().getFirst("X-DslOverride");

		Optional<String> dslApiVersionHeader = Optional.ofNullable(headers.getRequestHeaders().getFirst("X-DslApiVersion"));
		if (dslApiVersionHeader.isPresent()) {
			try {
				dslApiVersion = QueryVersion.valueOf(dslApiVersionHeader.get());
			} catch (IllegalArgumentException e) {
				LOGGER.debug("Defaulting DSL Api Version to  "+DEFAULT_VERSION);
			}
		}

		Response response;
		SchemaVersion version = new SchemaVersion(versionParam);

		TransactionalGraphEngine dbEngine = null;
		try {
			traversalUriHttpEntry.setHttpEntryProperties(version);
			traversalUriHttpEntry.setPaginationParameters(resultIndex, resultSize);
			dbEngine = traversalUriHttpEntry.getDbEngine();
			JsonObject input = new JsonParser().parse(content).getAsJsonObject();
			JsonElement dslElement = input.get("dsl");
			String dsl = "";
			if (dslElement != null) {
				dsl = dslElement.getAsString();
			}


			boolean isDslOverride = dslOverride != null && !AAIConfig.get(TraversalConstants.DSL_OVERRIDE).equals("false")
					&& dslOverride.equals(AAIConfig.get(TraversalConstants.DSL_OVERRIDE));
			
			if(isDslOverride) {
				dslQueryProcessor.setStartNodeValidationFlag(false);
			}

			dslQueryProcessor.setValidationRules(validate);

			Format format = Format.getFormat(queryFormat);

			if(isAggregate(format)){
				dslQueryProcessor.setAggregate(true);
			}

			if(isHistory(format)){
				validateHistoryParams(format, info.getQueryParameters());
			}

			GraphTraversalSource traversalSource = getTraversalSource(dbEngine, format, info);

			GenericQueryProcessor processor = new GenericQueryProcessor.Builder(dbEngine, gremlinServerSingleton)
					.queryFrom(dsl, "dsl").queryProcessor(dslQueryProcessor).version(dslApiVersion).processWith(processorType)
					.format(format).uriParams(info.getQueryParameters()).traversalSource(isHistory(format), traversalSource).create();

			SubGraphStyle subGraphStyle = SubGraphStyle.valueOf(subgraph);
			List<Object> vertTemp = processor.execute(subGraphStyle);

			List <Object> vertices;
			if (isAggregate(format)){
				vertices = traversalUriHttpEntry.getPaginatedVertexListForAggregateFormat(vertTemp);
			} else {
				vertices = traversalUriHttpEntry.getPaginatedVertexList(vertTemp);
			}

			DBSerializer serializer = new DBSerializer(version, dbEngine, ModelType.MOXY, sourceOfTruth);
			FormatFactory ff = new FormatFactory(traversalUriHttpEntry.getLoader(), serializer, schemaVersions,
					this.basePath);

			MultivaluedMap<String, String> mvm = new MultivaluedHashMap<>();
			mvm.putAll(info.getQueryParameters());
			if (isHistory(format)) {
				mvm.putSingle("startTs", Long.toString(getStartTime(format, mvm)));
				mvm.putSingle("endTs", Long.toString(getEndTime(mvm)));
			}
			Formatter formatter = ff.get(format, mvm);

			final Map<String, List<String>> propertiesMap = processor.getPropertiesMap();
			String result = "";
			if (propertiesMap != null && !propertiesMap.isEmpty()){
				result = formatter.output(vertices, propertiesMap).toString();
			}
			else {
				result = formatter.output(vertices).toString();
			}

			String acceptType = headers.getHeaderString("Accept");

			if(acceptType == null){
				acceptType = MediaType.APPLICATION_JSON;
			}

			if(MediaType.APPLICATION_XML_TYPE.isCompatible(MediaType.valueOf(acceptType))){
				result = xmlFormatTransformer.transform(result);
			}

			if(traversalUriHttpEntry.isPaginated()){
				response = Response.status(Status.OK)
						.type(acceptType)
						.header("total-results", traversalUriHttpEntry.getTotalVertices())
						.header("total-pages", traversalUriHttpEntry.getTotalPaginationBuckets())
						.entity(result)
						.build();
			}else {
				response = Response.status(Status.OK)
						.type(acceptType)
						.entity(result).build();
			}
			
		} catch (AAIException e) {
			response = consumerExceptionResponseGenerator(headers, info, HttpMethod.PUT, e);
		} catch (Exception e) {
			AAIException ex = new AAIException("AAI_4000", e);
			response = consumerExceptionResponseGenerator(headers, info, HttpMethod.PUT, ex);
		} finally {
			if (dbEngine != null) {
				dbEngine.rollback();
			}

		}

		return response;
	}
}
