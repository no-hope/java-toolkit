package org.nohope.cassandra.mapservice;

import org.junit.Test;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.NoSuchCTypeException;
import org.nohope.test.ContractUtils;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.nohope.cassandra.mapservice.ctypes.CoreConverter.TEXT;

public final class TableSchemeTest {
    private static final CColumn<String, String> NAME_COLUMN = CColumn.of("name", TEXT);
    private static final CColumn<String, String> SPECIES_COLUMN = CColumn.of("species", TEXT);
    private static final CColumn<String, String> HOOITA_COLUMN = CColumn.of("hooita", TEXT);
    private static final CColumn<String, String> LEPOTA_COLUMN = CColumn.of("lepota", TEXT);
    private static final String OWL_TABLE_ID = "Owls";

    @Test
    public void testContract() {
        final TableScheme s1 = getTableScheme();
        final TableScheme s2 = getTableScheme();
        ContractUtils.assertStrongEquality(s1, s2);
    }

    private static TableScheme getTableScheme() throws TableSchemeException, NoSuchCTypeException {
        return new CMapBuilder(OWL_TABLE_ID)
                .addColumns(NAME_COLUMN, SPECIES_COLUMN, HOOITA_COLUMN, LEPOTA_COLUMN)
                .end()
                .setPartition(NAME_COLUMN)
                .setClustering(SPECIES_COLUMN)
                .setStatic(HOOITA_COLUMN)
                .buildScheme();
    }

    @Test
    public void testGetTableName() {
        final TableScheme s1 = getTableScheme();
        assertEquals(OWL_TABLE_ID, s1.getTableName());
    }

    @Test
    public void testGetTableDescription() {
        final String expectedDescription =
                "CREATE TABLE IF NOT EXISTS \"Owls\" ("
                + "name text, species text, hooita text STATIC, lepota text, "
                + "PRIMARY KEY ((name), species));";

        final TableScheme s1 = getTableScheme();
        assertEquals(expectedDescription, s1.getTableDescription());
    }

    @Test
    public void testGetColumns() {
        final Map<String, CColumn<?, ?>> expectedColumns = new LinkedHashMap<>();
        expectedColumns.put(NAME_COLUMN.getName(), NAME_COLUMN);
        expectedColumns.put(SPECIES_COLUMN.getName(), SPECIES_COLUMN);
        expectedColumns.put(HOOITA_COLUMN.getName(), HOOITA_COLUMN);
        expectedColumns.put(LEPOTA_COLUMN.getName(), LEPOTA_COLUMN);

        final TableScheme s1 = getTableScheme();
        assertEquals(s1.getColumns(), expectedColumns);
    }

    // FIXME: wut?
    //@Test
    //public void testGetColumnsSet() {
    //    final Map<String, Converter<?, ?>> expectedColumns = new LinkedHashMap<>();
    //    expectedColumns.put(NAME_COLUMN.getName(), NAME_COLUMN.getConverter());
    //    expectedColumns.put(SPECIES_COLUMN.getName(), SPECIES_COLUMN.getConverter());
    //    expectedColumns.put(HOOITA_COLUMN.getName(), HOOITA_COLUMN.getConverter());
    //    expectedColumns.put(LEPOTA_COLUMN.getName(), LEPOTA_COLUMN.getConverter());
    //
    //    final TableScheme s1 = getTableScheme();
    //    // FIXME: values?
    //    assertEquals(s1.getColumnNames(), expectedColumns.keySet());
    //}

    @Test
    public void testIsPrimaryKey() {
        final TableScheme s1 = getTableScheme();
        assertTrue(s1.isPartitionKey(NAME_COLUMN));
    }

    @Test
    public void testPrimaryKeyGetter() {
        final TableScheme s1 = getTableScheme();
        assertEquals(1, s1.getPartitionKeys().size());
        assertTrue(s1.getPartitionKeys().contains(CColumn.of("name", TEXT)));
    }
}
