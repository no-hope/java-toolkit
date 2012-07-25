package org.nohope.reflection;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Date: 25.07.12
 * Time: 11:29
 */
public class MessageMethodInvokerTest {
    boolean intInvoked = false;
    boolean dblInvoked = false;
    private void onConcreteMessage(final Integer a) {
        intInvoked = true;
    }

    private void onConcreteMessage(final Double b) {
        dblInvoked = true;
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

    @Test(expected = NoSuchMethodException.class)
    public void testNoMethod() throws NoSuchMethodException {
            MessageMethodInvoker.invokeHandler(this, "xxx");
    }
}
