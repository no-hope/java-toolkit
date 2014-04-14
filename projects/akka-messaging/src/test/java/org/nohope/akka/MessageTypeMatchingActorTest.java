package org.nohope.akka;

import akka.actor.Actor;
import akka.actor.ActorSystem;
import akka.actor.InvalidMessageException;
import akka.actor.Props;
import akka.japi.Creator;
import akka.pattern.AskTimeoutException;
import akka.testkit.TestActorRef;
import akka.testkit.TestProbe;
import org.junit.Test;
import org.nohope.reflection.IntrospectionUtils;
import org.nohope.test.AkkaUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;
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
        @OnReceive public Integer one(final Integer param) {return param;}
    }

    public static class Handler1 implements Serializable {
        private static final long serialVersionUID = 1L;

        @OnReceive public Double two(final Double param) {return param;}
    }

    public static class Handler2 implements Serializable {
        private static final long serialVersionUID = 1L;

        @OnReceive public Double two(final Double param) {return param + 1;}
    }

    // parent contains clashes methods
    public static class IllegalChild extends IllegalParent {
    }

    // child method clashes with parent methods
    public static class IllegalChild2 extends LegalParent {
        @OnReceive public Integer two(final Integer param) {return param;}
    }

    public static class WideParent extends MessageTypeMatchingActor {
        @OnReceive public Integer one(final Number param) {return param.intValue();}
    }

    public static class NarrowChild extends WideParent {
        @OnReceive public Integer one(final Integer param) {return param + 1;}
    }

    public static class NarrowParent extends MessageTypeMatchingActor {
        @OnReceive public Integer one(final Integer param) {return param;}
    }

    public static class WideChild extends NarrowParent {
        @OnReceive public Integer one(final Number param) {return param.intValue() + 1;}
    }

    @Test
    public void wideNarrow() throws Exception {
        final ActorSystem system = AkkaUtils.createLocalSystem("test");

        final TestActorRef ref1 = TestActorRef.create(system, Props.create(NarrowChild.class), "actor1");
        assertEquals(2, Ask.waitReply(Number.class, ref1, 1));

        final TestActorRef ref2 = TestActorRef.create(system, Props.create(WideChild.class), "actor2");
        assertEquals(1, Ask.waitReply(Number.class, ref2, 1));
    }

    private static class HierComparator implements Comparator<Method>, Serializable {
        private final Class<? extends Actor> actorClass;
        private final Class<?> messageClass;

        private HierComparator(final Class<? extends Actor> actorClass,
                               final Class<?> messageClass) {
            this.actorClass = actorClass;
            this.messageClass = messageClass;
        }

        @Override
        public int compare(final Method o1, final Method o2) {
            final int m1Class = depth(actorClass, o1.getDeclaringClass());
            final int m2Class = depth(actorClass, o2.getDeclaringClass());
            final int actorCmp = Integer.compare(m1Class, m2Class);


            if (actorCmp != 0) {
                return actorCmp;
            }

            final int m1Msg = depth(messageClass, o1.getParameterTypes()[0]);
            final int m2Msg = depth(messageClass, o2.getParameterTypes()[1]);
            return Integer.compare(m1Msg, m2Msg);
        }

        private static int depth(final Class<?> that, final Class<?> other) {
            final Class<?> higher;
            final Class<?> lower;
            if (IntrospectionUtils.instanceOf(that, other)) {
                lower = that;
                higher = other;
            } else {
                higher = that;
                lower = other;
            }

            Class<?> parent = lower;
            int depth = 0;
            while (parent != null && !higher.equals(parent)) {
                depth++;
                parent = parent.getSuperclass();
            }

            return depth;
        }
    }

    public static class ErrorCatchingActor extends MessageTypeMatchingActor {
        private final AtomicReference<Exception> error = new AtomicReference<>();
        private final AtomicBoolean falg = new AtomicBoolean();

        @OnReceive public Serializable two(final String param) {
            if (param != null && param.isEmpty()) {
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
        final TestActorRef ref = TestActorRef.create(system, Props.create(ErrorCatchingActor.class), "actor1");

        try {
            Ask.waitReply(String.class, ref, "", 500);
            fail();
        } catch (final Exception e) {
            assertTrue(e.getCause() instanceof AskTimeoutException);
        }

        assertTrue(Ask.waitReply(ref, "getError") instanceof IllegalStateException);
        assertTrue((Boolean) Ask.waitReply(ref, "getFlag"));
    }

    @Test
    public void nullMessageReceiving() throws InterruptedException {
        final ActorSystem system = AkkaUtils.createLocalSystem("test");
        final TestActorRef ref = TestActorRef.create(system, Props.create(LegalParent.class), "actor");
        try {
            ref.receive(null);
            fail();
        } catch (final InvalidMessageException ignored) {
        }
    }

    @Test
    public void handlers() throws Exception {
        final ActorSystem system = AkkaUtils.createLocalSystem("test");
        final TestActorRef ref1 = TestActorRef.create(system, Props.create(new LegalChildWithHandlersCreator()), "actor1");

        assertEquals(1d, Ask.waitReply(Double.class, ref1, 1d), 0.01);

        final TestActorRef ref01 = TestActorRef.create(system, Props.create(new LegalChildWithHandlersCreator2()), "actor01");
        assertEquals(1d, Ask.waitReply(Double.class, ref01, 1d), 0.01);

        final TestActorRef ref2 = TestActorRef.create(system, Props.create(new LegalChildWithHandlersCreator3()), "actor2");
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
            final TestActorRef ref = TestActorRef.apply(Props.create(IllegalParent.class), system);
            ref.tell(1, ref);
            probe.expectNoMsg();
            assertTrue(ref.isTerminated());
        }

        {
            final TestActorRef ref = TestActorRef.apply(Props.create(IllegalChild.class), system);
            ref.tell(1, ref);
            probe.expectNoMsg();
            assertTrue(ref.isTerminated());
        }

        {
            final TestActorRef ref = TestActorRef.apply(Props.create(IllegalChild2.class), system);
            ref.tell(1, ref);
            probe.expectNoMsg();
            assertTrue(ref.isTerminated());
        }
    }

    private static class LegalChildWithHandlersCreator implements Creator<LegalChildWithHandlers> {
        private static final long serialVersionUID = 1;

        @Override
        public LegalChildWithHandlers create() throws Exception {
            final LegalChildWithHandlers child = new LegalChildWithHandlers();
            child.addHandlers(new Handler1(), new Handler2());
            return child;
        }
    }

    private static class LegalChildWithHandlersCreator2 implements Creator<LegalChildWithHandlers> {
        private static final long serialVersionUID = 1;

        @Override
        public LegalChildWithHandlers create() throws Exception {
            final LegalChildWithHandlers child = new LegalChildWithHandlers();
            child.setHandlers(new Handler1(), new Handler2());
            return child;
        }
    }

    private static class LegalChildWithHandlersCreator3 implements Creator<LegalChildWithHandlers> {
        private static final long serialVersionUID = 1;

        @Override
        public LegalChildWithHandlers create() throws Exception {
            final LegalChildWithHandlers child = new LegalChildWithHandlers();
            child.addHandlers(new Handler2(), new Handler1());
            return child;
        }
    }
}
