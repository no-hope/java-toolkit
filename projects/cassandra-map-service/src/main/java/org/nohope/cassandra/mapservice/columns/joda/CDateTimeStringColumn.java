package org.nohope.cassandra.mapservice.columns.joda;

import org.joda.time.DateTime;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.custom.DateTimeType;

/**
 */
public final class CDateTimeStringColumn extends CColumn<DateTime, String> {
    private CDateTimeStringColumn(final String name) {
        super(name, DateTimeType.INSTANCE);
    }

    public static CDateTimeStringColumn of(final String name) {
        return new CDateTimeStringColumn(name);
    }
}
