package org.nohope.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jongo.marshall.MarshallingException;
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
import static org.nohope.jongo.TypeSafeJacksonMapperBuilder.createPreConfiguredMapper;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/21/12 12:48 AM
 */
public final class SerializationUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SerializationUtils.class);
    private static final ObjectMapper MONGO_MAPPER = createPreConfiguredMapper();
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
                return (T) in.readObject();
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

    public static <T> T fromJSON(@Nonnull final ObjectMapper mapper,
                                 @Nonnull final String json,
                                 @Nonnull final Class<T> clazz) {
        try {

            return mapper.readValue(json, clazz);
        } catch (final Exception e) {
            final String message = String.format("Unable to unmarshall from json: %s to %s", json, clazz);
            throw new MarshallingException(message, e);
        }
    }

    public static <T> String toJSON(@Nonnull final ObjectMapper mapper, final T obj) {
        try {
            final Writer writer = new StringWriter();
            mapper.writeValue(writer, obj);
            return writer.toString();
        } catch (final Exception e) {
            final String message = String.format("Unable to marshall json from: %s", obj);
            throw new MarshallingException(message, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cloneJson(@Nonnull final ObjectMapper mapper, final T object) {
        final String marshalled = toJSON(mapper, object);
        LOG.trace("marshaled value {}", marshalled);
        return (T) fromJSON(mapper, marshalled, object.getClass());
    }

    public static<T> T assertJsonClonedEquals(@Nonnull final ObjectMapper mapper, final T origin) {
        final T result = cloneJson(mapper, origin);
        assertEquals(origin, result);
        return result;
    }

    public static <T> T fromMongo(@Nonnull final String json, @Nonnull final Class<T> clazz) {
        return fromJSON(MONGO_MAPPER, json, clazz);
    }

    public static <T> String toMongo(final T obj) {
        return toJSON(MONGO_MAPPER, obj);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cloneMongo(final T object) {
        return cloneJson(MONGO_MAPPER, object);
    }

    public static<T> T assertMongoClonedEquals(final T origin) {
        return assertJsonClonedEquals(MONGO_MAPPER, origin);
    }
}
