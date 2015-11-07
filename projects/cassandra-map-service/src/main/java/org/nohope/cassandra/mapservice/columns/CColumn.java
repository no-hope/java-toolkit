package org.nohope.cassandra.mapservice.columns;

import com.datastax.driver.core.Row;
import org.nohope.cassandra.mapservice.Value;
import org.nohope.cassandra.mapservice.ctypes.Converter;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;
import org.nohope.cassandra.mapservice.ctypes.TypeDescriptor;
import org.nohope.reflection.TypeReference;

import java.util.List;
import java.util.Set;

/**
 */
public class CColumn<JavaType, CassandraType> {
    private final String name;
    private final Converter<CassandraType, JavaType> converter;

    public CColumn(final String name, final Converter<CassandraType, JavaType> converter) {
        this.name = name;
        this.converter = converter;
    }

    public CColumn<List<JavaType>, List<CassandraType>> asList() {
        return new CColumn<>(name, CoreConverter.list(converter));
    }

    public CColumn<Set<JavaType>, Set<CassandraType>> asSet() {
        return new CColumn<>(name, CoreConverter.set(converter));
    }

    public static <V, C> CColumn<C, V> of(final String name,
                                          final Converter<V, C> converter) {
        return new CColumn<>(name, converter);
    }

    public TypeDescriptor<CassandraType> getCassandraType() {
        return converter.getCassandraType();
    }

    public TypeReference<JavaType> getJavaType() {
        return converter.getJavaType();
    }

    public String getName() {
        return name;
    }

    public String getColumnTemplate() {
        return name + ' ' + converter.getCassandraType().getTypeName();
    }

    public Value<JavaType> getValue(final Row row) {
        return Value.bound(this, converter.readValue(row, name));
    }

    public Object asCassandraValue(final Value<JavaType> value) {
        return value.getBoundValue()
                    .map(converter::asCassandraValue)
                    .map(Object.class::cast)
                    .orElse(value.getValue());
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
