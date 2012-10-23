package org.nohope.akka;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;
import static org.nohope.akka.MessageMethodInvoker.SignaturePair;

/**
 * Date: 25.07.12
 * Time: 11:29
 */
@SuppressWarnings("MethodMayBeStatic")
public class MessageMethodInvokerTest {

    @OnReceive
    private Integer processInteger(final Integer a) {
        return a;
    }

    @OnReceive
    private Double processDouble(final Double b) {
        return b;
    }

    @Test
    public void testAnnotatedInvoker() throws Exception {
        final Object a1 = 100;
        assertEquals(a1, MessageMethodInvoker.invokeOnReceive(this, a1));
        final Object a2 = 100.0;
        assertEquals(a2, MessageMethodInvoker.invokeOnReceive(this, a2));
    }

    @Test
    public void testCache() throws Exception {
        MessageMethodInvoker.invokeOnReceive(this, 100);
        assertTrue(MessageMethodInvoker.cache.containsKey(
                SignaturePair.of(MessageMethodInvokerTest.class, new Class<?>[]{Integer.class})));
    }

    @Test
    public void testNoAnnotatedMethod() throws Exception {
        try {
            MessageMethodInvoker.invokeOnReceive(this, "xxx");
            fail();
        } catch (final NoSuchMethodException e) {
        }

        try {
            MessageMethodInvoker.invokeOnReceive(new AnnotatedTestClass(), "yyy");
            fail();
        } catch (final NoSuchMethodException e) {
        }
    }

    @Test
    public void inheritance() throws Exception {
        assertEquals(1, MessageMethodInvoker.invokeOnReceive(new AnnotatedTestClass(), 1));
        assertEquals(2, MessageMethodInvoker.invokeOnReceive(new AnnotatedParentClass(), 2));
    }

    @Test
    public void multipleMatch() throws Exception {
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

    @Test(expected = UserException.class)
    public void exceptionRethrowing() throws Exception {
        MessageMethodInvoker.invokeOnReceive(new AnnotatedParentClass(), String.class);
    }

    @Test(expected = UserRuntimeException.class)
    public void runtimeExceptionRethrowing() throws Exception {
        MessageMethodInvoker.invokeOnReceive(new AnnotatedParentClass(), '\n');
    }

    @Test(expected = UserError.class)
    public void errorRethrowing() throws Exception {
        MessageMethodInvoker.invokeOnReceive(new AnnotatedParentClass(), 1d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwableRethrowing() throws Exception {
        MessageMethodInvoker.invokeOnReceive(new AnnotatedParentClass(), 1f);
    }

    @Test
    public void signaturePairEquals() {
        final SignaturePair pair1 = new SignaturePair(String.class,
                new Class<?>[] {String.class, Integer.class});
        final SignaturePair pair2 = new SignaturePair(String.class,
                new Class<?>[] {String.class, Integer.class});
        final SignaturePair pair3 = new SignaturePair(String.class,
                new Class<?>[] {String.class, String.class});

        assertEquals(pair1, pair2);
        assertEquals(pair1.hashCode(), pair2.hashCode());

        assertFalse(pair1.equals(pair3));
        assertFalse(pair2.equals(pair3));
    }

    private static class UserException extends Exception {
        private static final long serialVersionUID = 5588207554344224650L;
    }

    private static class UserRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 5588207554344224650L;
    }

    private static class UserError extends Error {
        private static final long serialVersionUID = 5588207554344224650L;
    }

    private static class UserThrowable extends Throwable {
        private static final long serialVersionUID = 5588207554344224650L;
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

        @OnReceive
        private void exceptional(final Class<?> x) throws UserException {
            throw new UserException();
        }

        @OnReceive
        private void runtimeExceptional(final Character x) {
            throw new UserRuntimeException();
        }

        @OnReceive
        private void erroneous(final Double x) {
            throw new UserError();
        }

        @OnReceive
        private void throwable(final Float x) throws UserThrowable {
            throw new UserThrowable();
        }
    }

    private static class AnnotatedTestClass extends AnnotatedParentClass {
        @SuppressWarnings("unused")
        private native void onConcreteMessage(final String x);

        @Override
        public String toString() {
            return "TestClass";
        }
    }
}
