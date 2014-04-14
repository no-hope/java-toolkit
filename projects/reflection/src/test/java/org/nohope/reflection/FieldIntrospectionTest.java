package org.nohope.reflection;

import org.junit.Test;
import org.nohope.reflection.test.ClassWithPrivateField;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 11:31
 */
public class FieldIntrospectionTest {
    @Test
    public void privateFieldAccess() throws NoSuchFieldException, IllegalAccessException {
        final ClassWithPrivateField obj = new ClassWithPrivateField();
        final Integer i = IntrospectionUtils.getFieldValue(obj, "i", Integer.class);
        assertEquals((Integer) 0, i);

        try {
            IntrospectionUtils.getFieldValue(obj, "j", Integer.class);
            fail();
        } catch (NoSuchFieldException ignored) {
        }

        try {
            IntrospectionUtils.getFieldValue(obj, "i", List.class);
            fail();
        } catch (final ClassCastException ignored) {
        }
    }

}
