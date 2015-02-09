package org.nohope.cassandra.mapservice.columns.joda;

import org.joda.time.DateTime;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.TimestampType;

import java.util.Date;

/**
 */
public final class CDateTimeTimestampColumn extends CColumn<DateTime, Date> {
    private CDateTimeTimestampColumn(final String name) {
        super(name, TimestampType.INSTANCE);
    }

    public static CDateTimeTimestampColumn of(final String name) {
        return new CDateTimeTimestampColumn(name);
    }
}
