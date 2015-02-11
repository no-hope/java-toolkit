package org.nohope.cassandra.mapservice;

import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.cfilter.CFilters;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;
import org.nohope.test.ContractUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public final class CFilterBuilderTest {
    private Collection<CFilter<?>> filters;
    private static final CColumn<Integer, Integer> INT_COL = CColumn.of("xxx", CoreConverter.INT);

    @Test
    public void testContract() {
        ContractUtils.assertStrongEquality(
                CFilters.eq(CColumn.of("aaa", CoreConverter.ASCII), "bbb"),
                CFilters.eq(CColumn.of("aaa", CoreConverter.ASCII), "bbb")
        );
    }

    @Before
    public void setUp() {
        filters = new ArrayList<>();
    }

    @Test
    public void testGetQueryEQFilters() {
        filters.add(CFilters.eq(INT_COL, 4));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .eq(INT_COL, 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryGTFilters() {
        filters.add(CFilters.gt(INT_COL, 4));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .gt(INT_COL, 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryGTEFilters() {
        filters.add(CFilters.gte(INT_COL, 4));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .gte(INT_COL, 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryLTFilters() {
        filters.add(CFilters.lt(INT_COL, 4));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .lt(INT_COL, 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryLTEFilters() {
        filters.add(CFilters.lte(INT_COL, 4));
        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .lte(INT_COL, 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryINFilters() {
        filters.add(CFilters.in(INT_COL, 2, 4, 5));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .in(INT_COL, 2, 4, 5)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testRemoveQueryINFilters() {
        filters.add(CFilters.in(INT_COL, 2, 4, 5));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .in(INT_COL, 2, 4, 5)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testRemoveQueryEqFilters() {
        filters.add(CFilters.eq(INT_COL, 2));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .eq(INT_COL, 2)
                .getFilters();

        assertEquals(filters, builderFilters);
    }
}
