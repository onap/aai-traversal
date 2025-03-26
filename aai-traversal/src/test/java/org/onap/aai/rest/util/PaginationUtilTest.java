package org.onap.aai.rest.util;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import org.onap.aai.query.builder.Pageable;
import org.onap.aai.exceptions.AAIException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PaginationUtilTest {

    @Test
    public void testGetPaginatedVertexListForAggregateFormat() throws AAIException {
        Pageable pageable = Mockito.mock(Pageable.class);
        Mockito.when(pageable.getPage()).thenReturn(0);
        Mockito.when(pageable.getPageSize()).thenReturn(2);

        List<Object> vertexList = Arrays.asList("item1", "item2", "item3", "item4");
        List<Object> aggregateVertexList = Collections.singletonList(vertexList);

        List<Object> paginatedResult = PaginationUtil.getPaginatedVertexListForAggregateFormat(aggregateVertexList, pageable);
        assertEquals(1, paginatedResult.size());
        List<Object> page = (List<Object>) paginatedResult.get(0);
        assertEquals(2, page.size());
        assertEquals("item1", page.get(0));
        assertEquals("item2", page.get(1));
    }

    @Test
    public void testGetPaginatedVertexListForAggregateFormatWithMultipleLists() throws AAIException {
        Pageable pageable = Mockito.mock(Pageable.class);
        Mockito.when(pageable.getPage()).thenReturn(0);
        Mockito.when(pageable.getPageSize()).thenReturn(2);

        List<Object> vertexList1 = Arrays.asList("item1", "item2");
        List<Object> vertexList2 = Arrays.asList("item3", "item4");
        List<Object> aggregateVertexList = Arrays.asList(vertexList1, vertexList2);

        List<Object> paginatedResult = PaginationUtil.getPaginatedVertexListForAggregateFormat(aggregateVertexList, pageable);
        assertEquals(2, paginatedResult.size());
        assertEquals(vertexList1, paginatedResult.get(0));
        assertEquals(vertexList2, paginatedResult.get(1));
    }

    @Test
    public void testGetPaginatedVertexListForAggregateFormatEmptyList() throws AAIException {
        Pageable pageable = Mockito.mock(Pageable.class);
        Mockito.when(pageable.getPage()).thenReturn(0);
        Mockito.when(pageable.getPageSize()).thenReturn(2);

        List<Object> aggregateVertexList = Collections.emptyList(); // empty list

        List<Object> paginatedResult = PaginationUtil.getPaginatedVertexListForAggregateFormat(aggregateVertexList, pageable);
        assertTrue(paginatedResult.isEmpty()); // should return empty list
    }

    @Test
    public void testGetPaginatedVertexListForAggregateFormatNullList() throws AAIException {
        Pageable pageable = Mockito.mock(Pageable.class);
        Mockito.when(pageable.getPage()).thenReturn(0);
        Mockito.when(pageable.getPageSize()).thenReturn(2);

        List<Object> aggregateVertexList = null; // null list

        List<Object> paginatedResult = PaginationUtil.getPaginatedVertexListForAggregateFormat(aggregateVertexList, pageable);
        assertNull(paginatedResult); // should return null as there's no aggregate list
    }

    @Test
    public void testGetPaginatedVertexListForAggregateFormatWithMultiplePages() throws AAIException {
        Pageable pageable = Mockito.mock(Pageable.class);
        Mockito.when(pageable.getPage()).thenReturn(1); // testing with a second page
        Mockito.when(pageable.getPageSize()).thenReturn(2);

        List<Object> vertexList = Arrays.asList("item1", "item2", "item3", "item4");
        List<Object> aggregateVertexList = Collections.singletonList(vertexList);

        List<Object> paginatedResult = PaginationUtil.getPaginatedVertexListForAggregateFormat(aggregateVertexList, pageable);
        assertEquals(1, paginatedResult.size());
        List<Object> page = (List<Object>) paginatedResult.get(0);
        assertEquals(2, page.size());
        assertEquals("item3", page.get(0)); // second page, item3
        assertEquals("item4", page.get(1)); // second page, item4
    }

    @Test
    public void testHasValidPaginationParams_ValidParams() {
        Pageable pageable = Mockito.mock(Pageable.class);
        Mockito.when(pageable.getPage()).thenReturn(0);
        Mockito.when(pageable.getPageSize()).thenReturn(10);

        assertTrue(PaginationUtil.hasValidPaginationParams(pageable));
    }

    @Test
    public void testHasValidPaginationParams_InvalidPage() {
        Pageable pageable = Mockito.mock(Pageable.class);
        Mockito.when(pageable.getPage()).thenReturn(-1);
        Mockito.when(pageable.getPageSize()).thenReturn(10);

        assertFalse(PaginationUtil.hasValidPaginationParams(pageable));
    }

    @Test
    public void testHasValidPaginationParams_InvalidPageSize() {
        Pageable pageable = Mockito.mock(Pageable.class);
        Mockito.when(pageable.getPage()).thenReturn(0);
        Mockito.when(pageable.getPageSize()).thenReturn(0);

        assertFalse(PaginationUtil.hasValidPaginationParams(pageable));
    }

    @Test
    public void testGetTotalPages() {
        Pageable pageable = Mockito.mock(Pageable.class);
        Mockito.when(pageable.getPageSize()).thenReturn(10);

        long totalCount = 25;
        long totalPages = PaginationUtil.getTotalPages(pageable, totalCount);
        assertEquals(3, totalPages); // 25 items, 10 items per page => 3 pages

        totalCount = 20;
        totalPages = PaginationUtil.getTotalPages(pageable, totalCount);
        assertEquals(2, totalPages); // 20 items, 10 items per page => 2 pages
    }
}
