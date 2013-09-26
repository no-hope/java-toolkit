package org.nohope.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/26/13 11:43 AM
 */
@Documented
@Retention(RUNTIME)
public @interface Validate {
    Class<? extends IValidator<?>> value();
}
