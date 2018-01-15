package org.onap.aai.rest.search;

import java.util.List;

public class CustomQueryConfig {
	public CustomQueryConfig() {
		// used by GetCustomQueryConfig
	}
	
	
	private String query;
	private List<String> queryOptionalProperties;
	private List<String> queryRequiredProperties;
	
	public void setQuery(String query) {
		this.query = query;
	}
	public String getQuery() {
		return this.query;
	}

	public void setQueryOptionalProperties( List<String> queryOptionalProperties) {
		this.queryOptionalProperties = queryOptionalProperties;
	}
	public List<String> getQueryOptionalProperties( ) {
		return queryOptionalProperties;
	}
	public void setQueryRequiredProperties( List<String> queryRequiredProperties) {
		this.queryRequiredProperties = queryRequiredProperties;
	}
	public List<String> getQueryRequiredProperties( ) {
		return queryRequiredProperties;
	}
}
