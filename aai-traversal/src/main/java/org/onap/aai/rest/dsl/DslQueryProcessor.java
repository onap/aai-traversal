/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2024 Deutsche Telekom SA.
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

import java.io.IOException;
import java.util.Map;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.enums.QueryVersion;

public abstract class DslQueryProcessor {

    private static final String EOF_TOKEN = "<EOF>";

    private boolean hasStartNodeValidationFlag = true;
    private boolean isAggregate = false;
    private String validationRules = "";

    protected abstract Map<String, Object> getQueryResultMap(String aaiQuery) throws IOException, AAIException;

    public Map<String, Object> parseAaiQuery(QueryVersion version, String aaiQuery)
        throws AAIException {
        try {
            return getQueryResultMap(aaiQuery);
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

    /**
     * Check if there is no EOF token at the end of the parsed aaiQuery
     * If none, DSL query may have not been parsed correctly and omitted part of the query
     * out. If so error out.
     * If it wasn't expecting an opening parenthesis after a closing bracket for union, it will
     * drop the proceeding part of the query.
     * @param tokens
     * @throws AAIException
     */
    protected void validateQueryIsParsable(CommonTokenStream tokens) throws AAIException {

        Token eofToken = tokens.get(tokens.size() - 1);
        if (eofToken != null && !eofToken.getText().equals(EOF_TOKEN)) {
            if (eofToken.getText().equals("(")) {
                throw new AAIException("AAI_6153",
                    "DSL Syntax Error while processing the query: DSL Query could not be parsed correctly. Please check your syntax.");
            }
        }
    }

    public boolean hasStartNodeValidationFlag() {
        return hasStartNodeValidationFlag;
    }

    public void setStartNodeValidationFlag(boolean startNodeValidationFlag) {
        this.hasStartNodeValidationFlag = startNodeValidationFlag;
    }

    public boolean isAggregate() {
        return isAggregate;
    }

    public void setAggregate(boolean aggregate) {
        this.isAggregate = aggregate;
    }

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }

}