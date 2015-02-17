package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import org.nohope.reflection.TypeReference;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
        return value.stream().map(converter::asCassandraValue)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<J> asJavaValue(final Set<C> value) {
        return value.stream().map(converter::asJavaValue)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
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
    public Set<J> readValue(final Row result, final String name) {
        return asJavaValue(result.getSet(name, converter.getCassandraType().getReference().getTypeClass()));
    }
}
