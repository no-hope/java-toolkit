package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.joda.time.DateTime;
import org.nohope.reflection.TypeReference;

/**
 */
public final class DateTimeType implements Converter<String, DateTime> {
    public static final DateTimeType INSTANCE = new DateTimeType();

    private DateTimeType() {
    }

    @Override
    public String asCassandraValue(final DateTime value) {
        return value.toString();
    }

    @Override
    public DateTime asJavaValue(final String value) {
        return DateTime.parse(value);
    }

    @Override
    public TypeDescriptor<String> getCassandraType() {
        return CoreConverter.TEXT.getCassandraType();
    }

    @Override
    public TypeReference<DateTime> getJavaType() {
        return TypeReference.erasure(DateTime.class);
    }

    @Override
    public DateTime readValue(final Row result, final String name) {
        return asJavaValue(result.getString(name));
    }
}
