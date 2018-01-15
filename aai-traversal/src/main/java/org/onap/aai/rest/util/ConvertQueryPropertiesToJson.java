package org.onap.aai.rest.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


import org.onap.aai.util.AAIConstants;

public class ConvertQueryPropertiesToJson {
	
	private final static int maxfilesize = 256000;
	
	private void addStart( StringBuilder sb ) {
		sb.append("{\n  \"stored-queries\":[{\n");
	}
	
	private void addRequiredQueryProperties( StringBuilder sb, List<String> rqd ) {
		Iterator it = rqd.iterator();
		sb.append("      \"query\":{\n        \"required-properties\":[");
		while( it.hasNext()) {
			sb.append("\"" + it.next() + "\"");
			if ( it.hasNext()) {
				sb.append(",");
			}
		}
		sb.append("]\n      },\n");
	}
	
	private void addAnotherQuery( StringBuilder sb, String queryName, String query, List<String> rqd ) {
		sb.append("    \"" + queryName + "\":{\n");
		if ( !rqd.isEmpty()) {
		      addRequiredQueryProperties( sb, rqd);
		}
		sb.append("      \"stored-query\":\"" + query + "\"\n    }\n  },{\n");
	}
	
	private void addLastQuery( StringBuilder sb, String queryName, String query, List<String> rqd ) {
		sb.append("    \"" + queryName + "\":{\n");
		if ( !rqd.isEmpty() ) {
		      addRequiredQueryProperties( sb, rqd);
		}
		sb.append("      \"stored-query\":\"" + query + "\"\n    }\n  }]\n}\n");
	}
	
	private String get2ndParameter( String paramString) {
		String endParams = paramString.substring(0, paramString.indexOf(')'));
		String result = endParams.substring(endParams.indexOf(',') + 1 );
		String lastParam = result.trim();
		if ( lastParam.startsWith("\\") || lastParam.startsWith("'") || lastParam.startsWith("new ") ){
			return null;
		}
		
		return lastParam;
	}
	
	private List<String> findRqdProperties( String query) {
		String[] parts = query.split("getVerticesByProperty");
		List<String> result = new ArrayList<String>();
		if ( parts.length == 1 )
			return result;
		int count = 0;
		String foundRqdProperty;
		while ( count++ < parts.length - 1 ) {
			foundRqdProperty = get2ndParameter(parts[count]);
			if ( foundRqdProperty != null  && !result.contains(foundRqdProperty)) {
				result.add(foundRqdProperty);
			}
		}
		return result;
	}

	public  String convertProperties( Properties props ) {
		Enumeration<?> e = props.propertyNames();
		StringBuilder sb = new StringBuilder(maxfilesize);
		String queryName;
		String query;
		addStart( sb );
		List<String> rqd;
		while ( e.hasMoreElements()) {
			queryName = (String)e.nextElement();
			query = props.getProperty(queryName).trim().replace("\"", "\\\"");
			rqd = findRqdProperties(  query);
			if ( e.hasMoreElements()) {
				addAnotherQuery( sb, queryName, query, rqd);
			} else {
				addLastQuery( sb, queryName, query, rqd);
			}
		}
		
        return sb.toString();
	}
	public static void main(String[] args) {
		File queryFile = new File(AAIConstants.AAI_HOME_ETC_QUERY);
		Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(queryFile)){
            properties.load(fis);
        } catch (IOException e) {
        	e.printStackTrace();
            System.out.println("Error occurred during the processing of query file: " + e);
        }
        ConvertQueryPropertiesToJson c = new ConvertQueryPropertiesToJson();
        String json = c.convertProperties(properties);
        System.out.println("returned json:\n" + json);
	}

}
