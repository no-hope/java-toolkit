package org.nohope.spring;

import org.nohope.reflection.TypeReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/16/12 7:24 PM
 */
public final class BeanDefinition<T> {
    private final String name;
    private final Class<T> clazz;
    private final TypeReference<T> reference;

    private BeanDefinition(@Nonnull final String name,
                           @Nullable final Class<T> clazz,
                           @Nullable final TypeReference<T> reference) {
        this.name = name;
        this.clazz = clazz;
        this.reference = reference;
        if (clazz == null && reference == null) {
            throw new IllegalStateException("One of clazz or ref arguments must be defined");
        }
    }

    public static<T> BeanDefinition<T> of(@Nonnull final String name,
                                          @Nullable final Class<T> clazz) {
        return new BeanDefinition<>(name, clazz, null);
    }

    public static<T> BeanDefinition<T> of(@Nonnull final String name,
                                          @Nullable final TypeReference<T> ref) {
        return new BeanDefinition<>(name, null, ref);
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nullable
    public Class<T> getClazz() {
        return clazz;
    }

    @Nullable
    public TypeReference<T> getReference() {
        return reference;
    }
}
