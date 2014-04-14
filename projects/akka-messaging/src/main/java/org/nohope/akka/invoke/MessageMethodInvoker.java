package org.nohope.akka.invoke;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.ArrayUtils;
import org.nohope.reflection.IntrospectionUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.nohope.reflection.IntrospectionUtils.*;

/**
 * Date: 25.07.12
 * Time: 11:26
 */
public final class MessageMethodInvoker {
    /** Cache for @OnReceive messages. */
    static final Map<Signature, Method> CACHE = new ConcurrentHashMap<>();

    private MessageMethodInvoker() {
    }

    /**
     * Invokes method annotated with {@link org.nohope.akka.OnReceive} and argument types
     * matching given message type.
     * <p />
     * In case method with given signature was not found in given target class
     * then examines list of fallback objects for matching method.
     * <p />
     * It's possible to invoke multi-args methods passing {@code true} to
     * expandObjectArray argument.
     * <pre>
     *     class A {
     *         {@link org.nohope.akka.OnReceive &#064;OnReceive}
     *         private void multiarg(final int param1, final double param2) {
     *         }
     *
     *         {@link org.nohope.akka.OnReceive &#064;OnReceive}
     *         private void onearg(final Object[] param) {
     *         }
     *     }
     *
     *     // multi-arg method will be invoked
     *     invokeOnReceive(new A(), new Object[] {1, 2.3}, true);
     *
     *     // one-arg method will be invoked
     *     invokeOnReceive(new A(), new Object[] {1, 2.3}, false);
     * </pre>
     *
     *
     * @param target target object/class
     * @param message message to be passed to target object method
     * @param expandObjectArray {@code true} allows object array expanding
     * @param fallbackObjects fallback
     * @return method invocation result
     * @throws NoSuchMethodException if no or more than one
     *         &#064;OnReceive method found or no methods found in target object
     *         and list of fallback objects
     */
    public static Object invokeOnReceive(@Nonnull final Object target,
                                         @Nonnull final Object message,
                                         final boolean expandObjectArray,
                                         final ComparatorProvider provider,
                                         final Object... fallbackObjects) throws Exception {
        final boolean expandNeeded = expandObjectArray && message instanceof Object[];
        final Class<?>[] parameterTypes = expandNeeded
                ? getClasses((Object[]) message)
                : getClasses(message)
                ;

        final Object[] targetObjects = ArrayUtils.add(fallbackObjects, 0, target);
        final Class<?>[] targetClasses =
                ArrayUtils.add(getClasses(fallbackObjects), 0, IntrospectionUtils.getClass(target));

        final Method method = getOrCache(provider, parameterTypes, targetClasses);
        Object realTarget = null;
        for (int i = 0; i <= fallbackObjects.length; i++) {
            if (instanceOf(targetClasses[i], method.getDeclaringClass())) {
                realTarget = targetObjects[i];
            }
        }

        if (realTarget == null) {
            throw new IllegalStateException("How did we even get here?");
        }

        try {
            if (expandNeeded) {
                return invoke(method, realTarget, (Object[]) message);
            } else {
                return invoke(method, realTarget, message);
            }
        } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();

            // trying to rethrow original exception
            if (targetException instanceof Exception) {
                throw (Exception) targetException;
            } else if (targetException instanceof Error) {
                throw (Error) targetException;
            }

            // unsupported exception - wrapping with runtime
            throw illegal(target, message, method, e);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            // this exception shouldn't be processed by underlying client logic
            throw illegal(target, message, method, e);
        }
    }

    public static Object invokeOnReceive(final ComparatorProvider provider,
                                         final Object target,
                                         final Object message,
                                         final Object... handlers)
            throws Exception {
        return invokeOnReceive(target, message, false, provider, handlers);
    }
    public static Object invokeOnReceive(final Object target,
                                         final Object message,
                                         final Object... handlers)
            throws Exception {
        return invokeOnReceive(target, message, false, InvokeStrategy.CLOSEST_BY_PARAMETER, handlers);
    }

    private static Method searchMethod(final ComparatorProvider provider,
                                       final Class<?> targetClass,
                                       final Class<?>[] parameterTypes)
            throws NoSuchMethodException {

        final Set<Method> methods = searchMethods(targetClass, new SignatureMatcher(parameterTypes));

        if (methods.isEmpty()) {
            return null;
        }

        if (parameterTypes.length == 1) {
            return Collections.min(methods, provider.getComparator(targetClass, parameterTypes[0]));
        } else {
            if (methods.size() > 1) {
                throw new NoSuchMethodException(
                        "Only one @OnReceive method expected to match ("
                                + Joiner.on(", ").useForNull("null").join(getClassNames(parameterTypes))
                                + ") parameter types but found "
                                + methods.size()
                                + "; happened at instance of class "
                                + targetClass.getCanonicalName()
                );
            }

            return methods.iterator().next();
        }
    }

    private static Method getOrCache(final ComparatorProvider provider,
                                     final Class<?>[] parameterTypes,
                                     final Class<?>... fallbackClasses)
            throws NoSuchMethodException {
        final Signature pair = Signature.of(provider, parameterTypes, fallbackClasses);
        if (!CACHE.containsKey(pair)) {
            Method method = null;
            for (final Class<?> clazz : fallbackClasses) {
                method = searchMethod(provider, clazz, parameterTypes);
                if (method != null) {
                    break;
                }
            }

            if (method == null) {
                throw new NoSuchMethodException(
                        "No @OnReceive methods found to match ("
                        + Joiner.on(", ").useForNull("null").join(getClassNames(parameterTypes))
                        + ") parameter types for handlers ["
                        + Joiner.on(", ").useForNull("null").join(getClassNames(fallbackClasses))
                        + ']'
                );
            }

            CACHE.put(pair, method);
            return method;
        }

        return CACHE.get(pair);
    }

    private static IllegalArgumentException
            illegal(@Nonnull final Object target,
                    @Nonnull final Object message,
                    final Method method,
                    final Throwable e) {
        return new IllegalArgumentException(MessageFormat.format(
                "Unable to invoke {0}.{1}({2})",
                getCanonicalClassName(target),
                method.getName(),
                getCanonicalClassName(message)), e);
    }

}
