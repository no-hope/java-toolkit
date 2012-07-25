package org.nohope.reflection;

import java.lang.reflect.InvocationTargetException;

/**
 * Date: 25.07.12
 * Time: 11:26
 */
public final class MessageMethodInvoker {
    public static void invokeHandler(final Object target, final Object message) throws NoSuchMethodException {
        try {
            IntrospectionUtils.invoke(target, "onConcreteMessage", message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
