package org.nohope.serialization.streams;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 */
public interface SerializationProvider {
    /**
     * Writes object to a given output stream.
     * Stream will be closed after method execution.
     */
    void writeObject(
            @Nonnull final OutputStream stream,
            @Nonnull Serializable object
                    ) throws IOException;

    /**
     * Reads object from a given input stream.
     * Stream will be closed after method execution.
     */
    <T extends Serializable> T readObject(
            @Nonnull InputStream stream,
            @Nonnull Class<T> clazz
                                         ) throws IOException;
}
