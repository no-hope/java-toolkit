package org.nohope.test.stress;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-12-29 19:30
 */
public class StatTest {

    @Test
    public void repr() {
        final Stat s = new Stat(TimerResolution.NANOSECONDS, "test");
        assertEquals("<No result collected>",s.toString());
        s.calculate();
        assertNotEquals("<No result collected>", s.toString());
    }
}
