package org.nohope.cassandra.mapservice;

import org.junit.Test;
import org.nohope.test.ContractUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 */
public final class ValueTupleTest {

    @Test
    public void testContract() {
        final ValueTuple v1 = ValueTuple.of("xxx", "yyy").with("aaa", "bbb");
        final ValueTuple v2 = ValueTuple.of("xxx", "yyy").with("aaa", "bbb");
        ContractUtils.assertStrongEquality(v1, v2);
    }

    @Test
    public void testOfAndWith() {
        final Map<String, Object> expectedColumns = new HashMap<>();

        expectedColumns.put("xxx", "yyy");
        expectedColumns.put("aaa", "bbb");

        final ValueTuple value = ValueTuple
                .of("xxx", "yyy")
                .with("aaa", "bbb");

        assertEquals(expectedColumns, value.getColumns());
    }
}
