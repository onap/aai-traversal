/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.util;
import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import org.onap.aai.introspection.Introspector;
import org.onap.aai.introspection.Loader;
import org.onap.aai.introspection.LoaderFactory;
import org.onap.aai.introspection.ModelType;
import org.onap.aai.introspection.Version;
import org.onap.aai.introspection.exceptions.AAIUnknownObjectException;
import org.onap.aai.util.AAIConfig;
import org.onap.aai.util.AAIConstants;

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

		ArrayList<String> processedWidgets = new ArrayList<>();


		HashMap<String, List<Introspector>> widgetToRelationship = new HashMap<String, List<Introspector>>();
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
							
							List<Introspector> relList = new ArrayList<Introspector>();
							Introspector widgetRelationship = makeWidgetRelationship(loader, modelInvariantId, 
									modelVer.getValue("model-version-id").toString());
							relList.add(widgetRelationship);
														
							widgetToRelationship.put(widgetName, relList);
						}
					}
				}
			}
		}
		
//		esr-system-info-from-vnf=builder.store('x').union(\
//                builder.newInstance().createEdgeTraversal(EdgeType.COUSIN, 'generic-vnf', 'vserver').store('x').union(\
//                        builder.newInstance().createEdgeTraversal(EdgeType.TREE, 'vserver', 'tenant').store('x')\
//                        .createEdgeTraversal(EdgeType.TREE, 'tenant', 'cloud-region').store('x')\
//                        .createEdgeTraversal(EdgeType.TREE, 'cloud-region', 'esr-system-info').store('x')\
//                )).cap('x').unfold.dedup()
 
		//source vnf-id, related service-instance-id, all related vnfs in this service-instance-id
		
		//this should be abstracted and moved to a file
		
		HashMap<String, List<Introspector>> relationshipMap = new HashMap<String, List<Introspector>>();
				
		List<Introspector> genericVnfRelationship = widgetToRelationship.get("generic-vnf");
		List<Introspector> vserverRelationship = widgetToRelationship.get("vserver");
		List<Introspector> tenantRelationship = widgetToRelationship.get("tenant");
		List<Introspector> cloudRegionRelationship = widgetToRelationship.get("cloud-region");
		List<Introspector> esrSystemInfoRelationship = widgetToRelationship.get("esr-system-info");

		Introspector namedQueryObj = loader.introspectorFromName("named-query");
		namedQueryObj.setValue("named-query-uuid", namedQueryUuid);
		namedQueryObj.setValue("named-query-name", "vnf-to-esr-system-info");
		namedQueryObj.setValue("named-query-version", "1.0");
		namedQueryObj.setValue("description", "Named Query - VNF to ESR System Info");
					 
		Introspector genericVnfNQE = setupNQElements(namedQueryObj, genericVnfRelationship);
				
		Introspector vserverNQE = setupNQElements(genericVnfNQE, vserverRelationship);
		
		Introspector tenantNQE = setupNQElements(vserverNQE, tenantRelationship);
		
		Introspector cloudRegionNQE = setupNQElements(tenantNQE, cloudRegionRelationship);

		Introspector esrSystemInfoNQE = setupNQElements(cloudRegionNQE, esrSystemInfoRelationship);
		
		System.out.println(namedQueryObj.marshal(true));
		
		System.exit(0);

	}	
	private static List<Introspector> getRels(String widgetName, HashMap<String, Introspector> widgetToRelationship) {
		List<Introspector> relList = new ArrayList<Introspector>();
		Introspector genericVnfRelationship = widgetToRelationship.get(widgetName);
		relList.add(genericVnfRelationship);
		return relList;
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