package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.nohope.cassandra.mapservice.CTypeConverter;

import java.util.List;

/**
 */
public class CListType<T, C> extends CTypeConverter<List<T>, C> {
    private final Class<T> clazz;
    private final CType type;

    CListType(final Class<T> clazz, final CType type) {
        this.clazz = clazz;
        this.type = type;
    }

    public static <T, C> CListType<T, C> of(final Class<T> clazz, final CType type) {
        return new CListType<>(clazz, type);
    }

    protected Class<T> getClazz() {
        return clazz;
    }

    @Override
    public CType getCType() {
        return type;
    }

    @Override
    public List<T> readValue(final Row result, final String name) {
        return result.getList(name, clazz);
    }

    @Override
    protected C convert(final List<T> value) {
        return (C) value;
    }
}
