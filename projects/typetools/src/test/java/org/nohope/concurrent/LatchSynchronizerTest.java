package org.nohope.concurrent;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 10/10/11 1:51 PM
 */
public class LatchSynchronizerTest {

    @Test public void availability() {
        assertFalse(new LatchSynchronizer().isAvailable());
    }

    @Test public void simpleTest() throws InterruptedException {
        final LatchSynchronizer<Integer> i = new LatchSynchronizer<>();
        i.set(1);
        assertEquals((Object) 1, i.get());
        i.set(2);
        assertEquals((Object) 1, i.get());
    }

    @Test(timeout = 3000)
    public void timeout() throws InterruptedException {
        final LatchSynchronizer<Integer> i = new LatchSynchronizer<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                i.set(1);
            }
        }).start();

        assertEquals((Object) 1, i.get(1, TimeUnit.SECONDS));
    }

}
