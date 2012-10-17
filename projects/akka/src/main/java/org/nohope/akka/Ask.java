package org.nohope.akka;

import akka.actor.ActorRef;
import akka.actor.InvalidMessageException;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.util.Duration;

import javax.annotation.Nonnull;

import static akka.pattern.Patterns.ask;
import static org.nohope.reflection.IntrospectionUtils.cast;

/**
 * Date: 27.07.12
 * Time: 14:53
 */
public final class Ask {
    private Ask() {
    }

    @Nonnull
    public static <T> T waitReply(@Nonnull final Class<T> clazz,
                                  @Nonnull final ActorRef ref,
                                  @Nonnull final Object message,
                                  final long timeout) {
        try {
            final Future<Object> childResponse =
                    ask(ref, message, Timeout.longToTimeout(timeout));

            final Object result = Await.result(childResponse, Duration.Inf());
            // will throw ClassCastException
            final T reply = cast(result, clazz);

            // maybe this is unnecessary check because
            // akka not allows to send null messages
            // (at least default dispatcher disallows)
            if (reply != null) {
                return reply;
            }

            throw new InvalidMessageException(
                    ref + " replied with unexpected null value");
        } catch (final Exception e) {
            // just to make current stack visible
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    public static <T> T waitReply(@Nonnull final Class<T> clazz,
                                  @Nonnull final ActorRef ref,
                                  @Nonnull final Object message) {
        // TODO: is it good idea to hardcode timeout?
        return waitReply(clazz, ref, message, 5000);
    }

    @Nonnull
    public static Object waitReply(@Nonnull final ActorRef ref,
                                   @Nonnull final Object message) {
        return waitReply(Object.class, ref, message);
    }
}
