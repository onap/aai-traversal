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
package org.onap.aai.rest.search;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.transform.TimedInterrupt;

/**
 * Creates and returns a groovy shell with the
 * configuration to statically import graph classes
 *
 */
public class GremlinGroovyShellSingleton {

	private final GroovyShell shell;
	private GremlinGroovyShellSingleton() {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("value", 30000);
		parameters.put("unit", new PropertyExpression(new ClassExpression(ClassHelper.make(TimeUnit.class)),"MILLISECONDS"));

		ASTTransformationCustomizer custom = new ASTTransformationCustomizer(parameters, TimedInterrupt.class);
		ImportCustomizer imports = new ImportCustomizer();
		imports.addStaticStars(
            "org.apache.tinkerpop.gremlin.process.traversal.P"
		);
		imports.addImports(
				"org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__",
				"org.apache.tinkerpop.gremlin.structure.T",
				"org.apache.tinkerpop.gremlin.process.traversal.P");
		CompilerConfiguration config = new CompilerConfiguration();
		config.addCompilationCustomizers(custom, imports);

		this.shell = new GroovyShell(config);
	}
	
	 private static class Helper {
		 private static final GremlinGroovyShellSingleton INSTANCE = new GremlinGroovyShellSingleton();
	 }

	 public static GremlinGroovyShellSingleton getInstance() {
		 
		 return Helper.INSTANCE;
	 }

	/** 
	 * @param traversal
	 * @param params
	 * @return result of graph traversal
	 */
	public GraphTraversal<?, ?> executeTraversal (String traversal, Map<String, Object> params) {
		Binding binding = new Binding(params);
		Script script = shell.parse(traversal);
		script.setBinding(binding);
		return (GraphTraversal<?, ?>) script.run();
	}
}
