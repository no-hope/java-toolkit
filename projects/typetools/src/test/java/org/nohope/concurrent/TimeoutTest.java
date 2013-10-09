package org.nohope.concurrent;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 11/16/11 12:40 PM
 */
public class TimeoutTest {
    @Test
    public void dummy() throws InterruptedException {
        final Timeout t = new Timeout(1);
        assertEquals(1000, t.milliseconds());
    }

    @Test(timeout = 3000)
    public void dummySleep() throws InterruptedException {
        new Timeout(1).sleep();
    }
}
