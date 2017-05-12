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

import org.openecomp.aai.exceptions.AAIException;
import org.openecomp.aai.introspection.Introspector;
import org.openecomp.aai.introspection.Loader;
import org.openecomp.aai.introspection.Version;
import org.openecomp.aai.util.StoreNotificationEvent;

/**
 * The Class NotificationEvent.
 */
public class NotificationEvent {

	private Loader loader = null;
	
	private Introspector eventHeader = null;
	
	private Introspector obj = null;
	
	/**
	 * Instantiates a new notification event.
	 *
	 * @param version the version
	 * @param eventHeader the event header
	 * @param obj the obj
	 */
	public NotificationEvent (Loader loader, Introspector eventHeader, Introspector obj) {
		this.loader = loader;
		this.eventHeader = eventHeader;
		this.obj = obj;
	}
	
	/**
	 * Trigger.
	 *
	 * @throws AAIException the AAI exception
	 */
	public void trigger() throws AAIException {
		
		StoreNotificationEvent sne = new StoreNotificationEvent();
		
		sne.storeEvent(loader, eventHeader, obj);

	}
	
	/**
	 * Gets the notification version.
	 *
	 * @return the notification version
	 */
	public Version getNotificationVersion() {
		return loader.getVersion();
	}
	
	/**
	 * Gets the event header.
	 *
	 * @return the event header
	 */
	public Introspector getEventHeader() {
		return eventHeader;
	}
	
	/**
	 * Gets the obj.
	 *
	 * @return the obj
	 */
	public Introspector getObj() {
		return obj;
	}
	
	
	
	
}
