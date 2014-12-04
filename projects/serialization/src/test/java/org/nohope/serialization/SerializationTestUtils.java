package org.nohope.serialization;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.nohope.serialization.streams.GZipProvider;
import org.nohope.serialization.streams.JavaProvider;
import org.nohope.serialization.streams.JsonProvider;
import org.nohope.serialization.streams.KryoProvider;
import org.nohope.serialization.streams.LZ4Provider;
import org.nohope.serialization.streams.SerializationProvider;
import org.nohope.serialization.streams.SerializationProviderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 */
public final class SerializationTestUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SerializationTestUtils.class);
    private static final List<SerializationProvider> DEFAULT_SERIALIZERS =
            Collections.unmodifiableList(getDefaultSerializers());

    private SerializationTestUtils() {
    }

    public static <T extends Serializable> void assertSerializable(final T origin, final SerializationProvider provider) {
        try {
            final T restored = SerializationProviderUtils.clone(origin, provider);
            if (!(origin instanceof Enum)) {
                Assert.assertNotSame(origin, restored);
            }
            Assert.assertEquals(
                    "Cloned instance of "
                            + origin.getClass().getCanonicalName()
                            + " is not equals to origin using "
                            + provider,
                    origin, restored
                               );
        } catch (final IOException e) {
            Assert.fail(ExceptionUtils.getStackTrace(e));
        }
    }

    public static void assertSerializable(final Serializable obj) {
        for (final SerializationProvider serializer : DEFAULT_SERIALIZERS) {
            assertSerializable(obj, serializer);
        }
    }

    public static List<SerializationProvider> getDefaultSerializers() {
        final List<SerializationProvider> providers = new CopyOnWriteArrayList<>();
        providers.add(new KryoProvider());
        providers.add(new JavaProvider());
        providers.add(new JsonProvider());

        for (final SerializationProvider provider : providers) {
            providers.add(new LZ4Provider(provider));
        }
        for (final SerializationProvider provider : providers) {
            providers.add(new GZipProvider(provider));
        }

        return providers;
    }

    public static void withSerializers(final Executor e) {
        for (final SerializationProvider serializer : DEFAULT_SERIALIZERS) {
            try {
                e.execute(serializer);
            } catch (final AssertionError ex) {
                throw ex;
            } catch (final Throwable t) {
                throw new AssertionError(t);
            }
        }
    }

    public static void assertEquality(final Object o1, final Object o2)  {
        Assert.assertEquals(o1, o1);
        Assert.assertEquals(o1, o2);
        Assert.assertEquals(o1.hashCode(), o2.hashCode());
        Assert.assertNotEquals(null, o1);
        Assert.assertNotEquals(new Object(), o1);
    }

    public static void assertClonedEquals(final Serializable o1)  {
        withSerializers(new Executor() {
            @Override
            public void execute(SerializationProvider provider) throws IOException {
                LOG.debug("asserting with {}", provider);
                assertEquality(o1, SerializationProviderUtils.clone(o1, provider));
            }
        });
    }

    public interface Executor {
        void execute(SerializationProvider provider) throws Throwable;
    }
}
