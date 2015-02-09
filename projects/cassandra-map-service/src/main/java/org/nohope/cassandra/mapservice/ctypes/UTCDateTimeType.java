package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import org.nohope.cassandra.mapservice.CTypeConverter;

/**
 */
public class UTCDateTimeType extends CTypeConverter<DateTime, String> {
    public static final UTCDateTimeType INSTANCE = new UTCDateTimeType();

    private UTCDateTimeType() {
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
        final ReadableInstant utcDateTime = new DateTime(value, DateTimeZone.UTC);
        return utcDateTime.toString();
    }
}
