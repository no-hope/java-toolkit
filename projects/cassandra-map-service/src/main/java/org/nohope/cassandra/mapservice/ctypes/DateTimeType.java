package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.joda.time.DateTime;
import org.nohope.cassandra.mapservice.CTypeConverter;

/**
 */
public final class DateTimeType extends CTypeConverter<DateTime, String> {
    public static final DateTimeType INSTANCE = new DateTimeType();

    private DateTimeType() {
    }

    @Override
    public CType getCType() {
        return CType.TEXT;
    }

    @Override
    public DateTime readValue(final Row result, final String name) {
        return DateTime.parse(result.getString(name));
    }

    @Override
    protected String convert(final DateTime value) {
        return value.toString();
    }
}
