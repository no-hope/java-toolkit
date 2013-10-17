package org.nohope.jaxb2.plugin.validation;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 12:13
 */
public interface StaticValidator<T, C> {
    void validate(final C context, @Nonnull final T obj) throws ValidationException;
}
