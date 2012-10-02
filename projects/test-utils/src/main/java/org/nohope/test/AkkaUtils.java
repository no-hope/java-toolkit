package org.nohope.test;

import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;

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
                    randomAvailableAddress.getHostString(),
                    randomAvailableAddress.getPort(),
                    actorName));
        } catch (final URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static ActorSystem createDefaultSystem(final String systemName) {
        return ActorSystem.create(systemName,
                ConfigFactory.parseString(String.format(
                        "akka {"
                        + "  loglevel = DEBUG\n"
                        + "  actor.provider = akka.remote.RemoteActorRefProvider\n"
                        + "  remote {\n"
                        + "    netty {\n"
                        + "      hostname = \"localhost\"\n"
                        + "      port = %d"
                        + "    }\n"
                        + "    transport = \"akka.remote.netty.NettyRemoteTransport\"\n"
                        + "    log-sent-messages = on\n"
                        + "    log-received-messages = on\n"
                        + "    log-remote-lifecycle-events = on"
                        + "  }\n"
                        + "  event-handlers = [akka.event.slf4j.Slf4jEventHandler]\n"
                        + "  default-dispatcher {\n"
                        + "    fork-join-executor {\n"
                        + "      parallelism-min = 16\n"
                        + "    }\n"
                        + "  }\n"
                        + "  event-handler-startup-timeout = 20s\n"
                        + "}", SocketUtils.getAvailablePort())));
    }

    public static URI generateLocalHostActorUri(final String actorSystemName,
                                                final String actorName) {
        return generateActorUri(actorSystemName,
                SocketUtils.getLocalHostAddress(),
                actorName);
    }
}
