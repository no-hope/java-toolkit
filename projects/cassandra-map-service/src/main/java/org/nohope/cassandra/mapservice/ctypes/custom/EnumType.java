package org.nohope.cassandra.mapservice.ctypes.custom;

import com.datastax.driver.core.Row;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;
import org.nohope.cassandra.mapservice.ctypes.Converter;
import org.nohope.cassandra.mapservice.ctypes.TypeDescriptor;
import org.nohope.reflection.TypeReference;

/**
 */
public class EnumType<E extends Enum<E>> implements Converter<String, E> {
    private final Class<E> clazz;

    public EnumType(final Class<E> clazz) {
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

    @Override
    public TypeDescriptor<String> getCassandraType() {
        return CoreConverter.TEXT.getCassandraType();
    }

    @Override
    public TypeReference<E> getJavaType() {
        return TypeReference.erasure(clazz);
    }

    @Override
    public E readValue(final Row result, final String name) {
        return E.valueOf(clazz, result.getString(name));
    }
}
