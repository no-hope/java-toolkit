package org.nohope.test.stress;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-03-18 12:33
 */
public class MemoryTest {
    @Test
    public void basic() {
        final Memory current = Memory.getCurrent();
        assertTrue(current.getTotalMemory() > current.getFreeMemory());
        assertTrue(current.getMaxMemory() >= current.getTotalMemory());
        assertTrue(current.getMaxMemory() > current.getFreeMemory());
        assertNotNull(current.toString());
    }
}
