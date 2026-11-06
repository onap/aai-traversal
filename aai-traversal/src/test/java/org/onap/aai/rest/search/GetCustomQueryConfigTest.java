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
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.aai.rest.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;

public class GetCustomQueryConfigTest {

    private String configJson;

    @Before
    public void setUp() throws Exception {
        System.setProperty("AJSC_HOME", ".");
        System.setProperty("BUNDLECONFIG_DIR", "src/main/resources");

        configJson = "{\n	\"stored-queries\": [{\n"
            + "		\"queryName1\": {\n			\"query\": {\n				\"required-properties\": [\"prop1\", \"prop2\"],\n				\"optional-properties\": [\"prop3\", \"prop4\"]\n			},\n			\"stored-query\": \"out('blah').has('something','foo')\"\n		}\n	}, {\n"
            + "		\"queryName2\": {\n			\"query\": {\n				\"optional-properties\": [\"prop5\"]\n			},\n			\"stored-query\": \"out('bar').has('stuff','baz')\"\n		}\n	}, {\n"
            + "		\"queryName3\": {\n			\"stored-query\": \"out('bar1').has('stuff','baz1')\"\n		}\n	}]\n}";
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
        assertEquals(new ArrayList<String>(), cqc.getQueryRequiredProperties());
        assertEquals("out('bar').has('stuff','baz')", cqc.getQuery());

    }

    @Test
    public void testGetStoredQueryNameWithNoProps() {

        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(configJson);
        CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery("queryName3");

        assertEquals(new ArrayList<String>(), cqc.getQueryOptionalProperties());
        assertEquals(new ArrayList<String>(), cqc.getQueryRequiredProperties());
        assertEquals("out('bar1').has('stuff','baz1')", cqc.getQuery());

    }

    @Test
    public void testGetStoredQueryIsThreadSafe() throws Exception {

        GetCustomQueryConfig getCustomQueryConfig = new GetCustomQueryConfig(configJson);
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        try {
            for (int t = 0; t < threads; t++) {
                String queryName = t % 2 == 0 ? "queryName1" : "queryName2";
                String expectedQuery = t % 2 == 0 ? "out('blah').has('something','foo')"
                    : "out('bar').has('stuff','baz')";
                Callable<Boolean> lookup = () -> {
                    start.await();
                    for (int i = 0; i < 20000; i++) {
                        CustomQueryConfig cqc = getCustomQueryConfig.getStoredQuery(queryName);
                        if (cqc == null || !expectedQuery.equals(cqc.getQuery())) {
                            return false;
                        }
                    }
                    return true;
                };
                results.add(executor.submit(lookup));
            }
            start.countDown();
            for (Future<Boolean> result : results) {
                assertTrue(result.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
