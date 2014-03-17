package org.nohope.test;

import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeNoException;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/16/12 10:42 PM
 */
public class SocketUtilsTest {

    @Test
    public void isUtility() throws Exception {
        UtilityClassUtils.assertUtilityClass(SocketUtils.class);
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
        try (final ServerSocket ignored = new ServerSocket(availablePort)) {
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
        try (final ServerSocket ignored = new ServerSocket(port)) {
            assertFalse(SocketUtils.isLocalPortAvailable(port));
        }
    }

    @Test
    public void localAddressChecks() {
        final InetAddress host = SocketUtils.getLocalHost();
        assertTrue(SocketUtils.isLocal(host));

    }

    @Test
    public void remoteAddress() {
        try {
            assertFalse(SocketUtils.isLocal(InetAddress.getByName("8.8.8.8")));
        } catch (final UnknownHostException e) {
            assumeNoException(e);
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
