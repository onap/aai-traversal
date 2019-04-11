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
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.onap.aai.AAIDslLexer;
import org.onap.aai.AAIDslParser;
import org.onap.aai.exceptions.AAIException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


/**
 * The Class DslQueryProcessor.
 */
public class DslQueryProcessor {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(DslQueryProcessor.class);

	private DslListener dslListener;
	private boolean validationFlag = true;

	@Autowired
	public DslQueryProcessor(DslListener dslListener) {
		this.dslListener = dslListener;
	}

	public String parseAaiQuery(String aaiQuery) throws AAIException {
		try {
			// Create a input stream that reads our string
			InputStream stream = new ByteArrayInputStream(aaiQuery.getBytes(StandardCharsets.UTF_8));

			// Create a lexer from the input CharStream
			AAIDslLexer lexer = new AAIDslLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
			lexer.removeErrorListeners();
			lexer.addErrorListener(new AAIDslErrorListener());

			// Get a list of tokens pulled from the lexer
			CommonTokenStream tokens = new CommonTokenStream(lexer);

			// Parser that feeds off of the tokens buffer
			AAIDslParser parser = new AAIDslParser(tokens);
			parser.removeErrorListeners(); // remove ConsoleErrorListener
			parser.addErrorListener(new AAIDslErrorListener());

			dslListener.setValidationFlag(isValidationFlag());
			// Specify our entry point
			ParseTree ptree = parser.aaiquery();
			LOGGER.info("QUERY-interim" + ptree.toStringTree(parser));


			// Walk it and attach our listener
			ParseTreeWalker walker = new ParseTreeWalker();
			walker.walk(dslListener, ptree);
			LOGGER.info("Final QUERY" + dslListener.getQuery());

			/*
			 * TODO - Visitor patternQueryDslVisitor visitor = new
			 * QueryDslVisitor(); String query = visitor.visit(ptree);
			 * 
			 */
			return dslListener.getQuery();
		} catch(ParseCancellationException e){
			throw new AAIException("AAI_6149", "DSL Syntax Error while processing the query :" + e.getMessage());
		} catch(AAIException e) {
			throw new AAIException("AAI_6149", "DSL Syntax Error  while processing the query :" + e.getMessage());
		} catch (Exception e) {
			throw new AAIException("AAI_6149","Error while processing the query :" + e.getMessage());
		}
	}
	public boolean isValidationFlag() {
		return validationFlag;
	}

	public void setValidationFlag(boolean validationFlag) {
		this.validationFlag = validationFlag;
	}
}
