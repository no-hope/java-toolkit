package org.nohope.bean;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 12/7/12 7:30 PM
 */
public privileged aspect DispatcherAspect {

    /**
     * Process all IDispatchable setter calls annotated with @Dispatch
     * annotation.
     */
    pointcut setter(IDispatchable t, Object arg):
          execution(@org.nohope.bean.Dispatch void set*(*))
          && args(arg)
          && target(t);

    @SuppressAjWarnings
    after(IDispatchable t, Object arg) returning: setter(t, arg) {
        final MethodSignature sig = (MethodSignature) thisJoinPoint.getStaticPart().getSignature();

        final Method method = sig.getMethod();
        final Dispatch annotation = method.getAnnotation(Dispatch.class);
        final String propertyName;
        if ("".equals(annotation.name())) {
            propertyName = setterToProperty(method.getName());
        } else {
            propertyName = annotation.name();
        }

        t.getDispatcher().handle(t, propertyName, arg);
    }

    private static String setterToProperty(final String methodName) {
        if (!methodName.startsWith("set") || methodName.length() <= 3) {
            return methodName;
        }

        final String without = methodName.replace("set", "");
        return Character.toLowerCase(without.charAt(0))
                + without.substring(1);
    }
}
