package org.nohope.akka;

import org.junit.Test;
import org.nohope.test.TRandom;
import org.nohope.test.UtilitiesTestSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.nohope.akka.SupervisorRequests.StartupReply;
import static org.nohope.akka.SupervisorRequests.StartupRequest;
import static org.nohope.test.SerializationUtils.assertJavaClonedEquals;
import static org.nohope.test.SerializationUtils.assertMongoClonedEquals;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-10 22:21
 */
public class SupervisorRequestsTest extends UtilitiesTestSupport<SupervisorRequests> {

    @Override
    protected Class<SupervisorRequests> getUtilityClass() {
        return SupervisorRequests.class;
    }

    @Test
    public void serialization() {
        final String id = TRandom.standard().nextString();
        final String data = TRandom.standard().nextString();
        final NamedWorkerMetadata meta = new NamedWorkerMetadata(id, data);
        final StartupReply reply = new StartupReply(meta);
        final StartupReply cloned = assertJavaClonedEquals(reply);
        final StartupReply cloned2 = assertMongoClonedEquals(reply);

        assertEquals(meta, cloned.getWorkerMetadata());
        assertEquals(meta, cloned2.getWorkerMetadata());
        assertEquals(reply, cloned);
        assertEquals(cloned, cloned2);
        assertEquals(reply.hashCode(), cloned.hashCode());

        final StartupRequest request = new StartupRequest();
        final StartupRequest cloned3 = assertJavaClonedEquals(request);
        final StartupRequest cloned4 = assertMongoClonedEquals(request);

        assertEquals(request, cloned3);
        assertEquals(cloned3, cloned4);
        assertEquals(request.hashCode(), cloned3.hashCode());

        assertNotEquals(reply, null);
        assertEquals(reply, reply);
        assertNotEquals(reply, request);

        assertNotEquals(request, null);
        assertEquals(request, request);
        assertNotEquals(request, reply);
    }
}
