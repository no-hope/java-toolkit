package org.nohope.test;

import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/16/12 10:42 PM
 */
public class SocketUtilsTest extends UtilitiesTestSupport {
    @Override
    protected Class<?> getUtilityClass() {
        return SocketUtils.class;
    }

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

    @Test
    public void ports() throws IOException {
        final int port = SocketUtils.getAvailablePort();
        assertFalse(SocketUtils.isRemoteAddressAvailable("localhost", port));
        try (final ServerSocket ss = new ServerSocket(port)) {
            assertFalse(SocketUtils.isLocalPortAvailable(port));
        }
    }

    @Test
    public void mocking() {
        {
            final InetAddress addr = createMock(InetAddress.class);
            expect(addr.isAnyLocalAddress()).andReturn(true);
            replay(addr);
            assertTrue(SocketUtils.isLocal(addr));
            verify(addr);
        }
        {
            final InetAddress addr = createMock(InetAddress.class);
            expect(addr.isAnyLocalAddress()).andReturn(false);
            expect(addr.isLoopbackAddress()).andReturn(true);
            replay(addr);
            assertTrue(SocketUtils.isLocal(addr));
            verify(addr);
        }
    }
}
