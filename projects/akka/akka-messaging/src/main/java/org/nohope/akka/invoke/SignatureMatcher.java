package org.nohope.akka.invoke;

import com.google.common.base.Predicate;
import org.nohope.akka.OnReceive;

import java.lang.reflect.Method;

import static org.nohope.reflection.IntrospectionUtils.areTypesCompatible;
import static org.nohope.reflection.IntrospectionUtils.areTypesVarargCompatible;

/**
* @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
* @since 2014-04-11 19:36
*/
final class SignatureMatcher implements Predicate<Method> {
    private final Class<?>[] classes;

    SignatureMatcher(final Class<?>[] classes) {
        this.classes = classes == null ? null : classes.clone();
    }

    @Override
    public boolean apply(final Method method) {
        final Class<?>[] params = method.getParameterTypes();
        return (areTypesCompatible(params, classes)
                || areTypesVarargCompatible(params, classes))
                && method.isAnnotationPresent(OnReceive.class);
    }
}
