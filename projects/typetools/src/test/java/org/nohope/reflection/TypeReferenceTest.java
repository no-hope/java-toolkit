package org.nohope.reflection;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;

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
                new TypeReference<Integer[]>() {
                }.getTypeClass());
        assertEquals((new ArrayList<Integer>()).getClass(),
                new TypeReference<ArrayList<Integer>>() {
                }.getTypeClass());
        assertEquals(Integer[][].class,
                new TypeReference<Integer[][]>() {
                }.getTypeClass());
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
