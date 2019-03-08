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

import java.util.ArrayDeque;
import java.util.Deque;

import org.onap.aai.AAIDslBaseListener;
import org.onap.aai.AAIDslParser;
import org.onap.aai.AAIDslParser.AaiqueryContext;
import org.onap.aai.AAIDslParser.SingleNodeStepContext;
import org.onap.aai.AAIDslParser.UnionQueryStepContext;
import org.onap.aai.AAIDslParser.WhereStepContext;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.setup.SchemaVersions;
import org.springframework.beans.factory.annotation.Autowired;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * The Class DslListener.
 */
public class DslListener extends AAIDslBaseListener {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(DslQueryProcessor.class);

	private DslQueryBuilder dslBuilder = null;
	
	private Deque<String> traversedNodeNames = new ArrayDeque<>();

	/**
	 * Instantiates a new DslListener.
	 */
	@Autowired
	public DslListener(EdgeIngestor edgeIngestor, SchemaVersions schemaVersions, LoaderFactory loaderFactory) {

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

		dslBuilder.start();
	}

	@Override
	public void exitAaiquery(AAIDslParser.AaiqueryContext ctx) {

		dslBuilder.end();
		if (ctx.limitStep() != null) {
			dslBuilder.limit(ctx.limitStep());
		}
	}

	@Override
	public void enterDslStatement(AAIDslParser.DslStatementContext ctx) {
		if (!(ctx.getParent() instanceof AaiqueryContext)) {
			dslBuilder.identity();
		}
	}
	
	@Override
	public void exitDslStatement(AAIDslParser.DslStatementContext ctx) {
		if (ctx.getParent() instanceof UnionQueryStepContext && ((UnionQueryStepContext)ctx.getParent()).dslStatement().size() > 1) {
			
			dslBuilder.comma();
		}
	}

	@Override
	public void enterSingleNodeStep(AAIDslParser.SingleNodeStepContext ctx) {
		try {
			
			//if in a union, start with a traversal
			if (ctx.getParent() != null && ctx.getParent().getParent() instanceof UnionQueryStepContext) {
				dslBuilder.edgeQuery(traversedNodeNames.peekFirst(), ctx.node().getText());
			}
			if (ctx.filterStep() != null) {
				dslBuilder.createNode(ctx);
			} 
			
			//store goes after where queries, only add it if we don't have one
			if (!(ctx.getChild(WhereStepContext.class,0) instanceof WhereStepContext)) {
				dslBuilder.store(ctx);
			}
			
			//push the node name on to our stack
			traversedNodeNames.addFirst(ctx.node().getText());
		} catch (AAIException e) {
			LOGGER.info("AAIException in DslListener " + e.getMessage());
			throw new RuntimeException(e);
		}

	}

	@Override
	public void exitSingleNodeStep(AAIDslParser.SingleNodeStepContext ctx) {
		if (!traversedNodeNames.isEmpty()) {
			traversedNodeNames.removeFirst();
		}
	}

	@Override
	public void enterUnionQueryStep(AAIDslParser.UnionQueryStepContext ctx) {
		
		dslBuilder.union();
	}

	@Override
	public void exitUnionQueryStep(AAIDslParser.UnionQueryStepContext ctx) {
		
		dslBuilder.endUnion();
	}

	@Override
	public void enterWhereStep(AAIDslParser.WhereStepContext ctx) {
		
		dslBuilder.where();
	}

	@Override
	public void exitWhereStep(AAIDslParser.WhereStepContext ctx) {

		dslBuilder.endWhere();
		if (ctx.getParent() instanceof SingleNodeStepContext) {
			dslBuilder.store(((SingleNodeStepContext)ctx.getParent()));
		}
	}


	@Override
	public void enterTraverseStep(AAIDslParser.TraverseStepContext ctx) {
		
		try {
			if (ctx.singleNodeStep() != null) {
				dslBuilder.edgeQuery(traversedNodeNames.peekFirst(), ctx.singleNodeStep().node().getText());
			}
		} catch (AAIException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	public void setValidationFlag(boolean validationFlag) {
		this.dslBuilder.setValidationFlag(validationFlag);
	}
}
