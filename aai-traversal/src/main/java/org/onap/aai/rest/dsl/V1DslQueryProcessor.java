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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.dsl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.dsl.v1.AAIDslLexer;
import org.onap.aai.dsl.v1.AAIDslParser;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.dsl.v1.DslListener;
import org.onap.aai.rest.dsl.validation.DslValidator;
import org.onap.aai.rest.enums.QueryVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The Class DslQueryProcessor.
 */
@Scope("prototype")
@Component(value = "dslQueryProcessor")
public class V1DslQueryProcessor implements DslQueryProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(V1DslQueryProcessor.class);
    private static final String EOF_TOKEN = "<EOF>";

    private boolean startNodeValidationFlag = true;
    private String validationRules = "";

    private boolean isAggregate = false;

    @Override
    public Map<String, Object> parseAaiQuery(QueryVersion version, String aaiQuery)
        throws AAIException {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // Create a input stream that reads our string
            InputStream stream =
                new ByteArrayInputStream(aaiQuery.getBytes(StandardCharsets.UTF_8));

            Lexer lexer = new AAIDslLexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
            lexer.removeErrorListeners();
            lexer.addErrorListener(new AAIDslErrorListener());
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            // Parser that feeds off of the tokens buffer
            AAIDslParser parser = new AAIDslParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(new AAIDslErrorListener());
            // ParseTreeListener dslListener = dslListeners.get(version);
            DslListener dslListener = SpringContextAware.getApplicationContext().getBean(DslListener.class);
            dslListener.setValidationFlag(isStartNodeValidationFlag());
            dslListener.setAggregateFlag(isAggregate());

            if (!getValidationRules().isEmpty() && !"none".equals(getValidationRules())) {
                DslValidator validator = new DslValidator.Builder().create();
                dslListener.setQueryValidator(validator, getValidationRules());
            }

            ParseTree ptree = parser.aaiquery();

            // Check if there is no EOF token at the end of the parsed aaiQuery
            // If none, DSL query may have not been parsed correctly and omitted part of the query
            // out. If so error out.
            // If it wasn't expecting a opening parenthesis after a closing bracket for union, it will
            // drop the proceeding part of the query.
            Token eofToken = tokens.get(tokens.size() - 1);
            if (eofToken != null && !eofToken.getText().equals(EOF_TOKEN)) {
                if (eofToken.getText().equals("(")) {
                    throw new AAIException("AAI_6153",
                        "DSL Syntax Error while processing the query: DSL Query could not be parsed correctly. Please check your syntax.");
                }
            }

            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("QUERY-interim {}", ptree.toStringTree(parser));
            }

            // Walk it and attach our listener
            ParseTreeWalker walker = new ParseTreeWalker();

            walker.walk(dslListener, ptree);
            String query = dslListener.getQuery();
            resultMap.put("query", query);

            LOGGER.info("Final QUERY {}", query);
            return resultMap;
        } catch(AAIException ex) {
            throw new AAIException((ex.getCode().isEmpty() ? "AAI_6149" : ex.getCode()),
                "DSL Error while processing the query :" + ex.getMessage());
        } catch (ParseCancellationException e) {
            throw new AAIException("AAI_6153",
                "DSL Syntax Error while processing the query: " + e.getMessage());

        } catch (Exception e) {
            throw new AAIException("AAI_6152",
                "Error while processing the query: " + e.getMessage());
        }
    }

    @Override
    public boolean isStartNodeValidationFlag() {
        return startNodeValidationFlag;
    }

    @Override
    public void setStartNodeValidationFlag(boolean startNodeValidationFlag) {
        this.startNodeValidationFlag = startNodeValidationFlag;
    }

    @Override
    public boolean isAggregate() {
        return isAggregate;
    }

    @Override
    public void setAggregate(boolean aggregate) {
        this.isAggregate = aggregate;
    }

    @Override
    public String getValidationRules() {
        return validationRules;
    }

    @Override
    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }
}
