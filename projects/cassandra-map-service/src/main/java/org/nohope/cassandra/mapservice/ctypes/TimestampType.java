package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.joda.time.DateTime;
import org.nohope.cassandra.mapservice.CTypeConverter;

import java.util.Date;

/**
 */
public class TimestampType extends CTypeConverter<DateTime, Date> {
    public static final TimestampType INSTANCE = new TimestampType();

    private TimestampType() {
    }

    @Override
    public CType getCType() {
        return CType.TIMESTAMP;
    }

    @Override
    public DateTime readValue(Row result, String name) {
        return new DateTime(result.getDate(name));
    }

    @Override
    protected Date convert(DateTime value) {
        DateTime utcDateTime = new DateTime(value);
        return utcDateTime.toDate();
    }
}
