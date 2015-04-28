package org.nohope.test.stresstooltest;

import org.junit.Test;
import org.nohope.test.UtilityClassUtils;
import org.nohope.test.stress.util.TimeUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.nohope.test.stress.util.TimeUtils.throughputTo;
import static org.nohope.test.stress.util.TimeUtils.timeTo;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-01-21 11:38
 */
public class TimeUtilsTest  {

    @Test
    public void isUtility() throws Exception {
        UtilityClassUtils.assertUtilityClass(TimeUtils.class);
    }

    @Test
    public void sample() {
        double test = 8.64e+13;
        assertEquals(test, timeTo(test, TimeUnit.NANOSECONDS), 1e-3);
        assertEquals(8.64e+10, timeTo(test, TimeUnit.MICROSECONDS), 1e-3);
        assertEquals(8.64e+7, timeTo(test, TimeUnit.MILLISECONDS), 1e-3);
        assertEquals(8.64e+4, timeTo(test, TimeUnit.SECONDS), 1e-3);
        assertEquals(1440, timeTo(test, TimeUnit.MINUTES), 1e-3);
        assertEquals(24, timeTo(test, TimeUnit.HOURS), 1e-3);
        assertEquals(1, timeTo(test, TimeUnit.DAYS), 1e-3);

        test = 1;
        assertEquals(1, throughputTo(test, TimeUnit.NANOSECONDS), 1e-3);
        assertEquals(1e3, throughputTo(test, TimeUnit.MICROSECONDS), 1e-3);
        assertEquals(1e6, throughputTo(test, TimeUnit.MILLISECONDS), 1e-3);
        assertEquals(1e9, throughputTo(test, TimeUnit.SECONDS), 1e-3);
        assertEquals(6e+10, throughputTo(test, TimeUnit.MINUTES), 1e-3);
        assertEquals(3.6e+12, throughputTo(test, TimeUnit.HOURS), 1e-3);
        assertEquals(8.64e+13, throughputTo(test, TimeUnit.DAYS), 1e-3);
    }
}
