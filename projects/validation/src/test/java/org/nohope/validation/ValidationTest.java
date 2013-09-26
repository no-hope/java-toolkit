package org.nohope.validation;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/26/13 1:43 PM
 */
public class ValidationTest {

    class SimpleObject {
        public SimpleObject() {
        }

        public SimpleObject(@Validate(TestValidator.class) final TestObject obj) {
        }

        public void setter(@Validate(TestValidator.class) final TestObject obj) {
        }

        public void illegalValidator(@Validate(TestValidator.class) final Object obj) {
        }

        @Validate(TestValidator.class)
        public TestObject getter(final int param) {
            return new TestObject(param);
        }
    }

    @Test
    public void returnValueTest() {
        try {
            new SimpleObject(new TestObject(-1));
            fail();
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof ValidationException);
        }

        try {
            new SimpleObject().getter(-1);
            fail();
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof ValidationException);
        }

        try {
            new SimpleObject().setter(new TestObject(-1));
            fail();
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof ValidationException);
        }

        try {
            new SimpleObject().illegalValidator(new Object());
            fail();
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof ValidatingTypeMismatch);
        }
    }
}
