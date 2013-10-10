package org.nohope.akka;

import akka.actor.ActorSystem;
import akka.actor.InvalidMessageException;
import akka.actor.Props;
import akka.pattern.AskTimeoutException;
import akka.testkit.TestActorRef;
import org.junit.Test;
import org.nohope.test.UtilitiesTestSupport;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
 * @since 10/17/12 9:13 PM
 */
@SuppressWarnings("MethodMayBeStatic")
public class AskTest extends UtilitiesTestSupport {

    @Override
    protected Class<?> getUtilityClass() {
        return Ask.class;
    }

    public static class EchoActor extends MessageTypeMatchingActor {
        @OnReceive
        public Integer echo(final Integer param) {
            return param;
        }

        @OnReceive
        public Object echo(final String param) {
            if ("fail".equals(param)) {
                return new Object() {
                    @Override
                    public String toString() {
                        return "'invalid object'";
                    }
                };
            }
            return null;
        }
    }

    @Test
    public void castingTest() throws Exception {
        final ActorSystem system = org.nohope.test.AkkaUtils.createLocalSystem("test");
        final TestActorRef ref = TestActorRef.apply(new Props(EchoActor.class), system);
        assertEquals(123, (int) Ask.waitReply(Integer.class, ref, 123));
        assertEquals(125, Ask.waitReply(ref, 125));

        try {
            Ask.waitReply(String.class, ref, 123);
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof ClassCastException);
        }

        try {
            Ask.waitReply(Object.class, ref, "fail");
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof InvalidMessageException);
            assertEquals("Message 'invalid object' must implement java.io.Serializable",
                    e.getCause().getMessage());
        }

        try {
            Ask.waitReply(Object.class, ref, "null");
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof AskTimeoutException);
        }
    }
}
