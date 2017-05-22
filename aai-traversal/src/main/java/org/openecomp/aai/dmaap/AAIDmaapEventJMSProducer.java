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

package org.openecomp.aai.dmaap;

//import org.apache.activemq.ActiveMQConnectionFactory;
//import org.apache.activemq.command.ActiveMQQueue;
import org.json.JSONObject;
//import org.springframework.jms.connection.CachingConnectionFactory;
//import org.springframework.jms.core.JmsTemplate;

public class AAIDmaapEventJMSProducer {

	//private JmsTemplate jmsTemplate;

	public AAIDmaapEventJMSProducer() {
		//this.jmsTemplate = new JmsTemplate();
		//this.jmsTemplate.setConnectionFactory(new CachingConnectionFactory(new ActiveMQConnectionFactory("tcp://localhost:61446")));
		//this.jmsTemplate.setDefaultDestination(new ActiveMQQueue("IN_QUEUE"));
	}

	public void sendMessageToDefaultDestination(JSONObject finalJson) {
		//jmsTemplate.convertAndSend(finalJson.toString());
		//CachingConnectionFactory ccf = (CachingConnectionFactory)this.jmsTemplate.getConnectionFactory();
		//ccf.destroy();
	}
}
