package org.nohope.test.annotations.domain;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is intended for tests which are testing
 * known incorrect behavior/bug which should be fixed in the future.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KnownBug {
    String description() default "";
    String ticket() default "";
}
