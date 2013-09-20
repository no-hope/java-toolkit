package org.nohope.typetools;

import org.junit.Test;
import org.nohope.reflection.UtilitiesTestSupport;

import java.util.*;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.nohope.typetools.TStr.join;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/31/11 7:12 PM
 */
public final class TStrTest extends UtilitiesTestSupport {

    @Override
    public Class<?> getUtilityClass() {
        return TStr.class;
    }

    @Test
    public void nullCollection() {
        assertNull(TStr.join((Collection<?>) null));
        assertNull(TStr.join((Object[]) null));
        assertNull(TStr.join((Object) null));
    }

    @Test
    public void nullIterator() {
        // emulating collection with null iterator
        final Collection<?> collection = createMock(Collection.class);
        expect(collection.iterator()).andReturn(null).once();
        replay(collection);

        assertNull(TStr.join(collection));
        verify(collection);
    }

    @Test
    public void nullSeparator() {
        assertEquals("123null",
                TStr.join(Arrays.asList(1, 2, 3, null), null, null));
    }

    @Test
    public void emptyNull() {
        assertEquals("1,2,,3,",
                TStr.join(Arrays.asList(1, 2, null, 3, null), ",", ""));

        assertEquals("1,2,,,3,,",
                TStr.join(Arrays.asList(1, 2, null, null, 3, null, null), ",", ""));
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

    @Test
    public void substitutionTest() {
        final Map<String, Object> values = new HashMap<String, Object>();
        values.put("value", 1);
        values.put("column", 1);
        final String result = TStr.format("There's an incorrect value '${value}' in column # ${column}", values);
        assertEquals("There's an incorrect value '1' in column # 1", result);
    }

    @Test
    public void formatPositionalTest() {
        assertEquals("a=123, b=456", TStr.pformat("a={}, b={}", 123, "456"));
    }

    @Test
    public void formatIndexedTest() {
        assertEquals("a=123, b=456", TStr.iformat("a={0}, b={1}", 123, "456"));
    }

    @Test
    public void formatTest() {
        assertEquals("a=123, b=456", TStr.format("a={}, b={}", 123, "456"));
        assertEquals("a=123, b=456", TStr.format("a={0}, b={1}", 123, "456"));
    }
}
