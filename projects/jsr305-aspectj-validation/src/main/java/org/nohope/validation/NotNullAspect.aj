package org.nohope.validation;

import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Nonnull;
import javax.annotation.meta.When;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final privileged aspect NotNullAspect {
    private static final Map<Constructor, MethodCache> constructorCache = new ConcurrentHashMap<>();
    private static final Map<Method, MethodCache> methodCache = new ConcurrentHashMap<>();

    /** Utility constructor. */
    private NotNullAspect() {
    }

    pointcut thisAdvice(): adviceexecution() && within(NotNullAspect);

    /**
     * matches all constructors with at last one parameter
     * marked with @Nonnull annotation
     */
    pointcut nonNullConstructorParameters():
            execution(new(.., @javax.annotation.Nonnull (*), ..))
            && !cflow(thisAdvice())
            ;

    /**
     * searches for all methods have at last one parameter
     * annotated with @Nonnull
     */
    pointcut nonNullMethodParameters():
            execution(* *.*(.., @javax.annotation.Nonnull (*), ..))
            && !cflow(thisAdvice())
            ;

    /**
     * searches for all methods which returns any reference type and
     * annotated with @Nonnull
     */
    static pointcut validatedReturnValue():
            execution(@javax.annotation.Nonnull (Object+) *.*(..))
            && !cflow(thisAdvice())
            ;

    @SuppressAjWarnings
    after() returning(final Object ret): validatedReturnValue() {
        final MethodSignature sig = (MethodSignature) thisJoinPoint.getStaticPart().getSignature();
        if (ret == null) {
            throw new IllegalStateException("@Nonnull method "
                    + toString(sig.getMethod())
                    + " must not return null");
        }
    }

    @SuppressAjWarnings
    before(): nonNullMethodParameters() || nonNullConstructorParameters() {
        final Signature signature = thisJoinPoint.getStaticPart().getSignature();
        final MethodCache cache;

        if (signature instanceof MethodSignature) {
            final MethodSignature sig = (MethodSignature) thisJoinPoint.getStaticPart().getSignature();
            final Method method = sig.getMethod();
            cache = getCached(method);
        } else if (signature instanceof ConstructorSignature) {
            final ConstructorSignature sig = (ConstructorSignature) signature;
            final Constructor method = sig.getConstructor();
            cache = getCached(method);
        } else {
            throw new IllegalStateException("Illegal advice for " + signature.getClass());
        }

        final Object[] args = thisJoinPoint.getArgs();
        final int constructorDiff = args.length - cache.params.length;
        for (int j = constructorDiff; j < args.length; j++) {
            final Object obj = args[j];
            Nonnull a = null;
            for (Annotation annotation : cache.params[j - constructorDiff]) {
                if (annotation instanceof Nonnull) {
                    a = (Nonnull) annotation;
                    break;
                }
            }

            if (obj == null && a != null && a.when() == When.ALWAYS) {
                throw new IllegalArgumentException("Argument "
                        + (j - constructorDiff + 1)
                        + " for @Nonnull parameter of "
                        + cache.message
                        + " must not be null"
                );
            }
        }
    }

    private static MethodCache getCached(final Method method) {
        MethodCache result = methodCache.get(method);
        if (result == null) {
            result = new MethodCache(NotNullAspect.toString(method),
                    method.getParameterAnnotations());
            methodCache.put(method, result);
        }
        return result;
    }

    private static MethodCache getCached(final Constructor constructor) {
        MethodCache result = constructorCache.get(constructor);
        if (result == null) {
            result = new MethodCache(NotNullAspect.toString(constructor),
                    constructor.getParameterAnnotations());
            constructorCache.put(constructor, result);
        }
        return result;
    }

    private static String toString(final Constructor method) {
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
            builder = arg.getCanonicalName();
        }
        builder = ")'";
        return builder;
    }

    private static String toString(final Method method) {
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

    private static final class MethodCache {
        private final String message;
        private final Annotation[][] params;

        private MethodCache(final String message, final Annotation[][] params) {
            this.message = message;
            this.params = params;
        }
    }
}
