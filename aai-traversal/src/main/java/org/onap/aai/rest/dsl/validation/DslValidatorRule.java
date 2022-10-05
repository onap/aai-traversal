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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.dsl.validation;

import java.util.LinkedList;
import java.util.List;

public class DslValidatorRule {

    private static final String LOOP_RULE = "loop";
    private static final String NODECOUNT_RULE = "nodeCount";
    private static final String ALL_RULE = "all";

    private String query;
    private boolean validateLoop;
    private boolean validateNodeCount;
    private int nodeCount;
    private List<String> edges;

    protected DslValidatorRule(DslValidatorRule.Builder builder) {
        query = builder.query;
        validateLoop = builder.validateLoop;
        validateNodeCount = builder.validateNodeCount;
        nodeCount = builder.nodeCount;
        edges = builder.getEdges();
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isValidateLoop() {
        return validateLoop;
    }

    public void setValidateLoop(boolean validateLoop) {
        this.validateLoop = validateLoop;
    }

    public boolean isValidateNodeCount() {
        return validateNodeCount;
    }

    public void setValidateNodeCount(boolean validateNodeCount) {
        this.validateNodeCount = validateNodeCount;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public List<String> getEdges() {
        return edges;
    }

    public void setEdges(List<String> edges) {
        this.edges = edges;
    }

    public static class Builder {

        // todo optional
        String query = "";
        boolean validateLoop = false;
        boolean validateNodeCount = false;
        int nodeCount = 0;
        List<String> edges = new LinkedList<>();

        public List<String> getEdges() {
            return edges;
        }

        public void setEdges(List<String> edges) {
            this.edges = edges;
        }

        public Builder query(String query) {
            this.setQuery(query);
            return this;
        }

        public Builder loop(String validateLoop, List<String> edges) {
            if (validateLoop.contains(LOOP_RULE) || validateLoop.contains(ALL_RULE)) {
                this.setValidateLoop(true);
                this.setEdges(edges);
            }

            return this;
        }

        public Builder nodeCount(String validateNodeCount, int nodeCount) {
            if (validateNodeCount.contains(NODECOUNT_RULE)
                || validateNodeCount.contains(ALL_RULE)) {
                this.setValidateNodeCount(true);
                this.nodeCount = nodeCount;
            }
            return this;
        }

        private void setQuery(String query) {
            this.query = query;
        }

        private void setValidateLoop(boolean validateLoop) {
            this.validateLoop = validateLoop;
        }

        private void setValidateNodeCount(boolean validateNodeCount) {
            this.validateNodeCount = validateNodeCount;
        }

        public DslValidatorRule build() {

            return new DslValidatorRule(this);
        }
    }
}
