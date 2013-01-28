package org.nohope.reflection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;
import static org.nohope.reflection.IntrospectionUtils.*;
import static org.nohope.reflection.ModifierMatcher.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/28/11 8:02 PM
 */
public final class IntrospectionTest extends UtilitiesTestSupport {
    private static final Map<Class<?>, Class<?>[]> classCache =
            new HashMap<>();

    @Override
    public Class<?> getUtilityClass() {
        return IntrospectionUtils.class;
    }

    @AfterClass
    public static void destroyCache() {
        classCache.clear();
    }

    /**
     * Test for function name reflecting.
     */
    @Test
    public void selfNameTest() {
        assertEquals("selfNameTest", reflectSelfName());
    }

    /**
     * Test for caller function name reflecting.
     */
    @Test
    public void callerNameTest() {
        assertEquals("callerNameTest", testCall());
    }

    @Test
    public void tryFromPrimitiveNull() {
        assertNull(IntrospectionUtils.tryFromPrimitive(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalAssignable1() {
        assertNull(IntrospectionUtils.isAssignable(null, int.class));
    }

    @Test
    public void interfaceAssignable() {
        assertFalse(isAssignable(ChildInterface.class, Integer.class));
        assertFalse(isAssignable(Integer.class, ChildInterface.class));

        assertTrue(isAssignable(ChildInterface.class, ChildObject.class));
        assertFalse(isAssignable(ChildObject.class, ChildInterface.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalAssignable2() {
        assertNull(IntrospectionUtils.isAssignable(int.class, null));
    }

    /**
     * Test for primitive list correctness.
     */
    @Test
    public void primitivesListTest() {
        final List<Class<?>> primitives = getPrimitives();
        assertEquals(8, primitives.size());
        assertEquals(8, new HashSet<Class>(primitives).size());

        for (final Class<?> primitive : primitives) {
            assertTrue(primitive.isPrimitive());
        }
    }

    /**
     * Test for primitive types incompatibility with null.
     */
    @Test
    public void primitiveNotCompatibleWithNull() {
        final Class[] source = {null};
        Class[] target;
        for (final Class<?> primitive : getPrimitives()) {
            target = new Class[]{primitive};
            assertFalse(areTypesCompatible(target, source));
        }
    }

    @Test
    public void autoboxing() {
        for (final Class<?> primitive : getPrimitives()) {
            final Class<?> referenced = primitiveToWrapper(primitive);
            assertNotNull(referenced);
            assertEquals(referenced, autoBox(primitive));
        }
    }

    @Test
    public void typesAutoboxingCompatibility() {
        for (final Class<?> primitive : getPrimitives()) {
            final Class<?> referenced = primitiveToWrapper(primitive);

            assertTrue(areTypesCompatible(
                    singletonArray(referenced),
                    singletonArray(primitive)
            ));
            assertTrue(areTypesCompatible(
                    singletonArray(primitive),
                    singletonArray(referenced)
            ));
        }
    }

    @Test
    public void inheritanceCompatibility() {
        assertTrue(areTypesCompatible(
                singletonArray(ParentObject.class),
                singletonArray(ChildObject.class)
        ));
        assertTrue(areTypesCompatible(
                singletonArray(GrandParentObject.class),
                singletonArray(ChildObject.class)
        ));
        assertTrue(areTypesCompatible(
                singletonArray(GrandParentObject.class),
                singletonArray(ParentObject.class)
        ));
        assertTrue(areTypesCompatible(
                singletonArray(ChildInterface.class),
                singletonArray(ChildObject.class)
        ));
    }

    @Test
    public void arrayTypesCompatibility() {
        Class[] source;
        Class[] target;

        for (int i = 1; i <= 10; i++) {
            source = singletonArray(toArrayType(Integer.class, i));
            target = singletonArray(toArrayType(int.class, i));

            assertTrue(areTypesCompatible(target, source));
            assertTrue(areTypesCompatible(source, target));

            source = new Class[]{
                    toArrayType(Integer.class, i - 1),
                    toArrayType(Integer.class, i - 1)
            };
            assertFalse(areTypesVarargCompatible(source, target));
            assertTrue(areTypesVarargCompatible(target, source));

            assertFalse(areTypesVarargCompatible(new Class[]{}, target));
            assertTrue(areTypesVarargCompatible(target, new Class[]{}));

            source = new Class[]{
                    toArrayType(Integer.class, i - 1),
                    toArrayType(Integer.class, i - 1),
                    toArrayType(Integer.class, i - 1),
                    toArrayType(Integer.class, i - 1)
            };

            assertFalse(areTypesVarargCompatible(source, target));
            assertTrue(areTypesVarargCompatible(target, source));
        }
    }

    @Test(expected = NoSuchMethodException.class)
    public void illegalReflectInvoke()
            throws InvocationTargetException, NoSuchMethodException,
            IllegalAccessException {
        invoke(this, "method1", 1, null);
    }

    @Test(expected = NoSuchMethodException.class)
    public void illegalReflectInvoke2()
            throws InvocationTargetException, NoSuchMethodException,
            IllegalAccessException {
        invoke(this, "method2", "foo", new Integer[]{null});
    }

    @Test(expected = NoSuchMethodException.class)
    public void illegalReflectInvoke3()
            throws InvocationTargetException, NoSuchMethodException,
            IllegalAccessException {
        invoke(this, "method3", "foo", new Integer[][][]{
                new Integer[][]{new Integer[]{null}}});
    }

    @Test
    public void reflectVarargsInvoke()
            throws InvocationTargetException, NoSuchMethodException,
            IllegalAccessException {
        Object res = invoke(this, "method2", "foo", 1, 3);

        assert (res instanceof int[]);
        int[] result = (int[]) res;
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(3, result[1]);

        res = invoke(this, "method2", "hi", new Integer[]{1, 3});

        assert (res instanceof int[]);
        result = (int[]) res;
        assertEquals(2, result.length);
        assertEquals(1, result[0]);
        assertEquals(3, result[1]);

        res = invoke(this, "method2", "foo");

        assertTrue(res instanceof int[]);
        result = (int[]) res;
        assertEquals(0, result.length);

        res = invoke(this, "method3", "foo", new Integer[][][]{
                new Integer[][]{new Integer[]{1, 2}, new Integer[]{3, 4}},
                new Integer[][]{new Integer[]{5, 6}, new Integer[]{7, 8}}
        });

        assertTrue(res instanceof int[][][]);
        final int[][][] result2 = (int[][][]) res;
        assertEquals(2, result2.length);

        assertNull(invoke(this, "method2", "foo", null));
    }

    @Test
    public void privateMethodInvoke()
            throws InvocationTargetException, NoSuchMethodException,
            IllegalAccessException {
        final Object result = invoke(this, PRIVATE, "method4", 3);
        assertTrue(result instanceof Integer);
        assertEquals(3, result);
    }

    @Test
    public void arrayTypeShrinking() {
        Object[] testArray;
        Object castedArray;

        // direct inheritance

        testArray = new Object[]{new ParentObject(), new ChildObject()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof ParentObject[]);
        assertEquals(2, Array.getLength(castedArray));

        testArray = new Object[]{new ChildObject(), new ParentObject()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof ParentObject[]);
        assertEquals(2, Array.getLength(castedArray));

        // deep direct inheritance

        testArray = new Object[]{new GrandParentObject(), new ChildObject()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof GrandParentObject[]);
        assertEquals(2, Array.getLength(castedArray));

        testArray = new Object[]{new ChildObject(), new GrandParentObject()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof GrandParentObject[]);
        assertEquals(2, Array.getLength(castedArray));

        // common parent

        testArray = new Object[]{new ParentObject(), new ParentObject2()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof GrandParentObject[]);
        assertEquals(2, Array.getLength(castedArray));

        testArray = new Object[]{new ParentObject2(), new ParentObject()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof GrandParentObject[]);
        assertEquals(2, Array.getLength(castedArray));

        // mixed up

        testArray = new Object[]{null, new ChildObject(), new ParentObject()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof ParentObject[]);
        assertEquals(3, Array.getLength(castedArray));

        testArray = new Object[]{null, new ChildObject(), null, null};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof ChildObject[]);
        assertEquals(4, Array.getLength(castedArray));

        testArray = new Object[]{null, null, null, null};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof Object[]);
        assertEquals(4, Array.getLength(castedArray));

        testArray = new Object[]{new ChildObject(), new ParentObject(),
                new GrandParentObject()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof GrandParentObject[]);
        assertEquals(3, Array.getLength(castedArray));

        testArray = new Object[]{new ChildObject(), new GrandGrandParentObject(),
                new ParentObject()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof GrandGrandParentObject[]);
        assertEquals(3, Array.getLength(castedArray));

        testArray = new Object[]{new GrandParentObject(), new ParentObject(),
                new ChildObject()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof GrandParentObject[]);
        assertEquals(3, Array.getLength(castedArray));

        testArray = new Object[]{new ChildObject(), new GrandGrandParentObject()};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof GrandGrandParentObject[]);
        assertEquals(2, Array.getLength(castedArray));

        testArray = new Object[]{new InetSocketAddress(1000), 1f};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof Object[]);
        assertEquals(2, Array.getLength(castedArray));
        assertSame(testArray, castedArray);

        testArray = new Object[]{1, 2, "foo", true};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof Object[]);
        assertEquals(4, Array.getLength(castedArray));

        testArray = new Object[]{1, 2L, 3, 4.5, null};
        castedArray = shrinkType(testArray);
        assertTrue(castedArray instanceof Number[]);
        assertEquals(5, Array.getLength(castedArray));

        testArray = toObjArray(new int[]{1, 2, 3, 4});
        assertEquals(4, Array.getLength(testArray));

        testArray = new Object[0];
        castedArray = shrinkType(testArray);
        assertEquals(0, Array.getLength(castedArray));
        assertSame(testArray, castedArray);

        testArray = new Object[]{null, null, null, null};
        castedArray = shrinkType(testArray);
        assertEquals(4, Array.getLength(castedArray));
        assertSame(testArray, castedArray);
    }

    @Test
    public void arrayCasts() {
        final Object[] test = new Object[]{1, 2L};
        final Object res = shrinkTypeTo(test, long.class);
        assertTrue(res instanceof long[]);
        assertEquals(2, Array.getLength(res));

        try {
            assertNull(shrinkTypeTo(null, long.class));
            fail("nulls are forbidden");
        } catch (final IllegalArgumentException e) {
        }

        try {
            assertNull(shrinkTypeTo(1, long.class));
            fail("not array types are forbidden");
        } catch (final IllegalArgumentException e) {
        }

        assertTrue(shrinkTypeTo(new long[]{1}, long.class) instanceof long[]);
        assertTrue(shrinkTypeTo(new Number[0], long.class) instanceof long[]);
        try {
            shrinkTypeTo(new long[0], Number.class);
            fail("should fail due to incompatible types");
        } catch (final ClassCastException e) {
        }
    }

    @Test
    public void parent() {
        assertEquals(A11.class, findCommonParent(B21.class, A32.class));
        assertEquals(A11.class, findCommonParent(A32.class, B21.class));
        assertEquals(Number.class, findCommonParent(Long.class, Float.class));
        assertEquals(Number.class, findCommonParent(long.class, float.class));
        assertEquals(String.class, findCommonParent(null, String.class));
        assertEquals(String.class, findCommonParent(String.class, null));

        assertNull(findCommonParent(ChildInterface.class, Integer.class));
        assertNull(findCommonParent(Integer.class, ChildInterface.class));
    }

    @Test(expected = ClassCastException.class)
    public void illegalArrayCast() {
        final Object[] test = new Object[]{1, 2L, "foo"};
        final Object res = shrinkTypeTo(test, long.class);
        assertTrue(res instanceof long[]);
        assertEquals(2, Array.getLength(res));
    }

    @Test(expected = NoSuchMethodException.class)
    public void collisionMethod()
            throws Exception {
        invoke(this, "method5", 1);
    }

    @Test
    public void methodSearch() throws NoSuchMethodException {
        assertNotNull(searchMethod(getClass(), not(or(PUBLIC, PROTECTED, PRIVATE)), "testCall"));
    }

    @Test
    public void flattenNonVarargs() {
        final Class[] types = {Integer.class};
        assertSame(types, flattenVarargs(types));
    }

    @Test
    public void typeAdapting() {
        assertEquals(0, adaptTo(new Object[0], new Class[]{int.class}).length);
        assertEquals(1, adaptTo(new Object[]{1, 2, 3, 4, 5},
                new Class[]{int[].class}).length);
        assertEquals(0, adaptTo(new Object[0],
                new Class[]{long.class, int[].class}).length);
    }

    @Test
    public void classNames() {
        final String[] names = getClassNames(1, null, "test");
        assertEquals(3, names.length);
        assertEquals("java.lang.Integer", names[0]);
        assertNull(names[1]);
        assertEquals("java.lang.String", names[2]);
    }

    @Test
    public void objArray() {
        assertNull(toObjArray(null));
        assertEquals(1, toObjArray(1)[0]);
    }

    @Test
    public void type() {
        assertEquals(Integer.class,
                IntrospectionUtils.getClass(Integer.class));

        final GenericArrayType gaMock = createMock(GenericArrayType.class);
        expect(gaMock.getGenericComponentType()).andReturn(null);
        assertNull(IntrospectionUtils.getClass(gaMock));
    }

    /**
     * array of arrays adapting test
     */
    @Test
    public void complexTypeAdapting() {
        final int[][][] complexIn = new int[][][]{
                new int[][]{new int[]{1, 2}, new int[]{3, 4}},
                new int[][]{new int[]{5, 6}, new int[]{7, 8}}
        };

        final Object[] result = adaptTo(new Object[]{complexIn},
                new Class[]{Integer[][][].class});
        assertEquals(1, result.length);
        assertTrue(result[0] instanceof Integer[][][]);

        final Integer[][][] complexOut = (Integer[][][]) result[0];
        assertEquals(2, complexOut.length);
        assertEquals(2, complexOut[0].length);
        assertEquals(2, complexOut[1].length);
        assertEquals(2, complexOut[0][0].length);
        assertEquals(2, complexOut[1][0].length);
        assertEquals(2, complexOut[1][0].length);
        assertEquals(2, complexOut[1][1].length);
        assertEquals(1, (int) complexOut[0][0][0]);
        assertEquals(2, (int) complexOut[0][0][1]);
        assertEquals(3, (int) complexOut[0][1][0]);
        assertEquals(4, (int) complexOut[0][1][1]);
        assertEquals(5, (int) complexOut[1][0][0]);
        assertEquals(6, (int) complexOut[1][0][1]);
        assertEquals(7, (int) complexOut[1][1][0]);
        assertEquals(8, (int) complexOut[1][1][1]);
    }

    @Test
    public void constructing() throws Exception {
        newInstance(VarargConstructable.class, 1, 2, 3);
    }

    @Test(expected = NoSuchMethodException.class)
    public void constructorCollision() throws Exception {
        newInstance(VarargConstructable.class, true);
    }

    @Test(expected = NoSuchMethodException.class)
    public void constructorCollision2() throws Exception {
        newInstance(VarargConstructable.class, 1L, 2L, 3L);
    }

    @Test(expected = NoSuchMethodException.class)
    public void constructorCollision3() throws Exception {
        newInstance(VarargConstructable.class, new long[]{1L, 2L});
    }

    @Test(expected = NoSuchMethodException.class)
    public void illegalVararg() throws Exception {
        newInstance(VarargConstructable.class, (Object) new Float[]{1f, null});
    }

    @Test(expected = NoSuchMethodException.class)
    public void constructorCollision4() throws Exception {
        newInstance(VarargConstructable.class, new GrandParentObject());
    }

    //-----------------------------------------------------------------------
    // Helper methods.
    //-----------------------------------------------------------------------
    private Class<?>[] singletonArray(final Class<?> c) {
        Class<?>[] result = classCache.get(c);
        if (result == null) {
            result = new Class[]{c};
            classCache.put(c, result);
        }
        return result;
    }

    //-----------------------------------------------------------------------
    // Test methods and classes.
    //-----------------------------------------------------------------------

    String testCall() {
        return IntrospectionUtils.reflectCallerName();
    }

    @SuppressWarnings("unused")
    public Integer[] method1(final Integer arg1, final int arg2) {
        return new Integer[]{arg1, arg2};
    }

    @SuppressWarnings("unused")
    public int[] method2(final String arg1, final int[] arg2) {
        return arg2;
    }

    @SuppressWarnings("unused")
    public int[][][] method3(final String arg1, final int[][][] arg2) {
        return arg2;
    }

    @SuppressWarnings("unused")
    private int method4(final int param) {
        return param;
    }

    @SuppressWarnings("unused")
    private int method5(final int param) {
        return param;
    }

    @SuppressWarnings("unused")
    private int method5(final Integer param) {
        return param;
    }

    @SuppressWarnings("unused")
    private void exceptionalMethod() {
        throw new RuntimeException();
    }

    @SuppressWarnings("unused")
    private static class VarargConstructable {
        VarargConstructable(final Integer... args) {
        }

        VarargConstructable(final Boolean arg) {
        }

        VarargConstructable(final boolean arg) {
        }

        VarargConstructable(final char arg) {
        }

        VarargConstructable(final Long... args) {
        }

        VarargConstructable(final long... args) {
        }

        VarargConstructable(final long arg1, final long arg2) {
        }

        VarargConstructable(final float... arg1) {
        }
    }

    private interface ChildInterface {
    }

    public static class GrandGrandParentObject {
    }

    public static class GrandParentObject extends GrandGrandParentObject {
    }

    public static class ParentObject extends GrandParentObject {
    }

    private static class ChildObject extends ParentObject implements ChildInterface {
    }

    private static class ParentObject2 extends GrandParentObject {
    }

    public static class A11 {
    }

    public static class A21 extends A11 {
    }

    private static class A32 extends A21 {
    }

    private static class B21 extends A11 {
    }


    private static class Parent {
        protected void protectedMethod() {
        }

        private void privateMethod() {
        }

        public void publicMethod() {
        }

        void packageDefaultMethod() {
        }
    }

    private static class Child extends Parent {
    }

    private static class ChildOverride extends Parent {
        @Override
        public void publicMethod() {
        }

        @Override
        void packageDefaultMethod() {
        }

        @Override
        protected void protectedMethod() {
        }
    }

    @Test
    public void inheritedMethod() {
        try {
            searchMethod(Child.class, "publicMethod");
            searchMethod(Child.class, PROTECTED, "protectedMethod");
            searchMethod(Child.class, PRIVATE, "privateMethod");
            searchMethod(Child.class, PACKAGE_DEFAULT, "packageDefaultMethod");

            searchMethod(Child.class, ALL, "publicMethod");
            searchMethod(Child.class, ALL, "protectedMethod");
            searchMethod(Child.class, ALL, "privateMethod");
            searchMethod(Child.class, ALL, "packageDefaultMethod");
        } catch (NoSuchMethodException e) {
            Assert.fail(e.getMessage());
        }

        try {
            searchMethod(Child.class, "protectedMethod");
            fail();
        } catch (NoSuchMethodException ignored) {
        }

        try {
            searchMethod(Child.class, "privateMethod");
            Assert.fail();
        } catch (NoSuchMethodException ignored) {
        }

        try {
            searchMethod(Child.class, "packageDefaultMethod");
            Assert.fail();
        } catch (NoSuchMethodException ignored) {
        }
    }

    private abstract static class AbstractParent {
        public abstract void absMethod();
    }

    private static class ChildOfAbstractParent extends AbstractParent {
        @Override
        public final void absMethod() {
        }
    }

    @Test
    public void inheritedMethodOverride() throws NoSuchMethodException {
        searchMethod(ChildOfAbstractParent.class, "absMethod");
        searchMethod(ChildOverride.class, "publicMethod");
    }
}
