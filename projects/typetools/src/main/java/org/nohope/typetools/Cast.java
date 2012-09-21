package org.nohope.typetools;

import org.nohope.reflection.TypeReference;

import static org.nohope.reflection.IntrospectionUtils.cast;

/**
 * Date: 31.07.12
 * Time: 15:12
 */
@Deprecated
public final class Cast {
    private Cast() {
    }

    /**
     * @deprecated use {@link org.nohope.reflection.IntrospectionUtils#cast} instead
     */
    @Deprecated
    public static <T> T as(final Object value, final Class<T> clazz) {
        return cast(value, clazz);
    }

    /**
     * @deprecated use {@link org.nohope.reflection.IntrospectionUtils#cast} instead
     */
    @Deprecated
    public static <T> T as(final Object value, final TypeReference<T> ref) {
        return cast(value, ref);
    }
}
