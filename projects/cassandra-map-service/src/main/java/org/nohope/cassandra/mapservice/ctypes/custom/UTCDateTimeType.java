package org.nohope.cassandra.mapservice.ctypes.custom;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import org.nohope.cassandra.mapservice.ctypes.AbstractConverter;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;

/**
 */
public class UTCDateTimeType extends AbstractConverter<String, DateTime> {
    public static final UTCDateTimeType INSTANCE = new UTCDateTimeType();

    private UTCDateTimeType() {
        super(DateTime.class, CoreConverter.TEXT);
    }

    @Override
    public String asCassandraValue(final DateTime value) {
        final ReadableInstant utcDateTime = new DateTime(value, DateTimeZone.UTC);
        return utcDateTime.toString();
    }

    @Override
    public DateTime asJavaValue(final String value) {
        return DateTime.parse(value);
    }
}
