package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import com.google.common.collect.Lists;
import org.nohope.reflection.TypeReference;

import java.util.List;
import java.util.Objects;
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
        return Lists.transform(value, converter::asCassandraValue);
    }

    @Override
    public List<C> asJavaValue(final List<T> value) {
        return Lists.transform(value, converter::asJavaValue);
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ListType<?, ?> listType = (ListType<?, ?>) o;
        return Objects.equals(converter, listType.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(converter);
    }

    @Override
    public String toString() {
        return "ListType{" +
               "converter=" + converter +
               '}';
    }
}
