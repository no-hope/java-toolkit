package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;

/**
 * Wrapper for cassandra trivial type blob
 */
public final class BigintTrivialType extends TrivialType<Long> {
    BigintTrivialType() {
        super(CType.BIGINT);
    }

    @Override
    public Long readValue(final Row result, final String name) {
        return result.getLong(name);
    }
}
