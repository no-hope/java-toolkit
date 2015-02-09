package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import com.google.common.base.Throwables;
import org.nohope.cassandra.mapservice.CTypeConverter;
import org.nohope.serialization.streams.SerializationProvider;
import org.nohope.serialization.streams.SerializationProviderUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class SerializableType<T extends Serializable> extends CTypeConverter<T, ByteBuffer> {
    private final SerializationProvider provider;
    private final Class<T> clazz;

    public SerializableType(final SerializationProvider provider, final Class<T> clazz) {
        this.provider = provider;
        this.clazz = clazz;
    }

    @Override
    public CType getCType() {
        return CType.BLOB;
    }

    @Override
    public T readValue(final Row result, final String name) {
        try {
            return fromCassandra(result.getBytes(name));
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    protected ByteBuffer convert(final T value) {
        try {
            return ByteBuffer.wrap(SerializationProviderUtils.toByteArray(value, provider));
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Nullable
    private T fromCassandra(final Object value) throws IOException {
        // abnormal case
        if (value == null) {
            return null;
        }

        final ByteBuffer buffer = (ByteBuffer) value;
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return SerializationProviderUtils.fromByteArray(bytes, clazz, provider);
    }
}
