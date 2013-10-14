package org.nohope.validation;

import org.junit.Test;

import static org.nohope.test.SerializationUtils.cloneJava;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-14 18:51
 */
public class ValidationExceptionTest {

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void serialization() {
        cloneJava(new ValidationException("test"));
        cloneJava(new ValidationException(new IllegalStateException()));
        cloneJava(new ValidationException("test", new IllegalStateException()));
    }
}
