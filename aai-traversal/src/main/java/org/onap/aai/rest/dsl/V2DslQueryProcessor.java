/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright © 2024 Deutsche Telekom SA.
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.dsl.v2.AAIDslLexer;
import org.onap.aai.dsl.v2.AAIDslParser;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.dsl.v2.DslListener;
import org.onap.aai.rest.dsl.validation.DslValidator;
import org.onap.aai.rest.enums.QueryVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The Class DslQueryProcessor.
 */
@Component
@Scope("prototype")
public class V2DslQueryProcessor extends DslQueryProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(V2DslQueryProcessor.class);

    @Override
    protected Map<String, Object> getQueryResultMap(String aaiQuery) throws IOException, AAIException {
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

        ParseTree ptree = parser.aaiquery();

        validateQueryIsParsable(tokens);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("QUERY-interim {}", ptree.toStringTree(parser));
        }

        DslListener dslListener = SpringContextAware.getApplicationContext().getBean(DslListener.class);
        dslListener.setValidationFlag(hasStartNodeValidationFlag());
        dslListener.setAggregateFlag(isAggregate());

        if (!getValidationRules().isEmpty() && !"none".equals(getValidationRules())) {
            DslValidator validator = new DslValidator.Builder().create();
            dslListener.setQueryValidator(validator, getValidationRules());
        }

        // Walk it and attach our listener
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(dslListener, ptree);
        String query = dslListener.getQuery();

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("query", query);

        Map<String, List<String>> selectKeys = dslListener.getSelectKeys();
        if (selectKeys != null && !selectKeys.isEmpty()) {
            resultMap.put("propertiesMap", selectKeys);
        }

        LOGGER.info("Final QUERY {}", query);
        return resultMap;
    }
}
