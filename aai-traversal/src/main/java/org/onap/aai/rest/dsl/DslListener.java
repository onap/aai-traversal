/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.rest.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import org.onap.aai.AAIDslParser;
import org.onap.aai.serialization.db.EdgeRules;
import org.onap.aai.AAIDslBaseListener;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * The Class DslListener.
 */
public class DslListener extends AAIDslBaseListener {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(DslQueryProcessor.class);
	private final EdgeRules edgeRules = EdgeRules.getInstance();

	protected List<String> list = null;
	//TODO Use StringBuilder to build the query than concat
	String query = "";

	Map<Integer, String> unionMap = new HashMap<>();
	Map<String, String> flags = new HashMap<>();

	String currentNode = "";
	String prevsNode = "";
	int commas = 0;

	int unionKey = 0;
	int unionMembers = 0;
	boolean isUnionBeg = false;
	boolean isUnionTraversal = false;

	boolean isTraversal = false;
	boolean isWhereTraversal = false;
	String whereTraversalNode = "";

	String limitQuery = "";
	boolean isNot = false;

	/**
	 * Instantiates a new DslListener.
	 */

	public DslListener() {
		list = new ArrayList<>();
	}

	@Override
	public void enterAaiquery(AAIDslParser.AaiqueryContext ctx) {
		query += "builder";
	}

	@Override
	public void enterDslStatement(AAIDslParser.DslStatementContext ctx) {
		// LOGGER.info("Statement Enter"+ctx.getText());
		/*
		 * This block of code is entered for every query statement
		 */
		if (isUnionBeg) {
			isUnionBeg = false;
			isUnionTraversal = true;

		} else if (unionMembers > 0) {
			unionMembers--;
			query += ",builder.newInstance()";
			isUnionTraversal = true;
		}

	}

	@Override
	public void exitDslStatement(AAIDslParser.DslStatementContext ctx) {
		/*
		 * Nothing to be done here for now
		 * LOGGER.info("Statement Exit"+ctx.getText());
		 */
	}

	@Override
	public void exitAaiquery(AAIDslParser.AaiqueryContext ctx) {
		/*
		 * dedup is by default for all queries If the query has limit in it
		 * include this as well LOGGER.info("Statement Exit"+ctx.getText());
		 */

		query += ".cap('x').unfold().dedup()" + limitQuery;
	}

	/*
	 * TODO: The contexts are not inherited from a single parent in AAIDslParser
	 * Need to find a way to do that
	 */
	@Override
	public void enterSingleNodeStep(AAIDslParser.SingleNodeStepContext ctx) {
		
		prevsNode = currentNode;
		currentNode = ctx.NODE().getText();

		this.generateQuery();
		if (ctx.STORE() != null && ctx.STORE().getText().equals("*")) {
			flags.put(currentNode, "store");
		}

	}

	@Override
	public void enterSingleQueryStep(AAIDslParser.SingleQueryStepContext ctx) {
		
		prevsNode = currentNode;
		currentNode = ctx.NODE().getText();
		this.generateQuery();

		if (ctx.STORE() != null && ctx.STORE().getText().equals("*")) {
			flags.put(currentNode, "store");
		}
	}

	@Override
	public void enterMultiQueryStep(AAIDslParser.MultiQueryStepContext ctx) {
		
		prevsNode = currentNode;
		currentNode = ctx.NODE().getText();
		this.generateQuery();
		
		if (ctx.STORE() != null && ctx.STORE().getText().equals("*")) {
			flags.put(currentNode, "store");
		}

	}

	/*
	 * Generates the QueryBuilder syntax for the dsl query
	 */
	private void generateQuery() {
		String edgeType = "";

		if (isUnionTraversal || isTraversal || isWhereTraversal) {
			String previousNode = prevsNode;
			if (isUnionTraversal) {
				previousNode = unionMap.get(unionKey);
				isUnionTraversal = false;
			}

			if (edgeRules.hasTreeEdgeRule(previousNode, currentNode)) {
				edgeType = "EdgeType.TREE";
			}else if (edgeRules.hasCousinEdgeRule(previousNode, currentNode, "")) {
				edgeType = "EdgeType.COUSIN";
			} else 
				edgeType = "EdgeType.COUSIN";
			
			query += ".createEdgeTraversal(" + edgeType + ", '" + previousNode + "','" + currentNode + "')";

		}

		else
			query += ".getVerticesByProperty('aai-node-type', '" + currentNode + "')";
	}

	@Override
	public void exitSingleNodeStep(AAIDslParser.SingleNodeStepContext ctx) {

		generateExitStep();
	}

	@Override
	public void exitSingleQueryStep(AAIDslParser.SingleQueryStepContext ctx) {
		generateExitStep();
	}

	@Override
	public void exitMultiQueryStep(AAIDslParser.MultiQueryStepContext ctx) {
		generateExitStep();

	}

	private void generateExitStep() {
		if (flags.containsKey(currentNode)) {
			String storeFlag = flags.get(currentNode);
			if (storeFlag != null && storeFlag.equals("store"))
				query += ".store('x')";
			flags.remove(currentNode);
		}
	}

	@Override
	public void enterUnionQueryStep(AAIDslParser.UnionQueryStepContext ctx) {
		isUnionBeg = true;

		unionKey++;
		unionMap.put(unionKey, currentNode);
		query += ".union(builder.newInstance()";

		List<TerminalNode> commaNodes = ctx.COMMA();

		for (TerminalNode node : commaNodes) {
			unionMembers++;
		}
	}

	@Override
	public void exitUnionQueryStep(AAIDslParser.UnionQueryStepContext ctx) {
		isUnionBeg = false;
		unionMap.remove(unionKey);

		query += ")";
		unionKey--;

	}

	@Override
	public void enterFilterTraverseStep(AAIDslParser.FilterTraverseStepContext ctx) {
		isWhereTraversal = true;
		whereTraversalNode = currentNode;
		query += ".where(builder.newInstance()";
	}

	@Override
	public void exitFilterTraverseStep(AAIDslParser.FilterTraverseStepContext ctx) {
		query += ")";
		isWhereTraversal = false;
		currentNode = whereTraversalNode;
	}

	@Override
	public void enterFilterStep(AAIDslParser.FilterStepContext ctx) {
		if (ctx.NOT() != null && ctx.NOT().getText().equals("!"))
			isNot = true;

		List<TerminalNode> nodes = ctx.KEY();
		String key = ctx.KEY(0).getText();

		if (isNot) {
			query += ".getVerticesExcludeByProperty(";
			isNot = false;
		} else
			query += ".getVerticesByProperty(";

		if (nodes.size() == 2) {
			query += key + "," + ctx.KEY(1).getText();
			query += ")";
		}

		if (nodes.size() > 2) {

			for (TerminalNode node : nodes) {
				if (node.getText().equals(key))
					continue;

				query += key + "," + node.getText();
				query += ")";
			}

		}

	}

	@Override
	public void exitFilterStep(AAIDslParser.FilterStepContext ctx) {
		// For now do nothing
	}

	@Override
	public void enterTraverseStep(AAIDslParser.TraverseStepContext ctx) {
		isTraversal = true;
	}

	@Override
	public void exitTraverseStep(AAIDslParser.TraverseStepContext ctx) {
		isTraversal = false;
	}

	@Override
	public void enterLimitStep(AAIDslParser.LimitStepContext ctx) {
		String value = ctx.NODE().getText();
		limitQuery += ".limit(" + value + ")";
	}
}
