package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.ctypes.Converter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Arrays;

/**
 * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#in(String, Object...)}
 */
@Immutable
final class InFilter<V> implements CFilter<V[]> {
    private final String columnName;
    private final V[] values;

    InFilter(@Nonnull final String key, @Nonnull final V... value) {
        this.columnName = key;
        this.values = value.clone();
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public Clause apply(final Converter<?, V[]> converter) {
        return QueryBuilder.in(columnName, converter.asCassandraValue(values));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final InFilter<?> inFilter = (InFilter<?>) o;
        return columnName.equals(inFilter.columnName) && Arrays.equals(values, inFilter.values);
    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = (31 * result) + Arrays.hashCode(values);
        return result;
    }
}
