package org.nohope.typetools;

import org.junit.Test;
import org.nohope.reflection.UtilitiesTestSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.nohope.typetools.StringUtils.join;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/31/11 7:12 PM
 */
public final class StringUtilsTest extends UtilitiesTestSupport {
    /**
     * Collection with null iterator.
     */
    private static final Collection<?> NULL_ITERATOR =
            new Collection<Object>() {
                @Override
                public int size() {
                    return 0;
                }

                @Override
                public boolean isEmpty() {
                    return true;
                }

                @Override
                public boolean contains(final Object o) {
                    return false;
                }

                @Override
                public Iterator<Object> iterator() {
                    return null;
                }

                @Override
                public Object[] toArray() {
                    return new Object[0];
                }

                @Override
                public <T> T[] toArray(final T[] a) {
                    return null;
                }

                @Override
                public boolean add(final Object o) {
                    return false;
                }

                @Override
                public boolean remove(final Object o) {
                    return false;
                }

                @Override
                public boolean containsAll(final Collection<?> c) {
                    return false;
                }

                @Override
                public boolean addAll(final Collection<?> c) {
                    return false;
                }

                @Override
                public boolean removeAll(final Collection<?> c) {
                    return false;
                }

                @Override
                public boolean retainAll(final Collection<?> c) {
                    return false;
                }

                @Override
                public void clear() {
                }
            };

    @Override
    public Class<?> getUtilityClass() {
        return StringUtils.class;
    }

    @Test
    public void nullCollection() {
        assertNull(StringUtils.join((Collection<?>) null));
        assertNull(StringUtils.join((Object[]) null));
        assertNull(StringUtils.join((Object) null));
    }

    @Test
    public void nullIterator() {
        assertNull(StringUtils.join(NULL_ITERATOR));
    }

    @Test
    public void nullSeparator() {
        assertEquals("123null",
                StringUtils.join(Arrays.asList(1, 2, 3, null), null, null));
    }

    @Test
    public void nestedArraysRegression() {
        String result = join(new Object[]{new int[][]{
                new int[]{1, 2}, new int[]{3, 4}
        }});

        assertEquals("[[1, 2], [3, 4]]", result);

        result = join(new int[][]{
                new int[]{1, 2}, new int[]{3, 4}
        });

        assertEquals("[1, 2], [3, 4]", result);
    }

    @Test
    public void collectionTests() {
        final List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add(null);
        assertEquals("a;b;c;d", join(list, ";", "d"));
        assertEquals("a;b;c;null", join(list, ";"));
        assertEquals("a, b, c, null", join(list));

        final List<Object> parent = new ArrayList<>();
        parent.add("x");
        parent.add(new int[]{1, 2, 3});
        parent.add(list);

        assertEquals("x;[1;2;3];[a;b;c;d]", join(parent, ";", "d"));
        assertEquals("x;[1;2;3];[a;b;c;null]", join(parent, ";"));
        assertEquals("x, [1, 2, 3], [a, b, c, null]", join(parent));
    }

    @Test
    public void arrayTests() {
        final int[] list = new int[]{1, 2, 3};

        assertEquals("1;2;3", join(list, ";", "d"));
        assertEquals("1;2;3", join(list, ";"));
        assertEquals("1, 2, 3", join(list));
    }
}
