package org.nohope.cassandra.mapservice;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.custom.DateTimeType;

import static org.nohope.cassandra.mapservice.ctypes.CoreConverter.TEXT;

/**
 */
public class CMapServiceTestIT {
    private static final String TABLE_NAME = "someSmartAndFunnyTableName";

    private CMapService service;
    private CassandraFactory cassandraFactory;

    @Before
    public void setUp() throws TableSchemeException {
        final CColumn<String, String> c1 = CColumn.of("First", TEXT);
        final CColumn<DateTime, String> c2 = CColumn.of("Second", DateTimeType.INSTANCE);
        final TableScheme scheme = new CMapBuilder(TABLE_NAME)
                .addColumn(c1)
                .addColumn(c2)
                .end()
                .setPartition(c1)
                .withoutClustering().buildScheme();

        cassandraFactory = ITHelpers.cassandraFactory();
        service = new CMapService(cassandraFactory, Lists.newArrayList(scheme));
    }

    @After
    public void tearDown() {
        ITHelpers.destroy(cassandraFactory);
    }

    @Test
    public void getExistingMap() {
        service.getMap(TABLE_NAME);
    }

    @Test(expected = CMapServiceException.class)
    public void getNonExistingMap() {
        service.getMap("NonExistingMap");
    }
}
