package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;

import java.util.UUID;

/**
 */
public final class UUIDTrivialType extends TrivialType<UUID> {

    UUIDTrivialType(final CType type) {
        super(type);
    }

    @Override
    public UUID readValue(final Row result, final String name) {
        return result.getUUID(name);
    }
}
