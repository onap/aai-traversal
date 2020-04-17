package org.onap.aai.rest;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.SubgraphStrategy;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.db.props.AAIProperties;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.rest.db.HttpEntry;
import org.onap.aai.restcore.RESTAPI;
import org.onap.aai.serialization.engines.QueryStyle;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.serialization.queryformats.Format;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import java.util.concurrent.TimeUnit;

public abstract class TraversalConsumer extends RESTAPI {

    private static final String HISTORICAL_FORMAT = "state,lifecycle";
    private final boolean historyEnabled;
    private final int historyTruncateWindow;
    private final long currentTime = System.currentTimeMillis();
    private Long startTime = null;
    private Long endTime = null;
    private Long furthestInThePast = null;

    public TraversalConsumer() {
        this.historyTruncateWindow = Integer.parseInt(
                SpringContextAware.getApplicationContext().getEnvironment().getProperty("history.truncate.window.days", "365"));
        this.historyEnabled = Boolean.parseBoolean(
                SpringContextAware.getApplicationContext().getEnvironment().getProperty("history.enabled", "false"));
    }

    public boolean isHistory(Format queryFormat) {
        return isHistoryEnabled() && HISTORICAL_FORMAT.contains(queryFormat.toString());
    }

    public boolean isAggregate(Format queryFormat) {
        return Format.aggregate.equals(queryFormat);
    }

    public boolean isHistoryEnabled() {
        return historyEnabled;
    }

    protected SubgraphStrategy getSubgraphStrategy(long startTs, long endTs, Format format) {

        if (Format.state.equals(format)) {
            return getStateSubgraphStrategy(startTs);
        } else if (Format.lifecycle.equals(format)) {
            return getLifeCycleSubgraphStrategy(startTs, endTs);
        } else {
            return SubgraphStrategy.build()
                    .vertices(__.has(AAIProperties.START_TS, P.gte(startTs)))
                    .vertexProperties(__.has(AAIProperties.START_TS, P.gte(startTs)))
                    .edges(__.has(AAIProperties.START_TS, P.gte(startTs))).create();
        }
    }

    private SubgraphStrategy getLifeCycleSubgraphStrategy(long startTs, long endTs) {
        return SubgraphStrategy.build()
                .vertices(
                        __.not(
                                __.or(
                                        __.and(
                                                __.has(AAIProperties.START_TS, P.gt(startTs)),
                                                __.has(AAIProperties.START_TS, P.gt(endTs))
                                        ),
                                        __.and(
                                                __.has(AAIProperties.END_TS).has(AAIProperties.END_TS, P.lt(startTs)),
                                                __.has(AAIProperties.END_TS).has(AAIProperties.END_TS, P.lt(endTs))
                                        )
                                )
                        )
                ).vertexProperties(
                        __.not(
                                __.or(
                                        __.and(
                                                __.has(AAIProperties.START_TS, P.gt(startTs)),
                                                __.has(AAIProperties.START_TS, P.gt(endTs))
                                        ),
                                        __.and(
                                                __.has(AAIProperties.END_TS).has(AAIProperties.END_TS, P.lt(startTs)),
                                                __.has(AAIProperties.END_TS).has(AAIProperties.END_TS, P.lt(endTs))
                                        )
                                )
                        )
                ).edges(
                        __.not(
                                __.or(
                                        __.and(
                                                __.has(AAIProperties.START_TS, P.gt(startTs)),
                                                __.has(AAIProperties.START_TS, P.gt(endTs))
                                        ),
                                        __.and(
                                                __.has(AAIProperties.END_TS).has(AAIProperties.END_TS, P.lt(startTs)),
                                                __.has(AAIProperties.END_TS).has(AAIProperties.END_TS, P.lt(endTs))
                                        )
                                )
                        )
                ).create();
    }

    private SubgraphStrategy getStateSubgraphStrategy(long startTs) {
        return SubgraphStrategy.build()
                .vertices(
                        __.and(__.has(AAIProperties.START_TS, P.lte(startTs)),
                                __.or(__.hasNot(AAIProperties.END_TS), __.has(AAIProperties.END_TS, P.gt(startTs))))
                ).vertexProperties(
                        __.and(__.has(AAIProperties.START_TS, P.lte(startTs)),
                                __.or(__.hasNot(AAIProperties.END_TS), __.has(AAIProperties.END_TS, P.gt(startTs))))
                ).edges(
                        __.and(__.has(AAIProperties.START_TS, P.lte(startTs)),
                                __.or(__.hasNot(AAIProperties.END_TS), __.has(AAIProperties.END_TS, P.gt(startTs))))
                ).create();
    }



    protected GraphTraversalSource getTraversalSource(TransactionalGraphEngine dbEngine, Format format, UriInfo info) throws AAIException {
        if (isHistory(format)) {
            long startTime = this.getStartTime(format, info.getQueryParameters());
            long endTime = this.getEndTime(info.getQueryParameters());
            return dbEngine.asAdmin().getTraversalSource().withStrategies(getSubgraphStrategy(startTime, endTime, format));
        }
        return dbEngine.asAdmin().getTraversalSource();
    }

    protected void validateHistoryParams(Format format, MultivaluedMap<String, String> params) throws AAIException {
        getStartTime(format, params);
        getEndTime(params);
    }

    /**
     * If a request comes in for information prior to our truncation timeframe, throw an error.
     * In the changes api, we never return change timestamps prior to the truncation timeframe.
     * In the lifecycle api, we should treat a call with no timestamp as a lifecycle since call with a timestamp of the truncation time
     * in the lifecycle api, we should return an error if the timestamp provided is prior to the truncation time
     * In the state api, we should return an error if the timestamp provided is prior to the truncation time
     * @param params
     * @return
     */
    protected long getStartTime(Format format, MultivaluedMap<String, String> params) throws AAIException {

        if (startTime != null) {
            return startTime;
        }

        String startTs = params.getFirst("startTs") ;

        if (Format.state.equals(format)) {
            if (startTs == null || startTs.isEmpty() || "-1".equals(startTs) || "now".equals(startTs)) {
                startTime = currentTime;
            } else {
                startTime = Long.valueOf(startTs);
                verifyTimeAgainstTruncationTime(startTime);
            }
        } else if (Format.lifecycle.equals(format)) {
            if("now".equals(startTs)) {
                startTime = currentTime;
            } else if (startTs == null || startTs.isEmpty()|| "-1".equals(startTs)) {
                startTime = getFurthestInThePast();
            } else {
                startTime = Long.valueOf(startTs);
                verifyTimeAgainstTruncationTime(startTime);
            }
        }

        return startTime;

    }

    private void verifyTimeAgainstTruncationTime(long timestamp) throws AAIException {
        if (timestamp < getFurthestInThePast()) {
            throw new AAIException("AAI_4019");
        }
    }

    protected long getEndTime(MultivaluedMap<String, String> params) throws AAIException {
        if (endTime != null) {
            return endTime;
        }

        String endTs = params.getFirst("endTs") ;

        if (endTs == null || endTs.isEmpty() || "-1".equals(endTs) || "now".equals(endTs)) {
            endTime = currentTime;
        } else {
            endTime = Long.valueOf(endTs);
            verifyTimeAgainstTruncationTime(endTime);
        }

        return endTime;
    }

    protected Long getFurthestInThePast() {
        if (furthestInThePast == null) {
            furthestInThePast = currentTime - TimeUnit.DAYS.toMillis(historyTruncateWindow);
        }
        return furthestInThePast;
    }

    protected QueryStyle getQueryStyle(Format format, HttpEntry traversalUriHttpEntry) {
        if (isHistory(format)) {
            return QueryStyle.HISTORY_TRAVERSAL;
        }
        return traversalUriHttpEntry.getQueryStyle();
    }

}
