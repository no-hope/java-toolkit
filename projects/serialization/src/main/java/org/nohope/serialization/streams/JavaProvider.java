package org.nohope.serialization.streams;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 */
public final class JavaProvider implements SerializationProvider {

    @Override
    public void writeObject(
            @Nonnull final OutputStream stream,
            @Nonnull final Serializable object) throws IOException {

        try (final ObjectOutput output = new ObjectOutputStream(stream)) {
            output.writeObject(object);
        }
    }

    @Override
    public <T extends Serializable> T readObject(
            @Nonnull final InputStream stream,
            @Nonnull final Class<T> clazz
    ) throws IOException {
        try (final ObjectInput output = new ObjectInputStream(stream)) {
            return clazz.cast(output.readObject());
        } catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }
    }
}
