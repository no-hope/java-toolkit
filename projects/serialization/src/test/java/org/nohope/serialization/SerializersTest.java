package org.nohope.serialization;


import org.junit.Test;
import org.nohope.serialization.streams.SerializationProvider;
import org.nohope.serialization.streams.SerializationProviderUtils;

import static org.junit.Assert.*;

/**
 */
public class SerializersTest {

    @Test
    public void simpleBeanSerialization() {
        SerializationTestUtils.assertClonedEquals(new TestBean(1, 2L));
        SerializationTestUtils.withSerializers(new SerializationTestUtils.Executor() {
            @Override
            public void execute(final SerializationProvider provider) throws Throwable {
                assertNotNull(provider.toString());
            }
        });
    }


    @Test
    public void illegalClassRead() {
        SerializationTestUtils.withSerializers(new SerializationTestUtils.Executor() {
            @Override
            public void execute(SerializationProvider provider) throws Throwable {
                final byte[] serialized = SerializationProviderUtils.toByteArray(new TestBean(1, 2L), provider);

                try {
                    SerializationProviderUtils.fromByteArray(serialized, String.class, provider);
                    fail();
                } catch (Exception ignored) {
                }
            }
        });
    }
}
