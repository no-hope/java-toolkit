package org.nohope.spring.app;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/22/12 9:46 PM
 */
@Qualifier
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Dependency {
    Class<?> value();
}
