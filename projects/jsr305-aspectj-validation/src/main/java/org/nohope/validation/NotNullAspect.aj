package org.nohope.validation;

import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Nonnull;
import javax.annotation.meta.When;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.nohope.validation.MethodsCache.CachedMethod;

public final privileged aspect NotNullAspect {
    private static final MethodsCache CACHE = new MethodsCache();

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
        final CachedMethod cache = CACHE.get(sig.getMethod());
        if (ret == null) {
            throw new IllegalStateException("@Nonnull method "
                    + cache
                    + " must not return null");
        }
    }

    @SuppressAjWarnings
    before(): nonNullMethodParameters() || nonNullConstructorParameters() {
        final Signature signature = thisJoinPoint.getStaticPart().getSignature();
        final CachedMethod cache;

        if (signature instanceof MethodSignature) {
            final MethodSignature sig = (MethodSignature) thisJoinPoint.getStaticPart().getSignature();
            final Method method = sig.getMethod();
            cache = CACHE.get(method);
        } else if (signature instanceof ConstructorSignature) {
            final ConstructorSignature sig = (ConstructorSignature) signature;
            final Constructor<?> method = sig.getConstructor();
            cache = CACHE.get(method);
        } else {
            throw new IllegalStateException("Illegal advice for " + signature.getClass());
        }

        cache.observeParameters(thisJoinPoint.getArgs(), Nonnull.class, new MethodsCache.IObserver<Nonnull>() {
            @Override
            public void observe(final Nonnull annotation, final Object arg, final int index) {
                if (arg == null && annotation != null && annotation.when() == When.ALWAYS) {
                    throw new IllegalArgumentException("Argument "
                                                       + index
                                                       + " for @Nonnull parameter of "
                                                       + cache
                                                       + " must not be null"
                    );
                }
            }
        });
    }
}
