package org.nohope.cassandra.mapservice.cops;

import com.datastax.driver.core.querybuilder.Assignment;
import com.datastax.driver.core.querybuilder.BindMarker;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.nohope.cassandra.mapservice.Value;

import java.util.function.BiFunction;

/**
 *
 */
public final class COperations {
    private COperations() {
    }

    public static <T> Operation<T> set(final Value<T> value) {
        return new OperationImpl<>(value, QueryBuilder::set);
    }

    public static Operation<Long> counterIncr(final Value<Long> value) {
        if (value.getType() != Value.Type.BOUND) {
            return new OperationImpl<>(value, (BiFunction<String, BindMarker, Assignment>) QueryBuilder::incr);
        }
        return new OperationImpl<>(value, (BiFunction<String, Long, Assignment>) QueryBuilder::incr);
    }

    public static Operation<Long> counterDecr(final Value<Long> value) {
        if (value.getType() != Value.Type.BOUND) {
            return new OperationImpl<>(value, (BiFunction<String, BindMarker, Assignment>) QueryBuilder::decr);
        }
        return new OperationImpl<>(value, (BiFunction<String, Long, Assignment>) QueryBuilder::decr);
    }
}
