package org.nohope.akka.invoke;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-04-11 20:07
 */
public enum InvokeStrategy implements ComparatorProvider {
    CLOSEST_BY_PARAMETER {
        @Override
        public Comparator<Method> getComparator(final Class<?> declaredClass, final Class<?> parameter) {
            return MethodComparator.byParameter(declaredClass, parameter);
        }
    },
    CLOSEST_BY_INHERITANCE {
        @Override
        public Comparator<Method> getComparator(final Class<?> declaredClass, final Class<?> parameter) {
            return MethodComparator.byParameter(declaredClass, parameter);
        }
    }
}
