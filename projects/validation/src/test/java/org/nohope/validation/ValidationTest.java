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

        public void illegalValidatorConstructor(@Validate(InvalidValidator.class) final TestObject obj) {
        }

        @Validate(TestValidator.class)
        public TestObject getter(final int param) {
            return new TestObject(param);
        }

        @Validate(InvalidValidator.class)
        public TestObject illegalGetter(final int param) {
            return new TestObject(param);
        }

        @Validate(TestValidator.class)
        public Object illegalGetter2(final Object param) {
            return param;
        }
    }

    @Test
    public void parameters() {
        new SimpleObject(new TestObject(1));

        try {
            new SimpleObject(new TestObject(-1));
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

        try {
            new SimpleObject().illegalValidatorConstructor(new TestObject(1));
            fail();
        } catch (final IllegalStateException e) {
            assertTrue(e.getCause() instanceof ValidatorInitializationException);
        }
    }

    @Test
    public void returnValue() {
        new SimpleObject().getter(1);

        try {
            new SimpleObject().illegalGetter2(1);
            fail();
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof ValidatingTypeMismatch);
        }

        try {
            new SimpleObject().illegalGetter(1);
            fail();
        } catch (final IllegalStateException e) {
            assertTrue(e.getCause() instanceof ValidatorInitializationException);
        }

        try {
            new SimpleObject().getter(-1);
            fail();
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getCause() instanceof ValidationException);
        }
    }
}
