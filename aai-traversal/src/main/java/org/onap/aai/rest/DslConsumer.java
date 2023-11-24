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
package org.onap.aai.rest;

import io.micrometer.core.annotation.Timed;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.onap.aai.concurrent.AaiCallable;
import org.onap.aai.restcore.HttpMethod;
import org.onap.aai.util.TraversalConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Timed
@Path("{version: v[1-9][0-9]*|latest}/dsl")
public class DslConsumer extends TraversalConsumer {

    private DslConsumerService dslConsumerService;

    @Autowired
    public DslConsumer(DslConsumerService dslConsumerService) {
        this.dslConsumerService = dslConsumerService;
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response executeQuery(String content, @PathParam("version") String versionParam,
        @DefaultValue("graphson") @QueryParam("format") String queryFormat,
        @DefaultValue("no_op") @QueryParam("subgraph") String subgraph,
        @DefaultValue("all") @QueryParam("validate") String validate,
        @Context HttpHeaders headers,
        @Context HttpServletRequest req,
        @Context UriInfo info,
        @DefaultValue("-1") @QueryParam("resultIndex") String resultIndex,
        @DefaultValue("-1") @QueryParam("resultSize") String resultSize) {
        Set<String> roles = this.getRoles(req.getUserPrincipal());

        return runner(TraversalConstants.AAI_TRAVERSAL_DSL_TIMEOUT_ENABLED,
            TraversalConstants.AAI_TRAVERSAL_DSL_TIMEOUT_APP,
            TraversalConstants.AAI_TRAVERSAL_DSL_TIMEOUT_LIMIT, headers, info, HttpMethod.PUT,
            new AaiCallable<Response>() {
                @Override
                public Response process() throws Exception {
                    return dslConsumerService.processExecuteQuery(content, req, versionParam, queryFormat, subgraph, validate, headers, info, resultIndex, resultSize, roles);
                    // return (processExecuteQuery(content, req, versionParam, queryFormat, subgraph,
                    //     validate, headers, info, resultIndex, resultSize, roles));
                }
            });
    }


}
