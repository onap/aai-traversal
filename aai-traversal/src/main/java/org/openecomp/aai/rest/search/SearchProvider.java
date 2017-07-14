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

package org.openecomp.aai.rest.search;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.openecomp.aai.db.props.AAIProperties;
import org.openecomp.aai.dbgraphmap.SearchGraph;
import org.openecomp.aai.dbmap.DBConnectionType;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.LoaderFactory;
import org.openecomp.aai.introspection.ModelType;
import org.openecomp.aai.introspection.Version;
import org.openecomp.aai.logging.ErrorLogHelper;
import org.openecomp.aai.restcore.RESTAPI;
import org.openecomp.aai.serialization.db.DBSerializer;
import org.openecomp.aai.serialization.engines.QueryStyle;
import org.openecomp.aai.serialization.engines.TitanDBEngine;
import org.openecomp.aai.serialization.engines.TransactionalGraphEngine;
import org.openecomp.aai.serialization.queryformats.utils.UrlBuilder;

/**
 * Implements the search subdomain in the REST API. All API calls must include
 * X-FromAppId and X-TransactionId in the header.
 * 
 
 *
 */

@Path("/{version: v2|v[789]|v1[01]|latest}/search")
public class SearchProvider extends RESTAPI {
	
	protected static String authPolicyFunctionName = "search";

	public static final String GENERIC_QUERY = "/generic-query";

	public static final String NODES_QUERY = "/nodes-query";

	/**
	 * Gets the generic query response.
	 *
	 * @param headers the headers
	 * @param req the req
	 * @param startNodeType the start node type
	 * @param startNodeKeyParams the start node key params
	 * @param includeNodeTypes the include node types
	 * @param depth the depth
	 * @return the generic query response
	 */
	/* ---------------- Start Generic Query --------------------- */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(GENERIC_QUERY)
	public Response getGenericQueryResponse(@Context HttpHeaders headers,
											@Context HttpServletRequest req,
											@QueryParam("start-node-type") final String startNodeType,
											@QueryParam("key") final List<String> startNodeKeyParams,
											@QueryParam("include") final List<String> includeNodeTypes,
											@QueryParam("depth") final int depth,
											@PathParam("version")String versionParam
											) {
		
		AAIException ex = null;
		Response searchResult = null;
		String fromAppId = null;
		String transId = null;
		String rqstTm = genDate();
		ArrayList<String> templateVars = new ArrayList<String>();
		try { 
			fromAppId = getFromAppId(headers);
			transId = getTransId(headers);
			String realTime = headers.getRequestHeaders().getFirst("Real-Time");
			//only consider header value for search		
			DBConnectionType type = this.determineConnectionType("force-cache", realTime);
			final Version version;
			if (versionParam.equals("latest")) {
				version = AAIProperties.LATEST;
			} else {
				version = Version.valueOf(versionParam);
			}
			final ModelType factoryType = ModelType.MOXY;
			Loader loader = LoaderFactory.createLoaderForVersion(factoryType, version);
			TransactionalGraphEngine dbEngine = new TitanDBEngine(
					QueryStyle.TRAVERSAL,
					type,
					loader);
			DBSerializer dbSerializer = new DBSerializer(version, dbEngine, factoryType, fromAppId);
			UrlBuilder urlBuilder = new UrlBuilder(version, dbSerializer);
			SearchGraph searchGraph = new SearchGraph();
			searchResult = searchGraph.runGenericQuery(
													   headers,
													   startNodeType,
													   startNodeKeyParams,
													   includeNodeTypes, 
													   depth,
													   dbEngine,
													   loader,
													   urlBuilder
													   
													   );
	
			String respTm = genDate();
		
		} catch (AAIException e) { 
			// send error response
			ex = e;
			templateVars.add("GET Search");
			templateVars.add("getGenericQueryResponse");
			searchResult =  Response
							.status(e.getErrorObject().getHTTPResponseCode())
							.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
							.build();
		} catch (Exception e) {
			// send error response
			ex = new AAIException("AAI_4000", e);
			templateVars.add("GET Search");
			templateVars.add("getGenericQueryResponse");
			searchResult = Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), ex, templateVars))
							.build();
		} finally {
			// log success or failure
			if (ex != null){
				ErrorLogHelper.logException(ex);
			}
		}

		return searchResult;
	}

	/* ---------------- End Generic Query --------------------- */

	/**
	 * Gets the nodes query response.
	 *
	 * @param headers the headers
	 * @param req the req
	 * @param searchNodeType the search node type
	 * @param edgeFilterList the edge filter list
	 * @param filterList the filter list
	 * @return the nodes query response
	 */
	/* ---------------- Start Nodes Query --------------------- */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(NODES_QUERY)
	public Response getNodesQueryResponse(@Context HttpHeaders headers,
											@Context HttpServletRequest req,
											@QueryParam("search-node-type") final String searchNodeType,
											@QueryParam("edge-filter") final List<String> edgeFilterList, 
											@QueryParam("filter") final List<String> filterList,
											@PathParam("version")String versionParam) {
		AAIException ex = null;
		Response searchResult = null;
		String fromAppId = null;
		String transId = null;
		String rqstTm = genDate();
		ArrayList<String> templateVars = new ArrayList<String>();	
		try { 
			fromAppId = getFromAppId(headers);
			transId = getTransId(headers);
			String realTime = headers.getRequestHeaders().getFirst("Real-Time");
			//only consider header value for search		
			DBConnectionType type = this.determineConnectionType("force-cache", realTime);
			
			final Version version;
			if (versionParam.equals("latest")) {
				version = AAIProperties.LATEST;
			} else {
				version = Version.valueOf(versionParam);
			}
			final ModelType factoryType = ModelType.MOXY;
			Loader loader = LoaderFactory.createLoaderForVersion(factoryType, version);
			TransactionalGraphEngine dbEngine = new TitanDBEngine(
					QueryStyle.TRAVERSAL,
					type,
					loader);
			DBSerializer dbSerializer = new DBSerializer(version, dbEngine, factoryType, fromAppId);
			UrlBuilder urlBuilder = new UrlBuilder(version, dbSerializer);
			SearchGraph searchGraph = new SearchGraph();
			
			searchResult = searchGraph.runNodesQuery(headers,
													searchNodeType,
													edgeFilterList, 
													filterList,
													dbEngine,
													loader,
													urlBuilder);
	
			String respTm = genDate();
		} catch (AAIException e) { 
			// send error response
			ex = e;
			templateVars.add("GET Search");
			templateVars.add("getNodesQueryResponse");
			searchResult =  Response
							.status(e.getErrorObject().getHTTPResponseCode())
							.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), e, templateVars))
							.build();
		} catch (Exception e) {
			// send error response
			ex = new AAIException("AAI_4000", e);
			templateVars.add("GET Search");
			templateVars.add("getNodesQueryResponse");
			searchResult = Response
							.status(Status.INTERNAL_SERVER_ERROR)
							.entity(ErrorLogHelper.getRESTAPIErrorResponse(headers.getAcceptableMediaTypes(), ex, templateVars))
							.build();
		} finally {
			// log success or failure
			if (ex != null){
				ErrorLogHelper.logException(ex);
			}
		}
		return searchResult;
	}


	
}
