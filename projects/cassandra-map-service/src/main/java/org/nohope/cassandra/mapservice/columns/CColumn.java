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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final CColumn<?, ?> cColumn = (CColumn<?, ?>) o;
        return converter.equals(cColumn.converter) && name.equals(cColumn.name);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = (31 * result) + converter.hashCode();
        return result;
    }
}
