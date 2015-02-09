package org.nohope.cassandra.mapservice;

import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.columns.collections.MapCColumn;
import org.nohope.cassandra.mapservice.columns.trivial.CTextColumn;
import org.nohope.cassandra.mapservice.ctypes.CType;
import org.nohope.cassandra.mapservice.ctypes.TrivialType;

import static org.junit.Assert.assertEquals;

/**
 */
public class CCollectionMapIT {

    private CassandraFactory cassandraFactory;
    private static final CColumn<String, String> COL_PERSON =
            MapCColumn.of("mapping", CType.TEXT, TrivialType.ASCII);
    private static final CTextColumn COL_ID = CTextColumn.of("id");

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

    @Test
    public void tableDescriptionScheme() {
        final String excpectedDescription =
                "CREATE TABLE IF NOT EXISTS \"RingOfPower\" ("
                + "id text, mapping map<text, ascii>, PRIMARY KEY ((id)));";
        assertEquals(excpectedDescription, SCHEME.buildTableDescription());
        cassandraFactory.getSession().execute(SCHEME.buildTableDescription());
    }
}
