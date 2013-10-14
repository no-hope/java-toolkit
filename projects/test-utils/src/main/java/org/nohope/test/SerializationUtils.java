package org.nohope.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jongo.marshall.MarshallingException;
import org.nohope.jongo.TypeSafeJacksonMapperBuilder;
import org.nohope.logging.Logger;
import org.nohope.logging.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/21/12 12:48 AM
 */
public final class SerializationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SerializationUtils.class);
    private static final ObjectMapper MAPPER = TypeSafeJacksonMapperBuilder.createPreConfiguredMapper();

    private static <T> T unmarshall(@Nonnull final String json, @Nonnull final Class<T> clazz) {
        try {

            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            final String message = String.format("Unable to unmarshall from json: %s to %s", json, clazz);
            throw new MarshallingException(message, e);
        }
    }

    private static <T> String marshall(final T obj) {
        try {
            final Writer writer = new StringWriter();
            MAPPER.writeValue(writer, obj);
            return writer.toString();
        } catch (Exception e) {
            final String message = String.format("Unable to marshall json from: %s", obj);
            throw new MarshallingException(message, e);
        }
    }

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
        final String marshalled = marshall(object);
        LOG.debug("marshaled value {}", marshalled);
        return (T) unmarshall(marshalled, object.getClass());
    }

    public static<T> T assertMongoClonedEquals(final T origin) {
        final T result = cloneMongo(origin);
        assertEquals(origin, result);
        return result;
    }
}
