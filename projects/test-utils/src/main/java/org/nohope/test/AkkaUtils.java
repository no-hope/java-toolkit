package org.nohope.test;

import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
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

    public static ActorSystem createLocalSystem(final String systemName) {
        return buildSystem(systemName).build();
    }

    public static ActorSystemBuilder buildSystem(final String systemName) {
        return new ActorSystemBuilder(systemName);
    }

    public static ActorSystemBuilder buildRemoteSystem(final String systemName,
                                                       final String host,
                                                       final int port) {
        return new ActorSystemBuilder(systemName)
                .put("actor.provider", "akka.remote.RemoteActorRefProvider")
                .put("remote.transport", "akka.remote.netty.NettyRemoteTransport")
                .put("remote.log-sent-messages", "on")
                .put("remote.log-received-messages", "on")
                .put("remote.log-received-messages", "on")
                .put("remote.netty.hostname", host)
                .put("remote.netty.port", port)
                ;
    }

    public static ActorSystem createRemoteSystem(final String systemName) {
        return buildRemoteSystem(systemName, "localhost", SocketUtils.getAvailablePort()).build();
    }

    public static URI generateLocalHostActorUri(final String actorSystemName,
                                                final String actorName) {
        return generateActorUri(actorSystemName,
                SocketUtils.getLocalHostAddress(),
                actorName);
    }

    public static class EntryBuilder {
        private final ActorSystemBuilder builder;
        private final String prefix;

        final EntryBuilder ebuilder;

        public EntryBuilder(final ActorSystemBuilder builder,
                            final String prefix,
                            final EntryBuilder ebuilder) {
            this.builder = builder;
            this.prefix = prefix;
            this.ebuilder = ebuilder;
        }

        public EntryBuilder put(final String key, final Object value) {
            builder.put(prefix + '.' + key, value);
            return this;
        }

        public EntryBuilder buildEntry(final String key) {
            return new EntryBuilder(builder, prefix + '.' +key, this);
        }

        public EntryBuilder finishEntry() {
            if (ebuilder == null) {
                throw new IllegalStateException();
            }
            return ebuilder;
        }

        public ActorSystemBuilder end() {
            return builder;
        }
    }

    public static class ActorSystemBuilder {
        private final StringBuilder config = new StringBuilder();
        private final String name;

        public ActorSystemBuilder(final String name) {
            this.name = name;
        }

        public EntryBuilder buildEntry(final String key) {
            return new EntryBuilder(this, key, null);
        }

        public ActorSystemBuilder put(final String key, final Object value) {
            config.append(key)
                  .append(" = ")
                  .append(value)
                  .append('\n');
            return this;
        }

        public ActorSystem build() {
            return ActorSystem.create(name, ConfigFactory.parseString(joinWithDefaultConfig()));
        }

        String joinWithDefaultConfig() {
            final String conf;
            try {
                conf = ResourceUtils.getResourceAsString("test/akka.conf");
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }

            return String.format(conf, this.config.toString());
        }
    }
}
