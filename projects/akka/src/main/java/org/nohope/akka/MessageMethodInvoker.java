package org.nohope.akka;

import org.nohope.IMatcher;
import org.nohope.reflection.IntrospectionUtils;
import org.nohope.typetools.StringUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.nohope.reflection.IntrospectionUtils.*;
import static org.nohope.reflection.IntrospectionUtils.getClassNames;

/**
 * Date: 25.07.12
 * Time: 11:26
 */
public final class MessageMethodInvoker {
    /** Cache for @OnReceive messages. */
    static final Map<SignaturePair, Method> cache = new ConcurrentHashMap<>();

    private MessageMethodInvoker() {
    }

    /** @see #invokeOnReceive(Object, Object, boolean) */
    public static Object invokeOnReceive(final Object target,
                                         final Object message)
            throws Exception {
        return invokeOnReceive(target, message, false);
    }

    /**
     * Invokes method annotated with {@link OnReceive} and argument types
     * matching given message type.
     * <p />
     * It's possible to invoke multi-args methods passing {@code true} to
     * expandObjectArray argument.
     * <pre>
     *     class A {
     *         {@link OnReceive &#064;OnReceive}
     *         private void multiarg(final int param1, final double param2) {
     *         }
     *
     *         {@link OnReceive &#064;OnReceive}
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
     * @return method invocation result
     * @throws NoSuchMethodException if no or more than one
     *         &#064;OnReceive method found
     */
    public static Object invokeOnReceive(@Nonnull final Object target,
                                         @Nonnull final Object message,
                                         final boolean expandObjectArray)
            throws Exception {
        final boolean expandNeeded = expandObjectArray && message instanceof Object[];
        final Class<?>[] classes = expandNeeded
                ? getClasses((Object[]) message)
                : getClasses(message)
                ;

        final Method method = getOrCache(IntrospectionUtils.getClass(target), classes);
        try {
            if (expandNeeded) {
                return invoke(method, target, (Object[]) message);
            } else {
                return invoke(method, target, message);
            }
        } catch (InvocationTargetException e) {
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

    private static Method getOrCache(final Class<?> targetClass,
                                     final Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        final SignaturePair pair = SignaturePair.of(targetClass, parameterTypes);
        if (!cache.containsKey(pair)) {
            final Set<Method> methods = searchMethods(
                    targetClass,
                    new SignatureMatcher(parameterTypes));
            if (methods.size() != 1) {
                throw new NoSuchMethodException(
                        "Only one @OnReceive method expected to match ("
                        + StringUtils.join(getClassNames(parameterTypes))
                        + ") parameter types but found "
                        + methods.size());
            }

            final Method m = methods.iterator().next();
            cache.put(pair, m);

            return m;
        }

        return cache.get(pair);
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

    static class SignaturePair {
        private final Class<?> target;
        private final Class<?>[] parameter;

        SignaturePair(final Class<?> target, final Class<?>[] parameter) {
            this.target = target;
            this.parameter = parameter;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final SignaturePair that = (SignaturePair) o;
            return Arrays.equals(parameter, that.parameter) && target.equals(that.target);

        }

        @Override
        public int hashCode() {
            return 31 * target.hashCode() + Arrays.hashCode(parameter);
        }

        public static SignaturePair of(final Class<?> target, final Class<?>[] parameter) {
            return new SignaturePair(target, parameter);
        }
    }

    private static class SignatureMatcher implements IMatcher<Method> {
        private final Class[] classes;

        public SignatureMatcher(final Class[] classes) {
            this.classes = classes;
        }

        @Override
        public boolean matches(final Method method) {
            final Class<?>[] params = method.getParameterTypes();
            return (areTypesCompatible(params, classes)
                    || areTypesVarargCompatible(params, classes))
                    && method.isAnnotationPresent(OnReceive.class);
        }
    }
}
