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
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.dbgraphgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.onap.aai.dbgen.PropertyLimitDesc;

public class ResultSet {
	private Vertex vert;
	private String newDataDelFlag;  
	private String doNotOutputFlag;
	private String locationInModelSubGraph;
	private List<ResultSet> subResultSet;
	private PropertyLimitDesc propertyLimitDesc;
	private Map<String,Object> propertyOverRideHash;
	private Map<String,Object> extraPropertyHash;
	
	 /**
 	 * Instantiates a new result set.
 	 */
 	public ResultSet(){
		 this.vert = null;
		 this.newDataDelFlag = "";
		 this.doNotOutputFlag = "";
		 this.locationInModelSubGraph = "";
		 this.subResultSet = new ArrayList<>();
		 this.propertyLimitDesc = null;
		 this.propertyOverRideHash = new HashMap<>();
		 this.extraPropertyHash = new HashMap<>();
	}
	 
 	
 	public void setPropertyLimitDesc(PropertyLimitDesc pld) {
 		this.propertyLimitDesc = pld;
 	}

 	/**
 	 * Gets the vert.
 	 *
 	 * @return the vert
 	 */
 	public Vertex getVert(){
		 return this.vert;
	 }
	 
 	/**
 	 * Gets the sub result set.
 	 *
 	 * @return the sub result set
 	 */
 	public List<ResultSet> getSubResultSet(){
		 return this.subResultSet;
	 }
	 
 	/**
 	 * Gets the new data del flag.
 	 *
 	 * @return the new data del flag
 	 */
 	public String getNewDataDelFlag(){
		 return this.newDataDelFlag;
	 }
	 
 	/**
 	 * Gets the do not output flag.
 	 *
 	 * @return the do not output flag
 	 */
 	public String getDoNotOutputFlag(){
		 return this.doNotOutputFlag;
	 }
	 
 	/**
 	 * Gets the location in model sub graph.
 	 *
 	 * @return the location in model sub graph
 	 */
 	public String getLocationInModelSubGraph(){
		 return this.locationInModelSubGraph;
	 }
	 
 	/**
 	 * Gets the property limit desc.
 	 *
 	 * @return the property limit desc
 	 */
 	public PropertyLimitDesc getPropertyLimitDesc(){
		 return this.propertyLimitDesc;
	 }
	 
 	/**
 	 * Gets the property over ride hash.
 	 *
 	 * @return the property over ride hash
 	 */
 	public Map<String,Object> getPropertyOverRideHash(){
		 return this.propertyOverRideHash;
	 }
	 
 	/**
 	 * Gets the extra property hash.
 	 *
 	 * @return the extra property hash
 	 */
 	public Map<String,Object> getExtraPropertyHash(){
		 return this.extraPropertyHash;
	 }


	public void setVert(Vertex vert) {
		this.vert = vert;
	}


	public void setNewDataDelFlag(String newDataDelFlag) {
		this.newDataDelFlag = newDataDelFlag;
	}


	public void setDoNotOutputFlag(String doNotOutputFlag) {
		this.doNotOutputFlag = doNotOutputFlag;
	}


	public void setLocationInModelSubGraph(String locationInModelSubGraph) {
		this.locationInModelSubGraph = locationInModelSubGraph;
	}


	public void setSubResultSet(List<ResultSet> subResultSet) {
		this.subResultSet = subResultSet;
	}


	public void setPropertyOverRideHash(Map<String, Object> propertyOverRideHash) {
		this.propertyOverRideHash = propertyOverRideHash;
	}


	public void setExtraPropertyHash(Map<String, Object> extraPropertyHash) {
		this.extraPropertyHash = extraPropertyHash;
	}
 	

}
