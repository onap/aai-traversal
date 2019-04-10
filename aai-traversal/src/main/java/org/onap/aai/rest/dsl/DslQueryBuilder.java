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

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.EdgeRule;
import org.onap.aai.edges.EdgeRuleQuery;
import org.onap.aai.edges.enums.EdgeType;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.exceptions.AAIUnknownObjectException;
import org.onap.aai.schema.enums.PropertyMetadata;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DslQueryBuilder {

    private final EdgeIngestor edgeRules;
    private final Loader loader;
    private StringBuilder query;
    private StringBuilder queryException;

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
    public DslQueryBuilder end() {
        query.append(".cap('x').unfold().dedup()");
        return this;
    }

    public DslQueryBuilder nodeQuery(String node) {
        query.append(".getVerticesByProperty('aai-node-type', '").append(node).append("')");
        return this;
    }

    public DslQueryBuilder edgeQuery(List<String> edgeLabels, String aNode, String bNode) {
        //TODO : change this for fuzzy search.

        String edgeType = "";
        String edgeLabelsClause = "";
        String edgeTraversalClause = ".createEdgeTraversal(";


        if (!edgeLabels.isEmpty()) {
            edgeTraversalClause = ".createEdgeTraversalWithLabels(";
            edgeLabelsClause = String.join("", ", new ArrayList<>(Arrays.asList(", Joiner.on(",").join(edgeLabels), "))");
        }

        EdgeRuleQuery.Builder baseQ = new EdgeRuleQuery.Builder(aNode, bNode);
        Multimap<String, EdgeRule> rules = ArrayListMultimap.create();
        try {
            //TODO chnage this - ugly
            if (edgeLabels.isEmpty()) {
                rules.putAll(edgeRules.getRules(baseQ.build()));
            } else {
                edgeLabels.stream().forEach(label -> {
                    try {
                        rules.putAll(edgeRules.getRules(baseQ.label(label).build()));
                    } catch (EdgeRuleNotFoundException e) {
                        queryException.append("AAI_6120" + "No EdgeRule found for passed nodeTypes: " + aNode
                                + ", " + bNode + label);

                    }
                });

            }
        } catch (EdgeRuleNotFoundException e) {
            if (!edgeLabels.isEmpty()) {
                queryException.append("AAI_6120" + "No EdgeRule found for passed nodeTypes: " + aNode
                        + ", " + bNode + edgeLabels.stream().toString());
            }
            else {
                queryException.append("AAI_6120" + "No EdgeRule found for passed nodeTypes: " + aNode
                        + ", " + bNode);
            }
            return this;
        }

        if (rules.isEmpty() || rules.keys().isEmpty()) {
            queryException.append("AAI_6120" + "No EdgeRule found for passed nodeTypes: " + aNode
                    + ", " + bNode);
        } else {
            if (edgeLabels.isEmpty()) {
                if (edgeRules.hasRule(baseQ.edgeType(EdgeType.TREE).build())) {
                    edgeType = "EdgeType.TREE" + ",";
                }
                if (edgeRules.hasRule(baseQ.edgeType(EdgeType.COUSIN).build())) {
                    if (edgeType.isEmpty()) {
                        edgeType = "EdgeType.COUSIN" + ",";
                    } else {
                        edgeType = "";
                    }
                }
            }
        }

        query.append(edgeTraversalClause).append(edgeType).append(" '").append(aNode)
                .append("','").append(bNode).append("'").append(edgeLabelsClause).append(")");

        return this;
    }


    public DslQueryBuilder where() {
        query.append(".where(");
        return this;
    }

    public DslQueryBuilder endWhere() {
        query.append(")");
        return this;
    }

    public DslQueryBuilder limit(String limit) {
        query.append(".limit(").append(limit).append(")");
        return this;
    }

    public DslQueryBuilder filter(boolean isNot, String node, String key, List<String> values) {
        return this.filterPropertyStart(isNot).filterPropertyKeys(node, key, values).filterPropertyEnd();
    }

    public DslQueryBuilder filterPropertyStart(boolean isNot) {
        if (isNot) {
            query.append(".getVerticesExcludeByProperty(");
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
                queryException.append("No keys sent. Valid keys for " + node + " are "
                        + String.join(",", obj.getIndexedProperties()));
                return this;
            }

            boolean notIndexed = keys.stream()
                    .filter(prop -> obj.getIndexedProperties().contains(prop)).collect(Collectors.toList()).isEmpty();

            if (notIndexed) {
                queryException.append("Non indexed keys sent. Valid keys for " + node + " "
                        + String.join(",", obj.getIndexedProperties()));
            }
        } catch (AAIUnknownObjectException e) {
            queryException.append("Unknown Object being referenced by the query" + node);
        }
        return this;

    }

    public DslQueryBuilder filterPropertyKeys(String node, String key, List<String> values) {
        try {
            Introspector obj = loader.introspectorFromName(node);

            Optional<String> alias = obj.getPropertyMetadata(key, PropertyMetadata.DB_ALIAS);
            if (alias.isPresent()) {
                key = alias.get();
            }
            query.append(key);

            if (!values.isEmpty()) {
                if (values.size() > 1) {
                    String valuesArray = String.join(",", values);
                    query.append(",").append(" new ArrayList<>(Arrays.asList(" + valuesArray + "))");
                } else {
                    query.append(",").append(values.get(0));
                }
            }
        } catch (AAIUnknownObjectException e) {
            queryException.append("Unknown Object being referenced by the query" + node);
        }
        return this;
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
