package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import org.nohope.cassandra.mapservice.BindUtils;
import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.Converter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#eq(String, Object)}
 */
@Immutable
final class Filter<V> implements CFilter<V> {
    private final CColumn<?, ?> column;
    private final V value;
    private final BiFunction<String, Object, Clause> underlyingExpression;

    Filter(@Nonnull final CColumn<?, ?> column,
           @Nonnull final V value,
           final BiFunction<String, Object, Clause> underlyingExpression) {
        this.column = column;
        this.value = value;
        this.underlyingExpression = underlyingExpression;
    }

    @Override
    public String getColumnName() {
        return column.getName();
    }

    @Override
    public Clause apply(final Converter<?, V> converter) {
        return underlyingExpression.apply(column.getName(), BindUtils.maybeBindable(converter, value));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final Filter<?> eqFilter = (Filter<?>) o;
        return column.equals(eqFilter.column)
            && Objects.deepEquals(value, eqFilter.value)
            && underlyingExpression.equals(eqFilter.underlyingExpression)
             ;
    }

    @Override
    public int hashCode() {
        int result = column.hashCode();
        result = (31 * result) + value.hashCode();
        result = (31 * result) + underlyingExpression.hashCode();
        return result;
    }
}
