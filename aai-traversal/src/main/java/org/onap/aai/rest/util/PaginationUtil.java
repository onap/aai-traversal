/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2024 Deutsche Telekom. All rights reserved.
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
package org.onap.aai.rest.util;

import java.util.Collections;
import java.util.List;

import org.onap.aai.exceptions.AAIException;
import org.onap.aai.query.builder.Pageable;

public class PaginationUtil {

    public static List<Object> getPaginatedVertexListForAggregateFormat(List<Object> aggregateVertexList, Pageable pageable) throws AAIException {
        if (aggregateVertexList != null && !aggregateVertexList.isEmpty() && aggregateVertexList.size() == 1) {
            List<Object> vertexList = (List<Object>) aggregateVertexList.get(0);
            int fromIndex = pageable.getPage() * pageable.getPageSize();
            List<Object> page = vertexList.subList(fromIndex, fromIndex + pageable.getPageSize());
            return Collections.singletonList(page);
        }
        // If the list size is greater than 1 or if pagination is not needed, return the original list.
        return aggregateVertexList;
    }

    public static boolean hasValidPaginationParams(Pageable pageable) {
      return pageable.getPage() >= 0 && pageable.getPageSize() > 0;
    }

    public static long getTotalPages(Pageable pageable, long totalCount) {
        int pageSize = pageable.getPageSize();
        long totalPages = totalCount / pageSize;
        // conditionally add a page for the remainder
        if (totalCount % pageSize > 0) {
            totalPages++;
        }
        return totalPages;
    }
}
