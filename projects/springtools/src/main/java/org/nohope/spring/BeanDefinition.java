package org.nohope.spring;

import org.nohope.reflection.TypeReference;

import javax.annotation.Nonnull;

/**
 * Class-helper allowing to unify relation between bean name and it's underlying type.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/16/12 7:24 PM
 * @apiviz.uses org.nohope.reflection.TypeReference
 * @apiviz.uses java.lang.Class
 */
public final class BeanDefinition<T> {
    private final String name;
    private final TypeReference<T> ref;

    private BeanDefinition(@Nonnull final String name,
                           @Nonnull final TypeReference<T> clazz) {
        this.name = name;
        this.ref = clazz;
    }

    public static<T> BeanDefinition<T> of(@Nonnull final String name,
                                          @Nonnull final Class<T> clazz) {
        return of(name, TypeReference.erasure(clazz));
    }

    public static<T> BeanDefinition<T> of(@Nonnull final String name,
                                          @Nonnull final TypeReference<T> ref) {
        return new BeanDefinition<>(name, ref);
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Class<T> getBeanClass() {
        return ref.getTypeClass();
    }

    @Nonnull
    public TypeReference<T> getTypeReference() {
        return ref;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BeanDefinition that = (BeanDefinition) o;
        return getBeanClass().equals(that.getBeanClass()) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + getBeanClass().hashCode();
    }
}
