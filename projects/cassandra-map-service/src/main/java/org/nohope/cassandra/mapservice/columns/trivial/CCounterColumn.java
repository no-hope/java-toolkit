package org.nohope.cassandra.mapservice.columns.trivial;

import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.TrivialType;

/**
 * Counter type wrapper
 */
public final class CCounterColumn extends CColumn<Long, Long> {

    CCounterColumn(final String name) {
        super(name, TrivialType.COUNTER);
    }

    public static CCounterColumn of(final String name) {
        return new CCounterColumn(name);
    }
}
