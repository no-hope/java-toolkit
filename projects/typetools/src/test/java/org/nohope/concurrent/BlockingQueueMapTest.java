package org.nohope.concurrent;

import org.junit.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 11/16/11 11:35 AM
 */
public class BlockingQueueMapTest {
    @Test(timeout = 3000)
    public void availability() throws InterruptedException, TimeoutException {
        final BlockingQueueMap<Integer, Integer> m =
                new BlockingQueueMap<>();
        // s = null
        assertFalse(m.isAvailable(1));
        m.put(1, 2);
        // s != null, available
        assertTrue(m.isAvailable(1));
        m.poll(1, new Timeout(1));
        // s != null, !available
        assertFalse(m.isAvailable(1));
    }

}
