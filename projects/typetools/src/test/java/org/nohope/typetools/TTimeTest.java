package org.nohope.typetools;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.nohope.test.UtilitiesTestSupport;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

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

    @Test
    public void comparing() {
        final XMLGregorianCalendar d1 = TTime.xmlCalendarUtcNow();
        final XMLGregorianCalendar d2 = TTime.xmlCalendarUtcNow();

        assertTrue(TTime.eq(d1, d1));
        assertFalse(TTime.eq(d2, d1));
        assertFalse(TTime.eq(d1, d2));

        assertFalse(TTime.ne(d1, d1));
        assertTrue(TTime.ne(d2, d1));
        assertTrue(TTime.ne(d1, d2));

        assertTrue(TTime.lte(d1, d1));
        assertTrue(TTime.lte(d1, d2));
        assertFalse(TTime.lte(d2, d1));

        assertTrue(TTime.gte(d1, d1));
        assertTrue(TTime.gte(d2, d1));
        assertFalse(TTime.gte(d1, d2));

        assertFalse(TTime.lt(d1, d1));
        assertFalse(TTime.lt(d2, d1));
        assertTrue(TTime.lt(d1, d2));

        assertFalse(TTime.gt(d1, d1));
        assertTrue(TTime.gt(d2, d1));
        assertFalse(TTime.gt(d1, d2));
    }

    private static void assertIsUTC(final XMLGregorianCalendar c) {
        assertEquals("GMT+00:00", c.toGregorianCalendar().getTimeZone().getID());
    }
}
