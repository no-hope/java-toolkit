package org.nohope.test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 8/16/12 10:15 PM
 */
public final class AkkaUtils {

    private AkkaUtils() {
    }

    public static URI generateActorUri(final String actorSystemName,
                                       final String hostName,
                                       final String actorName) {
        final InetSocketAddress randomAvailableAddress =
                SocketUtils.getAvailableAddress(hostName);
        try {
            return new URI(String.format("akka://%s@%s:%d/user/%s",
                    actorSystemName,
                    randomAvailableAddress.getHostName(),
                    randomAvailableAddress.getPort(),
                    actorName));
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static URI generateLocalHostActorUri(final String actorSystemName, final String actorName) {
        return generateActorUri(actorSystemName,
                SocketUtils.getLocalHostAddress(),
                actorName);
    }
}
