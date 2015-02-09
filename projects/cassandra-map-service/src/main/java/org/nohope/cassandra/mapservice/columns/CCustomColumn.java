package org.nohope.cassandra.mapservice.columns;

import org.nohope.cassandra.mapservice.CTypeConverter;

/**
 */
public class CCustomColumn<V, C> extends CColumn<V, C> {
    protected CCustomColumn(final String name, final CTypeConverter<V, C> converter) {
        super(name, converter);
    }

    public static <V, C> CCustomColumn<V, C> of(final String name, final CTypeConverter<V, C> converter) {
        return new CCustomColumn<>(name, converter);
    }
}
