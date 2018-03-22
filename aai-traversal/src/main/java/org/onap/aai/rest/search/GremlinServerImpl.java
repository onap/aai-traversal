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
package org.onap.aai.rest.search;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import org.onap.aai.util.AAIConfig;

public class GremlinServerImpl extends GenericQueryProcessor {

	
	protected GremlinServerImpl(Builder builder) {
		super(builder);
	}
	
	
	@Override
	protected GraphTraversal<?,?> runQuery(String query, Map<String, Object> params) {

		//must force them into ids because of serialization issue with 
		//tinkerpop-3.0.1-incubating
		query += ".id()";
        String rebindGraph = AAIConfig.get("aai.server.rebind", "g");

        if(!"g".equals(rebindGraph)){
        	query = query.replaceFirst("g\\.V\\(", rebindGraph + ".V(");
		}
        
		Cluster cluster = gremlinServerSingleton.getCluster();
		Client client = cluster.connect();

		ResultSet results = client.submit(query, params);
		

		List<Object> vIds = new Vector<>();
		results.stream().forEach(x -> {
			Object obj = x.getObject();
			vIds.add(obj);
		});
		
		client.close();
		
		if (vIds.isEmpty()) {
			return __.start();
		} else {
			return this.dbEngine.asAdmin().getTraversalSource().V(vIds.toArray());
		}
	}
	
}
