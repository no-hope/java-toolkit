package org.nohope.cassandra.mapservice;

import com.google.common.base.Optional;
import org.junit.Test;
import org.nohope.cassandra.factory.ITHelpers;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;

/**
 */
public final class CMapServiceIT {
    private static final CColumn<UUID, UUID> TIME = CColumn.of("time", CoreConverter.TIMEUUID);

    @Test
    public void timeUUIDTest() throws Exception {
        final TableScheme scheme = new CMapBuilder("testTable")
                .addColumn(TIME)
                .end()
                .setPartition(TIME)
                .withoutClustering()
                .withoutStatic()
                .buildScheme();

        ITHelpers.withFactory(f -> {
            final CMapService service = new CMapService(f, scheme);
            final CMapSync map = service.getMap("testTable");
            map.put(new CPutQuery(Optional.of(ValueTuple.of(Value.fcall(TIME, "now"))), Optional.<Integer> absent()));
            assertNotNull(map.get(CQueryBuilder.createQuery().of(TIME).end()));
        });
    }
}
