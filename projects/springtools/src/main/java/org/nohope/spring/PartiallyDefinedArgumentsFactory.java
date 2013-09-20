package org.nohope.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

/**
 * This class is used for spring-driven beans creation. Allows to inject
 * bean dependencies in beans and add external inject dependencies which
 * can be not defined in given context.
 *
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/16/12 11:09 PM
 */
public class PartiallyDefinedArgumentsFactory<T> implements Serializable {
    private static final long serialVersionUID = 0L;

    private final Class<T> clazz;
    private final transient List<Object> beans = new ArrayList<>();
    private final transient Map<String, Object> namedBeans = new HashMap<>();
    private final transient ApplicationContext ctx;

    public PartiallyDefinedArgumentsFactory(@Nonnull final ApplicationContext ctx,
                                            @Nonnull final Class<T> clazz) {
        this(ctx, clazz, null, null);
    }

    public static<T> PartiallyDefinedArgumentsFactory<T> create(
            @Nonnull final ApplicationContext ctx,
            @Nonnull final Class<T> clazz) {
        return new PartiallyDefinedArgumentsFactory<>(ctx, clazz);
    }

    public PartiallyDefinedArgumentsFactory(@Nonnull final ApplicationContext ctx,
                                            @Nonnull final Class<T> clazz,
                                            @Nullable final List<Object> objects,
                                            @Nullable final Map<String, Object> namedObjects) {
        this.clazz = clazz;
        this.ctx = ctx;

        if (objects != null) {
            beans.addAll(objects);
        }
        if (namedObjects != null) {
            namedBeans.putAll(namedObjects);
        }
    }

    public PartiallyDefinedArgumentsFactory<T> addBeans(@Nonnull final Object... beans) {
        final List<Object> list = Arrays.asList(beans);
        if (list.contains(null)) {
            throw new IllegalStateException("null reference not allowed in beans list");
        }

        this.beans.addAll(list);
        return this;
    }

    public PartiallyDefinedArgumentsFactory<T> addBean(final String name, @Nonnull final Object bean) {
        this.namedBeans.put(name, bean);
        return this;
    }

    public T instantiate() {
        final ConfigurableApplicationContext child =
                SpringUtils.propagateAnnotationProcessing(
                        new GenericApplicationContext(ctx));
        for (final Object obj : beans) {
            SpringUtils.registerSingleton(child, obj);
        }
        for (final Map.Entry<String, Object> e : namedBeans.entrySet()) {
            SpringUtils.registerSingleton(child, e.getKey(), e.getValue());
        }

        return SpringUtils.instantiate(child, clazz);
    }

    @Nonnull
    public Class<T> getTargetClass() {
        return clazz;
    }

    @Nonnull
    public List<Object> getBeans() {
        return beans;
    }

    @Nonnull
    public Map<String, Object> getNamedBeans() {
        return namedBeans;
    }

    @Nonnull
    public ApplicationContext getContext() {
        return ctx;
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private void writeObject(final ObjectOutputStream oos) throws IOException {
        throw new NotSerializableException();
    }
}
