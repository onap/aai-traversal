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

package org.openecomp.aai.rest.ueb;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.openecomp.aai.db.props.AAIProperties;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.introspection.Introspector;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.LoaderFactory;
import org.openecomp.aai.introspection.Version;
import org.openecomp.aai.introspection.exceptions.AAIUnknownObjectException;
import org.openecomp.aai.introspection.exceptions.AAIUnmarshallingException;
import org.openecomp.aai.parsers.uri.URIToObject;
import org.openecomp.aai.util.AAIConfig;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * The Class UEBNotification.
 */
public class UEBNotification {

	private static final EELFLogger LOGGER = EELFManager.getInstance().getLogger(UEBNotification.class);

	private Loader currentVersionLoader = null;
	protected List<NotificationEvent> events = null;
	private Version notificationVersion = null;
	
	/**
	 * Instantiates a new UEB notification.
	 *
	 * @param loader the loader
	 */
	public UEBNotification(Loader loader) {
		events = new ArrayList<>();
		currentVersionLoader = LoaderFactory.createLoaderForVersion(loader.getModelType(), AAIProperties.LATEST);
		notificationVersion = Version.valueOf(AAIConfig.get("aai.notification.current.version","v10"));
	}
	
	
	/**
	 * Creates the notification event.
	 *
	 * @param transactionId the X-TransactionId
	 * @param sourceOfTruth 
	 * @param status the status
	 * @param uri the uri
	 * @param obj the obj
	 * @throws AAIException the AAI exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 */
	public void createNotificationEvent(String transactionId, String sourceOfTruth, Status status, URI uri, Introspector obj, HashMap<String, Introspector> relatedObjects) throws AAIException, UnsupportedEncodingException {
		
		String action = "UPDATE";
		
		if (status.equals(Status.CREATED)) {
			action = "CREATE";
		} else if (status.equals(Status.OK)) {
			action = "UPDATE";
		} else if (status.equals(Status.NO_CONTENT)) {
			action = "DELETE";
		}

		try {
			Introspector eventHeader = currentVersionLoader.introspectorFromName("notification-event-header");
			URIToObject parser = new URIToObject(currentVersionLoader, uri, relatedObjects);

		String entityLink = "";
		if (uri.toString().startsWith("/")) {
			entityLink = "/aai/" + notificationVersion + uri;
		} else {
			entityLink = "/aai/" + notificationVersion + "/" + uri;
		}
		

			eventHeader.setValue("entity-link", entityLink);
			eventHeader.setValue("action", action);
			eventHeader.setValue("entity-type", obj.getDbName());
			eventHeader.setValue("top-entity-type", parser.getTopEntityName());
			eventHeader.setValue("source-name", sourceOfTruth);
			eventHeader.setValue("version", notificationVersion.toString());
			eventHeader.setValue("id", transactionId);

			List<Object> parentList = parser.getParentList();
			parentList.clear();

			if (!parser.getTopEntity().equals(parser.getEntity())) {
				Introspector child = obj;
				if (!parser.getLoader().getVersion().equals(obj.getVersion())) {
					String json = obj.marshal(false);
					child = parser.getLoader().unmarshal(parser.getEntity().getName(), json);
				}

				//wrap the child object in its parents
				parentList.add(child.getUnderlyingObject());
			}

			final Introspector eventObject;

			//convert to most resent version
			if (!parser.getLoader().getVersion().equals(currentVersionLoader.getVersion())) {
				String json = "";
				if (parser.getTopEntity().equals(parser.getEntity())) {
					//convert the parent object passed in
					json = obj.marshal(false);
					eventObject = currentVersionLoader.unmarshal(obj.getName(), json);
				} else {
					//convert the object created in the parser
					json = parser.getTopEntity().marshal(false);
					eventObject = currentVersionLoader.unmarshal(parser.getTopEntity().getName(), json);
				}
			} else {
				if (parser.getTopEntity().equals(parser.getEntity())) {
					//take the top level parent object passed in
					eventObject = obj;
				} else {
					//take the wrapped child objects (ogres are like onions)
					eventObject = parser.getTopEntity();
				}
			}

			final NotificationEvent event = new NotificationEvent(currentVersionLoader, eventHeader, eventObject, transactionId, sourceOfTruth);
			events.add(event);
		} catch (AAIUnknownObjectException e) {
			throw new RuntimeException("Fatal error - notification-event-header object not found!");
		} catch (AAIUnmarshallingException e) {
			LOGGER.error("Unmarshalling error occurred while generating UEBNotification", e);
		}
	}
	
	/**
	 * Trigger events.
	 *
	 * @throws AAIException the AAI exception
	 */
	public void triggerEvents() throws AAIException {
		for (NotificationEvent event : events) {
			event.trigger();
		}
		events.clear();
	}
	
	public List<NotificationEvent> getEvents() {
		return this.events;
	}
	
	

}
