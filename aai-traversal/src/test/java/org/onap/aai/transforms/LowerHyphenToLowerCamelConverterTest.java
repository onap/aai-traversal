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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LowerHyphenToLowerCamelConverterTest {

    private Converter converter = new LowerHyphenToLowerCamelConverter();

    private String input;
    private String expected;

    public LowerHyphenToLowerCamelConverterTest(String input, String expected) {
        this.input = input;
        this.expected = expected;
    }

    /**
     * Data Provider for the Lower Hyphen to Camel Converter Tests
     * Make sure the capitalization is not lost during the conversion
     * 
     * @return
     */
    @Parameters
    public static Collection<Object[]> data() {

        return Arrays.asList(
            new Object[][] {{null, null}, {"test-name", "testName"}, {"test---name", "testName"}, // Case
                                                                                                  // multiple
                {"testName", "testName"}, // Case where upper case word shouldn't be lowercased
                {"test-name-cool", "testNameCool"}, {"test-name-Cool", "testNameCool"},
                {"test-name-Cool-Name-wow----Rest", "testNameCoolNameWowRest"},
                {"test-name#fast#", "testName#fast#"}, {"test-name---", "testName"},
                {"----test-name", "TestName"},});
    }

    @Test
    public void testIfInputSuccessfullyModified() {
        String actual = converter.convert(input);
        assertEquals(expected, actual);
    }
}
