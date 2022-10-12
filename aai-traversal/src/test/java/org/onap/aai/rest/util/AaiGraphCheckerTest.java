/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2022 Bell Canada
 * Modification Copyright (C) 2022 Deutsche Telekom SA
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.aai.rest.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.aai.AAISetup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {AaiGraphChecker.class})
public class AaiGraphCheckerTest extends AAISetup {

  @Autowired
  private AaiGraphChecker graphChecker;

  @Test
  public void testIsAaiGraphDbAvailable() {
    Boolean result = graphChecker.isAaiGraphDbAvailable();

    assertNotNull(result);
    assertTrue(result);
  }
}
