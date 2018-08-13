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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.onap.aai.AAIDslBaseListener;
import org.onap.aai.AAIDslParser;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.EdgeRuleQuery;
import org.onap.aai.edges.enums.EdgeType;
import org.onap.aai.exceptions.AAIException;

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

	public DslQueryBuilder startUnion() {
		query.append("builder.newInstance()");
		return this;
	}

	public DslQueryBuilder end(DslContext context) {
		query.append(".cap('x').unfold().dedup()").append(context.getLimitQuery());
		return this;
	}

	public DslQueryBuilder nodeQuery(DslContext context) {
		query.append(".getVerticesByProperty('aai-node-type', '").append(context.getCurrentNode()).append("')");
		return this;
	}

	public DslQueryBuilder edgeQuery(DslContext context) throws AAIException {
		EdgeRuleQuery.Builder baseQ = new EdgeRuleQuery.Builder(context.getPreviousNode(), context.getCurrentNode());
		String edgeType = "";
		if (!edgeRules.hasRule(baseQ.build())) {
			throw new AAIException("AAI_6120", "No EdgeRule found for passed nodeTypes: " + context.getPreviousNode()
			+ ", " + context.getCurrentNode());
		} else if (edgeRules.hasRule(baseQ.edgeType(EdgeType.TREE).build())) {
			edgeType = "EdgeType.TREE";
		} else if (edgeRules.hasRule(baseQ.edgeType(EdgeType.COUSIN).build())) {
			edgeType = "EdgeType.COUSIN";
		} else
			edgeType = "EdgeType.COUSIN";

		query.append(".createEdgeTraversal(").append(edgeType).append(", '").append(context.getPreviousNode())
		.append("','").append(context.getCurrentNode()).append("')");

		return this;
	}

	public DslQueryBuilder where(DslContext context) {
		query.append(".where(builder.newInstance()");
		return this;
	}

	public DslQueryBuilder endWhere(DslContext context) {
		query.append(")");
		return this;
	}

	public DslQueryBuilder endUnion(DslContext context) {
		/*
		 * Need to delete the last comma
		 */
		if (query.toString().endsWith(",")) {
			query.deleteCharAt(query.length() - 1);
		}
		query.append(")");
		return this;
	}

	public DslQueryBuilder limit(DslContext context) {
		/*
		 * limit queries are strange - You have to append in the end
		 */
		AAIDslParser.LimitStepContext ctx = (AAIDslParser.LimitStepContext) context.getCtx();
		context.setLimitQuery(new StringBuilder(".limit(").append(ctx.NODE().getText()).append(")"));
		return this;
	}

	public DslQueryBuilder filter(DslContext context) {
		return this.filterPropertyStart(context).filterPropertyKeys(context).filterPropertyEnd();

	}

	public DslQueryBuilder filterPropertyStart(DslContext context) {
		AAIDslParser.FilterStepContext ctx = (AAIDslParser.FilterStepContext) context.getCtx();
		if (ctx.NOT() != null && ctx.NOT().getText().equals("!"))
			query.append(".getVerticesExcludeByProperty(");
		else
			query.append(".getVerticesByProperty(");

		return this;

	}

	public DslQueryBuilder filterPropertyEnd() {
		query.append(")");
		return this;

	}

	public DslQueryBuilder filterPropertyKeys(DslContext context) {
		AAIDslParser.FilterStepContext ctx = (AAIDslParser.FilterStepContext) context.getCtx();
		final String key = ctx.KEY(0).getText();
		
		query.append(key);

		List<TerminalNode> nodes = ctx.KEY();
		List<String> valuesArray = nodes.stream().filter((node) -> !key.equals(node.getText()))
				                              .map((node) -> "'" + node.getText().replace("'", "").trim() + "'")
		                                      .collect(Collectors.toList());
		
		/*
		 * The whole point of doing this to separate P.within from key-value search
		 * For a list of values QB uses P.within
		 * For just a single value QB uses key,value check
		 */
		if (nodes.size() > 2) {
			String values = String.join(",", valuesArray);
			query.append(",").append(" new ArrayList<>(Arrays.asList(" + values.toString() + "))");
		} else {
			if (!valuesArray.isEmpty())
				query.append(",").append(valuesArray.get(0).toString());
		}
		return this;
	}

	public DslQueryBuilder union(DslContext context) {
		query.append(".union(");
		return this;
	}

	public DslQueryBuilder store(DslContext context) {
		AAIDslParser.SingleNodeStepContext ctx = (AAIDslParser.SingleNodeStepContext) context.getCtx();
		if (ctx.STORE() != null && ctx.STORE().getText().equals("*")) {
			query.append(".store('x')");
		}
		return this;

	}

	public DslQueryBuilder comma(DslContext context) {
		query.append(",");
		return this;

	}
}
