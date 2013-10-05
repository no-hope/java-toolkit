package org.nohope.validation;

import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.nohope.reflection.IntrospectionUtils.instanceOf;
import static org.nohope.reflection.IntrospectionUtils.newInstance;
import static org.nohope.validation.MethodsCache.CachedMethod;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/26/13 11:51 AM
 */
public privileged aspect ValidatingAspect {
    private static final MethodsCache methodsCache = new MethodsCache();

    pointcut thisAdvice(): adviceexecution() && within(ValidatingAspect);

    /**
     * matches all constructors with at last one parameter
     * marked with @Nonnull annotation
     */
    pointcut constructorParameters():
            execution(new(.., @org.nohope.validation.Validate (*), ..))
            && !cflow(thisAdvice())
            ;

    /**
     * searches for all methods have at last one parameter
     * annotated with @Nonnull
     */
    pointcut methodParameters():
            execution(* *.*(.., @org.nohope.validation.Validate (*), ..))
            && !cflow(thisAdvice())
            ;

    /**
     * searches for all methods which returns any reference type and
     * annotated with @Nonnull
     */
    static pointcut returnValue():
            execution(@org.nohope.validation.Validate (Object+) *.*(..))
            && !cflow(thisAdvice())
            ;

    private ValidatingAspect() {
    }

    @SuppressWarnings("unchecked")
    private static void validate(final Class<? extends IValidator<?>> clazz,
                                 final Object target) throws ValidatingTypeMismatch,
                                                             ValidatorInitializationException,
                                                             ValidationException {
        if (clazz != null) {
            try {
                final IValidator<?> validator = newInstance(clazz);
                final Class<?> type = validator.getType();
                if (target != null && !instanceOf(target, type)) {
                    throw new ValidatingTypeMismatch();
                }

                // now we are sure that types are compatible
                ((IValidator<Object>) validator).validate(target);
            } catch (NoSuchMethodException
                    | InvocationTargetException
                    | IllegalAccessException
                    | InstantiationException e) {
                throw new ValidatorInitializationException(e);
            }
        }
    }

    @SuppressAjWarnings
    after() returning(final Object ret): returnValue() {
        final MethodSignature sig = (MethodSignature) thisJoinPoint.getStaticPart().getSignature();
        final Method method = sig.getMethod();
        final Validate annotation = method.getAnnotation(Validate.class);
        final CachedMethod cache = methodsCache.get(method);

        final Class<? extends IValidator<?>> clazz = annotation.value();
        try {
            validate(clazz, ret);
        } catch (ValidatingTypeMismatch e) {
            throw new IllegalArgumentException(
                    "Return value of method @Validate("
                    + clazz.getCanonicalName()
                    + ") "
                    + cache
                    + " cannot be applied to "
                    + ret.getClass().getCanonicalName());
        } catch (ValidationException e) {
            throw new IllegalArgumentException(
                    "Validation failed for return value of method @Validate("
                    + clazz.getCanonicalName()
                    + ") "
                    + cache, e);
        } catch (ValidatorInitializationException e) {
            throw new IllegalStateException("Unable to validate return value of @Validate("
                                            + clazz.getCanonicalName()
                                            + ")", e);
        }
    }

    @SuppressAjWarnings
    before(): methodParameters() || constructorParameters() {
        final Signature signature = thisJoinPoint.getStaticPart().getSignature();
        final CachedMethod cache;

        if (signature instanceof MethodSignature) {
            final MethodSignature sig = (MethodSignature) thisJoinPoint.getStaticPart().getSignature();
            final Method method = sig.getMethod();
            cache = methodsCache.get(method);
        } else if (signature instanceof ConstructorSignature) {
            final ConstructorSignature sig = (ConstructorSignature) signature;
            final Constructor method = sig.getConstructor();
            cache = methodsCache.get(method);
        } else {
            throw new IllegalStateException("Illegal advice for " + signature.getClass());
        }

        cache.observeParameters(thisJoinPoint.getArgs(), Validate.class, new MethodsCache.IObserver<Validate>() {
            @Override
            public void observe(final Validate annotation, final Object arg, final int index) {
                final Class<? extends IValidator<?>> clazz = annotation.value();
                try {
                    validate(clazz, arg);
                } catch (ValidatingTypeMismatch e) {
                    throw new IllegalArgumentException(
                            "Argument "
                            + index
                            + " for @Validate("
                            + clazz.getCanonicalName()
                            + ") parameter of "
                            + cache
                            + " cannot be applied to "
                            + arg.getClass().getCanonicalName(), e);
                } catch (ValidatorInitializationException e) {
                    throw new IllegalStateException(
                            "Unable to validate argument "
                            + index
                            + " for @Validate("
                            + clazz.getCanonicalName()
                            + ") parameter of "
                            + cache, e);
                } catch (ValidationException e) {
                    throw new IllegalArgumentException(
                            "Validation failed for argument "
                            + index
                            + " of @Validate("
                            + clazz.getCanonicalName()
                            + ") "
                            + cache, e);
                }
            }
        });
    }
}
