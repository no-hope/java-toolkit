package org.nohope.concurrent;

import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 11/16/11 11:35 AM
 */
public class BlockingMapTest {

    @Test(timeout = 10000)
    public void emptiness() throws InterruptedException {
        final BlockingMap<Integer, Integer> m = new BlockingMap<>();
        assertTrue(m.isEmpty());
        m.put(1, 2);
        assertFalse(m.isEmpty());
        m.take(1);
        assertTrue(m.isEmpty());
    }

    @Ignore("depends on cpu")
    @Test(timeout = 10000)
    public void clean() throws InterruptedException {
        final BlockingMap<Integer, Integer> m =
                new BlockingMap<>();
        assertTrue(m.isEmpty());
        m.put(1, 2);
        assertFalse(m.isEmpty());
        m.clear();
        assertTrue(m.isEmpty());

        final Thread consumer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    m.take(1);
                } catch (InterruptedException e) {
                    /* just ignore */
                }
            }
        });
        consumer.start();

        // let's wait till thread became waiting
        while (consumer.getState() != Thread.State.WAITING) {
            Thread.sleep(1);
        }

        m.clear();
        consumer.join(); // should finish
        assertTrue(m.isEmpty());
    }

    @Test(timeout = 10000)
    public void availability() throws InterruptedException {
        final BlockingMap<Integer, Integer> m =
                new BlockingMap<>();
        assertFalse(m.isAvailable(1));
        m.put(1, 2);
        assertTrue(m.isAvailable(1));
        m.take(1);
        assertFalse(m.isAvailable(1));
    }

    @Test(timeout = 10000)
    public void simpleTake() throws InterruptedException {
        final BlockingMap<Integer, Integer> m =
                new BlockingMap<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                m.put(1, 2);
            }
        }).start();

        assertEquals((Object) 2, m.take(1));
    }

    @Test(timeout = 10000)
    public void simplePoll() throws InterruptedException, TimeoutException {
        final BlockingMap<Integer, Integer> m =
                new BlockingMap<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                m.put(1, 2);
            }
        }).start();

        assertEquals((Object) 2, m.poll(1, new Timeout(1)));
    }

    @Test(expected = TimeoutException.class)
    public void simplePollTimeout()
            throws InterruptedException, TimeoutException {
        final BlockingMap<Integer, Integer> m =
                new BlockingMap<>();
        assertEquals((Object) 2, m.poll(1, new Timeout(1)));
    }

    /* Unsupported operations list */

    @Test(expected = UnsupportedOperationException.class)
    public void get() throws InterruptedException {
        new BlockingMap<Integer, Integer>().get(1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void keySet() throws InterruptedException {
        new BlockingMap<Integer, Integer>().keySet();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void size() throws InterruptedException {
        new BlockingMap<Integer, Integer>().size();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void remove() throws InterruptedException {
        new BlockingMap<Integer, Integer>().remove(1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void putAll() throws InterruptedException {
        new BlockingMap<Integer, Integer>()
                .putAll(new HashMap<Integer, Integer>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void values() throws InterruptedException {
        new BlockingMap<Integer, Integer>().values();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void entrySet() throws InterruptedException {
        new BlockingMap<Integer, Integer>().entrySet();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void containsKey() throws InterruptedException {
        new BlockingMap<Integer, Integer>().containsKey(1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void containsValue() throws InterruptedException {
        new BlockingMap<Integer, Integer>().containsValue(1);
    }
}
