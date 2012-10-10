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

@SuppressWarnings({
        "StringBufferReplaceableByString",
        "UtilityClassWithoutPrivateConstructor"
})
public final privileged aspect NotNullAspect {

    /**
     * matches all constructors with at last one parameter
     * marked with @Nonnull annotation
     */
    pointcut nonNullConstructorParameters():
            execution(new(.., @javax.annotation.Nonnull (*), ..))
            ;

    /**
     * searches for all methods have at last one parameter
     * annotated with @Nonnull
     */
    pointcut nonNullMethodParameters():
            execution(* *.*(.., @javax.annotation.Nonnull (*), ..))
            ;

    /**
     * searches for all methods which returns any reference type and
     * annotated with @Nonnull
     */
    pointcut validatedReturnValue():
            execution(@javax.annotation.Nonnull (Object+) *.*(..))
            ;

    @SuppressAjWarnings
    after() returning(final Object ret): validatedReturnValue() {
        final MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature();
        if (ret == null) {
            throw new IllegalStateException("@Nonnull method "
                    + toString(sig)
                    + " must not return null"
            );
        }
    }

    @SuppressAjWarnings
    before(): nonNullMethodParameters() || nonNullConstructorParameters() {
        final Signature signature = thisJoinPoint.getSignature();
        final Annotation[][] annotations;
        final String message;

        if (signature instanceof MethodSignature) {
            final MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature();
            final Method method = sig.getMethod();
            annotations = method.getParameterAnnotations();
            message = toString(sig).toString();
        } else if (signature instanceof ConstructorSignature) {
            final ConstructorSignature sig = (ConstructorSignature) signature;
            final Constructor method = sig.getConstructor();
            annotations = method.getParameterAnnotations();
            message = toString(sig).toString();
        } else {
            throw new IllegalStateException("Illegal advice for " + signature.getClass());
        }


        final Object[] args = thisJoinPoint.getArgs();
        final int constructorDiff = args.length - annotations.length;
        for (int j = constructorDiff; j < args.length; j++) {
            final Object obj = args[j];
            Nonnull a = null;
            for (Annotation annotation : annotations[j - constructorDiff]) {
                if (annotation instanceof Nonnull) {
                    a = (Nonnull) annotation;
                    break;
                }
            }

            if (obj == null && a != null && a.when() == When.ALWAYS) {
                throw new IllegalArgumentException("Argument "
                        + (j - constructorDiff + 1)
                        + " for @Nonnull parameter of "
                        + message
                        + " must not be null"
                );
            }
        }
    }

    private static StringBuilder toString(final ConstructorSignature sig) {
        final Constructor method = sig.getConstructor();
        final Class[] args = method.getParameterTypes();
        final StringBuilder builder = new StringBuilder();
        builder.append('\'')
               .append(method.getName())
               .append('(');
        boolean started = true;
        for (final Class arg : args) {
            if (!started) {
                builder.append(", ");
            } else {
                started = false;
            }
            builder.append(arg.getCanonicalName());
        }
        builder.append(")'");
        return builder;
    }

    private static StringBuilder toString(final MethodSignature sig) {
        final Method method = sig.getMethod();
        final Class[] args = method.getParameterTypes();
        final StringBuilder builder = new StringBuilder();
        builder.append('\'')
               .append(method.getReturnType().getSimpleName())
               .append(" ")
               .append(sig.getDeclaringType().getCanonicalName())
               .append('.')
               .append(method.getName())
               .append('(');
        boolean started = true;
        for (final Class arg : args) {
            if (!started) {
                builder.append(", ");
            } else {
                started = false;
            }
            builder.append(arg.getCanonicalName());
        }
        builder.append(")'");
        return builder;
    }
}
