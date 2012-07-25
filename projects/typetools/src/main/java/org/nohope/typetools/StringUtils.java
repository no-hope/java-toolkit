package org.nohope.typetools;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.nohope.reflection.IntrospectionUtils.toObjArray;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/29/11 5:16 PM
 */
public final class StringUtils {
    /**
     * Default representation for {@code null} reference.
     */
    public static final String NULL_STRING = "null";
    /**
     * Default element separator.
     */
    public static final String DEFAULT_SEPARATOR = ", ";

    /**
     * Utility constructor.
     */
    private StringUtils() {
    }

    /**
     * Joins the elements of the provided collection into a single String
     * containing the provided list of elements.
     * <p/>
     * <p>No delimiter is added before or after the list.
     * Handles nested collections.</p>
     * <p/>
     * <pre>
     * StringUtils.join(null, *)                      = null
     * StringUtils.join([], *)                        = ""
     * StringUtils.join([null], *)                    = "null"
     * StringUtils.join([null, "a"], ",", "x")        = "x,a"
     * StringUtils.join(["a", "b", "c"], ";")         = "a;b;c"
     * StringUtils.join(["a", "b", "c"], null)        = "abc"
     * StringUtils.join([null, "", "a"], ";")         = "null;;a"
     * StringUtils.join(["a", ["b", "c", ["d"]], ";") = "a[b;c;[d]]"
     * </pre>
     *
     * @param <T>        the specific type of values to join together
     * @param collection collection of elements
     * @param separator  delimiter for elements in joined string
     * @param nullString {@code null} objects within the array are
     *                   represented by this string
     * @return the joined string, {@code null} if null collection input
     */
    public static <T> String join(final Collection<T> collection,
                                  final String separator,
                                  final String nullString) {
        if (collection == null) {
            return null;
        }

        final Iterator<T> iterator = collection.iterator();
        if (iterator == null) {
            return null;
        }

        String delimiter = separator;
        if (null == separator) {
            delimiter = "";
        }

        // Java default is 16, probably too small
        final StringBuilder buf = new StringBuilder(256);

        while (iterator.hasNext()) {
            final Object obj = iterator.next();
            if (obj != null) {
                final Class clazz = obj.getClass();
                if (clazz.isArray()) {
                    buf.append('[');
                    buf.append(join(clazz.getComponentType().isPrimitive()
                            ? obj
                            : (Object[]) obj,
                            delimiter,
                            nullString));
                    buf.append(']');
                } else if (obj instanceof Collection) {
                    buf.append('[');
                    buf.append(join((Collection<?>) obj,
                            delimiter,
                            nullString));
                    buf.append(']');
                } else {
                    buf.append(obj);
                }
            } else {
                buf.append(nullString);
            }
            if (iterator.hasNext()) {
                buf.append(delimiter);
            }
        }
        return buf.toString();
    }

    /**
     * Joins the elements of the provided collection into a single String
     * containing the provided list of elements.
     * <p/>
     * <p>Uses default null object representation.</p>
     *
     * @param collection collection of elements
     * @param separator  delimiter for elements in joined string
     * @param <T>        the specific type of values to join together
     * @return the joined string, {@code null} if null collection input
     * @see #NULL_STRING
     * @see #join(java.util.Collection, String, String)
     */
    public static <T> String join(final Collection<T> collection,
                                  final String separator) {
        return join(collection, separator, NULL_STRING);
    }

    /**
     * Joins the elements of the provided collection into a single String
     * containing the provided list of elements.
     * <p/>
     * <p>Uses default delimiter and null object representation.</p>
     *
     * @param collection collection of elements
     * @param <T>        the specific type of values to join together
     * @return the joined string, {@code null} if null collection input
     * @see #DEFAULT_SEPARATOR
     * @see #NULL_STRING
     * @see #join(java.util.Collection, String, String)
     */
    public static <T> String join(final Collection<T> collection) {
        return join(collection, DEFAULT_SEPARATOR, NULL_STRING);
    }

    /**
     * Joins the elements of the provided array into a single String
     * containing the provided list of elements.
     *
     * @param objects    array of elements
     * @param separator  delimiter for elements in joined string
     * @param nullString {@code null} objects or empty strings within the
     *                   array are represented by this string
     * @return the joined string, {@code null} if null collection input
     */
    public static String join(final Object[] objects,
                              final String separator,
                              final String nullString) {
        return objects == null
                ? null
                : join(Arrays.asList(objects), separator, nullString);
    }

    /**
     * Joins the elements of the provided array into a single String
     * containing the provided list of elements.
     * <p/>
     * <p>Uses default null object representation.</p>
     *
     * @param objects   array of elements
     * @param separator delimiter for elements in joined string
     * @return the joined string, {@code null} if null collection input
     * @see #NULL_STRING
     * @see #join(java.util.Collection, String, String)
     */
    public static String join(final Object[] objects,
                              final String separator) {
        return join(objects, separator, NULL_STRING);
    }

    /**
     * Joins the elements of the provided array into a single String
     * containing the provided list of elements.
     * <p/>
     * <p>Uses default delimiter and null object representation.</p>
     *
     * @param objects array of elements
     * @return the joined string, {@code null} if null collection input
     * @see #DEFAULT_SEPARATOR
     * @see #NULL_STRING
     * @see #join(java.util.Collection, String, String)
     */
    public static String join(final Object[] objects) {
        return join(objects, DEFAULT_SEPARATOR);
    }

    /**
     * Joins the elements of the provided object into a single String
     * containing the provided list of elements.
     *
     * @param objects    array of elements
     * @param separator  delimiter for elements in joined string
     * @param nullString {@code null} objects or empty strings within the
     *                   array are represented by this string
     * @return the joined string, {@code null} if null collection input
     */
    public static String join(final Object objects,
                              final String separator,
                              final String nullString) {
        return objects == null
                ? null
                : join(toObjArray(objects), separator, nullString);
    }

    /**
     * Joins the elements of the provided object into a single String
     * containing the provided list of elements.
     * <p/>
     * <p>Uses default null object representation.</p>
     *
     * @param objects   array of elements
     * @param separator delimiter for elements in joined string
     * @return the joined string, {@code null} if null collection input
     * @see #NULL_STRING
     * @see #join(java.util.Collection, String, String)
     */
    public static String join(final Object objects,
                              final String separator) {
        return join(objects, separator, NULL_STRING);
    }

    /**
     * Joins the elements of the provided object into a single String
     * containing the provided list of elements.
     * <p/>
     * <p>Uses default delimiter and null object representation.</p>
     *
     * @param objects array of elements
     * @return the joined string, {@code null} if null collection input
     * @see #DEFAULT_SEPARATOR
     * @see #NULL_STRING
     * @see #join(java.util.Collection, String, String)
     */
    public static String join(final Object objects) {
        return join(objects, DEFAULT_SEPARATOR);
    }
}
