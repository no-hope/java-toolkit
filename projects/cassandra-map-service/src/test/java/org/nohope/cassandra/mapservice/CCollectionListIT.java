package org.nohope.cassandra.mapservice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 */
public class CCollectionListIT {
    private CassandraFactory cassandraFactory;

    private static final CColumn<List<String>, List<String>> COL_PERSON = CColumn.of("person", CoreConverter.list(CoreConverter.TEXT));
    private static final CColumn<String, String> COL_ID = CColumn.of("id", CoreConverter.TEXT);

    private static final TableScheme SCHEME = new CMapBuilder("RingOfPower")
            .addColumn(COL_ID)
            .addColumn(COL_PERSON)
            .end()
            .setPartition(COL_ID)
            .withoutClustering().buildScheme();

    @Before
    public void setUp() {
        cassandraFactory = ITHelpers.cassandraFactory();
    }

    @After
    public void tearDown() {
        ITHelpers.destroy(cassandraFactory);
    }

    @Test
    public void schemeTest() {
        final String expectedSchemeDescription =
                "CREATE TABLE IF NOT EXISTS \"RingOfPower\" ("
                + "id text, person list<text>, PRIMARY KEY ((id)));";
        assertEquals(expectedSchemeDescription, SCHEME.buildTableDescription());
        cassandraFactory.getSession().execute(SCHEME.buildTableDescription());
    }

    @Test(expected = TableSchemeException.class)
    public void tryToSetACoolectionColumnAsPartiotion() {
        new CMapBuilder("RingOfPower")
                .addColumn(COL_ID)
                .addColumn(COL_PERSON)
                .end()
                .setPartition(COL_PERSON)
                .withoutClustering().buildScheme();
    }
}
