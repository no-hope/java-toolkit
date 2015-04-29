package org.nohope.test;

import com.google.common.base.Throwables;
import org.junit.Assert;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.Assert.*;


/**
 * @author Fei Wong Reed
 * @since 2014-12-02 10:46
 */
public final class ContractUtils {
    private ContractUtils() {
    }

    public static <T> void assertStrongEquality(final T o1, final T o2) {
        assertEqualityContract(o1, o2);
        assertThatAllGettersEqual(o1, o2);
    }

    public static <T> void assertEqualityContract(final T o1, final T o2) {
        assertEquals(o1, o1);
        assertEquals(o1, o2);
        assertEquals(o2, o1);
        assertEquals(o1.hashCode(), o2.hashCode());
        assertNotEquals(null, o1);
        assertNotEquals(new Object(), o1);
    }

    public static <T> void assertThatAllGettersEqual(final T o1,final  T o2) {
        try {
            for (final PropertyDescriptor propertyDescriptor : Arrays.asList(
                    Introspector.getBeanInfo(o1.getClass())
                                .getPropertyDescriptors())) {
                final Method readMethod = propertyDescriptor.getReadMethod();
                readMethod.setAccessible(true);
                assertEquals(
                        "Invocation of " + readMethod.getName() + "() getter produces equal results",
                        readMethod.invoke(o1),
                        readMethod.invoke(o2)
                        );
            }
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void assertComparable(final Comparable<T> lesser,
                                            final Comparable<T> greater,
                                            final Comparable<T> equal) {
        assertNotEquals(lesser, greater);
        assertEquals(lesser, equal);
        assertTrue(lesser.compareTo((T) greater) < 0);
        assertTrue(greater.compareTo((T) lesser) > 0);
        assertEquals(0, lesser.compareTo((T) equal));
    }

    public static void assertUtilityClassWellDefined(final Class<?> clazz)
            throws NoSuchMethodException, InvocationTargetException,
                   InstantiationException, IllegalAccessException {
        assertTrue(
                "Class must be final",
                Modifier.isFinal(clazz.getModifiers())
                  );
        assertEquals(
                "There must be only one constructor",
                1,
                clazz.getDeclaredConstructors().length
                    );
        final Constructor<?> constructor = clazz.getDeclaredConstructor();
        if (constructor.isAccessible() || !Modifier.isPrivate(constructor.getModifiers())) {
            Assert.fail("Constructor is not private");
        }
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);

        for (final Method method : clazz.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())
                    && method.getDeclaringClass().equals(clazz)) {
                Assert.fail("There exists a non-static method:" + method);
            }
        }
    }
}
