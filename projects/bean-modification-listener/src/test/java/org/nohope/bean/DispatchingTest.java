package org.nohope.bean;

import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 12/7/12 7:50 PM
 */
public class DispatchingTest {

    @Test
    public void dispatching() {
        final Map<String, Object> map = new HashMap<>();
        final TestDispatchable test = new TestDispatchable(new IDispatcher() {
            @Override
            public void handle(@Nonnull final IDispatchable obj,
                               @Nonnull final String propertyName,
                               final Object newValue) {
                map.put(propertyName, newValue);
            }
        });

        test.setSomething("test1");
        test.set("test2");
        test.set(1);

        assertEquals(3, map.size());
        assertEquals("test1", map.get("something"));
        assertEquals("test2", map.get("set"));
        assertEquals(1, map.get("test"));
    }
}
