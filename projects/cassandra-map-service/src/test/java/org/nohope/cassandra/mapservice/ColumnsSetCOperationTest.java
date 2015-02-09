package org.nohope.cassandra.mapservice;

import org.junit.Test;
import org.nohope.test.ContractUtils;

import java.util.Collection;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

/**
 */
public final class ColumnsSetCOperationTest {

    private static final Collection<String> EXPECTED_COLUMNS = new LinkedHashSet<>();

    static {
        EXPECTED_COLUMNS.add("xxx");
        EXPECTED_COLUMNS.add("yyy");
        EXPECTED_COLUMNS.add("aaa");
        EXPECTED_COLUMNS.add("bbb");
    }

    @Test
    public void testContract() {
        final ColumnsSet s1 = new ColumnsSet("xxx", "yyy");
        final ColumnsSet s2 = new ColumnsSet("xxx", "yyy");
        ContractUtils.assertStrongEquality(s1, s2);
    }

    @Test
    public void testOf() {
        final ColumnsSet s1 = ColumnsSet.of("xxx", "yyy", "aaa", "bbb");
        assertEquals(EXPECTED_COLUMNS, s1.getColumns());
    }

    @Test
    public void testWith() {
        final ColumnsSet s1 = ColumnsSet.of("xxx")
                                        .with("yyy")
                                        .with("aaa")
                                        .with("bbb");

        assertEquals(EXPECTED_COLUMNS, s1.getColumns());
    }

    @Test
    public void testWithout() {
        final ColumnsSet s1 = ColumnsSet.of("xxx")
                                        .with("yyy")
                                        .with("aaa")
                                        .with("bbb")
                                        .with("toDelete");

        final ColumnsSet s2 = s1.without("toDelete");
        assertEquals(EXPECTED_COLUMNS, s2.getColumns());
    }

    @Test
    public void testWithAll() {
        final ColumnsSet s1 = ColumnsSet.of("xxx")
                                        .with("yyy")
                                        .with("aaa")
                                        .with("bbb");

        final ColumnsSet s2 = new ColumnsSet().withAll(s1);
        assertEquals(EXPECTED_COLUMNS, s2.getColumns());
    }
}
