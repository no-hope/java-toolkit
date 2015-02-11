package org.nohope.cassandra.mapservice.cops;

/**
 *
 */
public final class COperations {
    private COperations() {
    }

    public static <T> Operation<T> set(final String columnName, final T value) {
        return new SetCOperation<>(columnName, value);
    }

    public static Operation<Long> counterIncr(final String columnName, final long value) {
        return new CounterUpdate(columnName, value);
    }
}
