package org.nohope.jaxb2.plugin.validation;

import org.junit.Test;

import static org.nohope.test.SerializationUtils.cloneJava;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-16 19:12
 */
public class ValidationExceptionTest {
    @Test
    public void serialization() {
        cloneJava(new ValidationException());
        cloneJava(new ValidationException("test"));
        cloneJava(new ValidationException(new IllegalStateException()));
        cloneJava(new ValidationException("test", new IllegalStateException()));
    }
}
