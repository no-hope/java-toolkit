package org.nohope.validation;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Nonnull;
import javax.annotation.meta.When;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@SuppressWarnings({
        "StringBufferReplaceableByString",
        "UtilityClassWithoutPrivateConstructor"
})
public final aspect NotNullAspect {

    // searches for all methods have at last one parameter annotated with @Nonnull
    pointcut nonNullParameter():
            execution(* *.*(.., @javax.annotation.Nonnull (*), ..));

    // searches for all methods which returns any reference type and annotated with @Nonnull
    pointcut validatedReturnValue():
            execution(@javax.annotation.Nonnull Object+ *.*(..));

    @SuppressAjWarnings
    after() returning(final Object ret): validatedReturnValue() {
        final MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature();
        if (ret == null) {
            throw new IllegalStateException(new StringBuilder("@Nonnull method ")
                    .append(toString(sig))
                    .append(" must not return null")
                    .toString()
            );
        }
    }

    @SuppressAjWarnings
    before(): nonNullParameter() {
        final MethodSignature sig = (MethodSignature) thisJoinPoint.getSignature();
        final Method method = sig.getMethod();
        final Annotation[][] annotations = method.getParameterAnnotations();

        int i = 1;
        for (final Object obj : thisJoinPoint.getArgs()) {
            Nonnull a = null;
            for (Annotation annotation : annotations[i - 1]) {
                if (annotation instanceof Nonnull) {
                    a = (Nonnull) annotation;
                    break;
                }
            }

            if (obj == null && a != null && a.when() == When.ALWAYS) {
                throw new IllegalArgumentException(new StringBuilder("Argument ")
                        .append(i)
                        .append(" for @Nonnull parameter of ")
                        .append(toString(sig))
                        .append(" must not be null")
                        .toString()
                );
            }
            i++;
        }
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
