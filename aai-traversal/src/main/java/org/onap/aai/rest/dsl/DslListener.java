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
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import org.onap.aai.AAIDslParser;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.edges.EdgeRuleQuery;
import org.onap.aai.edges.enums.EdgeType;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.logging.LogFormatTools;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.springframework.beans.factory.annotation.Autowired;
import org.onap.aai.AAIDslBaseListener;
import org.onap.aai.edges.EdgeIngestor;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * The Class DslListener.
 */
public class DslListener extends AAIDslBaseListener {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(DslQueryProcessor.class);

	private final EdgeIngestor edgeRules;

	DslContext context = null;
	DslQueryBuilder dslBuilder = null;

	/**
	 * Instantiates a new DslListener.
	 */
	@Autowired
	public DslListener(EdgeIngestor edgeIngestor, SchemaVersions schemaVersions, LoaderFactory loaderFactory) {
		this.edgeRules = edgeIngestor;
		context = new DslContext();

		Loader loader = loaderFactory.createLoaderForVersion(ModelType.MOXY, schemaVersions.getDefaultVersion());
		dslBuilder = new DslQueryBuilder(edgeIngestor, loader);
	}

	public String getQuery() throws AAIException {
		if (!getException().isEmpty()) {
			LOGGER.error("Exception in the DSL Query" + getException());
			throw new AAIException("AAI_6149", getException());
		}
		return dslBuilder.getQuery().toString();
	}

	public String getException() {
		return dslBuilder.getQueryException().toString();
	}

	@Override
	public void enterAaiquery(AAIDslParser.AaiqueryContext ctx) {
		/*
		 * This is my start-node, have some validations here
		 */
		context.setStartNodeFlag(true);
		dslBuilder.start();
	}

	@Override
	public void exitAaiquery(AAIDslParser.AaiqueryContext ctx) {
		dslBuilder.end(context);
	}

	@Override
	public void enterDslStatement(AAIDslParser.DslStatementContext ctx) {
		if (context.isUnionStart()) {
			dslBuilder.startUnion();
		}
	}

	@Override
	public void exitDslStatement(AAIDslParser.DslStatementContext ctx) {
		if (context.isUnionQuery()) {
			dslBuilder.comma(context);
			context.setUnionStart(true);
		}
	}

	@Override
	public void enterSingleNodeStep(AAIDslParser.SingleNodeStepContext ctx) {
		try {
			/*
			 * Set the previous Node to current node and get the new current
			 * node
			 */
			context.setPreviousNode(context.getCurrentNode());
			context.setCurrentNode(ctx.NODE().getText());

			if (context.isUnionQuery() || context.isTraversal() || context.isWhereQuery()) {
				String oldPreviousNode = context.getPreviousNode();

				if (context.isUnionStart()) {
					String previousNode = context.getUnionStartNodes().peek();
					context.setPreviousNode(previousNode);

					context.setUnionStart(false);
				}

				dslBuilder.edgeQuery(context);

				/*
				 * Reset is required bcos for union queries im changing the
				 * context
				 */
				context.setPreviousNode(oldPreviousNode);

			}

			else {
				dslBuilder.nodeQuery(context);
			}

		} catch (AAIException e) {
			LOGGER.info("AAIException in DslListener" + e.getMessage());
		}

	}

	@Override
	public void exitSingleNodeStep(AAIDslParser.SingleNodeStepContext ctx) {
		if (context.isStartNode() && isValidationFlag()) {
			try {
				dslBuilder.validateFilter(context);
			} catch (AAIException e) {
				LOGGER.error("AAIException in DslListener" + LogFormatTools.getStackTop(e));
			}
		}
		context.setStartNodeFlag(false);
		context.setCtx(ctx);
		dslBuilder.store(context);
	}

	private void generateExitStep() {

	}

	@Override
	public void enterUnionQueryStep(AAIDslParser.UnionQueryStepContext ctx) {

		Deque<String> unionStartNodes = context.getUnionStartNodes();
		unionStartNodes.add(context.getCurrentNode());

		context.setUnionStart(true);
		/*
		 * I may not need this
		 */
		context.setUnionQuery(true);
		dslBuilder.union(context);

	}

	@Override
	public void exitUnionQueryStep(AAIDslParser.UnionQueryStepContext ctx) {
		context.setUnionStart(false);
		context.setUnionQuery(false);
		Deque<String> unionStartNodes = context.getUnionStartNodes();
		if (unionStartNodes.peek() != null) {
			unionStartNodes.pop();
		}

		dslBuilder.endUnion(context);

	}

	@Override
	public void enterFilterTraverseStep(AAIDslParser.FilterTraverseStepContext ctx) {
		context.setWhereQuery(true);
		context.setWhereStartNode(context.getCurrentNode());
		dslBuilder.where(context);

	}

	@Override
	public void exitFilterTraverseStep(AAIDslParser.FilterTraverseStepContext ctx) {
		context.setWhereQuery(false);
		context.setCurrentNode(context.getWhereStartNode());

		dslBuilder.endWhere(context);

	}

	@Override
	public void enterFilterStep(AAIDslParser.FilterStepContext ctx) {

		context.setCtx(ctx);
		dslBuilder.filter(context);
	}

	@Override
	public void exitFilterStep(AAIDslParser.FilterStepContext ctx) {
		// For now do nothing
	}

	@Override
	public void enterTraverseStep(AAIDslParser.TraverseStepContext ctx) {
		context.setTraversal(true);
	}

	@Override
	public void exitTraverseStep(AAIDslParser.TraverseStepContext ctx) {
		context.setTraversal(false);
	}

	@Override
	public void enterLimitStep(AAIDslParser.LimitStepContext ctx) {
		context.setCtx(ctx);
		dslBuilder.limit(context);
	}
	
	public void setValidationFlag(boolean validationFlag) {
		this.context.setValidationFlag(validationFlag);
	}
	
	public boolean isValidationFlag() {
		return this.context.isValidationFlag();
	}

}
