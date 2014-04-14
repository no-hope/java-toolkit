package org.nohope.akka.invoke;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2014-04-11 20:04
 */
public interface ComparatorProvider extends Serializable {
    Comparator<Method> getComparator(final Class<?> declaredClass, final Class<?> parameter);
}
