package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;

import java.util.Date;

/**
 */
public final class TimestampTrivialType extends TrivialType<Date> {
    TimestampTrivialType() {
        super(CType.TIMESTAMP);
    }

    @Override
    public Date readValue(final Row result, final String name) {
        return result.getDate(name);
    }
}
