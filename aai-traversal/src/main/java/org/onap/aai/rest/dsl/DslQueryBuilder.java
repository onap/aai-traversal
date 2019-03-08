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
import java.util.stream.Collectors;

import org.onap.aai.AAIDslParser;
import org.onap.aai.AAIDslParser.FilterStepContext;
import org.onap.aai.AAIDslParser.NodeContext;
import org.onap.aai.AAIDslParser.SingleNodeStepContext;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.EdgeRuleQuery;
import org.onap.aai.edges.enums.EdgeType;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;

import com.google.common.base.Joiner;

public class DslQueryBuilder {

	private StringBuilder query;
	private StringBuilder queryException;
	private final EdgeIngestor edgeRules;
	private final Loader loader;
	private boolean started = false;
	private boolean isEnded = false;
	private boolean validationFlag;

	public DslQueryBuilder(EdgeIngestor edgeIngestor, Loader loader) {
		this.edgeRules = edgeIngestor;
		this.loader = loader;
		query = new StringBuilder();
		queryException = new StringBuilder();
	}

	public boolean isEmpty() {
		return query.length() == 0;
	}
	public StringBuilder getQuery() {
		return query;
	}

	public void setQuery(StringBuilder query) {
		this.query = query;
	}

	public StringBuilder getQueryException() {
		return queryException;
	}

	public void setQueryException(StringBuilder queryException) {
		this.queryException = queryException;
	}

	public DslQueryBuilder start() {
		query.append("builder");
		return this;
	}

	public DslQueryBuilder identity() {
		query.append("builder.newInstance()");
		return this;
	}
	
	public DslQueryBuilder startUnion() {
		query.append("builder.newInstance()");
		return this;
	}

	public DslQueryBuilder end() {
		if (!isEnded) {
			query.append(".cap('x').unfold().dedup()");
			isEnded = true;
		}
		return this;
	}

	public DslQueryBuilder createNode(AAIDslParser.SingleNodeStepContext ctx) throws AAIException {
		
		NodeContext nodeContext = ctx.node();
		List<FilterStepContext> filterStepContexts = ctx.filterStep();
		
		if (!started) {
			if (validationFlag) {
				validateFilter(ctx);
			}
			this.nodeQuery(nodeContext);
			started = true;
		}
		

		for (FilterStepContext filter : filterStepContexts) {
			this.filter(filter);
		}
		return this;
	}

	public DslQueryBuilder nodeQuery(NodeContext context) {
		query.append(".getVerticesByProperty('aai-node-type', '").append(context.getText()).append("')");
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

	public DslQueryBuilder limit(AAIDslParser.LimitStepContext ctx) {
		/*
		 * limit queries are strange - You have to append in the end
		 */
		query.append(new StringBuilder(".limit(").append(ctx.numericValue().getText()).append(")"));
		return this;
	}

	public DslQueryBuilder filter(FilterStepContext context) {
		return this.filterPropertyStart(context).filterPropertyKeys(context).filterPropertyEnd();

	}

	public DslQueryBuilder filterPropertyStart(FilterStepContext ctx) {
		if (ctx.not() != null)
			query.append(".getVerticesExcludeByProperty(");
		else
			query.append(".getVerticesByProperty(");

		return this;

	}

	public DslQueryBuilder filterPropertyEnd() {
		query.append(")");
		return this;

	}

	public DslQueryBuilder validateFilter(AAIDslParser.SingleNodeStepContext ctx) throws AAIException {
		Introspector obj = loader.introspectorFromName(ctx.node().getText());
		if(ctx.filterStep().isEmpty()){
			queryException.append("No keys sent. Valid keys for " + ctx.node().getText() + " are "
					+ String.join(",", obj.getIndexedProperties()));
			return this;
		}
		boolean notIndexed = ctx.filterStep().stream()
				.filter((prop) -> obj.getIndexedProperties().contains(removeSingleQuotes(prop.key().getText()))).collect(Collectors.toList()).isEmpty();
		if (notIndexed)
			queryException.append("Non indexed keys sent. Valid keys for " + ctx.node().getText()+ " "
					+ String.join(",", obj.getIndexedProperties()));

		return this;
	}

	protected String removeSingleQuotes(String value) {
		return value.replaceFirst("^'(.*)'$", "$1");
	}
	public DslQueryBuilder filterPropertyKeys(FilterStepContext context) {
		final String key = context.key().getText();
		query.append(key);
		List<String> valueContext = context.value().stream().map(item -> item.getText()).collect(Collectors.toList());
		
		/*
		 * The whole point of doing this to separate P.within from key-value
		 * search For a list of values QB uses P.within For just a single value
		 * QB uses key,value check
		 */
		if (valueContext.size() > 1) {
			String values = Joiner.on(",").join(valueContext);
			query.append(",").append(" new ArrayList<>(Arrays.asList(" + values + "))");
		} else {
			if (!valueContext.isEmpty())
				query.append(",").append(valueContext.get(0).toString());
		}
		return this;
	}

	public DslQueryBuilder union() {
		query.append(".union(");
		return this;
	}

	public DslQueryBuilder store(SingleNodeStepContext context) {
		if (context.store() != null) {
			query.append(".store('x')");
		}
		return this;

	}

	public DslQueryBuilder comma() {
		query.append(",");
		return this;

	}
	
	public void setValidationFlag(boolean validationFlag) {
		this.validationFlag = validationFlag;
	}
}
