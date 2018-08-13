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
package org.onap.aai.config;

import org.onap.aai.dbgraphmap.SearchGraph;

import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.rest.dsl.DslListener;
import org.onap.aai.rest.dsl.DslQueryProcessor;
import org.onap.aai.rest.search.GremlinServerSingleton;
import org.onap.aai.setup.SchemaVersions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SearchConfiguration {

	@Bean
	public SearchGraph searchGraph(LoaderFactory loaderFactory, EdgeIngestor edgeIngestor, SchemaVersions schemaVersions) {
		SearchGraph searchGraph = new SearchGraph(loaderFactory, edgeIngestor, schemaVersions);
		return searchGraph;
	}

	@Bean
	public GremlinServerSingleton gremlinServerSingleton(){
		return new GremlinServerSingleton();
	}

}
