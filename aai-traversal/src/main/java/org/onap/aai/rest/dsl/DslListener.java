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
import java.util.List;
import java.util.stream.Collectors;

import org.onap.aai.AAIDslBaseListener;
import org.onap.aai.AAIDslParser;
import org.onap.aai.AAIDslParser.UnionQueryContext;
import org.onap.aai.AAIDslParser.WhereContext;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Introspector;
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

	private boolean validate = false;
	
	private final Loader loader;
	
	private boolean hasReturnValue = false;
	
	/**
	 * Instantiates a new DslListener.
	 */
	@Autowired
	public DslListener(EdgeIngestor edgeIngestor, SchemaVersions schemaVersions, LoaderFactory loaderFactory) {

		loader = loaderFactory.createLoaderForVersion(ModelType.MOXY, schemaVersions.getDefaultVersion());
		dslBuilder = new DslQueryBuilder(edgeIngestor);
	}

	public String getQuery() throws AAIException {
		return dslBuilder.getQuery().toString();
	}

	@Override
	public void enterAaiquery(AAIDslParser.AaiqueryContext ctx) {

		dslBuilder.start();
	}

	@Override
	public void exitAaiquery(AAIDslParser.AaiqueryContext ctx) {
		if (!hasReturnValue) {
			throw new RuntimeException(new AAIException("AAI_6149", "No nodes marked for output"));
		}
	}
	
	@Override
	public void enterStartStatement(AAIDslParser.StartStatementContext ctx) {

	}
	
	public void validateFilter(String nodeName, List<String> propKeys) throws AAIException {
		Introspector obj = loader.introspectorFromName(nodeName);
		if(propKeys.isEmpty()){
			throw new AAIException("AAI_6149", "No keys sent. Valid keys for " + nodeName + " are "
					+ String.join(",", obj.getIndexedProperties()));
		}
		boolean notIndexed = propKeys.stream()
				.filter((prop) -> obj.getIndexedProperties().contains(removeSingleQuotes(prop))).collect(Collectors.toList()).isEmpty();
		if (notIndexed)
			throw new AAIException("AAI_6149", "Non indexed keys sent. Valid keys for " + nodeName+ " "
					+ String.join(",", obj.getIndexedProperties()));

	}
	
	protected String removeSingleQuotes(String value) {
		return value.replaceFirst("^'(.*)'$", "$1");
	}
	
	@Override
	public void exitStartStatement(AAIDslParser.StartStatementContext ctx) {
		
		dslBuilder.end();
	}

	@Override
	public void enterDslStatement(AAIDslParser.DslStatementContext ctx) {
		if (ctx.getParent() instanceof UnionQueryContext || ctx.getParent() instanceof WhereContext) {
			dslBuilder.identity();
		}
	}
	
	@Override
	public void exitDslStatement(AAIDslParser.DslStatementContext ctx) {
		if (ctx.getParent() instanceof UnionQueryContext && ((UnionQueryContext)ctx.getParent()).dslStatement().size() > 1) {
			
			dslBuilder.comma();
		}
	}

	@Override
	public void enterNodeStep(AAIDslParser.NodeStepContext ctx) {
		try {

			if (!traversedNodeNames.isEmpty()) {
				dslBuilder.edgeQuery(traversedNodeNames.peekFirst(), ctx.node().getText());
			} else {
				if (validate) {
					String nodeName = ctx.node().getText();
					List<String> propKeys = ctx.storableStep().propertyFilter().stream().map(item -> item.key().getText()).collect(Collectors.toList());
					validateFilter(nodeName, propKeys);
				}
				dslBuilder.nodeQuery(ctx.node().getText());
			}
		} catch (AAIException e) {
			LOGGER.info("AAIException in DslListener " + e.getMessage());
			throw new RuntimeException(e);
		}
		traversedNodeNames.addFirst(ctx.node().getText());
	}
	
	@Override
	public void exitNodeStep(AAIDslParser.NodeStepContext ctx) {
		traversedNodeNames.removeFirst();
	}
	
	@Override
	public void enterPropertyFilter(AAIDslParser.PropertyFilterContext ctx) {
		final String key = ctx.key().getText();
		List<String> values = ctx.value().stream().map(item -> item.getText()).collect(Collectors.toList());
		dslBuilder.filterPropertyStart(ctx.not() != null);
		dslBuilder.filterPropertyKeys(key, values);
	}
	
	@Override
	public void exitPropertyFilter(AAIDslParser.PropertyFilterContext ctx) {
		dslBuilder.filterPropertyEnd();
	}
	
	@Override
	public void enterStorableStep(AAIDslParser.StorableStepContext ctx) {
	}
	
	@Override
	public void exitStorableStep(AAIDslParser.StorableStepContext ctx) {
		if (ctx.store() != null) {
			dslBuilder.store();
			hasReturnValue = true;
		}
	}

	@Override
	public void enterUnionQuery(AAIDslParser.UnionQueryContext ctx) {
		
		dslBuilder.union();
	}

	@Override
	public void exitUnionQuery(AAIDslParser.UnionQueryContext ctx) {
		
		dslBuilder.endUnion();
	}

	@Override
	public void enterWhere(AAIDslParser.WhereContext ctx) {
		
		dslBuilder.where();
	}

	@Override
	public void exitWhere(AAIDslParser.WhereContext ctx) {

		dslBuilder.endWhere();
		
	}
	
	@Override
	public void exitLimitStep(AAIDslParser.LimitStepContext ctx) {
		
		dslBuilder.limit(ctx.numericValue().getText());
	}
	
	public void setValidationFlag(boolean validationFlag) {
		this.validate = validationFlag;
	}
}
