package org.nohope.test;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/16/12 9:43 PM
 */
public final class SocketUtils {
    private SocketUtils() {
    }

    /**
     * Simple check if given integer is valid port address.
     *
     * @param port port number
     * @return {@code true} if port is valid
     */
    public static boolean isPortNumber(final Integer port) {
        return port != null && (port >= 0 && port <= 0xFFFF);
    }

    /**
     * Checks if given host name is local.
     *
     * @param hostName host name
     * @return {@code true} if host name pointing to local address
     */
    public static boolean isLocal(final String hostName) {
        try {
            return isLocal(InetAddress.getByName(hostName));
        } catch (final UnknownHostException e) {
            return false;
        }
    }

    /**
     * Check if given address is local.
     *
     * @param address address
     * @return {@code true} if address is local
     */
    public static boolean isLocal(final InetAddress address) {
        // Check if the address is a valid special local or loop back
        if (address.isAnyLocalAddress() || address.isLoopbackAddress()) {
            return true;
        }

        // Check if the address is defined on any interface
        try {
            return NetworkInterface.getByInetAddress(address) != null;
        } catch (final SocketException e) {
            return false;
        }
    }

    /**
     * Checks to see if a specific <b>local</b> port is available.
     *
     * @param port the port to check for availability
     * @return {@code true} if port is available
     */
    public static boolean isLocalPortAvailable(final int port) {
        try (final DatagramSocket ds = new DatagramSocket(port);
             final ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            ds.setReuseAddress(true);
            return true;
        } catch (final IOException ignored) {
            return false;
        }
    }

    /**
     * Checks if remote address is listening for connections.
     *
     * @param address remote address
     * @return {@code true} if address is available
     */
    public static boolean isRemoteAddressAvailable(final SocketAddress address) {
        try (final Socket sock = new Socket()) {
            sock.connect(address, 100);
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

    /**
     * @see #isRemoteAddressAvailable(java.net.SocketAddress)
     */
    public static boolean isRemoteAddressAvailable(final String host, final int port) {
        return isRemoteAddressAvailable(new InetSocketAddress(host, port));
    }

    /**
     * @see #isRemoteAddressAvailable(java.net.SocketAddress)
     */
    public static boolean isRemoteAddressAvailable(final URI uri) {
        return isRemoteAddressAvailable(uri.getHost(), uri.getPort());
    }

    /**
     * @see #isRemoteAddressAvailable(java.net.SocketAddress)
     */
    public static boolean isRemoteAddressAvailable(final URL url) {
        return isRemoteAddressAvailable(url.getHost(), url.getPort());
    }

    /**
     * Returns port number which is currently not listened by local services.
     *
     * @return port number
     */
    public static int getAvailablePort() {
        try (final ServerSocket ss = new ServerSocket(0)) {
            return ss.getLocalPort();
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to get local port: no ports available");
        }
    }

    public static InetSocketAddress getAvailableAddress(final String host) {
        return new InetSocketAddress(host, getAvailablePort());
    }

    public static InetSocketAddress getAvailableLocalAddress() {
        return getAvailableAddress(getLocalHostAddress());
    }

    public static InetAddress getLocalHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Unable to obtain local host name");
        }
    }

    public static String getLocalHostAddress() {
        return getLocalHost().getHostAddress();
    }

    public static String getLocalHostName() {
        return getLocalHost().getHostName();
    }
}
