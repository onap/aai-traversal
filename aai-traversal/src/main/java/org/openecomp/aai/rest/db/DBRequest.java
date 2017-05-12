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

package org.openecomp.aai.rest.db;

import java.net.URI;
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.openecomp.aai.introspection.Introspector;
import org.openecomp.aai.introspection.MarshallerProperties;
import org.openecomp.aai.parsers.query.QueryParser;
import org.openecomp.aai.restcore.HttpMethod;

/**
 * The Class DBRequest.
 */
public class DBRequest {

	private final QueryParser parser;
	
	private final Introspector introspector;
	
	private final HttpHeaders headers;
	
	private final String transactionId;
	
	private final UriInfo info;
	
	private final HttpMethod method;
	
	private final URI uri;
	
	private final Optional<String> rawRequestContent;
	
	private final Optional<MarshallerProperties> marshallerProperties;

	
	/**
	 * Instantiates a new DB request.
	 *
	 * @param method the method
	 * @param uri the uri
	 * @param parser the parser
	 * @param obj the obj
	 * @param headers the headers
	 * @param info the info
	 * @param transactionId the transaction id
	 */
	private DBRequest(Builder builder) {
		this.method = builder.getMethod();
		this.parser = builder.getParser();
		this.introspector = builder.getIntrospector();
		this.headers = builder.getHeaders();
		this.transactionId = builder.getTransactionId();
		this.info = builder.getInfo();
		this.uri = builder.getUri();
		this.marshallerProperties = builder.getMarshallerProperties();
		this.rawRequestContent = builder.getRawRequestContent();
	}
	
	/**
	 * Gets the headers.
	 *
	 * @return the headers
	 */
	public HttpHeaders getHeaders() {
		return headers;
	}
	
	
	/**
	 * Gets the transaction id.
	 *
	 * @return the transaction id
	 */
	public String getTransactionId() {
		return transactionId;
	}
	
	/**
	 * Gets the info.
	 *
	 * @return the info
	 */
	public UriInfo getInfo() {
		return info;
	}
	
	/**
	 * Gets the parser.
	 *
	 * @return the parser
	 */
	public QueryParser getParser() {
		return parser;
	}

	/**
	 * Gets the introspector.
	 *
	 * @return the introspector
	 */
	public Introspector getIntrospector() {
		return introspector;
	}

	/**
	 * Gets the method.
	 *
	 * @return the method
	 */
	public HttpMethod getMethod() {
		return method;
	}

	/**
	 * Gets the uri.
	 *
	 * @return the uri
	 */
	public URI getUri() {
		return uri;
	}
	
	/**
	 * Gets the raw content.
	 *
	 * @return the raw content
	 */
	public Optional<String> getRawRequestContent() {
		return rawRequestContent;
	}
	
	public Optional<MarshallerProperties> getMarshallerProperties() {
		return marshallerProperties;
	}



	public static class Builder {

		private QueryParser parser = null;
		
		private Introspector introspector = null;
		
		private HttpHeaders headers = null;
		
		private String transactionId = null;
		
		private UriInfo info = null;
		
		private HttpMethod method = null;
		
		private URI uri = null;
				
		private Optional<MarshallerProperties> marshallerProperties = Optional.empty();
		
		private Optional<String> rawRequestContent = Optional.empty();
		/**
		 * Instantiates a new DB request.
		 *
		 * @param method the method
		 * @param uri the uri
		 * @param parser the parser
		 * @param obj the obj
		 * @param headers the headers
		 * @param info the info
		 * @param transactionId the transaction id
		 */
		public Builder(HttpMethod method, URI uri, QueryParser parser, Introspector obj, HttpHeaders headers, UriInfo info, String transactionId) {
			this.method = method;
			this.parser = parser;
			this.introspector = obj;
			this.headers = headers;
			this.transactionId = transactionId;
			this.info = info;
			this.uri = uri;
			
		}

		public QueryParser getParser() {
			return parser;
		}

		public Introspector getIntrospector() {
			return introspector;
		}

		public HttpHeaders getHeaders() {
			return headers;
		}

		public String getTransactionId() {
			return transactionId;
		}

		public UriInfo getInfo() {
			return info;
		}

		public HttpMethod getMethod() {
			return method;
		}

		public URI getUri() {
			return uri;
		}
		
		public Builder customMarshaller(MarshallerProperties properties) {
			this.marshallerProperties = Optional.of(properties);
			return this;
		}
		
		public Builder rawRequestContent(String content) {
			this.rawRequestContent = Optional.of(content);
			return this;
		}
		protected Optional<MarshallerProperties> getMarshallerProperties() {
			return marshallerProperties;
		}
		protected Optional<String> getRawRequestContent() {
			return rawRequestContent;
		}
		public DBRequest build() {
			
			return new DBRequest(this);
		}
		
		
	}
}
