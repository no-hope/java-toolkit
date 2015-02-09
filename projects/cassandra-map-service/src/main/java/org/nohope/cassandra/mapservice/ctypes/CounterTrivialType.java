package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;

/**
 */
public final class CounterTrivialType extends TrivialType<Long> {
    CounterTrivialType() {
        super(CType.COUNTER);
    }

    @Override
    public Long readValue(final Row result, final String name) {
        return result.getLong(name);
    }
}
