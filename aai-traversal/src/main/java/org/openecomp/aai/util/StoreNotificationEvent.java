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

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import javax.xml.bind.Marshaller;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.json.JSONException;
import org.json.JSONObject;

import org.openecomp.aai.dmaap.AAIDmaapEventJMSProducer;
import org.openecomp.aai.domain.notificationEvent.NotificationEvent;
import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.introspection.Introspector;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.exceptions.AAIUnknownObjectException;

public class StoreNotificationEvent {

	private AAIDmaapEventJMSProducer messageProducer;
	private String fromAppId = "";
	private String transId = "";
	private final String transactionId;
	private final String sourceOfTruth;
	/**
	 * Instantiates a new store notification event.
	 */
	public StoreNotificationEvent(String transactionId, String sourceOfTruth) {
		this.messageProducer = new AAIDmaapEventJMSProducer();
		this.transactionId = transactionId;
		this.sourceOfTruth = sourceOfTruth;
	}

	/**
	 * Store event.
	 *
	 * @param eh
	 *            the eh
	 * @param obj
	 *            the obj
	 * @throws AAIException
	 *             the AAI exception
	 */
	public void storeEvent(NotificationEvent.EventHeader eh, Object obj) throws AAIException {

		if (obj == null) {
			throw new AAIException("AAI_7350");
		}

		org.openecomp.aai.domain.notificationEvent.ObjectFactory factory = new org.openecomp.aai.domain.notificationEvent.ObjectFactory();

		org.openecomp.aai.domain.notificationEvent.NotificationEvent ne = factory.createNotificationEvent();

		if (eh.getId() == null) {
			eh.setId(genDate2() + "-" + UUID.randomUUID().toString());
		}
		if (eh.getTimestamp() == null) {
			eh.setTimestamp(genDate());
		}

		// there's no default, but i think we want to put this in hbase?

		if (eh.getEntityLink() == null) {
			eh.setEntityLink("UNK");
		}

		if (eh.getAction() == null) {
			eh.setAction("UNK");
		}

		if (eh.getEventType() == null) {
			eh.setEventType(AAIConfig.get("aai.notificationEvent.default.eventType", "UNK"));
		}

		if (eh.getDomain() == null) {
			eh.setDomain(AAIConfig.get("aai.notificationEvent.default.domain", "UNK"));
		}

		if (eh.getSourceName() == null) {
			eh.setSourceName(AAIConfig.get("aai.notificationEvent.default.sourceName", "UNK"));
		}

		if (eh.getSequenceNumber() == null) {
			eh.setSequenceNumber(AAIConfig.get("aai.notificationEvent.default.sequenceNumber", "UNK"));
		}

		if (eh.getSeverity() == null) {
			eh.setSeverity(AAIConfig.get("aai.notificationEvent.default.severity", "UNK"));
		}

		if (eh.getVersion() == null) {
			eh.setVersion(AAIConfig.get("aai.notificationEvent.default.version", "UNK"));
		}

		ne.setCambriaPartition(AAIConstants.UEB_PUB_PARTITION_AAI);
		ne.setEventHeader(eh);
		ne.setEntity(obj);

		try {
			PojoUtils pu = new PojoUtils();
			String entityJson = pu.getJsonFromObject(ne);
			sendToDmaapJmsQueue(entityJson);
		} catch (Exception e) {
			throw new AAIException("AAI_7350", e);
		}
	}

	/**
	 * Store dynamic event.
	 *
	 * @param notificationJaxbContext
	 *            the notification jaxb context
	 * @param notificationVersion
	 *            the notification version
	 * @param eventHeader
	 *            the event header
	 * @param obj
	 *            the obj
	 * @throws AAIException
	 *             the AAI exception
	 */
	public void storeDynamicEvent(DynamicJAXBContext notificationJaxbContext, String notificationVersion, DynamicEntity eventHeader, DynamicEntity obj) throws AAIException {

		if (obj == null) {
			throw new AAIException("AAI_7350");
		}

		DynamicEntity notificationEvent = notificationJaxbContext.getDynamicType("inventory.aai.openecomp.org." + notificationVersion + ".NotificationEvent").newDynamicEntity();

		if (eventHeader.get("id") == null) {
			eventHeader.set("id", genDate2() + "-" + UUID.randomUUID().toString());
		}
		
		if (eventHeader.get("timestamp") == null) {
			eventHeader.set("timestamp", genDate());
		}

		if (eventHeader.get("entityLink") == null) {
			eventHeader.set("entityLink", "UNK");
		}

		if (eventHeader.get("action") == null) {
			eventHeader.set("action", "UNK");
		}

		if (eventHeader.get("eventType") == null) {
			eventHeader.set("eventType", AAIConfig.get("aai.notificationEvent.default.eventType", "UNK"));
		}

		if (eventHeader.get("domain") == null) {
			eventHeader.set("domain", AAIConfig.get("aai.notificationEvent.default.domain", "UNK"));
		}

		if (eventHeader.get("sourceName") == null) {
			eventHeader.set("sourceName", AAIConfig.get("aai.notificationEvent.default.sourceName", "UNK"));
		}

		if (eventHeader.get("sequenceNumber") == null) {
			eventHeader.set("sequenceNumber", AAIConfig.get("aai.notificationEvent.default.sequenceNumber", "UNK"));
		}

		if (eventHeader.get("severity") == null) {
			eventHeader.set("severity", AAIConfig.get("aai.notificationEvent.default.severity", "UNK"));
		}

		if (eventHeader.get("version") == null) {
			eventHeader.set("version", AAIConfig.get("aai.notificationEvent.default.version", "UNK"));
		}

		if (notificationEvent.get("cambriaPartition") == null) {
			notificationEvent.set("cambriaPartition", AAIConstants.UEB_PUB_PARTITION_AAI);
		}

		notificationEvent.set("eventHeader", eventHeader);
		notificationEvent.set("entity", obj);

		try {
			StringWriter result = new StringWriter();
			
			Marshaller marshaller = notificationJaxbContext.createMarshaller();
			marshaller.setProperty(org.eclipse.persistence.jaxb.MarshallerProperties.MEDIA_TYPE, "application/json");
			marshaller.setProperty(org.eclipse.persistence.jaxb.MarshallerProperties.JSON_INCLUDE_ROOT, false);
			marshaller.setProperty(org.eclipse.persistence.jaxb.MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, false);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
			marshaller.marshal(notificationEvent, result);
			
			this.sendToDmaapJmsQueue(result.toString());

		} catch (Exception e) {
			throw new AAIException("AAI_7350", e);
		}
	}

	public void storeEvent(Loader loader, Introspector eventHeader, Introspector obj) throws AAIException {
		if (obj == null) {
			throw new AAIException("AAI_7350");
		}

		try {
			final Introspector notificationEvent = loader.introspectorFromName("notification-event");

			if (eventHeader.getValue("id") == null) {
				eventHeader.setValue("id", genDate2() + "-" + UUID.randomUUID().toString());
			}

			if (eventHeader.getValue("timestamp") == null) {
				eventHeader.setValue("timestamp", genDate());
			}

			if (eventHeader.getValue("entity-link") == null) {
				eventHeader.setValue("entity-link", "UNK");
			}

			if (eventHeader.getValue("action") == null) {
				eventHeader.setValue("action", "UNK");
			}

			if (eventHeader.getValue("event-type") == null) {
				eventHeader.setValue("event-type", AAIConfig.get("aai.notificationEvent.default.eventType", "UNK"));
			}

			if (eventHeader.getValue("domain") == null) {
				eventHeader.setValue("domain", AAIConfig.get("aai.notificationEvent.default.domain", "UNK"));
			}

			if (eventHeader.getValue("source-name") == null) {
				eventHeader.setValue("source-name", AAIConfig.get("aai.notificationEvent.default.sourceName", "UNK"));
			}

			if (eventHeader.getValue("sequence-number") == null) {
				eventHeader.setValue("sequence-number", AAIConfig.get("aai.notificationEvent.default.sequenceNumber", "UNK"));
			}

			if (eventHeader.getValue("severity") == null) {
				eventHeader.setValue("severity", AAIConfig.get("aai.notificationEvent.default.severity", "UNK"));
			}

			if (eventHeader.getValue("version") == null) {
				eventHeader.setValue("version", AAIConfig.get("aai.notificationEvent.default.version", "UNK"));
			}

			if (notificationEvent.getValue("cambria-partition") == null) {
				notificationEvent.setValue("cambria-partition", AAIConstants.UEB_PUB_PARTITION_AAI);
			}

			notificationEvent.setValue("event-header", eventHeader.getUnderlyingObject());
			notificationEvent.setValue("entity", obj.getUnderlyingObject());

			String entityJson = notificationEvent.marshal(false);
			sendToDmaapJmsQueue(entityJson);
		} catch (JSONException e) {
			throw new AAIException("AAI_7350", e);
		} catch (AAIUnknownObjectException e) {
			throw new AAIException("AAI_7350", e);
		}
	}

	private void sendToDmaapJmsQueue(String entityString) throws JSONException {
		
		JSONObject entityJsonObject = new JSONObject(entityString);
		
		JSONObject entityJsonObjectUpdated = new JSONObject();
		JSONObject finalJson = new JSONObject();

		JSONObject entityHeader = entityJsonObject.getJSONObject("event-header");
		String cambriaPartition = entityJsonObject.getString("cambria.partition");

		entityJsonObject.remove("event-header");
		entityJsonObject.remove("cambria.partition");

		entityJsonObjectUpdated.put("event-header", entityHeader);
		entityJsonObjectUpdated.put("cambria.partition", cambriaPartition);
		
		Iterator<String> iter = entityJsonObject.keys();
		JSONObject entity = new JSONObject();
		if (iter.hasNext()) {
			entity = entityJsonObject.getJSONObject(iter.next());
		}
		
		entityJsonObjectUpdated.put("entity", entity);

		finalJson.put("event-topic", "AAI-EVENT");
		finalJson.put("transId", transId);
		finalJson.put("fromAppId", fromAppId);
		finalJson.put("fullId", "");
		finalJson.put("aaiEventPayload", entityJsonObjectUpdated);

		messageProducer.sendMessageToDefaultDestination(finalJson);
	}

	/**
	 * Gen date.
	 *
	 * @return the string
	 */
	public static String genDate() {
		Date date = new Date();
		DateFormat formatter = new SimpleDateFormat("YYYYMMdd-HH:mm:ss:SSS");
		return formatter.format(date);
	}

	/**
	 * Gen date 2.
	 *
	 * @return the string
	 */
	public static String genDate2() {
		Date date = new Date();
		DateFormat formatter = new SimpleDateFormat("YYYYMMddHHmmss");
		return formatter.format(date);
	}

}
