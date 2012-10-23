package org.nohope;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Date: 10/4/12
 * Time: 6:10 PM
 */
public class IntervalTest {
    @Test
    public void testInterval() throws Exception {
        final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0));
        assertTrue(usualIvl.contains(new DateTime(2005, 12, 3, 14, 35, 18)));
        assertFalse(usualIvl.contains(new DateTime(2005, 12, 3, 16, 35, 18)));

        final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0));
        assertTrue(midnightIvl.contains(new DateTime(2005, 12, 3, 16, 35, 18)));
        assertFalse(midnightIvl.contains(new DateTime(2005, 12, 3, 14, 35, 18)));
    }

    @Test
    public void testIntervalWithDayOfWeek() throws Exception {
        {
            final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0), 5);
            // 2012.10.5 is friday
            assertTrue(usualIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18)));
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18)));

            final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0), 4);
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18)));
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18)));
        }

        {
            final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0), 5);
            assertTrue(midnightIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18)));
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18)));

            final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0), 4);
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18)));
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18)));

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
            assertTrue(usualIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18)));
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18)));

            final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0), days);
            assertTrue(midnightIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18)));
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18)));

        }
        {
            final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0), days);
            assertTrue(usualIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18)));
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18)));

            final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0), days);
            assertTrue(midnightIvl.contains(new DateTime(2012, 10, 5, 16, 35, 18)));
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 5, 14, 35, 18)));
        }
        {
            final Interval usualIvl = new Interval(new LocalTime(12, 0), new LocalTime(16, 0), days);
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 3, 14, 35, 18)));
            assertFalse(usualIvl.contains(new DateTime(2012, 10, 3, 16, 35, 18)));

            final Interval midnightIvl = new Interval(new LocalTime(16, 0), new LocalTime(12, 0), days);
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 3, 16, 35, 18)));
            assertFalse(midnightIvl.contains(new DateTime(2012, 10, 3, 14, 35, 18)));
        }
    }


}
