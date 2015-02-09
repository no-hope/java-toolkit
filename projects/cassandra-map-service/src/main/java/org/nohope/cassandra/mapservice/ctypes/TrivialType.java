package org.nohope.cassandra.mapservice.ctypes;

import org.nohope.cassandra.mapservice.CTypeConverter;

import java.nio.ByteBuffer;
import java.util.Date;

public abstract class TrivialType<T> extends CTypeConverter<T, T> {
    public static final TrivialType<String> ASCII = new TextTrivialType(CType.ASCII);
    public static final TrivialType<Long> BIGINT = new BigintTrivialType();
    public static final TrivialType<ByteBuffer> BLOB = new BlobTrivialType();
    public static final TrivialType<Long> COUNTER = new CounterTrivialType();
    public static final TrivialType<String> TEXT = new TextTrivialType(CType.TEXT);
    public static final TrivialType<Date> TIMESTAMP = new TimestampTrivialType();
    public static final TrivialType<java.util.UUID> UUID = new UUIDTrivialType(CType.UUID);
    public static final TrivialType<String> VARCHAR = new TextTrivialType(CType.VARCHAR);
    public static final TrivialType<java.util.UUID> TIMEUUID = new UUIDTrivialType(CType.TIMEUUID);

    private final CType ctype;

    TrivialType(final CType ctype) {
        this.ctype = ctype;
    }

    @Override
    public final CType getCType() {
        return ctype;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        final TrivialType<?> that = (TrivialType<?>) o;
        return ctype == that.ctype;
    }

    @Override
    public int hashCode() {
        return ctype.hashCode();
    }

    @Override
    protected T convert(final T value) {
        return value;
    }
}
