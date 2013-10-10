package org.nohope.akka;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.nohope.test.RandomUtils.nextString;
import static org.nohope.test.RandomUtils.nextUtcDateTime;
import static org.nohope.test.SerializationUtils.cloneJava;
import static org.nohope.test.SerializationUtils.cloneMongo;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 9/22/12 1:56 AM
 */
public class NamedWorkerMetadataTest {
    @Test
    public void equals() {
        final String identifier = nextString();
        final DateTime data = nextUtcDateTime();

        assertEquals(new NamedWorkerMetadata(identifier, data), new NamedWorkerMetadata(identifier, data));
        assertEquals(new NamedWorkerMetadata(identifier, data).hashCode(),
                     new NamedWorkerMetadata(identifier, data).hashCode());
    }

    @Test
    public void basicJavaSerialization() {
        final NamedWorkerMetadata origin = new NamedWorkerMetadata(nextString(),
                nextUtcDateTime());

        final NamedWorkerMetadata result = cloneJava(origin);
        assertEquals(origin.getData(), result.getData());
        assertEquals(origin.getIdentifier(), result.getIdentifier());
    }

    @Test
    public void basicMongoSerialization() {
        final NamedWorkerMetadata origin = new NamedWorkerMetadata(nextString(),
                nextUtcDateTime());

        final NamedWorkerMetadata result = cloneMongo(origin);
        assertEquals(origin.getData(), result.getData());
        assertEquals(origin.getIdentifier(), result.getIdentifier());
    }
}
