package org.nohope.cassandra.mapservice.ctypes.custom;

import org.joda.time.DateTime;
import org.nohope.cassandra.mapservice.ctypes.AbstractConverter;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;

/**
 */
public final class DateTimeType extends AbstractConverter<String, DateTime> {
    public static final DateTimeType INSTANCE = new DateTimeType();

    private DateTimeType() {
        super(DateTime.class, CoreConverter.TEXT);
    }

    @Override
    public String asCassandraValue(final DateTime value) {
        return value.toString();
    }

    @Override
    public DateTime asJavaValue(final String value) {
        return DateTime.parse(value);
    }
}
