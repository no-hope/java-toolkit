package org.nohope.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/16/12 9:43 PM
 */
public final class SocketUtils {
    private SocketUtils() {
    }

    public static int getAvailablePort() {
        try {
            final ServerSocket ss = new ServerSocket(0);
            final int port = ss.getLocalPort();
            ss.close();
            return port;
        } catch (IOException e) {
            throw new IllegalStateException("no ports available");
        }
    }

    public static InetSocketAddress getAvailableAddress(final String host) {
        return new InetSocketAddress(host, getAvailablePort());
    }

    public static InetSocketAddress getAvailableLocalAddress() {
        return getAvailableAddress(getLocalHostAddress());
    }

    public static String getLocalHostAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to obtain local host address");
        }
    }

    public static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to obtain local host name");
        }
    }
}
