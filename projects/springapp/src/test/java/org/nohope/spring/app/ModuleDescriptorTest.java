package org.nohope.spring.app;

import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-10 14:15
 */
public class ModuleDescriptorTest {

    @Test
    public void repr() {
        final GenericApplicationContext ctx = new GenericApplicationContext();
        final ModuleDescriptor<Object> descriptor =
                new ModuleDescriptor<>("test-module", new Object(), new Properties(), ctx);
        assertEquals("test-module@java.lang.Object", descriptor.toString());
    }
}
