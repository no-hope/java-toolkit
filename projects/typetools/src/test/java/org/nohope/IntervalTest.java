package org.nohope;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Date: 10/4/12
 * Time: 6:10 PM
 */
public class IntervalTest {
    @Test
    public void testInterval() throws Exception {
        final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0));
        assertTrue(usualIvl.contains(new DateTime(2005, 12, 3, 14, 35, 18, DateTimeZone.UTC)));
        assertFalse(usualIvl.contains(new DateTime(2005, 12, 3, 16, 35, 18, DateTimeZone.UTC)));

        final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0));
        assertTrue(midnightIvl.contains(new DateTime(2005, 12, 3, 16, 35, 18, DateTimeZone.UTC)));
        assertFalse(midnightIvl.contains(new DateTime(2005, 12, 3, 14, 35, 18, DateTimeZone.UTC)));
    }

    @Test
    public void testIntervalWithDayOfWeek() throws Exception {
        {
            final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0), 5);
            // 2012.10.5 is friday
            assertTrue(usualIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18, DateTimeZone.UTC)));
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18, DateTimeZone.UTC)));

            final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0), 4);
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18, DateTimeZone.UTC)));
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18, DateTimeZone.UTC)));
        }

        {
            final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0), 5);
            assertTrue(midnightIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18, DateTimeZone.UTC)));
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18, DateTimeZone.UTC)));

            final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0), 4);
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18, DateTimeZone.UTC)));
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18, DateTimeZone.UTC)));

        }
    }

    @Test
    public void testIntervalWithDaysOfWeek() throws Exception {
        final Set<Integer> days = new HashSet<>();
        days.add(5);
        days.add(4);

        {
            final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0), days);
            // 2012.10.5 is friday
            assertTrue(usualIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18, DateTimeZone.UTC)));
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18, DateTimeZone.UTC)));

            final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0), days);
            assertTrue(midnightIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18, DateTimeZone.UTC)));
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18, DateTimeZone.UTC)));

        }
        {
            final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0), days);
            assertTrue(usualIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18, DateTimeZone.UTC)));
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18, DateTimeZone.UTC)));

            final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0), days);
            assertTrue(midnightIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18, DateTimeZone.UTC)));
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18, DateTimeZone.UTC)));
        }
        {
            final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0), days);
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 3, 14, 35, 18, DateTimeZone.UTC)));
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 3, 16, 35, 18, DateTimeZone.UTC)));

            final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0), days);
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 3, 16, 35, 18, DateTimeZone.UTC)));
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 3, 14, 35, 18, DateTimeZone.UTC)));
        }
    }


    @Test
    public void equality() {
        final LocalTime now = new LocalTime(16, 0);
        final LocalTime later = new LocalTime(16, 1);
        final LocalTime muchLater = new LocalTime(16, 2);

        final Interval interval = new Interval(now, later);

        final Interval interval1 = new Interval(now, later);
        final Interval interval2 = new Interval(now, muchLater);
        final Interval interval3 = new Interval(muchLater, later);

        assertEquals(interval, interval);
        assertEquals(interval.hashCode(), interval.hashCode());

        //noinspection ObjectEqualsNull
        assertFalse(interval.equals(null));
        //noinspection LiteralAsArgToStringEquals, EqualsBetweenInconvertibleTypes
        assertFalse(interval.equals("test"));

        assertEquals(interval, interval1);
        assertEquals(interval.hashCode(), interval1.hashCode());

        assertFalse(interval.equals(interval2));
        assertFalse(interval.equals(interval3));
    }


    @Test
    public void serialization() {
        //FIXME : test-utils causes cyclic dependency here...
    }
}
