/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2018-2019 AT&T Intellectual Property. All rights reserved.
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
package org.onap.aai.rest.dsl.validation;

import org.onap.aai.util.AAIConfig;
import org.onap.aai.util.TraversalConstants;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DslQueryValidator extends DslValidator {

    protected DslQueryValidator(Builder builder) {
        super(builder);
    }

    public boolean validate(DslValidatorRule dslValidatorRule) {

        return validateLoop(dslValidatorRule.isValidateLoop(), dslValidatorRule.getEdges()) && validateNodeCount(dslValidatorRule.isValidateNodeCount(), dslValidatorRule.getNodeCount());
    }

    private boolean validateLoop(boolean isValidateLoop, List<String> edges) {
        if (isValidateLoop) {
            Set<String> uniqueEdges = new LinkedHashSet<>(edges);

            if (uniqueEdges.size() < (edges.size() / 2)) {
                this.errorMessage.append("Loop Validation failed");
                return false;
            }
        }
        return true;
    }

    private boolean validateNodeCount(boolean isValidateNodeCount, int nodeCount) {
        String maxNodeString = AAIConfig.get("aai.dsl.max.nodecount", TraversalConstants.DSL_MAX_NODE_COUNT);
        int maxNodeCount = Integer.parseInt(maxNodeString);
        if (isValidateNodeCount && nodeCount > maxNodeCount) {
            this.errorMessage.append("NodeCount Validation failed");
            return false;
        }
        return true;
    }

}
