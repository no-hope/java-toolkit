package org.nohope.typetools;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.nohope.test.UtilitiesTestSupport;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Date: 07.08.12
 * Time: 17:26
 */
public class TTimeTest extends UtilitiesTestSupport {
    @Override
    protected Class<?> getUtilityClass() {
        return TTime.class;
    }

    @Test
    public void testDelta() {
        final DateTime now = DateTime.now(DateTimeZone.UTC);

        final DateTime future = now.plusSeconds(500);

        assertEquals(500, TTime.deltaInSeconds(now, future));
        assertEquals(500, TTime.deltaInSeconds(future, now));
    }

    @Test
    public void defaultTimeZone() {
        final DateTimeZone timeZone01 = DateTimeZone.getDefault();

        TTime.setUtcTimezone();
        final DateTimeZone timeZone11 = DateTimeZone.getDefault();
        final TimeZone timeZone12 = TimeZone.getDefault();
        assertEquals(TTime.UTC_ID, timeZone11.getID());
        assertEquals(TTime.UTC_ID, timeZone12.getID());

        TTime.setDefaultTimezone(timeZone01.getID());
    }


    @Test
    public void xmlCalendar() {
        final XMLGregorianCalendar now = TTime.xmlCalendarUtcNow();
        assertNotNull(now);
        assertIsUTC(now);
        assertIsUTC(TTime.toXmlUtcCalendar(new Date()));
    }

    private static void assertIsUTC(final XMLGregorianCalendar c) {
        assertEquals("GMT+00:00", c.toGregorianCalendar().getTimeZone().getID());
    }
}
