package org.nohope.cassandra.mapservice;

import org.junit.Test;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;
import org.nohope.test.ContractUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 */
public final class ValueTupleTest {

    @Test
    public void testContract() {
        final ValueTuple v1 = ValueTuple.of(CColumn.of("xxx", CoreConverter.TEXT), "yyy").with(CColumn.of("aaa", CoreConverter.TEXT), "bbb");
        final ValueTuple v2 = ValueTuple.of(CColumn.of("xxx", CoreConverter.TEXT), "yyy").with(CColumn.of("aaa", CoreConverter.TEXT), "bbb");
        ContractUtils.assertStrongEquality(v1, v2);
    }

    @Test
    public void testOfAndWith() {
        final Map<String, CColumn<?, ?>> expectedColumns = new HashMap<>();

        expectedColumns.put("xxx", CColumn.of("xxx", CoreConverter.TEXT));
        expectedColumns.put("aaa", CColumn.of("aaa", CoreConverter.TEXT));

        final ValueTuple value = ValueTuple
                .of(CColumn.of("xxx", CoreConverter.TEXT), "yyy")
                .with(CColumn.of("aaa", CoreConverter.TEXT), "bbb");

        assertEquals(expectedColumns, value.getColumns());
    }
}
