package org.nohope.concurrent;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 11/16/11 12:04 PM
 */
public class AbstractBlockingMapTest {
    @Test
    public void nullSynchronizer() throws InterruptedException {
        final AbstractBlockingMap<Object, Object> m =
                new AbstractBlockingMap<Object, Object>() {
            @Override
            protected IObjectSynchronizer<Object> makeSynchronizer() {
                return null;
            }
        };

        assertFalse(m.isAvailable(1));
        assertTrue(m.isEmpty());
        try {
            m.put(1, 2);
            fail("null synchronized should fail");
        } catch (final NullPointerException e) {
            /* it's ok */
        }
    }
}
