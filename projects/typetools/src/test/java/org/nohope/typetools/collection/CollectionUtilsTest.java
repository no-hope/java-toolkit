package org.nohope.typetools.collection;

import org.junit.Test;
import org.nohope.ITranslator;
import org.nohope.test.UtilitiesTestSupport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/18/12 5:34 PM
 */
public class CollectionUtilsTest extends UtilitiesTestSupport {

    @Override
    protected Class<?> getUtilityClass() {
        return CollectionUtils.class;
    }

    @Test
    public void toMap() {
        final Map<String,Integer> map = CollectionUtils.toMap(
                new HashMap<String, Integer>(),
                Arrays.asList(1, 2, 3),
                new ITranslator<Integer, String>() {
                    @Override
                    public String translate(final Integer source) {
                        return "0" + source;
                    }
                });

        assertEquals(3, map.size());

        for (final Map.Entry<String, Integer> entry : map.entrySet()) {
            assertEquals("0" + entry.getValue(), entry.getKey());
        }
    }

    @Test
    public void toCollection() {
        final Set<String> set = CollectionUtils.toCollection(
                new HashSet<String>(),
                Arrays.asList(1, 2, 3, 2, 1),
                new ITranslator<Integer, String>() {
                    @Override
                    public String translate(final Integer source) {
                        return "0" + source;
                    }
                });

        assertEquals(3, set.size());
        assertEquals(new HashSet<>(Arrays.asList("01", "02", "03")), set);
    }

    @Test
    public void fillListTest() {
        final Object marker = new Object();
        final List<Object> list = CollectionUtils.fillList(10, marker);
        assertEquals(10, list.size());
        for (final Object obj : list) {
            assertSame(marker, obj);
        }
    }

    @Test
    public void testToArray() throws Exception {

        final int size = 100;
        final Integer[] integerArray = new Integer[size];
        for (int i = 0; i < size; ++i) {
            integerArray[i] = i;
        }

        final String[] arr = CollectionUtils.mapArray(
                integerArray
                , String.class
                , new ITranslator<Integer, String>() {
            @Override
            public String translate(final Integer source) {
                return source.toString();
            }
        });

        assertEquals(arr.length, size);
        for (int i = 0; i < size; i++) {
            assertEquals(integerArray[i].toString(), arr[i]);
        }
    }
}
