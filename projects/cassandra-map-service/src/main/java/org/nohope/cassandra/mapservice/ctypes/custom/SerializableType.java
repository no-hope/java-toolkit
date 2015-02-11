package org.nohope.cassandra.mapservice.ctypes.custom;

import com.google.common.base.Throwables;
import org.nohope.cassandra.mapservice.ctypes.AbstractConverter;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;
import org.nohope.serialization.streams.KryoProvider;
import org.nohope.serialization.streams.SerializationProvider;
import org.nohope.serialization.streams.SerializationProviderUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class SerializableType<J extends Serializable> extends AbstractConverter<ByteBuffer, J> {
    private final SerializationProvider provider;

    public SerializableType(final SerializationProvider provider, final Class<J> clazz) {
        super(clazz, CoreConverter.BLOB);
        this.provider = provider;
    }

    public static <T extends Serializable> SerializableType<T> kryo(final Class<T> clazz) {
        return new SerializableType<>(new KryoProvider(), clazz);
    }

    @Override
    public ByteBuffer asCassandraValue(final J value) {
        return SerializationProviderUtils.toByteBuffer(value, provider);
    }

    @Override
    public J asJavaValue(final ByteBuffer buffer) {
        // abnormal case
        if (buffer == null) {
            return null;
        }

        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        try {
            return SerializationProviderUtils.fromByteArray(bytes, getJavaType().getTypeClass(), provider);
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
