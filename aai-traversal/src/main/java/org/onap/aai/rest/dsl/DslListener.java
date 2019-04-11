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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.collect.Lists;
import org.onap.aai.AAIDslBaseListener;
import org.onap.aai.AAIDslParser;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.setup.SchemaVersions;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Class DslListener.
 */
public class DslListener extends AAIDslBaseListener {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(DslListener.class);

	boolean validationFlag = false;
	EdgeIngestor edgeIngestor;
	Loader loader;

	private Deque<DslQueryBuilder> dslQueryBuilders = new LinkedList<>();
	private Deque<String> traversedNodes = new LinkedList<>();
	private Deque<List<String>> returnedNodes = new LinkedList<>();

	List<String> traversedEdgeLabels = new LinkedList<>();

	/**
	 * Instantiates a new DslListener.
	 */
	@Autowired
	public DslListener(EdgeIngestor edgeIngestor, SchemaVersions schemaVersions, LoaderFactory loaderFactory) {
		this.loader = loaderFactory.createLoaderForVersion(ModelType.MOXY, schemaVersions.getDefaultVersion());
		this.edgeIngestor = edgeIngestor;
	}

	public DslQueryBuilder builder() {
		return dslQueryBuilders.peekFirst();
	}

	public String getQuery() throws AAIException {
		//TODO Change the exception reporting
		if (!getException().isEmpty()) {
			LOGGER.error("Exception in the DSL Query" + getException());
			throw new AAIException("AAI_6149", getException());
		}

		return this.compile();
	}

	public String compile() {
		List<String> queries = dslQueryBuilders.stream().map(dslQb -> dslQb.getQuery().toString()).collect(Collectors.toList());
		return String.join("", Lists.reverse(queries));
	}

	public String getException() {
		return builder().getQueryException().toString();
	}

	@Override
	public void enterAaiquery(AAIDslParser.AaiqueryContext ctx) {
		dslQueryBuilders.push(new DslQueryBuilder(edgeIngestor, loader));
	}

	@Override
	public void enterStartStatement(AAIDslParser.StartStatementContext ctx) {
		builder().start();
	}

	@Override
	public void exitStartStatement(AAIDslParser.StartStatementContext ctx) {
		builder().end();
		if (!traversedNodes.isEmpty()) {
			traversedNodes.removeFirst();
		}

	}

	@Override
	public void exitLimit(AAIDslParser.LimitContext ctx) {
		builder().limit(ctx.num().getText());
	}

	@Override
	public void enterNestedStatement(AAIDslParser.NestedStatementContext ctx) {
		dslQueryBuilders.addFirst(new DslQueryBuilder(edgeIngestor, loader));
		builder().startInstance();
	}

	@Override
	public void exitNestedStatement(AAIDslParser.NestedStatementContext ctx) {
		int count = 1;
		if(!ctx.traversal().isEmpty()) {
			count += ctx.traversal().size() ;
		}
		//TODO so ugly
		String resultNode = traversedNodes.peekFirst();

		if (!traversedNodes.isEmpty()) {
			Stream<Integer> integers = Stream.iterate(0, i -> i + 1);
			integers.limit(count)
					.forEach(i -> traversedNodes.removeFirst());
		}
		List<String> resultNodes = returnedNodes.pop();
		resultNodes.add(resultNode);
		returnedNodes.addFirst(resultNodes);
	}

	@Override
	public void enterComma(AAIDslParser.CommaContext ctx) {
		builder().comma();
	}

	@Override
	public void enterVertex(AAIDslParser.VertexContext ctx) {

		if (!traversedNodes.isEmpty()) {
			builder().edgeQuery(traversedEdgeLabels, traversedNodes.peekFirst(), ctx.label().getText());
		} else {
			builder().nodeQuery(ctx.label().getText());
		}

		traversedNodes.addFirst(ctx.label().getText());
	}

	@Override
	public void exitVertex(AAIDslParser.VertexContext ctx) {

		/*TODO dont use context */
		if (ctx.getParent() instanceof AAIDslParser.StartStatementContext && isValidationFlag()) {
			List<String> allKeys = new ArrayList<>();

			if (ctx.filter() != null) {
				allKeys = ctx.filter().propertyFilter().stream().flatMap(
						pf -> pf.key().stream()).map(
						e -> e.getText().replaceAll("\'", "")).collect(Collectors.toList());

			}
			builder().validateFilter(ctx.label().getText(), allKeys);
		}
		if (ctx.store() != null) {
			builder().store();
		}
		traversedEdgeLabels = new ArrayList<>();
	}


	@Override
	public void enterUnionVertex(AAIDslParser.UnionVertexContext ctx) {
		returnedNodes.addFirst(new ArrayList<>());
		builder().union();
	}

	@Override
	public void exitUnionVertex(AAIDslParser.UnionVertexContext ctx) {
		String resultNode = returnedNodes.pop().get(0);
		traversedNodes.addFirst(resultNode);
		builder().endUnion();
	}

	@Override
	public void enterWhereFilter(AAIDslParser.WhereFilterContext ctx) {
		returnedNodes.addFirst(new ArrayList<>());
		builder().where();
	}

	@Override
	public void exitWhereFilter(AAIDslParser.WhereFilterContext ctx) {
		if(!returnedNodes.isEmpty()) {
			returnedNodes.pop();
		}
		builder().endWhere();
	}

	@Override
	public void enterTraversal(AAIDslParser.TraversalContext ctx) {
	}

	@Override
	public void enterEdge(AAIDslParser.EdgeContext ctx) {
	}

	@Override
	public void enterEdgeFilter(AAIDslParser.EdgeFilterContext ctx) {
		traversedEdgeLabels = ctx.key().stream().map(value -> value.getText()).collect(Collectors.toList());

	}

	@Override
	public void enterFilter(AAIDslParser.FilterContext ctx) {

	}

	@Override
	public void enterPropertyFilter(AAIDslParser.PropertyFilterContext ctx) {

		List<AAIDslParser.KeyContext> valueList = ctx.key();
		String filterKey = valueList.get(0).getText();

		boolean isNot = ctx.not() != null && !ctx.not().isEmpty();
		List<AAIDslParser.NumContext> numberValues = ctx.num();

		/*
		 * Add all String values
		 */
		List<String> values = valueList.stream().filter(value -> !filterKey.equals(value.getText()))
				.map(value -> "'" + value.getText().replace("'", "") + "'").collect(Collectors.toList());
		/*
		 * Add all numeric values
		 */
		values.addAll(numberValues.stream().filter(value -> !filterKey.equals(value.getText()))
				.map(value -> value.getText()).collect(Collectors.toList()));

		builder().filter(isNot, traversedNodes.peekFirst(), filterKey, values);

	}

	public boolean isValidationFlag() {
		return validationFlag;
	}

	public void setValidationFlag(boolean validationFlag) {
		this.validationFlag = validationFlag;
	}

}
