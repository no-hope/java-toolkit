package org.nohope.reflection;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * References a generic type.
 *
 * @param <T> referenced type
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 6/21/11 5:31 PM
 */
public abstract class TypeReference<T> {

    /** Type of token. */
    private final Type type;
    /** Class of type parameter. */
    private final Class<T> rawType;

    /** Default constructor. */
    @SuppressWarnings("unchecked")
    public TypeReference() {
        if (!getClass().isAnonymousClass()) {
            throw new IllegalArgumentException(getClass() + " should be anonymous");
        }

        final Type superClass = getClass().getGenericSuperclass();
        if (!(superClass instanceof ParameterizedType)) {
            throw new IllegalArgumentException("missing type parameter due to type erasure");
        }

        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];

        this.rawType = (Class<T>) IntrospectionUtils.getClass(type);

        // type parameter can't be retrieved
        if (null == rawType) {
            throw new IllegalArgumentException("missing type parameter for type "
                                               + type
                                               + " (external parametrization used?)");
        }
    }

    /**
     * 'Erasured' variant of type reference. Useful for wrapping {@link Class Class} instances.
     * <p/>
     * <p/>
     * Example
     * <pre>
     *    final TypeReference<Map> ref = TypeReference.erasure(Map.class);
     * </pre>
     *
     * @param clazz class to be wrapped
     */
    private TypeReference(final Class<T> clazz) {
        this.type = clazz;
        this.rawType = clazz;
    }

    private static String repr(final Type type) {
        if (!(type instanceof Class)) {
            return type.toString();
        }
        final Class clazz = (Class) type;
        if (clazz.isArray()) {
            Class component = (Class) type;
            final StringBuilder postfix = new StringBuilder("");
            while (component.isArray()) {
                component = component.getComponentType();
                postfix.append("[]");
            }
            return repr(component) + postfix;
        }

        if (clazz.isLocalClass()) {
            final Pattern localOrAnonymous = Pattern.compile("^.*\\$\\d([^\\$\\d]+)$");
            final Matcher matcher = localOrAnonymous.matcher(clazz.getName());
            final String name = matcher.matches() ? matcher.group(1) : clazz.getName();
            final Method method = clazz.getEnclosingMethod();
            final String methodName = method == null ? "new" : method.getName();
            return '{'
                   + repr(clazz.getEnclosingClass())
                   + '#'
                   + methodName
                   + " -> "
                   + name
                   + '}';
        }
        if (clazz.isAnonymousClass()) {
            final Method method = clazz.getEnclosingMethod();
            final String methodName = method == null ? "new" : method.getName();
            return '{'
                   + repr(clazz.getEnclosingClass())
                   + '#'
                   + methodName
                   + " -> <anonymous>"
                   + '}';
        }

        return clazz.getCanonicalName();
    }

    public static <T> TypeReference<T> erasure(final Class<T> type) {
        return new ErasureTypeReference<>(type);
    }

    /**
     * Instantiates a new instance of {@code T} using compatible constructor.
     *
     * @param args constructor arguments for referenced type.
     * @return new instance of type token
     *
     * @throws IllegalAccessException on reflection error
     * @throws InstantiationException on reflection error
     * @throws NoSuchMethodException on reflection error
     * @throws InvocationTargetException on reflection error
     * @see IntrospectionUtils#newInstance(Class, Object[])
     */
    public final T newInstance(final Object... args)
            throws NoSuchMethodException, IllegalAccessException,
                   InvocationTargetException, InstantiationException {
        return IntrospectionUtils.newInstance(rawType, args);
    }

    /** @return class of referenced type */
    @Nonnull
    public final Class<T> getTypeClass() {
        return rawType;
    }

    /** @return the referenced type */
    @Nonnull
    public final Type getType() {
        return this.type;
    }

    @Override
    public final int hashCode() {
        return 31 * type.hashCode() + rawType.hashCode();
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        //noinspection InstanceofInterfaces
        if (!(o instanceof TypeReference)) {
            return false;
        }

        final TypeReference<?> that = (TypeReference<?>) o;
        return rawType.equals(that.rawType) && type.equals(that.type);
    }

    @Override
    public final String toString() {
        return "TypeReference<" + repr(type) + '>';
    }

    private static final class ErasureTypeReference<T> extends TypeReference<T> {
        private ErasureTypeReference(final Class<T> type) {
            super(type);
        }
    }
}
