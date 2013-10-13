package org.nohope.test;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/16/12 10:42 PM
 */
public class SocketUtilsTest {
    @Test
    public void testLocalAddress() throws IOException, URISyntaxException {
        assertNotNull(SocketUtils.getLocalHostAddress());
        assertNotNull(SocketUtils.getAvailableLocalAddress());
        assertNotNull(SocketUtils.getLocalHostName());
        assertTrue(SocketUtils.isPortNumber(SocketUtils.getAvailablePort()));
        assertTrue(SocketUtils.isLocalPortAvailable(SocketUtils.getAvailablePort()));
        assertTrue(SocketUtils.isLocal(SocketUtils.getLocalHostAddress()));
        assertTrue(SocketUtils.isLocal(SocketUtils.getLocalHostName()));
        assertTrue(SocketUtils.isLocal(
                SocketUtils.getAvailableLocalAddress().getAddress()));

        final int availablePort = SocketUtils.getAvailablePort();
        try (final ServerSocket ss = new ServerSocket(availablePort)) {
            assertTrue(SocketUtils.isRemoteAddressAvailable(
                    new InetSocketAddress(
                            SocketUtils.getLocalHostAddress(),
                            availablePort
                    )));
            final String str = String.format("http://%s:%d",
                    SocketUtils.getLocalHostAddress(), availablePort);
            assertTrue(SocketUtils.isRemoteAddressAvailable(new URL(str)));
            assertTrue(SocketUtils.isRemoteAddressAvailable(new URI(str)));
        }
    }
}
