package org.nohope.cassandra.mapservice;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.cfilter.CFilters;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.cops.COperations;
import org.nohope.cassandra.mapservice.ctypes.custom.DateTimeType;
import org.nohope.cassandra.mapservice.update.CUpdate;
import org.nohope.cassandra.util.RowNotFoundException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.nohope.cassandra.mapservice.ctypes.CoreConverter.COUNTER;
import static org.nohope.cassandra.mapservice.ctypes.CoreConverter.TEXT;

/**
 */
public class CMapBatchIT {
    private static final CColumn<String, String> QUOTES_COL = CColumn.of("quotes", TEXT);
    private static final CColumn<DateTime, String> TIMESTAMP_COL = CColumn.of("timestamp", DateTimeType.INSTANCE);
    private static final String RING_OF_POWER_TABLE = "RingOfPower";
    private static final String DWARFS = "Dwarfs";
    private static final CColumn<String, String> NAME_COL = CColumn.of("name", TEXT);
    private static final CColumn<String, String> FATHER_COL = CColumn.of("father", TEXT);
    private static final String KINGS_TABLE = "Kings";
    private static final CColumn<String, String> KINGDOM_COL = CColumn.of("kingdom", TEXT);

    private CassandraFactory cassandraFactory;
    private CMapService service;

    @Before
    public void setUp() {
        cassandraFactory = ITHelpers.cassandraFactory();
        final TableScheme ringOfPower = new CMapBuilder(RING_OF_POWER_TABLE)
                .addColumn(QUOTES_COL)
                .addColumn(TIMESTAMP_COL)
                .end()
                .setPartition(QUOTES_COL)
                .withoutClustering().buildScheme();
        final TableScheme gnomes = new CMapBuilder(DWARFS)
                .addColumn(NAME_COL)
                .addColumn(FATHER_COL)
                .end()
                .setPartition(NAME_COL)
                .withoutClustering().buildScheme();
        final TableScheme kings = new CMapBuilder(KINGS_TABLE)
                .addColumn(NAME_COL)
                .addColumn(KINGDOM_COL)
                .end()
                .setPartition(NAME_COL)
                .withoutClustering().buildScheme();

        service = new CMapService(cassandraFactory, Lists.newArrayList(ringOfPower, gnomes, kings));
    }

    @After
    public void tearDown() {
        ITHelpers.destroy(cassandraFactory);
    }

    @Test
    public void batch_put_test() throws RowNotFoundException {
        final ValueTuple valueToPutInRingOfPower = ValueTuple
                .of(QUOTES_COL, "It's the job that's never started as takes longest to finish.")
                .with(TIMESTAMP_COL, DateTime.now(DateTimeZone.UTC));
        final ValueTuple valueToPutInGnomes = ValueTuple.of(NAME_COL, "Gloin")
                                                        .with(FATHER_COL, "Groin");
        final ValueTuple valueToPutInKings = ValueTuple.of(NAME_COL, "Aragorn")
                                                       .with(KINGDOM_COL, "Gondor");

        final CBatch batch = service.batch();
        batch.put(RING_OF_POWER_TABLE, new CPutQuery(valueToPutInRingOfPower));
        batch.put(DWARFS, new CPutQuery(valueToPutInGnomes));
        batch.put(KINGS_TABLE, new CPutQuery(valueToPutInKings));
        batch.apply();

        final CMapSync ringOfPower = service.getMap(RING_OF_POWER_TABLE);
        final ValueTuple resultRingOfPower =
                ringOfPower.getOne(new CQuery(QUOTES_COL, TIMESTAMP_COL));
        assertEquals(resultRingOfPower, valueToPutInRingOfPower);

        final CMapSync gnomes = service.getMap(DWARFS);
        final ValueTuple resultGnomes = gnomes.getOne(new CQuery(NAME_COL, FATHER_COL));
        assertEquals(resultGnomes, valueToPutInGnomes);

        final CMapSync kings = service.getMap(KINGS_TABLE);
        final ValueTuple resultKings = kings.getOne(new CQuery(NAME_COL, KINGDOM_COL));
        assertEquals(resultKings, valueToPutInKings);
    }

    @Test
    public void batch_remove_test() {
        final CMapSync ringOfPower = service.getMap(RING_OF_POWER_TABLE);
        final ValueTuple valueToPutInRingOfPower =
                ValueTuple.of(QUOTES_COL, "It's the job that's never started as takes longest to finish.")
                          .with(TIMESTAMP_COL, DateTime.now(DateTimeZone.UTC));
        ringOfPower.put(new CPutQuery(valueToPutInRingOfPower));

        final CMapSync dwarfs = service.getMap(DWARFS);
        final ValueTuple valueToPutInDwarfs =
                ValueTuple.of(NAME_COL, "Gloin")
                          .with(FATHER_COL, "Groin");
        dwarfs.put(new CPutQuery(valueToPutInDwarfs));

        final CMapSync kings = service.getMap(KINGS_TABLE);
        final ValueTuple valueToPutInKings =
                ValueTuple.of(NAME_COL, "Aragorn")
                          .with(KINGDOM_COL, "Gondor");
        kings.put(new CPutQuery(valueToPutInKings));

        final CQuery ringOfPowerQuery = CQueryBuilder
                .createRemoveQuery()
                .addFilters()
                .eq(QUOTES_COL, valueToPutInRingOfPower.get(QUOTES_COL))
                .eq(TIMESTAMP_COL, valueToPutInRingOfPower.get(TIMESTAMP_COL))
                .noMoreFilters()
                .end();
        final CQuery dwarfsQuery = CQueryBuilder
                .createRemoveQuery()
                .addFilters()
                .eq(NAME_COL, valueToPutInDwarfs.get(NAME_COL))
                .eq(FATHER_COL, valueToPutInDwarfs.get(FATHER_COL))
                .noMoreFilters()
                .end();
        final CQuery kingsQuery = CQueryBuilder
                .createRemoveQuery()
                .addFilters()
                .eq(NAME_COL, valueToPutInKings.get(NAME_COL))
                .eq(KINGDOM_COL, valueToPutInKings.get(KINGDOM_COL))
                .noMoreFilters()
                .end();

        final CBatch batch = service.batch();
        batch.remove(RING_OF_POWER_TABLE, ringOfPowerQuery);
        batch.remove(DWARFS, dwarfsQuery);
        batch.remove(KINGS_TABLE, kingsQuery);
        batch.apply();

        final Iterable<ValueTuple> removeRingOfPowerResults = ringOfPower.all();
        assertEquals(0, Lists.newArrayList(removeRingOfPowerResults).size());

        final Iterable<ValueTuple> removeGnomesResults = dwarfs.all();
        assertEquals(0, Lists.newArrayList(removeGnomesResults).size());

        final Iterable<ValueTuple> removeKingsResults = kings.all();
        assertEquals(0, Lists.newArrayList(removeKingsResults).size());
    }

    @Test
    public void batch_mixed_test() throws RowNotFoundException {
        final CMapSync dwarfs = service.getMap(DWARFS);
        final ValueTuple valueToPutInDwarfs =
                ValueTuple.of(NAME_COL, "Gloin")
                          .with(FATHER_COL, "Groin");
        dwarfs.put(new CPutQuery(valueToPutInDwarfs));

        final ValueTuple valueToPutInRingOfPower =
                ValueTuple.of(QUOTES_COL, "It's the job that's never started as takes longest to finish.")
                          .with(TIMESTAMP_COL, DateTime.now(DateTimeZone.UTC));
        final ValueTuple valueToPutInKings =
                ValueTuple.of(NAME_COL, "Aragorn")
                          .with(KINGDOM_COL, "Gondor");

        final CQuery dwarfsQuery = CQueryBuilder
                .createRemoveQuery()
                .addFilters()
                .eq(NAME_COL, valueToPutInDwarfs.get(NAME_COL))
                .eq(FATHER_COL, valueToPutInDwarfs.get(FATHER_COL))
                .noMoreFilters()
                .end();

        final CBatch batch = service.batch();
        batch.put(RING_OF_POWER_TABLE, new CPutQuery(valueToPutInRingOfPower));
        batch.remove(DWARFS, dwarfsQuery);
        batch.put(KINGS_TABLE, new CPutQuery(valueToPutInKings));
        batch.apply();

        final CMapSync ringOfPower = service.getMap(RING_OF_POWER_TABLE);
        final ValueTuple resultRingOfPower =
                ringOfPower.getOne(new CQuery(QUOTES_COL, TIMESTAMP_COL));
        assertEquals(resultRingOfPower, valueToPutInRingOfPower);

        final Iterable<ValueTuple> removeGnomesResults = dwarfs.all();
        assertEquals(0, Lists.newArrayList(removeGnomesResults).size());

        final CMapSync kings = service.getMap(KINGS_TABLE);
        final ValueTuple resultKings =
                kings.getOne(new CQuery(NAME_COL, KINGDOM_COL));
        assertEquals(resultKings, valueToPutInKings);
    }

    @Test(expected = CMapServiceException.class)
    public void no_such_map_test() {
        final CBatch batch = service.batch();
        batch.put("ghostMap", new CPutQuery(ValueTuple.of(CColumn.of("GhostKey", TEXT), "GhostValue")));
    }

    @Test(expected = CMapServiceException.class)
    public void no_operations_set_test() {
        service.batch().apply();
    }

    @Test(expected = CMapServiceException.class)
    public void batch_invalidation_test() {
        final ValueTuple valueToPutInRingOfPower = ValueTuple
                .of(QUOTES_COL, "It's the job that's never started as takes longest to finish.")
                .with(TIMESTAMP_COL, DateTime.now(DateTimeZone.UTC));
        final ValueTuple valueToPutInGnomes = ValueTuple.of(NAME_COL, "Gloin")
                                                        .with(FATHER_COL, "Groin");
        final ValueTuple valueToPutInKings = ValueTuple.of(NAME_COL, "Aragorn")
                                                       .with(KINGDOM_COL, "Gondor");

        final CBatch batch = service.batch();
        batch.put(RING_OF_POWER_TABLE, new CPutQuery(valueToPutInRingOfPower));
        batch.put(DWARFS, new CPutQuery(valueToPutInGnomes));
        batch.put(KINGS_TABLE, new CPutQuery(valueToPutInKings));
        batch.apply();

        batch.apply();
    }

    @Test
    public void countersTest() {
        final CColumn<String, String> c1 = CColumn.of("value1", TEXT);
        final CColumn<String, String> c2 = CColumn.of("value2", TEXT);
        final CColumn<Long, Long> c3 = CColumn.of("count", COUNTER);
        final TableScheme tableWithCounter = new CMapBuilder("counter_test")
                .addColumn(c1)
                .addColumn(c2)
                .addColumn(c3)
                .end()
                .setPartition(c1)
                .setClustering(c2)
                .buildScheme();

        service = new CMapService(cassandraFactory, Lists.newArrayList(tableWithCounter));

        // before:
        {
            final List<ValueTuple> all =
                    Lists.newArrayList(service.getMap("counter_test").all());
            assertEquals(0, all.size());
        }

        service.batch().update("counter_test",
                CUpdate.withFilters(CFilters.eq(Value.bound(c1, "123")),
                        CFilters.eq(Value.bound(c2, "456")))
                       .apply(COperations.counterIncr(Value.bound(c3, 1L))))
               .apply();

        // after:
        {
            final List<ValueTuple> all = Lists.newArrayList(service.getMap("counter_test").all());
            assertEquals(1, all.size());

            final ValueTuple value = all.get(0);
            assertEquals("123", value.get(c1));
            assertEquals("456", value.get(c2));
            assertEquals(1L, (long) value.get(c3));
        }

        service.batch().update("counter_test",
                CUpdate.withFilters(CFilters.eq(Value.bound(c1, "123")),
                        CFilters.eq(Value.bound(c2, "456")))
                       .apply(COperations.counterIncr(Value.bound(c3, 1L))))
               .apply();

        // after_update:
        {
            final List<ValueTuple> all = Lists.newArrayList(service.getMap("counter_test").all());
            assertEquals(1, all.size());

            final ValueTuple value = all.get(0);
            assertEquals("123", value.get(c1));
            assertEquals("456", value.get(c2));
            assertEquals(2L, (long) value.get(c3));
        }
    }
}
