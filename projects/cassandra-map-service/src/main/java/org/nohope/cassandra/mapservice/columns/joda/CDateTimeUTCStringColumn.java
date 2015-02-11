package org.nohope.cassandra.mapservice.columns.joda;

import org.joda.time.DateTime;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.custom.UTCDateTimeType;

/**
 */
public final class CDateTimeUTCStringColumn extends CColumn<DateTime, String> {

    private CDateTimeUTCStringColumn(final String name) {
        super(name, UTCDateTimeType.INSTANCE);
    }

    public static CDateTimeUTCStringColumn of(final String name) {
        return new CDateTimeUTCStringColumn(name);
    }
}
