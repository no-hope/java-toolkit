package org.nohope.cassandra.mapservice.cops;

import com.datastax.driver.core.querybuilder.Assignment;
import org.nohope.cassandra.mapservice.Value;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.function.BiFunction;

/**
 * Wrapper to {@link com.datastax.driver.core.querybuilder.QueryBuilder#eq(String, Object)}
 */
@Immutable
final class OperationImpl<V, T> implements Operation<V> {
    private final Value<V> value;
    private final BiFunction<String, T, Assignment> underlyingExpression;

    OperationImpl(@Nonnull final Value<V> value,
                  final BiFunction<String, T, Assignment> underlyingExpression) {
        this.value = value;
        this.underlyingExpression = underlyingExpression;
    }

    @Override
    public Assignment apply() {
        return underlyingExpression.apply(
                value.getColumn().getName(),
                (T) value.getColumn().asCassandraValue(value));
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

        final OperationImpl<?, ?> that = (OperationImpl<?, ?>) o;
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
