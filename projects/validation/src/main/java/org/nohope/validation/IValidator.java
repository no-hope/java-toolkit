package org.nohope.validation;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/26/13 11:43 AM
 */
public interface IValidator<T> {
    void validate(final T obj) throws ValidationException;
    @Nonnull Class<T> getType();
}
