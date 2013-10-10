package org.nohope.reflection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-11 01:27
 */
public class CallerReflectionTest {
    private class InnerClass {
        public class InnerClass2 {
            public Class<?> call() {
                return IntrospectionUtils.reflectCallerClass();
            }
        }

        public Class<?> call() {
            return IntrospectionUtils.reflectCallerClass();
        }

        public Class<?> callInner() {
            return new InnerClass2().call();
        }
    }

    @Test
    public void callerClass() {
        assertEquals(CallerReflectionTest.class, new InnerClass().call());
        assertEquals(CallerReflectionTest.class, new InnerClass().new InnerClass2().call());
        assertEquals(InnerClass.class, new InnerClass().callInner());
    }
}
