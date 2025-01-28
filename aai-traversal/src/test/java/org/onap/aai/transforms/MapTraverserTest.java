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
package org.onap.aai.transforms;

import com.bazaarvoice.jolt.JsonUtils;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;

public class MapTraverserTest {

    private final String testResources = "src/test/resources/maputils/testcases/";

    private String[] testCases = {"TestCase1.json", "TestCase2.json"};
    private MapTraverser traverser = new MapTraverser(new LowerCamelToLowerHyphenConverter());

    @Test(expected = NullPointerException.class)
    public void testIfMapIsNullThrowNullPointerException() {
        Map<String, Object> map = null;
        traverser.convertKeys(map);
    }

    @Test
    public void runTestCases() throws IOException {

        for (String testCase : testCases) {
            Map<String, Object> values = JsonUtils.filepathToMap(testResources + testCase);

            Object input = values.get("input");
            Object actual = traverser.convertKeys((Map<String, Object>) input);
            Object output = values.get("output");
            JoltTestUtil.runArrayOrderObliviousDiffy("failed case " + testCase, output, actual);
        }
    }
}
