package org.nohope.cassandra.mapservice;

import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.cfilter.CFilters;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;
import org.nohope.test.ContractUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public final class CFilterBuilderTest {
    private Collection<CFilter<?>> filters;
    private static final CColumn<Integer, Integer> INT_COL = CColumn.of("xxx", CoreConverter.INT);

    @Test
    public void testContract() {
        ContractUtils.assertStrongEquality(
                CFilters.eq(Value.bound(CColumn.of("aaa", CoreConverter.ASCII), "bbb")),
                CFilters.eq(Value.bound(CColumn.of("aaa", CoreConverter.ASCII), "bbb"))
        );
    }

    @Before
    public void setUp() {
        filters = new ArrayList<>();
    }

    @Test
    public void testGetQueryEQFilters() {
        filters.add(CFilters.eq(Value.bound(INT_COL, 4)));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .eq(INT_COL, 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryGTFilters() {
        filters.add(CFilters.gt(Value.bound(INT_COL, 4)));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .gt(INT_COL, 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryGTEFilters() {
        filters.add(CFilters.gte(Value.bound(INT_COL, 4)));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .gte(INT_COL, 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryLTFilters() {
        filters.add(CFilters.lt(Value.bound(INT_COL, 4)));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .lt(INT_COL, 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryLTEFilters() {
        filters.add(CFilters.lte(Value.bound(INT_COL, 4)));
        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .lte(INT_COL, 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryINFilters() {
        filters.add(CFilters.in(Value.bound(INT_COL.asList(), Arrays.asList(2, 4, 5))));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .in(INT_COL, 2, 4, 5)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testRemoveQueryINFilters() {
        filters.add(CFilters.in(Value.bound(INT_COL.asList(), Arrays.asList(2, 4, 5))));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .in(INT_COL, 2, 4, 5)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testRemoveQueryEqFilters() {
        filters.add(CFilters.eq(Value.bound(INT_COL, 2)));

        final List<CFilter<?>> builderFilters = CFilterBuilder
                .getQueryFilters()
                .eq(INT_COL, 2)
                .getFilters();

        assertEquals(filters, builderFilters);
    }
}
