package org.nohope.validation;

import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Nonnull;
import javax.annotation.meta.When;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public final aspect NotNullAspect {
    pointcut nonNullParameter():
            execution(* *.*(.., @javax.annotation.Nonnull (*), ..));

    pointcut validatedReturnValue():
            execution(@javax.annotation.Nonnull Object+ *.*(..));

    after() returning(final Object ret) : validatedReturnValue() {
        MethodSignature methodSignature =
                (MethodSignature) thisJoinPoint.getSignature();

        if (ret == null) {
            throw new IllegalStateException(
                    "@Nonnull method " + methodSignature.getDeclaringType()
                    + "." + toString(methodSignature.getMethod())
                    + " must not return null");
        }
    }

    before() : nonNullParameter() {
        final MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature();
        final Method method = sig.getMethod();
        final Annotation[][] annotations = method.getParameterAnnotations();

        int i = 1;
        for (final Object obj: thisJoinPoint.getArgs()) {
            Nonnull a = null;
            for (Annotation annotation : annotations[i-1]) {
                if (annotation instanceof Nonnull) {
                    a = (Nonnull) annotation;
                    break;
                }
            }

            if (obj == null && a != null && a.when() == When.ALWAYS) {
                throw new IllegalStateException("Argument "
                    + i
                    + " for @Nonnull parameter of "
                    + thisJoinPoint.getTarget().getClass()
                    + "." + toString(sig.getMethod())
                    + " must not be null");
            }
            i++;
        }
    }

    private static String toString(final Method method) {
        final Class[] args = method.getParameterTypes();
        final StringBuilder builder = new StringBuilder();
        builder.append(method.getName()).append('(');
        boolean started = true;
        for (final Class arg : args) {
            if (!started) {
                builder.append(", ");
            } else {
                started = false;
            }
            builder.append(arg.getCanonicalName());
        }
        builder.append(')');
        return builder.toString();
    }

}
