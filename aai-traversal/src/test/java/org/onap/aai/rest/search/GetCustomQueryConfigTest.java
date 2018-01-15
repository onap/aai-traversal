package org.onap.aai.rest.search;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class GetCustomQueryConfigTest {

	private String configJson;
	
	@Before
	public void setUp() throws Exception {
		System.setProperty("AJSC_HOME", ".");
		System.setProperty("BUNDLECONFIG_DIR", "bundleconfig-local");
		

		configJson = "{\n	\"stored-queries\": [{\n" +
				"		\"queryName1\": {\n			\"query\": {\n				\"required-properties\": [\"prop1\", \"prop2\"],\n				\"optional-properties\": [\"prop3\", \"prop4\"]\n			},\n			\"stored-query\": \"out('blah').has('something','foo')\"\n		}\n	}, {\n" +
				"		\"queryName2\": {\n			\"query\": {\n				\"optional-properties\": [\"prop5\"]\n			},\n			\"stored-query\": \"out('bar').has('stuff','baz')\"\n		}\n	}, {\n" +
				"		\"queryName3\": {\n			\"stored-query\": \"out('bar1').has('stuff','baz1')\"\n		}\n	}]\n}";
	}


	@Test
	public void testGetStoredQueryNameWithOptAndReqProps() {
		
		GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(configJson);
		CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName1");

		assertEquals(Lists.newArrayList("prop3", "prop4"), cqc.getQueryOptionalProperties());
		assertEquals(Lists.newArrayList("prop1", "prop2"), cqc.getQueryRequiredProperties());
		assertEquals("out('blah').has('something','foo')", cqc.getQuery());

	}

	@Test
	public void testGetStoredQueryNameWithOptProps() {
		
		GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(configJson);
		CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName2");

		assertEquals(Lists.newArrayList("prop5"), cqc.getQueryOptionalProperties());
		assertEquals(null, cqc.getQueryRequiredProperties());
		assertEquals("out('bar').has('stuff','baz')", cqc.getQuery());

	}

	@Test
	public void testGetStoredQueryNameWithNoProps() {
		
		GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(configJson);
		CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName3");

		assertEquals(null, cqc.getQueryOptionalProperties());
		assertEquals(null, cqc.getQueryRequiredProperties());
		assertEquals("out('bar1').has('stuff','baz1')", cqc.getQuery());

	}
}
