package org.nohope.cassandra.mapservice;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.cfilter.CFilter;
import org.nohope.cassandra.mapservice.cfilter.CFilters;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.columns.joda.CDateTimeUTCStringColumn;
import org.nohope.cassandra.mapservice.columns.trivial.CTextColumn;
import org.nohope.cassandra.mapservice.columns.trivial.CUUIDColumn;
import org.nohope.cassandra.util.RowNotFoundException;
import org.nohope.test.ContractUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;
import static org.nohope.cassandra.mapservice.QuoteTestGenerator.newQuote;

/**
 */
public class CMapIT {
    private static final CColumn<String, String> COL_QUOTES = CTextColumn.of("quotes");
    private static final CColumn<DateTime, String> COL_TIMESTAMP = CDateTimeUTCStringColumn.of("timestamp");
    private static final CColumn<UUID, UUID> COL_QUOTE_UUID = CUUIDColumn.of("quoteuuid");

    private static final TableScheme SCHEME = new CMapBuilder("RingOfPower")
            .addColumn(COL_QUOTES)
            .addColumn(COL_TIMESTAMP)
            .end()
            .setPartition(COL_QUOTES)
            .withoutClustering()
            .buildScheme();

    private static final TableScheme THREE_COLUMN_SCHEME = new CMapBuilder("RingOfStupidity")
            .addColumn(COL_QUOTES)
            .addColumn(COL_TIMESTAMP)
            .addColumn(COL_QUOTE_UUID)
            .end()
            .setPartition(COL_QUOTES)
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

    @Test
    public void contractTest() {
        final CMapSync testMap1 = new CMapSync(SCHEME, cassandraFactory);
        final CMapSync testMap2 = new CMapSync(SCHEME, cassandraFactory);
        ContractUtils.assertStrongEquality(testMap1, testMap2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuotedKeyspaceTest() {
        cassandraFactory.setKeyspace("\"test_Keyspace\"");
    }

    @Test
    public void severalPartitionKeysTest() {
        final CColumn<String, String> good = CTextColumn.of("Good");
        final CColumn<String, String> bad = CTextColumn.of("Bad");
        final CColumn<String, String> ugly = CTextColumn.of("Ugly");

        final TableScheme scheme = new CMapBuilder("testmap")
                .addColumn(good)
                .addColumn(bad)
                .addColumn(ugly)
                .end()
                .setPartition(good)
                .setClustering(bad, ugly)
                .buildScheme();

        new CMapSync(scheme, cassandraFactory);
    }

    @Test
    public void getOneTrivialTypeTest() throws RowNotFoundException {
        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC));
        final CPutQuery putQuery = new CPutQuery(valueToPut);
        final CQuery query = new CQuery(COL_QUOTES.getName(), COL_TIMESTAMP.getName());

        testMap.put(putQuery);

        final ValueTuple returnValue = testMap.getOne(query);
        assertEquals(returnValue, valueToPut);
    }

    @Test(expected = CQueryException.class)
    public void getOneWrongColumnTest() throws RowNotFoundException {
        final CQuery query = new CQuery(COL_QUOTES.getName(), "someColumn");
        testMap.getOne(query);
    }

    @Test
    public void getOneFiltersTrivialTypeTest() throws RowNotFoundException {
        final String quote = newQuote();
        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, quote)
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC));

        final CPutQuery putQuery = new CPutQuery(valueToPut);

        final CQuery query =
                CQueryBuilder.createQuery()
                             .of(COL_QUOTES, COL_TIMESTAMP)
                             .addFilters()
                             .eq(COL_QUOTES, quote)
                             .noMoreFilters()
                             .end();

        testMap.put(putQuery);
        final ValueTuple returnValue = testMap.getOne(query);
        assertEquals(returnValue, valueToPut);
    }

    @Test
    public void getAllTrivialTypeTest() {
        final CMapSync testMap = new CMapSync(THREE_COLUMN_SCHEME, cassandraFactory);

        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        final ValueTuple valueToPut2 =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        testMap.put(new CPutQuery(valueToPut));
        testMap.put(new CPutQuery(valueToPut2));

        final List<ValueTuple> returnValue = Lists.newArrayList(testMap.all());

        assertEquals(2, returnValue.size());
        assertTrue(returnValue.contains(valueToPut2));
        assertTrue(returnValue.contains(valueToPut));
    }

    @Test
    public void getTrivialTypeTest() {
        testMap = new CMapSync(THREE_COLUMN_SCHEME, cassandraFactory);

        final String quoteToPutAndToGet = newQuote();

        final DateTime dateToPutAndToGet = DateTime.now(DateTimeZone.UTC);
        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, quoteToPutAndToGet)
                          .with(COL_TIMESTAMP, dateToPutAndToGet)
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        final ValueTuple valueToPut2 =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        testMap.put(new CPutQuery(valueToPut));
        testMap.put(new CPutQuery(valueToPut2));
        final ColumnsSet set = new ColumnsSet()
                .with(COL_QUOTES.getName())
                .with(COL_TIMESTAMP.getName());

        final Collection<CFilter> filters = new ArrayList<>();

        filters.add(CFilters.eq(COL_QUOTES.getName(), quoteToPutAndToGet));
        final CQuery query = CQueryBuilder
                .createQuery()
                .of(set)
                .withFilters(filters)
                .end();
        final List<ValueTuple> returnValue = Lists.newArrayList(testMap.get(query));
        assertEquals(1, returnValue.size());

        final ValueTuple value = returnValue.get(0);
        assertEquals(value.getColumns().get(COL_QUOTES.getName()), quoteToPutAndToGet);
        assertEquals(value.getColumns().get(COL_TIMESTAMP.getName()), dateToPutAndToGet);
    }

    @Test
    public void getTrivialTypeAllowFilteringTest() {
        testMap = new CMapSync(THREE_COLUMN_SCHEME, cassandraFactory);

        final String quoteToPutAndToGet = newQuote();

        final DateTime dateToPutAndToGet = DateTime.now(DateTimeZone.UTC);
        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, quoteToPutAndToGet)
                          .with(COL_TIMESTAMP, dateToPutAndToGet)
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        final ValueTuple valueToPut2 =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        testMap.put(new CPutQuery(valueToPut));
        testMap.put(new CPutQuery(valueToPut2));

        final CQuery query = CQueryBuilder
                .createQuery()
                .of(COL_QUOTES, COL_TIMESTAMP)
                .addFilters()
                .eq(COL_QUOTES, quoteToPutAndToGet)
                .noMoreFilters()
                .allowFiltering()
                .end();

        final List<ValueTuple> returnValue = Lists.newArrayList(testMap.get(query));
        assertEquals(1, returnValue.size());
        final ValueTuple value = returnValue.get(0);
        assertEquals(value.get(COL_QUOTES), quoteToPutAndToGet);
        assertEquals(value.get(COL_TIMESTAMP), dateToPutAndToGet);
    }

    @Test
    public void removeTrivialTypeTest() {
        testMap = new CMapSync(THREE_COLUMN_SCHEME, cassandraFactory);

        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());
        final ValueTuple valueToPut2 =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        testMap.put(new CPutQuery(valueToPut));
        testMap.put(new CPutQuery(valueToPut2));

        final Collection<CFilter> filters = Lists.newArrayList(
                CFilters.eq(
                        COL_QUOTES.getName(),
                        valueToPut.getColumns().get(COL_QUOTES.getName())
                )
        );

        final CQuery query = CQueryBuilder
                .createRemoveQuery()
                .withFilters(filters)
                .end();

        testMap.remove(query);

        final List<ValueTuple> returnValue = Lists.newArrayList(testMap.all());

        assertEquals(1, returnValue.size());
        assertTrue(returnValue.contains(valueToPut2));
        assertFalse(returnValue.contains(valueToPut));
    }

    @Test
    public void multipleRemoveTrivialTypeTest() {
        testMap = new CMapSync(THREE_COLUMN_SCHEME, cassandraFactory);

        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        final ValueTuple valueToPut2 =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        testMap.put(new CPutQuery(valueToPut));
        testMap.put(new CPutQuery(valueToPut2));

        final CQuery query = CQueryBuilder
                .createRemoveQuery()
                .addFilters()
                .eq(COL_QUOTES, valueToPut.getColumns().get(COL_QUOTES.getName()))
                .noMoreFilters()
                .end();

        testMap.remove(query);

        final List<ValueTuple> returnValue = Lists.newArrayList(testMap.all());
        assertEquals(1, returnValue.size());
        assertTrue(returnValue.contains(valueToPut2));
    }

    @Test
    public void multipleRemoveTrivialTypeWithNColumnSetTest() {
        testMap = new CMapSync(THREE_COLUMN_SCHEME, cassandraFactory);

        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());
        final ValueTuple valueToPut2 =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        testMap.put(new CPutQuery(valueToPut));
        testMap.put(new CPutQuery(valueToPut2));

        final Collection<CFilter> filters = Lists.newArrayList(
                CFilters.eq(
                        COL_QUOTES.getName(),
                        valueToPut.getColumns().get(COL_QUOTES.getName())
                )
        );

        final CQuery query = CQueryBuilder
                .createRemoveQuery()
                .withFilters(filters)
                .end();

        testMap.remove(query);

        final List<ValueTuple> returnValue = Lists.newArrayList(testMap.all());

        assertEquals(1, returnValue.size());
        assertTrue(returnValue.contains(valueToPut2));
        assertFalse(returnValue.contains(valueToPut));
    }

    @Test
    public void putWithTtlTest() throws InterruptedException {
        testMap = new CMapSync(THREE_COLUMN_SCHEME, cassandraFactory);

        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        testMap.put(new CPutQuery(valueToPut, Optional.of(1)));

        sleep(2000);

        final List<ValueTuple> returnValue = Lists.newArrayList(testMap.all());
        assertEquals(0, returnValue.size());
    }

    @Test
    public void simpleCountTest() throws InterruptedException {
        generateTableEntries(testMap, 20);

        final CQuery query = CQueryBuilder
                .createCountQuery()
                .end();

        assertEquals(20L, testMap.count(query));
    }

    @Test
    public void filterCountTest() throws InterruptedException {
        generateTableEntries(testMap, 20);

        final CQuery query = CQueryBuilder
                .createCountQuery()
                .addFilters()
                .eq(COL_QUOTES, newQuote())
                .noMoreFilters()
                .end();

        assertEquals(0L, testMap.count(query));
    }

    @Test
    public void orderingByClusteringAndPartitionKeysTest() {
        final TableScheme scheme = new CMapBuilder("RingOfPowerOrderings")
                .addColumn(COL_QUOTES)
                .addColumn(COL_TIMESTAMP)
                .end()
                .setPartition(COL_QUOTES)
                .setClustering(COL_TIMESTAMP)
                .buildScheme();

        testMap = new CMapSync(scheme, cassandraFactory);

        // partition:
        {
            final CQuery query = CQueryBuilder
                    .createQuery()
                    .of(COL_QUOTES)
                    .addFilters()
                    .eq(COL_QUOTES, newQuote())
                    .noMoreFilters()
                    .orderingBy(COL_QUOTES, Orderings.ASC)
                    .end();

            testMap.get(query);
        }

        // clustering:
        {
            final CQuery query = CQueryBuilder
                    .createQuery()
                    .of(COL_QUOTES)
                    .addFilters()
                    .eq(COL_QUOTES, newQuote())
                    .noMoreFilters()
                    .orderingBy(COL_TIMESTAMP, Orderings.ASC)
                    .end();

            testMap.get(query);
        }
    }

    @Test
    public void dateTest() {
        final String quote = newQuote();
        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, quote)
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC));

        testMap.put(new CPutQuery(valueToPut));

        final CQuery query = CQueryBuilder
                .createRemoveQuery()
                .addFilters()
                .eq(COL_QUOTES, quote)
                .noMoreFilters()
                .end();

        List<ValueTuple> value = Lists.newArrayList(testMap.get(query));
        //TODO: WTF?
    }

    @Test
    public void removeTrivialTypeSeveralPrimaryKeysTest() {
        final TableScheme scheme = new CMapBuilder("table")
                .addColumn(COL_QUOTES)
                .addColumn(COL_TIMESTAMP)
                .addColumn(COL_QUOTE_UUID)
                .end()
                .setPartition(COL_QUOTES, COL_QUOTE_UUID)
                .withoutClustering().buildScheme();
        testMap = new CMapSync(scheme, cassandraFactory);

        final ValueTuple valueToPut =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());

        final ValueTuple valueToPut2 =
                ValueTuple.of(COL_QUOTES, newQuote())
                          .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                          .with(COL_QUOTE_UUID, UUID.randomUUID());
        testMap.put(new CPutQuery(valueToPut));
        testMap.put(new CPutQuery(valueToPut2));

        final Collection<CFilter> filters = Lists.newArrayList(
                CFilters.eq(COL_QUOTES.getName(), valueToPut.getColumns().get(COL_QUOTES.getName())),
                CFilters.eq(COL_QUOTE_UUID.getName(), valueToPut.getColumns().get(COL_QUOTE_UUID.getName())));

        final CQuery query = CQueryBuilder
                .createRemoveQuery()
                .withFilters(filters)
                .end();
        testMap.remove(query);
        final List<ValueTuple> returnValue = Lists.newArrayList(testMap.all());
        assertEquals(1, returnValue.size());
        assertTrue(returnValue.contains(valueToPut2));
        assertFalse(returnValue.contains(valueToPut));
    }

    private static void generateTableEntries(final CMapSync testMap, final int count) throws InterruptedException {
        for (int i = 0; i < count; ++i) {
            testMap.put(new CPutQuery(
                    ValueTuple.of(COL_QUOTES, newQuote())
                              .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))));

            sleep(count * 10);
        }
    }
}
