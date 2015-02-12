package org.nohope.cassandra.mapservice.cfilter;

import com.datastax.driver.core.querybuilder.Clause;
import org.nohope.cassandra.mapservice.BindUtils;
import org.nohope.cassandra.mapservice.Value;
import org.nohope.cassandra.mapservice.ctypes.Converter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.function.BiFunction;

/**
 * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#eq(String, Object)}
 */
@Immutable
final class Filter<V> implements CFilter<V> {
    private final Value<V> value;
    private final BiFunction<String, Object, Clause> underlyingExpression;

    Filter(@Nonnull final Value<V> value,
           final BiFunction<String, Object, Clause> underlyingExpression) {
        this.value = value;
        this.underlyingExpression = underlyingExpression;
    }

    @Override
    public Clause apply(final Converter<?, V> converter) {
        return underlyingExpression.apply(
                value.getColumn().getName(),
                BindUtils.maybeBindable(value));
    }

    @Override
    public Value<V> getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final Filter<?> that = (Filter<?>) o;
        return value.equals(that.value)
            && underlyingExpression.equals(that.underlyingExpression)
             ;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = (31 * result) + underlyingExpression.hashCode();
        return result;
    }
}
