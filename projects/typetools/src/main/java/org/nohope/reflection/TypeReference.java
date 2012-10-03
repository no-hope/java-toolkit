package org.nohope.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * References a generic type.
 *
 * @param <T> referenced type
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 6/21/11 5:31 PM
 */
public abstract class TypeReference<T> {
    /**
     * Type of token.
     */
    private final Type type;

    /**
     * Default constructor.
     */
    public TypeReference() {
        if (!getClass().isAnonymousClass()) {
            throw new IllegalArgumentException(getClass() + " should be anonymous");
        }

        final Type superClass = getClass().getGenericSuperclass();
        if (!(superClass instanceof ParameterizedType)) {
            throw new IllegalArgumentException("missing type parameter due to type erasure");
        }

        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];

        // type parameter can't be retrieved
        if (null == getTypeClass()) {
            throw new IllegalArgumentException("missing type parameter for type "
                                               + type
                                               + " (external parametrization used?)");
        }
    }

    /**
     * Instantiates a new instance of {@code T} using compatible constructor.
     *
     * @param args constructor arguments for referenced type.
     * @return new instance of type token
     * @throws IllegalAccessException    on reflection error
     * @throws InstantiationException    on reflection error
     * @throws NoSuchMethodException     on reflection error
     * @throws InvocationTargetException on reflection error
     * @see IntrospectionUtils#newInstance(Class, Object[])
     */
    public final T newInstance(final Object... args)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        return IntrospectionUtils.newInstance(getTypeClass(), args);
    }

    /**
     * @return class of referenced type
     */
    @SuppressWarnings("unchecked")
    public final Class<T> getTypeClass() {
        return (Class<T>) IntrospectionUtils.getClass(type);
    }

    /**
     * @return the referenced type
     */
    public final Type getType() {
        return this.type;
    }
}
