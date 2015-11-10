package org.nohope.cassandra.mapservice.ctypes.custom;

import org.joda.time.DateTime;
import org.nohope.cassandra.mapservice.ctypes.AbstractConverter;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;

import java.util.Date;

/**
 */
public class TimestampType extends AbstractConverter<Date, DateTime> {
    public static final TimestampType INSTANCE = new TimestampType();

    private TimestampType() {
        super(DateTime.class, CoreConverter.TIMESTAMP);
    }

    @Override
    public Date asCassandraValue(final DateTime value) {
        final DateTime utcDateTime = new DateTime(value);
        return utcDateTime.toDate();
    }

    @Override
    public DateTime asJavaValue(final Date value) {
        return new DateTime(value);
    }
}
