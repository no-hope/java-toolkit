package org.nohope.cassandra.mapservice.columns.trivial;

import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;

import java.util.Date;

/**
 */
public final class CTimestampTrivialColumn extends CColumn<Date, Date> {
    private CTimestampTrivialColumn(final String name) {
        super(name, CoreConverter.TIMESTAMP);
    }

    public static CTimestampTrivialColumn of(final String name) {
        return new CTimestampTrivialColumn(name);
    }
}
