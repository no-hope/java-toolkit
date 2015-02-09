package org.nohope.cassandra.mapservice;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.base.Optional;
import org.junit.Ignore;
import org.junit.Test;
import org.nohope.cassandra.factory.CassandraFactory;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.columns.joda.CDateTimeStringColumn;

import static org.junit.Assert.assertNotNull;

/**
 */
public final class CMapServiceIT {
    private static final CDateTimeStringColumn TIME = CDateTimeStringColumn.of("time");

    @Ignore
    @Test
    public void timeUUIDTest() throws Exception {
        final TableScheme scheme = new CMapBuilder("testTable")
                .addColumn(TIME)
                .end()
                .setPartition(TIME)
                .withoutClustering()
                .withoutStatic()
                .buildScheme();

        ITHelpers.withFactory(new ITHelpers.FactoryHandler() {
            @Override
            public void handle(final CassandraFactory f) {
                final CMapService service = new CMapService(f, scheme);
                final CMapSync map = service.getMap("testTable");
                map.put(new CPutQuery(Optional.of(ValueTuple.of("time", QueryBuilder.fcall("now"))),
                        Optional.<Integer> absent()));
                assertNotNull(map.get(CQueryBuilder.createQuery().of(CDateTimeStringColumn.of("time")).end()));
            }
        });
    }
}
