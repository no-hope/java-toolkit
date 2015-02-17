package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.nohope.reflection.TypeReference;

import java.util.List;
import java.util.stream.Collectors;

/**
 */
class ListType<T, C> implements Converter<List<T>, List<C>> {
    private final Converter<T, C> converter;

    ListType(final Converter<T, C> converter) {
        this.converter = converter;
    }

    public static <T> ListType<T, T> of(final CoreConverter<T> type) {
        return new ListType<>(type);
    }

    @Override
    public List<T> asCassandraValue(final List<C> value) {
        return Lists.transform(value, new Function<C, T>() {
            @Override
            public T apply(final C input) {
                return converter.asCassandraValue(input);
            }
        });
    }

    @Override
    public List<C> asJavaValue(final List<T> value) {
        return Lists.transform(value, new Function<T, C>() {
            @Override
            public C apply(final T input) {
                return converter.asJavaValue(input);
            }
        });
    }

    @Override
    public TypeDescriptor<List<T>> getCassandraType() {
        return TypeDescriptor.list(converter.getCassandraType());
    }

    @Override
    public TypeReference<List<C>> getJavaType() {
        return new TypeReference<List<C>>() {};
    }

    @Override
    public List<C> readValue(final Row result, final String name) {
        final Class<T> clazz = converter.getCassandraType().getReference().getTypeClass();
        return result.getList(name, clazz)
                     .stream()
                     .map(converter::asJavaValue)
                     .collect(Collectors.toList());
    }
}
