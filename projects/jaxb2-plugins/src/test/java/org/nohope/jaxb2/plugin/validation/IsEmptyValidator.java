package org.nohope.jaxb2.plugin.validation;

import org.nohope.reflection.TypeReference;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 14:10
 */
public class IsEmptyValidator implements StaticValidator<Object, StringContext> {
    @Override
    public void validate(final StringContext context,
                         @Nonnull final Object obj) throws ValidationException {

    }

    @Nonnull
    @Override
    public TypeReference<Object> getTargetClass() {
        return TypeReference.erasure(Object.class);
    }

    @Nonnull
    @Override
    public TypeReference<StringContext> getContextClass() {
        return TypeReference.erasure(StringContext.class);
    }

}
