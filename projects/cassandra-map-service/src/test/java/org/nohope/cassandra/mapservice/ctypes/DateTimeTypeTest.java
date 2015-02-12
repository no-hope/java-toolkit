package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.custom.DateTimeType;
import org.nohope.cassandra.mapservice.ctypes.custom.UTCDateTimeType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 */
public class DateTimeTypeTest {
    @Test
    public void testConversion() {
        final DateTime source = DateTime.now(DateTimeZone.UTC);
        final String serialized = DateTimeType.INSTANCE.asCassandraValue(source);

        final Row mock = mock(Row.class);
        when(mock.getString("test")).thenReturn(serialized);
        final DateTime restored = DateTimeType.INSTANCE.readValue(mock, CColumn.of("test", DateTimeType.INSTANCE));

        assertEquals(DateTimeZone.UTC, restored.getZone());
        assertEquals(source, restored);
    }

    @Test
    public void testForceUTCConversionDateTime() {
        final DateTime notUTCDateTime = DateTime.now();

        final String serialized = UTCDateTimeType.INSTANCE.asCassandraValue(notUTCDateTime);
        final Row mock = mock(Row.class);
        when(mock.getString("test")).thenReturn(serialized);
        final DateTime restored = UTCDateTimeType.INSTANCE.readValue(mock, CColumn.of("test", DateTimeType.INSTANCE));

        assertEquals(DateTimeZone.UTC, restored.getZone());
        assertNotEquals(notUTCDateTime, restored);
    }
}
