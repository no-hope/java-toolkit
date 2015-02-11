package org.nohope.cassandra.mapservice.columns.trivial;

import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;

/**
 */
public final class CLongColumn extends CColumn<Long, Long> {
    private CLongColumn(final String name) {
        super(name, CoreConverter.BIGINT);
    }

    public static CLongColumn of(final String name) {
        return new CLongColumn(name);
    }
}
