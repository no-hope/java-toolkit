package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import org.nohope.cassandra.mapservice.BindUtils;
import org.nohope.cassandra.mapservice.ctypes.Converter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.function.BiFunction;

/**
 * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#eq(String, Object)}
 */
@Immutable
abstract class AbstractFilter<V> implements CFilter<V> {
    private final String columnName;
    private final V value;

    protected AbstractFilter(@Nonnull final String key, final V value) {
        this.columnName = key;
        this.value = value;
    }

    @Override
    public final String getColumnName() {
        return columnName;
    }

    protected abstract BiFunction<String, Object, Clause> underlyingExpression();

    @Override
    public final Clause apply(final Converter<?, V> converter) {
        return underlyingExpression().apply(columnName, BindUtils.maybeBindable(converter, value));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final AbstractFilter<?> eqFilter = (AbstractFilter<?>) o;
        return columnName.equals(eqFilter.columnName)
               && ((value == null) ? (eqFilter.value == null) : value.equals(eqFilter.value));
    }

    @Override
    public int hashCode() {
        int result = columnName.hashCode();
        result = (31 * result) + ((value != null) ? value.hashCode() : 0);
        return result;
    }
}
