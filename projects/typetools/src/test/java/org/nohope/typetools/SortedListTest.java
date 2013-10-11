package org.nohope.typetools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-11 12:59
 */
public class SortedListTest {

    @Test
    public void sortingTest() {
        final SortedList<Integer> list = new SortedList<>(new SortedList.SerializableComparator<Integer>() {
            private static final long serialVersionUID = 1L;

            @Override
            public int compare(final Integer o1, final Integer o2) {
                return o1.compareTo(o2);
            }
        });

        list.add(2);
        list.add(3);
        list.add(1);

        try {
            list.add(1, 0);
            fail();
        } catch (final UnsupportedOperationException ignored) {
        }

        assertEquals(1, (int) list.get(0));
        assertEquals(1, (int) list.get(0));
        assertEquals(2, (int) list.get(1));
        assertEquals(3, (int) list.get(2));
    }
}
