package org.nohope.bean;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 12/10/12 3:28 PM
 */
public class StatefulDispatcherTest {

    @Test
    public void naiveTest() {
        final Dispatcher dispatcher = new Dispatcher();
        final SimpleBean a = new SimpleBean(dispatcher);
        final SimpleBean b = new SimpleBean(dispatcher);
        assertFalse(dispatcher.isHeated(a));
        assertFalse(dispatcher.isHeated(b));
        assertNull(dispatcher.getOldVal(a));
        assertNull(dispatcher.getNewVal(a));
        assertNull(dispatcher.getOldVal(b));
        assertNull(dispatcher.getNewVal(b));
        a.setA(1);
        assertFalse(dispatcher.isHeated(a));
        assertFalse(dispatcher.isHeated(b));
        assertNull(dispatcher.getOldVal(a));
        assertEquals(1, dispatcher.getNewVal(a));
        assertNull(dispatcher.getOldVal(b));
        assertNull(dispatcher.getNewVal(b));
        b.setA(2);
        assertFalse(dispatcher.isHeated(a));
        assertFalse(dispatcher.isHeated(b));
        assertNull(dispatcher.getOldVal(a));
        assertEquals(1, dispatcher.getNewVal(a));
        assertNull(dispatcher.getOldVal(b));
        assertEquals(2, dispatcher.getNewVal(b));
        a.setA(3);
        assertTrue(dispatcher.isHeated(a));
        assertFalse(dispatcher.isHeated(b));
        assertEquals(1, dispatcher.getOldVal(a));
        assertEquals(3, dispatcher.getNewVal(a));
        assertNull(dispatcher.getOldVal(b));
        assertEquals(2, dispatcher.getNewVal(b));
        b.setA(4);
        assertTrue(dispatcher.isHeated(a));
        assertTrue(dispatcher.isHeated(b));
        assertEquals(1, dispatcher.getOldVal(a));
        assertEquals(3, dispatcher.getNewVal(a));
        assertEquals(2, dispatcher.getOldVal(b));
        assertEquals(4, dispatcher.getNewVal(b));

    }

    private static class SimpleBean extends AbstractDispatchable<SimpleBean> {
        private int param = 0;

        protected SimpleBean(@Nonnull final IDispatcher<SimpleBean> dispatcher) {
            super(dispatcher);
        }

        @Dispatch
        public void setA(final int param) {
            this.param = param;
        }

        public int getA() {
            return param;
        }
    }

    private static class Dispatcher extends StatefulDispatcher<SimpleBean> {
        private final Map<Object, Boolean> heated = new HashMap<>();
        private final Map<Object, Object> oldVal = new HashMap<>();
        private final Map<Object, Object> newVal = new HashMap<>();

        @Override
        protected void handle(@Nonnull final SimpleBean obj
                , @Nonnull final String propertyName
                , final Object oldValue
                , final Object newValue
                , final boolean previousExists) {
            oldVal.put(obj, oldValue);
            newVal.put(obj, newValue);
            if (previousExists) {
                heated.put(obj, true);
            }
        }

        private Object getOldVal(final Object bean) {
            return oldVal.get(bean);
        }

        private Object getNewVal(final Object bean) {
            return newVal.get(bean);
        }

        public boolean isHeated(final Object bean) {
            return heated.containsKey(bean);
        }
    }
}
