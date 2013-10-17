package org.nohope.jaxb2.plugin.validation;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 14:10
 */
public class IsEmptyValidator implements StaticValidator<Object, String> {
    @Override
    public void validate(final String context, @Nonnull final Object obj) throws ValidationException {
    }
}
