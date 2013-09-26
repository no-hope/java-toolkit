package org.nohope.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Primitive cache for constructor/methods to avoid extensive introspection usage.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/26/13 2:43 PM
 */
public final class MethodsCache {
    private final Map<Constructor, CachedMethod> constructorCache = new ConcurrentHashMap<>();
    private final Map<Method, CachedMethod> methodCache = new ConcurrentHashMap<>();

    public CachedMethod get(final Method method) {
        CachedMethod result = methodCache.get(method);
        if (result == null) {
            result = new CachedMethod(toString(method),
                    method.getParameterAnnotations());
            methodCache.put(method, result);
        }
        return result;
    }

    public CachedMethod get(final Constructor constructor) {
        CachedMethod result = constructorCache.get(constructor);
        if (result == null) {
            result = new CachedMethod(toString(constructor),
                    constructor.getParameterAnnotations());
            constructorCache.put(constructor, result);
        }
        return result;
    }

    public static String toString(final Constructor method) {
        final Class[] args = method.getParameterTypes();
        String builder = '\''
                         + method.getName()
                         + '('
                         ;
        boolean started = true;
        for (final Class arg : args) {
            if (!started) {
                builder += ", ";
            } else {
                started = false;
            }
            builder += arg.getCanonicalName();
        }
        builder += ")'";
        return builder;
    }

    public static String toString(final Method method) {
        final Class[] args = method.getParameterTypes();
        String builder = '\''
                         + method.getReturnType().getSimpleName()
                         +  " "
                         + method.getDeclaringClass().getCanonicalName()
                         + '.'
                         + method.getName()
                         + '('
                ;
        boolean started = true;
        for (final Class arg : args) {
            if (!started) {
                builder += ", ";
            } else {
                started = false;
            }
            builder += arg.getCanonicalName();
        }
        builder += ")'";
        return builder;
    }

    interface IObserver<T extends Annotation> {
        void observe(final T annotation, final Object arg, final int index);
    }

    public static final class CachedMethod {
        private final String message;
        private final Annotation[][] params;

        private CachedMethod(final String message, final Annotation[][] params) {
            this.message = message;
            this.params = params;
        }

        public Annotation[][] getParams() {
            return params;
        }

        @Override
        public String toString() {
            return this.message;
        }

        @SuppressWarnings("unchecked")
        public <T extends Annotation> void  observeParameters(final Object[] args,
                                                              final Class<T> annotationClass,
                                                              final IObserver<T> observer) {
            final int constructorDiff = args.length - params.length;
            for (int j = constructorDiff; j < args.length; j++) {
                T a = null;
                for (final Annotation annotation : getParams()[j - constructorDiff]) {
                    if (annotationClass.isAssignableFrom(annotation.getClass())) {
                        a = (T) annotation;
                        break;
                    }
                }

                observer.observe(a, args[j], j - constructorDiff + 1);
            }
        }
    }
}
