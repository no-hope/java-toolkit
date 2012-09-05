package org.nohope.reflection;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import static java.lang.reflect.Modifier.methodModifiers;
import static org.nohope.reflection.IntrospectionUtils.getCanonicalClassName;
import static org.nohope.reflection.IntrospectionUtils.invoke;

/**
 * Date: 25.07.12
 * Time: 11:26
 */
public final class MessageMethodInvoker {
    private static final String METHOD = "onConcreteMessage";

    private MessageMethodInvoker() {
    }

    public static void invokeHandler(final Object target, final Object message)
            throws NoSuchMethodException {
        try {
            invoke(target, methodModifiers(), METHOD, message);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unable to invoke {0}.{1}({2})",
                    getCanonicalClassName(target),
                    METHOD,
                    getCanonicalClassName(message)), e);
        }
    }
}
