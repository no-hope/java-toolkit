package org.nohope.test;

import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;
import org.nohope.jongo.JacksonProcessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

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

    @SuppressWarnings("unchecked")
    public static <T> T cloneMongo(final T object) {
        final JacksonProcessor marshaller = new JacksonProcessor();
        final String marshalled = marshaller.marshall(object);
        LOG.debug("marshaled value {}", marshalled);

        // creating new processor just to be sure no state was shared
        // between jackson serializrs/deserializers
        final JacksonProcessor unmarshaller = new JacksonProcessor();
        return (T) unmarshaller.unmarshall(marshalled, object.getClass());
    }

    public static<T> T assertMongoClonedEquals(final T origin) {
        final T result = cloneMongo(origin);
        assertEquals(origin, result);
        return result;
    }
}
