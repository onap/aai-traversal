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

import java.util.Deque;
import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;

public class DslContext {

	private ParserRuleContext ctx;

	private String currentNode;

	private String previousNode;

	private boolean isTraversal = false;
	private boolean isWhereQuery = false;
	private boolean isUnionQuery = false;
	private boolean isUnionStart = false;

	private String whereStartNode = "";

	private Deque<String> unionStartNodes = new LinkedList<String>();

	/*
	 * Limit Queries have to be applied in the end - so i have to set this in
	 * context
	 */
	StringBuilder limitQuery = new StringBuilder();

	public ParserRuleContext getCtx() {
		return ctx;
	}

	public void setCtx(ParserRuleContext ctx) {
		this.ctx = ctx;
	}

	public String getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(String currentNode) {
		this.currentNode = currentNode;
	}

	public String getPreviousNode() {
		return previousNode;
	}

	public void setPreviousNode(String previousNode) {
		this.previousNode = previousNode;
	}

	public boolean isTraversal() {
		return isTraversal;
	}

	public void setTraversal(boolean isTraversal) {
		this.isTraversal = isTraversal;
	}

	public boolean isWhereQuery() {
		return isWhereQuery;
	}

	public void setWhereQuery(boolean isWhereQuery) {
		this.isWhereQuery = isWhereQuery;
	}

	public boolean isUnionQuery() {
		return isUnionQuery;
	}

	public void setUnionQuery(boolean isUnionQuery) {
		this.isUnionQuery = isUnionQuery;
	}

	public String getWhereStartNode() {
		return whereStartNode;
	}

	public void setWhereStartNode(String whereStartNode) {
		this.whereStartNode = whereStartNode;
	}

	public Deque<String> getUnionStartNodes() {
		return unionStartNodes;
	}

	public void setUnionStartNodes(Deque<String> unionStartNodes) {
		this.unionStartNodes = unionStartNodes;
	}

	public boolean isUnionStart() {
		return isUnionStart;
	}

	public void setUnionStart(boolean isUnionStart) {
		this.isUnionStart = isUnionStart;
	}

	public StringBuilder getLimitQuery() {
		return limitQuery;
	}

	public void setLimitQuery(StringBuilder limitQuery) {
		this.limitQuery = limitQuery;
	}

}
