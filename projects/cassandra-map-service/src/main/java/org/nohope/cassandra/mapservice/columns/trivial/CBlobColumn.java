package org.nohope.cassandra.mapservice.columns.trivial;

import org.nohope.cassandra.mapservice.columns.CColumn;
import org.nohope.cassandra.mapservice.ctypes.CoreConverter;

import java.nio.ByteBuffer;

/**
 */
public final class CBlobColumn extends CColumn<ByteBuffer, ByteBuffer> {

    CBlobColumn(final String name) {
        super(name, CoreConverter.BLOB);
    }

    public static CBlobColumn of(final String name) {
        return new CBlobColumn(name);
    }
}
