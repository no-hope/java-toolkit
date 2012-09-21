package org.nohope.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/21/12 12:48 AM
 */
public final class SerializationUtils {
    private SerializationUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T cloneJava(final T object) {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
            out.writeObject(object);
            try(ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                ObjectInputStream in = new ObjectInputStream(inputStream)) {
                final T result = (T) in.readObject();
                assertNotSame(object, result);
                return result;
            }
        } catch (IOException | ClassNotFoundException e) {
            fail(e.getMessage());
        }

        return null;
    }
}
