package org.nohope.akka;

import org.nohope.IMatcher;
import org.nohope.reflection.IntrospectionUtils;
import org.nohope.typetools.StringUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Set;

import static org.nohope.reflection.IntrospectionUtils.*;

/**
 * Date: 25.07.12
 * Time: 11:26
 */
public final class MessageMethodInvoker {
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
     *     // multiarg method will be invoked
     *     invokeOnReceive(new A(), new Object[] {1, 2.3}, true);
     *
     *     // onearg method will be invoked
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
        final Class[] classes = expandNeeded
                ? getClasses((Object[]) message)
                : getClasses(message);

        final Set<Method> methods = searchMethods(IntrospectionUtils.getClass(target),
                new SignatureMatcher(classes));

        if (methods.size() != 1) {
            throw new NoSuchMethodException(
                    "Only one @OnReceive method expected to match ("
                    + StringUtils.join(getClassNames(classes))
                    + ") parameter types but found "
                    + methods.size());
        }

        final Method method = methods.iterator().next();
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
