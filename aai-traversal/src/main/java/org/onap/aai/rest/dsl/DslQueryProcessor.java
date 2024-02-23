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

import java.util.Map;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.enums.QueryVersion;

public interface DslQueryProcessor {

  Map<String, Object> parseAaiQuery(QueryVersion version, String aaiQuery)
      throws AAIException;

  boolean isStartNodeValidationFlag();

  void setStartNodeValidationFlag(boolean startNodeValidationFlag);

  boolean isAggregate();

  void setAggregate(boolean aggregate);

  String getValidationRules();

  void setValidationRules(String validationRules);

}