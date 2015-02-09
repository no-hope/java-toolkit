package org.nohope.cassandra.mapservice.columns.trivial;

import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.TrivialType;

import java.util.UUID;

/**
 */
public final class CUUIDColumn extends CColumn<UUID, UUID> {
    private CUUIDColumn(final String name) {
        super(name, TrivialType.UUID);
    }

    public static CUUIDColumn of(final String name) {
        return new CUUIDColumn(name);
    }
}
