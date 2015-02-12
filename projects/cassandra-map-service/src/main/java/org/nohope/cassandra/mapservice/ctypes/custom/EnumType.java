package org.nohope.cassandra.mapservice.ctypes.custom;

import org.nohope.cassandra.mapservice.ctypes.AbstractConverter;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;

/**
 */
public class EnumType<E extends Enum<E>> extends AbstractConverter<String, E> {
    private final Class<E> clazz;

    public EnumType(final Class<E> clazz) {
        super(clazz, CoreConverter.TEXT);
        this.clazz = clazz;
    }

    @Override
    public String asCassandraValue(final E value) {
        return value.name();
    }

    @Override
    public E asJavaValue(final String value) {
        return E.valueOf(clazz, value);
    }
}
