package org.nohope.test;

import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;

import java.io.*;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/21/12 12:48 AM
 */
public final class SerializationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SerializationUtils.class);

    private SerializationUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T cloneJava(final T object) {
        try(final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
            out.writeObject(object);
            try(final ByteArrayInputStream inputStream =
                        new ByteArrayInputStream(outputStream.toByteArray());
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

    public static<T extends Serializable> T assertJavaClonedEquals(final T origin) {
        final T result = cloneJava(origin);
        assertEquals(origin, result);
        return result;
    }
}
