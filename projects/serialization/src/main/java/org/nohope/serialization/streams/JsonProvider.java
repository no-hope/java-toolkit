package org.nohope.serialization.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 */
public final class JsonProvider implements SerializationProvider {

    private final ObjectMapper mapper;

    public JsonProvider(@Nonnull final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonProvider() {
        this(new ObjectMapper());
    }

    @Override
    public void writeObject(@Nonnull final OutputStream stream, @Nonnull final Serializable object) throws IOException {
        mapper.writer().writeValue(stream, object);
    }

    @Override
    public <T extends Serializable> T readObject(@Nonnull final InputStream stream,
                                                  @Nonnull final Class<T> clazz) throws IOException {
        try {
            return mapper.readValue(stream, clazz);
        } catch (final IOException e) {
            throw new IOException(IOUtils.toString(stream), e);
        }
    }
}
