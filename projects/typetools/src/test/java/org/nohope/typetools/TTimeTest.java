package org.nohope.typetools;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.nohope.test.UtilitiesTestSupport;

import static org.junit.Assert.assertEquals;

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
}
