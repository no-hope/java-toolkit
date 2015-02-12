package org.nohope.cassandra.mapservice;

import org.junit.Test;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;
import org.nohope.test.ContractUtils;

import java.util.Collection;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;

/**
 */
public final class ColumnsSetCOperationTest {

    private static final Collection<CColumn<?, ?>> EXPECTED_COLUMNS = new LinkedHashSet<>();

    static {
        EXPECTED_COLUMNS.add(CColumn.of("xxx", CoreConverter.TEXT));
        EXPECTED_COLUMNS.add(CColumn.of("yyy", CoreConverter.TEXT));
        EXPECTED_COLUMNS.add(CColumn.of("aaa", CoreConverter.TEXT));
        EXPECTED_COLUMNS.add(CColumn.of("bbb", CoreConverter.TEXT));
    }

    @Test
    public void testContract() {
        final ColumnsSet s1 = new ColumnsSet(CColumn.of("xxx", CoreConverter.TEXT), CColumn.of("yyy", CoreConverter.TEXT));
        final ColumnsSet s2 = new ColumnsSet(CColumn.of("xxx", CoreConverter.TEXT), CColumn.of("yyy", CoreConverter.TEXT));
        ContractUtils.assertStrongEquality(s1, s2);
    }

    @Test
    public void testOf() {
        final ColumnsSet s1 = ColumnsSet.of(
                CColumn.of("xxx", CoreConverter.TEXT),
                CColumn.of("yyy", CoreConverter.TEXT),
                CColumn.of("aaa", CoreConverter.TEXT),
                CColumn.of("bbb", CoreConverter.TEXT)
        );
        assertEquals(EXPECTED_COLUMNS, s1.getColumns());
    }

    @Test
    public void testWith() {
        final ColumnsSet s1 =
                ColumnsSet.of(CColumn.of("xxx", CoreConverter.TEXT))
                          .with(CColumn.of("yyy", CoreConverter.TEXT))
                          .with(CColumn.of("aaa", CoreConverter.TEXT))
                          .with(CColumn.of("bbb", CoreConverter.TEXT));

        assertEquals(EXPECTED_COLUMNS, s1.getColumns());
    }

    @Test
    public void testWithout() {
        final CColumn<String, String> toDelete = CColumn.of("toDelete", CoreConverter.TEXT);
        final ColumnsSet s1 =
                ColumnsSet.of(CColumn.of("xxx", CoreConverter.TEXT))
                          .with(CColumn.of("yyy", CoreConverter.TEXT))
                          .with(CColumn.of("aaa", CoreConverter.TEXT))
                          .with(CColumn.of("bbb", CoreConverter.TEXT))
                          .with(toDelete);

        final ColumnsSet s2 = s1.without(toDelete);
        assertEquals(EXPECTED_COLUMNS, s2.getColumns());
    }

    @Test
    public void testWithAll() {
        final ColumnsSet s1 =
                ColumnsSet.of(CColumn.of("xxx", CoreConverter.TEXT))
                          .with(CColumn.of("yyy", CoreConverter.TEXT))
                          .with(CColumn.of("aaa", CoreConverter.TEXT))
                          .with(CColumn.of("bbb", CoreConverter.TEXT));

        final ColumnsSet s2 = new ColumnsSet().withAll(s1);
        assertEquals(EXPECTED_COLUMNS, s2.getColumns());
    }
}
