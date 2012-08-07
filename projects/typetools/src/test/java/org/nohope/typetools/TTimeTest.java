package org.nohope.typetools;

import org.joda.time.DateTime;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Date: 07.08.12
 * Time: 17:26
 */
public class TTimeTest {
    @Test
    public void testDelta() {
        final DateTime now = DateTime.now();

        final DateTime future = now.plusSeconds(500);

        assertEquals(500, TTime.deltaInSeconds(now, future));
        assertEquals(500, TTime.deltaInSeconds(future, now));
    }
}
