/*-
 * ============LICENSE_START=======================================================
 * org.openecomp.aai
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.aai.util;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec.OBJECT;
import org.openecomp.aai.introspection.Introspector;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.LoaderFactory;
import org.openecomp.aai.introspection.ModelType;
import org.openecomp.aai.introspection.Version;
import org.openecomp.aai.introspection.exceptions.AAIUnknownObjectException;
import org.openecomp.aai.util.AAIConfig;
import org.openecomp.aai.util.AAIConstants;

public class MakeNamedQuery {

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


		Loader loader = LoaderFactory.createLoaderForVersion(ModelType.MOXY, Version.valueOf(_apiVersion));

		// iterate the collection of resources

		ArrayList<String> processedWidgets = new ArrayList<String>();


		HashMap<String, Introspector> widgetToRelationship = new HashMap<String, Introspector>();
		for (Entry<String, Introspector> aaiResEnt : loader.getAllObjects().entrySet()) {
			Introspector meObject = loader.introspectorFromName("model");
			// no need for a ModelVers DynamicEntity

			Introspector aaiRes = aaiResEnt.getValue();

			if (!(aaiRes.isContainer() || aaiRes.getName().equals("aai-internal"))) {
				String resource = aaiRes.getName();

				if (processedWidgets.contains(resource)) {
					continue;
				}
				processedWidgets.add(resource);

				String widgetName = resource;
				String filePathString = widgetJsonDir + "/" + widgetName + "-" + modelVersion + ".json";
				File f = new File(filePathString);
				if (f.exists()) { 
					System.out.println(f.toString());
					String json = FileUtils.readFileToString(f);

					meObject = loader.unmarshal("Model", json);
					String modelInvariantId = meObject.getValue("model-invariant-id");
					if (meObject.hasProperty("model-vers")) { 
						Introspector modelVers = meObject.getWrappedValue("model-vers");
						List<Introspector> modelVerList = (List<Introspector>) modelVers.getWrappedListValue("model-ver");
						for (Introspector modelVer : modelVerList) {
							widgetToRelationship.put(widgetName, makeWidgetRelationship(loader, modelInvariantId, 
									modelVer.getValue("model-version-id").toString()));
						}
					}
				}
			}
		}
		//source vnf-id, related service-instance-id, all related vnfs in this service-instance-id
		Introspector genericVnfRelationship = widgetToRelationship.get("generic-vnf");
		Introspector serviceInstanceRelationship = widgetToRelationship.get("service-instance");
		Introspector vceRelationship = widgetToRelationship.get("vce");

		Introspector namedQueryObj = loader.introspectorFromName("named-query");
		namedQueryObj.setValue("named-query-uuid", namedQueryUuid);
		namedQueryObj.setValue("named-query-name", "vnf-to-service-instance");
		namedQueryObj.setValue("named-query-version", "1.0");
		namedQueryObj.setValue("description", "Named Query - VNF to Service Instance");
				
		List<Introspector> genericVnfRels = new ArrayList<Introspector>();
		genericVnfRels.add(genericVnfRelationship);
		List<Introspector> serviceInstanceRels = new ArrayList<Introspector>();
		serviceInstanceRels.add(serviceInstanceRelationship);
		List<Introspector> vceRels = new ArrayList<Introspector>();
		vceRels.add(vceRelationship);

		 
		Introspector genericVnfNQE = setupNQElements(namedQueryObj, genericVnfRels);
				
		//Introspector vceNQE2 = setupNQElements(namedQueryObj, vceRels);
		
		Introspector serviceInstanceNQE = setupNQElements(genericVnfNQE, serviceInstanceRels);
		
		Introspector newGenericVnfNQE = setupNQElements(serviceInstanceNQE, genericVnfRels);
		
		System.out.println(namedQueryObj.marshal(true));
		
		System.exit(0);

	}	

	private static Introspector setupNQElements (Introspector nqeObj, List<Introspector> listOfRelationships) {
		Introspector newNQElement = null;
		try {
			Introspector newNQElements = null;
			List<Object> nqElementList = null;
			if (nqeObj.getWrappedValue("named-query-elements") != null) {
				newNQElements = nqeObj.getWrappedValue("named-query-elements");
				nqElementList = newNQElements.getValue("named-query-element");
			} else { 
				newNQElements = nqeObj.newIntrospectorInstanceOfProperty("named-query-elements");
				nqeObj.setValue("named-query-elements",  newNQElements.getUnderlyingObject());
				nqElementList = (List<Object>)newNQElements.getValue("named-query-element");
			}
			newNQElement = loadNQElement(newNQElements, listOfRelationships);
			nqElementList.add(newNQElement.getUnderlyingObject());
		
		} catch (AAIUnknownObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newNQElement;
	}
	
	private static Introspector loadNQElement (Introspector nqElements, List<Introspector> listOfRelationships) {
		Introspector newNqElement = null;
		try {
			newNqElement = nqElements.getLoader().introspectorFromName("named-query-element");
				
			//newNqElement.setValue("named-query-element-uuid", UUID.randomUUID().toString());

			Introspector newRelationshipList = newNqElement.getLoader().introspectorFromName("relationship-list");
			newNqElement.setValue("relationship-list", newRelationshipList.getUnderlyingObject());

			List<Object> newRelationshipListList = (List<Object>)newRelationshipList.getValue("relationship");

			for (Introspector rel : listOfRelationships) { 
				newRelationshipListList.add(rel.getUnderlyingObject());
			}
			
		} catch (AAIUnknownObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newNqElement;

	}
	private static Introspector makeWidgetRelationship(Loader loader, String modelInvariantId, String modelVersionId) {

		Introspector newRelationship = null;
		try {
			newRelationship = loader.introspectorFromName("relationship");

			List<Object> newRelationshipData = (List<Object>)newRelationship.getValue("relationship-data");

			newRelationship.setValue("related-to", "model");

			Introspector newRelationshipDatum1 = newRelationship.getLoader().introspectorFromName("relationship-data");
			Introspector newRelationshipDatum2 = newRelationship.getLoader().introspectorFromName("relationship-data");


			newRelationshipDatum1.setValue("relationship-key", "model.model-invariant-id");
			newRelationshipDatum1.setValue("relationship-value", modelInvariantId);

			//newRelationshipDatum2.setValue("relationship-key", "model-ver.model-version-id");
			//newRelationshipDatum2.setValue("relationship-value", modelVersionId);

			newRelationshipData.add(newRelationshipDatum1.getUnderlyingObject());
			//newRelationshipData.add(newRelationshipDatum2.getUnderlyingObject());
		} catch (AAIUnknownObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return newRelationship;
	}

}
