package org.nohope.cassandra.mapservice.ctypes;

import com.datastax.driver.core.Row;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import org.nohope.serialization.streams.SerializationProvider;
import org.nohope.serialization.streams.SerializationProviderUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

import static java.nio.ByteBuffer.wrap;

/**
 */
public final class CListSerializableType<T extends Serializable> extends CListType<T, String> {

    private final SerializationProvider provider;

    CListSerializableType(final Class<T> clazz, final SerializationProvider provider) {
        super(clazz, CType.TEXT);
        this.provider = provider;
    }

    public static <T extends Serializable> CListSerializableType<T> of(final Class<T> clazz,
                                                                       final SerializationProvider provider) {
        return new CListSerializableType<>(clazz, provider);
    }

    @Override
    public List<T> readValue(final Row result, final String name) {
        try {
            return fromCassandra(result.getList(name, String.class));
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String convert(final List<T> value) {
        try {
            return new String(wrap(SerializationProviderUtils.toByteArray((Serializable) value, provider)).array(), Charsets.UTF_8);
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private List<T> fromCassandra(final Iterable<String> values) throws IOException {
        final Charset charset = Charset.forName("UTF-8");
        final CharsetEncoder encoder = charset.newEncoder();
        final List<T> listToReturn = new ArrayList<>();
        for (final String value : values) {
            final ByteBuffer buffer = encoder.encode(CharBuffer.wrap(value));
            final byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            listToReturn.add(SerializationProviderUtils.fromByteArray(bytes, super.getClazz(), provider));
        }
        return listToReturn;
    }
}
