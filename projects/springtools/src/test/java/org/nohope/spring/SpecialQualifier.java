package org.nohope.spring;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-17 00:37
 */
@Qualifier
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface SpecialQualifier {
    Type value();
}
