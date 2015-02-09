package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.nohope.cassandra.mapservice.CTypeConverter;

/**
 */
public class EnumType<E extends Enum<E>> extends CTypeConverter<E, String> {
    private final Class<E> clazz;

    public EnumType(final Class<E> clazz) {
        this.clazz = clazz;
    }

    @Override
    public CType getCType() {
        return CType.TEXT;
    }

    @Override
    public E readValue(final Row result, final String name) {
        return E.valueOf(clazz, result.getString(name));
    }

    @Override
    protected String convert(final E value) {
        return value.toString();
    }
}
