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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.EdgeRule;
import org.onap.aai.edges.EdgeRuleQuery;
import org.onap.aai.edges.enums.AAIDirection;
import org.onap.aai.edges.enums.EdgeType;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.exceptions.AAIUnknownObjectException;
import org.onap.aai.schema.enums.PropertyMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class DslQueryBuilder {

    private final EdgeIngestor edgeRules;
    private final Loader loader;
    private StringBuilder query;
    private StringBuilder queryException;

    private static final Logger LOGGER = LoggerFactory.getLogger(DslQueryBuilder.class);
    private long selectCount = 0;

    public DslQueryBuilder(EdgeIngestor edgeIngestor, Loader loader) {
        this.edgeRules = edgeIngestor;
        this.loader = loader;
        query = new StringBuilder();
        queryException = new StringBuilder();
    }

    public StringBuilder getQuery() {
        return query;
    }

    public void setQuery(StringBuilder query) {
        this.query = query;
    }

    public StringBuilder getQueryException() {
        return queryException;
    }

    public void setQueryException(StringBuilder queryException) {
        this.queryException = queryException;
    }

    public DslQueryBuilder start() {
        query.append("builder");
        return this;
    }

    /*
     * DSL always dedupes the results
     */
    public DslQueryBuilder end(long selectCounter) {
        selectCount = selectCounter;
        if (selectCounter <= 0) {
            return this.end();
        } else {
            query.append(".select(Pop.mixed,'stepMain').fold().dedup()");
        }
        return this;
    }

    public DslQueryBuilder end() {
        query.append(".cap('x').unfold().dedup()");
        return this;
    }

    public DslQueryBuilder nodeQuery(String node) {
        query.append(".getVerticesByProperty('aai-node-type', '").append(node).append("')");
        return this;
    }

    public DslQueryBuilder edgeQuery(List<String> edgeLabels, String aNode, String bNode) {
        EdgeRuleQuery.Builder baseQ = new EdgeRuleQuery.Builder(aNode, bNode);
        return edgeQueryWithBuilder(edgeLabels, aNode, bNode, baseQ);
    }

    public DslQueryBuilder edgeQuery(Edge edge, String aNode, String bNode) {
        List<String> edgeLabels = edge.getLabels().stream()
            .map(edgeLabel -> StringUtils.quote(edgeLabel.getLabel())).collect(Collectors.toList());
        EdgeRuleQuery.Builder baseQ = new EdgeRuleQuery.Builder(aNode, bNode);

        if ((AAIDirection.valueOf(edge.getDirection().name())) != AAIDirection.BOTH) {
            baseQ = baseQ.direction(AAIDirection.valueOf(edge.getDirection().name()));
        }
        return edgeQueryWithBuilder(edgeLabels, aNode, bNode, baseQ);
    }

    private DslQueryBuilder edgeQueryWithBuilder(List<String> edgeLabels, String aNode,
        String bNode, EdgeRuleQuery.Builder edgeBuilder) {
        // TODO : change this for fuzzy search.

        String edgeType = "";
        String edgeLabelsClause = "";
        String edgeTraversalClause = ".createEdgeTraversal(";

        if (!edgeLabels.isEmpty()) {
            edgeTraversalClause = ".createEdgeTraversalWithLabels(";
            edgeLabelsClause = String.join("", ", new ArrayList<>(Arrays.asList(",
                String.join(",", edgeLabels), "))");
        }
        LOGGER.debug("EdgeLabels Clause: {}", edgeLabelsClause);

        Multimap<String, EdgeRule> rules = ArrayListMultimap.create();
        try {
            if (edgeLabels.isEmpty()) {
                rules.putAll(edgeRules.getRules(edgeBuilder.build()));
            } else {
                edgeLabels.forEach(label -> {
                    try {
                        rules.putAll(edgeRules.getRules(edgeBuilder.label(label).build()));
                    } catch (EdgeRuleNotFoundException e) {
                        queryException
                            .append("Exception while finding the edge rule between the nodeTypes: ")
                            .append(aNode).append(", ").append(bNode).append(label);
                    }
                });

            }
        } catch (EdgeRuleNotFoundException e) {
            if (!edgeLabels.isEmpty()) {
                queryException.append("- No EdgeRule found for passed nodeTypes: ").append(aNode)
                    .append(", ").append(bNode).append(edgeLabels.stream().toString());
            } else {
                queryException.append("- No EdgeRule found for passed nodeTypes: ").append(aNode)
                    .append(", ").append(bNode);
            }
            return this;
        }

        if (rules.isEmpty() || rules.keys().isEmpty()) {
            queryException.append("- No EdgeRule found for passed nodeTypes: ").append(aNode)
                .append(", ").append(bNode);
        } else {
            if (edgeLabels.isEmpty()) {
                if (edgeRules.hasRule(edgeBuilder.edgeType(EdgeType.TREE).build())) {
                    edgeType = "EdgeType.TREE" + ",";
                }
                if (edgeRules.hasRule(edgeBuilder.edgeType(EdgeType.COUSIN).build())) {
                    if (edgeType.isEmpty()) {
                        edgeType = "EdgeType.COUSIN" + ",";
                    } else {
                        edgeType = "";
                    }
                }
            }
        }

        query.append(edgeTraversalClause).append(edgeType).append(" '").append(aNode).append("','")
            .append(bNode).append("'").append(edgeLabelsClause).append(")");

        return this;
    }

    public DslQueryBuilder where(boolean isNot) {
        query.append(".where(");
        if (isNot) {
            query.append("builder.newInstance().not(");
        }
        return this;
    }

    public DslQueryBuilder endWhere(boolean isNot) {
        query.append(")");
        if (isNot) {
            query.append(")");
        }
        return this;
    }

    public DslQueryBuilder limit(String limit) {
        query.append(".limit(").append(limit).append(")");
        return this;
    }

    public DslQueryBuilder filter(boolean isNot, String node, String key, List<String> values) {
        return this.filterPropertyStart(isNot, values).filterPropertyKeys(node, key, values)
            .filterPropertyEnd();
    }

    public DslQueryBuilder filterPropertyStart(boolean isNot, List<String> values) {
        if (isNot) {
            query.append(".getVerticesExcludeByProperty(");
        } else if (values != null && !values.isEmpty() && Boolean.parseBoolean(values.get(0))) {
            query.append(".getVerticesByBooleanProperty(");
        } else {
            query.append(".getVerticesByProperty(");
        }
        return this;
    }

    public DslQueryBuilder filterPropertyEnd() {
        query.append(")");
        return this;
    }

    public DslQueryBuilder validateFilter(String node, List<String> keys) {
        try {
            Introspector obj = loader.introspectorFromName(node);

            if (keys.isEmpty()) {
                queryException.append("No keys sent. Valid keys for ").append(node).append(" are ")
                    .append(String.join(",", obj.getIndexedProperties()));
                return this;
            }

        } catch (AAIUnknownObjectException e) {
            queryException.append("Unknown Object being referenced by the query").append(node);
        }
        return this;

    }

    private static final Pattern AAI_NODE_TYPE_PATTERN = Pattern.compile("aai-node-type");

    public DslQueryBuilder select(long selectCounter, List<String> keys) {
        /*
         * TODO : isNot should look at the vertex properties and include everything except the
         * notKeys
         */

        Matcher m = AAI_NODE_TYPE_PATTERN.matcher(query);
        int count = 0;
        while (m.find()) {
            count++;
        }

        if (selectCounter == count || keys == null) {
            String selectStep = "step" + selectCounter;
            // String keysArray = String.join(",", keys);
            query.append(".as('").append(selectStep).append("')").append(".as('stepMain').select(Pop.mixed,'")
                .append(selectStep).append("')");
        }
        return this;
    }

    public DslQueryBuilder filterPropertyKeys(String node, String key, List<String> values) {
        try {
            Introspector obj = loader.introspectorFromName(node);
            Optional<String> alias =
                obj.getPropertyMetadata(key.replace("'", ""), PropertyMetadata.DB_ALIAS);
            if (alias.isPresent()) {
                key = StringUtils.quote(alias.get());
            }
            String classType = obj.getType(trimSingleQuotes(key));
            query.append(key);

            if (values != null && !values.isEmpty()) {
                if (values.size() > 1) { // values.size() > 1 indicates possibility of a list
                    // eliminate quotes from each element
                    for (int i = 0; i < values.size(); i++) {
                        values.set(i, getConvertedValue(classType, key, values.get(i)));
                    }
                    String valuesArray = String.join(",", values);
                    query.append(",").append(" new ArrayList<>(Arrays.asList(").append(valuesArray)
                        .append("))");
                } else { // otherwise values should only contain one value
                    query.append(",").append(getConvertedValue(classType, key, values.get(0)));
                }
            }
        } catch (AAIUnknownObjectException e) {
            queryException.append("Unknown Object being referenced by the query").append(node);
        }
        return this;
    }

    private String getConvertedValue(String classType, String key, String value) {
        String convertedValue = value;
        if (isTypeSensitive(classType)) {
            convertedValue = trimSingleQuotes(value);
            try {
                // cast it to the corresponding type
                if (classType.equals(Integer.class.getName())) {
                    int castInt = Integer.parseInt(convertedValue);
                    convertedValue = String.valueOf(castInt);
                } else if (classType.equals(Long.class.getName())) {
                    long castLong = Long.parseLong(convertedValue);
                    convertedValue = String.valueOf(castLong);
                } else if (classType.equals(Boolean.class.getName())) {
                    if ("1".equals(convertedValue)) { // checking for integer true value
                        convertedValue = "true";
                    }
                    boolean castBoolean = Boolean.parseBoolean(convertedValue);
                    convertedValue = String.valueOf(castBoolean);
                }
            } catch (Exception e) {
                queryException.append("AAI_4020 ").append(String.format(
                    "Value [%s] is not an instance of the expected data type for property key [%s] and cannot be converted. "
                        + "Expected: class %s, found: class %s",
                    value, key, classType, String.class.getName()));
            }
        }
        return convertedValue;
    }

    private boolean isTypeSensitive(String classType) {
        if (classType.equals(Integer.class.getName()) || classType.equals(Boolean.class.getName())
            || classType.equals(Long.class.getName())) {
            return true;
        }
        return false;
    }

    private String trimSingleQuotes(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        String trimSingleQuotes = "";
        if (s.startsWith("'") && s.endsWith("'")) {
            trimSingleQuotes = s.substring(1, s.length() - 1);
        } else {
            trimSingleQuotes = s;
        }
        return trimSingleQuotes;
    }

    public DslQueryBuilder union() {
        query.append(".union(");
        return this;
    }

    public DslQueryBuilder endUnion() {
        query.append(")");
        return this;
    }

    public DslQueryBuilder store() {
        query.append(".store('x')");
        return this;
    }

    public DslQueryBuilder startInstance() {
        query.append("builder.newInstance()");
        return this;
    }

    public DslQueryBuilder endInstance() {
        return this;
    }

    public DslQueryBuilder comma() {
        query.append(",");
        return this;
    }

}
