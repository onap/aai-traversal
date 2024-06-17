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
package org.onap.aai.dbgraphgen;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.onap.aai.concurrent.AaiCallable;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.db.DbMethHelper;
import org.onap.aai.db.props.AAIProperties;
import org.onap.aai.dbgen.PropertyLimitDesc;
import org.onap.aai.edges.EdgeIngestor;
import org.onap.aai.edges.EdgeRuleQuery;
import org.onap.aai.edges.enums.EdgeType;
import org.onap.aai.exceptions.AAIException;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.exceptions.AAIUnknownObjectException;
import org.onap.aai.logging.ErrorLogHelper;
import org.onap.aai.query.builder.QueryBuilder;
import org.onap.aai.schema.enums.PropertyMetadata;
import org.onap.aai.serialization.db.DBSerializer;
import org.onap.aai.serialization.engines.TransactionalGraphEngine;
import org.onap.aai.util.AAIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that uses Model/Named-Query definitions to navigate the graph.
 */
public class ModelBasedProcessing {

    private static Logger logger = LoggerFactory.getLogger(ModelBasedProcessing.class);
    private static final int MAX_LEVELS = 50; // max depth allowed for our model - to protect
                                              // against infinite loop problems

    private TransactionalGraphEngine engine;
    private Loader loader;
    private DBSerializer serializer;
    private DbMethHelper dbMethHelper;

    protected ModelBasedProcessing() {

    }

    public ModelBasedProcessing(Loader loader, TransactionalGraphEngine engine,
        DBSerializer serializer) {
        this.loader = loader;
        this.engine = engine;
        this.serializer = serializer;
        dbMethHelper = new DbMethHelper(loader, engine);
    }

    /**
     * Gets the start nodes and model-ver's.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param passedModelVersionId the passed model-version-id -- optional (unique id for a
     *        model-ver)
     * @param passedModelInvId the passed model-invariant-id -- optional
     * @param passedModelName the passed model-name -- optional
     * @param passedTopNodeType the passed top node type -- optional (needed if neither
     *        model=invariant-id nor model-version-id is passed)
     * @param startNodeFilterArrayOfHashes the start node filter array of hashes -- optional (used
     *        to locate the first node(s) of instance data)
     * @param apiVer the api ver
     * @return HashMap of startNodes and their corresponding model-version-id's
     * @throws AAIException the AAI exception
     */
    public Map<String, String> getStartNodesAndModVersionIds(String transId, String fromAppId,
        String passedModelVersionId, String passedModelInvId, String passedModelName,
        String passedTopNodeType, List<Map<String, Object>> startNodeFilterArrayOfHashes,
        String apiVer) throws AAIException {
        // ----------------------------------------------------------------------------------------------------
        // Get a hash for all start-nodes (key = vtxId, val = modelVersionId that applies)
        // If no start-node-key info is passed, then use either the passed modelVersion or
        // the passed model-invariant-id or model-name to collect them.
        // If start-node-key info is given, use it instead to look for start-nodes.
        // Note: if ONLY start-node-key info is given, then it would have to map to nodes which
        // have persona data. Otherwise we'd have no way to know what model to collect data with.
        // ----------------------------------------------------------------------------------------------------

        Iterator<Vertex> startVerts = null;
        Map<String, String> startVertInfo = new HashMap<>();

        if (startNodeFilterArrayOfHashes.isEmpty()) {
            // Since they did not give any data to find start instances, we will have to find them
            // using whatever model-info they provided so we can use it to map to persona-data in
            // the db.
            if ((passedModelVersionId == null || passedModelVersionId.equals(""))
                && (passedModelInvId == null || passedModelInvId.equals(""))
                && (passedModelName == null || passedModelName.equals(""))) {
                throw new AAIException("AAI_6118",
                    "ModelInvariantId or ModelName or ModelVersionId required if no startNodeFilter data passed.");
            } else {
                // Use whatever model info they pass to find start-node instances
                // Get the first/top named-query-element used by this query
                if (passedModelVersionId != null && !passedModelVersionId.equals("")) {
                    // Need to look up the model-invariant-id and model-version to check against
                    // persona data
                    Vertex modVerVtx = getNodeUsingUniqueId(transId, fromAppId, "model-ver",
                        "model-version-id", passedModelVersionId);
                    Vertex modVtx = getModelGivenModelVer(modVerVtx, "");
                    String calcModId = modVtx.<String>property("model-invariant-id").orElse(null);
                    // Now we can look up instances that match this model's info
                    if (calcModId != null) {
                        startVerts = this.engine.asAdmin().getReadOnlyTraversalSource().V()
                            .has(addDBAliasedSuffix("model-invariant-id"), calcModId)
                            .has(addDBAliasedSuffix("model-version-id"), passedModelVersionId);
                    }
                } else if (passedModelInvId != null && !passedModelInvId.equals("")) {
                    // They gave us the model-invariant-id
                    startVerts = this.engine.asAdmin().getReadOnlyTraversalSource().V()
                        .has(addDBAliasedSuffix("model-invariant-id"), passedModelInvId);
                } else if (passedModelName != null && !passedModelName.equals("")) {
                    List<Vertex> modelVerVtxList =
                        getModelVersUsingName(transId, fromAppId, passedModelName);
                    List<Vertex> startVtxList = new ArrayList<>();
                    // Need to look up the model-inv-ids and model-versions to check against persona
                    // data
                    if (!modelVerVtxList.isEmpty()) {
                        for (int i = 0; i < modelVerVtxList.size(); i++) {
                            String calcModVerId = (modelVerVtxList.get(i))
                                .<String>property("model-version-id").orElse(null);
                            Vertex modVtx = getModelGivenModelVer(modelVerVtxList.get(i), "");
                            String calcModInvId =
                                modVtx.<String>property("model-invariant-id").orElse(null);
                            // Now we can look up instances that match this model's info
                            Iterator<Vertex> tmpStartIter =
                                this.engine.asAdmin().getReadOnlyTraversalSource().V()
                                    .has(addDBAliasedSuffix("model-invariant-id"), calcModInvId)
                                    .has(addDBAliasedSuffix("model-version-id"), calcModVerId);
                            while (tmpStartIter.hasNext()) {
                                Vertex tmpStartVert = tmpStartIter.next();
                                startVtxList.add(tmpStartVert);
                            }
                        }
                    }
                    if (!startVtxList.isEmpty()) {
                        startVerts = startVtxList.iterator();
                    }
                }
            }

            if (startVerts != null) {
                while (startVerts.hasNext()) {
                    Vertex tmpStartVert = startVerts.next();
                    String vid = tmpStartVert.id().toString();
                    // String tmpModId =
                    // tmpStartVert.<String>property(addDBAliasedSuffix("model-invariant-id")).orElse(null);
                    String tmpModVerId = tmpStartVert
                        .<String>property(addDBAliasedSuffix("model-version-id")).orElse(null);
                    startVertInfo.put(vid, tmpModVerId);
                }
            }
            if (startVertInfo.isEmpty()) {
                throw new AAIException("AAI_6114",
                    "Start Node(s) could not be found for model data passed.  "
                        + "(modelVersionId = [" + passedModelVersionId + "], modelInvariantId = ["
                        + passedModelInvId + "], modelName = [" + passedModelName + "])");
            }

            return startVertInfo;
        } else {
            // Use start-node filter info to find start-node(s) - Note - there could also be model
            // info passed that we'll need
            // to use to trim down the set of start-nodes that we find based on the startNodeFilter
            // data.
            String modTopNodeType = "";
            String modInfoStr = "";
            if (passedModelVersionId != null && !passedModelVersionId.equals("")) {
                modTopNodeType =
                    getModelVerTopWidgetType(transId, fromAppId, passedModelVersionId, "", "");
                modInfoStr = "modelVersionId = (" + passedModelVersionId + ")";
            } else if (passedModelInvId != null && !passedModelInvId.equals("")) {
                modTopNodeType =
                    getModelVerTopWidgetType(transId, fromAppId, "", passedModelInvId, "");
                modInfoStr = "modelId = (" + passedModelInvId + ")";
            } else if (passedModelName != null && !passedModelName.equals("")) {
                modTopNodeType =
                    getModelVerTopWidgetType(transId, fromAppId, "", "", passedModelName);
                modInfoStr = "modelName = (" + passedModelName + ")";
            }

            if (modTopNodeType.equals("")) {
                if ((passedTopNodeType == null) || passedTopNodeType.equals("")) {
                    String msg =
                        "Could not determine the top-node nodeType for this request. modelInfo: ["
                            + modInfoStr + "]";
                    throw new AAIException("AAI_6118", msg);
                } else {
                    // We couldn't find a top-model-type based on passed in model info, but they
                    // gave us a type to use -- so use it.
                    modTopNodeType = passedTopNodeType;
                }
            } else {
                // we did get a topNode type based on model info - make sure it doesn't contradict
                // the passsed-in one (if there is one)
                if (passedTopNodeType != null && !passedTopNodeType.equals("")
                    && !passedTopNodeType.equals(modTopNodeType)) {
                    throw new AAIException("AAI_6120",
                        "topNodeType passed in [" + passedTopNodeType
                            + "] does not match nodeType derived for model info passed in: ["
                            + modTopNodeType + "]");
                }
            }

            List<String> modelVersionIds2Check = new ArrayList<>();
            if ((passedModelName != null && !passedModelName.equals(""))) {
                // They passed a modelName, so find all the model UUIDs (model-version-id's) that
                // map to this
                modelVersionIds2Check =
                    getModelVerIdsUsingName(transId, fromAppId, passedModelName);
            }
            if ((passedModelVersionId != null && !passedModelVersionId.equals(""))) {
                // They passed in a modelNameVersionId
                if (modelVersionIds2Check.isEmpty()) {
                    // There was no modelName passed, so we can use the passed modelNameVersionId
                    modelVersionIds2Check.add(passedModelVersionId);
                } else if (modelVersionIds2Check.contains(passedModelVersionId)) {
                    // The passed in uuid does not conflict with what we got using the passed-in
                    // modelName.
                    // We'll just use the passed in uuid in this case.
                    // Hopefully they would not be passing strange combinations like this, but we'll
                    // try to deal with it.
                    modelVersionIds2Check = new ArrayList<>(); // Clear out what we had
                    modelVersionIds2Check.add(passedModelVersionId);
                }
            }

            // We should now be OK with our topNodeType for this request, so we can look for the
            // actual startNodes
            for (int i = 0; i < startNodeFilterArrayOfHashes.size(); i++) {
                // Locate the starting node which will be used to look which corresponds to this set
                // of filter data
                Vertex startVtx = null;
                try {
                    Optional<Vertex> result = dbMethHelper.searchVertexByIdentityMap(modTopNodeType,
                        startNodeFilterArrayOfHashes.get(i));
                    if (!result.isPresent()) {
                        throw new AAIException("AAI_6114",
                            "No Node of type " + modTopNodeType + " found for properties");
                    }
                    startVtx = result.get();
                } catch (AAIException e) {
                    String msg = "Could not find startNode of type = [" + modTopNodeType
                        + "], given these params: " + startNodeFilterArrayOfHashes.get(i)
                        + ". msg # from getUniqueNode() = " + e.getMessage();
                    throw new AAIException("AAI_6114", msg);
                }

                String vid = startVtx.id().toString();
                String personaModInvId = startVtx
                    .<String>property(addDBAliasedSuffix("model-invariant-id")).orElse(null);
                String personaModVerId =
                    startVtx.<String>property(addDBAliasedSuffix("model-version-id")).orElse(null);

                // Either this start-node has persona info (which should not contradict any
                // passed-in model info)
                // or they should have passed in the model to use - so we'd just use that.
                if (personaModVerId != null && !personaModVerId.equals("")) {
                    // There is persona data in this start-node. So make sure it doesn't contradict
                    // any "passed" stuff
                    if (modelVersionIds2Check.isEmpty()
                        && (passedModelInvId == null || passedModelInvId.equals(""))) {
                        // They didn't pass any model info, so use the persona one.
                        startVertInfo.put(vid, personaModVerId);
                    } else if (modelVersionIds2Check.isEmpty()
                        && (passedModelInvId != null && !passedModelInvId.equals(""))) {
                        // They passed in just the modelId - so check it
                        if (passedModelInvId.equals(personaModInvId)) {
                            startVertInfo.put(vid, personaModVerId);
                        }
                    } else if (!modelVersionIds2Check.isEmpty()
                        && (passedModelInvId == null || passedModelInvId.equals(""))) {
                        // They passed in just modelVersionId - so check
                        if (modelVersionIds2Check.contains(personaModVerId)) {
                            startVertInfo.put(vid, personaModVerId);
                        }
                    } else if (!modelVersionIds2Check.isEmpty()
                        && (passedModelInvId != null && !passedModelInvId.equals(""))) {
                        // We have BOTH a modelVersionIds and a modelId to check
                        if (passedModelInvId.equals(personaModInvId)
                            && modelVersionIds2Check.contains(personaModVerId)) {
                            startVertInfo.put(vid, personaModVerId);
                        }
                    }
                } else {
                    // This start node did not have persona info -- so we will use the passed in
                    // model info if they passed one
                    if (passedModelVersionId != null && !passedModelVersionId.equals("")) {
                        // The model-version-id uniquely identifies a model-ver, so we can use it.
                        startVertInfo.put(vid, passedModelVersionId);
                    } else {
                        throw new AAIException("AAI_6118",
                            "Found startNode but since it does not have persona data, the "
                                + " model-version-id is required. ");
                    }
                }
            }
        }

        return startVertInfo;

    }// end of getStartNodesAndModVersionIds()

    /**
     * Query by model. (really model-ver)
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param modelVersionId the model-version-id (unique id in model-ver)
     * @param modelInvariantId the model-invariant-id (unique id in model)
     * @param modelName the model name
     * @param topNodeType - optional (needed if neither model-invariant-id nor model-version-id is
     *        passed)
     * @param startNodeFilterArrayOfHashes the start node filter array of hashes -- optional (used
     *        to locate the first node(s) of instance data)
     * @param apiVer the api ver
     * @return resultSet
     * @throws AAIException the AAI exception
     */
    public List<ResultSet> queryByModel(String transId, String fromAppId, String modelVersionId,
        String modelInvariantId, String modelName, String topNodeType,
        List<Map<String, Object>> startNodeFilterArrayOfHashes, String apiVer) throws AAIException {

        final String transId_f = transId;
        final String fromAppId_f = fromAppId;
        final String modelVersionId_f = modelVersionId;
        final String modelInvId_f = modelInvariantId;
        final String modelName_f = modelName;
        final String topNodeType_f = topNodeType;
        final List<Map<String, Object>> startNodeFilterArrayOfHashes_f =
            startNodeFilterArrayOfHashes;
        final String apiVer_f = apiVer;

        // Find out what our time-limit should be
        int timeLimitSec = 0;
        String timeLimitString = AAIConfig.get("aai.model.query.timeout.sec");
        if (timeLimitString != null && !timeLimitString.equals("")) {
            try {
                timeLimitSec = Integer.parseInt(timeLimitString);
            } catch (Exception nfe) {
                // Don't worry, we will leave the limit as zero - which tells us not to use it.
            }
        }

        if (timeLimitSec <= 0) {
            // We will NOT be using a timer
            return queryByModel_Timed(transId, fromAppId, modelVersionId, modelInvariantId,
                modelName, topNodeType, startNodeFilterArrayOfHashes, apiVer);
        }

        List<ResultSet> resultList = new ArrayList<>();
        TimeLimiter limiter = new SimpleTimeLimiter();
        try {

            resultList = limiter.callWithTimeout(new AaiCallable<List<ResultSet>>() {
                public List<ResultSet> process() throws AAIException {
                    return queryByModel_Timed(transId_f, fromAppId_f, modelVersionId_f,
                        modelInvId_f, modelName_f, topNodeType_f, startNodeFilterArrayOfHashes_f,
                        apiVer_f);
                }
            }, timeLimitSec, TimeUnit.SECONDS, true);
        } catch (AAIException ae) {
            // Re-throw AAIException so we get can tell what happened internally
            throw ae;
        } catch (UncheckedTimeoutException ute) {
            throw new AAIException("AAI_6140",
                "Query Processing Limit exceeded. (limit = " + timeLimitSec + " seconds)");
        } catch (Exception e) {
            throw new AAIException("AAI_6128",
                "Unexpected exception in queryByModel(): " + e.getMessage());
        }

        return resultList;
    }

    /**
     * Query by model (model-ver) timed.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param modelVersionId the model-version-id (unique id in model-ver)
     * @param modelInvariantId the model-invariant-id (unique id in model)
     * @param modelName the model name
     * @param topNodeType the top node type
     * @param startNodeFilterArrayOfHashesVal the start node filter array of hashes
     * @param apiVer the api ver
     * @return the array list
     * @throws AAIException the AAI exception
     */
    public List<ResultSet> queryByModel_Timed(String transId, String fromAppId,
        String modelVersionId, String modelInvariantId, String modelName, String topNodeType,
        List<Map<String, Object>> startNodeFilterArrayOfHashesVal, String apiVer)
        throws AAIException {

        List<ResultSet> resultArray = new ArrayList<>();

        // NOTE: this method can be used for different styles of queries:
        // a) They could pass neither a modelVersionId or a modelInvariantId but just pass a set of
        // data defining start-nodes.
        // Note - with no model info, we need them to pass the startNodeType for us to be able to
        // use the
        // start-node-filter data. We would look at each start node and ensure that each has
        // persona-model info.
        // Then use whatever model corresponds to each instance to pull that instance's data.
        // b) They could pass a modelInvariantId, but no modelVersionId and no startNode info. In
        // this case, we
        // Would look in the database for all nodes that have a model-invariant-id-local that
        // matches what was
        // passed, and then for each of those instances, pull the data based on the corresponding
        // model.
        // c) They could pass a model-version-id, but no startNode info. We'd make sure that if a
        // model-invariant-id was also passed, that it does not conflict - but it really should be
        // null if they
        // are passing a full model-version-id. Like case -b-, we'd do a query for all nodes
        // that have persona info that corresponds to the model-version-id passed and then
        // collect data for each one.
        // d) They could pass either modelVersionId or modelInvariantId AND startNodeFilter info. In
        // this case we
        // would look at the model info to figure out what the top-node-type is, then look at the
        // top-node instances based on the startNodeFilter. We'd only collect data for each instance
        // if
        // it's persona model info matches what was passed in.

        // Sorry to do this, but code that gets called with an empty hash as the first array element
        // was causing errors
        List<Map<String, Object>> startNodeFilterArrayOfHashes = new ArrayList<>();
        if (!startNodeFilterArrayOfHashesVal.isEmpty()) {
            Map<String, Object> tmpH = startNodeFilterArrayOfHashesVal.get(0);
            if (!tmpH.isEmpty()) {
                for (int i = 0; i < startNodeFilterArrayOfHashesVal.size(); i++) {
                    startNodeFilterArrayOfHashes.add(startNodeFilterArrayOfHashesVal.get(i));
                }
            }
        }

        // ----------------------------------------------------------------------------------------------------------
        // Get a Hash of all the start-nodes (top instance-data node for a model-ver where we will
        // start collecting data) for startNode2ModelVerHash:
        // key = vertex-id for the startNode,
        // value = model-version-id for the corresponding model-ver
        // ----------------------------------------------------------------------------------------------------------
        Map<String, String> startNode2ModelVerHash =
            getStartNodesAndModVersionIds(transId, fromAppId, modelVersionId, modelInvariantId,
                modelName, topNodeType, startNodeFilterArrayOfHashes, apiVer);

        // System.out.println("\nDEBUG -- Here's a dump of the startnodes/model-vers: " +
        // startNode2ModelVerHash.toString());

        // --------------------------------------------------------------------------------------------------------
        // Figure out what-all models (model-ver nodes) we will be dealing with
        // Note - Instances must all use the same type of start-node, but do not have to all use the
        // same model-ver.
        // --------------------------------------------------------------------------------------------------------
        Map<String, Vertex> distinctModelVersHash = new HashMap<>();
        // For distinctModelVersHash: key = modelVersionId, val= modelVerVertex
        String startNodeType = "";
        if (topNodeType != null && !topNodeType.equals("")) {
            startNodeType = topNodeType;
        }

        List<String> skipModelVerIdList = new ArrayList<>();
        List<String> skipStartVertVerIdList = new ArrayList<>();
        Set<String> snKeySet = startNode2ModelVerHash.keySet();
        Iterator<String> startNodeIterator = snKeySet.iterator();
        while (startNodeIterator.hasNext()) {
            String modVerIdKey = startNodeIterator.next();
            String modVerId = startNode2ModelVerHash.get(modVerIdKey);
            if (!distinctModelVersHash.containsKey(modVerId)) {
                // First time seeing this model-version-id
                Vertex modVerVtx = getNodeUsingUniqueId(transId, fromAppId, "model-ver",
                    "model-version-id", modVerId);
                String tmpNodeType = "";
                try {
                    tmpNodeType = getModelVerTopWidgetType(modVerVtx, "");
                } catch (AAIException ae) {
                    // There must be some old bad data in the db - we will skip over this model-ver
                    // since its
                    // model is not good anymore - but will log that this is happening.
                    skipModelVerIdList.add(modVerId);
                    skipStartVertVerIdList.add(modVerIdKey);
                    System.out
                        .println(">>>  WARNING - will not collect model data for this vertex since "
                            + "it uses an inconsistant model-ver model.  Model-version-id = "
                            + modVerId);
                }

                if (tmpNodeType != null && !tmpNodeType.equals("")) {
                    if (startNodeType.equals("")) {
                        startNodeType = tmpNodeType;
                    } else if (!startNodeType.equals(tmpNodeType)) {
                        String msg = "Conflict between startNode types for models involved: ["
                            + startNodeType + "], [" + tmpNodeType + "]";
                        throw new AAIException("AAI_6125", msg);
                    }
                    distinctModelVersHash.put(modVerId, modVerVtx);
                }
            }
        }

        // System.out.println("\nDEBUG -- Here's a dump of the DISTINCT model-ver hash: " +
        // distinctModelVersHash.toString() );

        // ------------------------------------------------------------------------------------------------------
        // Get the "valid-next-step" hash for each distinct model-ver
        // While we're at it, get a mapping of model-invariant-id|model-version to model-version-id
        // for
        // the model-vers being used
        // ------------------------------------------------------------------------------------------------------
        Map<String, Multimap<String, String>> validNextStepHash = new HashMap<>();
        // validNextStepHash: key = modelVerId, value = nextStepMap
        Set<String> keySet = distinctModelVersHash.keySet();
        Iterator<String> modelVerIterator = keySet.iterator();
        while (modelVerIterator.hasNext()) {
            String modVerKey = modelVerIterator.next();
            if (!skipModelVerIdList.contains(modVerKey)) {
                Vertex modelVerVtx = distinctModelVersHash.get(modVerKey);
                Multimap<String, String> tmpTopoMap =
                    genTopoMap4ModelVer(transId, fromAppId, modelVerVtx, modVerKey);
                validNextStepHash.put(modVerKey, tmpTopoMap);
            }
        }

        // -------------------------------------------------------------------------------------------------
        // Figure out what the "start-node" for each instance will be (plus the info we will use to
        // represent that in our topology)
        // -------------------------------------------------------------------------------------------------
        List<String> failedPersonaCheckVids = new ArrayList<>();
        Map<String, String> firstStepInfoHash = new HashMap<>();
        // For firstStepInfoHash: key = startNodeVtxId, val=topNodeType plus personaData if
        // applicable
        // ie. the value is what we'd use as the "first-step" for this model.
        if (!nodeTypeSupportsPersona(startNodeType)) {
            // This node type doesn't have persona info, so we just use startNodeType for the
            // first-step-info
            snKeySet = startNode2ModelVerHash.keySet();
            startNodeIterator = snKeySet.iterator();
            while (startNodeIterator.hasNext()) {
                String vtxKey = startNodeIterator.next();
                firstStepInfoHash.put(vtxKey, startNodeType);
            }
        } else {
            // Need to check that this node's persona data is good and if it is - use it for the
            // first step info
            snKeySet = startNode2ModelVerHash.keySet();
            startNodeIterator = snKeySet.iterator();
            while (startNodeIterator.hasNext()) {
                String vtxKey = startNodeIterator.next();
                Iterator<Vertex> vtxIterator =
                    this.engine.asAdmin().getReadOnlyTraversalSource().V(vtxKey);
                Vertex tmpVtx = vtxIterator.next();
                String thisVtxModelVerId = startNode2ModelVerHash.get(vtxKey);
                if (skipModelVerIdList.contains(thisVtxModelVerId)) {
                    // Skip this vertex because it uses a model-ver that is bad
                    continue;
                }
                Vertex modelVerVtx = distinctModelVersHash.get(thisVtxModelVerId);
                Vertex modelVtx = getModelGivenModelVer(modelVerVtx, "");
                String modInvId = modelVtx.<String>property("model-invariant-id").orElse(null);
                String personaModInvId =
                    tmpVtx.<String>property(addDBAliasedSuffix("model-invariant-id")).orElse(null);
                String personaModVerId =
                    tmpVtx.<String>property(addDBAliasedSuffix("model-version-id")).orElse(null);
                if (modInvId.equals(personaModInvId) && thisVtxModelVerId.equals(personaModVerId)) {
                    String tmpPersonaInfoStr =
                        startNodeType + "," + personaModInvId + "," + personaModVerId;
                    firstStepInfoHash.put(vtxKey, tmpPersonaInfoStr);
                } else {
                    // we won't use this start node below when we collect data because it should
                    // have
                    // had persona data that matched it's model - but it did not.
                    failedPersonaCheckVids.add(vtxKey);
                }
            }
        }

        // System.out.println("\nDEBUG -- Here's a dump of the firstStepInfoHash hash: " +
        // firstStepInfoHash.toString() );

        // ------------------------------------------------------------------------------------------------
        // Loop through each start-node, collect it's data using collectInstanceData() and put the
        // resultSet onto the resultArray.
        // ------------------------------------------------------------------------------------------------

        // Make sure they're not bringing back too much data
        String maxString = AAIConfig.get("aai.model.query.resultset.maxcount");
        if (maxString != null && !maxString.equals("")) {
            int maxSets = 0;
            try {
                maxSets = Integer.parseInt(maxString);
            } catch (Exception nfe) {
                // Don't worry, we will leave the max as zero - which tells us not to use it.
            }

            if (maxSets > 0 && (startNode2ModelVerHash.size() > maxSets)) {
                String msg = " Query returns " + startNode2ModelVerHash.size()
                    + " resultSets.  Max allowed is: " + maxSets;
                throw new AAIException("AAI_6141", msg);
            }
        }

        snKeySet = startNode2ModelVerHash.keySet();
        startNodeIterator = snKeySet.iterator();
        while (startNodeIterator.hasNext()) {
            String topNodeVtxId = startNodeIterator.next();
            if (failedPersonaCheckVids.contains(topNodeVtxId)
                || skipStartVertVerIdList.contains(topNodeVtxId)) {
                // Skip this vertex because it failed it's persona-data check above
                // Skip this vertex because it uses a model-ver that is bad
                continue;
            }

            Iterator<Vertex> vtxIterator =
                this.engine.asAdmin().getReadOnlyTraversalSource().V(topNodeVtxId);
            Vertex tmpStartVtx = vtxIterator.next();
            String elementLocationTrail = firstStepInfoHash.get(topNodeVtxId);
            String modelVerId = startNode2ModelVerHash.get(topNodeVtxId);
            Multimap<String, String> validNextStepMap = validNextStepHash.get(modelVerId);

            List<String> vidsTraversed = new ArrayList<>();
            Map<String, String> emptyDelKeyHash = new HashMap<>();
            Map<String, String> emptyNQElementHash = new HashMap<>(); // Only applies to Named
                                                                      // Queries
            ResultSet tmpResSet = collectInstanceData(transId, fromAppId, tmpStartVtx,
                elementLocationTrail, validNextStepMap, vidsTraversed, 0, emptyDelKeyHash,
                emptyNQElementHash, apiVer);

            resultArray.add(tmpResSet);
        }

        return resultArray;

    }// queryByModel_Timed()

    /**
     * Run delete by model-ver.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param modelVersionId the model version id -- unique id for a model-ver node
     * @param topNodeTypeVal the top node type val -- required if no model-version-id is passed
     * @param startNodeFilterHash the start node filter hash -- used to locate the first node of
     *        instance data
     * @param apiVer the api ver
     * @param resVersion the res version -- resourceVersion of the top/first widget in the model
     *        instance
     * @return HashMap (keys = vertexIds that were deleted)
     * @throws AAIException the AAI exception
     */
    public Map<String, String> runDeleteByModel(String transId, String fromAppId,
        String modelVersionId, String topNodeTypeVal, Map<String, Object> startNodeFilterHash,
        String apiVer, String resVersion) throws AAIException {

        Map<String, String> retHash = new HashMap<>();

        // Locate the Model-ver node to be used
        Vertex modelVerVtx = null;
        if (modelVersionId != null && !modelVersionId.equals("")) {
            modelVerVtx = getNodeUsingUniqueId(transId, fromAppId, "model-ver", "model-version-id",
                modelVersionId);
        } else {
            // if they didn't pass the modelVersionId, then we need to use the startNode to figure
            // it out
            // Locate the starting node based on the start node params
            if (topNodeTypeVal == null || topNodeTypeVal.equals("")) {
                throw new AAIException("AAI_6118",
                    "If no model info is passed, then topNodeType is required. ");
            }

            Optional<Vertex> result =
                dbMethHelper.searchVertexByIdentityMap(topNodeTypeVal, startNodeFilterHash);
            if (!result.isPresent()) {
                throw new AAIException("AAI_6114",
                    "No Node of type " + topNodeTypeVal + " found for properties");
            }
            Vertex startVtx = result.get();

            String startVertModVerId =
                startVtx.<String>property(addDBAliasedSuffix("model-version-id")).orElse(null);
            modelVerVtx = getNodeUsingUniqueId(transId, fromAppId, "model-ver", "model-version-id",
                startVertModVerId);
        }

        if (modelVerVtx == null) {
            throw new AAIException("AAI_6114",
                "Could not determine the model-ver for the given input parameters. ");
        }

        String topNType = "unknown";
        String modelType = getModelTypeFromModelVer(modelVerVtx, "");

        if (modelType.equals("widget")) {
            // If they want to delete using a widget-level model.. That is just a delete of the one
            // instance of one of our nodes.
            String widgModNodeType = modelVerVtx.<String>property("model-name").orElse(null);
            if ((widgModNodeType == null) || widgModNodeType.equals("")) {
                String msg =
                    "Could not find model-name for the widget model  [" + modelVersionId + "].";
                throw new AAIException("AAI_6132", msg);
            }
            Optional<Vertex> result =
                dbMethHelper.locateUniqueVertex(widgModNodeType, startNodeFilterHash);
            if (!result.isPresent()) {
                throw new AAIException("AAI_6114",
                    "No Node of type " + topNType + " found for properties");
            }
            Vertex widgetVtx = result.get();
            String widgId = widgetVtx.id().toString();
            serializer.delete(widgetVtx, resVersion, true);
            retHash.put(widgId, widgModNodeType);
            return retHash;
        }

        // ---------------------------------------------------------------------------------
        // If we got to here, this must be either a service or resource model.
        // So, we'll need to get a Hash of which parts of the model to delete.
        // NOTE- deleteByModel is deleting data based on one specific version of a model.
        // ---------------------------------------------------------------------------------
        String chkFirstNodePersonaModInvId = "";
        String chkFirstNodePersonaModVerId = "";
        String personaData = "";
        Vertex firstModElementVertex = getTopElementForSvcOrResModelVer(modelVerVtx, "");
        topNType = getModElementWidgetType(firstModElementVertex, "");
        if ((topNType == null) || topNType.equals("")) {
            String msg = "Could not determine the top-node nodeType for model-version-id: ["
                + modelVersionId + "]";
            throw new AAIException("AAI_6132", msg);
        }
        if (nodeTypeSupportsPersona(topNType)) {
            Vertex modelVtx = getModelGivenModelVer(modelVerVtx, "");
            chkFirstNodePersonaModInvId =
                modelVtx.<String>property("model-invariant-id").orElse(null);
            chkFirstNodePersonaModVerId =
                modelVerVtx.<String>property("model-version-id").orElse(null);
            personaData = "," + chkFirstNodePersonaModInvId + "," + chkFirstNodePersonaModVerId;
        }

        // Get the deleteKeyHash for this model
        String incomingTrail = "";
        Map<String, String> currentHash = new HashMap<>();
        Map<String, Vertex> modConHash = new HashMap<>();
        ArrayList<String> vidsTraversed = new ArrayList<>();
        Map<String, String> delKeyHash = collectDeleteKeyHash(transId, fromAppId,
            firstModElementVertex, incomingTrail, currentHash, vidsTraversed, 0, modConHash,
            chkFirstNodePersonaModInvId, chkFirstNodePersonaModVerId);

        System.out.println(
            "\n ----DEBUG -----:  Delete Hash for model: [" + modelVersionId + "] looks like: ");
        for (Map.Entry<String, String> entry : delKeyHash.entrySet()) {
            System.out.println("key = [" + entry.getKey() + "], val = [" + entry.getValue() + "]");
        }
        System.out.println("\n -----");
        // Locate the starting node that we'll use to start looking for instance data
        Optional<Vertex> result =
            dbMethHelper.searchVertexByIdentityMap(topNType, startNodeFilterHash);
        if (!result.isPresent()) {
            throw new AAIException("AAI_6114",
                "No Node of type " + topNType + " found for properties");
        }
        Vertex startVtx = result.get();
        if (!chkFirstNodePersonaModInvId.equals("")) {
            // NOTE: For Service or Resource models, if this is a nodeType that supports persona's,
            // then
            // we need to make sure that the start node matches the persona values.
            String startVertPersonaModInvId =
                startVtx.<String>property(addDBAliasedSuffix("model-invariant-id")).orElse(null);
            String startVertPersonaModVerId =
                startVtx.<String>property(addDBAliasedSuffix("model-version-id")).orElse(null);
            if (!chkFirstNodePersonaModInvId.equals(startVertPersonaModInvId)
                || !chkFirstNodePersonaModVerId.equals(startVertPersonaModVerId)) {
                String msg = "Persona-Model data mismatch for start node (" + topNType + "), "
                    + startNodeFilterHash;
                throw new AAIException("AAI_6114", msg);
            }
        }
        String topVid = startVtx.id().toString();

        // Read the model-ver into a Map for processing
        Multimap<String, String> validNextStepMap =
            genTopoMap4ModelVer(transId, fromAppId, modelVerVtx, modelVersionId);

        // Collect the data
        String elementLocationTrail = topNType + personaData;
        vidsTraversed = new ArrayList<>();
        Map<String, String> emptyHash = new HashMap<>();

        // Pass emptyHash for the NQElement hash since that parameter only applies to Named Queries
        ResultSet retResSet =
            collectInstanceData(transId, fromAppId, startVtx, elementLocationTrail,
                validNextStepMap, vidsTraversed, 0, delKeyHash, emptyHash, apiVer);

        // Note: the new ResultSet will have each element tagged with the del flag so we'll know if
        // it
        // should be deleted or not - so loop through the results in a try-block since some things
        // will get auto-deleted by parents before we get to them --- and try to remove each one.
        String vidToResCheck = topVid;

        retHash = deleteAsNeededFromResultSet(transId, fromAppId, retResSet, vidToResCheck, apiVer,
            resVersion, emptyHash);
        // String msgStr = "processed deletes for these vids: (\n"+ retHash.keySet().toString() +
        // ").";

        return retHash;

    }// End of runDeleteByModel()

    /**
     * Delete as needed from result set.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param resSet the res set
     * @param vidToResCheck -- this vertex will need to have its resource-version checked
     * @param apiVer the api ver
     * @param resVersion the res version
     * @param hashSoFar the hash so far -- hash of what's been deleted so far
     * @return String
     * @throws AAIException the AAI exception
     */
    public Map<String, String> deleteAsNeededFromResultSet(String transId, String fromAppId,
        ResultSet resSet, String vidToResCheck, String apiVer, String resVersion,
        Map<String, String> hashSoFar) throws AAIException {
        Map<String, String> retHash = new HashMap<>();
        retHash.putAll(hashSoFar);
        Boolean deleteIt = false;

        if (resSet.getVert() == null) {
            return retHash;
        }

        Vertex thisVtx = resSet.getVert();
        String thisGuyId = "";
        String thisNT = "";
        String thisGuyStr = "";

        Boolean gotVtxOK = false;
        try {
            if (thisVtx != null) {
                thisGuyId = thisVtx.id().toString();
                thisNT = thisVtx.<String>property(AAIProperties.NODE_TYPE).orElse(null);
                thisGuyStr = thisGuyId + "[" + thisNT + " found at:"
                    + resSet.getLocationInModelSubGraph() + "]";

                // NOTE -- will try to set the NodeType to itself to see if the node has been
                // deleted already in
                // this transaction. It lets you get properties from nodes being deleted where the
                // delete hasn't been committed yet. This check used to be accomplished with a call
                // to
                // "vtx.isRemoved()" but that was a Titan-only feature and is not available anymore
                // since
                // we no longer use Titan vertices.
                // If we don't do this check, we get errors later when we try to delete the node.
                thisVtx.property(AAIProperties.NODE_TYPE, thisNT);
                gotVtxOK = true;
            }
        } catch (Exception ex) {
            // Sometimes things have already been deleted by the time we get to them - just log it.
            AAIException aaiException =
                new AAIException("AAI_6154", thisGuyStr + ".  msg = " + ex.getMessage());
            ErrorLogHelper.logException(aaiException);

        }

        if (!gotVtxOK) {
            // The vertex must have already been removed. Just return.
            // Note - We need to catch this because the DB sometimes can still have the vtx
            // and be able to get its ID but it is flagged internally as removed already.
            return retHash;
        } else {
            if (resSet.getNewDataDelFlag() != null && resSet.getNewDataDelFlag().equals("T")) {
                logger.debug(">>  will try to delete this one >> " + thisGuyStr);

                try {
                    Boolean requireResourceVersion = false;
                    if (thisGuyId.equals(vidToResCheck)) {
                        // This is the one vertex that we want to check the resourceId before
                        // deleting
                        requireResourceVersion = true;
                    }
                    this.serializer.delete(thisVtx, resVersion, requireResourceVersion);
                } catch (AAIException ae) {
                    String errorCode = ae.getErrorObject().getErrorCode();
                    if (errorCode.equals("6130") || errorCode.equals("6131")) {
                        // They didn't pass the correct resource-version for the top node.
                        throw ae;
                    } else {
                        ErrorLogHelper.logException(ae);
                        String errText = ae.getErrorObject().getErrorText();
                        String errDetail = ae.getMessage();
                        logger.debug("Exception when deleting " + thisGuyStr + ".  ErrorCode = "
                            + errorCode + ", errorText = " + errText + ", details = " + errDetail);
                    }
                } catch (Exception e) {
                    // We'd expect to get a "node not found" here sometimes depending on the order
                    // that
                    // the model has us finding / deleting nodes.
                    // Ignore the exception - but log it so we can see what happened.
                    AAIException aaiException =
                        new AAIException("AAI_6154", thisGuyStr + ".  msg = " + e.getMessage());
                    ErrorLogHelper.logException(aaiException);

                }

                // We can't depend on a thrown exception to tell us if a node was deleted since it
                // may
                // have been auto=deleted before this removeAaiNode() call.
                // --- Not sure if we would want to check anything here -- because the
                // graph.commit() is done outside of this call.

                deleteIt = true;
            } else {
                // --- DEBUG ----
                System.out.println(">>>>>>> NOT DELETING THIS ONE >>>> " + thisGuyStr);
                List<String> retArr = dbMethHelper.getVertexProperties(thisVtx);
                for (String info : retArr) {
                    System.out.println(info);
                }
                // --- DEBUG ----
            }
        }

        // Now call this routine for the sub-resultSets
        List<ResultSet> subResultSetList = resSet.getSubResultSet();
        Iterator<ResultSet> subResSetIter = subResultSetList.iterator();
        while (subResSetIter.hasNext()) {
            ResultSet tmpSubResSet = subResSetIter.next();
            retHash = deleteAsNeededFromResultSet(transId, fromAppId, tmpSubResSet, vidToResCheck,
                apiVer, resVersion, retHash);
        }

        if (deleteIt) {
            retHash.put(thisGuyId, thisGuyStr);
        }

        return retHash;

    }// deleteAsNeededFromResultSet()

    /**
     * Query by named query (old version).
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param namedQueryUuid the named query uuid
     * @param startNodeFilterArrayOfHashes the start node filter array of hashes --used to locate
     *        the first nodes of instance data
     * @param apiVer the api ver
     * @return resultSet
     * @throws AAIException the AAI exception
     */
    public List<ResultSet> queryByNamedQuery(String transId, String fromAppId,
        String namedQueryUuid, List<Map<String, Object>> startNodeFilterArrayOfHashes,
        String apiVer) throws AAIException {

        String dummyCutPoint = null;
        Map<String, Object> dummySecondaryFilterHash = null;

        return queryByNamedQuery(transId, fromAppId, namedQueryUuid, startNodeFilterArrayOfHashes,
            apiVer, dummyCutPoint, dummySecondaryFilterHash);
    }

    /**
     * Query by named query.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param namedQueryUuid the named query uuid
     * @param startNodeFilterArrayOfHashes the start node filter array of hashes --used to locate
     *        the first nodes of instance data
     * @param apiVer the api ver
     * @param secondaryFilterCutPoint nodeType where we will prune if secondary filter is not met
     * @param secondaryFilterHash secondary filter params
     * @return resultSet
     * @throws AAIException the AAI exception
     */
    public List<ResultSet> queryByNamedQuery(String transId, String fromAppId,
        String namedQueryUuid, List<Map<String, Object>> startNodeFilterArrayOfHashes,
        String apiVer, String secondaryFilterCutPoint, Map<String, Object> secondaryFilterHash)
        throws AAIException {

        final String transId_f = transId;
        final String fromAppId_f = fromAppId;
        final String namedQueryUuid_f = namedQueryUuid;
        final List<Map<String, Object>> startNodeFilterArrayOfHashes_f =
            startNodeFilterArrayOfHashes;
        final String apiVer_f = apiVer;
        final String secondaryFilterCutPoint_f = secondaryFilterCutPoint;
        final Map<String, Object> secondaryFilterHash_f = secondaryFilterHash;

        // Find out what our time-limit should be
        int timeLimitSec = 0;
        String timeLimitString = AAIConfig.get("aai.model.query.timeout.sec");
        if (timeLimitString != null && !timeLimitString.equals("")) {
            try {
                timeLimitSec = Integer.parseInt(timeLimitString);
            } catch (Exception nfe) {
                // Don't worry, we will leave the limit as zero - which tells us not to use it.
            }
        }

        if (timeLimitSec <= 0) {
            // We will NOT be using a timer
            return queryByNamedQuery_Timed(transId, fromAppId, namedQueryUuid,
                startNodeFilterArrayOfHashes, apiVer, secondaryFilterCutPoint_f,
                secondaryFilterHash_f);
        }

        List<ResultSet> resultList = new ArrayList<>();
        TimeLimiter limiter = SimpleTimeLimiter.create(Executors.newCachedThreadPool());
        try {
            resultList = limiter.callWithTimeout(new AaiCallable<List<ResultSet>>() {
                public List<ResultSet> process() throws AAIException {
                    return queryByNamedQuery_Timed(transId_f, fromAppId_f, namedQueryUuid_f,
                        startNodeFilterArrayOfHashes_f, apiVer_f, secondaryFilterCutPoint_f,
                        secondaryFilterHash_f);
                }
            }, timeLimitSec, TimeUnit.SECONDS);
        } catch (UncheckedTimeoutException ute) {
            throw new AAIException("AAI_6140",
                "Query Processing Limit exceeded. (limit = " + timeLimitSec + " seconds)");
        } catch (Exception e) {
            throw new AAIException("AAI_6128",
                "Unexpected exception in queryByNamedQuery(): " + e.getMessage());
        }

        return resultList;
    }

    /**
     * Query by named query timed.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param namedQueryUuid the named query uuid
     * @param startNodeFilterArrayOfHashes the start node filter array of hashes --used to locate
     *        the first nodes of instance data
     * @param apiVer the api ver
     * @param secondaryFilterCutPoint the nodeType where we will parse for the secondary Filter
     * @param secondaryFilterHash the secondary filter hash
     * @return resultSet
     * @throws AAIException the AAI exception
     */
    public List<ResultSet> queryByNamedQuery_Timed(String transId, String fromAppId,
        String namedQueryUuid, List<Map<String, Object>> startNodeFilterArrayOfHashes,
        String apiVer, String secondaryFilterCutPoint, Map<String, Object> secondaryFilterHash)
        throws AAIException {

        // Locate the Query to be used
        Vertex queryVtx = getNodeUsingUniqueId(transId, fromAppId, "named-query",
            "named-query-uuid", namedQueryUuid);

        // Get the first/top named-query-element used by this query
        Iterator<Vertex> vertI =
            this.traverseIncidentEdges(EdgeType.TREE, queryVtx, "named-query-element");
        Vertex firstNqElementVert = null;
        int count = 0;
        String topNType = "";
        while (vertI != null && vertI.hasNext()) {
            firstNqElementVert = vertI.next();
            count++;
            topNType = getNqElementWidgetType(transId, fromAppId, firstNqElementVert, "");
        }

        if (count < 1) {
            // A named query must start with a single top element
            throw new AAIException("AAI_6133",
                "No top-node defined for named-query-uuid = [" + namedQueryUuid + "]");
        } else if (count > 1) {
            // A named query should start with a single top element
            throw new AAIException("AAI_6133",
                "More than one top-node defined for named-query-uuid = [" + namedQueryUuid + "]");
        }
        if ((topNType == null) || topNType.equals("")) {
            String msg = "Could not determine the top-node nodeType for Named Query: ["
                + namedQueryUuid + "]";
            throw new AAIException("AAI_6133", msg);
        }

        // Read the topology into a hash for processing
        Multimap<String, String> validNextStepMap =
            genTopoMap4NamedQ(transId, fromAppId, queryVtx, namedQueryUuid);

        List<Vertex> startVertList = new ArrayList<>();
        if (startNodeFilterArrayOfHashes.size() == 1) {
            // If there is only one set of startFilter info given, then allow it to possibly not be
            // defining just one start node.
            Map<String, Object> tmpHash = startNodeFilterArrayOfHashes.get(0);
            Set<String> propKeySet = tmpHash.keySet();
            Iterator<String> propIter = propKeySet.iterator();
            Introspector obj = loader.introspectorFromName(topNType);
            Set<String> keys = obj.getKeys();
            boolean foundIndexedField = false;
            int propertiesSet = 0;
            while (propIter.hasNext()) {
                String oldVtxKey = propIter.next();
                String newKey = oldVtxKey;
                String[] parts = oldVtxKey.split("\\.");
                if (parts.length == 2) {
                    newKey = parts[1];
                }
                Object obVal = tmpHash.get(oldVtxKey);
                if (obj.hasProperty(newKey)) {
                    if (keys.contains(newKey)) {
                        foundIndexedField = true;
                    }
                    obj.setValue(newKey, obVal);
                    propertiesSet++;
                }
            }
            // we found all the properties in the startNodeType
            if (propertiesSet == propKeySet.size()) {
                if (foundIndexedField) {
                    QueryBuilder builder = this.engine.getQueryBuilder().exactMatchQuery(obj);
                    startVertList = builder.toList();
                } else {
                    // force a filter from aai-node-type
                    QueryBuilder builder = this.engine.getQueryBuilder().createContainerQuery(obj)
                        .exactMatchQuery(obj);
                    startVertList = builder.toList();
                }
            } else {
                Optional<Vertex> tmpVtx = dbMethHelper.searchVertexByIdentityMap(topNType,
                    startNodeFilterArrayOfHashes.get(0));
                // Only found one, so just use it.
                if (tmpVtx.isPresent()) {
                    startVertList.add(tmpVtx.get());
                }
            }
        } else {
            // Since they give an array of startNodeFilterHash info, we expect each one
            // to just point to one node.
            for (int i = 0; i < startNodeFilterArrayOfHashes.size(); i++) {
                // Locate the starting node for each set of data
                Optional<Vertex> tmpVtx = dbMethHelper.searchVertexByIdentityMap(topNType,
                    startNodeFilterArrayOfHashes.get(i));
                if (tmpVtx.isPresent()) {
                    startVertList.add(tmpVtx.get());
                }
            }
        }

        if (startVertList.isEmpty()) {
            throw new AAIException("AAI_6114",
                "No Node of type " + topNType + " found for properties");
        }
        // Make sure they're not bringing back too much data
        String maxString = AAIConfig.get("aai.model.query.resultset.maxcount");
        if (maxString != null && !maxString.equals("")) {
            int maxSets = Integer.parseInt(maxString);
            if (startVertList.size() > maxSets) {
                String msg = " Query returns " + startVertList.size()
                    + " resultSets.  Max allowed is: " + maxSets;
                throw new AAIException("AAI_6141", msg);
            }
        }

        // Loop through each start node and get its data
        List<ResultSet> resSetList = new ArrayList<>();
        for (int i = 0; i < startVertList.size(); i++) {
            Vertex startVtx = startVertList.get(i);
            // Collect the data
            String elementLocationTrail = topNType;
            ArrayList<String> vidsTraversed = new ArrayList<>();
            Map<String, String> emptyDelKeyHash = new HashMap<>(); // Does not apply to Named
                                                                   // Queries

            // Get the mapping of namedQuery elements to our widget topology for this namedQuery
            String incomingTrail = "";
            Map<String, String> currentHash = new HashMap<>();

            Map<String, String> namedQueryElementHash = collectNQElementHash(transId, fromAppId,
                firstNqElementVert, incomingTrail, currentHash, vidsTraversed, 0);

            vidsTraversed = new ArrayList<>();
            ResultSet tmpResSet = collectInstanceData(transId, fromAppId, startVtx,
                elementLocationTrail, validNextStepMap, vidsTraversed, 0, emptyDelKeyHash,
                namedQueryElementHash, apiVer);
            resSetList.add(tmpResSet);
        }

        // If a secondary filter was defined, we will prune the collected instance data result
        // set(s) based on it.
        List<ResultSet> prunedResSetList = new ArrayList<>();
        if (resSetList != null && !resSetList.isEmpty()) {
            for (int i = 0; i < resSetList.size(); i++) {
                if (secondaryFilterCutPoint == null || secondaryFilterCutPoint.equals("")
                    || secondaryFilterHash == null) {
                    // They didn't want to do any pruning, so just use the results we already had
                    prunedResSetList.add(resSetList.get(i));
                } else {
                    ResultSet tmpResSet = pruneResultSet(resSetList.get(i), secondaryFilterCutPoint,
                        secondaryFilterHash);
                    if (tmpResSet != null) {
                        prunedResSetList.add(tmpResSet);
                    }
                }
            }
        }

        // Since a NamedQuery can mark some nodes as "do-not-display", we need to collapse our
        // resultSet so
        // does not display those nodes.
        List<ResultSet> collapsedResSetList = new ArrayList<>();
        if (prunedResSetList != null && !prunedResSetList.isEmpty()) {
            for (int i = 0; i < prunedResSetList.size(); i++) {
                // Note - a single resultSet could be collapsed into many smaller ones if they
                // marked all the "top" node-elements as do-not-output. Ie. the query may
                // have had a top-node of "generic-vnf" which joins down to different l-interfaces.
                // If they only want to see the l-interfaces, then a single result set
                // would be "collapsed" into many separate resultSets - each of which is
                // just a single l-interface.
                List<ResultSet> tmpResSetList = collapseForDoNotOutput(prunedResSetList.get(i));
                if (tmpResSetList != null && !tmpResSetList.isEmpty()) {
                    for (int x = 0; x < tmpResSetList.size(); x++) {
                        // showResultSet( tmpResSetList.get(x), 0 ); //DEBUG-- this was just for
                        // testing
                        collapsedResSetList.add(tmpResSetList.get(x));
                    }
                }
            }
        }

        return collapsedResSetList;

    }// End of queryByNamedQuery()

    /**
     * Prune a result set as per a secondary filter.
     *
     * @param resSetVal the res set val
     * @param cutPointType the nodeType where the trim will happen
     * @param secFilterHash hash of properties and values to use as the secondary filter
     * @return pruned result set
     * @throws AAIException the AAI exception
     */
    public ResultSet pruneResultSet(ResultSet resSetVal, String cutPointType,
        Map<String, Object> secFilterHash) throws AAIException {

        // Given a ResultSet and some secondary filter info, do pruning as needed
        ResultSet pResSet = new ResultSet();

        // For this ResultSet, we will see if we are on a node of the type that is our cutPoint;
        // then only keep it if we peek "below" and see a match for our filter.

        String nt = resSetVal.getVert().<String>property(AAIProperties.NODE_TYPE).orElse(null);
        if (nt != null && nt.equals(cutPointType)) {
            // We are on the type of node that may need to be "pruned" along with it's sub-results
            if (!satisfiesFilters(resSetVal, secFilterHash)) {
                // Return an empty result set since we are pruning at this level.
                return pResSet;
            }
        }

        // If we made it to here, we will not be pruning at this level, so we will
        // be returning a copy of this resultSet that has it's subResults pruned (as needed).
        pResSet.setVert(resSetVal.getVert());
        pResSet.setDoNotOutputFlag(resSetVal.getDoNotOutputFlag());
        pResSet.setExtraPropertyHash(resSetVal.getExtraPropertyHash());
        pResSet.setLocationInModelSubGraph(resSetVal.getLocationInModelSubGraph());
        pResSet.setNewDataDelFlag(resSetVal.getNewDataDelFlag());
        pResSet.setPropertyLimitDesc(resSetVal.getPropertyLimitDesc());
        pResSet.setPropertyOverRideHash(resSetVal.getPropertyOverRideHash());

        if (!resSetVal.getSubResultSet().isEmpty()) {
            ListIterator<ResultSet> listItr = resSetVal.getSubResultSet().listIterator();
            List<ResultSet> newSubSetList = new ArrayList<>();
            while (listItr.hasNext()) {
                ResultSet tmpSubResSet =
                    pruneResultSet(listItr.next(), cutPointType, secFilterHash);
                if (tmpSubResSet.getVert() != null) {
                    // This one wasn't pruned - so keep it.
                    newSubSetList.add(tmpSubResSet);
                }
            }
            pResSet.setSubResultSet(newSubSetList);
        }

        return pResSet;

    }// End pruneResultSet()

    /**
     * Satisfies hash of filters.
     *
     * @param resSet the res set
     * @param filterHash the filter hash
     * @return true, if successful
     * @throws AAIException the AAI exception
     */
    public boolean satisfiesFilters(ResultSet resSet, Map<String, Object> filterHash)
        throws AAIException {

        if (filterHash.isEmpty()) {
            // Nothing to look for, so no, we didn't find it.
            return false;
        }

        Iterator<?> it = filterHash.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?, ?> filtEntry = (Map.Entry<?, ?>) it.next();
            String propNodeTypeDotName = (filtEntry.getKey()).toString();
            String fpv = (filtEntry.getValue()).toString();

            int periodLoc = propNodeTypeDotName.indexOf('.');
            if (periodLoc <= 0) {
                String emsg = "Bad filter param key passed in: [" + propNodeTypeDotName
                    + "].  Expected format = [nodeName.paramName]\n";
                throw new AAIException("AAI_6120", emsg);
            } else {
                String fnt = propNodeTypeDotName.substring(0, periodLoc);
                String fpn = propNodeTypeDotName.substring(periodLoc + 1);
                if (filterMetByThisSet(resSet, fnt, fpn, fpv)) {
                    // System.out.println(" DEBUG -- satisfied/matched filter: [" + fnt + "|" + fpn
                    // + "|" + fpv + "].");
                } else {
                    // System.out.println(" DEBUG -- NOT satisfied/matched filter: [" + fnt + "|" +
                    // fpn + "|" + fpv + "].");
                    return false;
                }
            }
        }

        // Made it through all the filters -- it found what we were looking for.
        return true;

    }// end of satisfiesFilters()

    /**
     * Filter met by this set.
     *
     * @param resSet the res set
     * @param filtNodeType the filt node type
     * @param filtPropName the filt prop name
     * @param filtPropVal the filt prop val
     * @return true, if successful
     */
    public boolean filterMetByThisSet(ResultSet resSet, String filtNodeType, String filtPropName,
        String filtPropVal) {
        // Note - we are just looking for a positive match for one filter for this resultSet
        // NOTE: we're expecting the filter to have a format like this:
        // "nodeType.parameterName:parameterValue"

        Vertex vert = resSet.getVert();
        if (vert == null) {
            return false;
        } else {
            String nt = resSet.getVert().<String>property(AAIProperties.NODE_TYPE).orElse(null);
            if (nt.equals(filtNodeType)) {
                if (filtPropName.equals("vertex-id")) {
                    // vertex-id can't be gotten the same way as other properties
                    String thisVtxId = vert.id().toString();
                    if (thisVtxId.equals(filtPropVal)) {
                        return true;
                    }
                } else {
                    Object thisValObj = vert.property(filtPropName).orElse(null);
                    if (thisValObj != null) {
                        String thisVal = thisValObj.toString();
                        if (thisVal.equals(filtPropVal)) {
                            return true;
                        }
                    }
                }
            }
        }

        // Didn't find a match at the this level, so check the sets below it meet the criteria
        if (resSet.getSubResultSet() != null) {
            ListIterator<ResultSet> listItr = resSet.getSubResultSet().listIterator();
            while (listItr.hasNext()) {
                if (filterMetByThisSet(listItr.next(), filtNodeType, filtPropName, filtPropVal)) {
                    return true;
                }
            }
        }

        return false;

    }// end of filterMetByThisSet()

    /**
     * Collapse for do not output.
     *
     * @param resSetVal the res set val
     * @return the array list
     * @throws AAIException the AAI exception
     */
    public List<ResultSet> collapseForDoNotOutput(ResultSet resSetVal) throws AAIException {

        // Given a ResultSet -- if it is tagged to NOT be output, then replace it with
        // it's sub-ResultSets if it has any.
        List<ResultSet> colResultSet = new ArrayList<>();

        if (resSetVal.getDoNotOutputFlag().equals("true")) {
            // This ResultSet isn't to be displayed, so replace it with it's sub-ResultSets
            List<ResultSet> subResList = resSetVal.getSubResultSet();
            for (int k = 0; k < subResList.size(); k++) {
                List<ResultSet> newSubResList = collapseForDoNotOutput(subResList.get(k));
                colResultSet.addAll(newSubResList);
            }
        } else {
            // This set will be displayed
            colResultSet.add(resSetVal);
        }

        // For each result set now at this level, call this same routine to collapse their
        // sub-resultSets
        for (int i = 0; i < colResultSet.size(); i++) {
            List<ResultSet> newSubSet = new ArrayList<>();
            List<ResultSet> subResList = colResultSet.get(i).getSubResultSet();
            for (int n = 0; n < subResList.size(); n++) {
                List<ResultSet> newSubResList = collapseForDoNotOutput(subResList.get(n));
                newSubSet.addAll(newSubResList);
            }
            // Replace the old subResultSet with the collapsed set
            colResultSet.get(i).setSubResultSet(newSubSet);
        }

        return colResultSet;

    }// End collapseForDoNotOutput()

    /**
     * Collect instance data.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param thisLevelElemVtx the element vtx at this level
     * @param thisVertsTrail the this verts trail
     * @param validNextStepMap the valid next step map -- hash of valid next steps (node types) for
     *        this model
     * @param vidsTraversed the vids traversed -- ArrayList of vertexId's that we traversed to get
     *        to this point
     * @param levelCounter the level counter
     * @param delKeyHash -- hashMap of which spots on our topology should be deleted during a
     *        modelDelete
     * @param namedQueryElementHash - hashMap which maps each spot in our widget topology to the
     *        NamedQueryElemment that it maps to
     * @param apiVer the api ver
     * @return resultSet
     * @throws AAIException the AAI exception
     */
    public ResultSet collectInstanceData(String transId, String fromAppId, Vertex thisLevelElemVtx,
        String thisVertsTrail, Multimap<String, String> validNextStepMap,
        List<String> vidsTraversed, int levelCounter, Map<String, String> delKeyHash, // only
                                                                                      // applies
                                                                                      // when
                                                                                      // collecting
                                                                                      // data using
                                                                                      // the default
                                                                                      // model for
                                                                                      // delete
        Map<String, String> namedQueryElementHash, // only applies to named-query data collecting
        String apiVer) throws AAIException {

        levelCounter++;

        String thisElemVid = thisLevelElemVtx.id().toString();

        if (levelCounter > MAX_LEVELS) {
            throw new AAIException("AAI_6125",
                "collectInstanceData() has looped across more levels than allowed: " + MAX_LEVELS
                    + ". ");
        }

        ResultSet rs = new ResultSet();
        if (namedQueryElementHash.containsKey(thisVertsTrail)) {
            // We're collecting data for a named-query, so need to see if we need to do anything
            // special
            String nqElUuid = namedQueryElementHash.get(thisVertsTrail);
            Vertex nqElementVtx = getNodeUsingUniqueId(transId, fromAppId, "named-query-element",
                "named-query-element-uuid", nqElUuid);

            String tmpDoNotShow = nqElementVtx.<String>property("do-not-output").orElse(null);
            if (tmpDoNotShow != null && tmpDoNotShow.equals("true")) {
                rs.setDoNotOutputFlag("true");
            }

            if (namedQueryConstraintSaysStop(transId, fromAppId, nqElementVtx, thisLevelElemVtx,
                apiVer)) {
                // There was a property constraint which says they do not want to collect this
                // vertex or whatever
                // might be below it. Just return the empty rs here.
                return rs;
            }

            String propLimDesc = nqElementVtx.<String>property("property-limit-desc").orElse(null);
            if ((propLimDesc != null) && !propLimDesc.equals("")) {
                if (propLimDesc.equalsIgnoreCase("show-all")) {
                    rs.setPropertyLimitDesc(PropertyLimitDesc.SHOW_ALL);
                } else if (propLimDesc.equalsIgnoreCase("show-none")) {
                    rs.setPropertyLimitDesc(PropertyLimitDesc.SHOW_NONE);
                } else if (propLimDesc.equalsIgnoreCase("name-and-keys-only")) {
                    rs.setPropertyLimitDesc(PropertyLimitDesc.SHOW_NAME_AND_KEYS_ONLY);
                }
            }

            // Look to see if we need to use an Override of the normal properties
            Map<String, Object> tmpPropertyOverRideHash = getNamedQueryPropOverRide(transId,
                fromAppId, nqElementVtx, thisLevelElemVtx, apiVer);
            // System.out.println(" DEBUG --- USING this propertyOverride data set on ResSet [" +
            // tmpPropertyOverRideHash.toString() + "]");
            rs.setPropertyOverRideHash(tmpPropertyOverRideHash);

            // See if we need to look up any "unconnected" data that needs to be associated with
            // this result set
            Map<String, Object> tmpExtraPropHash = getNamedQueryExtraDataLookup(transId, fromAppId,
                nqElementVtx, thisLevelElemVtx, apiVer);
            // System.out.println(" DEBUG --- ADDING this EXTRA Lookup data to the ResSet [" +
            // tmpExtraPropHash.toString() + "]");
            rs.setExtraPropertyHash(tmpExtraPropHash);
        }

        rs.setVert(thisLevelElemVtx);
        rs.setLocationInModelSubGraph(thisVertsTrail);
        if (delKeyHash.containsKey(thisVertsTrail) && delKeyHash.get(thisVertsTrail).equals("T")) {
            rs.setNewDataDelFlag("T");
        } else {
            rs.setNewDataDelFlag("F");
        }

        // Use Gremlin-pipeline to just look for edges that go to a valid "next-steps"
        Collection<String> validNextStepColl = validNextStepMap.get(thisVertsTrail);

        // Because of how we process linkage-points, we may have duplicate node-types in our
        // next-stepMap (for one step)
        // So, to keep from looking (and bringing back) the same data twice, we need to make sure
        // our next-steps are unique
        Set<String> validNextStepHashSet = new HashSet<>();
        Iterator<String> ntcItr = validNextStepColl.iterator();
        while (ntcItr.hasNext()) {
            String targetStepStr = ntcItr.next();
            validNextStepHashSet.add(targetStepStr);
        }

        List<String> tmpVidsTraversedList = new ArrayList<>();
        tmpVidsTraversedList.addAll(vidsTraversed);
        tmpVidsTraversedList.add(thisElemVid);

        Iterator<String> ntItr = validNextStepHashSet.iterator();
        while (ntItr.hasNext()) {
            String targetStep = ntItr.next();
            // NOTE: NextSteps can either be just a nodeType, or can be a nodeType plus
            // model-invariant-id-local and model-version-id-local (the two persona properties)
            // if those need to be checked also.
            // When the persona stuff is part of the step, it is a comma separated string.
            // Ie. "nodeType,model-inv-id-local,model-version-id-local" (the two "persona" props)
            //
            String targetNodeType = "";
            String pmid = "";
            String pmv = "";
            Boolean stepIsJustNT = true;
            if (targetStep.contains(",")) {
                stepIsJustNT = false;
                String[] pieces = targetStep.split(",");
                if (pieces.length != 3) {
                    throw new AAIException("AAI_6128",
                        "Unexpected format for nextStep in model processing = [" + targetStep
                            + "]. ");
                } else {
                    targetNodeType = pieces[0];
                    pmid = pieces[1];
                    pmv = pieces[2];
                }
            } else {
                // It's just the nodeType with no other info
                targetNodeType = targetStep;
            }

            GraphTraversal<Vertex, Vertex> modPipe = null;
            if (stepIsJustNT) {
                modPipe = this.engine.asAdmin().getReadOnlyTraversalSource().V(thisLevelElemVtx)
                    .both().has(AAIProperties.NODE_TYPE, targetNodeType);
            } else {
                modPipe = this.engine.asAdmin().getReadOnlyTraversalSource().V(thisLevelElemVtx)
                    .both().has(AAIProperties.NODE_TYPE, targetNodeType)
                    .has(addDBAliasedSuffix("model-invariant-id"), pmid)
                    .has(addDBAliasedSuffix("model-version-id"), pmv);
            }

            if (modPipe == null || !modPipe.hasNext()) {
                // System.out.println("DEBUG - didn't find any [" + targetStep + "] connected to
                // this guy (which is ok)");
            } else {
                while (modPipe.hasNext()) {
                    Vertex tmpVert = modPipe.next();
                    String tmpVid = tmpVert.id().toString();
                    String tmpTrail = thisVertsTrail + "|" + targetStep;
                    if (!vidsTraversed.contains(tmpVid)) {
                        // This is one we would like to use - so we'll include the result set we get
                        // for it
                        ResultSet tmpResSet = collectInstanceData(transId, fromAppId, tmpVert,
                            tmpTrail, validNextStepMap, tmpVidsTraversedList, levelCounter,
                            delKeyHash, namedQueryElementHash, apiVer);

                        rs.getSubResultSet().add(tmpResSet);
                    }
                }
            }
        }

        return rs;

    } // End of collectInstanceData()

    /**
     * Gen topo map 4 model.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param modelVerVertex the model-ver vertex
     * @param modelVerId the model-version-id
     * @return MultiMap of valid next steps for each potential model-element
     * @throws AAIException the AAI exception
     */
    public Multimap<String, String> genTopoMap4ModelVer(String transId, String fromAppId,
        Vertex modelVerVertex, String modelVerId) throws AAIException {

        if (modelVerVertex == null) {
            throw new AAIException("AAI_6114",
                "null modelVerVertex passed to genTopoMap4ModelVer()");
        }

        Multimap<String, String> initialEmptyMap = ArrayListMultimap.create();
        List<String> vidsTraversed = new ArrayList<>();
        String modelType = getModelTypeFromModelVer(modelVerVertex, "");
        if (modelType.equals("widget")) {
            // A widget model by itself does not have a topoplogy. That is - it has no
            // "model-elements" which
            // define how it is connected to other things. All it has is a name which ties it to
            // an aai-node-type
            Iterator<Vertex> vertI =
                this.traverseIncidentEdges(EdgeType.TREE, modelVerVertex, "model-element");
            if (vertI != null && vertI.hasNext()) {
                throw new AAIException("AAI_6132",
                    "Bad Model Definition: Widget Model has a startsWith edge to a model-element.  "
                        + " model-version-id = " + modelVerId);
            } else {
                return initialEmptyMap;
            }
        }

        String firstModelVerId = modelVerVertex.<String>property("model-version-id").orElse(null);
        String firstModelVersion = modelVerVertex.<String>property("model-version").orElse(null);
        if (firstModelVerId == null || firstModelVerId.equals("") || firstModelVersion == null
            || firstModelVersion.equals("")) {
            throw new AAIException("AAI_6132",
                "Bad Model Definition: Bad model-version-id or model-version.  model-version-id = "
                    + modelVerId);
        }

        Vertex firstElementVertex = getTopElementForSvcOrResModelVer(modelVerVertex, "");
        Vertex firstEleModVerVtx = getModelVerThatElementRepresents(firstElementVertex, "");
        String firstElemModelType = getModelTypeFromModelVer(firstEleModVerVtx, "");
        if (!firstElemModelType.equals("widget")) {
            throw new AAIException("AAI_6132",
                "Bad Model Definition: First element must correspond to a widget type model.  Model UUID = "
                    + modelVerId);
        }

        Vertex firstModVtx = getModelGivenModelVer(modelVerVertex, "");
        String firstModelInvId = firstModVtx.<String>property("model-invariant-id").orElse(null);
        if (firstModelInvId == null || firstModelInvId.equals("")) {
            throw new AAIException("AAI_6132",
                "Bad Model Definition: Could not find model.model-invariant-id given model-ver.model-version-id = "
                    + modelVerId);
        }

        return collectTopology4ModelVer(transId, fromAppId, firstElementVertex, "", initialEmptyMap,
            vidsTraversed, 0, null, firstModelInvId, firstModelVersion);
    } // End of genTopoMap4ModelVer()

    public List<String> makeSureItsAnArrayList(String listStringVal) {
        // We're sometimes getting a String back on db properties that should be ArrayList<String>
        // Seems to be how they're defined in OXM - whether they use a "xml-wrapper" or not
        // Need to translate them into ArrayLists sometimes...

        List<String> retArrList = new ArrayList<>();
        String listString = listStringVal;
        listString = listString.replace(" ", "");
        listString = listString.replace("\"", "");
        listString = listString.replace("[", "");
        listString = listString.replace("]", "");
        String[] pieces = listString.split(",");
        if (pieces.length > 0) {
            retArrList.addAll(Arrays.asList(pieces));
        }
        return retArrList;
    }

    /**
     * Gets the mod constraint hash.
     *
     * @param modelElementVtx the model element vtx
     * @param currentHash -- the current ModelConstraint's that this routine will add to if it finds
     *        any.
     * @return HashMap of model-constraints that will be looked at for this model-element and what's
     *         "below" it.
     * @throws AAIException the AAI exception
     */
    public Map<String, Vertex> getModConstraintHash(Vertex modelElementVtx,
        Map<String, Vertex> currentHash) throws AAIException {

        // For a given model-element vertex, look to see if there are any "model-constraint"
        // elements that is has
        // an OUT "uses" edge to. If it does, then get any "constrained-element-set" nodes that are
        // pointed to
        // by the "model-constraint". That will be the replacement "constrained-element-set". The
        // UUID of the
        // "constrained-element-set" that it is supposed to replace is found in the property:
        // model-constraint.constrained-element-set-uuid-to-replace
        //
        // For now, that is the only type of model-constraint allowed, so that is all we will look
        // for.
        // Pass back any of these "constrained-element-set" nodes along with any that were passed in
        // by
        // the "currentHash" parameter.

        if (modelElementVtx == null) {
            String msg = " null modelElementVtx passed to getModConstraintHash() ";
            throw new AAIException("AAI_6114", msg);
        }

        String modelType = modelElementVtx.<String>property(AAIProperties.NODE_TYPE).orElse(null);
        if (modelType == null || (!modelType.equals("model-element"))) {
            String msg =
                " getModConstraintHash() called with wrong type model: [" + modelType + "]. ";
            throw new AAIException("AAI_6114", msg);
        }

        Map<String, Vertex> thisHash = new HashMap<>();
        if (currentHash != null) {
            thisHash.putAll(currentHash);
        }

        int count = 0;
        List<Vertex> modelConstraintArray = new ArrayList<>();
        Iterator<Vertex> vertI =
            this.traverseIncidentEdges(EdgeType.TREE, modelElementVtx, "model-constraint");
        while (vertI != null && vertI.hasNext()) {
            Vertex tmpVert = vertI.next();
            String connectToType = tmpVert.<String>property(AAIProperties.NODE_TYPE).orElse(null);
            if ((connectToType != null) && connectToType.equals("model-constraint")) {
                // We need to find the constrained element set pointed to by this and add it to the
                // Hash to return
                modelConstraintArray.add(tmpVert);
                count++;
            }
        }

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                Vertex vtxOfModelConstraint = modelConstraintArray.get(i);
                String uuidOfTheOneToBeReplaced = vtxOfModelConstraint
                    .<String>property("constrained-element-set-uuid-2-replace").orElse(null);
                // We have the UUID of the constrained-element-set that will be superseded, now find
                // the
                // constrained-element-set to use in its place
                Iterator<Vertex> mvertI = this.traverseIncidentEdges(EdgeType.TREE,
                    vtxOfModelConstraint, "constrained-element-set");
                while (mvertI != null && mvertI.hasNext()) {
                    // There better only be one...
                    Vertex tmpVert = mvertI.next();
                    String connectToType =
                        tmpVert.<String>property(AAIProperties.NODE_TYPE).orElse(null);
                    if ((connectToType != null)
                        && connectToType.equals("constrained-element-set")) {
                        // This is the "constrained-element-set" that we want to use as the
                        // Replacement
                        thisHash.put(uuidOfTheOneToBeReplaced, tmpVert);
                    }
                }
            }
            return thisHash;
        } else {
            // Didn't find anything to add, so just return what they passed in.
            return currentHash;
        }

    } // End of getModConstraintHash()

    /**
     * Gets the top element vertex for service or resource model.
     *
     * @param modelVerVtx the model-ver vertex
     * @return first element pointed to by this model-ver
     * @throws AAIException the AAI exception
     */
    public Vertex getTopElementForSvcOrResModelVer(Vertex modelVerVtx, String trail)
        throws AAIException {

        // For a "resource" or "service" type model, return the "top" element in that model
        if (modelVerVtx == null) {
            String msg = " null modelVertex passed to getTopoElementForSvcOrResModelVer() at ["
                + trail + "]. ";
            throw new AAIException("AAI_6114", msg);
        }

        String modelVerId = modelVerVtx.<String>property("model-version-id").orElse(null);
        if (modelVerId == null) {
            String nt = modelVerVtx.<String>property(AAIProperties.NODE_TYPE).orElse(null);
            if (nt != null && !nt.equals("model-ver")) {
                String msg = "Illegal model defined: model element pointing to nodeType: [" + nt
                    + "], should be pointing to: [model-ver] at [" + trail + "]. ";
                throw new AAIException("AAI_6132", msg);
            }
        }

        Vertex firstElementVertex = null;

        Iterator<Vertex> vertI =
            this.traverseIncidentEdges(EdgeType.TREE, modelVerVtx, "model-element");
        int elCount = 0;
        while (vertI != null && vertI.hasNext()) {
            elCount++;
            firstElementVertex = vertI.next();
        }

        if (elCount > 1) {
            String msg =
                "Illegal model defined: More than one first element defined for model-ver-id = "
                    + modelVerId + " at [" + trail + "]. ";
            throw new AAIException("AAI_6132", msg);
        }

        if (firstElementVertex == null) {
            String msg = "Could not find first model element for model-ver-id = " + modelVerId
                + " at [" + trail + "]. ";
            throw new AAIException("AAI_6132", msg);
        }

        return firstElementVertex;

    } // End of getTopElementForSvcOrResModelVer()

    /**
     * Gets the named query prop over ride.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param namedQueryElementVertex the named query element vertex
     * @param instanceVertex the instance vertex
     * @param apiVer the api ver
     * @return HashMap of alternate properties to return for this element
     * @throws AAIException the AAI exception
     */
    public Map<String, Object> getNamedQueryPropOverRide(String transId, String fromAppId,
        Vertex namedQueryElementVertex, Vertex instanceVertex, String apiVer) throws AAIException {

        // If this model-element says that they want an alternative set of properties returned, then
        // pull that
        // data out of the instance vertex.

        Map<String, Object> altPropHash = new HashMap<>();

        if (namedQueryElementVertex == null) {
            String msg = " null namedQueryElementVertex passed to getNamedQueryPropOverRide() ";
            throw new AAIException("AAI_6114", msg);
        }

        List<String> propCollectList = new ArrayList<>();
        Iterator<VertexProperty<Object>> vpI =
            namedQueryElementVertex.properties("property-collect-list");
        while (vpI.hasNext()) {
            propCollectList.add((String) vpI.next().value());
        }

        for (int i = 0; i < propCollectList.size(); i++) {
            String thisPropName = propCollectList.get(i);
            Object instanceVal = instanceVertex.<Object>property(thisPropName).orElse(null);
            altPropHash.put(thisPropName, instanceVal);
        }

        return altPropHash;

    } // End of getNamedQueryPropOverRide()

    /**
     * Named query constraint says stop.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param namedQueryElementVertex the named query element vertex
     * @param instanceVertex the instance vertex
     * @param apiVer the api ver
     * @return true - if a constraint was defined that has not been met by the passed instanceVertex
     * @throws AAIException the AAI exception
     */
    public Boolean namedQueryConstraintSaysStop(String transId, String fromAppId,
        Vertex namedQueryElementVertex, Vertex instanceVertex, String apiVer) throws AAIException {

        // For each (if any) property-constraint defined for this named-query-element, we will
        // evaluate if
        // the constraint is met or not-met. if there are constraints and any are not-met, then
        // we return "true".

        if (namedQueryElementVertex == null) {
            String msg = " null namedQueryElementVertex passed to namedQueryConstraintSaysStop() ";
            throw new AAIException("AAI_6114", msg);
        }
        if (instanceVertex == null) {
            String msg = " null instanceVertex passed to namedQueryConstraintSaysStop() ";
            throw new AAIException("AAI_6114", msg);
        }

        Iterator<Vertex> constrPipe = this.traverseIncidentEdges(EdgeType.TREE,
            namedQueryElementVertex, "property-constraint");
        if (constrPipe == null || !constrPipe.hasNext()) {
            // There's no "property-constraint" defined for this named-query-element. No problem.
            return false;
        }

        while (constrPipe.hasNext()) {
            Vertex constrVtx = constrPipe.next();
            // We found a property constraint that we will need to check
            String conType = constrVtx.<String>property("constraint-type").orElse(null);
            if ((conType == null) || conType.equals("")) {
                String msg =
                    " Bad property-constraint (constraint-type) found in Named Query definition. ";
                throw new AAIException("AAI_6133", msg);
            }
            String propName = constrVtx.<String>property("property-name").orElse(null);
            if ((propName == null) || propName.equals("")) {
                String msg =
                    " Bad property-constraint (property-name) found in Named Query definition. ";
                throw new AAIException("AAI_6133", msg);
            }
            String propVal = constrVtx.<String>property("property-value").orElse(null);
            if ((propVal == null) || propVal.equals("")) {
                String msg = " Bad property-constraint (propVal) found in Named Query definition. ";
                throw new AAIException("AAI_6133", msg);
            }

            // See if that constraint is met or not
            String val = instanceVertex.<String>property(propName).orElse(null);
            if (val == null) {
                val = "";
            }

            if (conType.equals("EQUALS")) {
                if (!val.equals(propVal)) {
                    // This constraint was not met
                    return true;
                }
            } else if (conType.equals("NOT-EQUALS")) {
                if (val.equals(propVal)) {
                    // This constraint was not met
                    return true;
                }
            } else {
                String msg =
                    " Bad property-constraint (constraint-type) found in Named Query definition. ";
                throw new AAIException("AAI_6133", msg);
            }
        }

        return false;

    } // End of namedQueryConstraintSaysStop()

    /**
     * Gets the named query extra data lookup.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param namedQueryElementVertex the named query element vertex
     * @param instanceVertex the instance vertex
     * @param apiVer the api ver
     * @return HashMap of alternate properties to return for this element
     * @throws AAIException the AAI exception
     */
    public Map<String, Object> getNamedQueryExtraDataLookup(String transId, String fromAppId,
        Vertex namedQueryElementVertex, Vertex instanceVertex, String apiVer) throws AAIException {

        // For each (if any) related-lookup defined for this named-query-element, we will go and
        // and try to find it. All the related-lookup data will get put in a hash and returned.

        if (namedQueryElementVertex == null) {
            String msg = " null namedQueryElementVertex passed to getNamedQueryExtraDataLookup() ";
            throw new AAIException("AAI_6114", msg);
        }
        if (instanceVertex == null) {
            String msg = " null instanceVertex passed to getNamedQueryExtraDataLookup() ";
            throw new AAIException("AAI_6114", msg);
        }

        Map<String, Object> retHash = new HashMap<>();

        Iterator<Vertex> lookPipe =
            this.traverseIncidentEdges(EdgeType.TREE, namedQueryElementVertex, "related-lookup");
        if (lookPipe == null || !lookPipe.hasNext()) {
            // There's no "related-lookup" defined for this named-query-element. No problem.
            return retHash;
        }

        while (lookPipe.hasNext()) {
            Vertex relLookupVtx = lookPipe.next();
            // We found a related-lookup record to try and use
            String srcProp = relLookupVtx.<String>property("source-node-property").orElse(null);
            String srcNodeType = relLookupVtx.<String>property("source-node-type").orElse(null);
            srcProp = getPropNameWithAliasIfNeeded(srcNodeType, srcProp);

            if ((srcProp == null) || srcProp.equals("")) {
                String msg =
                    " Bad related-lookup (source-node-property) found in Named Query definition. ";
                throw new AAIException("AAI_6133", msg);
            }
            String targetNodeType = relLookupVtx.<String>property("target-node-type").orElse(null);
            if ((targetNodeType == null) || targetNodeType.equals("")) {
                String msg =
                    " Bad related-lookup (targetNodeType) found in Named Query definition. ";
                throw new AAIException("AAI_6133", msg);
            }
            String targetProp = relLookupVtx.<String>property("target-node-property").orElse(null);
            targetProp = getPropNameWithAliasIfNeeded(targetNodeType, targetProp);

            if ((targetProp == null) || targetProp.equals("")) {
                String msg =
                    " Bad related-lookup (target-node-property) found in Named Query definition. ";
                throw new AAIException("AAI_6133", msg);
            }

            List<String> propCollectList = new ArrayList<>();
            Iterator<VertexProperty<Object>> vpI = relLookupVtx.properties("property-collect-list");
            while (vpI.hasNext()) {
                propCollectList.add((String) vpI.next().value());
            }

            // Use the value from the source to see if we can find ONE target record using the
            // value from the source
            String valFromInstance = instanceVertex.<String>property(srcProp).orElse(null);
            if (valFromInstance == null) {
                // if there is no key to use to go look up something, we should end it here and just
                // note what happened - no need to try to look something up by an empty key
                logger.debug("WARNING - the instance data node of type [" + srcNodeType
                    + "] did not have a value for property [" + srcProp
                    + "], so related-lookup is being abandoned.");
                return retHash;
            }

            Map<String, Object> propHash = new HashMap<>();
            propHash.put(targetProp, valFromInstance);

            Optional<Vertex> result = dbMethHelper.locateUniqueVertex(targetNodeType, propHash);
            if (!result.isPresent()) {
                // If it can't find the lookup node, don't fail, just log that it couldn't be found
                // ---
                logger.debug("WARNING - Could not find lookup node that corresponds to nodeType ["
                    + targetNodeType + "] propertyName = [" + srcProp + "], propVal = ["
                    + valFromInstance + "] so related-lookup is being abandoned.");
                return retHash;
            } else {
                Vertex tmpVtx = result.get();
                // Pick up the properties from the target vertex that they wanted us to get
                for (int j = 0; j < propCollectList.size(); j++) {
                    String tmpPropName = propCollectList.get(j);
                    tmpPropName = getPropNameWithAliasIfNeeded(targetNodeType, tmpPropName);
                    Object valObj = tmpVtx.<Object>property(tmpPropName).orElse(null);
                    String lookupKey = targetNodeType + "." + tmpPropName;
                    retHash.put(lookupKey, valObj);

                }
            }
        }

        return retHash;

    } // End of getNamedQueryExtraDataLookup()

    /**
     * Collect NQ element hash.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param thisLevelElemVtx the element verrtx for this level
     * @param incomingTrail the incoming trail -- trail of nodeTypes that got us here (this
     *        nq-element vertex) from the top
     * @param currentHash the current hash
     * @param vidsTraversed the vids traversed -- ArrayList of vertexId's that we traversed to get
     *        to this point
     * @param levelCounter the level counter
     * @return HashMap of all widget-points on a namedQuery topology with the value being the
     *         "named-query-element-uuid" for that spot.
     * @throws AAIException the AAI exception
     */
    public Map<String, String> collectNQElementHash(String transId, String fromAppId,
        Vertex thisLevelElemVtx, String incomingTrail, Map<String, String> currentHash,
        List<String> vidsTraversed, int levelCounter) throws AAIException {

        levelCounter++;

        Map<String, String> thisHash = new HashMap<>();
        thisHash.putAll(currentHash);

        if (levelCounter > MAX_LEVELS) {
            throw new AAIException("AAI_6125",
                "collectNQElementHash() has looped across more levels than allowed: " + MAX_LEVELS
                    + ". ");
        }
        String thisGuysTrail = "";
        String thisElemVid = thisLevelElemVtx.id().toString();

        // Find out what widget (and thereby what aai-node-type) this element represents.
        String thisElementNodeType =
            getNqElementWidgetType(transId, fromAppId, thisLevelElemVtx, incomingTrail);

        if (incomingTrail == null || incomingTrail.equals("")) {
            // This is the first one
            thisGuysTrail = thisElementNodeType;
        } else {
            thisGuysTrail = incomingTrail + "|" + thisElementNodeType;
        }
        vidsTraversed.add(thisElemVid);

        String nqElementUuid =
            thisLevelElemVtx.<String>property("named-query-element-uuid").orElse(null);
        if (nqElementUuid == null || nqElementUuid.equals("")) {
            String msg = " named-query element UUID not found at trail = [" + incomingTrail + "].";
            throw new AAIException("AAI_6133", msg);
        }
        thisHash.put(thisGuysTrail, nqElementUuid);

        // Now go "down" and look at the sub-elements pointed to so we can get their data.
        Iterator<Vertex> vertI =
            this.traverseIncidentEdges(EdgeType.TREE, thisLevelElemVtx, "named-query-element");
        while (vertI != null && vertI.hasNext()) {
            Vertex tmpVert = vertI.next();
            String vid = tmpVert.id().toString();
            Map<String, Object> elementHash = new HashMap();

            String connectToType = tmpVert.<String>property(AAIProperties.NODE_TYPE).orElse(null);
            if (connectToType != null && connectToType.equals("named-query-element")) {
                // This is what we would expect
                elementHash.put(vid, tmpVert);
            } else {
                String msg = " named query element has [connectedTo] edge to improper nodeType= ["
                    + connectToType + "] trail = [" + incomingTrail + "].";
                throw new AAIException("AAI_6133", msg);
            }
            for (Map.Entry<String, Object> entry : elementHash.entrySet()) {
                Vertex elVert = (Vertex) (entry.getValue());
                String tmpElVid = elVert.id().toString();
                if (!vidsTraversed.contains(tmpElVid)) {
                    // This is one we would like to use - so we'll recursively get it's result set
                    // to add to ours
                    Map<String, String> tmpHash = collectNQElementHash(transId, fromAppId, elVert,
                        thisGuysTrail, currentHash, vidsTraversed, levelCounter);
                    thisHash.putAll(tmpHash);
                }
            }
        }
        return thisHash;

    } // End of collectNQElementHash()

    /**
     * Collect delete key hash.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param thisLevelElemVtx the element vertex at this level
     * @param incomingTrail the incoming trail -- trail of nodeTypes that got us here (this vertex)
     *        from the top
     * @param currentHash the current hash
     * @param vidsTraversed the vids traversed ---- ArrayList of vertexId's that we traversed to get
     *        to this point
     * @param levelCounter the level counter
     * @param modConstraintHash the mod constraint hash
     * @param overRideModelId the over ride model id
     * @param overRideModelVersionId the over ride model version id
     * @return HashMap of all widget-points on a model topology with the value being the
     *         "newDataDelFlag" for that spot.
     * @throws AAIException the AAI exception
     */
    public Map<String, String> collectDeleteKeyHash(String transId, String fromAppId,
        Vertex thisLevelElemVtx, String incomingTrail, Map<String, String> currentHash,
        List<String> vidsTraversed, int levelCounter, Map<String, Vertex> modConstraintHash,
        String overRideModelId, String overRideModelVersionId) throws AAIException {

        levelCounter++;

        Map<String, String> thisHash = new HashMap<>();
        thisHash.putAll(currentHash);

        if (levelCounter > MAX_LEVELS) {
            throw new AAIException("AAI_6125",
                "collectDeleteKeyHash() has looped across more levels than allowed: " + MAX_LEVELS
                    + ". ");
        }
        String thisGuysTrail = "";
        String thisElemVid = thisLevelElemVtx.id().toString();
        Map<String, Vertex> modConstraintHash2Use = null;

        // If this element represents a resource or service model, then we will replace this element
        // with
        // the "top" element of that resource or service model. That model-element already points to
        // its
        // topology, so it will graft in that model's topology.
        // EXCEPT - if this element has "linkage-points" defined, then we need to do some extra
        // processing for how we join to that model and will not try to go any "deeper".
        List<String> linkagePtList = new ArrayList<>();
        Iterator<VertexProperty<Object>> vpI = thisLevelElemVtx.properties("linkage-points");

        // I am not sure why, but since "linkage-points" is an xml-element-wrapper in the OXM
        // definition,
        // we get back the whole array of Strings in one String - but still use the
        // "vtx.properties()" to
        // get it - but only look at the first thing returned by the iterator.
        if (vpI.hasNext()) {
            String tmpLinkageThing = (String) vpI.next().value();
            linkagePtList = makeSureItsAnArrayList(tmpLinkageThing);
        }

        if (linkagePtList != null && !linkagePtList.isEmpty()) {
            // Whatever this element is - we are connecting to it via a linkage-point
            // We will figure out what to do and then return without going any deeper
            String elemFlag = thisLevelElemVtx.<String>property("new-data-del-flag").orElse(null);

            Set<String> linkageConnectNodeTypes = getLinkageConnectNodeTypes(linkagePtList);
            Iterator<?> linkNtIter = linkageConnectNodeTypes.iterator();
            String incTrail = "";
            if (incomingTrail != null && !incomingTrail.equals("")) {
                incTrail = incomingTrail + "|";
            }

            while (linkNtIter.hasNext()) {
                // The 'trail' (or trails) for this element should just be the to the first-contact
                // on the linkage point
                String linkTrail = incTrail + linkNtIter.next();
                Boolean alreadyTaggedFalse = false;
                if (thisHash.containsKey(linkTrail) && thisHash.get(linkTrail).equals("F")) {
                    // some other path with a matching trail has the deleteFlag set to "F", so we do
                    // not want
                    // to override that since our model code only uses nodeTypes to know where it is
                    // - and we
                    // would rather do less deleting than needed instead of too much deleting.
                    alreadyTaggedFalse = true;
                }
                if (elemFlag != null && elemFlag.equals("T") && !alreadyTaggedFalse) {
                    // This trail should be marked with an "T"
                    thisHash.put(linkTrail, "T");
                } else {
                    thisHash.put(linkTrail, "F");
                }
            }
            return thisHash;
        }

        // ----------------------------------------------------------------------------
        // If we got to here, then this was not an element that used a linkage-point
        // ----------------------------------------------------------------------------

        // Find out what widget-model (and thereby what aai-node-type) this element represents.
        // Even if this element is pointing to a service or resource model, it must have a
        // first element which is a single widget-type model.
        String thisElementNodeType = getModElementWidgetType(thisLevelElemVtx, incomingTrail);
        String firstElementModelInfo = "";

        vidsTraversed.add(thisElemVid);
        Vertex elementVtxForThisLevel = null;
        Vertex thisElementsModelVerVtx =
            getModelVerThatElementRepresents(thisLevelElemVtx, incomingTrail);
        Vertex thisElementsModelVtx = getModelGivenModelVer(thisElementsModelVerVtx, incomingTrail);
        String modType = getModelTypeFromModel(thisElementsModelVtx, incomingTrail);
        String subModelFirstModInvId =
            thisElementsModelVtx.<String>property("model-invariant-id").orElse(null);
        String subModelFirstVerId =
            thisElementsModelVerVtx.<String>property("model-version-id").orElse(null);
        if (modType.equals("widget")) {
            if (overRideModelId != null && !overRideModelId.equals("")) {
                // Note - this is just to catch the correct model for the TOP node in a model since
                // it will have an element which will always be a widget even though the model
                // could be a resource or service model.
                firstElementModelInfo = "," + overRideModelId + "," + overRideModelVersionId;
            }
        } else if (nodeTypeSupportsPersona(thisElementNodeType)) {
            firstElementModelInfo = "," + subModelFirstModInvId + "," + subModelFirstVerId;
        }

        if (incomingTrail.equals("")) {
            // This is the first one
            thisGuysTrail = thisElementNodeType + firstElementModelInfo;
        } else {
            thisGuysTrail = incomingTrail + "|" + thisElementNodeType + firstElementModelInfo;
        }

        String tmpFlag = "F";
        Boolean stoppedByASvcOrResourceModelElement = false;
        if (modType.equals("widget")) {
            elementVtxForThisLevel = thisLevelElemVtx;
            // For the element-model for the widget at this level, record it's delete flag
            tmpFlag = elementVtxForThisLevel.<String>property("new-data-del-flag").orElse(null);
        } else {
            // For an element that is referring to a resource or service model, we replace
            // this element with the "top" element for that resource/service model so that the
            // topology of that resource/service model will be included in this topology.
            String modelVerId =
                thisElementsModelVerVtx.<String>property("model-version-id").orElse(null);
            if (subModelFirstModInvId == null || subModelFirstModInvId.equals("")
                || subModelFirstVerId == null || subModelFirstVerId.equals("")) {
                throw new AAIException("AAI_6132",
                    "Bad Model Definition: Bad model-invariant-id or model-version-id.  Model-version-id = "
                        + modelVerId + ", at [" + incomingTrail + "]");
            }

            // BUT -- if the model-element HERE at the resource/service level does NOT have
            // it's new-data-del-flag set to "T", then we do not need to go down into the
            // sub-model looking for delete-able things.

            tmpFlag = thisLevelElemVtx.<String>property("new-data-del-flag").orElse(null);
            elementVtxForThisLevel =
                getTopElementForSvcOrResModelVer(thisElementsModelVerVtx, thisGuysTrail);
            if (tmpFlag != null && tmpFlag.equals("T")) {
                modConstraintHash2Use = getModConstraintHash(thisLevelElemVtx, modConstraintHash);
            } else {
                stoppedByASvcOrResourceModelElement = true;
            }
            // For the element-model for the widget at this level, record it's delete flag
            tmpFlag = elementVtxForThisLevel.<String>property("new-data-del-flag").orElse(null);
        }

        String flag2Use = "F"; // by default we'll use "F" for the delete flag
        if (!stoppedByASvcOrResourceModelElement) {
            // Since we haven't been stopped by a resource/service level "F", we can look at the
            // lower level flag
            if (thisHash.containsKey(thisGuysTrail)) {
                // We've seen this spot in the topology before - do not override the delete flag if
                // the older one is "F"
                // We will only over-ride it if the old one was "T" and the new one is "F" (anything
                // but "T")
                String oldFlag = thisHash.get(thisGuysTrail);
                if (oldFlag.equals("T") && (tmpFlag != null) && tmpFlag.equals("T")) {
                    // The old flag was "T" and the new flag is also "T"
                    flag2Use = "T";
                } else {
                    // the old flag was not "F" - so don't override it
                    flag2Use = "F";
                }
            } else if ((tmpFlag != null) && tmpFlag.equals("T")) {
                // We have not seen this one, so we can set it to "T" if that's what it is.
                flag2Use = "T";
            }
        }

        thisHash.put(thisGuysTrail, flag2Use);
        if (!stoppedByASvcOrResourceModelElement) {
            // Since we haven't been stopped by a resource/service level "F", we will continue to
            // go "down" and look at the elements pointed to so we can get their data.
            Iterator<Vertex> vertI = this.traverseIncidentEdges(EdgeType.TREE,
                elementVtxForThisLevel, "model-element", "constrained-element-set");
            while (vertI != null && vertI.hasNext()) {
                Vertex tmpVert = vertI.next();
                String vid = tmpVert.id().toString();
                Map<String, Object> elementHash = new HashMap<>();

                String connectToType =
                    tmpVert.<String>property(AAIProperties.NODE_TYPE).orElse(null);
                if (connectToType != null && connectToType.equals("model-element")) {
                    // A nice, regular old model-element
                    elementHash.put(vid, tmpVert);
                } else if ((connectToType != null)
                    && connectToType.equals("constrained-element-set")) {
                    // translate the constrained-element-set into a hash of model-element Vertex's
                    String constrainedElementSetUuid =
                        tmpVert.<String>property("constrained-element-set-uuid").orElse(null);
                    if ((modConstraintHash2Use != null)
                        && modConstraintHash2Use.containsKey(constrainedElementSetUuid)) {
                        // This constrained-element-set is being superseded by a different one
                        Vertex replacementConstraintVert =
                            modConstraintHash.get(constrainedElementSetUuid);
                        elementHash = getNextStepElementsFromSet(replacementConstraintVert);
                        // Now that we've found and used the replacement constraint, we don't need
                        // to carry it along any farther
                        modConstraintHash.remove(constrainedElementSetUuid);
                    } else {
                        elementHash = getNextStepElementsFromSet(tmpVert);
                    }
                } else {
                    String msg = " model-element has [connectedTo] edge to improper nodeType= ["
                        + connectToType + "] trail = [" + incomingTrail + "].";
                    throw new AAIException("AAI_6132", msg);
                }

                for (Map.Entry<String, Object> entry : elementHash.entrySet()) {
                    Vertex elVert = (Vertex) (entry.getValue());
                    String tmpElVid = elVert.id().toString();
                    String tmpElNT = getModElementWidgetType(elVert, thisGuysTrail);
                    check4EdgeRule(tmpElNT, thisElementNodeType);
                    if (!vidsTraversed.contains(tmpElVid)) {
                        // This is one we would like to use - so we'll recursively get it's result
                        // set to add to ours
                        Map<String, String> tmpHash = collectDeleteKeyHash(transId, fromAppId,
                            elVert, thisGuysTrail, currentHash, vidsTraversed, levelCounter,
                            modConstraintHash2Use, "", "");
                        thisHash.putAll(tmpHash);
                    }
                }
            }
        }
        return thisHash;

    } // End of collectDeleteKeyHash()

    /**
     * Gets the linkage connect node types.
     *
     * @param linkagePtList the linkage pt list
     * @return the linkage connect node types
     * @throws AAIException the AAI exception
     */
    public Set<String> getLinkageConnectNodeTypes(List<String> linkagePtList) throws AAIException {
        // linkage points are a path from the top of a model to where we link in.
        // This method wants to just bring back a list of distinct last items.
        // Ie: for the input with these two: "pserver|lag-link|l-interface" and
        // "pserver|p-interface|l-interface"
        // it would just return a single item, "l-interface" since both linkage points end in that
        // same node-type.

        Set<String> linkPtSet = new HashSet<>();

        if (linkagePtList == null) {
            String detail = " Bad (null) linkagePtList passed to getLinkageConnectNodeTypes() ";
            throw new AAIException("AAI_6125", detail);
        }

        for (int i = 0; i < linkagePtList.size(); i++) {
            String[] trailSteps = linkagePtList.get(i).split("\\|");
            if (trailSteps == null || trailSteps.length == 0) {
                String detail = " Bad incomingTrail passed to getLinkageConnectNodeTypes(): ["
                    + linkagePtList + "] ";
                throw new AAIException("AAI_6125", detail);
            }
            String lastStepNT = trailSteps[trailSteps.length - 1];
            linkPtSet.add(lastStepNT);
        }

        return linkPtSet;

    }// End getLinkageConnectNodeTypes()

    /**
     * Collect topology for model-ver.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param thisLevelElemVtx vertex to collect for
     * @param incomingTrail the incoming trail -- trail of nodeTypes/personaInfo that got us here
     *        (this vertex) from the top
     * @param currentMap the current map -- map that got us to this point (that we will use as the
     *        base of the map we will return)
     * @param vidsTraversed the vids traversed -- ArrayList of vertexId's that we traversed to get
     *        to this point
     * @param levelCounter the level counter
     * @param modConstraintHash the mod constraint hash
     * @param overRideModelInvId the override model-invariant-id
     * @param overRideModelVersionId the override model-version-id
     * @return Map of the topology
     * @throws AAIException the AAI exception
     */
    public Multimap<String, String> collectTopology4ModelVer(String transId, String fromAppId,
        Vertex thisLevelElemVtx, String incomingTrail, Multimap<String, String> currentMap,
        List<String> vidsTraversed, int levelCounter, Map<String, Vertex> modConstraintHash,
        String overRideModelInvId, String overRideModelVersionId) throws AAIException {

        levelCounter++;

        Multimap<String, String> thisMap = ArrayListMultimap.create();
        thisMap.putAll(currentMap);

        if (levelCounter > MAX_LEVELS) {
            throw new AAIException("AAI_6125",
                "collectTopology4ModelVer() has looped across more levels than allowed: "
                    + MAX_LEVELS + ". ");
        }
        String thisGuysTrail = "";
        String thisElemVid = thisLevelElemVtx.id().toString();
        Map<String, Vertex> modConstraintHash2Use = null;

        // If this element represents a resource or service model, then we will replace this element
        // with
        // the "top" element of that resource or service model. That model-element already points to
        // its
        // topology, so it will graft in that model's topology.
        // EXCEPT - if this element defines "linkage-points" defined, then we need to do some extra
        // processing for how we join to that model.

        // Find out what widget-model (and thereby what aai-node-type) this element represents.
        // Even if this element is pointing to a service or resource model, it must have a
        // first element which is a single widget-type model.
        String firstElementModelInfo = "";
        String thisElementNodeType = getModElementWidgetType(thisLevelElemVtx, incomingTrail);
        if (nodeTypeSupportsPersona(thisElementNodeType) && overRideModelInvId != null
            && !overRideModelInvId.equals("")) {
            firstElementModelInfo = "," + overRideModelInvId + "," + overRideModelVersionId;
        }

        Vertex elementVtxForThisLevel = null;
        Vertex thisElementsModelVerVtx =
            getModelVerThatElementRepresents(thisLevelElemVtx, incomingTrail);
        String subModelFirstModInvId = "";
        String subModelFirstModVerId = "";
        String modInfo4Trail = "";
        String modType = getModelTypeFromModelVer(thisElementsModelVerVtx, incomingTrail);
        if (modType.equals("resource") || modType.equals("service")) {
            // For an element that is referring to a resource or service model, we replace this
            // this element with the "top" element for that resource/service model so that the
            // topology of that resource/service model gets included in this topology.
            // -- Note - since that top element of a service or resource model will point to a
            // widget model,
            // we have to track what modelId/version it really maps so we can make our recursive
            // call
            Vertex thisElementsModelVtx =
                getModelGivenModelVer(thisElementsModelVerVtx, incomingTrail);
            subModelFirstModInvId =
                thisElementsModelVtx.<String>property("model-invariant-id").orElse(null);
            subModelFirstModVerId =
                thisElementsModelVerVtx.<String>property("model-version-id").orElse(null);

            if (nodeTypeSupportsPersona(thisElementNodeType)) {
                modInfo4Trail = "," + subModelFirstModInvId + "," + subModelFirstModVerId;
            }
            String modelVerId =
                thisElementsModelVerVtx.<String>property("model-version-id").orElse(null);
            if (subModelFirstModInvId == null || subModelFirstModInvId.equals("")
                || subModelFirstModVerId == null || subModelFirstModVerId.equals("")) {
                throw new AAIException("AAI_6132",
                    "Bad Model Definition: Bad model-invariant-id or model-version-id.  Model-ver-id = "
                        + modelVerId);
            }

            elementVtxForThisLevel =
                getTopElementForSvcOrResModelVer(thisElementsModelVerVtx, incomingTrail);
            modConstraintHash2Use = getModConstraintHash(thisLevelElemVtx, modConstraintHash);
        } else {
            elementVtxForThisLevel = thisLevelElemVtx;
        }

        if (incomingTrail.equals("")) {
            // This is the first one
            thisGuysTrail = thisElementNodeType + firstElementModelInfo;
        } else {
            thisGuysTrail = incomingTrail + "|" + thisElementNodeType + modInfo4Trail;
        }

        // We only want to ensure that a particular element does not repeat on a single "branch".
        // It could show up on other branches in the case where it is a sub-model which is being
        // used in more than one place.
        //
        List<String> thisTrailsVidsTraversed = new ArrayList<>(vidsTraversed);
        thisTrailsVidsTraversed.add(thisElemVid);

        // Look at the elements pointed to at this level and add on their data
        Iterator<Vertex> vertI = this.traverseIncidentEdges(EdgeType.TREE, elementVtxForThisLevel,
            "model-element", "constrained-element-set");

        while (vertI != null && vertI.hasNext()) {
            Vertex tmpVert = vertI.next();
            String vid = tmpVert.id().toString();
            Map<String, Object> elementHash = new HashMap<>();
            String connectToType = tmpVert.<String>property(AAIProperties.NODE_TYPE).orElse(null);
            if (connectToType != null && connectToType.equals("model-element")) {
                // A nice, regular old model-element
                elementHash.put(vid, tmpVert);
            } else if ((connectToType != null) && connectToType.equals("constrained-element-set")) {
                // translate the constrained-element-set into a hash of model-element Vertex's
                String constrainedElementSetUuid =
                    tmpVert.<String>property("constrained-element-set-uuid").orElse(null);
                if ((modConstraintHash2Use != null)
                    && modConstraintHash2Use.containsKey(constrainedElementSetUuid)) {
                    // This constrained-element-set is being superseded by a different one
                    Vertex replacementConstraintVert =
                        modConstraintHash.get(constrainedElementSetUuid);
                    elementHash = getNextStepElementsFromSet(replacementConstraintVert);
                    // Now that we've found and used the replacement constraint, we don't need to
                    // carry it along any farther
                    modConstraintHash.remove(constrainedElementSetUuid);
                } else {
                    elementHash = getNextStepElementsFromSet(tmpVert);
                }
            } else {
                String msg = " model element has [connectedTo] edge to improper nodeType= ["
                    + connectToType + "] trail = [" + incomingTrail + "].";
                throw new AAIException("AAI_6132", msg);
            }

            for (Map.Entry<String, Object> entry : elementHash.entrySet()) {
                Vertex elVert = (Vertex) (entry.getValue());
                String tmpElVid = elVert.id().toString();
                String tmpElNT = getModElementWidgetType(elVert, thisGuysTrail);
                String tmpElStepName = getModelElementStepName(elVert, thisGuysTrail);

                List<String> linkagePtList = new ArrayList<>();
                Iterator<VertexProperty<Object>> vpI = elVert.properties("linkage-points");

                // I am not sure why, but since "linkage-points" is an xml-element-wrapper in the
                // OXM definition,
                // we get back the whole array of Strings in one String - but still use the
                // "vtx.properties()" to
                // get it - but only look at the first thing returned by the iterator.
                if (vpI.hasNext()) {
                    String tmpLinkageThing = (String) vpI.next().value();
                    linkagePtList = makeSureItsAnArrayList(tmpLinkageThing);
                }

                if (linkagePtList != null && !linkagePtList.isEmpty()) {
                    // This is as far as we can go, we will use the linkage point info to define the
                    // rest of this "trail"
                    for (int i = 0; i < linkagePtList.size(); i++) {
                        Multimap<String, String> tmpMap = collectTopology4LinkagePoint(transId,
                            fromAppId, linkagePtList.get(i), thisGuysTrail, currentMap);
                        thisMap.putAll(tmpMap);
                    }
                } else {
                    check4EdgeRule(tmpElNT, thisElementNodeType);
                    thisMap.put(thisGuysTrail, tmpElStepName);
                    if (!thisTrailsVidsTraversed.contains(tmpElVid)) {
                        // This is one we would like to use - so we'll recursively get it's result
                        // set to add to ours
                        Multimap<String, String> tmpMap = collectTopology4ModelVer(transId,
                            fromAppId, elVert, thisGuysTrail, currentMap, thisTrailsVidsTraversed,
                            levelCounter, modConstraintHash2Use, subModelFirstModInvId,
                            subModelFirstModVerId);
                        thisMap.putAll(tmpMap);
                    } else {
                        String modelElementUuid =
                            elVert.<String>property("model-element-uuid").orElse(null);
                        String msg =
                            "Bad Model Definition: looping model-element (model-element-uuid = ["
                                + modelElementUuid + "]) found trying to add step: ["
                                + tmpElStepName + "], " + " on trail = [" + thisGuysTrail + "]. ";
                        System.out.println(msg);
                        throw new AAIException("AAI_6132", msg);
                    }
                }
            }
        }

        return thisMap;

    } // End of collectTopology4ModelVer()

    /**
     * Check 4 edge rule.
     *
     * @param nodeTypeA the node type A
     * @param nodeTypeB the node type B
     * @throws AAIException the AAI exception
     */
    public void check4EdgeRule(String nodeTypeA, String nodeTypeB) throws AAIException {
        // Throw an exception if there is no defined edge rule for this combination of nodeTypes in
        // DbEdgeRules.

        final EdgeIngestor edgeRules =
            SpringContextAware.getApplicationContext().getBean(EdgeIngestor.class);
        // final EdgeRules edgeRules = EdgeRules.getInstance();

        EdgeRuleQuery.Builder baseQ = new EdgeRuleQuery.Builder(nodeTypeA, nodeTypeB);
        if (!edgeRules.hasRule(baseQ.build())) {

            /*
             * if( !edgeRules.hasEdgeRule(nodeTypeA, nodeTypeB)
             * && !edgeRules.hasEdgeRule(nodeTypeB, nodeTypeA) ){
             */
            // There's no EdgeRule for this -- find out if one of the nodeTypes is invalid or if
            // they are valid, but there's just no edgeRule for them.
            try {
                loader.introspectorFromName(nodeTypeA);
            } catch (AAIUnknownObjectException e) {
                String emsg = " Unrecognized nodeType aa [" + nodeTypeA + "]\n";
                throw new AAIException("AAI_6115", emsg);
            }
            try {
                loader.introspectorFromName(nodeTypeB);
            } catch (AAIUnknownObjectException e) {
                String emsg = " Unrecognized nodeType bb [" + nodeTypeB + "]\n";
                throw new AAIException("AAI_6115", emsg);
            }

            String msg = " No Edge Rule found for this pair of nodeTypes (order does not matter) ["
                + nodeTypeA + "], [" + nodeTypeB + "].";
            throw new AAIException("AAI_6120", msg);
        }

    }

    /**
     * Collect topology 4 linkage point.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param linkagePointStrVal -- Note it is in reverse order from where we connect to it.
     * @param incomingTrail -- trail of nodeTypes that got us here (this vertex) from the top
     * @param currentMap the current map -- that got us to this point (that we will use as the base
     *        of the map we will return)
     * @return Map of the topology
     * @throws AAIException the AAI exception
     */
    public Multimap<String, String> collectTopology4LinkagePoint(String transId, String fromAppId,
        String linkagePointStrVal, String incomingTrail, Multimap<String, String> currentMap)
        throws AAIException {

        Multimap<String, String> thisMap = ArrayListMultimap.create();
        thisMap.putAll(currentMap);
        String thisGuysTrail = incomingTrail;

        // NOTE - "trails" can have multiple parts now since we track persona info for some.
        // We just want to look at the node type info - which would be the piece
        // before any commas (if there are any).

        String[] trailSteps = thisGuysTrail.split("\\|");
        if (trailSteps.length == 0) {
            throw new AAIException("AAI_6125",
                "Bad incomingTrail passed to collectTopology4LinkagePoint(): [" + incomingTrail
                    + "] ");
        }
        String lastStepString = trailSteps[trailSteps.length - 1];
        String[] stepPieces = lastStepString.split(",");
        String lastStepNT = stepPieces[0];

        // It is assumed that the linkagePoint string will be a pipe-delimited string where each
        // piece is an AAIProperties.NODE_TYPE. For now, the first thing to connect to is what is on
        // the farthest right.
        // Example: linkagePoint = "pserver|p-interface|l-interface" would mean that we're
        // connecting to the l-interface
        // but that after that, we connect to a p-interface followed by a pserver.
        // It might have been more clear to define it in the other direction, but for now, that is
        // it. (16-07)
        String linkagePointStr = linkagePointStrVal;

        // We are getting these with more than linkage thing in one string.
        // Ie. "pserver|lag-interface|l-interface, pserver|p-interface|l-interface,
        // vlan|l-interface"
        linkagePointStr = linkagePointStr.replace("[", "");
        linkagePointStr = linkagePointStr.replace("]", "");
        linkagePointStr = linkagePointStr.replace(" ", "");

        String[] linkage = linkagePointStr.split("\\,");
        for (int x = 0; x < linkage.length; x++) {
            lastStepNT = stepPieces[0];
            String thisStepNT = "";
            String[] linkageSteps = linkage[x].split("\\|");
            if (linkageSteps.length == 0) {
                throw new AAIException("AAI_6125",
                    "Bad linkagePointStr passed to collectTopology4LinkagePoint(): ["
                        + linkagePointStr + "] ");
            }
            for (int i = (linkageSteps.length - 1); i >= 0; i--) {
                thisStepNT = linkageSteps[i];
                check4EdgeRule(lastStepNT, thisStepNT);
                thisMap.put(thisGuysTrail, thisStepNT);
                thisGuysTrail = thisGuysTrail + "|" + thisStepNT;
                lastStepNT = thisStepNT;
            }
        }
        return thisMap;

    } // End of collectTopology4LinkagePoint()

    /**
     * Gets the next step elements from set.
     *
     * @param constrElemSetVtx the constr elem set vtx
     * @return Hash of the set of model-elements this set represents
     * @throws AAIException the AAI exception
     */
    public Map<String, Object> getNextStepElementsFromSet(Vertex constrElemSetVtx)
        throws AAIException {
        // Take a constrained-element-set and figure out the total set of all the possible elements
        // that it
        // represents and return them as a Hash.

        Map<String, Object> retElementHash = new HashMap<>();

        if (constrElemSetVtx == null) {
            String msg = " getNextStepElementsFromSet() called with null constrElemSetVtx ";
            throw new AAIException("AAI_6125", msg);
        }

        String constrNodeType =
            constrElemSetVtx.<String>property(AAIProperties.NODE_TYPE).orElse(null);
        String constrElemSetUuid =
            constrElemSetVtx.<String>property("constrained-element-set-uuid").orElse(null);
        if (constrNodeType == null || !constrNodeType.equals("constrained-element-set")) {
            String msg = " getNextStepElementsFromSet() called with wrong type model: ["
                + constrNodeType + "]. ";
            throw new AAIException("AAI_6125", msg);
        }

        ArrayList<Vertex> choiceSetVertArray = new ArrayList<>();
        Iterator<Vertex> vertI =
            this.traverseIncidentEdges(EdgeType.TREE, constrElemSetVtx, "element-choice-set");
        int setCount = 0;
        while (vertI != null && vertI.hasNext()) {
            Vertex choiceSetVertex = vertI.next();
            String constrSetType =
                choiceSetVertex.<String>property(AAIProperties.NODE_TYPE).orElse(null);
            if (constrSetType != null && constrSetType.equals("element-choice-set")) {
                choiceSetVertArray.add(choiceSetVertex);
                setCount++;
            }
        }

        if (setCount == 0) {
            String msg = "No element-choice-set found under constrained-element-set-uuid = "
                + constrElemSetUuid;
            throw new AAIException("AAI_6132", msg);
        }

        // Loop through each choice-set and grab the model-elements
        for (int i = 0; i < setCount; i++) {
            Vertex choiceSetVert = choiceSetVertArray.get(i);
            Iterator<Vertex> mVertI =
                this.traverseIncidentEdges(EdgeType.TREE, choiceSetVert, "model-element");
            int elCount = 0;
            while (mVertI != null && mVertI.hasNext()) {
                Vertex tmpElVertex = mVertI.next();
                String elNodeType =
                    tmpElVertex.<String>property(AAIProperties.NODE_TYPE).orElse(null);
                if (elNodeType != null && elNodeType.equals("model-element")) {
                    String tmpVid = tmpElVertex.id().toString();
                    retElementHash.put(tmpVid, tmpElVertex);
                    elCount++;
                } else {
                    // unsupported node type found for this choice-set
                    String msg = "Unsupported nodeType (" + elNodeType
                        + ") found under choice-set under constrained-element-set-uuid = "
                        + constrElemSetUuid;
                    throw new AAIException("AAI_6132", msg);
                }
            }

            if (elCount == 0) {
                String msg =
                    "No model-elements found in choice-set under constrained-element-set-uuid = "
                        + constrElemSetUuid;
                throw new AAIException("AAI_6132", msg);
            }

        }
        return retElementHash;

    } // End of getNextStepElementsFromSet()

    /**
     * Gen topo map 4 named Q.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param queryVertex the query vertex
     * @param namedQueryUuid the named query uuid
     * @return MultiMap of valid next steps for each potential query-element
     * @throws AAIException the AAI exception
     */
    public Multimap<String, String> genTopoMap4NamedQ(String transId, String fromAppId,
        Vertex queryVertex, String namedQueryUuid) throws AAIException {

        if (queryVertex == null) {
            throw new AAIException("AAI_6125", "null queryVertex passed to genTopoMap4NamedQ()");
        }

        Multimap<String, String> initialEmptyMap = ArrayListMultimap.create();
        List<String> vidsTraversed = new ArrayList<>();

        Vertex firstElementVertex = null;
        Iterator<Vertex> vertI =
            this.traverseIncidentEdges(EdgeType.TREE, queryVertex, "named-query-element");
        int elCount = 0;
        while (vertI != null && vertI.hasNext()) {
            elCount++;
            firstElementVertex = vertI.next();
        }

        if (elCount > 1) {
            throw new AAIException("AAI_6133",
                "Illegal query defined: More than one first element defined for = "
                    + namedQueryUuid);
        }

        if (firstElementVertex == null) {
            throw new AAIException("AAI_6114",
                "Could not find first query element = " + namedQueryUuid);
        }

        Vertex modVtx = getModelThatNqElementRepresents(firstElementVertex, "");
        String modelType = getModelTypeFromModel(modVtx, "");
        if (!modelType.equals("widget")) {
            throw new AAIException("AAI_6133",
                "Bad Named Query Definition: First element must correspond to a widget type model.  Named Query UUID = "
                    + namedQueryUuid);
        }

        return collectTopology4NamedQ(transId, fromAppId, firstElementVertex, "", initialEmptyMap,
            vidsTraversed, 0);
    } // End of genTopoMap4NamedQ()

    /**
     * Collect topology 4 named Q.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param thisLevelElemVtx the model element vertex for this level
     * @param levelCounter the level counter
     * @return resultSet
     * @throws AAIException the AAI exception
     */
    public Multimap<String, String> collectTopology4NamedQ(String transId, String fromAppId,
        Vertex thisLevelElemVtx, String incomingTrail, Multimap<String, String> currentMap,
        List<String> vidsTraversed, int levelCounter) throws AAIException {

        levelCounter++;

        Multimap<String, String> thisMap = ArrayListMultimap.create();
        thisMap.putAll(currentMap);

        String thisElemVid = thisLevelElemVtx.id().toString();
        if (levelCounter > MAX_LEVELS) {
            throw new AAIException("AAI_6125",
                "collectModelStructure() has looped across more levels than allowed: " + MAX_LEVELS
                    + ". ");
        }
        String thisGuysTrail = "";

        // find out what widget (and thereby what aai-node-type) this element represents
        String thisElementNodeType =
            getNqElementWidgetType(transId, fromAppId, thisLevelElemVtx, incomingTrail);

        if (incomingTrail.equals("")) {
            // This is the first one
            thisGuysTrail = thisElementNodeType;
        } else {
            thisGuysTrail = incomingTrail + "|" + thisElementNodeType;
        }

        vidsTraversed.add(thisElemVid);

        // Look at the elements pointed to at this level and add on their data
        Iterator<Vertex> vertI =
            this.traverseIncidentEdges(EdgeType.TREE, thisLevelElemVtx, "named-query-element");
        while (vertI != null && vertI.hasNext()) {
            Vertex tmpVert = vertI.next();
            String tmpVid = tmpVert.id().toString();
            String tmpElNT = getNqElementWidgetType(transId, fromAppId, tmpVert, thisGuysTrail);
            thisMap.put(thisGuysTrail, tmpElNT);
            if (!vidsTraversed.contains(tmpVid)) {
                // This is one we would like to use - so we'll recursively get it's result set to
                // add to ours
                Multimap<String, String> tmpMap = collectTopology4NamedQ(transId, fromAppId,
                    tmpVert, thisGuysTrail, currentMap, vidsTraversed, levelCounter);
                thisMap.putAll(tmpMap);
            }
        }

        return thisMap;

    } // End of collectTopology4NamedQ()

    /**
     * Gets the model that NamedQuery element represents.
     *
     * @param elementVtx the NQ element vtx
     * @param elementTrail the element trail
     * @return the model that element represents
     * @throws AAIException the AAI exception
     */
    public Vertex getModelThatNqElementRepresents(Vertex elementVtx, String elementTrail)
        throws AAIException {

        // Get the model that a named-query element represents
        Vertex modVtx = null;
        Iterator<Vertex> mvertI = this.traverseIncidentEdges(EdgeType.COUSIN, elementVtx, "model");
        int modCount = 0;
        while (mvertI != null && mvertI.hasNext()) {
            modCount++;
            modVtx = mvertI.next();
        }

        if (modCount > 1) {
            String msg =
                "Illegal element defined: More than one model pointed to by a single named-query-element at ["
                    + elementTrail + "].";
            throw new AAIException("AAI_6125", msg);
        }

        if (modVtx == null) {
            String msg = "Bad named-query definition: Could not find model for element. ";
            if (!elementTrail.equals("")) {
                msg =
                    "Bad named-query definition: Could not find model for named-query-element at ["
                        + elementTrail + "].";
            }
            throw new AAIException("AAI_6132", msg);
        }

        String nodeType = modVtx.<String>property(AAIProperties.NODE_TYPE).orElse(null);
        if ((nodeType != null) && nodeType.equals("model")) {
            return modVtx;
        } else {
            String msg =
                "Illegal Named Query element defined: expecting a 'model', but found 'isA' edge pointing to nodeType = "
                    + nodeType + "] at [" + elementTrail + "].";
            throw new AAIException("AAI_6125", msg);
        }

    }// getModelThatNqElementRepresents()

    /**
     * Gets the model-ver that element represents.
     *
     * @param elementVtx the element vtx
     * @param elementTrail the element trail
     * @return the model-ver that element represents
     * @throws AAIException the AAI exception
     */
    public Vertex getModelVerThatElementRepresents(Vertex elementVtx, String elementTrail)
        throws AAIException {

        // Get the model-ver that an element represents
        Vertex modVerVtx = null;
        Iterator<Vertex> mvertI =
            this.traverseIncidentEdges(EdgeType.COUSIN, elementVtx, "model-ver");
        int modCount = 0;
        while (mvertI != null && mvertI.hasNext()) {
            modCount++;
            modVerVtx = mvertI.next();
        }

        if (modCount > 1) {
            String msg =
                "Illegal element defined: More than one model pointed to by a single element at ["
                    + elementTrail + "].";
            throw new AAIException("AAI_6125", msg);
        }

        if (modVerVtx == null) {
            String msg = "Bad model definition: Could not find model-ver for model-element. ";
            if (!elementTrail.equals("")) {
                msg = "Bad model definition: Could not find model-VER for model-element at ["
                    + elementTrail + "].";
            }
            throw new AAIException("AAI_6132", msg);
        }

        String nodeType = modVerVtx.<String>property(AAIProperties.NODE_TYPE).orElse(null);
        if ((nodeType != null) && nodeType.equals("model-ver")) {
            return modVerVtx;
        } else {
            String msg =
                "Illegal model-element defined: expecting a 'model-ver', but found 'isA' edge pointing to nodeType = "
                    + nodeType + "] at [" + elementTrail + "].";
            throw new AAIException("AAI_6125", msg);
        }

    }// getModelVerThatElementRepresents()

    /**
     * Gets the model that is parent to model-ver node.
     *
     * @param modVerVtx the model-ver vtx
     * @param elementTrail the element trail
     * @return the model that element represents
     * @throws AAIException the AAI exception
     */
    public Vertex getModelGivenModelVer(Vertex modVerVtx, String elementTrail) throws AAIException {

        // Get the parent model for this "model-ver" node
        Vertex modVtx = null;
        Iterator<Vertex> mvertI = this.traverseIncidentEdges(EdgeType.TREE, modVerVtx, "model");
        int modCount = 0;
        while (mvertI != null && mvertI.hasNext()) {
            modCount++;
            modVtx = mvertI.next();
        }

        if (modCount > 1) {
            String msg =
                "Illegal model-ver node defined: More than one model points to it with a 'has' edge ["
                    + elementTrail + "].";
            throw new AAIException("AAI_6125", msg);
        }

        if (modVtx == null) {
            String msg = "Bad model-ver node: Could not find parent model. ";
            if (!elementTrail.equals("")) {
                msg = "Bad model-ver node: Could not find parent model. [" + elementTrail + "].";
            }
            throw new AAIException("AAI_6132", msg);
        }

        String nodeType = modVtx.<String>property(AAIProperties.NODE_TYPE).orElse(null);
        if ((nodeType != null) && nodeType.equals("model")) {
            // Found what we were looking for.
            return modVtx;
        } else {
            // Something is amiss
            String msg =
                " Could not find parent model node for model-ver node at [" + elementTrail + "].";
            throw new AAIException("AAI_6125", msg);
        }

    }// getModelGivenModelVer()

    /**
     * Gets the model type.
     *
     * @param modelVtx the model vtx
     * @param elementTrail the element trail
     * @return the model type
     * @throws AAIException the AAI exception
     */
    public String getModelTypeFromModel(Vertex modelVtx, String elementTrail) throws AAIException {

        // Get the model-type from a model vertex
        if (modelVtx == null) {
            String msg = " null modelVtx passed to getModelTypeFromModel() ";
            throw new AAIException("AAI_6114", msg);
        }

        String modelType = modelVtx.<String>property("model-type").orElse(null);
        if ((modelType == null) || modelType.equals("")) {
            String msg =
                "Could not find model-type for model encountered at [" + elementTrail + "].";
            throw new AAIException("AAI_6132", msg);
        }

        if (!modelType.equals("widget") && !modelType.equals("resource")
            && !modelType.equals("service")) {
            String msg = "Unrecognized model-type, [" + modelType
                + "] for model pointed to by element at [" + elementTrail + "].";
            throw new AAIException("AAI_6132", msg);
        }

        return modelType;

    }// getModelTypeFromModel()

    /**
     * Gets the model type given model-ver
     *
     * @param modelVerVtx the model-ver vtx
     * @param elementTrail the element trail
     * @return the model type
     * @throws AAIException the AAI exception
     */
    public String getModelTypeFromModelVer(Vertex modelVerVtx, String elementTrail)
        throws AAIException {

        // Get the model-type given a model-ver vertex
        if (modelVerVtx == null) {
            String msg = " null modelVerVtx passed to getModelTypeFromModelVer() ";
            throw new AAIException("AAI_6114", msg);
        }

        Vertex modVtx = getModelGivenModelVer(modelVerVtx, elementTrail);
        String modelType = modVtx.<String>property("model-type").orElse(null);
        if ((modelType == null) || modelType.equals("")) {
            String msg =
                "Could not find model-type for model encountered at [" + elementTrail + "].";
            throw new AAIException("AAI_6132", msg);
        }

        if (!modelType.equals("widget") && !modelType.equals("resource")
            && !modelType.equals("service")) {
            String msg = "Unrecognized model-type, [" + modelType
                + "] for model pointed to by element at [" + elementTrail + "].";
            throw new AAIException("AAI_6132", msg);
        }

        return modelType;

    }// getModelTypeFromModelVer()

    /**
     * Gets the model-element step name.
     *
     * @param elementVtx the model-element vtx
     * @param elementTrail the element trail
     * @return the element step name
     * @throws AAIException the AAI exception
     */
    public String getModelElementStepName(Vertex elementVtx, String elementTrail)
        throws AAIException {

        // Get the "step name" for a model-element
        // Step names look like this for widget-models: AAIProperties.NODE_TYPE
        // Step names look like this for resource/service models:
        // "aai-node-type,model-invariant-id,model-version-id"
        // NOTE -- if the element points to a resource or service model, then we'll return the
        // widget-type of the first element (crown widget) for that model.
        String thisElementNodeType = "?";
        Vertex modVerVtx = getModelVerThatElementRepresents(elementVtx, elementTrail);
        String modelType = getModelTypeFromModelVer(modVerVtx, elementTrail);

        if (modelType == null) {
            String msg =
                " could not determine modelType in getModelElementStepName().  elementTrail = ["
                    + elementTrail + "].";
            throw new AAIException("AAI_6132", msg);
        }

        if (modelType.equals("widget")) {
            // NOTE: for models that have model-type = "widget", their "model-name" maps directly to
            // aai-node-type
            thisElementNodeType = modVerVtx.<String>property("model-name").orElse(null);
            if ((thisElementNodeType == null) || thisElementNodeType.equals("")) {
                String msg =
                    "Could not find model-name for the widget model pointed to by element at ["
                        + elementTrail + "].";
                throw new AAIException("AAI_6132", msg);
            }
            return thisElementNodeType;
        } else if (modelType.equals("resource") || modelType.equals("service")) {
            Vertex modVtx = getModelGivenModelVer(modVerVtx, elementTrail);
            String modInvId = modVtx.<String>property("model-invariant-id").orElse(null);
            String modVerId = modVerVtx.<String>property("model-version-id").orElse(null);
            Vertex relatedTopElementModelVtx =
                getTopElementForSvcOrResModelVer(modVerVtx, elementTrail);
            Vertex relatedModelVtx =
                getModelVerThatElementRepresents(relatedTopElementModelVtx, elementTrail);
            thisElementNodeType = relatedModelVtx.<String>property("model-name").orElse(null);

            if ((thisElementNodeType == null) || thisElementNodeType.equals("")) {
                String msg =
                    "Could not find model-name for the widget model pointed to by element at ["
                        + elementTrail + "].";
                throw new AAIException("AAI_6132", msg);
            }

            String stepName = "";
            if (nodeTypeSupportsPersona(thisElementNodeType)) {
                // This nodeType that this resource or service model refers to does support
                // persona-related fields, so
                // we will use model-invariant-id and model-version-id as part of the step name.
                stepName = thisElementNodeType + "," + modInvId + "," + modVerId;
            } else {
                stepName = thisElementNodeType;
            }
            return stepName;
        } else {
            String msg = " Unrecognized model-type = [" + modelType + "] pointed to by element at ["
                + elementTrail + "].";
            throw new AAIException("AAI_6132", msg);
        }

    }// getModelElementStepName()

    /**
     * Node type supports persona.
     *
     * @param nodeType the node type
     * @return the boolean
     * @throws AAIException the AAI exception
     */
    public Boolean nodeTypeSupportsPersona(String nodeType) throws AAIException {

        if (nodeType == null || nodeType.equals("")) {
            return false;
        }
        Introspector introspector = null;
        try {
            introspector = loader.introspectorFromName(nodeType);
        } catch (AAIUnknownObjectException e) {
            String emsg = " Unrecognized nodeType [" + nodeType + "]\n";
            throw new AAIException("AAI_6115", emsg);
        }

        Collection<String> props4ThisNT = introspector.getProperties();
        return props4ThisNT.contains(addDBAliasedSuffix("model-invariant-id"))
            && props4ThisNT.contains(addDBAliasedSuffix("model-version-id"));

    }// nodeTypeSupportsPersona()

    /**
     * Gets a Named Query element's widget type.
     *
     * @param elementVtx the named-query element vtx
     * @param elementTrail the element trail
     * @return the element widget type
     * @throws AAIException the AAI exception
     */
    public String getNqElementWidgetType(String transId, String fromAppId, Vertex elementVtx,
        String elementTrail) throws AAIException {

        String thisNqElementWidgetType = "";
        // Get the associated node-type for the model pointed to by a named-query-element.
        // NOTE -- if the element points to a resource or service model, then we'll return the
        // widget-type of the first element (crown widget) for that model.
        Vertex modVtx = getModelThatNqElementRepresents(elementVtx, elementTrail);
        String modelType = getModelTypeFromModel(modVtx, elementTrail);

        if (modelType == null || !modelType.equals("widget")) {
            String emsg = " Model Type must be 'widget' for NamedQuery elements.  Found ["
                + modelType + "] at [" + elementTrail + "]\n";
            throw new AAIException("AAI_6132", emsg);
        } else {
            // For a Widget model, the nodeType is just mapped to the model-element.model-name
            List<Vertex> modVerVtxArr = getModVersUsingModel(transId, fromAppId, modVtx);
            if (modVerVtxArr != null && !modVerVtxArr.isEmpty()) {
                thisNqElementWidgetType =
                    (modVerVtxArr.get(0)).<String>property("model-name").orElse(null);
            }
            if (thisNqElementWidgetType == null || thisNqElementWidgetType.equals("")) {
                String emsg = " Widget type could not be determined at [" + elementTrail + "]\n";
                throw new AAIException("AAI_6132", emsg);
            } else {
                return thisNqElementWidgetType;
            }
        }

    }// End getNqElementWidgetType()

    /**
     * Gets a model-element's top widget type.
     *
     * @param elementVtx the model element vtx
     * @param elementTrail the element trail
     * @return the element widget type
     * @throws AAIException the AAI exception
     */
    public String getModElementWidgetType(Vertex elementVtx, String elementTrail)
        throws AAIException {

        // Get the associated node-type for the model-ver pointed to by a model-element.
        // NOTE -- if the element points to a resource or service model, then we'll return the
        // widget-type of the first element (crown widget) for that model.
        Vertex modVerVtx = getModelVerThatElementRepresents(elementVtx, elementTrail);
        return getModelVerTopWidgetType(modVerVtx, elementTrail);

    }// End getModElementWidgetType()

    /**
     * Gets the node using unique identifier
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param nodeType the nodeType
     * @param idPropertyName the property name of the unique identifier
     * @param uniqueIdVal the UUID value
     * @return unique vertex found using UUID
     * @throws AAIException the AAI exception
     */
    public Vertex getNodeUsingUniqueId(String transId, String fromAppId, String nodeType,
        String idPropertyName, String uniqueIdVal) throws AAIException {

        // Given a unique identifier, get the Vertex
        if (uniqueIdVal == null || uniqueIdVal.equals("")) {
            String emsg =
                " Bad uniqueIdVal passed to getNodeUsingUniqueId(): [" + uniqueIdVal + "]\n";
            throw new AAIException("AAI_6118", emsg);
        }

        if (idPropertyName == null || idPropertyName.equals("")) {
            String emsg =
                " Bad idPropertyName passed to getNodeUsingUniqueId(): [" + idPropertyName + "]\n";
            throw new AAIException("AAI_6118", emsg);
        }

        if (nodeType == null || nodeType.equals("")) {
            String emsg = " Bad nodeType passed to getNodeUsingUniqueId(): [" + nodeType + "]\n";
            throw new AAIException("AAI_6118", emsg);
        }

        Vertex uniqVtx = null;
        Iterable<?> uniqVerts = null;
        uniqVerts = engine.asAdmin().getReadOnlyTraversalSource().V()
            .has(AAIProperties.NODE_TYPE, nodeType).has(idPropertyName, uniqueIdVal).toList();
        if (uniqVerts == null) {
            String emsg =
                "Node could not be found for nodeType = [" + nodeType + "], propertyName = ["
                    + idPropertyName + "], propertyValue = [" + uniqueIdVal + "]\n";
            throw new AAIException("AAI_6114", emsg);
        } else {
            int count = 0;
            Iterator<?> uniqVertsIter = uniqVerts.iterator();
            if (!uniqVertsIter.hasNext()) {
                String emsg =
                    "Node could not be found for nodeType = [" + nodeType + "], propertyName = ["
                        + idPropertyName + "], propertyValue = [" + uniqueIdVal + "]\n";
                throw new AAIException("AAI_6114", emsg);
            } else {
                while (uniqVertsIter.hasNext()) {
                    count++;
                    uniqVtx = (Vertex) uniqVertsIter.next();
                    if (count > 1) {
                        String emsg = "More than one node found for nodeType = [" + nodeType
                            + "], propertyName = [" + idPropertyName + "], propertyValue = ["
                            + uniqueIdVal + "]\n";
                        throw new AAIException("AAI_6132", emsg);
                    }
                }
            }
        }

        return uniqVtx;
    }// End getNodeUsingUniqueId()

    /**
     * Gets the model-ver nodes using name.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param modelName the model name
     * @return the model-ver's that use this name
     * @throws AAIException the AAI exception
     */
    public List<Vertex> getModelVersUsingName(String transId, String fromAppId, String modelName)
        throws AAIException {

        // Given a "model-name", find the model-ver vertices that this maps to
        if (modelName == null || modelName.equals("")) {
            String emsg = " Bad modelName passed to getModelVersUsingName(): [" + modelName + "]\n";
            throw new AAIException("AAI_6118", emsg);
        }

        List<Vertex> retVtxArr = new ArrayList<>();
        Iterator<Vertex> modVertsIter = this.engine.asAdmin().getReadOnlyTraversalSource().V()
            .has(AAIProperties.NODE_TYPE, "model-ver").has("model-name", modelName);
        if (!modVertsIter.hasNext()) {
            String emsg =
                "Model-ver record(s) could not be found for model-ver data passed.  model-name = ["
                    + modelName + "]\n";
            throw new AAIException("AAI_6132", emsg);
        } else {
            while (modVertsIter.hasNext()) {
                Vertex tmpModelVerVtx = modVertsIter.next();
                retVtxArr.add(tmpModelVerVtx);
            }
        }

        return retVtxArr;

    }// End getModelVersUsingName()

    /**
     * Gets the model-ver nodes using model-invariant-id.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param modelInvId-invariant-id (uniquely identifies a model)
     * @return the model-ver's defined for the corresponding model
     * @throws AAIException the AAI exception
     */
    public Iterator<Vertex> getModVersUsingModelInvId(String transId, String fromAppId,
        String modelInvId) throws AAIException {

        // Given a "model-invariant-id", find the model-ver nodes that this maps to
        if (modelInvId == null || modelInvId.equals("")) {
            String emsg = " Bad model-invariant-id passed to getModVersUsingModelInvId(): ["
                + modelInvId + "]\n";
            throw new AAIException("AAI_6118", emsg);
        }

        Vertex modVtx =
            getNodeUsingUniqueId(transId, fromAppId, "model", "model-invariant-id", modelInvId);
        List<Vertex> retVtxArr = getModVersUsingModel(transId, fromAppId, modVtx);
        if (retVtxArr == null || retVtxArr.isEmpty()) {
            String emsg =
                " Model-ver record(s) could not be found attached to model with model-invariant-id = ["
                    + modelInvId + "]\n";
            throw new AAIException("AAI_6132", emsg);
        }

        return retVtxArr.iterator();
    }// End getModVersUsingModelInvId()

    /**
     * Gets the model-ver nodes using a model node.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param modVtx vertex
     * @return the model-ver's defined for the corresponding model
     * @throws AAIException the AAI exception
     */
    public List<Vertex> getModVersUsingModel(String transId, String fromAppId, Vertex modVtx)
        throws AAIException {

        if (modVtx == null) {
            String emsg = " Null model vertex passed to getModVersUsingModel(): ";
            throw new AAIException("AAI_6118", emsg);
        }

        List<Vertex> retVtxArr = new ArrayList<>();
        Iterator<Vertex> modVerVertsIter =
            this.traverseIncidentEdges(EdgeType.TREE, modVtx, "model-ver");
        if (!modVerVertsIter.hasNext()) {
            String modelInvId = modVtx.<String>property("model-invariant-id").orElse(null);
            String emsg =
                "Model-ver record(s) could not be found attached to model with model-invariant-id = ["
                    + modelInvId + "]\n";
            throw new AAIException("AAI_6132", emsg);
        } else {
            while (modVerVertsIter.hasNext()) {
                Vertex tmpModelVtx = modVerVertsIter.next();
                retVtxArr.add(tmpModelVtx);
            }
        }

        return retVtxArr;

    }// End getModVersUsingModel()

    /**
     * Gets the model-version-ids using model-name.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param modelName the model name
     * @return the model uuids using name
     * @throws AAIException the AAI exception
     */
    public List<String> getModelVerIdsUsingName(String transId, String fromAppId, String modelName)
        throws AAIException {

        // Given a model-name find the model-ver nodes that it maps to
        if (modelName == null || modelName.equals("")) {
            String emsg =
                " Bad modelName passed to getModelVerIdsUsingName(): [" + modelName + "]\n";
            throw new AAIException("AAI_6118", emsg);
        }

        List<String> retArr = new ArrayList<>();
        Iterator<Vertex> modVerVertsIter = this.engine.asAdmin().getReadOnlyTraversalSource().V()
            .has(AAIProperties.NODE_TYPE, "model-ver").has("model-name", modelName);
        if (!modVerVertsIter.hasNext()) {
            String emsg =
                " model-ver record(s) could not be found for model data passed.  model-name = ["
                    + modelName + "]\n";
            throw new AAIException("AAI_6114", emsg);
        } else {
            while (modVerVertsIter.hasNext()) {
                Vertex modelVerVtx = modVerVertsIter.next();
                String tmpUuid = modelVerVtx.<String>property("model-version-id").orElse(null);
                if ((tmpUuid != null) && !tmpUuid.equals("") && !retArr.contains(tmpUuid)) {
                    retArr.add(tmpUuid);
                }
            }
        }

        if (retArr.isEmpty()) {
            String emsg = "No model-ver record found for model-name = [" + modelName + "]\n";
            throw new AAIException("AAI_6132", emsg);
        }

        return retArr;
    }// End getModelVerIdsUsingName()

    /**
     * Gets the model top widget type.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param modelVersionId the model-version-id
     * @param modelInvId the model-invariant-id
     * @param modelName the model-name
     * @return the model top widget type
     * @throws AAIException the AAI exception
     */
    public String getModelVerTopWidgetType(String transId, String fromAppId, String modelVersionId,
        String modelInvId, String modelName) throws AAIException {

        // Could be given a model-ver's key info (model-version-id), OR, just a (non-unique)
        // model-name,
        // Or just a model-invariant-id (which could have multiple model-ver records under it).
        // In any case, they should only map to one single "top" node-type for the first element.

        String nodeType = "?";
        Iterator<Vertex> modVerVertsIter;

        if (modelVersionId != null && !modelVersionId.equals("")) {
            // this would be the best - we can just look up the model-ver records directly
            modVerVertsIter = this.engine.asAdmin().getReadOnlyTraversalSource().V()
                .has(AAIProperties.NODE_TYPE, "model-ver").has("model-version-id", modelVersionId);
        } else if (modelName != null && !modelName.equals("")) {
            modVerVertsIter = this.engine.asAdmin().getReadOnlyTraversalSource().V()
                .has(AAIProperties.NODE_TYPE, "model-ver").has("model-name", modelName);
        } else if (modelInvId != null && !modelInvId.equals("")) {
            modVerVertsIter = getModVersUsingModelInvId(transId, fromAppId, modelInvId);
        } else {
            String msg =
                "Neither modelVersionId, modelInvariantId, nor modelName passed to: getModelVerTopWidgetType() ";
            throw new AAIException("AAI_6120", msg);
        }

        if (!modVerVertsIter.hasNext()) {
            String emsg =
                "model-ver record(s) could not be found for model data passed:  modelInvariantId = ["
                    + modelInvId + "], modeVersionId = [" + modelVersionId + "], modelName = ["
                    + modelName + "]\n";
            throw new AAIException("AAI_6114", emsg);
        } else {
            String lastNT = "";
            if (!modVerVertsIter.hasNext()) {
                String emsg =
                    "model-ver record(s) could not be found for model data passed:  modelInvariantId = ["
                        + modelInvId + "], modeVersionId = [" + modelVersionId + "], modelName = ["
                        + modelName + "]\n";
                throw new AAIException("AAI_6114", emsg);
            }
            while (modVerVertsIter.hasNext()) {
                Vertex tmpModVerVtx = modVerVertsIter.next();
                String tmpNT = getModelVerTopWidgetType(tmpModVerVtx, "");
                if (lastNT != null && !lastNT.equals("")) {
                    if (!lastNT.equals(tmpNT)) {
                        String emsg = "Different top-node-types (" + tmpNT + ", " + lastNT
                            + ") found for model data passed.  (" + " modelVersionId = ["
                            + modelVersionId + "], modelId = [" + modelInvId + "], modelName = ["
                            + modelName + "])\n";
                        throw new AAIException("AAI_6114", emsg);
                    }
                }
                lastNT = tmpNT;
                nodeType = tmpNT;
            }
        }

        return nodeType;

    }// End getModelVerTopWidgetType()

    /**
     * Gets the widget type that this model-ver starts with.
     *
     * @param modVerVtx the model-version vtx
     * @param elementTrail the element trail
     * @return the widget type of the starting node of this model
     * @throws AAIException the AAI exception
     */
    public String getModelVerTopWidgetType(Vertex modVerVtx, String elementTrail)
        throws AAIException {
        // Get the associated nodeType (Ie. aai-node-type / widget-type) for a model-ver.
        // NOTE -- if the element points to a resource or service model, then we'll return the
        // widget-type of the first element (crown widget) for that model.
        String modelType = getModelTypeFromModelVer(modVerVtx, elementTrail);
        if (modelType == null) {
            String msg =
                " Could not determine modelType in getModelVerTopWidgetType().  elementTrail = ["
                    + elementTrail + "].";
            throw new AAIException("AAI_6132", msg);
        }

        String thisElementNodeType = "?";
        if (modelType.equals("widget")) {
            // NOTE: for models that have model-type = "widget", their child model-ver nodes will
            // have "model-name" which maps directly to aai-node-type (all model-ver's under one
            // model should start with the same widget-type, so we only need to look at one).
            thisElementNodeType = modVerVtx.<String>property("model-name").orElse(null);
            if ((thisElementNodeType == null) || thisElementNodeType.equals("")) {
                String msg =
                    "Could not find model-name for the widget model pointed to by element at ["
                        + elementTrail + "].";
                throw new AAIException("AAI_6132", msg);
            }
        } else if (modelType.equals("resource") || modelType.equals("service")) {
            Vertex relatedTopElementVtx = getTopElementForSvcOrResModelVer(modVerVtx, elementTrail);
            Vertex relatedModVerVtx =
                getModelVerThatElementRepresents(relatedTopElementVtx, elementTrail);
            thisElementNodeType = relatedModVerVtx.<String>property("model-name").orElse(null);
            if ((thisElementNodeType == null) || thisElementNodeType.equals("")) {
                String msg =
                    "Could not find model-name for the widget model pointed to by element at ["
                        + elementTrail + "].";
                throw new AAIException("AAI_6132", msg);
            }
        } else {
            String msg = " Unrecognized model-type = [" + modelType + "] pointed to by element at ["
                + elementTrail + "].";
            throw new AAIException("AAI_6132", msg);
        }

        return thisElementNodeType;

    }// getModelVerTopWidgetType()

    /**
     * Validate model.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param modelVersionIdVal the model name version id
     * @param apiVersion the api version
     * @throws AAIException the AAI exception
     */
    public void validateModel(String transId, String fromAppId, String modelVersionIdVal,
        String apiVersion) throws AAIException {

        // Note - this will throw an exception if the model either can't be found, or if
        // we can't figure out its topology map.
        Vertex modelVerVtx = getNodeUsingUniqueId(transId, fromAppId, "model-ver",
            "model-version-id", modelVersionIdVal);
        if (modelVerVtx == null) {
            String msg =
                " Could not find model-ver with modelVersionId = [" + modelVersionIdVal + "].";
            throw new AAIException("AAI_6114", msg);
        } else {
            Multimap<String, String> topoMap =
                genTopoMap4ModelVer(transId, fromAppId, modelVerVtx, modelVersionIdVal);
            String msg = " modelVer [" + modelVersionIdVal + "] topo multiMap looks like: \n["
                + topoMap + "]";
            System.out.println("INFO --  " + msg);
        }
    }// End validateModel()

    /**
     * Validate named query.
     *
     * @param transId the trans id
     * @param fromAppId the from app id
     * @param namedQueryUuid the named query uuid
     * @param apiVersion the api version
     * @throws AAIException the AAI exception
     */
    public void validateNamedQuery(String transId, String fromAppId, String namedQueryUuid,
        String apiVersion) throws AAIException {

        // Note - this will throw an exception if the named query either can't be found, or if
        // we can't figure out its topology map.
        Vertex nqVtx = getNodeUsingUniqueId(transId, fromAppId, "named-query", "named-query-uuid",
            namedQueryUuid);

        if (nqVtx == null) {
            String msg =
                " Could not find named-query with namedQueryUuid = [" + namedQueryUuid + "].";
            throw new AAIException("AAI_6114", msg);
        } else {
            // Multimap<String, String> topoMap = genTopoMap4NamedQ( "junkTransId", "junkFromAppId",
            // graph, nqVtx, namedQueryUuid );
            // System.out.println("DEBUG -- for test only : --- ");
            // System.out.println("DEBUG -- topomap = [" + topoMap + "]");
        }

    }// End validateNamedQuery()

    /**
     * Show result set.
     *
     * @param resSet the res set
     * @param levelCount the level count
     */
    public void showResultSet(ResultSet resSet, int levelCount) {

        levelCount++;
        String propsStr = "";
        for (int i = 1; i <= levelCount; i++) {
            propsStr = propsStr + "-";
        }
        if (resSet.getVert() == null) {
            return;
        }
        String nt = resSet.getVert().<String>property(AAIProperties.NODE_TYPE).orElse(null);
        propsStr = propsStr + "[" + nt + "] ";

        // propsStr = propsStr + " newDataDelFlag = " + resSet.getNewDataDelFlag() + ", trail = " +
        // resSet.getLocationInModelSubGraph();
        // propsStr = propsStr + "limitDesc = [" + resSet.getPropertyLimitDesc() + "]";
        propsStr = propsStr + " trail = " + resSet.getLocationInModelSubGraph();

        Map<String, Object> overrideHash = resSet.getPropertyOverRideHash();
        if (overrideHash != null && !overrideHash.isEmpty()) {
            for (Map.Entry<String, Object> entry : overrideHash.entrySet()) {
                String propName = entry.getKey();
                Object propVal = entry.getValue();
                propsStr = propsStr + " [" + propName + " = " + propVal + "]";
            }
        } else {
            Iterator<VertexProperty<Object>> pI = resSet.getVert().properties();
            while (pI.hasNext()) {
                VertexProperty<Object> tp = pI.next();
                if (!tp.key().startsWith("aai") && !tp.key().equals("source-of-truth")
                // && ! tp.key().equals("resource-version")
                    && !tp.key().startsWith("last-mod")) {
                    propsStr = propsStr + " [" + tp.key() + " = " + tp.value() + "]";
                }
            }
        }
        // Show the "extra" lookup values too
        Map<String, Object> extraPropHash = resSet.getExtraPropertyHash();
        if (extraPropHash != null && !extraPropHash.isEmpty()) {
            for (Map.Entry<String, Object> entry : extraPropHash.entrySet()) {
                String propName = entry.getKey();
                Object propVal = entry.getValue();
                propsStr = propsStr + " [" + propName + " = " + propVal.toString() + "]";
            }
        }

        System.out.println(propsStr);
        logger.info(propsStr);

        if (!resSet.getSubResultSet().isEmpty()) {
            ListIterator<ResultSet> listItr = resSet.getSubResultSet().listIterator();
            while (listItr.hasNext()) {
                showResultSet(listItr.next(), levelCount);
            }
        }

    }// end of showResultSet()

    private Iterator<Vertex> traverseIncidentEdges(EdgeType treeType, Vertex startV,
        String connectedNodeType) throws AAIException {
        return this.engine.getQueryBuilder(startV).createEdgeTraversal(treeType, startV,
            loader.introspectorFromName(connectedNodeType));
    }

    private Iterator<Vertex> traverseIncidentEdges(EdgeType treeType, Vertex startV,
        String... connectedNodeType) throws AAIException {
        QueryBuilder[] builders = new QueryBuilder[connectedNodeType.length];
        for (int i = 0; i < connectedNodeType.length; i++) {
            builders[i] = this.engine.getQueryBuilder(startV).createEdgeTraversal(EdgeType.TREE,
                startV, loader.introspectorFromName(connectedNodeType[i]));
        }
        return this.engine.getQueryBuilder(startV).union(builders);
    }

    private String addDBAliasedSuffix(String propName) {
        return propName + AAIProperties.DB_ALIAS_SUFFIX;
    }

    protected String getPropNameWithAliasIfNeeded(String nodeType, String propName)
        throws AAIUnknownObjectException {

        String retPropName = propName;
        if (loader.introspectorFromName(nodeType)
            .getPropertyMetadata(propName, PropertyMetadata.DB_ALIAS).isPresent()) {
            return propName + AAIProperties.DB_ALIAS_SUFFIX;
        }
        return retPropName;
    }

}
