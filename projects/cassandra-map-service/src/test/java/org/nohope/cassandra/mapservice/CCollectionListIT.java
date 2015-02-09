package org.nohope.cassandra.mapservice;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.columns.collections.ListCColumn;
import org.nohope.cassandra.mapservice.columns.trivial.CTextColumn;

import static org.junit.Assert.assertEquals;

/**
 */
public class CCollectionListIT {
    private CassandraFactory cassandraFactory;

    private static final ListCColumn<String, Object> COL_PERSON = ListCColumn.ofText("person");
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
