package org.nohope.serialization.streams;

import com.esotericsoftware.kryo.io.Input;
import org.nohope.serialization.ByteBufferUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 */
public final class SerializationProviderUtils {

    private SerializationProviderUtils() {
    }


    public static <T extends Serializable> ByteBuffer toByteBuffer(
            final T object,
            final SerializationProvider serializationProvider
                                                        ) {
        final ByteBuffer value;
        try {
            value = ByteBuffer.wrap(toByteArray(object, serializationProvider));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        return value;
    }


    public static <T extends Serializable> T fromByteBuffer(final Class<T> clazz, final ByteBuffer blob, final SerializationProvider serializationProvider) {
        try {
            return fromByteArray(ByteBufferUtils.continuousArray(blob), clazz, serializationProvider);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public static byte[] toByteArray(
            final Serializable object,
            final SerializationProvider provider
    ) throws IOException {
        try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            provider.writeObject(stream, object);
            return stream.toByteArray();
        }
    }

    public static <T extends Serializable> T fromByteArray(
            final byte[] content,
            final Class<T> valueType,
            final SerializationProvider provider
    ) throws IOException {
        try (final ByteArrayInputStream stream = new ByteArrayInputStream(content)) {
            return provider.readObject(stream, valueType);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(
            final T origin,
            final SerializationProvider provider
    ) throws IOException {
        final Class<T> clazz = (Class<T>) origin.getClass();

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            provider.writeObject(outputStream, origin);
            outputStream.flush();
            //LOG.debug("Stream size for {} = {}", provider, FileUtils.byteCountToDisplaySize(outputStream.size()));
            try (final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                 final Input input = new Input(inputStream)) {
                return provider.readObject(input, clazz);
            }
        }
    }
}
