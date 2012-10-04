package org.nohope;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

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
}
