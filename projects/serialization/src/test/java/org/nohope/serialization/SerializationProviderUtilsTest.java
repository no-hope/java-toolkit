package org.nohope.serialization;

import org.junit.Test;
import org.nohope.serialization.streams.SerializationProvider;
import org.nohope.serialization.streams.SerializationProviderUtils;

import java.io.Serializable;

import static org.junit.Assert.assertEquals;


/**
 */
public class SerializationProviderUtilsTest {

    @Test
    public void cloneUsingByteArray() {
        final Serializable origin = new TestBean(1, 2L);
        SerializationTestUtils.withSerializers(
                new SerializationTestUtils.Executor() {
                    @Override
                    public void execute(final SerializationProvider provider) throws Throwable {
                        final byte[] bytes = SerializationProviderUtils.toByteArray(origin, provider);
                        final TestBean result = SerializationProviderUtils.fromByteArray(bytes, TestBean.class, provider);
                        assertEquals(result, origin);
                    }
                }
         );
    }
}
