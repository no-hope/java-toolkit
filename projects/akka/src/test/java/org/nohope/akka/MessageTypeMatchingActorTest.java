package org.nohope.akka;

import akka.actor.ActorInitializationException;
import akka.actor.ActorSystem;
import akka.actor.InvalidMessageException;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import org.junit.Test;
import org.nohope.test.AkkaUtils;

import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("MethodMayBeStatic")
public class MessageTypeMatchingActorTest {
    public static class IllegalParent extends MessageTypeMatchingActor {
        @OnReceive public Integer one(final Integer param) {return param;}
        @OnReceive public Integer two(final Integer param) {return param;}
    }

    public static class LegalParent extends MessageTypeMatchingActor {
        @OnReceive public Integer one(final Integer param) {return param;}
    }

    // parent contains clashes methods
    public static class IllegalChild extends IllegalParent {
    }

    // child method clashes eith parent methods
    public static class IllegalChild2 extends LegalParent {
        @OnReceive public Integer two(final Integer param) {return param;}
    }

    @Test
    public void nullMessageReceiving() throws InterruptedException {
        final ActorSystem system = AkkaUtils.createLocalSystem("test");
        final TestActorRef ref = TestActorRef.create(system, new Props(LegalParent.class), "actor");
        try {
            ref.receive(null);
            fail();
        } catch (final InvalidMessageException e) {
        }
    }

    @Test
    public void clashingMethods() throws InterruptedException {
        final ActorSystem system =
                AkkaUtils.buildSystem("test")
                         .put("actor.creation-timeout", "500ms")
                         .put("test.single-expect-default", "500ms")
                         .build();
        final TestProbe probe = TestProbe.apply(system);
        {
            final TestActorRef ref = TestActorRef.apply(new Props(IllegalParent.class), system);
            ref.tell(1);
            probe.expectNoMsg();
            assertTrue(ref.isTerminated());
        }

        {
            final TestActorRef ref = TestActorRef.apply(new Props(IllegalChild.class), system);
            ref.tell(1);
            probe.expectNoMsg();
            assertTrue(ref.isTerminated());
        }

        {
            final TestActorRef ref = TestActorRef.apply(new Props(IllegalChild2.class), system);
            ref.tell(1);
            probe.expectNoMsg();
            assertTrue(ref.isTerminated());
        }
    }
}
