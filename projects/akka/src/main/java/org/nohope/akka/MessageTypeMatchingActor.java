package org.nohope.akka;

import akka.actor.ActorInitializationException;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import org.nohope.IMatcher;
import org.nohope.typetools.JSON;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.nohope.akka.MessageMethodInvoker.invokeOnReceive;
import static org.nohope.reflection.IntrospectionUtils.getClassNames;
import static org.nohope.reflection.IntrospectionUtils.searchMethods;
import static org.nohope.typetools.StringUtils.join;

/**
 * This actor allows to simplify working with typed messages using java reflection.
 * <p />
 * <b>Usages</b>:
 * <pre>
 *     class MyActor extends MessageTypeMatchingActor
 *         {@link OnReceive &#064;OnReceive}
 *         private AnotherObject processSomeObject(final SomeObject readMessage) {
 *             if (condition) {
 *                 return new AnotherObject(data); // will be sent back to sender
 *             }
 *
 *             return null; // will not be send
 *         }
 *
 *         {@link OnReceive &#064;OnReceive}
 *         private void processOtherObject(final OtherObject readMessage) {
 *             // nothing will be sent back to sender
 *         }
 *     }
 * </pre>
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/2/12 2:11 PM
 */
public class MessageTypeMatchingActor extends UntypedActor {
    @SuppressWarnings("ThisEscapedInObjectConstruction")
    protected final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final boolean expandObjectArrays;

    protected MessageTypeMatchingActor(final boolean expandObjectArrays) {
        this.expandObjectArrays = expandObjectArrays;

        final List<Class<?>[]> signatures = new ArrayList<>();
        searchMethods(getClass(), new IMatcher<Method>() {
            @Override
            public boolean matches(final Method method) {
                final boolean check = method.isAnnotationPresent(OnReceive.class);
                if (check) {
                    final Class<?>[] types = method.getParameterTypes();
                    for (final Class<?>[] prev : signatures) {
                        if (Arrays.deepEquals(prev, types)) {
                            throw new ActorInitializationException(
                                    "More than one @OnReceive "
                                    + "method found conforming signature ["
                                    + join(getClassNames(types))
                                    + ']');
                        }
                    }

                    signatures.add(types);

                }
                return check;
            }
        });
    }

    protected MessageTypeMatchingActor() {
        this(false);
    }

    @Override
    public final void onReceive(final Object message) {
        try {
            final Object result = invokeOnReceive(this, message, expandObjectArrays);
            // if method return type is not void
            // or return result = null
            if (result != null) {
                onResult(result);
            }
        } catch (final Exception e) {
            onReceiveError(e, message);
        }

        postReceive(message);
    }

    /**
     * Allows to process message after it was processed by one of @OnReceive
     * method. This method will not be invoked if {@link #onReceiveError(Exception, Object)}
     * terminated message processing (i.e throws an runtime exception)
     *
     * @param response origin message
     */
    protected void postReceive(final Object response) {
    }

    /**
     * This hook allows to override behavior for @OnReceive methods which
     * returns not {@code null} values.
     *
     * <pre>
     *     getSender().tell(response, getSelf());
     * </pre>
     *
     * @param response original message
     */
    protected void onResult(final Object response) {
        getSender().tell(response, getSelf());
    }

    /**
     * This hook allows to override exceptional @OnReceive method. by default
     * error just logged and not pushed to higher level.
     *
     * @param e exception
     * @param message original message caused an error
     */
    protected void onReceiveError(final Exception e, final Object message) {
        log.error(e, "Unhandled message: {}", JSON.pretty(message));
    }
}
