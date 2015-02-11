package org.nohope.cassandra.mapservice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.columns.trivial.CTextColumn;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.nohope.cassandra.mapservice.ctypes.CoreConverter.*;

/**
 */
public class CCollectionMapIT {
    private CassandraFactory cassandraFactory;
    private static final CColumn<Map<String, String>, Map<String, String>> COL_PERSON = CColumn.of("mapping", map(TEXT, ASCII));
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

    @After
    public void tearDown() {
        ITHelpers.destroy(cassandraFactory);
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
