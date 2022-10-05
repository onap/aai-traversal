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

import org.onap.aai.exceptions.AAIException;

public abstract class DslValidator {
    protected StringBuilder errorMessage = new StringBuilder("");

    protected DslValidator(DslValidator.Builder builder) {

    }

    public abstract boolean validate(DslValidatorRule dslValidatorRule) throws AAIException;

    public String getErrorMessage() {
        return errorMessage.toString();
    }

    public static class Builder {

        boolean isSchemaValidation = false;

        public Builder schema() {
            this.setSchemaValidation(true);
            return this;
        }

        private void setSchemaValidation(boolean schemaValidation) {
            isSchemaValidation = schemaValidation;
        }

        public DslValidator create() {
            if (isSchemaValidation) {
                return new DslSchemaValidator(this);
            }
            return new DslQueryValidator(this);
        }
    }
}
