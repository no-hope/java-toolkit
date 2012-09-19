package org.nohope.akka;

import akka.actor.ActorRef;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.util.Duration;

import static akka.pattern.Patterns.ask;

/**
 * Date: 27.07.12
 * Time: 14:53
 */
public final class Ask {
    private Ask() {
    }

    public static <T> T waitReply(final ActorRef ref, final Object message, final long timeout) throws Exception {
        try {
            @SuppressWarnings("unchecked")
            final Future<T> childResponse =
                    (Future<T>) ask(ref, message, Timeout.longToTimeout(timeout));

            return Await.result(childResponse, Duration.Inf());
        } catch (Exception e) { // just to make current stack visible
            throw new IllegalStateException(e);
        }
    }

    public static <T> T waitReply(final ActorRef ref, final Object message) throws Exception {
        // TODO: is it good idea to hardcode timeout?
        return waitReply(ref, message, 5000);
    }
}
