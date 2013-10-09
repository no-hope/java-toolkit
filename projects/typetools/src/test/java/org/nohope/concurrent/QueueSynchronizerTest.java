package org.nohope.concurrent;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 10/10/11 1:51 PM
 */
public class QueueSynchronizerTest {

    @Test public void availability() {
        assertFalse(new QueueSynchronizer().isAvailable());
    }

    @Test public void availability2() throws InterruptedException {
        final QueueSynchronizer<Integer> i = new QueueSynchronizer<>();
        i.set(1);
        assertTrue(i.isAvailable());
    }

    @Test public void simpleTest() throws InterruptedException {
        final QueueSynchronizer<Integer> i = new QueueSynchronizer<>();
        i.set(1);
        assertEquals((Object) 1, i.get());
        i.set(2);
        assertEquals((Object) 2, i.get());
    }

    @Test(timeout = 3000)
    public void timeout() throws InterruptedException {
        final QueueSynchronizer<Integer> i = new QueueSynchronizer<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                i.set(1);
            }
        }).start();

        assertEquals((Object) 1, i.get(1, TimeUnit.SECONDS));
    }

}
