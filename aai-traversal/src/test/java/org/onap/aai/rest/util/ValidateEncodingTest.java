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
package org.onap.aai.rest.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidateEncodingTest {

    @Test
    public void badPath() throws UnsupportedEncodingException {
        String badPath = "/aai/v6/network/vces/vce/blahh::blach/others/other/jklfea{}";
        UriInfo mockUriInfo = getMockUriInfo(badPath, new MultivaluedHashMap<String, String>());
        ValidateEncoding validator = ValidateEncoding.getInstance();

        assertEquals(false, validator.validate(mockUriInfo));
    }

    @Test
    public void goodPath() throws UnsupportedEncodingException {
        String goodPath = "/aai/v6/network/vces/vce/blahh%3A%3Ablach/others/other/jklfea%7B%7D";
        UriInfo mockUriInfo = getMockUriInfo(goodPath, new MultivaluedHashMap<String, String>());
        ValidateEncoding validator = ValidateEncoding.getInstance();

        assertEquals(true, validator.validate(mockUriInfo));
    }

    @Test
    public void badQueryParamsKey() throws UnsupportedEncodingException {
        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<String, String>();
        map.putSingle("blahblah", "test");
        map.putSingle("blahblah", "test2");
        map.putSingle("bad::bad", "test3");
        UriInfo mockUriInfo = getMockUriInfo("", map);

        ValidateEncoding validator = ValidateEncoding.getInstance();

        assertEquals(false, validator.validate(mockUriInfo));

    }

    @Test
    public void badQueryParamsValue() throws UnsupportedEncodingException {
        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<String, String>();
        map.putSingle("blahblah", "test");
        map.putSingle("blahblah", "test//:2");
        map.putSingle("badbad", "test3");
        UriInfo mockUriInfo = getMockUriInfo("", map);

        ValidateEncoding validator = ValidateEncoding.getInstance();

        assertEquals(false, validator.validate(mockUriInfo));
    }

    @Test
    public void goodQueryParams() throws UnsupportedEncodingException {
        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<String, String>();
        map.putSingle("blahblah", "test");
        map.putSingle("blahblah", "test2");
        map.putSingle("badbad", "~test%2F%2F%3A3");
        UriInfo mockUriInfo = getMockUriInfo("", map);

        ValidateEncoding validator = ValidateEncoding.getInstance();

        assertEquals(true, validator.validate(mockUriInfo));
    }

    @Test
    public void testWhenQueryParameterHasPlusSignItShouldPass()
        throws UnsupportedEncodingException {

        MultivaluedHashMap<String, String> map = new MultivaluedHashMap<String, String>();
        map.putSingle("some-key", "test+one+two+three");
        UriInfo mockUriInfo = getMockUriInfo("", map);

        ValidateEncoding validator = ValidateEncoding.getInstance();

        assertEquals(true, validator.validate(mockUriInfo));
    }

    @Test
    public void badUriPath() throws UnsupportedEncodingException {
        // Bad URI with unencoded illegal characters
        String badPath = "/aai/v6/network/vces/vce/blahh::blach/others/other/jklfea{}";

        // Mock UriInfo and avoid creating the URI directly to prevent IllegalArgumentException
        UriInfo mockUriInfo = getMockUriInfo(badPath, new MultivaluedHashMap<String, String>());

        // Use the ValidateEncoding class to check if this path is valid
        ValidateEncoding validator = ValidateEncoding.getInstance();

        // Assert that the path is invalid due to illegal characters
        assertEquals(false,validator.validate(mockUriInfo),"The path should be invalid due to illegal characters");
    }


    @Test
    public void goodUriPath() throws UnsupportedEncodingException {
        // Valid URI path with correctly encoded characters
        URI goodUri = URI.create("http://example.com/aai/v6/network/vces/vce/blahh%3A%3Ablach/others/other/jklfea%7B%7D");
        ValidateEncoding validator = ValidateEncoding.getInstance();

        // This should return true since the URI path is properly encoded
        assertEquals(true, validator.validate(goodUri));
    }

    @Test
    public void emptyUriPath() throws UnsupportedEncodingException {
        // URI with an empty path
        URI emptyUri = URI.create("http://example.com");
        ValidateEncoding validator = ValidateEncoding.getInstance();

        // This should return true, as an empty path is valid
        assertEquals(true, validator.validate(emptyUri));
    }

    private UriInfo getMockUriInfo(String path, MultivaluedMap<String, String> map) {
        UriInfo mockUriInfo = Mockito.mock(UriInfo.class);
        Mockito.when(mockUriInfo.getPath(false)).thenReturn(path);
        Mockito.when(mockUriInfo.getQueryParameters(false)).thenReturn(map);

        return mockUriInfo;
    }

}
