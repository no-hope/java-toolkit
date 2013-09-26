package org.nohope.validation;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/26/13 11:50 AM
 */
public class TestValidator implements IValidator<TestObject> {
    @Override
    public void validate(final TestObject obj) throws ValidationException {
        if (obj.getVal() < 0) {
            throw new ValidationException("Value should be >= 0");
        }
    }

    @Nonnull
    @Override
    public Class<TestObject> getType() {
        return TestObject.class;
    }
}
