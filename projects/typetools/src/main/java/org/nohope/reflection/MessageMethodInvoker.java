package org.nohope.reflection;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import static org.nohope.reflection.IntrospectionUtils.getCanonicalClassName;
import static org.nohope.reflection.IntrospectionUtils.invoke;
import static org.nohope.reflection.ModifierMatcher.ALL;

/**
 * Date: 25.07.12
 * Time: 11:26
 */
public final class MessageMethodInvoker {
    private static final String METHOD_NAME = "onConcreteMessage";

    private MessageMethodInvoker() {
    }

    public static Object invokeHandler(final Object target, final Object message)
            throws NoSuchMethodException {
        return invokeHandler(target, message, false);
    }

    public static Object invokeHandler(final Object target,
                                       final Object message,
                                       final boolean expandObjectArray)
            throws NoSuchMethodException {
        try {
            if (expandObjectArray && message instanceof Object[]) {
                return invoke(target, ALL, METHOD_NAME, (Object[]) message);
            } else {
                return invoke(target, ALL, METHOD_NAME, message);
            }
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unable to invoke {0}.{1}({2})",
                    getCanonicalClassName(target),
                    METHOD_NAME,
                    getCanonicalClassName(message)), e);
        }
    }
}
