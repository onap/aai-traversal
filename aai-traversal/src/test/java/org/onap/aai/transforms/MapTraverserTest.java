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
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapTraverserTest {

    private final String testResources = "src/test/resources/maputils/testcases/";

    private String[] testCases = {"TestCase1.json", "TestCase2.json"};
    private MapTraverser traverser = new MapTraverser(new LowerCamelToLowerHyphenConverter());

    @Test
    void testIfMapIsNullThrowNullPointerException() {
        // Test case where the map is null and should throw a NullPointerException
        Map<String, Object> map = null;
        assertThrows(NullPointerException.class, () -> traverser.convertKeys(map));
    }

    @Test
    void runTestCases() throws IOException {
        // Run multiple test cases
        for (String testCase : testCases) {
            Map<String, Object> values = JsonUtils.filepathToMap(testResources + testCase);

            Object input = values.get("input");
            Object actual = traverser.convertKeys((Map<String, Object>) input);
            Object output = values.get("output");
            JoltTestUtil.runArrayOrderObliviousDiffy("failed case " + testCase, output, actual);
        }
    }

    @Test
    void testListWithNestedMaps() {
        // Test case where list contains maps
        Map<String, Object> input = Map.of(
                "key1", List.of(Map.of("nestedKey", "value1"), Map.of("nestedKey", "value2")),
                "key2", "value"
        );

        Map<String, Object> expectedOutput = Map.of(
                "key1", List.of(Map.of("nested-key", "value1"), Map.of("nested-key", "value2")),
                "key2", "value"
        );

        Map<String, Object> actual = traverser.convertKeys(input);
        assertEquals(expectedOutput, actual, "Test failed for list with nested maps.");
    }

    @Test
    void testEmptyList() {
        // Test case for an empty list
        Map<String, Object> input = Map.of(
                "key1", List.of(),
                "key2", "value"
        );

        Map<String, Object> expectedOutput = Map.of(
                "key1", List.of(),
                "key2", "value"
        );

        Map<String, Object> actual = traverser.convertKeys(input);
        assertEquals(expectedOutput, actual, "Test failed for empty list.");
    }

    @Test
    void testListWithPrimitives() {
        // Test case for a list of primitive values
        Map<String, Object> input = Map.of(
                "key1", List.of("string1", "string2", "string3"),
                "key2", "value"
        );

        Map<String, Object> expectedOutput = Map.of(
                "key1", List.of("string1", "string2", "string3"),
                "key2", "value"
        );

        Map<String, Object> actual = traverser.convertKeys(input);
        assertEquals(expectedOutput, actual, "Test failed for list with primitive values.");
    }

    @Test
    void testNullListInMap() {
        // Test case where the list is null inside the map
        Map<String, Object> input = new HashMap<>();
        input.put("key1", null);  // Null list in the map
        input.put("key2", "value");

        Map<String, Object> expectedOutput = new HashMap<>();
        expectedOutput.put("key1", null);  // key1 should remain null
        expectedOutput.put("key2", "value");

        Map<String, Object> actual = traverser.convertKeys(input);
        assertEquals(expectedOutput, actual, "Test failed for null list in map.");
    }
}
