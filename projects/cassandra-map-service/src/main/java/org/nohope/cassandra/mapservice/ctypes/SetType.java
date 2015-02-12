package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.reflection.TypeReference;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 */
class SetType<C, J> implements Converter<Set<C>, Set<J>> {
    private final Converter<C, J> converter;

    SetType(final Converter<C, J> converter) {
        this.converter = converter;
    }

    public static <T> SetType<T, T> of(final CoreConverter<T> type) {
        return new SetType<>(type);
    }

    @Override
    public Set<C> asCassandraValue(final Set<J> value) {
        final Set<C> result = new LinkedHashSet<>();
        for (final J elem : value) {
            result.add(converter.asCassandraValue(elem));
        }

        return result;
    }

    @Override
    public Set<J> asJavaValue(final Set<C> value) {
        final Set<J> result = new LinkedHashSet<>();
        for (final C elem : value) {
            result.add(converter.asJavaValue(elem));
        }

        return result;
    }

    @Override
    public TypeDescriptor<Set<C>> getCassandraType() {
        return TypeDescriptor.set(converter.getCassandraType());
    }

    @Override
    public TypeReference<Set<J>> getJavaType() {
        return new TypeReference<Set<J>>() {};
    }

    @Override
    public Set<J> readValue(final Row result, final CColumn<Set<J>, Set<C>> column) {
        return asJavaValue(result.getSet(column.getName(), converter.getCassandraType().getReference().getTypeClass()));
    }
}
