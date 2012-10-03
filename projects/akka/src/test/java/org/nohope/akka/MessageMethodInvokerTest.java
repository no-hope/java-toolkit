package org.nohope.akka;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * Date: 25.07.12
 * Time: 11:29
 */
public class MessageMethodInvokerTest {
    private boolean intInvoked = false;
    private boolean dblInvoked = false;

    private void onConcreteMessage(final Integer a) {
        intInvoked = true;
    }

    private void onConcreteMessage(final Double b) {
        dblInvoked = true;
    }

    private static class TestClass {
        private native void onConcreteMessage(final String x);

        @Override
        public String toString() {
            return "TestClass";
        }
    }

    @Test
    public void testInvoker() throws NoSuchMethodException {
        final Object a1 = 100;
        MessageMethodInvoker.invokeHandler(this, a1);
        assertEquals(true, intInvoked);
        assertEquals(false, dblInvoked);
        final Object a2 = 100.0;
        MessageMethodInvoker.invokeHandler(this, a2);
        assertEquals(true, intInvoked);
        assertEquals(true, dblInvoked);
    }

    @Test
    public void testNoMethod() throws NoSuchMethodException {
        try {
            MessageMethodInvoker.invokeHandler(this, "xxx");
            fail();
        } catch (final NoSuchMethodException e) {
        }

        try {
            MessageMethodInvoker.invokeHandler(new TestClass(), "yyy");
            fail();
        } catch (final IllegalArgumentException e) {
        }
    }

    /// Annotated version of tests

    @OnReceive
    private Integer processInteger(final Integer a) {
        return a;
    }

    @OnReceive
    private Double processDouble(final Double b) {
        return b;
    }

    @Test
    public void testAnnotatedInvoker() throws NoSuchMethodException {
        final Object a1 = 100;
        assertEquals(a1, MessageMethodInvoker.invokeOnReceive(this, a1));
        final Object a2 = 100.0;
        assertEquals(a2, MessageMethodInvoker.invokeOnReceive(this, a2));
    }

    @Test
    public void testNoAnnotatedMethod() throws NoSuchMethodException {
        try {
            MessageMethodInvoker.invokeOnReceive(this, "xxx");
            fail();
        } catch (final NoSuchMethodException e) {
        }

        try {
            MessageMethodInvoker.invokeOnReceive(new AnnotatedTestClass(), "yyy");
            fail();
        } catch (final IllegalArgumentException e) {
        }
    }

    @Test
    public void inheritance() throws NoSuchMethodException {
        assertEquals(1, MessageMethodInvoker.invokeOnReceive(new AnnotatedTestClass(), 1));
        assertEquals(2, MessageMethodInvoker.invokeOnReceive(new AnnotatedParentClass(), 2));
    }

    @Test
    public void multipleMatch() {
        try {
            MessageMethodInvoker.invokeOnReceive(new AnnotatedTestClass(), 1L);
            fail();
        } catch (final NoSuchMethodException e) {
        }

        try {
            MessageMethodInvoker.invokeOnReceive(new AnnotatedParentClass(), 1L);
            fail();
        } catch (final NoSuchMethodException e) {
        }
    }

    private static class AnnotatedTestClass extends AnnotatedParentClass {
        @OnReceive
        private native void onConcreteMessage(final String x);

        @Override
        public String toString() {
            return "TestClass";
        }
    }

    private static class AnnotatedParentClass {
        @OnReceive
        private Integer onConcreteMessage(final Integer x) {
            return x;
        }

        @OnReceive
        private Long one(final Long x) {
            return x;
        }

        @OnReceive
        private Long another(final Long x) {
            return x;
        }
    }
}
