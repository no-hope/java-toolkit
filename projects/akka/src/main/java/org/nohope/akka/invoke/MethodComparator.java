package org.nohope.akka.invoke;

import org.nohope.reflection.IntrospectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-04-11 19:39
 */
class MethodComparator implements Comparator<Method>, Serializable {
    private static final long serialVersionUID = 1L;

    protected final Class<?> actorClass;
    protected final Class<?> messageClass;
    private final boolean preferParameter;

    private MethodComparator(final Class<?> actorClass,
                             final Class<?> messageClass,
                             final boolean preferParam) {
        this.actorClass = actorClass;
        this.messageClass = messageClass;
        this.preferParameter = preferParam;
    }

    public static Comparator<Method> byParameter(final Class<?> declaringClass, final Class<?> parameterType) {
        return new MethodComparator(declaringClass, parameterType, true);
    }

    public static Comparator<Method> byDeclaringClassType(final Class<?> declaringClass, final Class<?> parameterType) {
        return new MethodComparator(declaringClass, parameterType, false);
    }

    @Override
    public int compare(final Method o1, final Method o2) {
        final int byParameter = Integer.compare(
                depth(messageClass, o1.getParameterTypes()[0]),
                depth(messageClass, o2.getParameterTypes()[0]));

        final int byDeclaringClass = Integer.compare(
                depth(actorClass, o1.getDeclaringClass()),
                depth(actorClass, o2.getDeclaringClass()));

        final int preferred = preferParameter ? byParameter : byDeclaringClass;
        final int deferred = preferParameter ? byDeclaringClass : byParameter;

        if (preferred != 0) {
            return preferred;
        }

        return deferred;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final MethodComparator that = (MethodComparator) o;
        return preferParameter == that.preferParameter
            && actorClass.equals(that.actorClass)
            && messageClass.equals(that.messageClass);
    }

    @Override
    public int hashCode() {
        int result = actorClass.hashCode();
        result = 31 * result + messageClass.hashCode();
        result = 31 * result + (preferParameter ? 1 : 0);
        return result;
    }

    private static int depth(final Class<?> that, final Class<?> other) {
        final Class<?> higher;
        final Class<?> lower;
        if (IntrospectionUtils.instanceOf(that, other)) {
            lower = that;
            higher = other;
        } else {
            higher = that;
            lower = other;
        }

        Class<?> parent = lower;
        int depth = 0;
        while (parent != null && !higher.equals(parent)) {
            depth++;
            parent = parent.getSuperclass();
        }

        return depth;
    }
}
