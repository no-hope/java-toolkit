package org.nohope.akka;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;
import akka.pattern.AskTimeoutException;
import akka.testkit.TestActorRef;
import org.junit.Test;
import org.nohope.test.AkkaUtils;
import org.nohope.test.TRandom;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.nohope.akka.SupervisorRequests.StartupReply;

/**
 * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
 * @since 2013-10-10 17:26
 */
public class BaseSupervisorTest {
    private static class MyActor extends BaseWorkerActor {
        private MyActor(final NamedWorkerMetadata meta) {
            super(meta);
        }

        @OnReceive
        public Serializable getter(final Integer param) {
            return getWorkerMetadata();
        }
    }

    public static class Bean implements Serializable {
        private static final long serialVersionUID = 1L;

        final String a;
        final Integer b;

        public Bean(final String a, final Integer b) {
            this.a = a;
            this.b = b;
        }
    }

    private static class Supervisor extends BaseSupervisor {
        protected Supervisor(final boolean expandObjectArrays) {
            super(expandObjectArrays);
        }

        protected Supervisor() {
            super();
        }

        @OnReceive
        private ActorRef getExistingWorker(final Bean meta) {
            return obtainWorker(new NamedWorkerMetadata(meta.a, meta.b));
        }

        @Override
        protected Props newInputProps(final NamedWorkerMetadata inputClassId) {
            return new Props(new UntypedActorFactory() {
                @Override
                public Actor create() throws Exception {
                    return new MyActor(inputClassId);
                }
            });
        }
    }

    @Test
    public void supervisor() throws Exception {
        final List<Props> props = Arrays.asList(
                new Props(Supervisor.class),
                new Props(new UntypedActorFactory() {
                    @Override
                    public Actor create() throws Exception {
                        return new Supervisor(false);
                    }
                }));

        for (final Props prop : props) {
            final ActorSystem system =
                    AkkaUtils.buildSystem("test")
                             .put("actor.creation-timeout", "500ms")
                             .put("test.single-expect-default", "500ms")
                             .build();
            final TestActorRef ref = TestActorRef.apply(prop, system);

            final ActorRef actorRef = Ask.waitReply(ActorRef.class, ref, new Bean("a", 1));
            final NamedWorkerMetadata metadata = Ask.waitReply(NamedWorkerMetadata.class, actorRef, 0);
            assertNotNull(metadata);

            final ActorRef actorRef2 = Ask.waitReply(ActorRef.class, ref, new Bean("a", 1));
            assertEquals(actorRef, actorRef2);

            final String id = TRandom.standard().nextString();
            final String data = TRandom.standard().nextString();
            final NamedWorkerMetadata meta = new NamedWorkerMetadata(id, data);
            try {
                Ask.waitReply(ActorRef.class, ref, new StartupReply(meta), 500);
                fail();
            } catch (final Exception e) {
                assertTrue(e.getCause() instanceof AskTimeoutException);
            }

            ref.stop();
        }
    }
}
