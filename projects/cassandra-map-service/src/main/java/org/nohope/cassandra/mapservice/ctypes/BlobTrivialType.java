package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;

import java.nio.ByteBuffer;

/**
 * Wrapper for cassandra trivial type blob
 */
public final class BlobTrivialType extends TrivialType<ByteBuffer> {
    BlobTrivialType() {
        super(CType.BLOB);
    }

    @Override
    public ByteBuffer readValue(final Row result, final String name) {
        return result.getBytes(name);
    }
}
