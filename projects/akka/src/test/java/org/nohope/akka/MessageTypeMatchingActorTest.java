package org.nohope.akka;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.actor.InvalidMessageException;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;
import akka.pattern.AskTimeoutException;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import org.junit.Test;
import org.nohope.test.AkkaUtils;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@SuppressWarnings("MethodMayBeStatic")
public class MessageTypeMatchingActorTest {
    public static class IllegalParent extends MessageTypeMatchingActor {
        @OnReceive public Integer one(final Integer param) {return param;}
        @OnReceive public Integer two(final Integer param) {return param;}
    }

    public static class LegalParent extends MessageTypeMatchingActor {
        @OnReceive public Integer one(final Integer param) {return param;}
    }

    public static class LegalChildWithHandlers extends MessageTypeMatchingActor {
        public LegalChildWithHandlers() {
        }

        @OnReceive public Integer one(final Integer param) {return param;}
    }

    public static class Handler1 implements Serializable {
        private static final long serialVersionUID = 2089491553140241315L;

        @OnReceive public Double two(final Double param) {return param;}
    }

    public static class Handler2 implements Serializable {
        private static final long serialVersionUID = 2089491553140241315L;

        @OnReceive public Double two(final Double param) {return param + 1;}
    }

    // parent contains clashes methods
    public static class IllegalChild extends IllegalParent {
    }

    // child method clashes eith parent methods
    public static class IllegalChild2 extends LegalParent {
        @OnReceive public Integer two(final Integer param) {return param;}
    }

    public static class ErrorCatchingActor extends MessageTypeMatchingActor {
        private final AtomicReference<Exception> error = new AtomicReference<>();
        private final AtomicBoolean falg = new AtomicBoolean();

        @OnReceive public Serializable two(final String param) {
            if ("".equals(param)) {
                throw new IllegalStateException("");
            }
            if ("getError".equals(param)) {
                return error.get();
            }
            if ("getFlag".equals(param)) {
                return falg.get();
            }
            return param;
        }

        @Override
        protected void onReceiveError(final Exception e, final Object message) {
            super.onReceiveError(e, message);
            error.set(e);
        }

        @Override
        protected void postReceive(final Object response) {
            falg.set(true);
        }
    }

    @Test
    public void errorCatching() throws Exception {
        final ActorSystem system = AkkaUtils.createLocalSystem("test");
        final TestActorRef ref = TestActorRef.create(system, new Props(ErrorCatchingActor.class), "actor1");

        try {
            Ask.waitReply(String.class, ref, "", 500);
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof AskTimeoutException);
        }

        assertTrue(Ask.waitReply(ref, "getError") instanceof IllegalStateException);
        assertTrue((Boolean) Ask.waitReply(ref, "getFlag"));
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
    public void handlers() throws Exception {
        final ActorSystem system = AkkaUtils.createLocalSystem("test");
        final TestActorRef ref1 = TestActorRef.create(system, new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 3890337666530891766L;

            @Override
            public Actor create() throws Exception {
                final LegalChildWithHandlers child = new LegalChildWithHandlers();
                child.addHandlers(new Handler1(), new Handler2());
                return child;
            }
        }), "actor1");

        assertEquals(1d, Ask.waitReply(Double.class, ref1, 1d), 0.01);

        final TestActorRef ref01 = TestActorRef.create(system, new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = 3890337666530891766L;

            @Override
            public Actor create() throws Exception {
                final LegalChildWithHandlers child = new LegalChildWithHandlers();
                child.setHandlers(new Handler1(), new Handler2());
                return child;
            }
        }), "actor01");

        assertEquals(1d, Ask.waitReply(Double.class, ref01, 1d), 0.01);

        final TestActorRef ref2 = TestActorRef.create(system, new Props(new UntypedActorFactory() {
            private static final long serialVersionUID = -6999529383860449265L;

            @Override
            public Actor create() throws Exception {
                final LegalChildWithHandlers child = new LegalChildWithHandlers();
                child.addHandlers(new Handler2(), new Handler1());
                return child;
            }
        }), "actor2");

        assertEquals(2d, Ask.waitReply(Double.class, ref2, 1d), 0.01);
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
            ref.tell(1, ref);
            probe.expectNoMsg();
            assertTrue(ref.isTerminated());
        }

        {
            final TestActorRef ref = TestActorRef.apply(new Props(IllegalChild.class), system);
            ref.tell(1, ref);
            probe.expectNoMsg();
            assertTrue(ref.isTerminated());
        }

        {
            final TestActorRef ref = TestActorRef.apply(new Props(IllegalChild2.class), system);
            ref.tell(1, ref);
            probe.expectNoMsg();
            assertTrue(ref.isTerminated());
        }
    }
}
