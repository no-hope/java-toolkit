package org.nohope.cassandra.mapservice;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.custom.DateTimeType;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.nohope.cassandra.mapservice.ctypes.CoreConverter.TEXT;

/**
 */
public class CMapBuilderTest {
    private CMapBuilder tableBuilder;
    private static final CColumn<String, String> FIRST = CColumn.of("First", TEXT);
    private static final CColumn<DateTime, String> SECOND = CColumn.of("Second", DateTimeType.INSTANCE);

    @Before
    public void setUpBuilder() {
        tableBuilder = new CMapBuilder("table");
    }

    @Test
    public void simpleTableSchemeTest() {
        final String expectedDescription =
                "CREATE TABLE IF NOT EXISTS \"table\" ("
                + "First text, Second text, PRIMARY KEY ((First)));";
        final TableScheme scheme = tableBuilder
                .addColumn(FIRST)
                .addColumn(SECOND)
                .end()
                .setPartition(FIRST)
                .withoutClustering().buildScheme();
        assertEquals(expectedDescription, scheme.getTableDescription());
        assertEquals("table", scheme.getTableName());
    }

    @Test
    public void simpleClusteringKeyTest() {
        final String expectedDescription =
                "CREATE TABLE IF NOT EXISTS \"table\" ("
                + "First text, Second text, PRIMARY KEY ((First), Second));";
        final TableScheme scheme = tableBuilder
                .addColumn(FIRST)
                .addColumn(SECOND)
                .end()
                .setPartition(FIRST)
                .setClustering(SECOND).buildScheme();
        assertEquals(expectedDescription, scheme.getTableDescription());
    }

    @Test(expected = TableSchemeException.class)
    public void addSameClusteringKeyAsPartitionKeyTest() {
        tableBuilder
                .addColumn(FIRST)
                .addColumn(SECOND)
                .end()
                .setPartition(SECOND)
                .setClustering(SECOND).buildScheme();
    }

    @Test(expected = TableSchemeException.class)
    public void addNonExistingPartitionKeyTest() {
        tableBuilder
                .addColumn(FIRST)
                .addColumn(SECOND)
                .end()
                .setPartition(FIRST, CColumn.of("god", TEXT))
                .withoutClustering()
                .buildScheme();
    }

    @Test(expected = TableSchemeException.class)
    public void add_non_existing_clusteringKey_test() {
        tableBuilder
                .addColumn(FIRST)
                .addColumn(SECOND)
                .end()
                .setPartition(FIRST)
                .setClustering(SECOND, CColumn.of("god", TEXT))
                .buildScheme();
    }

    @Test(expected = TableSchemeException.class)
    public void badTableNameTest() {
        final String badLongTerribleNameForATable = UUID.randomUUID().toString();

        new CMapBuilder(badLongTerribleNameForATable)
                .addColumn(FIRST)
                .addColumn(SECOND)
                .end()
                .setPartition(FIRST)
                .setClustering(SECOND).buildScheme();
    }

    @Test
    public void add_several_existing_partition_keys() {
        final CColumn<String, String> good = CColumn.of("Good", TEXT);
        final CColumn<String, String> bad = CColumn.of("Bad", TEXT);
        final CColumn<String, String> ugly = CColumn.of("Ugly", TEXT);
        final String expectedTableDescription =
                "CREATE TABLE IF NOT EXISTS \"table\" ("
                + "Good text, Bad text, Ugly text, PRIMARY KEY ((Good, Ugly)));";
        final TableScheme scheme = tableBuilder
                .addColumn(good)
                .addColumn(bad)
                .addColumn(ugly)
                .end()
                .setPartition(good, ugly)
                .withoutClustering().buildScheme();
        assertEquals(expectedTableDescription, scheme.getTableDescription());
    }

    @Test
    public void add_several_existing_clustering_keys() {
        final CColumn<String, String> good = CColumn.of("Good", TEXT);
        final CColumn<String, String> bad = CColumn.of("Bad", TEXT);
        final CColumn<String, String> ugly = CColumn.of("Ugly", TEXT);
        final String expectedTableDescription =
                "CREATE TABLE IF NOT EXISTS \"table\" ("
                + "Good text, Bad text, Ugly text, PRIMARY KEY ((Good), Bad, Ugly));";
        final TableScheme scheme = tableBuilder
                .addColumn(good)
                .addColumn(bad)
                .addColumn(ugly)
                .end()
                .setPartition(good)
                .setClustering(bad, ugly)
                .buildScheme();
        assertEquals(expectedTableDescription, scheme.getTableDescription());
    }

    @Test(expected = TableSchemeException.class)
    public void quotedTableName() {
        new CMapBuilder("\"quotedTable\"")
                .addColumn(CColumn.of("first", TEXT))
                .end()
                .setPartition(CColumn.of("first", TEXT))
                .withoutClustering()
                .buildScheme();
    }
}
