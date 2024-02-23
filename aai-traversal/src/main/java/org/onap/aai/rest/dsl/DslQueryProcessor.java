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