package org.nohope.serialization.streams;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 */
public final class GZipProvider implements SerializationProvider {

    private final SerializationProvider provider;

    public GZipProvider(@Nonnull final SerializationProvider provider) {
        this.provider = provider;
    }

    @Override
    public void writeObject(@Nonnull final OutputStream stream, @Nonnull final Serializable clazz) throws IOException {
        try (final GZIPOutputStream compressed = new GZIPOutputStream(stream)) {
            provider.writeObject(compressed, clazz);
        }
    }

    @Override
    public <T extends Serializable> T readObject(
            @Nonnull final InputStream stream,
            @Nonnull final Class<T> clazz
    ) throws IOException {
        try (final GZIPInputStream decompressed = new GZIPInputStream(stream)) {
            return provider.readObject(decompressed, clazz);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " (" + provider + ')';
    }
}
