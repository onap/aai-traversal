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
package org.onap.aai.rest.dsl.v2;

import com.google.common.collect.Lists;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.onap.aai.dsl.v2.AAIDslBaseListener;
import org.onap.aai.dsl.v2.AAIDslParser;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.rest.dsl.DslQueryBuilder;
import org.onap.aai.rest.dsl.Edge;
import org.onap.aai.rest.dsl.EdgeLabel;
import org.onap.aai.rest.dsl.validation.DslValidator;
import org.onap.aai.rest.dsl.validation.DslValidatorRule;
import org.onap.aai.rest.enums.EdgeDirection;
import org.onap.aai.setup.SchemaVersions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class DslListener.
 */
public class DslListener extends AAIDslBaseListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DslListener.class.getName());

    private boolean validationFlag = false;
    private EdgeIngestor edgeIngestor;
    private Loader loader;
    private Optional<DslValidator> queryValidator = Optional.empty();
    private boolean hasReturnValue = false;

    private String validationRules = "none";

    private Deque<DslQueryBuilder> dslQueryBuilders = new LinkedList<>();
    private Deque<String> traversedNodes = new LinkedList<>();
    private Deque<List<String>> returnedNodes = new LinkedList<>();

    private Deque<Edge> traversedEdges = new ArrayDeque<>();
    private long selectCounter = 0;

    /*
     * Additional datastructures to store all nodeCount & looped edges
     */
    int nodeCount = 0;
    private List<String> traversedEdgesOld = new LinkedList<>();

    private Map<String, List<String>> selectKeys = new HashMap<String, List<String>>();
    private boolean isAggregate;

    public Map<String, List<String>> getSelectKeys() {
        return selectKeys;
    }

    public void setSelectKeys(String nodeType, List<String> properties) {
        this.selectKeys.put(nodeType, properties);
    }

    /**
     * Instantiates a new DslListener.
     */
    public DslListener(EdgeIngestor edgeIngestor, SchemaVersions schemaVersions,
        LoaderFactory loaderFactory) {
        this.loader = loaderFactory.createLoaderForVersion(ModelType.MOXY,
            schemaVersions.getDefaultVersion());
        this.edgeIngestor = edgeIngestor;
    }

    public DslQueryBuilder builder() {
        return dslQueryBuilders.peekFirst();
    }

    public String getQuery() throws AAIException {
        // TODO Change the exception reporting
        if (!getException().isEmpty()) {
            AAIException aaiException = new AAIException("AAI_6149", getException());
            ErrorLogHelper.logException(aaiException);
            throw aaiException;
        }

        DslValidatorRule ruleValidator =
            new DslValidatorRule.Builder().loop(getValidationRules(), traversedEdgesOld)
                .nodeCount(getValidationRules(), nodeCount).build();
        if (queryValidator.isPresent() && !queryValidator.get().validate(ruleValidator)) {
            AAIException aaiException = new AAIException("AAI_6151",
                "Validation error " + queryValidator.get().getErrorMessage());
            ErrorLogHelper.logException(aaiException);
            throw aaiException;
        }
        return this.compile();
    }

    public String compile() {
        List<String> queries = dslQueryBuilders.stream().map(dslQb -> dslQb.getQuery().toString())
            .collect(Collectors.toList());
        return String.join("", Lists.reverse(queries));
    }

    public String getException() {
        List<String> exceptions = dslQueryBuilders.stream()
            .map(dslQb -> dslQb.getQueryException().toString()).collect(Collectors.toList());
        return String.join("", Lists.reverse(exceptions));
    }

    @Override
    public void enterAaiquery(AAIDslParser.AaiqueryContext ctx) {
        dslQueryBuilders.push(new DslQueryBuilder(edgeIngestor, loader));
    }

    @Override
    public void exitAaiquery(AAIDslParser.AaiqueryContext ctx) {
        if (!hasReturnValue) {
            throw new RuntimeException(new AAIException("AAI_6149", "No nodes marked for output"));
        }
    }

    @Override
    public void enterStartStatement(AAIDslParser.StartStatementContext ctx) {
        builder().start();
    }

    @Override
    public void exitStartStatement(AAIDslParser.StartStatementContext ctx) {
        builder().end(selectCounter);
        if (!traversedNodes.isEmpty()) {
            traversedNodes.removeFirst();
        }

    }

    @Override
    public void exitLimit(AAIDslParser.LimitContext ctx) {
        builder().limit(ctx.num().getText());
    }

    @Override
    public void enterNestedStatement(AAIDslParser.NestedStatementContext ctx) {
        dslQueryBuilders.addFirst(new DslQueryBuilder(edgeIngestor, loader));
        builder().startInstance();
    }

    @Override
    public void exitNestedStatement(AAIDslParser.NestedStatementContext ctx) {
        int count = 1;
        if (!ctx.traversal().isEmpty()) {
            count = ctx.traversal().size();
        }
        String resultNode = traversedNodes.peekFirst();

        if (!traversedNodes.isEmpty()) {
            Stream<Integer> integers = Stream.iterate(0, i -> i + 1);
            integers.limit(count).forEach(i -> traversedNodes.removeFirst());
        }
        List<String> resultNodes = returnedNodes.pop();
        resultNodes.add(resultNode);
        returnedNodes.addFirst(resultNodes);
    }

    @Override
    public void enterComma(AAIDslParser.CommaContext ctx) {
        builder().comma();
    }

    @Override
    public void enterVertex(AAIDslParser.VertexContext ctx) {

        if (!traversedEdges.isEmpty() && !traversedNodes.isEmpty()) {
            builder().edgeQuery(traversedEdges.peekFirst(), traversedNodes.peekFirst(),
                ctx.label().getText());
            traversedEdgesOld.add(traversedNodes.peekFirst() + ctx.label().getText());
        } else {
            builder().nodeQuery(ctx.label().getText());
        }

        traversedNodes.addFirst(ctx.label().getText());
    }

    @Override
    public void exitVertex(AAIDslParser.VertexContext ctx) {

        /* TODO dont use context */
        if (ctx.getParent() instanceof AAIDslParser.StartStatementContext && isValidationFlag()) {
            List<String> allKeys = new ArrayList<>();

            if (ctx.filter() != null) {
                allKeys = ctx
                    .filter().propertyFilter().stream().flatMap(pf -> pf.key().stream()).map(e -> e
                        .getText().replaceFirst("\'", "").substring(0, e.getText().length() - 2))
                    .collect(Collectors.toList());
            }
            builder().validateFilter(ctx.label().getText(), allKeys);
        }
        if (ctx.store() != null) {
            if (isAggregate()) {
                builder().select(selectCounter++, null);
            }
            builder().store();
            hasReturnValue = true;
        }
        nodeCount++;
    }

    @Override
    public void enterUnionVertex(AAIDslParser.UnionVertexContext ctx) {
        returnedNodes.addFirst(new ArrayList<>());
        builder().union();
    }

    @Override
    public void exitUnionVertex(AAIDslParser.UnionVertexContext ctx) {
        String resultNode = returnedNodes.pop().get(0);
        traversedNodes.addFirst(resultNode);
        builder().endUnion();
        if (ctx.store() != null) {
            builder().store();
            hasReturnValue = true;
        }
    }

    @Override
    public void enterWhereFilter(AAIDslParser.WhereFilterContext ctx) {
        boolean isNot = ctx.not() != null && !ctx.not().isEmpty();
        returnedNodes.addFirst(new ArrayList<>());
        builder().where(isNot);
    }

    @Override
    public void exitWhereFilter(AAIDslParser.WhereFilterContext ctx) {
        if (!returnedNodes.isEmpty()) {
            returnedNodes.pop();
        }
        boolean isNot = ctx.not() != null && !ctx.not().isEmpty();
        builder().endWhere(isNot);
    }

    @Override
    public void enterEdge(AAIDslParser.EdgeContext ctx) {
        List<EdgeLabel> labels = new ArrayList<>();

        if (ctx.edgeFilter() != null) {
            labels.addAll(ctx.edgeFilter().key().stream()
                .map(e -> new EdgeLabel(removeSingleQuotes(e.getText()), true))
                .collect(Collectors.toList()));
        }
        EdgeDirection direction = EdgeDirection.BOTH;
        if (ctx.DIRTRAVERSE() != null) {
            direction = EdgeDirection.fromValue(ctx.DIRTRAVERSE().getText());
        }
        traversedEdges.addFirst(new Edge(direction, labels));
    }

    protected String removeSingleQuotes(String value) {
        return value.replaceFirst("^'(.*)'$", "$1");
    }

    @Override
    public void enterPropertyFilter(AAIDslParser.PropertyFilterContext ctx) {

        List<AAIDslParser.KeyContext> valueList = ctx.key();
        String filterKey = valueList.get(0).getText();

        boolean isNot = ctx.not() != null && !ctx.not().isEmpty();
        List<AAIDslParser.NumContext> numberValues = ctx.num();

        List<AAIDslParser.BoolContext> booleanValues = ctx.bool();

        /*
         * Add all String values
         */
        List<String> values = valueList.stream().filter(value -> !filterKey.equals(value.getText()))
            .map(value -> value.getText()).collect(Collectors.toList());
        /*
         * Add all numeric values
         */
        values.addAll(numberValues.stream().filter(value -> !filterKey.equals(value.getText()))
            .map(value -> value.getText()).collect(Collectors.toList()));

        /*
         * Add all boolean values
         */
        values.addAll(booleanValues.stream().filter(value -> !filterKey.equals(value.getText()))
            .map(value -> value.getText().toLowerCase()).collect(Collectors.toList()));

        builder().filter(isNot, traversedNodes.peekFirst(), filterKey, values);

    }

    @Override
    public void enterSelectFilter(AAIDslParser.SelectFilterContext ctx) {

        List<AAIDslParser.KeyContext> keyList = ctx.key();

        /*
         * Add all String values
         */
        List<String> allKeys =
            keyList.stream().map(keyValue -> "'" + keyValue.getText().replace("'", "") + "'")
                .collect(Collectors.toList());
        if (allKeys != null && !allKeys.isEmpty()) {
            setSelectKeys(traversedNodes.getFirst(), allKeys);
        }
        if (isAggregate() && (traversedNodes.size() == nodeCount)) {
            builder().select(selectCounter++, allKeys);
        }
    }

    public boolean isValidationFlag() {
        return validationFlag;
    }

    public void setValidationFlag(boolean validationFlag) {
        this.validationFlag = validationFlag;
    }

    public void setAggregateFlag(boolean isAggregate) {
        this.isAggregate = isAggregate;
    }

    public boolean isAggregate() {
        return this.isAggregate;
    }

    public void setQueryValidator(DslValidator queryValidator, String validationRules) {
        this.queryValidator = Optional.of(queryValidator);
        this.validationRules = validationRules;
    }

    public String getValidationRules() {
        return validationRules;
    }

}
