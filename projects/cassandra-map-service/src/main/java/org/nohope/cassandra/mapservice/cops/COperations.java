package org.nohope.cassandra.mapservice.cops;

/**
 *
 */
public final class COperations {
    private COperations() {
    }

    public static Operation set(final String columnName, final Object value) {
        return new SetCOperation(columnName, value);
    }

    public static Operation counterIncr(final String columnName, final long value) {
        return new CounterUpdate(columnName, value);
    }
}
