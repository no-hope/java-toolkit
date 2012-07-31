package org.nohope.typetools;

/**
 * Date: 31.07.12
 * Time: 15:12
 */
public final class Cast {
    private Cast() {

    }

    @SuppressWarnings("unchecked")
    public static <T> T as(final Object value, final Class clazz) {
        if (clazz.isAssignableFrom(value.getClass())) {
            final T ret = (T) value;
            return ret;
        }

        return null;
    }
}
