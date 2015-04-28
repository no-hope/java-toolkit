package org.nohope.test.stresstooltest;

import org.junit.Test;
import org.nohope.test.EnumUtils;
import org.nohope.test.stress.TimerResolution;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 20:10
 */
public class TimerResolutionTest {
    @Test
    public void enumTest() {
        EnumUtils.basicAssertions(TimerResolution.class);
        EnumUtils.assertEnumConstructor(TimerResolution.class);
    }
}
