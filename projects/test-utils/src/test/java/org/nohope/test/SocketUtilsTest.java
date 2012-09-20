package org.nohope.test;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/16/12 10:42 PM
 */
public class SocketUtilsTest {
    @Test
    public void testLocalAddress() {
        assertNotNull(SocketUtils.getLocalHostAddress());
        assertNotNull(SocketUtils.getLocalHostName());
    }
}
