package org.nohope.akka;

import akka.actor.ActorRef;

/**
 * Date: 07.08.12
 * Time: 15:49
 */
public final class SupervisorRequests {
    private SupervisorRequests() {
    }

    public static class StartupRequest {

        private final ActorRef supervisor;

        public StartupRequest(final ActorRef supervisor) {
            this.supervisor = supervisor;
        }

        public ActorRef getSupervisor() {
            return supervisor;
        }
    }

    private static class BaseSupervisorRequest {
        protected final NamedActorIdentifier actorIdentifier;

        public BaseSupervisorRequest(final NamedActorIdentifier actorIdentifier) {
            this.actorIdentifier = actorIdentifier;
        }

        public NamedActorIdentifier getActorIdentifier() {
            return actorIdentifier;
        }
    }

    public static class StartupReply extends BaseSupervisorRequest {

        public StartupReply(final NamedActorIdentifier actorIdentifier) {
            super(actorIdentifier);
        }

    }
}
