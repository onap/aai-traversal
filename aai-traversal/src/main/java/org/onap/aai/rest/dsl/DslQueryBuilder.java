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
package org.onap.aai.rest.dsl;

import java.util.List;

import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.EdgeRuleQuery;
import org.onap.aai.edges.enums.EdgeType;
import org.onap.aai.exceptions.AAIException;

import com.google.common.base.Joiner;

public class DslQueryBuilder {

	private StringBuilder query;
	private final EdgeIngestor edgeRules;

	public DslQueryBuilder(EdgeIngestor edgeIngestor) {
		this.edgeRules = edgeIngestor;
		query = new StringBuilder();
	}

	public StringBuilder getQuery() {
		return query;
	}

	public void setQuery(StringBuilder query) {
		this.query = query;
	}

	public DslQueryBuilder start() {
		query.append("builder");
		return this;
	}

	public DslQueryBuilder identity() {
		query.append("builder.newInstance()");
		return this;
	}

	public DslQueryBuilder end() {
		
		query.append(".cap('x').unfold().dedup()");
		
		return this;
	}

	public DslQueryBuilder nodeQuery(String nodeName) {
		query.append(".getVerticesByProperty('aai-node-type', '").append(nodeName).append("')");
		return this;
	}

	public DslQueryBuilder edgeQuery(String previousNode, String currentNode) throws AAIException {
		EdgeRuleQuery.Builder baseQ = new EdgeRuleQuery.Builder(previousNode, currentNode);
		String edgeType = "";
		if (!edgeRules.hasRule(baseQ.build())) {
			throw new AAIException("AAI_6120", "No EdgeRule found for passed nodeTypes: " + previousNode
					+ ", " + currentNode);
		} else if (edgeRules.hasRule(baseQ.edgeType(EdgeType.TREE).build())) {
			edgeType = "EdgeType.TREE";
		} else if (edgeRules.hasRule(baseQ.edgeType(EdgeType.COUSIN).build())) {
			edgeType = "EdgeType.COUSIN";
		} else
			edgeType = "EdgeType.COUSIN";

		query.append(".createEdgeTraversal(").append(edgeType).append(", '").append(previousNode)
				.append("','").append(currentNode).append("')");

		return this;
	}

	public DslQueryBuilder where() {
		query.append(".where(builder.newInstance()");
		return this;
	}

	public DslQueryBuilder endWhere() {
		query.append(")");
		return this;
	}

	public DslQueryBuilder endUnion() {
		/*
		 * Need to delete the last comma
		 */
		if (query.toString().endsWith(",")) {
			query.deleteCharAt(query.length() - 1);
		}
		query.append(")");
		return this;
	}

	public DslQueryBuilder limit(String number) {

		query.append(new StringBuilder(".limit(").append(number).append(")"));
		return this;
	}

	public DslQueryBuilder filterPropertyStart(boolean isNegated) {
		if (isNegated)
			query.append(".getVerticesExcludeByProperty(");
		else
			query.append(".getVerticesByProperty(");

		return this;

	}

	public DslQueryBuilder filterPropertyEnd() {
		query.append(")");
		return this;

	}

	public DslQueryBuilder filterPropertyKeys(String key, List<String> valuesList) {

		/*
		 * The whole point of doing this to separate P.within from key-value
		 * search For a list of values QB uses P.within For just a single value
		 * QB uses key,value check
		 */
		query.append(key);
		if (valuesList.size() > 1) {
			String values = Joiner.on(",").join(valuesList);
			query.append(",").append(" new ArrayList<>(Arrays.asList(" + values + "))");
		} else {
			if (!valuesList.isEmpty())
				query.append(",").append(valuesList.get(0).toString());
		}
		return this;
	}

	public DslQueryBuilder union() {
		query.append(".union(");
		return this;
	}

	public DslQueryBuilder store() {
		query.append(".store('x')");
		return this;

	}

	public DslQueryBuilder comma() {
		query.append(",");
		return this;

	}
}
