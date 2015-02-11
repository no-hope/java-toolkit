package org.nohope.cassandra.mapservice.columns;

import org.nohope.cassandra.mapservice.ctypes.Converter;

/**
 */
public class CColumn<V, C> {
    private final String name;
    private final Converter<C, V> converter;

    public CColumn(final String name, final Converter<C, V> converter) {
        this.name = name;
        this.converter = converter;
    }

    public static <V, C> CColumn<C, V> of(final String name, final Converter<V, C> converter) {
        return new CColumn<>(name, converter);
    }

    public String getName() {
        return name;
    }

    public Converter<C, V> getConverter() {
        return converter;
    }

    public String getColumnTemplate() {
        return name + ' ' + converter.getCassandraType().getTypeName();
    }

    @Override
    public String toString() {
        return name + ':' + converter.getCassandraType().getTypeName();
    }
}
