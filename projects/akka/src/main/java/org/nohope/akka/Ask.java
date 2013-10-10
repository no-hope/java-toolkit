package org.nohope.akka;

import akka.actor.ActorRef;
import akka.actor.InvalidMessageException;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.annotation.Nonnull;
import java.io.Serializable;

import static akka.pattern.Patterns.ask;
import static org.nohope.reflection.IntrospectionUtils.cast;
import static org.nohope.reflection.IntrospectionUtils.instanceOf;

/**
 * Date: 27.07.12
 * Time: 14:53
 */
public final class Ask {
    private Ask() {
    }

    /**
     * Sends message to given actor in synchronous manner and waits for
     * given time till actor will send back reply.
     *
     * <b>NOTE</b>: expected message type might not extend {@link Serializable} interface
     * but message itself should implement it. So in case message will not implement
     * Serializable interface runtime error will be thrown.
     *
     * @param clazz expected message type
     * @param ref target actor
     * @param message message will be sent to actor
     * @param timeout time in milliseconds to wait for reply
     * @param <T> expected message type
     * @throws InvalidMessageException in case reply was unexpectedly equals to
     *         {@code null} or does not implement {@link Serializable} interface
     * @throws ClassCastException if actor reply super type does not does not
     *         equals to given class
     * @throws IllegalStateException if await was failed (one of
     *         {@link java.util.concurrent.TimeoutException TimeoutException},
     *         {@link InterruptedException InterruptedException},
     *         {@link java.util.concurrent.CancellationException CancellationException}
     *         was actually thrown)
     * @return reply of given type
     */
    @Nonnull
    public static <T> T waitReply(@Nonnull final Class<T> clazz,
                                  @Nonnull final ActorRef ref,
                                  @Nonnull final Serializable message,
                                  final long timeout) throws Exception {
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
                if (!instanceOf(reply, Serializable.class)) {
                    throw new InvalidMessageException(
                            "Message "
                            + reply
                            + " must implement java.io.Serializable");
                }
                return reply;
            }

            // unreachable de facto, but left here for history
            throw new InvalidMessageException(ref + " replied with unexpected null value");
        } catch (final Exception e) {
            // just to make current stack visible
            throw new Exception(e);
        }
    }

    @Nonnull
    public static <T> T waitReply(@Nonnull final Class<T> clazz,
                                  @Nonnull final ActorRef ref,
                                  @Nonnull final Serializable message) throws Exception {
        // TODO: is it good idea to hardcode timeout?
        return waitReply(clazz, ref, message, 5000);
    }

    @Nonnull
    public static Serializable waitReply(@Nonnull final ActorRef ref,
                                         @Nonnull final Serializable message) throws Exception {
        return waitReply(Serializable.class, ref, message);
    }
}
