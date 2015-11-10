package org.nohope.cassandra.mapservice;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.cfilter.CFilters;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;
import org.nohope.cassandra.mapservice.ctypes.custom.DateTimeType;

import java.util.UUID;

import static org.nohope.cassandra.mapservice.ctypes.CoreConverter.TEXT;

/**
 */
public class CMapExceptionsIT {
    private static final CColumn<String, String> COL_QUOTES = CColumn.of("quotes", TEXT);
    private static final CColumn<DateTime, String> COL_TIMESTAMP = CColumn.of("timestamp", DateTimeType.INSTANCE);
    private static final CColumn<UUID, UUID> COL_QUOTE_UUID = CColumn.of("quoteUUID", CoreConverter.UUID);
    private static final TableScheme SCHEME = new CMapBuilder("RingOfPower")
            .addColumn(COL_QUOTES)
            .addColumn(COL_TIMESTAMP)
            .addColumn(COL_QUOTE_UUID)
            .end()
            .setPartition(COL_QUOTES)
            .withoutClustering().buildScheme();
    private static final TableScheme TWO_PARTITION_SCHEME = new CMapBuilder("RingOfPower")
            .addColumn(COL_QUOTES)
            .addColumn(COL_TIMESTAMP)
            .addColumn(COL_QUOTE_UUID)
            .end()
            .setPartition(COL_QUOTES, COL_QUOTE_UUID)
            .withoutClustering().buildScheme();

    private CMapSync testMap;
    private CassandraFactory cassandraFactory;

    @Before
    public void setUp() {
        cassandraFactory = ITHelpers.cassandraFactory();
        testMap = new CMapSync(SCHEME, cassandraFactory);
    }

    @After
    public void tearDown() {
        ITHelpers.destroy(cassandraFactory);
    }

    @Test(expected = CQueryException.class)
    public void putLackColumn() {
        final ValueTuple valueToPut = ValueTuple.of(COL_QUOTES, newQuote());
        testMap.put(new CPutQuery(valueToPut));
    }

    @Test(expected = CQueryException.class)
    public void tryToOderByLackColumn() {
        final CColumn<String, String> lackColumn = CColumn.of("xxx", TEXT);
        final CQuery query = CQueryBuilder
                .createQuery()
                .of(COL_QUOTES, COL_TIMESTAMP)
                .orderingBy(lackColumn, Orderings.ASC)
                .end();

        testMap.get(query);
    }

    @Test(expected = CQueryException.class)
    public void orderByNotAClusteringColumn() {
        final CQuery query = CQueryBuilder
                .createQuery()
                .of(COL_QUOTES, COL_TIMESTAMP)
                .orderingBy(COL_QUOTE_UUID, Orderings.ASC)
                .end();

        testMap.get(query);
    }

    @Test(expected = CQueryException.class)
    public void allowOrderingByWithABadColumnName() throws CQueryException {
        final CColumn<String, String> lackColumn1 = CColumn.of("xxx", TEXT);
        final CColumn<String, String> lackColumn2 = CColumn.of("yyy", TEXT);
        final CColumn<String, String> lackColumn3 = CColumn.of("god", TEXT);
        final CQuery query = CQueryBuilder
                .createQuery()
                .of(lackColumn1, lackColumn2)
                .orderingBy(lackColumn3, Orderings.ASC)
                .end();

        testMap.get(query);
    }

    @Test(expected = CQueryException.class)
    public void nonExistingColumnInAPutQuery() {
        testMap = new CMapSync(TWO_PARTITION_SCHEME, cassandraFactory);

        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now())
                          .with(COL_QUOTE_UUID, UUID.randomUUID())
                          .with(CColumn.of("god", CoreConverter.UUID), UUID.randomUUID());

        testMap.put(new CPutQuery(valueToPut));
    }

    @Test(expected = CQueryException.class)
    public void filtersHaveNonPartitionKeyColumn() {
        final CQuery query = CQueryBuilder
                .createQuery()
                .of(COL_QUOTES, COL_TIMESTAMP)
                .withFilters(CFilters.eq(Value.bound(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))))
                .end();

        testMap.get(query);
    }

    @Test(expected = IllegalStateException.class)
    public void traversableOnceTest() {
        final int count = 5;
        for (int i = 0; i < count; i++) {
            testMap.put(new CPutQuery(ValueTuple.of(COL_QUOTES, newQuote())
                                                .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                                                .with(COL_QUOTE_UUID, UUID.randomUUID())));
        }

        final Iterable<ValueTuple> iterable = testMap.all();
        iterable.iterator();
        iterable.iterator();
    }

    private static String newQuote() {
        return "It's the job that's never started as takes longest to finish."
               + RandomStringUtils.randomAlphanumeric(10);
    }
}
