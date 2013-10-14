package org.nohope.reflection;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.util.MyAsserts.assertFalse;
import static com.mongodb.util.MyAsserts.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 11/17/11 2:46 PM
 */
public final class TypeReferenceTest {

    @Test(expected = IllegalArgumentException.class)
    public void erasedType() {
        new ReferenceHolderAbstractChild() {
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void erasedType2() {
        new ReferenceHolderChild() {
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonAnonymousClass() {
        new ReferenceHolderChild();
    }

    @Test
    public void childReferenceSaving() {
        new ReferenceHolder<Integer[]>(){
        };
    }

    private static <T> TypeReference<T> getReference() {
        return new TypeReference<T>() {
        };
    }

    @Test(expected = IllegalArgumentException.class)
    public void missedTypeParameter() {
        final TypeReference<String> reference = getReference();
        assertNull(reference.getTypeClass());
    }

    @Test
    public void simpleTest() {
        assertEquals(Integer[].class,
                new TypeReference<Integer[]>() {}.getTypeClass());
        assertEquals((new ArrayList<Integer>()).getClass(),
                new TypeReference<ArrayList<Integer>>() {}.getTypeClass());
        assertEquals(Integer[][].class,
                new TypeReference<Integer[][]>() {}.getTypeClass());
    }

    @Test
    public void instantiation() throws Exception {
        final TypeReference<InetSocketAddress> addressTypeReference =
                new TypeReference<InetSocketAddress>() {
                };

        final InetSocketAddress address = addressTypeReference.newInstance(123);

        assertEquals(123, address.getPort());
        assertEquals(InetSocketAddress.class, addressTypeReference.getType());
    }

    @Test
    @SuppressWarnings({
            "ObjectEqualsNull",
            "LiteralAsArgToStringEquals",
            "EqualsBetweenInconvertibleTypes",
            "AssertEqualsBetweenInconvertibleTypes"})
    public void equality() throws Exception {
        assertEquals(new TypeReference<List<Integer>>() {},
                new TypeReference<List<Integer>>() {});
        assertNotEquals(new TypeReference<List<Integer>>() {},
                new TypeReference<List<Boolean>>() {});

        assertNotEquals(new TypeReference<List<Integer>>() {},
                new TypeReference<List>() {});

        assertFalse(new TypeReference<List<Integer>>() {
        }.equals(null));
        assertFalse(new TypeReference<List<Integer>>() {}.equals(""));
        final TypeReference<List<Integer>> ref = new TypeReference<List<Integer>>() {};
        final TypeReference<List<Integer>> ref2 = new TypeReference<List<Integer>>() {};
        assertEquals(ref, ref);
        assertEquals(ref.hashCode(), ref2.hashCode());
    }

    @Test
    public void erasure() throws Exception {
        final TypeReference<Map> ref = TypeReference.erasure(Map.class);
        final TypeReference<Map> ref2 = new TypeReference<Map>() {};
        assertEquals(ref, ref2);

        final Map<String, String> map1 = new HashMap<>();
        final Map<String, String> map2 = new LinkedHashMap<>();
        final TypeReference<? extends Map> erasure1 = TypeReference.erasure(map1.getClass());
        final TypeReference<? extends Map> erasure2 = TypeReference.erasure(map2.getClass());
        assertNotEquals(erasure1, erasure2);
    }

    @Test
    public void repr() throws Exception {
        class TC {}
        assertEquals("TypeReference<java.util.List<java.lang.Integer>>",
                new TypeReference<List<Integer>>() {}.toString());
        assertEquals("TypeReference<int[][]>",
                new TypeReference<int[][]>() {}.toString());
        assertEquals("TypeReference<{org.nohope.reflection.TypeReferenceTest#repr -> TC}>",
                TypeReference.erasure(TC.class).toString());
        assertEquals("TypeReference<{org.nohope.reflection.TypeReferenceTest#repr -> <anonymous>}>",
                TypeReference.erasure(new TC() {}.getClass()).toString());
    }

    private abstract static class ReferenceHolderAbstractChild<T>
            extends ReferenceHolder<T> {
    }

    private static class ReferenceHolderChild<T>
            extends ReferenceHolder<T> {
    }

    private abstract static class ReferenceHolder<T>
            extends TypeReference<T> {
    }
}
