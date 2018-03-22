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
package org.onap.aai.transforms;

/**
 * <b>LowerHyphenToLowerCamelConverter</b> is the converter to use
 * for converting from the lower hyphen to lower camel case
 * <p>
 * Examples:
 *  lower-test => lowerTest
 *  lower-Test => lowerTest
 *  lowerTest  => lowerTest
 *  lower-test-val => lowerTestVal
 * <p>
 *
 */
public class LowerHyphenToLowerCamelConverter implements Converter {

    /**
     * Converts the dash formatted string into a camel case string
     * Ensure that the capitalization is not lost during this conversion
     * <p>
     * Loops through each character in the string
     * checks if the current character is '-' and if it is then sets the
     * boolean isPreviousCharDash to true and continues to the next iteration
     * If the character is not '-', then checks if the previous character is dash
     * If it is, then it will upper case the current character and appends to the builder
     * Otherwise, it will just append the current character without any modification
     *
     * @param input the input string to convert to camel case
     * @return a string that is converted to camel case
     *          if the input is null, then it returns null
     */
    @Override
    public String convert(String input) {
        if(input == null){
            return null;
        }

        int size = input.length();
        StringBuilder builder = new StringBuilder(size);

        boolean isPreviousCharDash = false;

        for(int index = 0; index < size; ++index){
            char ch = input.charAt(index);

            if(ch == '-'){
                isPreviousCharDash = true;
                continue;
            }
            if(isPreviousCharDash){
                builder.append(Character.toUpperCase(ch));
                isPreviousCharDash = false;
            } else{
                builder.append(ch);
            }
        }

        return builder.toString();
    }

}
