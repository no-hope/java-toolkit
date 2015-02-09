package org.nohope.cassandra.mapservice.columns;

import org.nohope.cassandra.mapservice.CTypeConverter;

/**
 */
public class CColumn<V, C> {
    private final String name;
    private final CTypeConverter<V, C> converter;

    public CColumn(final String name, final CTypeConverter<V, C> converter) {
        this.name = name;
        this.converter = converter;
    }

    public String getName() {
        return name;
    }

    public CTypeConverter<V, C> getConverter() {
        return converter;
    }

    public String getColumnTemplate() {
        return name + ' ' + converter.getCType().getType();
    }

    @Override
    public String toString() {
        return name + ':' + converter.getCType();
    }
}
