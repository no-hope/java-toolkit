package org.nohope.serialization.streams;

import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 */
public final class LZ4Provider implements SerializationProvider {

    private final SerializationProvider provider;

    public LZ4Provider(@Nonnull final SerializationProvider provider) {
        this.provider = provider;
    }

    @Override
    public void writeObject(
            @Nonnull final OutputStream stream, @Nonnull final Serializable clazz) throws IOException {
        try (final LZ4BlockOutputStream compressed = new LZ4BlockOutputStream(stream)) {
            provider.writeObject(compressed, clazz);
        }
    }

    @Override
    public <T extends Serializable> T readObject(
            @Nonnull final InputStream stream,
            @Nonnull final Class<T> clazz
    ) throws IOException {
        try (final LZ4BlockInputStream decompressed = new LZ4BlockInputStream(stream)) {
            return provider.readObject(decompressed, clazz);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " (" + provider + ')';
    }
}
