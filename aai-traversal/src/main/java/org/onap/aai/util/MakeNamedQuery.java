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
package org.onap.aai.util;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.onap.aai.config.SpringContextAware;
import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.introspection.exceptions.AAIUnknownObjectException;
import org.onap.aai.setup.SchemaVersion;
import org.onap.aai.setup.SchemaVersions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MakeNamedQuery {

    private static final Logger logger = LoggerFactory.getLogger(MakeNamedQuery.class.getName());

    public static void main(String[] args) throws Exception {
        String _apiVersion = AAIConfig.get(AAIConstants.AAI_DEFAULT_API_VERSION_PROP);
        String widgetJsonDir = null;
        String modelVersion = null;
        String namedQueryUuid = UUID.randomUUID().toString();
        if (args.length > 0) {
            if (args[0] != null) {
                _apiVersion = args[0];
            }
            if (args[1] != null) {
                widgetJsonDir = args[1];
            }
            if (args[2] != null) {
                modelVersion = args[2];
            }
            if (args[3] != null) {
                namedQueryUuid = args[3];
            }
        }

        if (widgetJsonDir == null) {
            System.err.println("You must specify a directory for widgetModelJson");
            System.exit(0);
        }
        if (modelVersion == null) {
            System.err.println("You must specify a modelVersion");
            System.exit(0);
        }

        AnnotationConfigApplicationContext ctx =
            new AnnotationConfigApplicationContext("org.onap.aai.config", "org.onap.aai.setup");

        LoaderFactory loaderFactory = ctx.getBean(LoaderFactory.class);
        SchemaVersions schemaVersions = ctx.getBean(SchemaVersions.class);

        if (schemaVersions.getVersions().contains(_apiVersion)) {

            Loader loader = loaderFactory.createLoaderForVersion(ModelType.MOXY,
                new SchemaVersion(_apiVersion));

            // iterate the collection of resources

            ArrayList<String> processedWidgets = new ArrayList<>();

            HashMap<String, List<Introspector>> widgetToRelationship = new HashMap<>();
            for (Entry<String, Introspector> aaiResEnt : loader.getAllObjects().entrySet()) {
                Introspector meObject;
                // no need for a ModelVers DynamicEntity

                Introspector aaiRes = aaiResEnt.getValue();

                if (!(aaiRes.isContainer() || aaiRes.getName().equals("aai-internal"))) {
                    String resource = aaiRes.getName();

                    if (processedWidgets.contains(resource)) {
                        continue;
                    }
                    processedWidgets.add(resource);

                    String widgetName = resource;
                    String filePathString =
                        widgetJsonDir + "/" + widgetName + "-" + modelVersion + ".json";
                    File f = new File(filePathString);
                    if (f.exists()) {
                        System.out.println(f.toString());
                        String json = FileUtils.readFileToString(f, Charset.defaultCharset());

                        meObject = loader.unmarshal("Model", json);
                        String modelInvariantId = meObject.getValue("model-invariant-id");
                        if (meObject.hasProperty("model-vers")) {
                            Introspector modelVers = meObject.getWrappedValue("model-vers");
                            List<Introspector> modelVerList =
                                modelVers.getWrappedListValue("model-ver");
                            for (Introspector modelVer : modelVerList) {

                                List<Introspector> relList = new ArrayList<>();
                                Introspector widgetRelationship =
                                    makeWidgetRelationship(loader, modelInvariantId,
                                        modelVer.getValue("model-version-id").toString());
                                relList.add(widgetRelationship);

                                widgetToRelationship.put(widgetName, relList);
                            }
                        }
                    }
                }
            }

            // source vnf-id, related service-instance-id, all related vnfs in this
            // service-instance-id
            // this should be abstracted and moved to a file

            List<Introspector> genericVnfRelationship = widgetToRelationship.get("generic-vnf");
            List<Introspector> vserverRelationship = widgetToRelationship.get("vserver");
            List<Introspector> tenantRelationship = widgetToRelationship.get("tenant");
            List<Introspector> cloudRegionRelationship = widgetToRelationship.get("cloud-region");
            List<Introspector> esrSystemInfoRelationship =
                widgetToRelationship.get("esr-system-info");

            Introspector namedQueryObj = loader.introspectorFromName("named-query");
            namedQueryObj.setValue("named-query-uuid", namedQueryUuid);
            namedQueryObj.setValue("named-query-name", "vnf-to-esr-system-info");
            namedQueryObj.setValue("named-query-version", "1.0");
            namedQueryObj.setValue("description", "Named Query - VNF to ESR System Info");

            Introspector genericVnfNQE = setupNQElements(namedQueryObj, genericVnfRelationship);

            Introspector vserverNQE = setupNQElements(genericVnfNQE, vserverRelationship);

            Introspector tenantNQE = setupNQElements(vserverNQE, tenantRelationship);

            Introspector cloudRegionNQE = setupNQElements(tenantNQE, cloudRegionRelationship);

            Introspector esrSystemInfoNQE =
                setupNQElements(cloudRegionNQE, esrSystemInfoRelationship);

            System.out.println(namedQueryObj.marshal(true));

        }

        System.exit(0);

    }

    private static Introspector setupNQElements(Introspector nqeObj,
        List<Introspector> listOfRelationships) {
        Introspector newNQElement = null;
        try {
            Introspector newNQElements = null;
            List<Object> nqElementList = null;
            if (nqeObj.getWrappedValue("named-query-elements") != null) {
                newNQElements = nqeObj.getWrappedValue("named-query-elements");
                nqElementList = newNQElements.getValue("named-query-element");
            } else {
                newNQElements = nqeObj.newIntrospectorInstanceOfProperty("named-query-elements");
                nqeObj.setValue("named-query-elements", newNQElements.getUnderlyingObject());
                nqElementList = newNQElements.getValue("named-query-element");
            }
            newNQElement = loadNQElement(newNQElements, listOfRelationships);
            if (newNQElement != null) {
                nqElementList.add(newNQElement.getUnderlyingObject());
            }

        } catch (AAIUnknownObjectException e) {
            logger.info("AAIUnknownObjectException in MakeNamedQuery.setupNQElements() " + e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.info("IllegalArgumentException in MakeNamedQuery.setupNQElements() " + e);
            e.printStackTrace();
        }
        return newNQElement;
    }

    private static Introspector loadNQElement(Introspector nqElements,
        List<Introspector> listOfRelationships) {
        Introspector newNqElement = null;
        try {
            newNqElement = nqElements.getLoader().introspectorFromName("named-query-element");

            // newNqElement.setValue("named-query-element-uuid", UUID.randomUUID().toString());

            Introspector newRelationshipList =
                newNqElement.getLoader().introspectorFromName("relationship-list");
            newNqElement.setValue("relationship-list", newRelationshipList.getUnderlyingObject());

            List<Object> newRelationshipListList = newRelationshipList.getValue("relationship");

            for (Introspector rel : listOfRelationships) {
                newRelationshipListList.add(rel.getUnderlyingObject());
            }

        } catch (AAIUnknownObjectException e) {
            logger.info("AAIUnknownObjectException in MakeNamedQuery.loadNQElement() " + e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.info("IllegalArgumentException in MakeNamedQuery.loadNQElement() " + e);
            e.printStackTrace();
        }
        return newNqElement;

    }

    private static Introspector makeWidgetRelationship(Loader loader, String modelInvariantId,
        String modelVersionId) {

        Introspector newRelationship = null;
        try {
            newRelationship = loader.introspectorFromName("relationship");

            List<Object> newRelationshipData = newRelationship.getValue("relationship-data");

            newRelationship.setValue("related-to", "model");

            Introspector newRelationshipDatum1 =
                newRelationship.getLoader().introspectorFromName("relationship-data");
            Introspector newRelationshipDatum2 =
                newRelationship.getLoader().introspectorFromName("relationship-data");

            newRelationshipDatum1.setValue("relationship-key", "model.model-invariant-id");
            newRelationshipDatum1.setValue("relationship-value", modelInvariantId);

            // newRelationshipDatum2.setValue("relationship-key", "model-ver.model-version-id");
            // newRelationshipDatum2.setValue("relationship-value", modelVersionId);

            newRelationshipData.add(newRelationshipDatum1.getUnderlyingObject());
            // newRelationshipData.add(newRelationshipDatum2.getUnderlyingObject());
        } catch (AAIUnknownObjectException e) {
            logger
                .info("AAIUnknownObjectException in MakeNamedQuery.makeWidgetRelationship() " + e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.info("IllegalArgumentException in MakeNamedQuery.makeWidgetRelationship() " + e);
            e.printStackTrace();
        }

        return newRelationship;
    }

}
