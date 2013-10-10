package org.nohope.akka;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;
import akka.testkit.TestActorRef;
import org.junit.Test;

import java.io.Serializable;

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
        final ActorSystem system = org.nohope.test.AkkaUtils.createLocalSystem("test");
        final TestActorRef ref = TestActorRef.apply(new Props(Supervisor.class), system);

        final ActorRef actorRef = Ask.waitReply(ActorRef.class, ref, new Bean("a", 1));
        final NamedWorkerMetadata metadata = Ask.waitReply(NamedWorkerMetadata.class, actorRef, 0);

        System.err.println(metadata);
    }
}
