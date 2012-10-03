package org.nohope.typetools;

import org.junit.Test;
import org.nohope.reflection.UtilitiesTestSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.easymock.EasyMock.*;
import static org.nohope.typetools.StringUtils.join;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/31/11 7:12 PM
 */
public final class StringUtilsTest extends UtilitiesTestSupport {

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
        // emulating collection with null iterator
        final Collection<?> collection = createMock(Collection.class);
        expect(collection.iterator()).andReturn(null).once();
        replay(collection);

        assertNull(StringUtils.join(collection));
        verify(collection);
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
