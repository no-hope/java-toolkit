package org.nohope.typetools;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static org.nohope.reflection.IntrospectionUtils.toObjArray;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/29/11 5:16 PM
 */
public final class TStr {
    /** Default representation for {@code null} reference. */
    private static final String NULL_STRING = "null";
    /** Default element separator. */
    private static final String DEFAULT_SEPARATOR = ", ";

    /** Array start symbol. */
    private static final char START_ARRAY = '[';
    /** Array end symbol. */
    private static final char END_ARRAY = ']';

    /** Utility constructor. */
    private TStr() {
    }

    /**
     * Joins the elements of the provided collection into a single String
     * containing the provided list of elements.
     * <p/>
     * <p>No delimiter is added before or after the list.
     * Handles nested collections.</p>
     * <p/>
     * <pre>
     * TStr.join(null, *)                      = null
     * TStr.join([], *)                        = ""
     * TStr.join([null], *)                    = "null"
     * TStr.join([null, "a"], ",", "x")        = "x,a"
     * TStr.join(["a", "b", "c"], ";")         = "a;b;c"
     * TStr.join(["a", "b", "c"], null)        = "abc"
     * TStr.join([null, "", "a"], ";")         = "null;;a"
     * TStr.join(["a", ["b", "c", ["d"]], ";") = "a[b;c;[d]]"
     * </pre>
     *
     * @param <T>        the specific type of values to join together
     * @param collection collection of elements
     * @param separator  delimiter for elements in joined string
     * @param nullString {@code null} objects within the array are
     *                   represented by this string
     * @return the joined string, {@code null} if null collection input
     */
    @Nullable
    public static <T> String join(final Collection<T> collection,
                                  final String separator,
                                  final String nullString) {
        if (collection == null) {
            return null;
        }

        final Iterator<T> iterator = collection.iterator();
        //noinspection ConstantConditions
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
                    buf.append(START_ARRAY);
                    buf.append(join(clazz.getComponentType().isPrimitive()
                            ? obj
                            : (Object[]) obj,
                            delimiter,
                            nullString));
                    buf.append(END_ARRAY);
                } else if (obj instanceof Collection) {
                    buf.append(START_ARRAY);
                    buf.append(join((Collection<?>) obj,
                            delimiter,
                            nullString));
                    buf.append(END_ARRAY);
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
    @Nullable
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
    @Nullable
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
    @Nullable
    private static <T> String join(final T[] objects,
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
    @Nullable
    private static <T> String join(final T[] objects,
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
    @Nullable
    public static <T> String join(final T[] objects) {
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
    @Nullable
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
    @Nullable
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
    @Nullable
    public static String join(final Object objects) {
        return join(objects, DEFAULT_SEPARATOR);
    }

    @Nonnull
    public static byte[] toLatin1(@Nonnull final String str) {
        try {
            return str.getBytes("latin1");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * It's a "too intellectual" (but very useful) method,
     * which invokes slf4j's {@code MessageFormatter} when {@code format}
     * contains {@code {}} token, or call {@code java.text.MessageFormat}
     * otherwise
     */
    public static String format(final String format, final Object... args) {
        if (format.contains("{}")) {
            return pformat(format, args);
        } else {
            return iformat(format, args);
        }
    }

    /**
     * Just an alias for
     * {@link org.apache.commons.lang3.text.StrSubstitutor#replace(Object, java.util.Map)}
     */
    public static <V> String format(final String format, final Map<String, V> valueMap) {
        return StrSubstitutor.replace(format, valueMap);
    }

    /**
     * Just an alias for {@code java.text.MessageFormat.format(String, Object[])}
     */
    public static String iformat(final String format, final Object... args) {
        final MessageFormat temp = new MessageFormat(format);
        return temp.format(args);
    }

    /**
     * Just an alias for {@code org.slf4j.helpers.MessageFormatter.format(String, Object[])}
     */
    public static String pformat(final String format, final Object... args) {
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }
}

