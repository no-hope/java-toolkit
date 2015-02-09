package org.nohope.cassandra.mapservice.ctypes;

import org.nohope.serialization.streams.KryoProvider;
import org.nohope.serialization.streams.SerializationProvider;

import java.io.Serializable;

/**
 */
public final class KryoSerializableType<T extends Serializable> extends SerializableType<T> {
    private static final SerializationProvider KRYO = new KryoProvider();

    public KryoSerializableType(final Class<T> clazz) {
        super(KRYO, clazz);
    }
}
