package org.nohope.cassandra.mapservice;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.CPreparedGet.PreparedGetExecutor;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.custom.UTCDateTimeType;
import org.nohope.cassandra.util.RowNotFoundException;

import java.util.List;

import static org.junit.Assert.*;
import static org.nohope.cassandra.mapservice.QuoteTestGenerator.newQuote;
import static org.nohope.cassandra.mapservice.ctypes.CoreConverter.TEXT;

/**
 */
public class PreparedStatementsIT {

    private static final String RING_OF_POWER_TABLE = "RingOfPower";
    private static final CColumn<String, String> COL_QUOTES = CColumn.of("quotes", TEXT);
    private static final CColumn<DateTime, String> COL_TIMESTAMP = CColumn.of("timestamp", UTCDateTimeType.INSTANCE);
    private static final TableScheme SCHEME = new CMapBuilder(RING_OF_POWER_TABLE)
            .addColumn(COL_QUOTES)
            .addColumn(COL_TIMESTAMP)
            .end()
            .setPartition(COL_QUOTES)
            .setClustering(COL_TIMESTAMP)
            .withoutStatic()
            .buildScheme();

    private CMapService mapService;
    private CassandraFactory cassandraFactory;

    @Before
    public void setUp() {
        cassandraFactory = ITHelpers.cassandraFactory();
        mapService = new CMapService(cassandraFactory, SCHEME);
    }

    @After
    public void tearDown() {
        ITHelpers.destroy(cassandraFactory);
    }

    @Test
    public void multiple_same_column() throws RowNotFoundException {
        final CMapSync map = mapService.getMap(RING_OF_POWER_TABLE);
        final String quote = newQuote();

        final DateTime ts = DateTime.now(DateTimeZone.UTC);
        final ValueTuple valueToPut = ValueTuple
                .of(COL_QUOTES, quote)
                .with(COL_TIMESTAMP, ts);

        map.put(new CPutQuery(valueToPut));
        final CQuery query = CQueryBuilder.createPreparedQuery()
                                          .of(COL_QUOTES, COL_TIMESTAMP)
                                          .addFilters()
                                          .eq(COL_QUOTES)
                                          .gte(COL_TIMESTAMP)
                                          .lte(COL_TIMESTAMP)
                                          .noMoreFilters().noFiltering();
        final CPreparedGet preparedQuery = mapService.prepareGet(RING_OF_POWER_TABLE, query);
        final ValueTuple value = preparedQuery.bind()
                                              .bindTo(COL_QUOTES, quote)
                                              .bindTo(COL_TIMESTAMP, ts)
                                              .stopBinding()
                                              .one();

        assertEquals(value, valueToPut);
    }

    @Test
    public void getPreparedStatement() {
        final CMapSync map = mapService.getMap(RING_OF_POWER_TABLE);
        final String quote = newQuote();
        final ValueTuple valueToPut = ValueTuple
                .of(COL_QUOTES, quote)
                .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC));

        map.put(new CPutQuery(valueToPut));

        final CQuery query = CQueryBuilder.createPreparedQuery()
                                          .of(COL_QUOTES, COL_TIMESTAMP)
                                          .addFilters()
                                          .eq(COL_QUOTES)
                                          .lte(COL_TIMESTAMP)
                                          .noMoreFilters().noFiltering();
        final CPreparedGet preparedQuery = mapService.prepareGet(RING_OF_POWER_TABLE, query);
        final CPreparedGet.PreparedGetExecutor preparedExecutor =
                preparedQuery.bind()
                             .bindTo(COL_QUOTES, quote)
                             .bindTo(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                             .stopBinding();

        final List<ValueTuple> values =
                Lists.newArrayList(preparedExecutor.all());

        assertEquals(1, values.size());
        assertTrue(values.contains(valueToPut));

        final ValueTuple valueToPut2 = ValueTuple
                .of(COL_QUOTES, newQuote())
                .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC));

        map.put(new CPutQuery(valueToPut2));

        final List<ValueTuple> valueTuples =
                Lists.newArrayList(preparedExecutor.all());
        assertEquals(1, valueTuples.size());
        assertTrue(valueTuples.contains(valueToPut));
    }

    @Test
    public void getPreparedOrderTest() throws RowNotFoundException {
        final CMapSync map = mapService.getMap(RING_OF_POWER_TABLE);
        final String quote = newQuote();
        final ValueTuple valueToPut = ValueTuple
                .of(COL_QUOTES, quote)
                .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC));

        map.put(new CPutQuery(valueToPut));

        final CQuery query = CQueryBuilder.createPreparedQuery()
                                          .of(COL_QUOTES, COL_TIMESTAMP)
                                          .addFilters()
                                          .eq(COL_QUOTES)
                                          .lte(COL_TIMESTAMP)
                                          .noMoreFilters().noFiltering();

        final CPreparedGet preparedQuery =
                mapService.prepareGet(RING_OF_POWER_TABLE, query);

        final ValueTuple value = preparedQuery.bind()
                                              .bindTo(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                                              .bindTo(COL_QUOTES, quote)
                                              .stopBinding()
                                              .one();

        assertEquals(valueToPut, value);
    }

    @Test(expected = CQueryException.class)
    public void getPreparedStatementBindNotAllColumns() {
        final CQuery query = CQueryBuilder.createPreparedQuery()
                                          .of(COL_QUOTES, COL_TIMESTAMP)
                                          .addFilters()
                                          .eq(COL_QUOTES)
                                          .lte(COL_TIMESTAMP)
                                          .noMoreFilters().noFiltering();

        final CPreparedGet preparedQuery =
                mapService.prepareGet(RING_OF_POWER_TABLE, query);

        preparedQuery.bind()
                     .bindTo(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                     .stopBinding().all();
    }

    @Test(expected = CQueryException.class)
    public void getPreparedStatementWrongColumn() {
        final CQuery query = CQueryBuilder.createPreparedQuery()
                                          .of(COL_QUOTES, COL_TIMESTAMP)
                                          .addFilters()
                                          .eq(COL_QUOTES)
                                          .lte(COL_TIMESTAMP)
                                          .noMoreFilters().noFiltering();

        final CPreparedGet preparedQuery =
                mapService.prepareGet(RING_OF_POWER_TABLE, query);
        preparedQuery.bind().bindTo(CColumn.of("god", TEXT), (String) null).stopBinding().all();
    }

    @Test
    public void geOnePreparedStatement() throws RowNotFoundException {
        final CMapSync map = mapService.getMap(RING_OF_POWER_TABLE);
        final String quote = newQuote();
        final ValueTuple valueToPut = ValueTuple
                .of(COL_QUOTES, quote)
                .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC));

        map.put(new CPutQuery(valueToPut));
        final CQuery query = CQueryBuilder.createPreparedQuery()
                                          .of(COL_QUOTES, COL_TIMESTAMP)
                                          .addFilters()
                                          .eq(COL_QUOTES)
                                          .lte(COL_TIMESTAMP)
                                          .noMoreFilters().noFiltering();

        final CPreparedGet preparedQuery =
                mapService.prepareGet(RING_OF_POWER_TABLE, query);

        final PreparedGetExecutor prepared =
                preparedQuery.bind()
                             .bindTo(COL_QUOTES, quote)
                             .bindTo(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                             .stopBinding();

        final ValueTuple value = prepared.one();
        assertEquals(valueToPut, value);

        final ValueTuple valueToPut2 = ValueTuple
                .of(COL_QUOTES, newQuote())
                .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC));

        map.put(new CPutQuery(valueToPut2));
        final ValueTuple one = prepared.one();
        assertEquals(valueToPut, one);
    }

    @Test(expected = CQueryException.class)
    public void getOnePreparedStatementBindNotAllColumns() throws RowNotFoundException {
        final CQuery query = CQueryBuilder.createPreparedQuery()
                                          .of(COL_QUOTES, COL_TIMESTAMP)
                                          .addFilters()
                                          .eq(COL_QUOTES)
                                          .lte(COL_TIMESTAMP)
                                          .noMoreFilters().noFiltering();

        final CPreparedGet preparedQuery =
                mapService.prepareGet(RING_OF_POWER_TABLE, query);
        preparedQuery.bind()
                     .bindTo(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                     .stopBinding().one();
    }

    @Test
    public void prepared_get_multiple_times() throws RowNotFoundException {
        final CQuery query = CQueryBuilder
                .createPreparedQuery()
                .of(COL_QUOTES, COL_TIMESTAMP)
                .addFilters()
                .eq(COL_QUOTES)
                .lte(COL_TIMESTAMP)
                .noMoreFilters().noFiltering();
        final CQuery delQuery = CQueryBuilder
                .createPreparedRemoveQuery()
                .addFilters()
                .eq(COL_QUOTES)
                .noMoreFilters().noFiltering();

        final CPreparedGet preparedQuery = mapService.prepareGet(RING_OF_POWER_TABLE, query);
        final CPreparedRemove preparedDelQuery =
                mapService.prepareRemove(RING_OF_POWER_TABLE, delQuery);

        final String quote = newQuote();
        final DateTime time = DateTime.now(DateTimeZone.UTC);

        try {
            preparedQuery.bind()
                         .bindTo(COL_QUOTES, quote)
                         .bindTo(COL_TIMESTAMP, time)
                         .stopBinding()
                         .one();
            fail();
        } catch (final RowNotFoundException ignored) {
        }

        final CMapSync map = mapService.getMap(RING_OF_POWER_TABLE);
        map.put(CQueryBuilder.createPutQuery()
                             .addValueTuple(ValueTuple.of(COL_QUOTES, quote)
                                                      .with(COL_TIMESTAMP, time))
                             .end());

        preparedQuery.bind()
                     .bindTo(COL_QUOTES, quote)
                     .bindTo(COL_TIMESTAMP, time)
                     .stopBinding()
                     .one();

        preparedDelQuery.bind()
                        .bindTo(COL_QUOTES, quote)
                        .stopBinding()
                        .execute();

        try {
            preparedQuery.bind()
                         .bindTo(COL_QUOTES, quote)
                         .bindTo(COL_TIMESTAMP, time)
                         .stopBinding()
                         .one();
            fail();
        } catch (final RowNotFoundException ignored) {
        }
    }

    //  @Test(expected = CQueryException.class, expectedExceptionsMessageRegExp = "No such key as god. Has keys .*")
    //TODO: exception message
    @Test(expected = CQueryException.class)
    public void getOnePreparedStatementWrongColumn() throws RowNotFoundException {
        final CQuery query =
                CQueryBuilder.createPreparedQuery()
                             .of(COL_QUOTES, COL_TIMESTAMP)
                             .addFilters()
                             .eq(COL_QUOTES)
                             .lte(COL_TIMESTAMP)
                             .noMoreFilters().noFiltering();

        final CPreparedGet preparedQuery = mapService.prepareGet(RING_OF_POWER_TABLE, query);
        preparedQuery.bind().bindTo(CColumn.of("god", TEXT), (String) null).stopBinding().one();
    }

    @Test
    public void removePreparedStatement() {
        final CMapSync map = mapService.getMap(RING_OF_POWER_TABLE);
        final String quote = newQuote();
        final ValueTuple valueToPut = ValueTuple
                .of(COL_QUOTES, quote)
                .with(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC));

        map.put(new CPutQuery(valueToPut));

        final CQuery query = CQueryBuilder
                .createPreparedRemoveQuery()
                .addFilters()
                .eq(COL_QUOTES)
                .noMoreFilters().noFiltering();

        final CPreparedRemove preparedQuery =
                mapService.prepareRemove(RING_OF_POWER_TABLE, query);

        final CPreparedRemove.PreparedRemoveExecutor prepared =
                preparedQuery.bind()
                             .bindTo(COL_QUOTES, quote)
                             .stopBinding();

        prepared.execute();
        assertEquals(0, Lists.newArrayList(map.all()).size());
        map.put(new CPutQuery(valueToPut));
        prepared.execute();
        assertEquals(0, Lists.newArrayList(map.all()).size());
    }

    @Test
    public void putPreparedStatement() {
        final CMapSync map = mapService.getMap(RING_OF_POWER_TABLE);
        final CPreparedPut preparedQuery = mapService.preparePut(RING_OF_POWER_TABLE);
        final CPreparedPut.PreparedPutExecutor preparedExecutor =
                preparedQuery.bind()
                             .bindTo(COL_QUOTES, newQuote())
                             .bindTo(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                             .stopBinding();

        preparedExecutor.execute();
        assertEquals(1, Lists.newArrayList(map.all()).size());
        preparedQuery.bind()
                     .bindTo(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                     .bindTo(COL_QUOTES, newQuote())
                     .stopBinding()
                     .execute();

        assertEquals(2, Lists.newArrayList(map.all()).size());
    }

    @Test
    public void batchStatementTest() {
        final CMapSync map = mapService.getMap(RING_OF_POWER_TABLE);
        final CQuery query =
                CQueryBuilder.createPreparedRemoveQuery()
                             .addFilters()
                             .eq(COL_QUOTES)
                             .noMoreFilters()
                             .noFiltering();

        final CPreparedPut preparedPutQuery =
                mapService.preparePut(RING_OF_POWER_TABLE);
        final CPreparedRemove preparedRemoveQuery =
                mapService.prepareRemove(RING_OF_POWER_TABLE, query);
        final CPreparedPut.PreparedPutExecutor preparedPutExecutor =
                preparedPutQuery.bind()
                                .bindTo(COL_QUOTES, newQuote())
                                .bindTo(COL_TIMESTAMP, DateTime.now(DateTimeZone.UTC))
                                .stopBinding();

        final CPreparedRemove.PreparedRemoveExecutor preparedRemoveExecutor =
                preparedRemoveQuery.bind()
                                   .bindTo(COL_QUOTES, newQuote())
                                   .stopBinding();

        final CBatch batch = mapService.batch();

        batch.put(preparedPutExecutor);
        batch.remove(preparedRemoveExecutor);
        batch.apply();
        assertEquals(1, Lists.newArrayList(map.all()).size());
    }
}
