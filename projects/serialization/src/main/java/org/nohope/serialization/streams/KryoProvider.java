package org.nohope.serialization.streams;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 */
public final class KryoProvider implements SerializationProvider {
    private final KryoFactory kryoFactory;

    // not a bug: kryo instance is not thread-safe, thus should be thread-local
    @SuppressWarnings("ThreadLocalNotStaticFinal")
    private final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return kryoFactory.newKryo();
        }
    };

    public KryoProvider() {
        this(KryoFactory.DEFAULT);
    }

    public KryoProvider(@Nonnull final KryoFactory kryoFactory) {
        this.kryoFactory = kryoFactory;
    }

    @Override
    public void writeObject(
            @Nonnull final OutputStream stream, @Nonnull final Serializable object) {
        try (final Output output = new Output(stream)) {
            kryoThreadLocal.get().writeClass(output, object.getClass());
            kryoThreadLocal.get().writeObject(output, object);
        }
    }

    @Override
    public <T extends Serializable> T readObject(@Nonnull final InputStream stream,
                                                 @Nonnull final Class<T> clazz) {
        try (final Input input = new Input(stream)) {
            final Class<?> restoredClass = kryoThreadLocal.get().readClass(input).getType();
            return clazz.cast(kryoThreadLocal.get().readObject(input, restoredClass));
        }
    }
}
