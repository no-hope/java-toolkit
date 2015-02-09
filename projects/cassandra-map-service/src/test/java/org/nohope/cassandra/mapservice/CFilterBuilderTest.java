package org.nohope.cassandra.mapservice;

import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.cfilter.CFilters;
import org.nohope.test.ContractUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

public final class CFilterBuilderTest {
    private Collection<CFilter> filters;

    @Test
    public void testContract() {
        ContractUtils.assertStrongEquality(
                CFilters.eq("aaa", "bbb"),
                CFilters.eq("aaa", "bbb")
        );
    }

    @Before
    public void setUp() {
        filters = new ArrayList<>();
    }

    @Test
    public void testGetQueryEQFilters() {
        filters.add(CFilters.eq("xxx", 4));

        final List<CFilter> builderFilters = CFilterBuilder
                .getQueryFilters()
                .eq("xxx", 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryGTFilters() {
        filters.add(CFilters.gt("xxx", 4));

        final List<CFilter> builderFilters = CFilterBuilder
                .getQueryFilters()
                .gt("xxx", 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryGTEFilters() {
        filters.add(CFilters.gte("xxx", 4));

        final List<CFilter> builderFilters = CFilterBuilder
                .getQueryFilters()
                .gte("xxx", 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryLTFilters() {
        filters.add(CFilters.lt("xxx", 4));

        final List<CFilter> builderFilters = CFilterBuilder
                .getQueryFilters()
                .lt("xxx", 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryLTEFilters() {
        filters.add(CFilters.lte("xxx", 4));

        final List<CFilter> builderFilters = CFilterBuilder
                .getQueryFilters()
                .lte("xxx", 4)
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testGetQueryINFilters() {
        filters.add(CFilters.in("xxx", "aaa", "bbb", "ccc"));

        final List<CFilter> builderFilters = CFilterBuilder
                .getQueryFilters()
                .in("xxx", "aaa", "bbb", "ccc")
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testRemoveQueryINFilters() {
        filters.add(CFilters.in("xxx", "aaa", "bbb", "ccc"));

        final List<CFilter> builderFilters = CFilterBuilder
                .getQueryFilters()
                .in("xxx", "aaa", "bbb", "ccc")
                .getFilters();

        assertEquals(filters, builderFilters);
    }

    @Test
    public void testRemoveQueryEqFilters() {
        filters.add(CFilters.eq("xxx", "ccc"));

        final List<CFilter> builderFilters = CFilterBuilder
                .getQueryFilters()
                .eq("xxx", "ccc")
                .getFilters();

        assertEquals(filters, builderFilters);
    }
}
